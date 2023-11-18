package api

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
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
	Authorities  []string
	LogAction    bool
	FeatureCode  string
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
	//router.Use(middleware.AuthApi)

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
	httpHandlerFuncList = append(httpHandlerFuncList) // contract instance
	//&handlerFuncObj{Url: "/scene/v1/health-check", Method: http.MethodGet, HandlerFunc: HealthCheck,
	//	ApiCode: "HealthCheck", Authorities: []string{constant.NoAuthRequired}, FeatureCode: ""},

}
