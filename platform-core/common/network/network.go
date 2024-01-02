package network

import (
	"fmt"
	"io/ioutil"
	"net/http"
	"strings"
)

// HttpGet  Get请求
func HttpGet(url, userToken string) (byteArr []byte, err error) {
	req, newReqErr := http.NewRequest(http.MethodGet, url, strings.NewReader(""))
	if newReqErr != nil {
		err = fmt.Errorf("Try to new http request fail,%s ", newReqErr.Error())
		return
	}
	req.Header.Set("Authorization", userToken)
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("Try to do http request fail,%s ", respErr.Error())
		return
	}
	byteArr, _ = ioutil.ReadAll(resp.Body)
	defer resp.Body.Close()
	return
}
