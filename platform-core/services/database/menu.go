package database

import (
	"context"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

// GetAllSysMenus 查询所有系统菜单
func GetAllSysMenus(ctx context.Context) (result []*models.MenuItemDto, err error) {
	var list []*models.MenuItems
	err = db.MysqlEngine.Context(ctx).SQL("select * from menus_items").Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	for _, item := range list {
		result = append(result, &models.MenuItemDto{
			ID:               item.Id,
			Category:         item.ParentCode,
			Code:             item.Code,
			Source:           item.Source,
			MenuOrder:        item.MenuOrder,
			DisplayName:      item.Description,
			LocalDisplayName: item.LocalDisplayName,
			Active:           true,
		})
	}
	return
}

// CalAvailablePluginPackageMenus 获取可用的插件菜单
func CalAvailablePluginPackageMenus(ctx context.Context) (result []*models.PluginPackageMenus, err error) {
	var codeAndMenus = make(map[string]*models.PluginPackageMenus)
	allPackageMenusForActivePackages, err := GetAllMenusByPackageStatus(ctx, []string{"REGISTERED", "RUNNING", "STOPPED"})
	if err != nil {
		return
	}
	for _, activePackage := range allPackageMenusForActivePackages {
		if v, ok := codeAndMenus[activePackage.Code]; !ok {
			codeAndMenus[activePackage.Code] = activePackage
		} else {
			if isBetterThanExistOne(activePackage, v) {
				codeAndMenus[activePackage.Code] = activePackage
			}
		}
	}
	for _, menus := range codeAndMenus {
		result = append(result, menus)
	}
	return
}

// GetMenuItemsByCode 根据code返回菜单
func GetMenuItemsByCode(ctx context.Context, code string) (result *models.MenuItems, err error) {
	var list []*models.MenuItems
	err = db.MysqlEngine.Context(ctx).SQL("select id,parent_code,code,source,description,local_display_name,menu_order from menu_items where code =?", code).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(list) > 0 {
		result = list[0]
	}
	return
}

func BuildPackageMenuItemDto(ctx context.Context, menus *models.PluginPackageMenus) *models.MenuItemDto {
	result, err := GetMenuItemsByCode(ctx, menus.Code)
	if err != nil {
		log.Logger.Error("Cannot find system menu item by package menus category", log.String("category", menus.Category))
		return nil
	}
	pluginPackageMenuDto := &models.MenuItemDto{
		ID:               menus.Id,
		Category:         menus.Category,
		Code:             menus.Code,
		Source:           menus.Source,
		MenuOrder:        result.MenuOrder*10000 + menus.MenuOrder,
		DisplayName:      menus.DisplayName,
		LocalDisplayName: menus.LocalDisplayName,
		Path:             menus.Path,
		Active:           menus.Active,
	}
	return pluginPackageMenuDto
}

func isBetterThanExistOne(menuEntityToCheck *models.PluginPackageMenus, existMenuEntity *models.PluginPackageMenus) bool {
	if existMenuEntity.Active {
		if !menuEntityToCheck.Active {
			return false
		}
		if menuEntityToCheck.MenuOrder > existMenuEntity.MenuOrder {
			return true
		}
		return false
	}
	if menuEntityToCheck.Active {
		return true
	}
	if menuEntityToCheck.MenuOrder > existMenuEntity.MenuOrder {
		return true
	}
	return false
}

func GetAllByRoleName(ctx context.Context, roleName string) (list []*models.RoleMenu, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("  select id,proc_id,role_id,role_name,permission from core_ru_proc_role_binding where role_name =?", roleName).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

// DeleteRoleMenuById 根据id删除roleMenu
func DeleteRoleMenuById(ctx context.Context, id string) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "delete from role_menu where id=?", Param: []interface{}{id}})
	err = db.Transaction(actions, ctx)
	return
}

// AddRoleMenu 添加roleMenu
func AddRoleMenu(ctx context.Context, menu models.RoleMenu) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "insert into  role_menu(id,role_name,menu_code) values (?,?,?)", Param: []interface{}{menu.Id, menu.RoleName, menu.MenuCode}})
	err = db.Transaction(actions, ctx)
	return
}
