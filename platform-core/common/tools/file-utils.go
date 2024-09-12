package tools

import (
	"archive/zip"
	"encoding/json"
	"fmt"
	"io"
	"os"
	"path/filepath"
)

func GetPath(path string) string {
	exist, err := pathExist(path)
	if err != nil {
		panic(err)
	}
	if !exist {
		err := os.Mkdir(path, 0755)
		if err != nil {
			panic(err)
		}
	}
	return path
}

func pathExist(path string) (bool, error) {
	_, err := os.Stat(path)
	if err == nil {
		return true, nil
	}
	if os.IsNotExist(err) {
		return false, nil
	}
	return false, err
}

func CreateZipCompressAndUpload(dirPath string) (url string, err error) {
	// 创建一个新的zip文件
	var result []*NexusUploadFileRet
	var zipFile *os.File
	zipFile, err = os.Create("export.zip")
	if err != nil {
		panic(err)
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
		_, err = io.Copy(zipFile, file)
		if err != nil {
			return err
		}

		return nil
	})
	if err != nil {
		panic(err)
	}
	uploadReqParam := &NexusReqParam{
		UserName:   "xx",
		Password:   "xx",
		RepoUrl:    "http://127.0.0.1:8081/repository",
		Repository: "test",
		TimeoutSec: 60,
		FileParams: []*NexusFileParam{
			{
				SourceFilePath: "/Users/apple/Downloads/ecies.zip",
				DestFilePath:   "ecies/ecies.zip",
			},
		},
	}

	if result, err = UploadFile(uploadReqParam); err != nil {
		return
	}
	fmt.Println(result)
	return
}

func WriteJsonData2File(path string, inter interface{}) (err error) {
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
