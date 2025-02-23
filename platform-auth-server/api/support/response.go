package support

import (
	"encoding/json"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"go.uber.org/zap"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

func ReturnPageData(c *gin.Context, pageInfo model.PageInfo, contents interface{}) {
	if contents == nil {
		contents = []string{}
	}
	obj := model.ResponseWrap{Status: model.ResponseStatusOk, Message: model.ResponseMessageOk, Data: model.ResponsePageData{PageInfo: pageInfo, Contents: contents}}
	bodyBytes, _ := json.Marshal(obj)
	c.Set("responseBody", string(bodyBytes))
	c.JSON(http.StatusOK, obj)
}

func ReturnData(c *gin.Context, data interface{}) {
	obj := model.ResponseWrap{Status: model.ResponseStatusOk, Message: model.ResponseMessageOk, Data: data}
	bodyBytes, _ := json.Marshal(obj)
	c.Set("responseBody", string(bodyBytes))
	c.Set("logOperation", true)
	log.Debug(nil, log.LOGGER_APP, "Handle success with data response", log.JsonObj("response", obj))
	c.JSON(http.StatusOK, obj)
}

func ReturnError(c *gin.Context, err error) {
	ReturnErrorWithHttpCode(c, err, http.StatusOK)
}

func ReturnErrorWithHttpCode(c *gin.Context, err error, httpCode int) {
	// c.GetHeader("Accept-Language") 由于 auth-server没有做中文国际化,当成英文处理
	errorResponse := exterror.GetErrorResult("", err)
	if !exterror.IsBusinessErrorCode(errorResponse.ErrorCode) {
		log.Error(nil, log.LOGGER_ACCESS, "systemError", zap.Int("errorCode", errorResponse.ErrorCode), zap.String("message", errorResponse.Message), zap.Error(err))
		log.Error(nil, log.LOGGER_APP, "Return error", zap.Int("errorCode", errorResponse.ErrorCode), zap.String("message", errorResponse.Message), zap.Error(err))
	} else {
		log.Warn(nil, log.LOGGER_APP, "Return business error", zap.Int("errorCode", errorResponse.ErrorCode), zap.String("message", errorResponse.Message), zap.Error(err))
	}
	bodyBytes, _ := json.Marshal(errorResponse)
	c.Set("responseBody", string(bodyBytes))
	c.Writer.Header().Add("Error-Code", strconv.Itoa(errorResponse.ErrorCode))
	c.JSON(httpCode, errorResponse)
}

func ReturnSuccess(c *gin.Context) {
	obj := model.ResponseWrap{Status: model.ResponseStatusOk, Message: model.ResponseMessageOk}
	bodyBytes, _ := json.Marshal(obj)
	c.Set("responseBody", string(bodyBytes))
	c.Set("logOperation", true)
	c.JSON(http.StatusOK, obj)
}
