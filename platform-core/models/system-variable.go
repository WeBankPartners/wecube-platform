package models

type SystemVariables struct {
	Id           string `json:"Id" xorm:"id"`                      // 唯一标识
	PackageName  string `json:"PackageName" xorm:"package_name"`   // 包名
	Name         string `json:"Name" xorm:"name"`                  // 变量名
	Value        string `json:"Value" xorm:"value"`                // 变量值
	DefaultValue string `json:"DefaultValue" xorm:"default_value"` // 默认值
	Scope        string `json:"Scope" xorm:"scope"`                // 作用范围
	Source       string `json:"Source" xorm:"source"`              // 来源
	Status       string `json:"Status" xorm:"status"`              // 状态
}
