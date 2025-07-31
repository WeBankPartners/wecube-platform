package database

import (
	"context"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"go.uber.org/zap"
	"strings"
)

// GetAllRootMenus 查询所有根菜单
func GetAllRootMenus(ctx context.Context) (result []*models.MenuItemDto, err error) {
	var list []*models.MenuItems
	err = db.MysqlEngine.Context(ctx).SQL("select id, parent_code, code, source, description, local_display_name,menu_order from menu_items  where parent_code IS NULL or  parent_code = ''").Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	result = models.ConvertMenuItems2Dto(list)
	return
}

// GetAllSysMenus 查询所有系统菜单
func GetAllSysMenus(ctx context.Context) (result []*models.MenuItemDto, err error) {
	var list []*models.MenuItems
	err = db.MysqlEngine.Context(ctx).SQL("select * from menu_items").Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	result = models.ConvertMenuItems2Dto(list)
	return
}

// CalAvailablePluginPackageMenus 获取可用的插件菜单
func CalAvailablePluginPackageMenus(ctx context.Context) (result []*models.PluginPackageMenus, err error) {
	var codeAndMenus = make(map[string]*models.PluginPackageMenus)
	allPackageMenusForActivePackages, err := GetAllMenusByPackageStatus(ctx, []string{"REGISTERED", "RUNNING", "STOPPED"})
	if err != nil || len(allPackageMenusForActivePackages) == 0 {
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

// CalAssignedPluginPackageMenusByMenuCode 通过menuCode查找可用插件菜单
func CalAssignedPluginPackageMenusByMenuCode(ctx context.Context, menuCode string) (result []*models.PluginPackageMenus, err error) {
	var codeAndMenus = make(map[string]*models.PluginPackageMenus)
	allPackageMenusForActivePackages, err := GetAllMenusByCodeAndPackageStatus(ctx, menuCode, []string{"REGISTERED", "RUNNING", "STOPPED"})
	if err != nil || len(allPackageMenusForActivePackages) == 0 {
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
	result, err := GetMenuItemsByCode(ctx, menus.Category)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "Cannot find system menu item by package menus category", zap.String("category", menus.Category))
		return nil
	}
	if menus == nil {
		return nil
	}
	pluginPackageMenuDto := &models.MenuItemDto{
		ID:               menus.Id,
		Category:         menus.Category,
		Code:             menus.Code,
		Source:           menus.Source,
		MenuOrder:        menus.MenuOrder,
		DisplayName:      menus.DisplayName,
		LocalDisplayName: menus.LocalDisplayName,
		Path:             menus.Path,
		Active:           menus.Active,
	}
	if result != nil {
		pluginPackageMenuDto.MenuOrder = result.MenuOrder*10000 + menus.MenuOrder
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
	err = db.MysqlEngine.Context(ctx).SQL("select id,role_name,menu_code from role_menu where role_name =?", roleName).Find(&list)
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
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	return
}

// AddRoleMenu 添加roleMenu
func AddRoleMenu(ctx context.Context, menu models.RoleMenu) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "insert into  role_menu(id,role_name,menu_code) values (?,?,?)", Param: []interface{}{menu.Id, menu.RoleName, menu.MenuCode}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	return
}

// GetMenuItemsByCodes 批量根据codes返回菜单
func GetMenuItemsByCodes(ctx context.Context, codes []string) (result map[string]*models.MenuItems, err error) {
	if len(codes) == 0 {
		return make(map[string]*models.MenuItems), nil
	}

	var list []*models.MenuItems
	placeholders := make([]string, len(codes))
	params := make([]interface{}, len(codes))
	for i, code := range codes {
		placeholders[i] = "?"
		params[i] = code
	}

	sql := fmt.Sprintf("select id,parent_code,code,source,description,local_display_name,menu_order from menu_items where code in (%s)", strings.Join(placeholders, ","))
	err = db.MysqlEngine.Context(ctx).SQL(sql, params...).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	result = make(map[string]*models.MenuItems)
	for _, item := range list {
		result[item.Code] = item
	}
	return
}

// GetAllMenusByCodesAndPackageStatus 批量根据codes和包状态返回对应菜单列表
func GetAllMenusByCodesAndPackageStatus(ctx context.Context, codes []string, statusArr []string) (result map[string][]*models.PluginPackageMenus, err error) {
	if len(codes) == 0 {
		return make(map[string][]*models.PluginPackageMenus), nil
	}

	var list []*models.PluginPackageMenus
	codePlaceholders := make([]string, len(codes))
	statusPlaceholders := make([]string, len(statusArr))
	params := make([]interface{}, 0, len(codes)+len(statusArr))

	for i, code := range codes {
		codePlaceholders[i] = "?"
		params = append(params, code)
	}

	for i, status := range statusArr {
		statusPlaceholders[i] = "?"
		params = append(params, status)
	}

	sql := fmt.Sprintf("select ppm.id,ppm.category,ppm.code,ppm.source,ppm.display_name,ppm.local_display_name,ppm.path,ppm.menu_order,ppm.active,pp.status from plugin_package_menus ppm left join plugin_packages pp on ppm.plugin_package_id = pp.id where ppm.code in (%s) and pp.status in (%s)",
		strings.Join(codePlaceholders, ","),
		strings.Join(statusPlaceholders, ","))

	err = db.MysqlEngine.Context(ctx).SQL(sql, params...).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	result = make(map[string][]*models.PluginPackageMenus)
	for _, item := range list {
		result[item.Code] = append(result[item.Code], item)
	}
	return
}

// GetAllByRoleNames 批量根据角色名称获取角色菜单
func GetAllByRoleNames(ctx context.Context, roleNames []string) (list []*models.RoleMenu, err error) {
	if len(roleNames) == 0 {
		return make([]*models.RoleMenu, 0), nil
	}

	placeholders := make([]string, len(roleNames))
	params := make([]interface{}, len(roleNames))
	for i, roleName := range roleNames {
		placeholders[i] = "?"
		params[i] = roleName
	}

	sql := fmt.Sprintf("select id,role_name,menu_code from role_menu where role_name in (%s)", strings.Join(placeholders, ","))
	err = db.MysqlEngine.Context(ctx).SQL(sql, params...).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}
