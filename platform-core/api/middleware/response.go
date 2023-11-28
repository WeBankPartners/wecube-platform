package middleware

import (
	"encoding/json"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
	"net/http"
)

func ReturnPageData(c *gin.Context, pageInfo models.PageInfo, contents interface{}) {
	if contents == nil {
		contents = []string{}
	}
	obj := models.ResponseJson{StatusCode: models.DefaultHttpSuccessCode, Data: models.ResponsePageData{PageInfo: pageInfo, Contents: contents}}
	bodyBytes, _ := json.Marshal(obj)
	c.Set(models.ContextResponseBody, string(bodyBytes))
	c.JSON(http.StatusOK, obj)
}

func ReturnEmptyPageData(c *gin.Context) {
	c.JSON(http.StatusOK, models.ResponseJson{StatusCode: models.DefaultHttpSuccessCode, Data: models.ResponsePageData{PageInfo: models.PageInfo{StartIndex: 0, PageSize: 0, TotalRows: 0}, Contents: []string{}}})
}

func ReturnData(c *gin.Context, data interface{}) {
	if data == nil {
		data = []string{}
	}
	obj := models.ResponseJson{StatusCode: models.DefaultHttpSuccessCode, Data: data}
	bodyBytes, _ := json.Marshal(obj)
	c.Set(models.ContextResponseBody, string(bodyBytes))
	c.JSON(http.StatusOK, obj)
}

func ReturnSuccess(c *gin.Context) {
	c.Set(models.ContextResponseBody, "{\"statusCode\":\"OK\",\"data\":[]}")
	c.JSON(http.StatusOK, models.ResponseJson{StatusCode: models.DefaultHttpSuccessCode, Data: []string{}})
}

func ReturnError(c *gin.Context, statusCode, statusMessage string, data interface{}) {
	if data == nil {
		data = []string{}
	}
	log.Logger.Error("Handle error", log.String("statusCode", statusCode), log.String("message", statusMessage))
	obj := models.ResponseErrorJson{StatusCode: statusCode, StatusMessage: statusMessage, Data: data}
	bodyBytes, _ := json.Marshal(obj)
	c.Set(models.ContextResponseBody, string(bodyBytes))
	c.JSON(http.StatusOK, obj)
}

func InitHttpError() {
	err := exterror.InitErrorTemplateList(models.Config.HttpServer.ErrorTemplateDir, models.Config.HttpServer.ErrorDetailReturn)
	if err != nil {
		log.Logger.Error("Init error template list fail", log.Error(err))
	}
}
