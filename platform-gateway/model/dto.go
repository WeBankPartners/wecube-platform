package model

type RouteItemPushDto struct {
	Context string
	Items   []RouteItemInfoDto
}

type RouteItemInfoDto struct {
	Context        string `json:"context"`
	HttpMethod     string `json:"httpMethod"`
	Path           string `json:"path"`
	HttpScheme     string `json:"httpScheme"`
	Host           string `json:"host"`
	Port           string `json:"port"`
	Weight         string `json:"weight"`
	CreateTime     int    `json:"createTime"`
	LastModifyTime int    `json:"lastModifyTime"`
	Available      bool   `json:"available"`
}

type MvcContextRouteConfigDto struct {
	Context                     string
	CreatedTime                 int64
	LastModifiedTime            int64
	Disabled                    bool
	Version                     int
	DefaultHttpDestinations     []*HttpDestinationDto
	MvcHttpMethodAndPathConfigs []*MvcHttpMethodAndPathConfigDto
}

type HttpDestinationDto struct {
	Scheme           string
	Port             int
	Host             string
	Weight           int
	CreatedTime      int64
	LastModifiedTime int64
	Version          int
	Disabled         bool
}

type MvcHttpMethodAndPathConfigDto struct {
	HttpMethod       string
	Path             string
	CreatedTime      int64
	LastModifiedTime int64
	Disabled         bool
	Version          int
	HttpDestinations []*HttpDestinationDto
}
