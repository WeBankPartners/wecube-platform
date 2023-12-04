package support

import (
	"encoding/json"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/model"
	"net/http"

	"github.com/gin-gonic/gin"
)

func ReturnData(c *gin.Context, data interface{}) {
	obj := model.ResponseWrap{Status: model.StatusOK, Message: model.OkMessage, Data: data}
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
	obj := model.ResponseWrap{Status: model.StatusError, Message: err.Error()}
	c.JSON(httpCode, obj)
}

func ReturnSuccess(c *gin.Context) {
	obj := model.ResponseWrap{Status: model.StatusOK, Message: model.OkMessage}
	bodyBytes, _ := json.Marshal(obj)
	c.Set("responseBody", string(bodyBytes))
	c.Set("logOperation", true)
	c.JSON(http.StatusOK, obj)
}
