package api

import (
	"github.com/WeBankPartners/wecube-platform/platform-gateway/api/support"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/service"
	"github.com/gin-gonic/gin"
)

func RefreshRoute(c *gin.Context) {
	log.Logger.Info("refresh route")
	service.RefreshRoutes()
	support.ReturnSuccess(c)
}

func ListRouteItems(c *gin.Context) {
	routeItems := service.ListAllContextRouteItems()
	support.ReturnData(c, routeItems)
}
