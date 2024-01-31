package support

import (
	"fmt"
	"io/ioutil"
	"net/http"
	"net/http/httputil"
	"strings"

	"github.com/WeBankPartners/wecube-platform/platform-gateway/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/model"
	"github.com/gin-gonic/gin"
)

type RedirectInvoke struct {
	TargetUrl string
	//RequestHandler  RequestHandlerFunc
	//ResponseHandler ResponseHandlerFunc
}

type RequestHandlerFunc func(request *http.Request, c *gin.Context) error

type ResponseHandlerFunc func(body *[]byte, c *gin.Context) error

func (invoke RedirectInvoke) Do(c *gin.Context) error {
	log.Logger.Info(fmt.Sprintf("Redirecting request to downstream system: [Method: %s] [URL: %s]", c.Request.Method, invoke.TargetUrl))
	cloneRequest := c.Request.Clone(c.Request.Context()) // deep copy original request

	/*	if invoke.RequestHandler != nil {
			log.Logger.Info(fmt.Sprintf("Start to handle request [%s]-[%s]", c.GetHeader(constant.TransactionId), c.GetHeader(constant.RequestId)))
			if err := invoke.RequestHandler(cloneRequest, c); err != nil {
				ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("failed handle request: %s", err.Error())))
				return
			}
		}
	*/
	newRequest, _ := http.NewRequest(cloneRequest.Method, invoke.TargetUrl, cloneRequest.Body)
	newRequest.Header = cloneRequest.Header
	// pass through content length
	newRequest.ContentLength = c.Request.ContentLength
	if clientIp := c.ClientIP(); clientIp != "" {
		newRequest.Header.Set("X-Forwarded-For", clientIp)
	}
	newRequest.URL.RawQuery = cloneRequest.URL.RawQuery
	//auth.SetRequestSourceAuth(newRequest, config.Config.Auth.Source.AppId, config.Config.Auth.Source.PrivateKeyBytes)

	client := &http.Client{}
	if strings.EqualFold(model.Config.Log.Level, "debug") {
		requestDump, _ := httputil.DumpRequest(newRequest, true)
		log.Logger.Debug("Request to downstream system: " + string(requestDump))
	}
	log.Logger.Debug(fmt.Sprintf("Sending request to downstream system: [Method: %s] [URL: %s]", newRequest.Method, invoke.TargetUrl))

	if response, err := client.Do(newRequest); err != nil {
		return err
	} else {
		if strings.EqualFold(model.Config.Log.Level, "debug") {
			responseDump, _ := httputil.DumpResponse(response, true)
			log.Logger.Debug("Response from downstream system: " + string(responseDump))
		}

		/*		if response.StatusCode >= 400 {
				ReturnError(c, fmt.Errorf("error from downstream system: %s", response.Status))
				return
			}*/

		respBody, _ := ioutil.ReadAll(response.Body)
		defer response.Body.Close()

		respHeader := c.Writer.Header()
		for k, v := range response.Header {
			respHeader[k] = v
		}
		c.Data(response.StatusCode, response.Header.Get("Content-Type"), respBody)

		log.Logger.Debug(fmt.Sprintf("Success request with response body: %s", respBody))
		return nil
	}

}
