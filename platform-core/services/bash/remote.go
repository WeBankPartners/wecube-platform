package bash

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"os/exec"
	"strconv"
	"strings"
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

func GetRemoteHostAvailablePort(targetIp string) (port int, err error) {
	commandString := fmt.Sprintf("sshpass -p '%s' ssh %s@%s -p %s 'netstat -ltnp|awk \"{print $4}\"|grep \":2\"'", models.Config.StaticResource.Password, models.Config.StaticResource.User, targetIp, models.Config.StaticResource.Port)
	output, execErr := exec.Command("/bin/bash", "-c", commandString).Output()
	if execErr != nil {
		err = fmt.Errorf("run remote ssh command to get available port target %s fail,%s ", targetIp, execErr.Error())
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

func CreatePluginDatabase(username string, mysqlResource *models.PluginPackageRuntimeResourcesMysql) (err error) {

	return
}
