package remote_route_config

import (
	"errors"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/model"
	"net"
	"net/http"
	"net/url"
	"strings"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-gateway/common/constant"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/common/log"
	resty "github.com/go-resty/resty/v2"
	"github.com/mitchellh/mapstructure"
)

const (
	ContentType    = "Content-Type"
	DefContentType = "application/json"

	TransactionId = "transactionId"
	RequestId     = "requestId"

	Create    int8 = 0
	Update    int8 = 1
	Suspend   int8 = 2
	Unsuspend int8 = 3
)

type ResponseWrap struct {
	Status  string `json:"status"`
	Message string `json:"message"`
	Data    any    `json:"data"`
}

type RemoteServiceInvoke struct {
	Url         string
	Method      string //http.Method
	Headers     map[string]string
	QueryParams map[string]interface{} //for Get method
	//TransactionId string
}

type DaServiceResponse struct {
	ErrorCode int32 `json:"errorCode"`

	ErrorMessage string `json:"errorMessage,omitempty"`

	Data interface{} `json:"data,omitempty"`
}

var (
	ErrNetworkTimeOut = errors.New("remote request time out")
)

func Execute(serviceInvoke RemoteServiceInvoke, reqBody interface{}, resultPtr interface{}) error {
	log.Logger.Info("execute remote service", log.JsonObj("remote service", serviceInvoke))
	if strings.HasPrefix(serviceInvoke.Url, constant.NotApplicableRemoteCall) {
		log.Logger.Info(fmt.Sprintf("skipped call to [%s] on purpose", strings.TrimPrefix(serviceInvoke.Url, constant.NotApplicableRemoteCall)))
		return nil
	}
	if url, err := url.ParseRequestURI(serviceInvoke.Url); err != nil {
		errStr := fmt.Sprintf("invalid request url: [%+v]", err)
		log.Logger.Error(errStr)
		return errors.New(errStr)
	} else if url.Scheme == "" || url.Host == "" || url.Path == "" {
		errStr := fmt.Sprintf("invalid remote service url [%s]", url.String())
		log.Logger.Error(errStr)
		return errors.New(errStr)
	}
	if reqBody != nil {
		log.Logger.Debug("request body", log.JsonObj("reqBody", reqBody))
	}
	client := resty.New()
	client.SetTimeout(time.Duration(model.Config.Remote.Timeout) * time.Second)

	if serviceInvoke.Headers == nil {
		serviceInvoke.Headers = make(map[string]string)
	}

	if _, ok := serviceInvoke.Headers[ContentType]; !ok {
		serviceInvoke.Headers[ContentType] = DefContentType
	}

	var remoteResponse ResponseWrap
	request := client.R().SetResult(&remoteResponse)
	for key, value := range serviceInvoke.Headers {
		request.SetHeader(key, value)
	}

	if len(serviceInvoke.QueryParams) > 0 {
		items := make([]string, 0)
		for key, value := range serviceInvoke.QueryParams {
			item := fmt.Sprintf("%s=%v", key, value)
			items = append(items, item)
		}
		queryString := strings.Join(items, "&")
		request.SetQueryString(queryString)
	}

	if reqBody != nil {
		request.SetBody(reqBody)
	}

	var resp *resty.Response
	var err error
	if serviceInvoke.Method == http.MethodPost {
		resp, err = request.Post(serviceInvoke.Url)
	} else {
		resp, err = request.Get(serviceInvoke.Url)
	}

	remoteSysName := getRemoteSystemName(serviceInvoke.Url)
	if err != nil {
		log.Logger.Error(fmt.Sprintf("error on remote %s request", remoteSysName), log.Error(err))
		if isNetworkTimeout(err) {
			return ErrNetworkTimeOut
		}
		return err
	}
	log.Logger.Info(fmt.Sprintf("response from remote %s system. HttpStatus: %s", remoteSysName, resp.Status()))
	log.Logger.Debug(fmt.Sprintf("response Body: %+v", remoteResponse))
	if resp.IsError() {
		errStr := fmt.Sprintf("error http status from remote %s system: %s", remoteSysName, resp.Status())
		log.Logger.Error(errStr)
		return errors.New(errStr)
	}
	if remoteResponse.Status != constant.DefaultHttpSuccessStatus {
		errStr := fmt.Sprintf("error from remote %s system. status: %d, message: %s", remoteSysName, remoteResponse.Status, remoteResponse.Message)
		log.Logger.Error(errStr)
		return errors.New(errStr)
	}

	if remoteResponse.Data != nil && resultPtr != nil {
		if err := mapstructure.Decode(remoteResponse.Data, resultPtr); err != nil {
			log.Logger.Error("failed to decode result data", log.Error(err))
			return errors.New("failed to decode result data")
		} else {
			return nil
		}
	}
	return nil
}

func getRemoteSystemName(url string) string {
	address := strings.Split(url, "://")[1]
	paths := strings.Split(address, "/")
	return paths[1]
}

func isNetworkTimeout(err error) bool {
	if err == nil {
		return false
	}
	netErr, ok := err.(net.Error)
	if !ok {
		return false
	}

	if netErr.Timeout() {
		return true
	}
	return false
}
