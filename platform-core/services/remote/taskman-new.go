package remote

import (
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/network"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

const (
	// 获取编排关联模版
	pathImportRequestTemplate = "/taskman/api/v1/workflow/request?procInstanceId=%s"
)

func QueryAssociationRequest(procInstanceId, userToken, language string) (result []*models.SimpleRequestDto, err error) {
	var byteArr []byte
	var response models.RequestResponse
	uri := models.Config.Gateway.Url + fmt.Sprintf(pathImportRequestTemplate, procInstanceId)
	if models.Config.HttpsEnable == "true" {
		uri = "https://" + uri
	} else {
		uri = "http://" + uri
	}
	if byteArr, err = network.HttpGet(uri, userToken, language); err != nil {
		return
	}
	if err = json.Unmarshal(byteArr, &response); err != nil {
		return
	}
	if response.StatusCode != "OK" {
		err = fmt.Errorf("QueryAssociationRequest fail,%+v", response.StatusMessage)
		return
	}
	result = response.Data
	return
}
