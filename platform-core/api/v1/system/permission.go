package system

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"github.com/gin-gonic/gin"
	"sort"
)

// GetAllUser 获取全量用户
func GetAllUser(c *gin.Context) {
	var list []models.UserDto
	response, err := remote.RetrieveAllUsers(c)
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
	response, err := remote.RetrieveAllLocalRoles(c, requiredAll)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, response.Data)
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
	response, err := remote.GetRolesByUsername(c, username)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if len(response.Data) > 0 {
		for _, item := range response.Data {
			roleMenuDto, err := retrieveMenusByRoleId(c, item.ID)
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
	var result []*models.SimpleLocalRoleDto
	username := c.Param("username")
	response, err := remote.GetRolesByUsername(c, username)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if len(response.Data) > 0 {
		for _, item := range response.Data {
			result = append(result, item)
		}
	}
	middleware.ReturnData(c, result)
}

func retrieveMenusByRoleId(c *gin.Context, roleId string) (roleMenuDto *models.RoleMenuDto, err error) {
	var menuItemDtoList []*models.MenuItemDto
	var roleRes models.QueryRolesResponse
	var roleMenuEntities []*models.RoleMenu
	var menuItemsEntity *models.MenuItems
	var pluginPackageMenusEntities []*models.PluginPackageMenus
	roleRes, err = remote.RetrieveRoleInfo(c, roleId)
	if err != nil {
		return
	}
	if len(roleRes.Data) > 0 {
		roleName := roleRes.Data[0].Name
		roleMenuEntities, err = database.GetAllByRoleName(c, roleName)
		if err != nil {
			return
		}
		roleMenuDto.RoleId = roleId
		roleMenuDto.RoleName = roleName
		for _, roleMenuEntity := range roleMenuEntities {
			menuCode := roleMenuEntity.MenuCode
			menuItemsEntity, err = database.GetMenuItemsByCode(c, menuCode)
			if err != nil {
				return
			}
			if menuItemsEntity != nil && menuItemsEntity.Id != "" {
				log.Logger.Info(fmt.Sprintf("System menu was found.The menu code is:[%s]", menuCode))
				menuItemDtoList = append(menuItemDtoList, buildMenuItemDto(menuItemsEntity))
			} else {
				pluginPackageMenusEntities, err = database.GetAllMenusByCodeAndPackageStatus(c, menuCode, []string{"REGISTERED", "RUNNING", "STOPPED"})
				if err != nil {
					return
				}
				for _, pluginPackageMenusEntity := range pluginPackageMenusEntities {
					log.Logger.Info(fmt.Sprintf("Plugin package menu was found.The menu code is:[%s]", menuCode))
					dto := database.BuildPackageMenuItemDto(c, pluginPackageMenusEntity)
					if dto != nil {
						menuItemDtoList = append(menuItemDtoList, dto)
					}
				}
			}
		}
	}
	roleMenuDto.MenuList = menuItemDtoList
	return
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
