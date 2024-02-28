package bash

import (
	"bytes"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/encrypt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/tools"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"os"
	"os/exec"
	"regexp"
	"strings"
	"time"
)

func GetHostSerialNum(hostIp string) {

}

func SaveTmpFile(fileName string, fileContent []byte) (filePath, fileDir string, err error) {
	if fileDir, err = newTmpDir(); err != nil {
		return
	}
	if fileName == "" {
		fileName = "tmp_" + guid.CreateGuid()
	}
	filePath = fmt.Sprintf("%s/%s", fileDir, fileName)
	if err = os.WriteFile(filePath, fileContent, 0644); err != nil {
		err = fmt.Errorf("write tmp file fail,%s ", err.Error())
	}
	return
}

func newTmpDir() (fileDir string, err error) {
	fileDir = fmt.Sprintf("/tmp/%d", time.Now().UnixNano())
	if err = os.MkdirAll(fileDir, 0777); err != nil {
		err = fmt.Errorf("make tmp dir fail,%s ", err.Error())
	}
	return
}

func DecompressFile(filePath, targetDir string) (localDir string, err error) {
	var fileDir, fileName, fileType, execCommand string
	localDir = targetDir
	if dirIndex := strings.LastIndex(filePath, "/"); dirIndex >= 0 {
		fileDir = filePath[:dirIndex]
		fileName = filePath[dirIndex+1:]
	} else {
		fileName = filePath
	}
	tmpFp := strings.ToLower(fileName)
	if lastPointIndex := strings.LastIndex(tmpFp, "."); lastPointIndex > 0 {
		fileType = tmpFp[lastPointIndex+1:]
	}
	if fileType == "" {
		err = fmt.Errorf("fileName: %s type illegal", fileName)
		return
	}
	if fileDir != "" {
		execCommand = "cd " + fileDir + " && "
		if localDir == "" {
			localDir = fileDir
		}
	}
	if fileType == "zip" {
		execCommand += "unzip " + fileName
		if targetDir != "" {
			execCommand += " -d " + targetDir
		}
	}
	_, execErr := exec.Command("/bin/bash", "-c", execCommand).Output()
	if execErr != nil {
		err = fmt.Errorf("exec decompress file fail,command:%s,error:%s ", execCommand, execErr.Error())
	}
	return
}

func ListDirFiles(dirPath string) (result []string, err error) {
	dirEntry, readErr := os.ReadDir(dirPath)
	if readErr != nil {
		err = fmt.Errorf("read dir %s fail,%s ", dirPath, readErr.Error())
		return
	}
	for _, v := range dirEntry {
		if !v.IsDir() {
			result = append(result, v.Name())
		}
	}
	return
}

func ListContains(inputList []string, target string) bool {
	existFlag := false
	for _, v := range inputList {
		if v == target {
			existFlag = true
			break
		}
	}
	return existFlag
}

func BuildPluginUpgradeSqlFile(initSqlFile, upgradeSqlFile, currentVersion string) (outputFile string, err error) {
	buff := new(bytes.Buffer)
	re, _ := regexp.Compile("^#@(.*)-begin@;$")
	if initSqlFile != "" {
		fileBytes, readErr := os.ReadFile(initSqlFile)
		if readErr != nil {
			err = fmt.Errorf("read file error,%s ", err.Error())
			return
		}
		if currentVersion == "" {
			buff.Write(fileBytes)
		} else {
			startFlag := false
			for _, v := range strings.Split(string(fileBytes), "\n") {
				if !startFlag {
					if matchList := re.FindStringSubmatch(strings.TrimSpace(v)); len(matchList) > 1 {
						if tools.CompareVersion(matchList[1], currentVersion) {
							startFlag = true
						}
					}
					if !startFlag {
						continue
					}
				}
				lineValue := strings.ReplaceAll(v, "\r", "")
				buff.WriteString(lineValue + "\n")
			}
		}
	}
	if upgradeSqlFile != "" {
		fileBytes, readErr := os.ReadFile(upgradeSqlFile)
		if readErr != nil {
			err = fmt.Errorf("read file error,%s ", err.Error())
			return
		}
		startFlag := false
		for _, v := range strings.Split(string(fileBytes), "\n") {
			if !startFlag {
				if matchList := re.FindStringSubmatch(strings.TrimSpace(v)); len(matchList) > 1 {
					if tools.CompareVersion(matchList[1], currentVersion) {
						startFlag = true
					}
				}
				if !startFlag {
					continue
				}
			}
			buff.WriteString(strings.ReplaceAll(v, "\r", "") + "\n")
		}
	}

	if buff.Len() > 0 {
		outputFile, _, err = SaveTmpFile(fmt.Sprintf("tmp_%s.sql", guid.CreateGuid()), buff.Bytes())
	}
	return
}

func GetDirIndexPath(targetDir string) (subPath string, matchFlag bool, err error) {
	dirEntries, readDirErr := os.ReadDir(targetDir)
	if readDirErr != nil {
		err = fmt.Errorf("read path %s fail,%s ", targetDir, readDirErr.Error())
		return
	}
	for _, v := range dirEntries {
		if !v.IsDir() && v.Name() == "index.html" {
			matchFlag = true
		}
	}
	if matchFlag {
		return
	}
	for _, v := range dirEntries {
		if v.IsDir() {
			tmpSubPath, tmpMatch, subErr := GetDirIndexPath(targetDir + "/" + v.Name())
			if subErr != nil {
				err = subErr
				break
			}
			if tmpMatch {
				subPath = v.Name() + "/" + tmpSubPath
				matchFlag = true
				break
			}
		}
	}
	return
}

func ListDirAllFiles(targetDir string) (resultPaths []string, err error) {
	dirEntries, readDirErr := os.ReadDir(targetDir)
	if readDirErr != nil {
		err = fmt.Errorf("read path %s fail,%s ", targetDir, readDirErr.Error())
		return
	}
	for _, v := range dirEntries {
		if v.IsDir() {
			subResults, subErr := ListDirAllFiles(targetDir + "/" + v.Name())
			if subErr != nil {
				err = subErr
				break
			}
			for _, subPath := range subResults {
				resultPaths = append(resultPaths, subPath)
			}
		} else {
			resultPaths = append(resultPaths, targetDir+"/"+v.Name())
		}
	}
	return
}

func InitPluginDockerHostSSH() {
	var resourceServers []*models.ResourceServer
	err := db.MysqlEngine.SQL("select name,host,login_password,login_username,port,login_mode from resource_server where `type`='docker'").Find(&resourceServers)
	if err != nil {
		log.Logger.Error("init plugin docker ssh fail", log.Error(err))
		return
	}
	for _, row := range resourceServers {
		if strings.HasPrefix(row.LoginPassword, models.AESPrefix) {
			row.LoginPassword = encrypt.DecryptWithAesECB(row.LoginPassword[5:], models.Config.Plugin.ResourcePasswordSeed, row.Name)
		}
		exec.Command("/bin/bash", "-c", fmt.Sprintf("sshpass -p '%s' ssh -o StrictHostKeyChecking=no %s@%s -p %s", row.LoginPassword, row.LoginUsername, row.Host, row.Port)).Output()
		log.Logger.Info("init plugin docker ssh", log.String("server", row.Host))
	}
	for _, staticResource := range models.Config.StaticResources {
		exec.Command("/bin/bash", "-c", fmt.Sprintf("sshpass -p '%s' ssh -o StrictHostKeyChecking=no %s@%s -p %s", staticResource.Password, staticResource.User, staticResource.Server, staticResource.Port)).Output()
		log.Logger.Info("init platform docker ssh", log.String("server", staticResource.Server))
	}
}
