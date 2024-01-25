package model

type RequestHeader struct {
	RequestId                string
	TransactionId            string
	Did                      string
	OrgUserId                string
	ManagerAuthToken         string
	CustomerAuthToken        string
	Operator                 string
	IsAdminTokenAuthRequired bool
	OrgAccounts              []string
	SceneId                  string
	ExtraHeaders             map[string]string
	HandlerName              string
}

func (header RequestHeader) ToMap() map[string]string {
	headerMap := make(map[string]string)
	/*	if header.ManagerAuthToken != "" {
			headerMap[constant.ManagerAuthorizationHeader] = header.ManagerAuthToken

		}
		if header.CustomerAuthToken != "" {
			headerMap[constant.CustomerAuthorizationHeader] = header.CustomerAuthToken

		}
	*/if len(header.ExtraHeaders) != 0 {
		for key, value := range header.ExtraHeaders {
			headerMap[key] = value
		}
	}
	return headerMap
}

type QueryRequestFilterObj struct {
	Name     string      `json:"name"`     // 字段名
	Operator string      `json:"operator"` // 条件[eq,like,in,notIn,lt,gt,neq,null,notNull]
	Value    interface{} `json:"value"`    // 值
}

type QueryRequestSorting struct {
	Asc   bool   `json:"asc"`   // 正序反序
	Field string `json:"field"` // 字段名
}

type PageInfo struct {
	StartIndex int `json:"startIndex"` // 开始页
	PageSize   int `json:"pageSize"`   // 每页数量
	TotalRows  int `json:"totalRows"`  // 总量,查询时不用传
}

type QueryRequestParam struct {
	Filters       []*QueryRequestFilterObj `json:"filters"`       // 过滤条件,默认为空
	Paging        bool                     `json:"paging"`        // 是否分页,默认为false
	Pageable      *PageInfo                `json:"pageable"`      // 分页信息,默认为空
	Sorting       []*QueryRequestSorting   `json:"sorting"`       // 排序,默认为空
	ResultColumns []string                 `json:"resultColumns"` // 返回列,默认为空全返回
	QueryMode     string                   `json:"queryMode"`     // basic, enrich, all
	Meta          map[string]string        `json:"meta"`          //额外和查询无关的数据,例如blockchain_id
}

type TransFiltersParam struct {
	IsStruct   bool
	StructObj  interface{}
	Prefix     string
	KeyMap     map[string]string
	PrimaryKey string
}

type CommonRequest interface {
	GetId() string
	GetVersion() int32
}

type RecordPageResponse struct {
	PageInfo PageInfo `json:"pageInfo"`

	Contents []any `json:"contents"`
}
