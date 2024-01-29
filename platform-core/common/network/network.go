package network

import (
	"bytes"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"io"
	"net/http"
	"strings"
)

// HttpGet  Get请求
func HttpGet(url, userToken, language string) (byteArr []byte, err error) {
	req, newReqErr := http.NewRequest(http.MethodGet, url, strings.NewReader(""))
	if newReqErr != nil {
		err = fmt.Errorf("Try to new http request fail,%s ", newReqErr.Error())
		return
	}
	req.Header.Set("Authorization", userToken)
	req.Header.Set("Accept-Language", language)
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("Try to do http request fail,%s ", respErr.Error())
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
		err = fmt.Errorf("do http reqeust fail,%s ", reqErr.Error())
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
		err = fmt.Errorf("do http reqeust fail,%s ", reqErr.Error())
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
		err = fmt.Errorf("Try to new http request fail,%s ", newReqErr.Error())
		return
	}
	req.Header.Set("Authorization", userToken)
	req.Header.Set("Accept-Language", language)
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("Try to do http request fail,%s ", respErr.Error())
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
