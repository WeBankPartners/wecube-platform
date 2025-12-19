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
	startTime := time.Now()

	log.Debug(nil, log.LOGGER_APP, fmt.Sprintf("Redirecting request to downstream system: [Method: %s] [URL: %s] [ContentLength: %d]", c.Request.Method, invoke.TargetUrl, c.Request.ContentLength))
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

	log.Debug(nil, log.LOGGER_APP, "Sending request to downstream system",
		zap.String("targetUrl", invoke.TargetUrl),
		zap.String("method", newRequest.Method),
		zap.Int64("contentLength", newRequest.ContentLength))

	requestStartTime := time.Now()
	response, err := client.Do(newRequest)
	requestDuration := time.Since(requestStartTime)

	if err != nil {
		log.Error(nil, log.LOGGER_APP, "Failed to send request to downstream system",
			zap.String("targetUrl", invoke.TargetUrl),
			zap.String("method", newRequest.Method),
			zap.Duration("duration", requestDuration),
			zap.Error(err))
		return fmt.Errorf("failed to send request to downstream system: %w", err)
	}
	defer response.Body.Close()

	log.Debug(nil, log.LOGGER_APP, "Received response from downstream system",
		zap.String("targetUrl", invoke.TargetUrl),
		zap.Int("statusCode", response.StatusCode),
		zap.String("contentType", response.Header.Get("Content-Type")),
		zap.String("contentLength", response.Header.Get("Content-Length")),
		zap.Duration("requestDuration", requestDuration))

	// 复制响应头（排除不应该复制的系统级响应头）
	skipHeaders := map[string]bool{
		"Content-Length":    true, // Gin 会自动设置
		"Connection":        true, // 由 Gin 管理
		"Transfer-Encoding": true, // 由 Gin 管理
		"Content-Type":      true, // 会在 c.Data() 中设置
	}

	// 统计响应头信息
	headerCount := 0
	headerSize := 0
	headerNames := make([]string, 0)

	// 先处理业务响应头（Api-Code, Error-Code），使用 Set 确保只有一个值
	for k, v := range response.Header {
		if skipHeaders[k] {
			continue
		}
		if (k == "Api-Code" || k == "Error-Code") && len(v) > 0 {
			c.Writer.Header().Set(k, v[0])
			headerCount++
			headerSize += len(k) + len(v[0]) + 4
			headerNames = append(headerNames, k)
		}
	}

	// 再处理其他响应头
	for k, v := range response.Header {
		if skipHeaders[k] {
			continue
		}
		// 跳过已经处理过的业务响应头
		if k == "Api-Code" || k == "Error-Code" {
			continue
		}
		// 正确复制响应头：遍历值数组，每个值单独添加
		for _, val := range v {
			c.Writer.Header().Add(k, val)
			headerCount++
			headerSize += len(k) + len(val) + 4
		}
		headerNames = append(headerNames, k)
	}

	log.Debug(nil, log.LOGGER_APP, "Copied response headers",
		zap.String("targetUrl", invoke.TargetUrl),
		zap.Int("headerCount", headerCount),
		zap.Int("headerSize", headerSize),
		zap.Strings("headerNames", headerNames))

	if strings.Contains(responseContentType, "application/json") {
		log.Debug(nil, log.LOGGER_APP, "Reading JSON response body",
			zap.String("targetUrl", invoke.TargetUrl),
			zap.Int("statusCode", response.StatusCode),
			zap.String("contentType", responseContentType))

		readStartTime := time.Now()
		respBody, readErr := ioutil.ReadAll(response.Body)
		readDuration := time.Since(readStartTime)
		defer response.Body.Close()

		if readErr != nil {
			log.Error(nil, log.LOGGER_APP, "Failed to read response body from downstream system",
				zap.String("targetUrl", invoke.TargetUrl),
				zap.Int("statusCode", response.StatusCode),
				zap.String("contentType", responseContentType),
				zap.Duration("readDuration", readDuration),
				zap.Error(readErr))
			return fmt.Errorf("failed to read response body: %w", readErr)
		}

		log.Debug(nil, log.LOGGER_APP, "Read response body successfully",
			zap.String("targetUrl", invoke.TargetUrl),
			zap.Int("statusCode", response.StatusCode),
			zap.Int("bodySize", len(respBody)),
			zap.Duration("readDuration", readDuration))

		if strings.EqualFold(model.Config.Log.Level, "debug") {
			responseDump, _ := httputil.DumpResponse(response, false)
			log.Debug(nil, log.LOGGER_APP, fmt.Sprintf("Response from downstream system: %s  [body size]: %d", string(responseDump), len(respBody)))
		}

		log.Debug(nil, log.LOGGER_APP, "Writing response to client",
			zap.String("targetUrl", invoke.TargetUrl),
			zap.Int("statusCode", response.StatusCode),
			zap.String("contentType", responseContentType),
			zap.Int("bodySize", len(respBody)))

		writeStartTime := time.Now()
		c.Data(response.StatusCode, responseContentType, respBody)
		writeDuration := time.Since(writeStartTime)

		log.Debug(nil, log.LOGGER_APP, "Successfully wrote JSON response to client",
			zap.String("targetUrl", invoke.TargetUrl),
			zap.Int("statusCode", response.StatusCode),
			zap.Int("bodySize", len(respBody)),
			zap.Duration("writeDuration", writeDuration))
	} else {
		log.Debug(nil, log.LOGGER_APP, "Handling non-JSON response",
			zap.String("targetUrl", invoke.TargetUrl),
			zap.Int("statusCode", response.StatusCode),
			zap.String("contentType", responseContentType),
			zap.String("contentLength", response.Header.Get("Content-Length")))

		c.Status(response.StatusCode)
		c.Header("Content-Type", responseContentType)
		if contentLength := response.Header.Get("Content-Length"); contentLength != "" {
			c.Header("Content-Length", contentLength)
		}
		defer response.Body.Close()

		streamStartTime := time.Now()
		totalBytes := 0
		// 使用 c.Stream() 逐步转发数据流
		streamErr := c.Stream(func(w io.Writer) bool {
			// 缓冲区（可根据实际情况调整大小）
			buf := make([]byte, 32*1024) // 32KB 缓冲区
			for {
				n, readErr := response.Body.Read(buf)
				if n > 0 {
					written, writeErr := w.Write(buf[:n])
					totalBytes += written
					if writeErr != nil {
						log.Error(nil, log.LOGGER_APP, "Failed to write response stream",
							zap.String("targetUrl", invoke.TargetUrl),
							zap.Int("bytesWritten", totalBytes),
							zap.Error(writeErr))
						return false // 发生错误，停止传输
					}
				}
				if readErr == io.EOF {
					return false // 读取完成
				}
				if readErr != nil {
					log.Error(nil, log.LOGGER_APP, "Failed to read response stream",
						zap.String("targetUrl", invoke.TargetUrl),
						zap.Int("bytesRead", totalBytes),
						zap.Error(readErr))
					return false // 发生错误，停止传输
				}
			}
		})
		streamDuration := time.Since(streamStartTime)

		if streamErr != nil {
			log.Error(nil, log.LOGGER_APP, "Failed to stream response",
				zap.String("targetUrl", invoke.TargetUrl),
				zap.Int("statusCode", response.StatusCode),
				zap.Int("totalBytes", totalBytes),
				zap.Duration("streamDuration", streamDuration),
				zap.Error(streamErr))
			return fmt.Errorf("failed to stream response: %w", streamErr)
		}

		log.Debug(nil, log.LOGGER_APP, "Successfully streamed response to client",
			zap.String("targetUrl", invoke.TargetUrl),
			zap.Int("statusCode", response.StatusCode),
			zap.Int("totalBytes", totalBytes),
			zap.Duration("streamDuration", streamDuration))
	}

	totalDuration := time.Since(startTime)
	log.Debug(nil, log.LOGGER_APP, "Completed redirect request",
		zap.String("targetUrl", invoke.TargetUrl),
		zap.Int("statusCode", response.StatusCode),
		zap.Duration("totalDuration", totalDuration))

	return nil
}
