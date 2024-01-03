package api

import (
	"bytes"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/v1/plugin"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/v1/system"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
	"io"
	"net/http"
	"strings"
	"time"
)

type handlerFuncObj struct {
	HandlerFunc  func(c *gin.Context)
	Method       string
	Url          string
	LogOperation bool
	PreHandle    func(c *gin.Context)
	ApiCode      string
}

var (
	httpHandlerFuncList []*handlerFuncObj
	apiCodeMap          = make(map[string]string)
)

func init() {
	httpHandlerFuncList = append(httpHandlerFuncList,
		// health check
		&handlerFuncObj{Url: "/health-check", Method: "GET", HandlerFunc: healthCheck, ApiCode: "health"},
		// base
		&handlerFuncObj{Url: "/appinfo/version", Method: "GET", HandlerFunc: system.AppVersion, ApiCode: "get-version"},
		// system-variable
		&handlerFuncObj{Url: "/system-variables/retrieve", Method: "POST", HandlerFunc: system.QuerySystemVariables, ApiCode: "query-system-variables"},
		&handlerFuncObj{Url: "/system-variables/create", Method: "POST", HandlerFunc: system.CreateSystemVariable, ApiCode: "create-system-variables"},
		&handlerFuncObj{Url: "/system-variables/update", Method: "POST", HandlerFunc: system.UpdateSystemVariable, ApiCode: "update-system-variables"},
		&handlerFuncObj{Url: "/system-variables/delete", Method: "POST", HandlerFunc: system.DeleteSystemVariable, ApiCode: "delete-system-variables"},
		// resource
		&handlerFuncObj{Url: "/resource/constants/resource-server-status", Method: "GET", HandlerFunc: system.GetResourceServerStatus, ApiCode: "get-resource-server-status"},
		&handlerFuncObj{Url: "/resource/constants/resource-server-types", Method: "GET", HandlerFunc: system.GetResourceServerTypes, ApiCode: "get-resource-server-types"},
		&handlerFuncObj{Url: "/resource/constants/resource-item-status", Method: "GET", HandlerFunc: system.GetResourceItemStatus, ApiCode: "get-resource-item-status"},
		&handlerFuncObj{Url: "/resource/constants/resource-item-types", Method: "GET", HandlerFunc: system.GetResourceItemTypes, ApiCode: "get-resource-item-types"},
		&handlerFuncObj{Url: "/resource/servers/retrieve", Method: "POST", HandlerFunc: system.QueryResourceServer, ApiCode: "query-resource-server"},
		&handlerFuncObj{Url: "/resource/items/retrieve", Method: "POST", HandlerFunc: system.QueryResourceItem, ApiCode: "query-resource-item"},
		&handlerFuncObj{Url: "/resource/servers/create", Method: "POST", HandlerFunc: system.CreateResourceServer, ApiCode: "create-resource-server"},
		&handlerFuncObj{Url: "/resource/servers/update", Method: "POST", HandlerFunc: system.UpdateResourceServer, ApiCode: "update-resource-server"},
		&handlerFuncObj{Url: "/resource/servers/delete", Method: "POST", HandlerFunc: system.DeleteResourceServer, ApiCode: "delete-resource-server"},
		&handlerFuncObj{Url: "/resource/servers/:resourceServerId/product-serial", Method: "GET", HandlerFunc: system.GetResourceServerSerialNum, ApiCode: "get-serial-num"},
		// plugin
		&handlerFuncObj{Url: "/packages", Method: "GET", HandlerFunc: plugin.GetPackages, ApiCode: "get-packages"},
		&handlerFuncObj{Url: "/packages", Method: "POST", HandlerFunc: plugin.UploadPackage, ApiCode: "upload-packages"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/dependencies", Method: "GET", HandlerFunc: plugin.GetPluginDependencies, ApiCode: "get-plugin-dependencies"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/menus", Method: "GET", HandlerFunc: plugin.GetPluginMenus, ApiCode: "get-plugin-menus"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/models", Method: "GET", HandlerFunc: plugin.GetPluginModels, ApiCode: "get-plugin-models"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/system-parameters", Method: "GET", HandlerFunc: plugin.GetPluginSystemParameters, ApiCode: "get-plugin-system-parameters"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/authorities", Method: "GET", HandlerFunc: plugin.GetPluginAuthorities, ApiCode: "get-plugin-authorities"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/runtime-resources", Method: "GET", HandlerFunc: plugin.GetPluginRuntimeResources, ApiCode: "get-runtime-resource"},
		&handlerFuncObj{Url: "/packages/register/:pluginPackageId", Method: "POST", HandlerFunc: plugin.RegisterPackage, ApiCode: "register-package"},
		&handlerFuncObj{Url: "/available-container-hosts", Method: "GET", HandlerFunc: plugin.GetAvailableContainerHost, ApiCode: "get-available-host"},
		&handlerFuncObj{Url: "/hosts/:hostIp/next-available-port", Method: "GET", HandlerFunc: plugin.GetHostAvailablePort, ApiCode: "get-available-port"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/hosts/:hostIp/ports/:port/instance/launch", Method: "POST", HandlerFunc: plugin.LaunchPlugin, ApiCode: "launch-plugin"},
		&handlerFuncObj{Url: "/packages/instances/:pluginInstanceId/remove", Method: "DELETE", HandlerFunc: plugin.RemovePlugin, ApiCode: "remove-plugin"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/instances", Method: "GET", HandlerFunc: plugin.GetPluginRunningInstances, ApiCode: "get-plugin-running-instance"},
		// plugin-config
		&handlerFuncObj{Url: "/packages/:pluginPackageId/plugin-configs", Method: "GET", HandlerFunc: plugin.GetPluginConfigs, ApiCode: "get-plugin-configs"},
		&handlerFuncObj{Url: "/plugins/interfaces/:pluginConfigId", Method: "GET", HandlerFunc: plugin.GetConfigInterfaces, ApiCode: "get-config-interface"},
		&handlerFuncObj{Url: "/plugins/roles/configs/:pluginConfigId", Method: "POST", HandlerFunc: plugin.UpdatePluginConfigRoles, ApiCode: "update-config-roles"},
		&handlerFuncObj{Url: "/plugins/disable/:pluginConfigId", Method: "POST", HandlerFunc: plugin.DisablePluginConfig, ApiCode: "disable-plugin-config"},
		&handlerFuncObj{Url: "/plugins/enable/:pluginConfigId", Method: "POST", HandlerFunc: plugin.EnablePluginConfig, ApiCode: "enable-plugin-configs"},
		&handlerFuncObj{Url: "/plugins", Method: "POST", HandlerFunc: plugin.SavePluginConfig, ApiCode: "save-plugin-configs"},
		&handlerFuncObj{Url: "/plugins/configs/:pluginConfigId", Method: "DELETE", HandlerFunc: plugin.DeletePluginConfig, ApiCode: "delete-plugin-configs"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/plugin-config-outlines", Method: "GET", HandlerFunc: plugin.GetBatchPluginConfigs, ApiCode: "get-batch-plugin-configs"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/plugin-configs/enable-in-batch", Method: "POST", HandlerFunc: plugin.BatchEnablePluginConfig, ApiCode: "batch-enable-plugin-configs"},
		&handlerFuncObj{Url: "/plugins/packages/export/:pluginPackageId", Method: "GET", HandlerFunc: plugin.ExportPluginConfigs, ApiCode: "export-plugin-configs"},
		&handlerFuncObj{Url: "/plugins/packages/import/:pluginPackageId", Method: "POST", HandlerFunc: plugin.ImportPluginConfigs, ApiCode: "import-plugin-configs"},
		&handlerFuncObj{Url: "/packages/decommission/:pluginPackageId", Method: "POST", HandlerFunc: plugin.DeletePlugin, ApiCode: "delete-plugin"},

		// permission
		&handlerFuncObj{Url: "/user/retrieve", Method: "GET", HandlerFunc: system.GetAllUser, ApiCode: "get-all-user"},
		&handlerFuncObj{Url: "/roles/retrieve", Method: "GET", HandlerFunc: system.QueryRoles, ApiCode: "query-roles"},
		&handlerFuncObj{Url: "/all-menus", Method: "GET", HandlerFunc: system.AllMenus, ApiCode: "all-menus"},
		&handlerFuncObj{Url: "/user/:username/menus", Method: "GET", HandlerFunc: system.GetMenusByUsername, ApiCode: "get-user-menus"},
		&handlerFuncObj{Url: "/user/:username/roles", Method: "GET", HandlerFunc: system.GetRolesByUsername, ApiCode: "get-user-roles"},
	)
}

func InitHttpServer() {
	middleware.InitHttpError()
	r := gin.New()
	// access log
	r.Use(httpLogHandle())
	// recover
	r.Use(gin.CustomRecovery(recoverHandle))
	// register handler func with auth
	authRouter := r.Group(models.UrlPrefix, middleware.AuthToken)
	for _, funcObj := range httpHandlerFuncList {
		if !strings.HasPrefix(funcObj.Url, "/resource/") {
			funcObj.Url = "/v1" + funcObj.Url
		}
		apiCodeMap[fmt.Sprintf("%s_%s", funcObj.Method, funcObj.Url)] = funcObj.ApiCode
		handleFuncList := []gin.HandlerFunc{funcObj.HandlerFunc}
		if funcObj.PreHandle != nil {
			handleFuncList = append([]gin.HandlerFunc{funcObj.PreHandle}, funcObj.HandlerFunc)
		}
		switch funcObj.Method {
		case "GET":
			authRouter.GET(funcObj.Url, handleFuncList...)
			break
		case "POST":
			authRouter.POST(funcObj.Url, handleFuncList...)
			break
		case "PUT":
			authRouter.PUT(funcObj.Url, handleFuncList...)
			break
		case "DELETE":
			authRouter.DELETE(funcObj.Url, handleFuncList...)
			break
		}
	}
	r.Run(":" + models.Config.HttpServer.Port)
}

func httpLogHandle() gin.HandlerFunc {
	return func(c *gin.Context) {
		start := time.Now()
		requestId := c.GetHeader(models.RequestIdHeader)
		transactionId := c.GetHeader(models.TransactionIdHeader)
		if requestId == "" {
			requestId = "req_" + guid.CreateGuid()
		}
		if transactionId == "" {
			transactionId = "trans_" + guid.CreateGuid()
		}
		c.Set(models.RequestIdHeader, requestId)
		c.Set(models.TransactionIdHeader, transactionId)
		bodyBytes, _ := io.ReadAll(c.Request.Body)
		c.Request.Body.Close()
		c.Request.Body = io.NopCloser(bytes.NewReader(bodyBytes))
		c.Set(models.ContextRequestBody, string(bodyBytes))
		log.AccessLogger.Info(fmt.Sprintf("[%s] [%s] ->", requestId, transactionId), log.String("uri", c.Request.RequestURI), log.String("method", c.Request.Method), log.String("sourceIp", getRemoteIp(c)), log.String(models.ContextOperator, c.GetString(models.ContextOperator)), log.String(models.ContextRequestBody, c.GetString(models.ContextRequestBody)))
		c.Next()
		costTime := time.Now().Sub(start).Seconds() * 1000
		userId := c.GetString(models.ContextUserId)
		if log.DebugEnable {
			log.AccessLogger.Info(fmt.Sprintf("[%s] [%s] [%s] <-", requestId, transactionId, userId), log.String("uri", c.Request.RequestURI), log.String("method", c.Request.Method), log.Int("httpCode", c.Writer.Status()), log.Int(models.ContextErrorCode, c.GetInt(models.ContextErrorCode)), log.String(models.ContextErrorMessage, c.GetString(models.ContextErrorMessage)), log.Float64("costTime", costTime), log.String(models.ContextResponseBody, c.GetString(models.ContextResponseBody)))
		} else {
			log.AccessLogger.Info(fmt.Sprintf("[%s] [%s] [%s] <-", requestId, transactionId, userId), log.String("uri", c.Request.RequestURI), log.String("method", c.Request.Method), log.Int("httpCode", c.Writer.Status()), log.Int(models.ContextErrorCode, c.GetInt(models.ContextErrorCode)), log.String(models.ContextErrorMessage, c.GetString(models.ContextErrorMessage)), log.Float64("costTime", costTime))
		}
	}
}

func getRemoteIp(c *gin.Context) string {
	return c.ClientIP()
}

func recoverHandle(c *gin.Context, err interface{}) {
	var errorMessage string
	if err != nil {
		errorMessage = err.(error).Error()
	}
	log.Logger.Error("Handle recover error", log.Int("code", -2), log.String("message", errorMessage))
	c.JSON(http.StatusInternalServerError, models.HttpResponseMeta{Code: -2, Status: models.DefaultHttpErrorCode})
}

// @Summary 健康检查
// @description 健康检查
// @Tags 健康检查接口
// @Produce  application/json
// @Success 200 {object} models.ResponseJson
// @Router /health-check [get]
func healthCheck(c *gin.Context) {
	if err := db.CheckDbConnection(); err != nil {
		c.JSON(http.StatusInternalServerError, models.HttpResponseMeta{Status: models.DefaultHttpErrorCode, Message: err.Error()})
	} else {
		middleware.ReturnSuccess(c)
	}
}
