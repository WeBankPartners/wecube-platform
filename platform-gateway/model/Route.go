package model

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
