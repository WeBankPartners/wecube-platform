package models

type QueryRequestFilterObj struct {
	Name     string      `json:"name"`     // 字段名
	Operator string      `json:"operator"` // 条件[contains]
	Value    interface{} `json:"value"`    // 值
}

type QueryRequestSorting struct {
	Asc   bool   `json:"asc"`   // 正序反序
	Field string `json:"field"` // 字段名
}

type QueryRequestParam struct {
	Filters       []*QueryRequestFilterObj `json:"filters"`       // 过滤条件,默认为空
	Paging        bool                     `json:"paging"`        // 是否分页,默认为false
	Pageable      *PageInfo                `json:"pageable"`      // 分页信息,默认为空
	Sorting       []*QueryRequestSorting   `json:"sorting"`       // 排序,默认为空
	ResultColumns []string                 `json:"resultColumns"` // 返回列,默认为空全返回
}

type TransFiltersParam struct {
	IsStruct   bool
	StructObj  interface{}
	Prefix     string
	KeyMap     map[string]string
	PrimaryKey string
}
