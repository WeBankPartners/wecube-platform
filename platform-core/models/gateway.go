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
