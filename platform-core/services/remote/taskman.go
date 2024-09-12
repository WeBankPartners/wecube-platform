package remote

import (
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/network"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

const (
	// pathRequestTemplateRoles 查询taskman模版角色
	pathRequestTemplateRoles = "/taskman/api/v1/request-template/roles"
	// pathRequestTemplates 查询taskman模版列表
	pathRequestTemplates = "/taskman/api/v1/request-template/export/batch"
)

// GetRequestTemplateRoles 查询taskman模版角色
func GetRequestTemplateRoles(dto models.GetRequestTemplateRolesDto, userToken, language string) (response models.QueryRequestTemplateRolesResponse, err error) {
	var byteArr []byte
	postBytes, _ := json.Marshal(dto)
	uri := models.Config.Gateway.Url + pathRequestTemplateRoles
	if models.Config.HttpsEnable == "true" {
		uri = "https://" + uri
	} else {
		uri = "http://" + uri
	}
	byteArr, err = network.HttpPost(uri, userToken, language, postBytes)
	if err != nil {
		return
	}
	err = json.Unmarshal(byteArr, &response)
	if err != nil {
		err = fmt.Errorf("try to json unmarshal response body fail,%s ", err.Error())
	}
	return
}

// GetRequestTemplates 查询taskman模版列表
func GetRequestTemplates(dto models.GetRequestTemplatesDto, userToken, language string) (response models.QueryRequestTemplatesResponse, err error) {
	var byteArr []byte
	postBytes, _ := json.Marshal(dto)
	uri := models.Config.Gateway.Url + pathRequestTemplates
	if models.Config.HttpsEnable == "true" {
		uri = "https://" + uri
	} else {
		uri = "http://" + uri
	}
	byteArr, err = network.HttpPost(uri, userToken, language, postBytes)
	if err != nil {
		return
	}
	err = json.Unmarshal(byteArr, &response)
	if err != nil {
		err = fmt.Errorf("try to json unmarshal response body fail,%s ", err.Error())
	}
	return
}
