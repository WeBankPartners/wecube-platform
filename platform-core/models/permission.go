package models

type MenuItems struct {
	Id               string `json:"Id" xorm:"id"`                               // 唯一标识
	ParentCode       string `json:"ParentCode" xorm:"parent_code"`              // 所属菜单栏
	Code             string `json:"Code" xorm:"code"`                           // 编码
	Source           string `json:"Source" xorm:"source"`                       // 来源
	Description      string `json:"Description" xorm:"description"`             // 描述
	LocalDisplayName string `json:"LocalDisplayName" xorm:"local_display_name"` // 显示名
	MenuOrder        int    `json:"MenuOrder" xorm:"menu_order"`                // 菜单排序
}

type RoleMenu struct {
	Id       string `json:"Id" xorm:"id"`              // 唯一标识
	RoleName string `json:"RoleName" xorm:"role_name"` // 角色
	MenuCode string `json:"MenuCode" xorm:"menu_code"` // 菜单编码
}
