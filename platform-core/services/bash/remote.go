package bash

import (
	"context"
	"crypto/rand"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	_ "github.com/go-sql-driver/mysql"
	"os/exec"
	"strconv"
	"strings"
	"xorm.io/xorm"
)

func RemoteSSHCommand(targetIp, command string) (err error) {
	commandString := fmt.Sprintf("sshpass -p '%s' ssh %s@%s -p %s '%s'", models.Config.StaticResource.Password, models.Config.StaticResource.User, targetIp, models.Config.StaticResource.Port, command)
	_, err = exec.Command("/bin/bash", "-c", commandString).Output()
	if err != nil {
		err = fmt.Errorf("run remote ssh command to target %s fail,%s ", targetIp, err.Error())
	}
	return
}

func RemoteSCP(targetIp, localFile, targetPath string) (err error) {
	commandString := fmt.Sprintf("sshpass -p '%s' scp -P %s %s %s@%s:%s", models.Config.StaticResource.Password, models.Config.StaticResource.Port, localFile, models.Config.StaticResource.User, targetIp, targetPath)
	_, err = exec.Command("/bin/bash", "-c", commandString).Output()
	if err != nil {
		err = fmt.Errorf("scp file %s to target %s fail,%s ", localFile, targetIp, err.Error())
	}
	return
}

func isStaticResource(targetIp string) bool {
	matchFlag := false
	for _, v := range strings.Split(models.Config.StaticResource.Servers, ",") {
		if v == targetIp {
			matchFlag = true
			break
		}
	}
	return matchFlag
}

func GetRemoteHostAvailablePort(resourceServer *models.ResourceServer) (port int, err error) {
	commandString := fmt.Sprintf("sshpass -p '%s' ssh %s@%s -p %s 'netstat -ltnp|awk \"{print $4}\"|grep \":2\"'", resourceServer.LoginPassword, resourceServer.LoginUsername, resourceServer.Host, resourceServer.Port)
	output, execErr := exec.Command("/bin/bash", "-c", commandString).Output()
	if execErr != nil {
		err = fmt.Errorf("run remote ssh command to get available port target %s fail,%s ", resourceServer.Host, execErr.Error())
		return
	}
	existPortLines := strings.Split(string(output), "\n")
	existPortMap := make(map[int]int)
	for _, v := range existPortLines {
		if splitIndex := strings.LastIndex(v, ":"); splitIndex >= 0 {
			if tmpPort, _ := strconv.Atoi(v[splitIndex+1:]); tmpPort > 0 {
				existPortMap[tmpPort] = 1
			}
		}
	}
	for i := 20000; i <= 21000; i++ {
		if _, b := existPortMap[i]; !b {
			port = i
			break
		}
	}
	return
}

func CreatePluginDatabase(ctx context.Context, username string, mysqlResource *models.PluginPackageRuntimeResourcesMysql, mysqlServer *models.ResourceServer) (password string, err error) {
	connStr := fmt.Sprintf("%s:%s@%s(%s)/?collation=utf8mb4_unicode_ci&allowNativePasswords=true",
		mysqlServer.LoginUsername, mysqlServer.LoginPassword, "tcp", fmt.Sprintf("%s:%s", mysqlServer.Host, mysqlServer.Port))
	engine, connectErr := xorm.NewEngine("mysql", connStr)
	if connectErr != nil {
		err = fmt.Errorf("try to connect to mysql resource server fail,%s ", connectErr.Error())
		return
	}
	session := engine.NewSession().Context(ctx)
	session.Begin()
	defer session.Close()
	queryResult, queryErr := session.QueryString("show databases")
	if queryErr == nil {
		existFlag := false
		for _, v := range queryResult {
			if v["Database"] == mysqlResource.SchemaName {
				existFlag = true
			}
		}
		if existFlag {
			log.Logger.Info("CreatePluginDatabase break,database already exists", log.String("database", mysqlResource.SchemaName))
			session.Commit()
			return
		}
	} else {
		err = fmt.Errorf("try to query database list fail,%s ", queryErr.Error())
		session.Rollback()
		return
	}
	if _, err = session.Exec(fmt.Sprintf("CREATE DATABASE %s", mysqlResource.SchemaName)); err != nil {
		session.Rollback()
		return
	}
	b := make([]byte, 16)
	rand.Read(b)
	password = fmt.Sprintf("%x", b[4:8])
	if _, err = session.Exec(fmt.Sprintf("CREATE USER '%s'@'%%' IDENTIFIED BY '%s'", username, password)); err != nil {
		session.Rollback()
		return
	}
	if _, err = session.Exec(fmt.Sprintf("GRANT ALL PRIVILEGES ON %s.* TO '%s'@'%%'", mysqlResource.SchemaName, username)); err != nil {
		session.Rollback()
		return
	}
	if _, err = session.Exec("flush privileges"); err != nil {
		session.Rollback()
		return
	}
	session.Commit()
	return
}

func ExecPluginUpgradeSql(ctx context.Context, mysqlInstance *models.PluginMysqlInstances, mysqlServer *models.ResourceServer, sqlFilePath string) (err error) {
	connStr := fmt.Sprintf("%s:%s@%s(%s)/%s?collation=utf8mb4_unicode_ci&allowNativePasswords=true",
		mysqlInstance.Username, mysqlInstance.Password, "tcp", fmt.Sprintf("%s:%s", mysqlServer.Host, mysqlServer.Port), mysqlInstance.SchemaName)
	engine, connectErr := xorm.NewEngine("mysql", connStr)
	if connectErr != nil {
		err = fmt.Errorf("try to connect to mysql resource server fail,%s ", connectErr.Error())
		return
	}
	session := engine.NewSession().Context(ctx)
	session.Begin()
	if _, err = session.Exec("source " + sqlFilePath); err != nil {
		session.Rollback()
	} else {
		session.Commit()
	}
	session.Close()
	return
}
