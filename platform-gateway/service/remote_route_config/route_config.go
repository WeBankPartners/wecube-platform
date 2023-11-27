package remote_route_config

import (
	"github.com/WeBankPartners/wecube-platform/platform-gateway/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/model"
	"net/http"
)

type RouteItemInfoDto struct {
	Sontext        string `json:"sontext"`
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

func FetchAllRouteItemsWithRestClient() ([]RouteItemInfoDto, error) {
	log.Logger.Info("calling route config server to fetch all route items")
	serviceDef := RemoteServiceInvoke{
		Url:    model.Config.Remote.RouteConfigAddress,
		Method: http.MethodGet,
	}
	var remoteResult []RouteItemInfoDto
	if err := Execute(serviceDef, nil, &remoteResult); err != nil {
		log.Logger.Error("failed to route config server to fetch all route items", log.JsonObj("serviceDef", serviceDef))
		return nil, err
	} else {
		log.Logger.Info("complete calling route config server to fetch all route items", log.JsonObj("serviceDef", serviceDef))
		return remoteResult, nil
	}
}
