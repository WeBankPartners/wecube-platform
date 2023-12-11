package bash

import (
	"fmt"
	"os"
	"time"
)

func GetHostSerialNum(hostIp string) {

}

func SaveTmpFile(fileName string, fileContent []byte) (filePath, fileDir string, err error) {
	tmpDir := fmt.Sprintf("/tmp/%d", time.Now().UnixNano())
	if err = os.MkdirAll(tmpDir, 0644); err != nil {
		err = fmt.Errorf("make tmp dir fail,%s ", err.Error())
		return
	}
	filePath = fmt.Sprintf("%s/%s", tmpDir, fileName)
	if err = os.WriteFile(filePath, fileContent, 0644); err != nil {
		err = fmt.Errorf("write tmp file fail,%s ", err.Error())
	}
	return
}

func DecompressFile(filePath string) {

}
