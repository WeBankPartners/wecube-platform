package api

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/model"
	"net/http"

	"github.com/gin-gonic/gin"
)

type handlerFuncObj struct {
	HandlerFunc  func(c *gin.Context)
	Method       string
	Url          string
	LogOperation bool
	PreHandle    func(c *gin.Context)
	ApiCode      string
	//Authorities  []string
	//LogAction bool
}

var (
	httpHandlerFuncList []*handlerFuncObj
	apiCodeMap          = make(map[string]string)
)

// NewRouter returns a new router.
func NewRouter() *gin.Engine {
	//redirectRoutes := buildRedirectRoutes()
	//httpHandlerFuncList = append(httpHandlerFuncList, redirectRoutes...)
	router := gin.Default()

	if model.Config.Log.AccessLogEnable {
		router.Use(middleware.HttpLogHandle())
	}
	router.Use(gin.CustomRecovery(middleware.RecoverHandle))
	router.Use(middleware.Redirect())

	for _, funcObj := range httpHandlerFuncList {
		switch funcObj.Method {
		case http.MethodGet:
			router.GET(funcObj.Url, funcObj.HandlerFunc)
		case http.MethodPost:
			router.POST(funcObj.Url, funcObj.HandlerFunc)
		case http.MethodPut:
			router.PUT(funcObj.Url, funcObj.HandlerFunc)
		case http.MethodPatch:
			router.PATCH(funcObj.Url, funcObj.HandlerFunc)
		case http.MethodDelete:
			router.DELETE(funcObj.Url, funcObj.HandlerFunc)
		}
		apiCodeMap[fmt.Sprintf("%s_%s", funcObj.Url, funcObj.Method)] = funcObj.ApiCode
	}

	return router
}

func init() {
	httpHandlerFuncList = append(httpHandlerFuncList,
		&handlerFuncObj{Url: "/gateway/v1/route-items", Method: http.MethodPost, HandlerFunc: RefreshRoute,
			ApiCode: "RefreshRoute"},
		&handlerFuncObj{Url: "/gateway/v1/route-items", Method: http.MethodGet, HandlerFunc: ListRouteItems,
			ApiCode: "ListRouteItems"},
		&handlerFuncObj{Url: "/gateway/v1/loaded-routes", Method: http.MethodGet, HandlerFunc: ListLoadedRouteItems,
			ApiCode: "ListLoadedRouteItems"},
		&handlerFuncObj{Url: "/gateway/v1/route-items/:route-name", Method: http.MethodDelete, HandlerFunc: DeleteRouteItems,
			ApiCode: "DeleteRouteItems"},
	)

}
