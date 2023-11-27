package auth

import (
	"bytes"
	"crypto/sha256"
	"encoding/base64"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/cipher"
	"github.com/gin-gonic/gin"
	"io/ioutil"
	"net/http"
)

const (
	SourceAuthHeaderName = "Source-Auth"
	AppIdHeaderName      = "App-Id"
)

type HttpSourceAuth struct {
	SourceAuth           string            `json:"source_auth"`
	AppId                string            `json:"app_id"`
	RequestURI           string            `json:"request_uri"`
	RequestBodyString    string            `json:"request_body_string"`
	LegalSourcePubKeyMap map[string]string `json:"legal_source_pub_key_map"`
}

func (h *HttpSourceAuth) validateParam() error {
	if h.SourceAuth == "" || h.AppId == "" {
		return fmt.Errorf("Http header Source-Auth and App-Id can not empty ")
	}
	if len(h.LegalSourcePubKeyMap) == 0 {
		return fmt.Errorf("Source public key list is empty ")
	}
	return nil
}

func (h *HttpSourceAuth) auth() error {
	var pubKeyString string
	if v, b := h.LegalSourcePubKeyMap[h.AppId]; b {
		pubKeyString = v
	} else {
		return fmt.Errorf("Can not find appId:%s public key ", h.AppId)
	}
	decodeBytes, err := cipher.RSADecryptByPublic([]byte(h.SourceAuth), []byte(pubKeyString))
	if err != nil {
		return err
	}
	if string(decodeBytes) != hashSourceAuthHeader(h.AppId, h.RequestURI, h.RequestBodyString) {
		return fmt.Errorf("Validate sign content fail ")
	}
	return nil
}

func (h *HttpSourceAuth) GetAppId() string {
	return h.AppId
}

func VerifySourceWithGin(c *gin.Context, pubKeyMap map[string]string) (appId string, err error) {
	httpSourceAuth := HttpSourceAuth{SourceAuth: c.GetHeader(SourceAuthHeaderName), AppId: c.GetHeader(AppIdHeaderName), RequestURI: c.Request.RequestURI}
	var bodyBytes []byte
	if c.Request.Body != nil {
		bodyBytes, _ = ioutil.ReadAll(c.Request.Body)
		c.Request.Body.Close()
		c.Request.Body = ioutil.NopCloser(bytes.NewReader(bodyBytes))
	}
	httpSourceAuth.RequestBodyString = string(bodyBytes)
	httpSourceAuth.LegalSourcePubKeyMap = pubKeyMap
	err = httpSourceAuth.validateParam()
	if err != nil {
		return
	}
	appId = httpSourceAuth.AppId
	err = httpSourceAuth.auth()
	return
}

func VerifySourceWithGinPublicKey(c *gin.Context, publicKeyBytes []byte) (err error) {
	httpSourceAuth := HttpSourceAuth{SourceAuth: c.GetHeader(SourceAuthHeaderName), AppId: c.GetHeader(AppIdHeaderName), RequestURI: c.Request.RequestURI}
	var bodyBytes []byte
	if c.Request.Body != nil {
		bodyBytes, _ = ioutil.ReadAll(c.Request.Body)
		c.Request.Body.Close()
		c.Request.Body = ioutil.NopCloser(bytes.NewReader(bodyBytes))
	}
	httpSourceAuth.RequestBodyString = string(bodyBytes)
	httpSourceAuth.LegalSourcePubKeyMap = map[string]string{httpSourceAuth.AppId: string(publicKeyBytes)}
	err = httpSourceAuth.validateParam()
	if err != nil {
		return
	}
	err = httpSourceAuth.auth()
	return
}

func VerifySourceWithHttp(httpRequest *http.Request, pubKeyMap map[string]string) (appId string, err error) {
	httpSourceAuth := HttpSourceAuth{SourceAuth: httpRequest.Header.Get(SourceAuthHeaderName), AppId: httpRequest.Header.Get(AppIdHeaderName), RequestURI: httpRequest.URL.RequestURI()}
	var bodyBytes []byte
	if httpRequest.Body != nil {
		bodyBytes, _ = ioutil.ReadAll(httpRequest.Body)
		httpRequest.Body.Close()
		httpRequest.Body = ioutil.NopCloser(bytes.NewReader(bodyBytes))
	}
	httpSourceAuth.RequestBodyString = string(bodyBytes)
	httpSourceAuth.LegalSourcePubKeyMap = pubKeyMap
	err = httpSourceAuth.validateParam()
	if err != nil {
		return
	}
	appId = httpSourceAuth.AppId
	err = httpSourceAuth.auth()
	return
}

func SetRequestSourceAuth(httpRequest *http.Request, appId string, privateKey []byte) error {
	httpRequest.Header.Set(AppIdHeaderName, appId)
	var bodyBytes []byte
	if httpRequest.Body != nil {
		bodyBytes, _ = ioutil.ReadAll(httpRequest.Body)
		httpRequest.Body.Close()
		httpRequest.Body = ioutil.NopCloser(bytes.NewReader(bodyBytes))
	}
	enBytes, enErr := cipher.RSAEncryptByPrivate([]byte(hashSourceAuthHeader(appId, httpRequest.URL.RequestURI(), string(bodyBytes))), privateKey)
	if enErr != nil {
		return fmt.Errorf("Rsa encrypt error:%s \n", enErr.Error())
	}
	httpRequest.Header.Set(SourceAuthHeaderName, base64.StdEncoding.EncodeToString(enBytes))
	return nil
}

func hashSourceAuthHeader(appId, httpURI, requestBodyString string) string {
	return fmt.Sprintf("%x", sha256.Sum256([]byte(fmt.Sprintf("%s%s%s", appId, httpURI, requestBodyString))))
}
