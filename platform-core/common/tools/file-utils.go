package tools

import (
	"archive/zip"
	"encoding/json"
	"fmt"
	"io"
	"os"
	"path/filepath"
)

func GetPath(path string) (newPath string, err error) {
	var exist bool
	if exist, err = PathExist(path); err != nil {
		return newPath, err
	}
	if !exist {
		if err = os.MkdirAll(path, 0755); err != nil {
			return
		}
	}
	newPath = path
	return
}

func PathExist(path string) (bool, error) {
	_, err := os.Stat(path)
	if err == nil {
		return true, nil
	}
	if os.IsNotExist(err) {
		return false, nil
	}
	return false, err
}

func CreateZipCompress(zipPath, dirPath, fileName string) (err error) {
	// 创建一个新的zip文件
	var zipFile *os.File
	if zipFile, err = os.Create(fmt.Sprintf("%s/%s", zipPath, fileName)); err != nil {
		return
	}
	defer zipFile.Close()

	// 创建一个新的zip.Writer
	zipWriter := zip.NewWriter(zipFile)
	defer zipWriter.Close()
	// 遍历目录
	err = filepath.Walk(dirPath, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}
		// 如果info是一个目录，我们不需要写入任何内容，所以我们只是返回nil
		if info.IsDir() {
			return nil
		}
		// 创建一个新的zip.File
		zipFile, err := zipWriter.Create(filepath.Base(path))
		if err != nil {
			return err
		}
		// 打开文件
		file, err := os.Open(path)
		if err != nil {
			return err
		}
		defer file.Close()

		// 将文件内容复制到zipFile
		if _, err = io.Copy(zipFile, file); err != nil {
			return err
		}
		return nil
	})
	return
}

func WriteJsonData2File(path string, inter interface{}) (err error) {
	if inter == nil {
		return
	}
	var file *os.File
	// 打开文件
	if file, err = os.Create(path); err != nil {
		return
	}
	defer file.Close()

	// 创建json编码器
	encoder := json.NewEncoder(file)

	// 将json实例编码到文件
	if err = encoder.Encode(inter); err != nil {
		return
	}
	return
}
