package api_platform

import (
	"encoding/json"
	"fmt"

	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/network"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
)

const (
	// pathQuerySystemVariables 注册用户
	pathQuerySystemVariables = "/platform/v1/system-variables/retrieve"
)

// QuerySystemVariables 查询系统参数
func QuerySystemVariables(userToken, language string, param *model.QueryRequestParam) (result *model.PlatSystemVariablesListPageData, err error) {
	postBytes, err := json.Marshal(param)
	if err != nil {
		return
	}
	byteArr, err := network.HttpPost(model.Config.RemoteConfig.PlatformUrl+pathQuerySystemVariables, userToken, language, postBytes)
	if err != nil {
		return
	}
	var response model.ResponseWrap
	if err = json.Unmarshal(byteArr, &response); err != nil {
		err = fmt.Errorf("json unmarhsal response body fail,%s ", err.Error())
		return
	}
	if response.Status != "OK" {
		err = fmt.Errorf(response.Message)
		return
	}
	dataBytes, err := json.Marshal(response.Data)
	if err != nil {
		return
	}
	result = &model.PlatSystemVariablesListPageData{}
	err = json.Unmarshal(dataBytes, result)
	if err != nil {
		return
	}
	return
}
