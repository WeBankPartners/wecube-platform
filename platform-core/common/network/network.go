package network

import (
	"bytes"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"go.uber.org/zap"
	"io"
	"mime/multipart"
	"net/http"
	"os"
	"strings"
)

// HttpGet  Get请求
func HttpGet(url, userToken, language string) (byteArr []byte, err error) {
	req, newReqErr := http.NewRequest(http.MethodGet, url, strings.NewReader(""))
	if newReqErr != nil {
		err = fmt.Errorf("try to new http request fail,%s ", newReqErr.Error())
		return
	}
	req.Header.Set("Authorization", userToken)
	req.Header.Set("Accept-Language", language)
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("try to do http request fail,%s ", respErr.Error())
		return
	}
	byteArr, _ = io.ReadAll(resp.Body)
	defer resp.Body.Close()
	return
}

// HttpPost Post请求,关注返回结果
func HttpPost(url, userToken, language string, postBytes []byte) (byteArr []byte, err error) {
	req, reqErr := http.NewRequest(http.MethodPost, url, bytes.NewReader(postBytes))
	if reqErr != nil {
		err = fmt.Errorf("new http reqeust fail,%s ", reqErr.Error())
		return
	}
	req.Header.Set("Authorization", userToken)
	req.Header.Set("Accept-Language", language)
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("do http reqeust fail,%s ", respErr.Error())
		return
	}
	byteArr, _ = io.ReadAll(resp.Body)
	defer resp.Body.Close()
	return
}

// HttpPostCommon Post请求,通用返回处理
func HttpPostCommon(url, userToken, language string, postBytes []byte) (err error) {
	req, reqErr := http.NewRequest(http.MethodPost, url, bytes.NewReader(postBytes))
	if reqErr != nil {
		err = fmt.Errorf("new http reqeust fail,%s ", reqErr.Error())
		return
	}
	req.Header.Set("Authorization", userToken)
	req.Header.Set("Accept-Language", language)
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("do http reqeust fail,%s ", respErr.Error())
		return
	}
	respBytes, _ := io.ReadAll(resp.Body)
	var response models.CommonGatewayResp
	if err = json.Unmarshal(respBytes, &response); err != nil {
		err = fmt.Errorf("json unmarhsal response body fail,%s ", err.Error())
		return
	}
	if response.Status != "OK" {
		err = fmt.Errorf(response.Message)
	}
	return
}

// HttpDeleteCommon http Delete
func HttpDeleteCommon(url, userToken, language string) (err error) {
	req, newReqErr := http.NewRequest(http.MethodDelete, url, strings.NewReader(""))
	if newReqErr != nil {
		err = fmt.Errorf("try to new http request fail,%s ", newReqErr.Error())
		return
	}
	req.Header.Set("Authorization", userToken)
	req.Header.Set("Accept-Language", language)
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("try to do http request fail,%s ", respErr.Error())
		return
	}
	respBytes, _ := io.ReadAll(resp.Body)
	var response models.CommonGatewayResp
	if err = json.Unmarshal(respBytes, &response); err != nil {
		err = fmt.Errorf("json unmarhsal response body fail,%s ", err.Error())
		return
	}
	if response.Status != "OK" {
		err = fmt.Errorf(response.Message)
	}
	return
}

// HttpPostJsonFile  上传文件
func HttpPostJsonFile(filePath, url, userToken, language string) (uploadData []byte, err error) {
	var file *os.File
	var part io.Writer
	var req *http.Request
	var resp *http.Response
	if file, err = os.Open(filePath); err != nil {
		return
	}
	defer file.Close()

	body := &bytes.Buffer{}
	writer := multipart.NewWriter(body)
	if part, err = writer.CreateFormFile("file", "a.json"); err != nil {
		return
	}
	if _, err = io.Copy(part, file); err != nil {
		return
	}
	if err = writer.Close(); err != nil {
		return
	}
	if req, err = http.NewRequest(http.MethodPost, url, body); err != nil {
		return
	}
	req.Header.Set("Content-Type", writer.FormDataContentType())
	req.Header.Set("Authorization", userToken)
	req.Header.Set("Accept-Language", language)
	client := &http.Client{}
	if resp, err = client.Do(req); err != nil {
		return
	}
	defer resp.Body.Close()
	if uploadData, err = io.ReadAll(resp.Body); err != nil {
		return
	}
	log.Info(nil, log.LOGGER_APP, "HttpPostJsonFile response", zap.String("response", string(uploadData)))
	return
}
