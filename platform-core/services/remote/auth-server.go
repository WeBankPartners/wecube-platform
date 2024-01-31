package remote

import (
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/network"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

const (
	// pathRegisterLocalUser 注册用户
	pathRegisterLocalUser = "/auth/v1/users"
	// pathRetrieveAllUserAccounts 查询所有用户
	pathRetrieveAllUserAccounts = "/auth/v1/users"
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
	// pathGetUserByUserId 查询用户
	pathGetUserByUserId = "/auth/v1/users/%s"
	// pathDeleteUserAccountByUserId 根据用户id删除用户
	pathDeleteUserAccountByUserId = "/auth/v1/users/%s"
	// pathUpdateLocalRole 更新角色
	pathUpdateLocalRole = "/auth/v1/roles/update"
	// pathConfigureRoleForUsers 角色增加用户
	pathConfigureRoleForUsers = "/auth/v1/roles/%s/users"
	// pathRevokeRoleFromUsers 角色移除用户
	pathRevokeRoleFromUsers = "/auth/v1/roles/%s/users/revoke"
	// pathRevokeAuthoritiesFromRole 取消角色授权
	pathRevokeAuthoritiesFromRole = "/auth/v1/roles/%s/authorities/revoke"
	// pathConfigureRoleAuthorities 配置角色权限
	pathConfigureRoleAuthorities = "/auth/v1/roles/%s/authorities"
	// pathRegisterLocalRole 注册角色
	pathRegisterLocalRole = "/auth/v1/roles"
	// pathLogin 登录
	pathLogin = "/auth/v1/api/login"
	// pathRegisterSubSystem 注册插件subsys
	pathRegisterSubSystem = "/auth/v1/sub-systems"
)

// TODO
func RegisterSubSystem(pluginPackageObj *models.PluginPackages) (subSystemCode, subSystemKey string, err error) {
	//subSysCode := fmt.Sprintf("SYS_%s", strings.ToUpper(pluginPackageObj.Name))
	//param := models.SimpleSubSystemDto{
	//	Name:        pluginPackageObj.Name,
	//	SystemCode:  subSystemCode,
	//	Active:      true,
	//	Blocked:     false,
	//	Description: fmt.Sprintf("Plugin %s registered from platform.", pluginPackageObj.Name),
	//}

	return
}

func RegisterLocalUser(userDto *models.SimpleLocalUserDto, userToken, language string) (response models.QuerySingleUserResponse, err error) {
	var byteArr []byte
	postBytes, _ := json.Marshal(userDto)
	byteArr, err = network.HttpPost(models.Config.Auth.Url+pathRegisterLocalUser, userToken, language, postBytes)
	if err != nil {
		return
	}
	if err = json.Unmarshal(byteArr, &response); err != nil {
		err = fmt.Errorf("json unmarhsal response body fail,%s ", err.Error())
		return
	}
	return
}

// RetrieveAllUsers 获取所有用户
func RetrieveAllUsers(userToken, language string) (response models.QueryUserResponse, err error) {
	byteArr, err := network.HttpGet(models.Config.Auth.Url+pathRetrieveAllUserAccounts, userToken, language)
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
func RetrieveAllLocalRoles(requiredAll, userToken, language string) (response models.QueryRolesResponse, err error) {
	byteArr, err := network.HttpGet(fmt.Sprintf(models.Config.Auth.Url+pathRetrieveAllRoles, requiredAll), userToken, language)
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
func GetRolesByUsername(username, userToken, language string) (response models.QueryRolesResponse, err error) {
	byteArr, err := network.HttpGet(fmt.Sprintf(models.Config.Auth.Url+pathRetrieveGrantedRolesByUsername, username), userToken, language)
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
func RetrieveRoleInfo(roleId, userToken, language string) (response models.QuerySingleRolesResponse, err error) {
	url := fmt.Sprintf(models.Config.Auth.Url+pathRetrieveRoleById, roleId)
	byteArr, err := network.HttpGet(url, userToken, language)
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
func GetUsersByRoleId(roleId, userToken, language string) (response models.QueryUserResponse, err error) {
	byteArr, err := network.HttpGet(fmt.Sprintf(models.Config.Auth.Url+pathRetrieveAllUsersBelongsToRoleId, roleId), userToken, language)
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
func ConfigureUserWithRoles(userId, userToken, language string, rolesList []string) (err error) {
	var postParams []*models.SimpleLocalRoleDto
	for _, role := range rolesList {
		postParams = append(postParams, &models.SimpleLocalRoleDto{ID: role})
	}
	postBytes, _ := json.Marshal(postParams)
	err = network.HttpPostCommon(fmt.Sprintf(models.Config.Auth.Url+pathConfigureRolesForUser, userId), userToken, language, postBytes)
	return
}

// ModifyLocalUserPassword 修改密码
func ModifyLocalUserPassword(param models.UserPasswordChangeParam, username, userToken, language string) (response models.QuerySingleUserPassResponse, err error) {
	var byteArr []byte
	userPassDto := &models.SimpleLocalUserPassDto{
		Username:         username,
		OriginalPassword: param.OriginalPassword,
		ChangedPassword:  param.NewPassword,
	}
	postBytes, _ := json.Marshal(userPassDto)
	byteArr, err = network.HttpPost(models.Config.Auth.Url+pathUserChangePassword, userToken, language, postBytes)
	if err = json.Unmarshal(byteArr, &response); err != nil {
		err = fmt.Errorf("json unmarhsal response body fail,%s ", err.Error())
		return
	}
	return
}

// ResetLocalUserPassword 重置密码
func ResetLocalUserPassword(param models.UserPasswordResetParam, userToken, language string) (response models.RestUserPasswordResponse, err error) {
	var byteArr []byte
	userPassDto := &models.SimpleLocalUserPassDto{Username: param.Username}
	postBytes, _ := json.Marshal(userPassDto)
	byteArr, err = network.HttpPost(models.Config.Auth.Url+pathUserResetPassword, userToken, language, postBytes)
	if err = json.Unmarshal(byteArr, &response); err != nil {
		err = fmt.Errorf("json unmarhsal response body fail,%s ", err.Error())
		return
	}
	return
}

// RetrieveUserByUserId 获取用户信息
func RetrieveUserByUserId(userId, userToken, language string) (response models.QuerySingleUserResponse, err error) {
	byteArr, err := network.HttpGet(fmt.Sprintf(models.Config.Auth.Url+pathGetUserByUserId, userId), userToken, language)
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

// UnregisterLocalUser 删除用户
func UnregisterLocalUser(userId, userToken, language string) error {
	return network.HttpDeleteCommon(fmt.Sprintf(models.Config.Auth.Url+pathDeleteUserAccountByUserId, userId), userToken, language)
}

// UpdateLocalRole 更新角色
func UpdateLocalRole(userToken, language string, param models.SimpleLocalRoleDto) (response models.QuerySingleRolesResponse, err error) {
	var byteArr []byte
	postBytes, _ := json.Marshal(param)
	byteArr, err = network.HttpPost(models.Config.Auth.Url+pathUpdateLocalRole, userToken, language, postBytes)
	if err = json.Unmarshal(byteArr, &response); err != nil {
		err = fmt.Errorf("json unmarhsal response body fail,%s ", err.Error())
		return
	}
	return
}

// ConfigureRoleForUsers 角色添加用户列表
func ConfigureRoleForUsers(userId, userToken, language string, userIdList []string) (err error) {
	var postParams []*models.SimpleLocalUserDto
	for _, userId := range userIdList {
		postParams = append(postParams, &models.SimpleLocalUserDto{ID: userId})
	}
	postBytes, _ := json.Marshal(postParams)
	err = network.HttpPostCommon(fmt.Sprintf(models.Config.Auth.Url+pathConfigureRoleForUsers, userId), userToken, language, postBytes)
	return
}

// RevokeRoleFromUsers 角色移除用户
func RevokeRoleFromUsers(roleId, userToken, language string, userIdList []string) (err error) {
	var postParams []*models.SimpleLocalUserDto
	for _, userId := range userIdList {
		postParams = append(postParams, &models.SimpleLocalUserDto{ID: userId})
	}
	postBytes, _ := json.Marshal(postParams)
	err = network.HttpPostCommon(fmt.Sprintf(models.Config.Auth.Url+pathRevokeRoleFromUsers, roleId), userToken, language, postBytes)
	return
}

// RevokeRoleAuthoritiesById 取消角色授权
func RevokeRoleAuthoritiesById(roleId, userToken, language string, authoritiesToRevoke []*models.SimpleAuthorityDto) error {
	postBytes, _ := json.Marshal(authoritiesToRevoke)
	return network.HttpPostCommon(fmt.Sprintf(models.Config.Auth.Url+pathRevokeAuthoritiesFromRole, roleId), userToken, language, postBytes)
}

// ConfigureRoleWithAuthoritiesById 配置角色权限
func ConfigureRoleWithAuthoritiesById(roleId, userToken, language string, authoritiesToGrantList []*models.SimpleAuthorityDto) error {
	postBytes, _ := json.Marshal(authoritiesToGrantList)
	return network.HttpPostCommon(fmt.Sprintf(models.Config.Auth.Url+pathConfigureRoleAuthorities, roleId), userToken, language, postBytes)
}

// RegisterLocalRole 创建角色
func RegisterLocalRole(roleDto *models.SimpleLocalRoleDto, userToken, language string) (response models.QuerySingleRolesResponse, err error) {
	var byteArr []byte
	postBytes, _ := json.Marshal(roleDto)
	byteArr, err = network.HttpPost(models.Config.Auth.Url+pathRegisterLocalRole, userToken, language, postBytes)
	if err != nil {
		return
	}
	if err = json.Unmarshal(byteArr, &response); err != nil {
		err = fmt.Errorf("json unmarhsal response body fail,%s ", err.Error())
		return
	}
	return
}
