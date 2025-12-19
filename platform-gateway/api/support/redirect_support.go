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

	// 判断是否是 mfa/verify 请求，用于决定日志级别
	isMfaVerify := strings.Contains(c.Request.URL.Path, "mfa/verify") || strings.Contains(invoke.TargetUrl, "mfa/verify")

	if isMfaVerify {
		log.Info(nil, log.LOGGER_APP, "[MFA_VERIFY] Start redirecting request to downstream system",
			zap.String("method", c.Request.Method),
			zap.String("url", c.Request.URL.Path),
			zap.String("targetUrl", invoke.TargetUrl),
			zap.Int64("contentLength", c.Request.ContentLength))
	} else {
		log.Debug(nil, log.LOGGER_APP, fmt.Sprintf("Redirecting request to downstream system: [Method: %s] [URL: %s] [ContentLength: %d]", c.Request.Method, invoke.TargetUrl, c.Request.ContentLength))
	}
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

	if isMfaVerify {
		log.Info(nil, log.LOGGER_APP, "[MFA_VERIFY] Sending request to downstream system",
			zap.String("targetUrl", invoke.TargetUrl),
			zap.String("method", newRequest.Method),
			zap.Int64("contentLength", newRequest.ContentLength))
	} else {
		log.Debug(nil, log.LOGGER_APP, "Sending request to downstream system",
			zap.String("targetUrl", invoke.TargetUrl),
			zap.String("method", newRequest.Method),
			zap.Int64("contentLength", newRequest.ContentLength))
	}

	requestStartTime := time.Now()
	response, err := client.Do(newRequest)
	requestDuration := time.Since(requestStartTime)

	if err != nil {
		if isMfaVerify {
			log.Error(nil, log.LOGGER_APP, "[MFA_VERIFY] Failed to send request to downstream system",
				zap.String("targetUrl", invoke.TargetUrl),
				zap.String("method", newRequest.Method),
				zap.Duration("duration", requestDuration),
				zap.Error(err))
		} else {
			log.Error(nil, log.LOGGER_APP, "Failed to send request to downstream system",
				zap.String("targetUrl", invoke.TargetUrl),
				zap.String("method", newRequest.Method),
				zap.Duration("duration", requestDuration),
				zap.Error(err))
		}
		return fmt.Errorf("failed to send request to downstream system: %w", err)
	}
	defer response.Body.Close()

	if isMfaVerify {
		log.Info(nil, log.LOGGER_APP, "[MFA_VERIFY] Received response from downstream system",
			zap.String("targetUrl", invoke.TargetUrl),
			zap.Int("statusCode", response.StatusCode),
			zap.String("contentType", response.Header.Get("Content-Type")),
			zap.String("contentLength", response.Header.Get("Content-Length")),
			zap.String("requestDuration", requestDuration.String()),
			zap.Int64("requestDurationMs", requestDuration.Milliseconds()))
	} else {
		log.Debug(nil, log.LOGGER_APP, "Received response from downstream system",
			zap.String("targetUrl", invoke.TargetUrl),
			zap.Int("statusCode", response.StatusCode),
			zap.String("contentType", response.Header.Get("Content-Type")),
			zap.String("contentLength", response.Header.Get("Content-Length")),
			zap.Duration("requestDuration", requestDuration))
	}

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
	headerDetails := make(map[string]int) // 记录每个响应头的大小

	// 先处理业务响应头（Api-Code, Error-Code），使用 Set 确保只有一个值
	for k, v := range response.Header {
		if skipHeaders[k] {
			continue
		}
		if (k == "Api-Code" || k == "Error-Code") && len(v) > 0 {
			c.Writer.Header().Set(k, v[0])
			headerCount++
			size := len(k) + len(v[0]) + 4 // key + value + ": " + "\r\n"
			headerSize += size
			headerDetails[k] = size
			headerNames = append(headerNames, k)
		}
	}

	// 再处理其他响应头
	// 对于某些关键 header（如 Authorization），应该使用 Set 确保只有一个值
	singleValueHeaders := map[string]bool{
		"Authorization":      true,
		"Authorization-Info": true,
		"Set-Cookie":         true,
		"Location":           true,
	}

	for k, v := range response.Header {
		if skipHeaders[k] {
			continue
		}
		// 跳过已经处理过的业务响应头
		if k == "Api-Code" || k == "Error-Code" {
			continue
		}

		// 对于单值 header，使用 Set；对于多值 header，使用 Add
		if singleValueHeaders[k] && len(v) > 0 {
			c.Writer.Header().Set(k, v[0])
			headerCount++
			size := len(k) + len(v[0]) + 4 // key + value + ": " + "\r\n"
			headerSize += size
			headerDetails[k] = size
			headerNames = append(headerNames, k)
		} else {
			// 正确复制响应头：遍历值数组，每个值单独添加
			for _, val := range v {
				c.Writer.Header().Add(k, val)
				headerCount++
				size := len(k) + len(val) + 4 // key + value + ": " + "\r\n"
				headerSize += size
				if existingSize, exists := headerDetails[k]; exists {
					headerDetails[k] = existingSize + size
				} else {
					headerDetails[k] = size
				}
			}
			headerNames = append(headerNames, k)
		}
	}

	if isMfaVerify {
		// 特别记录 Authorization header 的大小
		authHeader := response.Header.Get("Authorization")
		authHeaderSize := 0
		if authHeader != "" {
			authHeaderSize = len(authHeader)
		}

		// 检查是否超过 Nginx proxy_buffer_size (128k = 131072 bytes)
		nginxBufferSize := 128 * 1024 // 128k
		exceedsNginxLimit := headerSize > nginxBufferSize

		log.Info(nil, log.LOGGER_APP, "[MFA_VERIFY] Copied response headers",
			zap.String("targetUrl", invoke.TargetUrl),
			zap.Int("headerCount", headerCount),
			zap.Int("headerSize", headerSize),
			zap.Int("nginxBufferSize", nginxBufferSize),
			zap.Bool("exceedsNginxLimit", exceedsNginxLimit),
			zap.Int("authHeaderSize", authHeaderSize),
			zap.Strings("headerNames", headerNames),
			zap.Any("headerDetails", headerDetails))

		if exceedsNginxLimit {
			log.Warn(nil, log.LOGGER_APP, "[MFA_VERIFY] Response header size exceeds Nginx proxy_buffer_size limit",
				zap.Int("headerSize", headerSize),
				zap.Int("nginxBufferSize", nginxBufferSize),
				zap.Int("excessSize", headerSize-nginxBufferSize))
		}
	} else {
		log.Debug(nil, log.LOGGER_APP, "Copied response headers",
			zap.String("targetUrl", invoke.TargetUrl),
			zap.Int("headerCount", headerCount),
			zap.Int("headerSize", headerSize),
			zap.Strings("headerNames", headerNames))
	}

	responseContentType := response.Header.Get("Content-Type")
	var respBodySize int
	if strings.Contains(responseContentType, "application/json") {
		if isMfaVerify {
			log.Info(nil, log.LOGGER_APP, "[MFA_VERIFY] Reading JSON response body",
				zap.String("targetUrl", invoke.TargetUrl),
				zap.Int("statusCode", response.StatusCode),
				zap.String("contentType", responseContentType))
		} else {
			log.Debug(nil, log.LOGGER_APP, "Reading JSON response body",
				zap.String("targetUrl", invoke.TargetUrl),
				zap.Int("statusCode", response.StatusCode),
				zap.String("contentType", responseContentType))
		}

		readStartTime := time.Now()
		respBody, readErr := ioutil.ReadAll(response.Body)
		readDuration := time.Since(readStartTime)
		respBodySize = len(respBody)
		defer response.Body.Close()

		if readErr != nil {
			if isMfaVerify {
				log.Error(nil, log.LOGGER_APP, "[MFA_VERIFY] Failed to read response body from downstream system",
					zap.String("targetUrl", invoke.TargetUrl),
					zap.Int("statusCode", response.StatusCode),
					zap.String("contentType", responseContentType),
					zap.Duration("readDuration", readDuration),
					zap.Error(readErr))
			} else {
				log.Error(nil, log.LOGGER_APP, "Failed to read response body from downstream system",
					zap.String("targetUrl", invoke.TargetUrl),
					zap.Int("statusCode", response.StatusCode),
					zap.String("contentType", responseContentType),
					zap.Duration("readDuration", readDuration),
					zap.Error(readErr))
			}
			return fmt.Errorf("failed to read response body: %w", readErr)
		}

		if isMfaVerify {
			log.Info(nil, log.LOGGER_APP, "[MFA_VERIFY] Read response body successfully",
				zap.String("targetUrl", invoke.TargetUrl),
				zap.Int("statusCode", response.StatusCode),
				zap.Int("bodySize", len(respBody)),
				zap.String("readDuration", readDuration.String()),
				zap.Int64("readDurationMs", readDuration.Milliseconds()))
		} else {
			log.Debug(nil, log.LOGGER_APP, "Read response body successfully",
				zap.String("targetUrl", invoke.TargetUrl),
				zap.Int("statusCode", response.StatusCode),
				zap.Int("bodySize", len(respBody)),
				zap.Duration("readDuration", readDuration))
		}

		if strings.EqualFold(model.Config.Log.Level, "debug") {
			responseDump, _ := httputil.DumpResponse(response, false)
			log.Debug(nil, log.LOGGER_APP, fmt.Sprintf("Response from downstream system: %s  [body size]: %d", string(responseDump), len(respBody)))
		}

		// 在写入响应之前，记录实际要写入的响应头（用于调试）
		if isMfaVerify {
			// 记录所有响应头的实际值
			actualHeaders := make(map[string][]string)
			for k, v := range c.Writer.Header() {
				actualHeaders[k] = v
			}

			// 检查响应头中是否有特殊字符
			hasInvalidChars := false
			invalidHeaderNames := make([]string, 0)
			for k, v := range actualHeaders {
				for _, val := range v {
					// 检查是否包含换行符、回车符等控制字符
					if strings.Contains(val, "\n") || strings.Contains(val, "\r") || strings.Contains(k, "\n") || strings.Contains(k, "\r") {
						hasInvalidChars = true
						invalidHeaderNames = append(invalidHeaderNames, k)
					}
				}
			}

			log.Info(nil, log.LOGGER_APP, "[MFA_VERIFY] Writing response to client",
				zap.String("targetUrl", invoke.TargetUrl),
				zap.Int("statusCode", response.StatusCode),
				zap.String("contentType", responseContentType),
				zap.Int("bodySize", len(respBody)),
				zap.Any("actualHeaders", actualHeaders),
				zap.Bool("hasInvalidChars", hasInvalidChars),
				zap.Strings("invalidHeaderNames", invalidHeaderNames))
		} else {
			log.Debug(nil, log.LOGGER_APP, "Writing response to client",
				zap.String("targetUrl", invoke.TargetUrl),
				zap.Int("statusCode", response.StatusCode),
				zap.String("contentType", responseContentType),
				zap.Int("bodySize", len(respBody)))
		}

		writeStartTime := time.Now()
		c.Data(response.StatusCode, responseContentType, respBody)
		writeDuration := time.Since(writeStartTime)

		// 检查写入后的状态
		writtenStatusCode := c.Writer.Status()
		writtenHeaders := make(map[string][]string)
		for k, v := range c.Writer.Header() {
			writtenHeaders[k] = v
		}

		if isMfaVerify {
			log.Info(nil, log.LOGGER_APP, "[MFA_VERIFY] Successfully wrote JSON response to client",
				zap.String("targetUrl", invoke.TargetUrl),
				zap.Int("expectedStatusCode", response.StatusCode),
				zap.Int("writtenStatusCode", writtenStatusCode),
				zap.Int("bodySize", len(respBody)),
				zap.String("writeDuration", writeDuration.String()),
				zap.Int64("writeDurationMs", writeDuration.Milliseconds()),
				zap.Any("writtenHeaders", writtenHeaders))

			// 检查状态码是否匹配
			if writtenStatusCode != response.StatusCode {
				log.Warn(nil, log.LOGGER_APP, "[MFA_VERIFY] Status code mismatch",
					zap.Int("expectedStatusCode", response.StatusCode),
					zap.Int("writtenStatusCode", writtenStatusCode))
			}
		} else {
			log.Debug(nil, log.LOGGER_APP, "Successfully wrote JSON response to client",
				zap.String("targetUrl", invoke.TargetUrl),
				zap.Int("statusCode", response.StatusCode),
				zap.Int("bodySize", len(respBody)),
				zap.Duration("writeDuration", writeDuration))
		}
	} else {
		if isMfaVerify {
			log.Info(nil, log.LOGGER_APP, "[MFA_VERIFY] Handling non-JSON response",
				zap.String("targetUrl", invoke.TargetUrl),
				zap.Int("statusCode", response.StatusCode),
				zap.String("contentType", responseContentType),
				zap.String("contentLength", response.Header.Get("Content-Length")))
		} else {
			log.Debug(nil, log.LOGGER_APP, "Handling non-JSON response",
				zap.String("targetUrl", invoke.TargetUrl),
				zap.Int("statusCode", response.StatusCode),
				zap.String("contentType", responseContentType),
				zap.String("contentLength", response.Header.Get("Content-Length")))
		}

		c.Status(response.StatusCode)
		c.Header("Content-Type", responseContentType)
		if contentLength := response.Header.Get("Content-Length"); contentLength != "" {
			c.Header("Content-Length", contentLength)
		}
		defer response.Body.Close()

		streamStartTime := time.Now()
		totalBytes := 0
		streamError := false
		// 使用 c.Stream() 逐步转发数据流
		clientDisconnected := c.Stream(func(w io.Writer) bool {
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
						streamError = true
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
					streamError = true
					return false // 发生错误，停止传输
				}
			}
		})
		streamDuration := time.Since(streamStartTime)

		if clientDisconnected {
			log.Warn(nil, log.LOGGER_APP, "Client disconnected during stream",
				zap.String("targetUrl", invoke.TargetUrl),
				zap.Int("statusCode", response.StatusCode),
				zap.Int("totalBytes", totalBytes),
				zap.Duration("streamDuration", streamDuration))
		} else if streamError {
			log.Error(nil, log.LOGGER_APP, "Error occurred during stream",
				zap.String("targetUrl", invoke.TargetUrl),
				zap.Int("statusCode", response.StatusCode),
				zap.Int("totalBytes", totalBytes),
				zap.Duration("streamDuration", streamDuration))
			return fmt.Errorf("error occurred during stream response")
		}

		if isMfaVerify {
			log.Info(nil, log.LOGGER_APP, "[MFA_VERIFY] Successfully streamed response to client",
				zap.String("targetUrl", invoke.TargetUrl),
				zap.Int("statusCode", response.StatusCode),
				zap.Int("totalBytes", totalBytes),
				zap.Duration("streamDuration", streamDuration))
		} else {
			log.Debug(nil, log.LOGGER_APP, "Successfully streamed response to client",
				zap.String("targetUrl", invoke.TargetUrl),
				zap.Int("statusCode", response.StatusCode),
				zap.Int("totalBytes", totalBytes),
				zap.Duration("streamDuration", streamDuration))
		}
	}

	totalDuration := time.Since(startTime)
	if isMfaVerify {
		// 记录响应头详情（特别是 Authorization header 的大小）
		authHeader := c.Writer.Header().Get("Authorization")
		authHeaderSize := 0
		if authHeader != "" {
			authHeaderSize = len(authHeader)
		}

		log.Info(nil, log.LOGGER_APP, "[MFA_VERIFY] Completed redirect request",
			zap.String("targetUrl", invoke.TargetUrl),
			zap.Int("statusCode", response.StatusCode),
			zap.String("totalDuration", totalDuration.String()),
			zap.Int64("totalDurationMs", totalDuration.Milliseconds()),
			zap.String("requestDuration", requestDuration.String()),
			zap.Int64("requestDurationMs", requestDuration.Milliseconds()),
			zap.Int("headerSize", headerSize),
			zap.Int("authHeaderSize", authHeaderSize),
			zap.Int("bodySize", respBodySize),
			zap.String("contentType", responseContentType))
	} else {
		log.Debug(nil, log.LOGGER_APP, "Completed redirect request",
			zap.String("targetUrl", invoke.TargetUrl),
			zap.Int("statusCode", response.StatusCode),
			zap.Duration("totalDuration", totalDuration))
	}

	return nil
}
