package main

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"io/ioutil"
	"net/http"
	"os"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
)

type NexusReqParam struct {
	UserName   string            `json:"userName"`   // 用户名
	Password   string            `json:"password"`   // 密码
	RepoUrl    string            `json:"repoUrl"`    // repo 的 url: http://127.0.0.1:8081/repository
	Repository string            `json:"repository"` // repo 名称: test
	TimeoutSec int64             `json:"timeoutSec"` // 超时时间, 单位:s
	FileParams []*NexusFileParam `json:"fileParams"` // 文件的参数列表
}

type NexusFileParam struct {
	SourceFilePath string `json:"sourceFilePath"` // 源文件的路径
	DestFilePath   string `json:"destFilePath"`   // 目标文件的路径
}

func validateNexusReqParam(reqParam *NexusReqParam, isUploadFile bool) (err error) {
	if isUploadFile {
		if reqParam.RepoUrl == "" {
			err = fmt.Errorf("repoUrl should not be empty")
			return
		}
		if reqParam.Repository == "" {
			err = fmt.Errorf("repository should not be empty")
			return
		}
	}

	if reqParam.UserName == "" {
		err = fmt.Errorf("userName should not be empty")
		return
	}
	if reqParam.Password == "" {
		err = fmt.Errorf("password should not be empty")
		return
	}
	if reqParam.TimeoutSec <= 0 {
		err = fmt.Errorf("timeoutSec should > 0")
		return
	}

	if len(reqParam.FileParams) == 0 {
		err = fmt.Errorf("fileParams should not be empty")
		return
	}
	for i, fileParam := range reqParam.FileParams {
		if fileParam.SourceFilePath == "" {
			err = fmt.Errorf("%d th fileParam.SourceFilePath should not be empty", i+1)
			return
		}
		if fileParam.DestFilePath == "" {
			err = fmt.Errorf("%d th fileParam.DestFilePath should not be empty", i+1)
			return
		}
	}
	return
}

func UploadFile(reqParam *NexusReqParam) (err error) {
	err = validateNexusReqParam(reqParam, true)
	if err != nil {
		err = fmt.Errorf("validate nexus req param failed: %s", err.Error())
		return
	}

	for _, uploadFileParam := range reqParam.FileParams {
		tmpErr := doUploadFile(reqParam, uploadFileParam)
		if tmpErr != nil {
			err = fmt.Errorf("doUploadFile for %s failed: %s", uploadFileParam.SourceFilePath, tmpErr.Error())
			return
		}
	}
	return
}

func DownloadFile(reqParam *NexusReqParam) (err error) {
	err = validateNexusReqParam(reqParam, false)
	if err != nil {
		err = fmt.Errorf("validate nexus req param failed: %s", err.Error())
		return
	}

	for _, downloadFileParam := range reqParam.FileParams {
		tmpErr := doDownloadFile(reqParam, downloadFileParam)
		if tmpErr != nil {
			err = fmt.Errorf("doDownloadFile for %s failed: %s", downloadFileParam.SourceFilePath, tmpErr.Error())
			return
		}
	}
	return
}

func doUploadFile(reqParam *NexusReqParam, uploadFileParam *NexusFileParam) (err error) {
	srcFilePath := uploadFileParam.SourceFilePath
	log.Logger.Info(fmt.Sprintf("start to upload file: %s", srcFilePath))

	// 打开文件
	file, tmpErr := os.Open(srcFilePath)
	if tmpErr != nil {
		err = fmt.Errorf("open file: %s failed: %s", srcFilePath, tmpErr.Error())
		return
	}
	defer file.Close()

	// 创建 HTTP 请求
	reqUrl := fmt.Sprintf("%s/%s/%s", reqParam.RepoUrl, reqParam.Repository, uploadFileParam.DestFilePath)
	ctx, cancelFunc := context.WithTimeout(context.Background(), time.Duration(reqParam.TimeoutSec)*time.Second)
	defer cancelFunc()

	req, tmpErr := http.NewRequestWithContext(ctx, http.MethodPut, reqUrl, file)
	if tmpErr != nil {
		err = fmt.Errorf("create request for reqUrl: %s failed: %s", reqUrl, tmpErr.Error())
		return
	}

	req.SetBasicAuth(reqParam.UserName, reqParam.Password)
	req.Header.Set("Content-Type", "application/octet-stream") // 设置内容类型

	// 发送请求
	resp, tmpErr := http.DefaultClient.Do(req)
	if tmpErr != nil {
		err = fmt.Errorf("do request: %s failed: %s", reqUrl, tmpErr.Error())
		return
	}
	bodyBytes, _ := ioutil.ReadAll(resp.Body)
	resp.Body.Close()

	// 检查响应状态
	if resp.StatusCode != http.StatusOK && resp.StatusCode != http.StatusCreated {
		err = fmt.Errorf("upload file: %s failed, resp.Status: %s", srcFilePath, resp.Status)
		return
	}

	log.Logger.Info(fmt.Sprintf("upload file: %s successfully: %s", srcFilePath, bodyBytes))
	return
}

func doDownloadFile(reqParam *NexusReqParam, downloadFileParam *NexusFileParam) (err error) {
	srcFilePath := downloadFileParam.SourceFilePath
	log.Logger.Info(fmt.Sprintf("start to download file: %s", srcFilePath))

	// 创建 HTTP 请求
	ctx, cancelFunc := context.WithTimeout(context.Background(), time.Duration(reqParam.TimeoutSec)*time.Second)
	defer cancelFunc()

	reqUrl := srcFilePath
	req, tmpErr := http.NewRequestWithContext(ctx, http.MethodGet, reqUrl, nil)
	if tmpErr != nil {
		err = fmt.Errorf("create request for reqUrl: %s failed: %s", reqUrl, tmpErr.Error())
		return
	}

	req.SetBasicAuth(reqParam.UserName, reqParam.Password)

	// 发送请求
	resp, tmpErr := http.DefaultClient.Do(req)
	if tmpErr != nil {
		err = fmt.Errorf("do request: %s failed: %s", reqUrl, tmpErr.Error())
		return
	}
	bodyBytes, _ := ioutil.ReadAll(resp.Body)
	resp.Body.Close()

	// 检查响应状态
	if resp.StatusCode != http.StatusOK {
		err = fmt.Errorf("download file: %s failed, resp.Status: %s", srcFilePath, resp.Status)
		return
	}

	// 创建本地文件
	destFilePath := downloadFileParam.DestFilePath
	out, tmpErr := os.Create(destFilePath)
	if tmpErr != nil {
		err = fmt.Errorf("create output file: %s failed: %s", destFilePath, tmpErr.Error())
		return
	}
	defer out.Close()

	// 将响应内容写入本地文件
	_, tmpErr = io.Copy(out, bytes.NewReader(bodyBytes))
	if tmpErr != nil {
		err = fmt.Errorf("copy content to output file: %s failed: %s", destFilePath, tmpErr.Error())
		return
	}

	log.Logger.Info(fmt.Sprintf("download file: %s successfully", srcFilePath))
	return
}

// for test
/*
func main() {
	uploadReqParam := &NexusReqParam{
		UserName:   "admin",
		Password:   "admin",
		RepoUrl:    "http://127.0.0.1:8081/repository",
		Repository: "test",
		TimeoutSec: 60,
		FileParams: []*NexusFileParam{
			{
				SourceFilePath: "/Users/apple/Downloads/ecies.zip",
				DestFilePath:   "ecies/ecies.zip",
			},
			{
				SourceFilePath: "/Users/apple/Downloads/pic.png",
				DestFilePath:   "pic/pic.png",
			},
		},
	}

	err := UploadFile(uploadReqParam)
	if err != nil {
		fmt.Printf("upload files failed: %s", err.Error())
		return
	}
	fmt.Printf("upload files successfully")

	downloadReqParam := &NexusReqParam{
		UserName:   "admin",
		Password:   "admin",
		RepoUrl:    "http://127.0.0.1:8081/repository",
		Repository: "test",
		TimeoutSec: 60,
		FileParams: []*NexusFileParam{
			{
				SourceFilePath: "http://127.0.0.1:8081/repository/test/ecies/ecies.zip",
				DestFilePath:   "/tmp/ecies.zip",
			},
			{
				SourceFilePath: "http://127.0.0.1:8081/repository/test/pic/pic.png",
				DestFilePath:   "/tmp/pic.png",
			},
		},
	}

	err = DownloadFile(downloadReqParam)
	if err != nil {
		fmt.Printf("download files failed: %s", err.Error())
		return
	}
	fmt.Printf("download files successfully")
	return
}
*/
