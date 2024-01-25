package support

import (
	"encoding/json"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"net/http"

	"github.com/gin-gonic/gin"
)

func ReturnData(c *gin.Context, data interface{}) {
	obj := model.ResponseWrap{Status: model.ResponseStatusOk, Message: model.ResponseMessageOk, Data: data}
	bodyBytes, _ := json.Marshal(obj)
	c.Set("responseBody", string(bodyBytes))
	c.Set("logOperation", true)
	log.Logger.Debug("Handle success with data response", log.JsonObj("response", obj))
	c.JSON(http.StatusOK, obj)
}

func ReturnError(c *gin.Context, err error) {
	ReturnErrorWithHttpCode(c, err, http.StatusOK)
}

func ReturnErrorWithHttpCode(c *gin.Context, err error, httpCode int) {
	errorResponse := exterror.GetErrorResult(c.GetHeader("Accept-Language"), err)
	if !exterror.IsBusinessErrorCode(errorResponse.ErrorCode) {
		log.AccessLogger.Error("systemError", log.Int("errorCode", errorResponse.ErrorCode), log.String("message", errorResponse.Message), log.Error(err))
		log.Logger.Error("Return error", log.Int("errorCode", errorResponse.ErrorCode), log.String("message", errorResponse.Message), log.Error(err))
	} else {
		log.Logger.Warn("Return business error", log.Int("errorCode", errorResponse.ErrorCode), log.String("message", errorResponse.Message), log.Error(err))
	}
	bodyBytes, _ := json.Marshal(errorResponse)
	c.Set("responseBody", string(bodyBytes))
	c.JSON(httpCode, errorResponse)
}

func ReturnSuccess(c *gin.Context) {
	obj := model.ResponseWrap{Status: model.ResponseStatusOk, Message: model.ResponseMessageOk}
	bodyBytes, _ := json.Marshal(obj)
	c.Set("responseBody", string(bodyBytes))
	c.Set("logOperation", true)
	c.JSON(http.StatusOK, obj)
}
