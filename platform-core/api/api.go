package api

import (
	"bytes"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
	"io"
	"net/http"
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
	authRouter := r.Group(models.GlobalProjectName, middleware.AuthToken)
	for _, funcObj := range httpHandlerFuncList {
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
			requestId = guid.CreateGuid()
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
