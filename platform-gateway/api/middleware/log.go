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
		var errCode string
		if strings.EqualFold(model.Config.Log.Level, "debug") && c.Request.RequestURI != "/platform/v1/packages" && !strings.HasSuffix(c.Request.RequestURI, "/packages/upload") {
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
		apiCode := c.Writer.Header().Get("Api-Code")
		// 业务错误码
		var subCode, subErrorCode string
		errCode = c.Writer.Header().Get("Error-Code")
		if strings.HasPrefix(c.Request.RequestURI, "/platform") {
			subCode = model.Config.SubSystemCode.Core
			apiCode = fmt.Sprintf("platform_%s", apiCode)
			// platform-core 错误码,以1开头表示技术类错误,其他则是业务类错误,本身就是8位,不需要扩展
			if strings.HasPrefix(errCode, "1") {
				// 技术错误
				subErrorCode = subCode + fmt.Sprintf("T%s", errCode)
			} else {
				// 业务错误
				subErrorCode = subCode + fmt.Sprintf("B%s", errCode)
			}
		} else if strings.HasPrefix(c.Request.RequestURI, "/auth") {
			subCode = model.Config.SubSystemCode.Auth
			apiCode = fmt.Sprintf("auth_%s", apiCode)
			// platform-auth 错误码,以3开头表示业务类错误,其他则是技术类错误,本身4位,需要前面扩展4个0
			if strings.HasPrefix(errCode, "3") {
				// 业务错误
				subErrorCode = subCode + fmt.Sprintf("B0000%s", errCode)
			} else {
				// 非业务错误
				subErrorCode = subCode + fmt.Sprintf("T0000%s", errCode)
			}
		} else {
			subCode = model.Config.SubSystemCode.Plugin
			apiCode = fmt.Sprintf("plugin_%s", apiCode)
		}
		if c.Writer.Status() == 200 {
			// 状态码 200 需要区分是否为业务错误
			if strings.TrimSpace(errCode) == "" {
				// errCode 为空,表示请求正常,对应errCode=0
				errCode = "0"
			} else {
				errCode = subErrorCode
			}
		} else {
			// 状态码 不为200 都是技术错误,T表示技术类错误
			errCode = model.Config.SubSystemCode.Core + fmt.Sprintf("T00000%d", c.Writer.Status())
		}
		log.AccessLogger.Info(fmt.Sprintf("Got request -"), log.String("url", c.Request.RequestURI), log.String("apiCode", apiCode), log.String("method", c.Request.Method),
			log.Int("code", c.Writer.Status()), log.String("errCode", errCode), log.String("operator", c.GetString("user")), log.String("ip", getRemoteIp(c)), log.Float64("cost_ms", costTime),
			log.String("body", c.GetString("responseBody")))

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
