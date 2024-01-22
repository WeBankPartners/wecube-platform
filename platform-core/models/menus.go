package models

type MenuItems struct {
	Id               string `json:"id" xorm:"id"`                               // 唯一标识
	ParentCode       string `json:"parentCode" xorm:"parent_code"`              // 所属菜单栏
	Code             string `json:"code" xorm:"code"`                           // 编码
	Source           string `json:"source" xorm:"source"`                       // 来源
	Description      string `json:"description" xorm:"description"`             // 描述
	LocalDisplayName string `json:"localDisplayName" xorm:"local_display_name"` // 显示名
	MenuOrder        int    `json:"menuOrder" xorm:"menu_order"`                // 菜单排序
}

type UserIdsParam struct {
	UserIds []string `json:"userIds"` // 用户列表
}

type MenuCodesParam struct {
	MenuCodeList []string `json:"menuCodeList"` // 菜单code列表
}

type RoleMenu struct {
	Id       string `json:"id" xorm:"id"`              // 唯一标识
	RoleName string `json:"roleName" xorm:"role_name"` // 角色
	MenuCode string `json:"menuCode" xorm:"menu_code"` // 菜单编码
}

type UserPasswordResetParam struct {
	Username string `json:"username"` //用户名
}

type UserPasswordChangeParam struct {
	OriginalPassword string `json:"originalPassword"` // 原始密码
	NewPassword      string `json:"newPassword"`      // 新密码
}

func ConvertMenuItems2Dto(menuItems []*MenuItems) []*MenuItemDto {
	var result []*MenuItemDto
	for _, item := range menuItems {
		result = append(result, ConvertMenuItem2Dto(item))
	}
	return result
}

func ConvertMenuItem2Dto(item *MenuItems) *MenuItemDto {
	return &MenuItemDto{
		ID:               item.Id,
		Category:         item.ParentCode,
		Code:             item.Code,
		Source:           item.Source,
		MenuOrder:        item.MenuOrder,
		DisplayName:      item.Description,
		LocalDisplayName: item.LocalDisplayName,
		Active:           true,
	}
}
