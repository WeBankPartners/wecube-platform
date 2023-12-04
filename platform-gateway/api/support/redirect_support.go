package support

import (
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/auth"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/gin-gonic/gin"
	"github.com/inmerits/inmerits-admin/scene-mgmt-server/api/model"
	"github.com/inmerits/inmerits-admin/scene-mgmt-server/common/config"
	"github.com/inmerits/inmerits-admin/scene-mgmt-server/common/constant"
	"github.com/inmerits/inmerits-admin/scene-mgmt-server/common/exterror"
	"github.com/inmerits/inmerits-admin/scene-mgmt-server/common/log"
	"io/ioutil"
	"net/http"
	"net/http/httputil"
	"strings"
)

type RedirectInvoke struct {
	TargetUrl       string
	RequestHandler  RequestHandlerFunc
	ResponseHandler ResponseHandlerFunc
}

type RequestHandlerFunc func(request *http.Request, c *gin.Context) error

type ResponseHandlerFunc func(body *[]byte, c *gin.Context) error

func (invoke RedirectInvoke) Do(c *gin.Context) {
	log.Logger.Info(fmt.Sprintf("Redirecting request [%s]-[%s] to downstream system: [Method: %s] [URL: %s]", c.GetHeader(constant.TransactionId), c.GetHeader(constant.RequestId), c.Request.Method, invoke.TargetUrl))
	cloneRequest := c.Request.Clone(c.Request.Context()) // deep copy original request

	if invoke.RequestHandler != nil {
		log.Logger.Info(fmt.Sprintf("Start to handle request [%s]-[%s]", c.GetHeader(constant.TransactionId), c.GetHeader(constant.RequestId)))
		if err := invoke.RequestHandler(cloneRequest, c); err != nil {
			ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("failed handle request: %s", err.Error())))
			return
		}
	}
	newRequest, _ := http.NewRequest(cloneRequest.Method, invoke.TargetUrl, cloneRequest.Body)
	newRequest.Header = cloneRequest.Header
	newRequestId := constant.PrefixSubSysId + guid.CreateGuid()
	newRequest.Header.Set(constant.RequestId, newRequestId)
	newRequest.URL.RawQuery = cloneRequest.URL.RawQuery
	auth.SetRequestSourceAuth(newRequest, config.Config.Auth.Source.AppId, config.Config.Auth.Source.PrivateKeyBytes)

	client := &http.Client{}
	if strings.EqualFold(config.Config.Log.Level, "debug") {
		requestDump, _ := httputil.DumpRequest(newRequest, true)
		log.Logger.Debug("Request to downstream system: " + string(requestDump))
	}
	log.Logger.Info(fmt.Sprintf("Sending request [%s]-[%s] to downstream system: [Method: %s] [URL: %s]", c.GetHeader(constant.TransactionId), newRequestId, newRequest.Method, invoke.TargetUrl))

	if response, err := client.Do(newRequest); err != nil {
		ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("error calling downstream system: %s", err.Error())))
		return
	} else {
		if strings.EqualFold(config.Config.Log.Level, "debug") {
			responseDump, _ := httputil.DumpResponse(response, true)
			log.Logger.Debug("Response from downstream system: " + string(responseDump))
		}

		if response.StatusCode >= 400 {
			ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("error from downstream system: %s", response.Status)))
			return
		}

		respBody, _ := ioutil.ReadAll(response.Body)
		defer response.Body.Close()
		var commonResp model.ResponseWrap
		if jsonErr := json.Unmarshal(respBody, &commonResp); jsonErr != nil {
			ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("failed unmarshal json: %s", jsonErr.Error())))
			return
		}
		if commonResp.ErrorCode != constant.DefaultHttpSuccessCode {
			var customErr exterror.CustomError
			if exterror.IsBusinessErrorCode(commonResp.ErrorCode) {
				customErr = exterror.CustomError{Code: commonResp.ErrorCode, Message: commonResp.ErrorMessage, PassEnable: true}
			} else {
				customErr = exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf(string(respBody)))
			}
			ReturnError(c, customErr)
			return
		}
		if invoke.ResponseHandler != nil {
			log.Logger.Info(fmt.Sprintf("Start to handle response body of request [%s]-[%s]", c.GetHeader(constant.TransactionId), newRequestId))
			if err := invoke.ResponseHandler(&respBody, c); err != nil {
				ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("failed handle response: %s", err.Error())))
				return
			}
		}

		c.Set("logOperation", true)
		c.Data(http.StatusOK, response.Header["Content-Type"][0], respBody)
		log.Logger.Info(fmt.Sprintf("Success request [%s]-[%s] with response body: %s", c.GetHeader(constant.TransactionId), newRequestId, respBody))
		return
	}

}
