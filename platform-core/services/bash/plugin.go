package bash

import (
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"os"
	"os/exec"
	"strings"
)

func GetHostSerialNum(hostIp string) {

}

func SaveTmpFile(fileName string, fileContent []byte) (filePath, fileDir string, err error) {
	if fileDir, err = newTmpDir(); err != nil {
		return
	}
	filePath = fmt.Sprintf("%s/%s", fileDir, fileName)
	if err = os.WriteFile(filePath, fileContent, 0644); err != nil {
		err = fmt.Errorf("write tmp file fail,%s ", err.Error())
	}
	return
}

func newTmpDir() (dirPath string, err error) {
	fileDir := fmt.Sprintf("/tmp/%s", guid.CreateGuid())
	if err = os.MkdirAll(fileDir, 0644); err != nil {
		err = fmt.Errorf("make tmp dir fail,%s ", err.Error())
	}
	return
}

func DecompressFile(filePath, targetDir string) error {
	var fileDir, fileName, fileType, execCommand string
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
		return fmt.Errorf("fileName: %s type illegal", fileName)
	}
	if fileDir != "" {
		execCommand = "cd " + fileDir + " && "
	}
	if fileType == "zip" {
		execCommand += "unzip " + fileName
		if targetDir != "" {
			execCommand += " -d " + targetDir
		}
	}
	_, execErr := exec.Command("/bin/sh", "-c", execCommand).Output()
	if execErr != nil {
		return fmt.Errorf("exec decompress file fail,command:%s,error:%s ", execCommand, execErr.Error())
	}
	return nil
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
