package middleware

import (
	"bytes"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/model"
	"io/ioutil"
	"net/http/httputil"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
)

func HttpLogHandle() gin.HandlerFunc {
	return func(c *gin.Context) {
		start := time.Now()
		if c.Request.RequestURI != "/platform/v1/packages" && !strings.HasSuffix(c.Request.RequestURI, "/packages/upload") {
			bodyBytes, _ := ioutil.ReadAll(c.Request.Body)
			c.Request.Body.Close()
			c.Request.Body = ioutil.NopCloser(bytes.NewReader(bodyBytes))
			c.Set("requestBody", string(bodyBytes))
		}
		log.Logger.Info(fmt.Sprintf("Received request "), log.String("url", c.Request.RequestURI), log.String("method", c.Request.Method), log.String("body", c.GetString("requestBody")))
		if strings.EqualFold(model.Config.Log.Level, "debug") && c.Request.RequestURI != "/platform/v1/packages" && !strings.HasSuffix(c.Request.RequestURI, "/packages/upload") {
			requestDump, _ := httputil.DumpRequest(c.Request, true)
			log.Logger.Debug("Received request: " + string(requestDump))
		}
		c.Next()
		costTime := time.Now().Sub(start).Seconds() * 1000
		log.AccessLogger.Info(fmt.Sprintf("Got request -"), log.String("url", c.Request.RequestURI), log.String("method", c.Request.Method), log.Int("code", c.Writer.Status()), log.String("operator", c.GetString("user")), log.String("ip", getRemoteIp(c)), log.Float64("cost_ms", costTime), log.String("body", c.GetString("responseBody")))

	}
}

func getRemoteIp(c *gin.Context) string {
	netIp := c.RemoteIP()
	if len(netIp) > 0 {
		return netIp
	}
	return c.ClientIP()
}

func RecoverHandle(c *gin.Context, err interface{}) {
	var errorMessage string
	if err != nil {
		errorMessage = err.(error).Error()
	}
	log.Logger.Error("Handle error", log.Int("errorCode", 1), log.String("message", errorMessage))
	//c.JSON(http.StatusInternalServerError, model.ResponseWrap{ErrorCode: 10400001, ErrorMessage: errorMessage})
}
