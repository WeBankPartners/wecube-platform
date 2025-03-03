package middleware

import (
	"encoding/json"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
	"net/http"
	"strconv"
)

const AcceptLanguageHeader = "Accept-Language"

func ReturnPageData(c *gin.Context, pageInfo models.PageInfo, contents interface{}) {
	if contents == nil {
		contents = []string{}
	}
	obj := models.ResponseJson{HttpResponseMeta: models.HttpResponseMeta{Code: 0, Status: models.DefaultHttpSuccessCode}, Data: models.ResponsePageData{PageInfo: pageInfo, Contents: contents}}
	bodyBytes, _ := json.Marshal(obj)
	c.Set("responseBody", string(bodyBytes))
	c.JSON(http.StatusOK, obj)
}

func ReturnData(c *gin.Context, data interface{}) {
	returnObj := models.ResponseJson{HttpResponseMeta: models.HttpResponseMeta{Code: 0, Status: models.DefaultHttpSuccessCode}, Data: data}
	if log.DebugEnable {
		bodyBytes, _ := json.Marshal(returnObj)
		c.Set(models.ContextResponseBody, string(bodyBytes))
	}
	c.Set(models.ContextErrorCode, 0)
	c.Set(models.ContextErrorKey, models.DefaultHttpSuccessCode)
	c.JSON(http.StatusOK, returnObj)
}

func ReturnDataWithStatus(c *gin.Context, data interface{}, status string) {
	returnObj := models.ResponseJson{HttpResponseMeta: models.HttpResponseMeta{Code: 0, Status: status}, Data: data}
	if log.DebugEnable {
		bodyBytes, _ := json.Marshal(returnObj)
		c.Set(models.ContextResponseBody, string(bodyBytes))
	}
	c.Set(models.ContextErrorCode, 0)
	c.Set(models.ContextErrorKey, status)
	c.JSON(http.StatusOK, returnObj)
}

func ReturnXMLData(c *gin.Context, data interface{}) {
	returnObj := models.ResponseJson{HttpResponseMeta: models.HttpResponseMeta{Code: 0, Status: models.DefaultHttpSuccessCode}, Data: data}
	if log.DebugEnable {
		bodyBytes, _ := json.Marshal(returnObj)
		c.Set(models.ContextResponseBody, string(bodyBytes))
	}
	c.Set(models.ContextErrorCode, 0)
	c.Set(models.ContextErrorKey, models.DefaultHttpSuccessCode)
	c.XML(http.StatusOK, data)
}

func Return(c *gin.Context, response interface{}) {
	if log.DebugEnable {
		bodyBytes, _ := json.Marshal(response)
		c.Set(models.ContextResponseBody, string(bodyBytes))
	}
	c.Set(models.ContextErrorCode, 0)
	c.Set(models.ContextErrorKey, models.DefaultHttpSuccessCode)
	c.JSON(http.StatusOK, response)
}

func ReturnSuccess(c *gin.Context) {
	ReturnData(c, nil)
}

func ReturnError(c *gin.Context, err error) {
	errorCode, errorKey, errorMessage := exterror.GetErrorResult(c.GetHeader("Accept-Language"), err, -1)
	if !exterror.IsBusinessErrorCode(errorCode) {
		log.Error(nil, log.LOGGER_APP, "systemError", zap.Int("errorCode", errorCode), zap.String("errorKey", errorKey), zap.String("message", errorMessage), zap.Error(err))
	} else {
		log.Error(nil, log.LOGGER_APP, "businessError", zap.Int("errorCode", errorCode), zap.String("errorKey", errorKey), zap.String("message", errorMessage), zap.Error(err))
	}
	errorKey = models.DefaultHttpErrorCode
	returnObj := models.HttpResponseMeta{Code: errorCode, Status: errorKey, Message: errorMessage}
	if log.DebugEnable {
		bodyBytes, _ := json.Marshal(returnObj)
		c.Set(models.ContextResponseBody, string(bodyBytes))
	}
	c.Set(models.ContextErrorKey, errorKey)
	c.Set(models.ContextErrorCode, errorCode)
	c.Set(models.ContextErrorMessage, errorMessage)
	c.Writer.Header().Add("Error-Code", strconv.Itoa(errorCode))
	c.JSON(http.StatusOK, returnObj)
}

func ReturnApiPermissionError(c *gin.Context) {
	log.Warn(nil, log.LOGGER_APP, "ReturnApiPermissionError", zap.String("url", c.Request.URL.Path), zap.String("method", c.Request.Method), zap.String("sourceIp", c.ClientIP()), zap.Strings("roles", GetRequestRoles(c)))
	ReturnError(c, exterror.New().ApiPermissionDeniedError)
}

func ReturnAuthError(c *gin.Context, err exterror.CustomError, token string) {
	errorCode, errorKey, errorMessage := exterror.GetErrorResult(c.GetHeader("Accept-Language"), err, -1)
	log.Error(nil, log.LOGGER_APP, "tokenValidateError", zap.Int("errorCode", errorCode), zap.String("errorKey", errorKey), zap.String("message", errorMessage), zap.Error(err), zap.String("token", token))
	errorKey = models.DefaultHttpErrorCode
	returnObj := models.HttpResponseMeta{Code: errorCode, Status: errorKey, Message: errorMessage}
	if log.DebugEnable {
		bodyBytes, _ := json.Marshal(returnObj)
		c.Set(models.ContextResponseBody, string(bodyBytes))
	}
	c.Set(models.ContextErrorKey, errorKey)
	c.Set(models.ContextErrorCode, errorCode)
	c.Set(models.ContextErrorMessage, errorMessage)
	c.JSON(http.StatusUnauthorized, returnObj)
}

func InitHttpError() {
	err := exterror.InitErrorTemplateList(models.Config.HttpServer.ErrorTemplateDir, models.Config.HttpServer.ErrorDetailReturn)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "Init error template list fail", zap.Error(err))
	}
}
