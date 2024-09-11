package remote

import (
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/network"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

const (
	// pathRequestTemplateRoles 查询taskman模版角色
	pathRequestTemplateRoles = "/taskman/api/v1/request-template/roles "
)

// GetRequestTemplateRoles 查询taskman模版角色
func GetRequestTemplateRoles(dto models.GetRequestTemplateRolesDto, userToken, language string) (response models.QueryRequestTemplateRolesResponse, err error) {
	var byteArr []byte
	postBytes, _ := json.Marshal(dto)
	byteArr, err = network.HttpPost(models.Config.Gateway.Url+pathRequestTemplateRoles, userToken, language, postBytes)
	if err != nil {
		return
	}
	err = json.Unmarshal(byteArr, &response)
	if err != nil {
		err = fmt.Errorf("try to json unmarshal response body fail,%s ", err.Error())
	}
	return
}
