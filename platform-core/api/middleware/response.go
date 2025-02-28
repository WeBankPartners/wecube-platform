package middleware

import (
	"encoding/json"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
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
		log.Logger.Error("systemError", log.Int("errorCode", errorCode), log.String("errorKey", errorKey), log.String("message", errorMessage), log.Error(err))
	} else {
		log.Logger.Error("businessError", log.Int("errorCode", errorCode), log.String("errorKey", errorKey), log.String("message", errorMessage), log.Error(err))
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
	log.Logger.Warn("ReturnApiPermissionError", log.String("url", c.Request.URL.Path), log.String("method", c.Request.Method), log.String("sourceIp", c.ClientIP()), log.StringList("roles", GetRequestRoles(c)))
	ReturnError(c, exterror.New().ApiPermissionDeniedError)
}

func ReturnAuthError(c *gin.Context, err exterror.CustomError, token string) {
	errorCode, errorKey, errorMessage := exterror.GetErrorResult(c.GetHeader("Accept-Language"), err, -1)
	log.Logger.Error("tokenValidateError", log.Int("errorCode", errorCode), log.String("errorKey", errorKey), log.String("message", errorMessage), log.Error(err), log.String("token", token))
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
		log.Logger.Error("Init error template list fail", log.Error(err))
	}
}
