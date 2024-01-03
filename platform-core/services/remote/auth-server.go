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
	// pathRetrieveAllUsersBelongsToRoleId 查询角色用户
	pathRetrieveAllUsersBelongsToRoleId = "/auth/v1/roles/%s/users"
	// pathConfigureRolesForUser 修改用户角色
	pathConfigureRolesForUser = "/auth/v1/users/%s/roles"
	// pathUserChangePassword 修改用户密码
	pathUserChangePassword = "/auth/v1/users/change-password"
	// pathUserResetPassword 重置用户密码
	pathUserResetPassword = "/auth/v1/users/reset-password"
)

// TODO
func RegisterSubSystem(pluginPackageObj *models.PluginPackages) (subSystemCode, subSystemKey string, err error) {

	return
}

// RetrieveAllUsers 获取所有用户
func RetrieveAllUsers(c *gin.Context) (response models.QueryUserResponse, err error) {
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

// GetUsersByRoleId 返回角色用户列表
func GetUsersByRoleId(c *gin.Context, roleId string) (response models.QueryUserResponse, err error) {
	url := fmt.Sprintf(httpAuthServer+pathRetrieveAllUsersBelongsToRoleId, roleId)
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

// ConfigureUserWithRoles 修改用户角色
func ConfigureUserWithRoles(c *gin.Context, userId string, rolesList []string) (err error) {
	var postParams []*models.SimpleLocalRoleDto
	for _, role := range rolesList {
		postParams = append(postParams, &models.SimpleLocalRoleDto{ID: role})
	}
	postBytes, _ := json.Marshal(postParams)
	err = network.HttpPostCommon(fmt.Sprintf(httpAuthServer+pathConfigureRolesForUser, userId), c.GetHeader("Authorization"), postBytes)
	return
}

// ModifyLocalUserPassword 修改密码
func ModifyLocalUserPassword(c *gin.Context, param models.UserPasswordChangeParam, username string) (response models.QueryUserResponse, err error) {
	var byteArr []byte
	userPassDto := &models.SimpleLocalUserPassDto{
		Username:         username,
		OriginalPassword: param.OriginalPassword,
		ChangedPassword:  param.NewPassword,
	}
	postBytes, _ := json.Marshal(userPassDto)
	byteArr, err = network.HttpPost(httpAuthServer+pathUserChangePassword, c.GetHeader("Authorization"), postBytes)
	if err = json.Unmarshal(byteArr, &response); err != nil {
		err = fmt.Errorf("json unmarhsal response body fail,%s ", err.Error())
		return
	}
	return
}

// ResetLocalUserPassword 重置密码
func ResetLocalUserPassword(c *gin.Context, param models.UserPasswordResetParam) (response models.RestUserPasswordResponse, err error) {
	var byteArr []byte
	userPassDto := &models.SimpleLocalUserPassDto{Username: param.Username}
	postBytes, _ := json.Marshal(userPassDto)
	byteArr, err = network.HttpPost(httpAuthServer+pathUserResetPassword, c.GetHeader("Authorization"), postBytes)
	if err = json.Unmarshal(byteArr, &response); err != nil {
		err = fmt.Errorf("json unmarhsal response body fail,%s ", err.Error())
		return
	}
	return
}
