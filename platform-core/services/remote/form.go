package remote

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"io"
	"net/http"
	"net/url"
)

func GetInputFormMeta(ctx context.Context, procInstId, nodeDefId string, pluginInterface *models.PluginConfigInterfaces) (result *models.TaskMetaResultData, err error) {
	uri := fmt.Sprintf("%s%s/meta", models.Config.Gateway.Url, pluginInterface.Path)
	if models.Config.HttpsEnable == "true" {
		uri = "https://" + uri
	} else {
		uri = "http://" + uri
	}
	urlParams := url.Values{}
	urlParams.Set("procInstId", procInstId)
	urlParams.Set("nodeDefId", nodeDefId)
	urlObj, _ := url.Parse(uri)
	urlObj.RawQuery = urlParams.Encode()
	req, reqErr := http.NewRequest(http.MethodGet, urlObj.String(), nil)
	if reqErr != nil {
		err = fmt.Errorf("new request fail,%s ", reqErr.Error())
		return
	}
	reqId := "req_" + guid.CreateGuid()
	transId := ctx.Value(models.TransactionIdHeader).(string)
	req.Header.Set(models.RequestIdHeader, reqId)
	req.Header.Set(models.TransactionIdHeader, transId)
	req.Header.Set(models.AuthorizationHeader, GetToken())
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("do request fail,%s ", respErr.Error())
		return
	}
	var response models.TaskMetaResult
	respBody, readBodyErr := io.ReadAll(resp.Body)
	if readBodyErr != nil {
		err = fmt.Errorf("read response body fail,%s ", readBodyErr.Error())
		return
	}
	resp.Body.Close()
	if err = json.Unmarshal(respBody, &response); err != nil {
		err = fmt.Errorf("json unmarshal response body fail,%s ", err.Error())
		return
	}
	if response.Status != models.DefaultHttpSuccessCode {
		err = fmt.Errorf(response.Message)
		return
	}
	result = &response.Data
	return
}

func CallPluginCustomForm() {

}
