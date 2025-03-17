package remote_route_config

import (
	"github.com/WeBankPartners/wecube-platform/platform-gateway/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/model"
	"net/http"
)

func FetchAllRouteItemsWithRestClient() ([]*model.RouteItemInfoDto, error) {
	log.Info(nil, log.LOGGER_APP, "calling route config server to fetch all route items")
	serviceDef := RemoteServiceInvoke{
		Url:    model.Config.Remote.RouteConfigAddress,
		Method: http.MethodGet,
	}
	var remoteResult []*model.RouteItemInfoDto
	if err := Execute(serviceDef, nil, &remoteResult); err != nil {
		log.Error(nil, log.LOGGER_APP, "failed to route config server to fetch all route items", log.JsonObj("serviceDef", serviceDef))
		return nil, err
	} else {
		log.Info(nil, log.LOGGER_APP, "complete calling route config server to fetch all route items", log.JsonObj("serviceDef", serviceDef))
		return remoteResult, nil
	}
}
