package system

import (
	"context"
	"fmt"
	"sort"
	"strings"

	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"github.com/gin-gonic/gin"
)

// CreateUser 添加用户
func CreateUser(c *gin.Context) {
	var param models.UserDto
	var response models.QuerySingleUserResponse
	var authContext string
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if strings.TrimSpace(param.UserName) == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("name is empty")))
		return
	}

	userDto := &models.SimpleLocalUserDto{
		Username:   param.UserName,
		Password:   param.Password,
		AuthSource: param.AuthType,
	}
	if strings.TrimSpace(userDto.AuthSource) == "" {
		userDto.AuthSource = models.AuthTypeLocal
	}
	// 计算 AuthContext
	if userDto.AuthSource == models.AuthTypeUm {
		authContext, err = tryCalculateUmAuthContext(c)
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
		userDto.AuthContext = authContext
	}
	response, err = remote.RegisterLocalUser(userDto, c.GetHeader("Authorization"), c.GetHeader("Accept-Language"))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.Return(c, response)
}

// CreateRole 创建角色
func CreateRole(c *gin.Context) {
	var param models.SimpleLocalRoleDto
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if param.Administrator == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("administrator not empty")))
		return
	}
	response, err := remote.RegisterLocalRole(&param, c.GetHeader("Authorization"), c.GetHeader("Accept-Language"))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.Return(c, response)
}

// GetAllUser 获取全量用户
func GetAllUser(c *gin.Context) {
	var list []models.UserDto
	response, err := remote.RetrieveAllUsers(c.GetHeader("Authorization"), c.GetHeader("Accept-Language"))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if len(response.Data) > 0 {
		for _, item := range response.Data {
			list = append(list, models.UserDto{ID: item.ID, UserName: item.Username, Password: item.Password})
		}
	}
	middleware.ReturnData(c, list)
}

// QueryRoles 查询角色
func QueryRoles(c *gin.Context) {
	requiredAll := c.Query("all")
	if requiredAll == "" {
		requiredAll = "N"
	}
	response, err := remote.RetrieveAllLocalRoles(requiredAll, c.GetHeader("Authorization"), c.GetHeader("Accept-Language"))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.Return(c, response)
}

// AllMenus 查询所有菜单
func AllMenus(c *gin.Context) {
	allSysMenuList, err := database.GetAllSysMenus(c)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	pluginPackageMenusEntities, err := database.CalAvailablePluginPackageMenus(c)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	for _, pluginPackageMenEntity := range pluginPackageMenusEntities {
		menuItemsDto := database.BuildPackageMenuItemDto(c, pluginPackageMenEntity)
		if menuItemsDto != nil {
			allSysMenuList = append(allSysMenuList, menuItemsDto)
		}
	}
	sort.Sort(models.MenuItemDtoSort(allSysMenuList))
	middleware.ReturnData(c, allSysMenuList)
}

// GetMenusByUsername 根据用户名获取菜单列表
func GetMenusByUsername(c *gin.Context) {
	var result []*models.RoleMenuDto
	username := c.Param("username")
	token := c.GetHeader("Authorization")
	response, err := remote.GetRolesByUsername(username, token, c.GetHeader("Accept-Language"))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if response.Status != "OK" {
		err = fmt.Errorf(response.Message)
		middleware.ReturnError(c, err)
		return
	}
	if len(response.Data) > 0 {
		for _, item := range response.Data {
			roleMenuDto, err := retrieveMenusByRoleId(c, item.ID, token, c.GetHeader("Accept-Language"))
			if err != nil {
				middleware.ReturnError(c, err)
				return
			}
			result = append(result, roleMenuDto)
		}
	}
	middleware.ReturnData(c, result)
}

// GetRolesByUsername 根据用户名获取用户角色
func GetRolesByUsername(c *gin.Context) {
	username := c.Param("username")
	response, err := remote.GetRolesByUsername(username, c.GetHeader("Authorization"), c.GetHeader("Accept-Language"))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.Return(c, response)
}

// GetMenusByRoleId 返回角色菜单
func GetMenusByRoleId(c *gin.Context) {
	roleId := c.Param("role-id")
	roleMenuDto, err := retrieveMenusByRoleId(c, roleId, c.GetHeader("Authorization"), c.GetHeader("Accept-Language"))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, roleMenuDto)
}

// GetUsersByRoleId 查询角色用户
func GetUsersByRoleId(c *gin.Context) {
	var result = make([]*models.UserDto, 0)
	roleId := c.Param("role-id")
	response, err := remote.GetUsersByRoleId(roleId, c.GetHeader("Authorization"), c.GetHeader("Accept-Language"))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if len(response.Data) > 0 {
		for _, dto := range response.Data {
			result = append(result, &models.UserDto{
				ID:                dto.ID,
				UserName:          dto.Username,
				Password:          dto.Password,
				RoleAdministrator: dto.RoleAdministrator,
			})
		}
	}
	middleware.ReturnData(c, result)
}

// GrantRoleToUsers 修改用户角色
func GrantRoleToUsers(c *gin.Context) {
	userId := c.Param("user-id")
	var roleIds []string
	if err := c.ShouldBindJSON(&roleIds); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	err := remote.ConfigureUserWithRoles(userId, c.GetHeader("Authorization"), c.GetHeader("Accept-Language"), roleIds)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnSuccess(c)
}

// GrantUserAddRoles 角色添加用户
func GrantUserAddRoles(c *gin.Context) {
	roleId := c.Param("role-id")
	var userIds []string
	if err := c.ShouldBindJSON(&userIds); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	err := remote.ConfigureRoleForUsers(roleId, c.GetHeader("Authorization"), c.GetHeader("Accept-Language"), userIds)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnSuccess(c)
}

// ResetUserPassword 重置用户密码
func ResetUserPassword(c *gin.Context) {
	var param models.UserPasswordResetParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	response, err := remote.ResetLocalUserPassword(param, c.GetHeader("Authorization"), c.GetHeader("Accept-Language"))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.Return(c, response)
}

// ChangeUserPassword 修改用户密码
func ChangeUserPassword(c *gin.Context) {
	var param models.UserPasswordChangeParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	response, err := remote.ModifyLocalUserPassword(param, middleware.GetRequestUser(c), c.GetHeader("Authorization"), c.GetHeader("Accept-Language"))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.Return(c, response)
}

// DeleteUserByUserId 删除用户
func DeleteUserByUserId(c *gin.Context) {
	userId := c.Param("user-id")
	token := c.GetHeader("Authorization")
	response, err := remote.RetrieveUserByUserId(userId, token, c.GetHeader("Accept-Language"))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if response.Data == nil {
		err = fmt.Errorf("not found user-id:%s", userId)
		middleware.ReturnError(c, err)
		return
	}
	if middleware.GetRequestUser(c) == response.Data.Username {
		err = fmt.Errorf("cannot remove the account which belongs to the login user")
		middleware.ReturnError(c, err)
		return
	}
	// 删除用户
	err = remote.UnregisterLocalUser(userId, c.GetHeader("Authorization"), c.GetHeader("Accept-Language"))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnSuccess(c)
}

// UpdateRole 更新角色
func UpdateRole(c *gin.Context) {
	var param models.SimpleLocalRoleDto
	roleId := c.Param("role-id")
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if roleId == "" || param.Administrator == "" {
		err := fmt.Errorf("param roleId or administrator is empty")
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	param.ID = roleId
	response, err := remote.UpdateLocalRole(c.GetHeader("Authorization"), c.GetHeader("Accept-Language"), param)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.Return(c, response)
}

// RevokeRoleFromUsers 角色移除用户
func RevokeRoleFromUsers(c *gin.Context) {
	roleId := c.Param("role-id")
	var userIds []string
	if err := c.ShouldBindJSON(&userIds); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if roleId == "" {
		err := fmt.Errorf("param roleId is empty")
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	err := remote.RevokeRoleFromUsers(roleId, c.GetHeader("Authorization"), c.GetHeader("Accept-Language"), userIds)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnSuccess(c)
}

// UpdateRoleToMenusByRoleId 更新角色菜单
func UpdateRoleToMenusByRoleId(c *gin.Context) {
	roleId := c.Param("role-id")
	var menuCodeList []string
	if err := c.ShouldBindJSON(&menuCodeList); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	err := updateRoleToMenusByRoleId(c, roleId, c.GetHeader("Authorization"), c.GetHeader("Accept-Language"), convertList2Map(menuCodeList))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnSuccess(c)
}

// GetRolesOfCurrentUser 获取当前用户的roles
func GetRolesOfCurrentUser(c *gin.Context) {
	response, err := remote.GetRolesByUsername(middleware.GetRequestUser(c), c.GetHeader("Authorization"), c.GetHeader("Accept-Language"))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.Return(c, response)
}

func retrieveMenusByRoleId(ctx context.Context, roleId, userToken, language string) (roleMenuDto *models.RoleMenuDto, err error) {
	roleMenuDto = &models.RoleMenuDto{}
	var menuItemDtoList []*models.MenuItemDto
	var roleRes models.QuerySingleRolesResponse
	var roleMenuEntities []*models.RoleMenu
	var menuItemsEntity *models.MenuItems
	var pluginPackageMenusEntities []*models.PluginPackageMenus
	roleRes, err = remote.RetrieveRoleInfo(roleId, userToken, language)
	if err != nil {
		return
	}
	roleName := roleRes.Data.Name
	roleMenuEntities, err = database.GetAllByRoleName(ctx, roleName)
	if err != nil {
		return
	}
	roleMenuDto.RoleId = roleId
	roleMenuDto.RoleName = roleName
	for _, roleMenuEntity := range roleMenuEntities {
		menuCode := roleMenuEntity.MenuCode
		menuItemsEntity, err = database.GetMenuItemsByCode(ctx, menuCode)
		if err != nil {
			return
		}
		if menuItemsEntity != nil && menuItemsEntity.Id != "" {
			log.Logger.Info(fmt.Sprintf("System menu was found.The menu code is:[%s]", menuCode))
			menuItemDtoList = append(menuItemDtoList, buildMenuItemDto(menuItemsEntity))
		} else {
			pluginPackageMenusEntities, err = database.GetAllMenusByCodeAndPackageStatus(ctx, menuCode, []string{"REGISTERED", "RUNNING", "STOPPED"})
			if err != nil {
				return
			}
			for _, pluginPackageMenusEntity := range pluginPackageMenusEntities {
				log.Logger.Info(fmt.Sprintf("Plugin package menu was found.The menu code is:[%s]", menuCode))
				dto := database.BuildPackageMenuItemDto(ctx, pluginPackageMenusEntity)
				if dto != nil {
					menuItemDtoList = append(menuItemDtoList, dto)
				}
			}
		}
	}
	roleMenuDto.MenuList = menuItemDtoList
	// 菜单需要排序
	if len(roleMenuDto.MenuList) == 0 {
		// 空数据给默认值
		roleMenuDto.MenuList = make([]*models.MenuItemDto, 0)
	} else {
		sort.Sort(models.MenuItemDtoSort(roleMenuDto.MenuList))
	}
	return
}

func convertList2Map(list []string) map[string]bool {
	hashMap := make(map[string]bool)
	for _, s := range list {
		hashMap[s] = true
	}
	return hashMap
}

func buildMenuItemDto(entity *models.MenuItems) *models.MenuItemDto {
	dto := &models.MenuItemDto{
		ID:               entity.Id,
		Category:         entity.ParentCode,
		Code:             entity.Code,
		Source:           entity.Source,
		MenuOrder:        entity.MenuOrder,
		DisplayName:      entity.Description,
		LocalDisplayName: entity.LocalDisplayName,
		Active:           true,
	}
	return dto
}

func updateRoleToMenusByRoleId(ctx context.Context, roleId, userToken, language string, menuCodeMap map[string]bool) (err error) {
	var roleName string
	var roleMenuList []*models.RoleMenu
	var currentMenuCodeMap = make(map[string]bool)
	var authoritiesToRevoke []*models.SimpleAuthorityDto
	var needAddAuthoritiesToGrantList []*models.SimpleAuthorityDto
	roleRes, err := remote.RetrieveRoleInfo(roleId, userToken, language)
	if err != nil {
		return
	}
	roleName = roleRes.Data.Name
	roleMenuList, err = database.GetAllByRoleName(ctx, roleName)
	if err != nil {
		return
	}
	for _, menu := range roleMenuList {
		if _, ok := menuCodeMap[menu.MenuCode]; !ok {
			// 删除 roleMenu
			err = database.DeleteRoleMenuById(ctx, menu.Id)
			if err != nil {
				return
			}
			authoritiesToRevoke = append(authoritiesToRevoke, &models.SimpleAuthorityDto{Code: menu.MenuCode})
		}
		currentMenuCodeMap[menu.MenuCode] = true
	}
	if len(authoritiesToRevoke) > 0 {
		err = remote.RevokeRoleAuthoritiesById(roleId, userToken, language, authoritiesToRevoke)
		if err != nil {
			return
		}
	}
	for code, _ := range menuCodeMap {
		if _, ok := currentMenuCodeMap[code]; !ok {
			log.Logger.Info(fmt.Sprintf("create menus:[%s]", code))
			roleMenu := models.RoleMenu{
				Id:       guid.CreateGuid(),
				RoleName: roleName,
				MenuCode: code,
			}
			err = database.AddRoleMenu(ctx, roleMenu)
			if err != nil {
				return
			}
			needAddAuthoritiesToGrantList = append(needAddAuthoritiesToGrantList, &models.SimpleAuthorityDto{Code: code})
		}
	}
	if len(needAddAuthoritiesToGrantList) > 0 {
		err = remote.ConfigureRoleWithAuthoritiesById(roleId, userToken, language, needAddAuthoritiesToGrantList)
	}
	return
}

func tryCalculateUmAuthContext(ctx context.Context) (authContext string, err error) {
	var list []*models.SystemVariables
	condition := models.SystemVariablesQueryCondition{Name: models.SystemVariableUmContext, Scope: models.ScopeGlobal, Status: models.SystemVariableActive}
	list, err = database.QuerySystemVariablesByCondition(ctx, condition)
	if err != nil {
		return
	}
	for _, variables := range list {
		if strings.TrimSpace(variables.DefaultValue) != "" {
			authContext = variables.DefaultValue
			return
		}
	}
	return
}
