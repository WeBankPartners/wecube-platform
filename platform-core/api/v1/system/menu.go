package system

import (
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/gin-gonic/gin"
	"sort"
)

// GetMyMenuItems 获取我的菜单项
func GetMyMenuItems(c *gin.Context) {
	var resultMenuItemDtoList []*models.MenuItemDto
	var userRoles = middleware.GetRequestRoles(c)
	var menuCodeMap = make(map[string]bool)
	var addedMenuMap = make(map[string]bool) // Track added menus to prevent duplicates
	var roleMenus []*models.RoleMenu
	var menuItems *models.MenuItems
	var pluginPackageMenus []*models.PluginPackageMenus
	var err error

	// 1. 统计系统根菜单
	rootSysMenuItemDtoList, err := database.GetAllRootMenus(c)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if len(rootSysMenuItemDtoList) > 0 {
		for _, menuItem := range rootSysMenuItemDtoList {
			if !addedMenuMap[menuItem.Code] {
				resultMenuItemDtoList = append(resultMenuItemDtoList, menuItem)
				addedMenuMap[menuItem.Code] = true
			}
		}
	}

	// 2. 计算当前账号角色拥有的菜单code
	if len(userRoles) > 0 {
		for _, roleName := range userRoles {
			roleMenus, err = database.GetAllByRoleName(c, roleName)
			if err != nil {
				middleware.ReturnError(c, err)
			}
			if len(roleMenus) > 0 {
				for _, menu := range roleMenus {
					menuCodeMap[menu.MenuCode] = true
				}
			}
		}
	}

	if len(menuCodeMap) == 0 {
		sort.Sort(models.MenuItemDtoSort(resultMenuItemDtoList))
		middleware.ReturnData(c, resultMenuItemDtoList)
		return
	}

	//  3.根据menuCode 查询
	for menuCode := range menuCodeMap {
		// Skip if menu already added
		if addedMenuMap[menuCode] {
			continue
		}

		menuItems, err = database.GetMenuItemsByCode(c, menuCode)
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
		// 菜单项不为空直接 统计
		if menuItems != nil {
			menuDto := models.ConvertMenuItem2Dto(menuItems)
			resultMenuItemDtoList = append(resultMenuItemDtoList, menuDto)
			addedMenuMap[menuCode] = true
		} else {
			pluginPackageMenus, err = database.CalAssignedPluginPackageMenusByMenuCode(c, menuCode)
			if err != nil {
				middleware.ReturnError(c, err)
				return
			}
			if len(pluginPackageMenus) == 0 {
				continue
			}
			for _, pluginPackageMenusEntity := range pluginPackageMenus {
				menuDto := database.BuildPackageMenuItemDto(c, pluginPackageMenusEntity)
				if !addedMenuMap[menuDto.Code] {
					resultMenuItemDtoList = append(resultMenuItemDtoList, menuDto)
					addedMenuMap[menuDto.Code] = true
				}
			}
		}
	}
	sort.Sort(models.MenuItemDtoSort(resultMenuItemDtoList))
	middleware.ReturnData(c, resultMenuItemDtoList)
}
