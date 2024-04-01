package api

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/api/support"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/service"
	"github.com/gin-gonic/gin"
)

func RefreshRoute(c *gin.Context) {
	log.Logger.Info("refresh route")
	service.DynamicRouteConfigurationServiceInstance.RefreshRoutes()
	support.ReturnSuccess(c)
}

func ListRouteItems(c *gin.Context) {
	routeItems := service.ListAllContextRouteItems()
	support.ReturnData(c, routeItems)
}

func ListLoadedRouteItems(c *gin.Context) {
	contextRouteConfigs := service.GetAllMvcContextRouteConfigs()
	support.ReturnData(c, contextRouteConfigs)
}

func DeleteRouteItems(c *gin.Context) {
	routeName := c.Param("route-name")
	log.Logger.Info(fmt.Sprintf("to delete route %v", routeName))

	service.DynamicRouteConfigurationServiceInstance.DeleteRouteItem(routeName)
	support.ReturnSuccess(c)
}
