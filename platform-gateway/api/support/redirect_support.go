package support

import (
	"fmt"
	"io"
	"io/ioutil"
	"net/http"
	"net/http/httputil"
	"strings"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-gateway/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/model"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

type RedirectInvoke struct {
	TargetUrl string
	//RequestHandler  RequestHandlerFunc
	//ResponseHandler ResponseHandlerFunc
}

type RequestHandlerFunc func(request *http.Request, c *gin.Context) error

type ResponseHandlerFunc func(body *[]byte, c *gin.Context) error

func (invoke RedirectInvoke) Do(c *gin.Context) error {

	log.Info(nil, log.LOGGER_APP, fmt.Sprintf("Redirecting request to downstream system: [Method: %s] [URL: %s] [ContentLength: %d]", c.Request.Method, invoke.TargetUrl, c.Request.ContentLength))
	cloneRequest := c.Request.Clone(c.Request.Context()) // deep copy original request
	newRequest, _ := http.NewRequest(cloneRequest.Method, invoke.TargetUrl, cloneRequest.Body)
	newRequest.Header = cloneRequest.Header
	// pass through content length
	newRequest.ContentLength = c.Request.ContentLength
	if clientIp := c.ClientIP(); clientIp != "" {
		newRequest.Header.Set("X-Forwarded-For", clientIp)
	}
	newRequest.URL.RawQuery = cloneRequest.URL.RawQuery

	client := &http.Client{
		Timeout: 30 * time.Minute,
		CheckRedirect: func(req *http.Request, via []*http.Request) error {
			return http.ErrUseLastResponse // 阻止重定向
		},
	}

	if model.Config.ProxyConfig.Timeout > 0 {
		client.Timeout = time.Duration(model.Config.ProxyConfig.Timeout) * time.Minute
	}

	if strings.EqualFold(model.Config.Log.Level, "debug") {
		requestDump, _ := httputil.DumpRequest(newRequest, true)
		log.Debug(nil, log.LOGGER_APP, "Request to downstream system: "+string(requestDump))
	}
	log.Debug(nil, log.LOGGER_APP, fmt.Sprintf("Sending request to downstream system: [Method: %s] [URL: %s]", newRequest.Method, invoke.TargetUrl))

	response, err := client.Do(newRequest)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "Failed to send request to downstream system",
			zap.String("targetUrl", invoke.TargetUrl),
			zap.String("method", newRequest.Method),
			zap.Error(err))
		return fmt.Errorf("failed to send request to downstream system: %w", err)
	}
	defer response.Body.Close()

	// 复制响应头（排除不应该复制的系统级响应头）
	skipHeaders := map[string]bool{
		"Content-Length":    true, // Gin 会自动设置
		"Connection":        true, // 由 Gin 管理
		"Transfer-Encoding": true, // 由 Gin 管理
		"Content-Type":      true, // 会在 c.Data() 中设置
	}

	for k, v := range response.Header {
		if skipHeaders[k] {
			continue
		}
		// 正确复制响应头：遍历值数组，每个值单独添加
		for _, val := range v {
			c.Writer.Header().Add(k, val)
		}
		// 特殊处理业务响应头，使用 Set 确保只有一个值
		if (k == "Api-Code" || k == "Error-Code") && len(v) > 0 {
			c.Writer.Header().Set(k, v[0])
		}
	}

	responseContentType := response.Header.Get("Content-Type")
	if strings.Contains(responseContentType, "application/json") {
		respBody, readErr := ioutil.ReadAll(response.Body)
		defer response.Body.Close()

		if readErr != nil {
			log.Error(nil, log.LOGGER_APP, "Failed to read response body from downstream system",
				zap.String("targetUrl", invoke.TargetUrl),
				zap.Int("statusCode", response.StatusCode),
				zap.String("contentType", responseContentType),
				zap.Error(readErr))
			return fmt.Errorf("failed to read response body: %w", readErr)
		}

		if strings.EqualFold(model.Config.Log.Level, "debug") {
			responseDump, _ := httputil.DumpResponse(response, false)
			log.Debug(nil, log.LOGGER_APP, fmt.Sprintf("Response from downstream system: %s  [body size]: %d", string(responseDump), len(respBody)))
		}
		c.Data(response.StatusCode, responseContentType, respBody)
	} else {
		c.Status(response.StatusCode)
		c.Header("Content-Type", responseContentType)
		c.Header("Content-Length", response.Header.Get("Content-Length"))
		defer response.Body.Close()
		// 使用 c.Stream() 逐步转发数据流
		c.Stream(func(w io.Writer) bool {
			// 缓冲区（可根据实际情况调整大小）
			buf := make([]byte, 32*1024) // 32KB 缓冲区
			for {
				n, readErr := response.Body.Read(buf)
				if n > 0 {
					_, writeErr := w.Write(buf[:n])
					if writeErr != nil {
						return false // 发生错误，停止传输
					}
				}
				if readErr != nil {
					break // 读取完成或发生错误
				}
			}
			return false // 结束流
		})
		log.Info(nil, log.LOGGER_APP, "Success done with response",
			zap.String("targetUrl", invoke.TargetUrl),
			zap.Int("statusCode", response.StatusCode))
	}

	return nil
}
