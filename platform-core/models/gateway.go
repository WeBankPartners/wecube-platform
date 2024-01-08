package models

type RegisterGatewayRouteParam struct {
	Context string                      `json:"context"`
	Items   []*RegisterGatewayRouteItem `json:"items"`
}

type RegisterGatewayRouteItem struct {
	Context    string `json:"context"`
	HttpScheme string `json:"httpScheme"`
	Host       string `json:"host"`
	Port       string `json:"port"`
}

type CommonGatewayResp struct {
	Status  string `json:"status"`
	Message string `json:"message"`
}

type RouteItem struct {
	Context    string `json:"context"`
	HttpScheme string `json:"httpScheme"`
	Host       string `json:"host"`
	Port       string `json:"port"`
	Path       string `json:"path"`
	HttpMethod string `json:"httpMethod"`
	Weight     string `json:"weight"`
}

type RouteInstanceQueryObj struct {
	Id   string `json:"id" xorm:"id"`     // 唯一标识
	Host string `json:"host" xorm:"host"` // 主机ip
	Port int    `json:"port" xorm:"port"` // 服务端口
	Name string `json:"name" xorm:"name"` // 插件名
}

type RouteInterfaceQueryObj struct {
	Path       string `json:"path" xorm:"path"`
	HttpMethod string `json:"httpMethod" xorm:"http_method"`
	Name       string `json:"name" xorm:"name"` // 插件名
}
