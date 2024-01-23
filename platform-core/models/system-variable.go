package models

const (
	SystemVariableInactive  = "inactive"
	SystemVariableActive    = "active"
	ScopeGlobal             = "global"
	SourceSystem            = "system"
	SystemVariableUmContext = "UM_AUTH_CONTEXT"
)

type SystemVariables struct {
	Id           string `json:"id" xorm:"id"`                      // 唯一标识
	PackageName  string `json:"packageName" xorm:"package_name"`   // 包名
	Name         string `json:"name" xorm:"name"`                  // 变量名
	Value        string `json:"value" xorm:"value"`                // 变量值
	DefaultValue string `json:"defaultValue" xorm:"default_value"` // 默认值
	Scope        string `json:"scope" xorm:"scope"`                // 作用范围
	Source       string `json:"source" xorm:"source"`              // 来源
	Status       string `json:"status" xorm:"status"`              // 状态 -> active | inactive
}

type SystemVariablesListPageData struct {
	PageInfo *PageInfo          `json:"pageInfo"` // 分页信息
	Contents []*SystemVariables `json:"contents"` // 列表内容
}

type SystemVariablesQueryCondition struct {
	Name   string `json:"name"`   // 变量名
	Scope  string `json:"scope"`  // 作用范围
	Status string `json:"status"` // 状态 -> active | inactive
}
