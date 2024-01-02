package system

import (
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/network"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/gin-gonic/gin"
	"sort"
)

// GetAllUser 获取全量用户
func GetAllUser(c *gin.Context) {
	var response models.GetAllUserResponse
	var list []models.UserDto
	url := fmt.Sprintf("%s/auth/v1/users", "http://localhost:9090")
	byteArr, err := network.HttpGet(url, c.GetHeader("Authorization"))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	err = json.Unmarshal(byteArr, &response)
	if err != nil {
		err = fmt.Errorf("Try to json unmarshal response body fail,%s ", err.Error())
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
	var response models.QueryRolesResponse
	requiredAll := c.Query("all")
	if requiredAll == "" {
		requiredAll = "N"
	}
	url := fmt.Sprintf("%s/auth/v1/roles", "http://localhost:9090")
	byteArr, err := network.HttpGet(url, c.GetHeader("Authorization"))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	err = json.Unmarshal(byteArr, &response)
	if err != nil {
		err = fmt.Errorf("Try to json unmarshal response body fail,%s ", err.Error())
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
