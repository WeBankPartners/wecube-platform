package remote

import (
	"bufio"
	"context"
	"fmt"
	"io"
	"net/http"
	"os"
	"strings"

	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

func GetOnliePluginPackageList(ctx context.Context) (result []*models.OnlinePackage, err error) {
	uri := models.Config.Plugin.PublicReleaseUrl + "public-plugin-artifacts.release"
	req, reqErr := http.NewRequest(http.MethodGet, uri, nil)
	if reqErr != nil {
		err = fmt.Errorf("new request fail,%s ", reqErr.Error())
		return
	}
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("do request fail,%s ", respErr.Error())
		return
	}
	respBody, readBodyErr := io.ReadAll(resp.Body)
	if readBodyErr != nil {
		err = fmt.Errorf("read response body fail,%s ", readBodyErr.Error())
		return
	}
	resp.Body.Close()
	reader := bufio.NewScanner(strings.NewReader(string(respBody)))
	for reader.Scan() {
		line := strings.TrimRight(reader.Text(), "\n\r")
		result = append(result, &models.OnlinePackage{BucketName: line, KeyName: line})
	}
	// 检查是否有错误发生
	if errRead := reader.Err(); errRead != nil {
		err = errRead
		return
	}
	return
}

func GetOnlinePluginPackageFile(ctx context.Context, fileName string) (result *os.File, err error) {
	uri := models.Config.Plugin.PublicReleaseUrl + fileName
	req, reqErr := http.NewRequest(http.MethodGet, uri, nil)
	if reqErr != nil {
		err = fmt.Errorf("new request fail,%s ", reqErr.Error())
		return
	}
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("do request fail,%s ", respErr.Error())
		return
	}
	if resp.StatusCode != 200 {
		err = fmt.Errorf("do request fail, status code=%d", resp.StatusCode)
		return
	}
	defer resp.Body.Close()
	result, err = os.CreateTemp("", "*-"+fileName)
	// 将响应体内容写入临时文件
	_, err = io.Copy(result, resp.Body)
	if err != nil {
		err = fmt.Errorf("read response body fail,%s ", err.Error())
		os.Remove(result.Name())
		return
	}
	result.Sync()
	return
}
