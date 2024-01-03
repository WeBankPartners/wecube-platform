package remote

import (
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/network"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
)

const (
	httpAuthServer = "http://localhost:9090"
	// pathRetrieveAllUserAccounts 查询所有用户
	pathRetrieveAllUserAccounts = "/auth/v1/user"
	// pathRetrieveAllRoles  查询所有角色
	pathRetrieveAllRoles = "/auth/v1/roles?all=%s"
	// pathRetrieveGrantedRolesByUsername 根据用户名查询角色
	pathRetrieveGrantedRolesByUsername = "/auth/v1/users/roles-by-name/%s"
	// pathRetrieveRoleById 根据roleId查询role
	pathRetrieveRoleById = "/auth/v1/roles/%s"
)

// TODO
func RegisterSubSystem(pluginPackageObj *models.PluginPackages) (subSystemCode, subSystemKey string, err error) {

	return
}

// RetrieveAllUsers 获取所有用户
func RetrieveAllUsers(c *gin.Context) (response models.GetAllUserResponse, err error) {
	byteArr, err := network.HttpGet(httpAuthServer+pathRetrieveAllUserAccounts, c.GetHeader("Authorization"))
	if err != nil {
		return
	}
	err = json.Unmarshal(byteArr, &response)
	if err != nil {
		err = fmt.Errorf("Try to json unmarshal response body fail,%s ", err.Error())
		return
	}
	return
}

// RetrieveAllLocalRoles 查询所有角色
func RetrieveAllLocalRoles(c *gin.Context, requiredAll string) (response models.QueryRolesResponse, err error) {
	url := fmt.Sprintf(httpAuthServer+pathRetrieveAllRoles, requiredAll)
	byteArr, err := network.HttpGet(url, c.GetHeader("Authorization"))
	if err != nil {
		return
	}
	err = json.Unmarshal(byteArr, &response)
	if err != nil {
		err = fmt.Errorf("Try to json unmarshal response body fail,%s ", err.Error())
		return
	}
	return
}

// GetRolesByUsername 根据用户名获取角色
func GetRolesByUsername(c *gin.Context, username string) (response models.QueryRolesResponse, err error) {
	url := fmt.Sprintf(httpAuthServer+pathRetrieveGrantedRolesByUsername, username)
	byteArr, err := network.HttpGet(url, c.GetHeader("Authorization"))
	if err != nil {
		return
	}
	err = json.Unmarshal(byteArr, &response)
	if err != nil {
		err = fmt.Errorf("Try to json unmarshal response body fail,%s ", err.Error())
		return
	}
	return
}

// RetrieveRoleInfo 根据roleId获取角色
func RetrieveRoleInfo(c *gin.Context, roleId string) (response models.QueryRolesResponse, err error) {
	url := fmt.Sprintf(httpAuthServer+pathRetrieveRoleById, roleId)
	byteArr, err := network.HttpGet(url, c.GetHeader("Authorization"))
	if err != nil {
		return
	}
	err = json.Unmarshal(byteArr, &response)
	if err != nil {
		err = fmt.Errorf("Try to json unmarshal response body fail,%s ", err.Error())
		return
	}
	return
}
