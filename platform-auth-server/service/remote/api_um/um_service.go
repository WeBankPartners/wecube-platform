package api_um

import (
	"crypto/md5"
	"crypto/sha256"
	"encoding/json"
	"errors"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	resty "github.com/go-resty/resty/v2"
	"math/rand"
	"net"
	"strconv"
	"strings"
	"time"
)

type UmAuthResponse struct {
	Code     int    `json:"code"`
	Desc     string `json:"desc"`
	Id       string `json:"id"`
	Org      string `json:"org"`
	Dept     string `json:"dept"`
	AcType   int    `json:"actype"`
	Email    string `json:"email"`
	UserName string `json:"userName"`
	ExpDate  string `json:"expDate"`
}

type UmToken struct {
	Token   string `json:"tok"`
	Auth    string `json:"auth"`
	ExpTime int64  `json:"expTime"`
}

type UmPermissionResp struct {
	RetCode int    `json:"retCode"`
	Id      string `json:"id"`
	Tok     string `json:"tok"`
	Auth    string `json:"auth"`
	ExpTime int    `json:"expTime"`
}

type UmRoleItem struct {
	RoleCode   string `json:"roleCode"`
	RoleName   string `json:"roleName"`
	RoleNameCn string `json:"roleNameCn"`
	Org        string `json:"org"`
	SystemId   string `json:"systemId"`
	DelFlag    string `json:"delFlag"`
}

type UmUserRoleItem struct {
	UserId     string `json:"userId"`
	RoleCode   string `json:"roleCode"`
	Org        string `json:"org"`
	ExpireTime string `json:"expireTime,omitempty" `
	SystemId   string `json:"systemId"`
	DelFlag    string `json:"delFlag"`
}

var (
	umToken          UmToken
	umPermissionResp UmPermissionResp
)

func UmAppAuth(umAuthContext model.UmAuthContext) error {
	client := resty.New()
	timeStamp := strconv.Itoa(int(time.Now().Unix()))

	appToken := umAuthContext.Appname
	nonce := strconv.Itoa(rand.Int()%90000 + 10000)
	data := []byte(umAuthContext.Appid + nonce + timeStamp)
	tmp := fmt.Sprintf("%x", md5.Sum(data))
	sign := fmt.Sprintf("%x", md5.Sum([]byte(tmp+appToken)))

	reqDomain := fmt.Sprintf("http://%s:%d", umAuthContext.Host, umAuthContext.Port)
	resp, err := client.R().
		SetQueryParams(map[string]string{
			"appid":     umAuthContext.Appid,
			"style":     "2",
			"timeStamp": timeStamp,
			"nonce":     nonce,
			"sign":      sign,
		}).
		SetHeader("ContentType", "application/x-www-form-urlencoded;charset=UTF-8").
		Get(reqDomain + "/um_service")

	log.Logger.Debug("UM request:", log.String("url", resp.Request.URL))
	if err != nil {
		log.Logger.Error("failed to authenticate with um", log.Error(err))
		return err
	}
	if resp.IsError() {
		errStr := "error http status from um system: " + resp.Status()
		return errors.New(errStr)
	}
	log.Logger.Debug("Got UM response:" + string(resp.Body()))

	if err := json.Unmarshal(resp.Body(), &umToken); err != nil {
		log.Logger.Error("failed to unmarshal um reponse.", log.Error(err))
		return err
	}
	return nil
}

func generatePwd(loginId, loginPwd string) string {
	var result strings.Builder

	// Step 1: Calculate the hash of the loginPwd
	hash := sha256.New()
	hash.Write([]byte(loginPwd))
	//hashedPwd := hash.Sum(nil)

	// Step 2: Add the salt (loginId) to the hash
	salt := "{" + loginId + "}"
	hash.Write([]byte(salt))
	finalHash := hash.Sum(nil)

	// Step 3: Convert the final hash to a hexadecimal string
	for _, b := range finalHash {
		result.WriteString(fmt.Sprintf("%02x", b))
	}

	return result.String()
}

func convertUmAuthContext(authCtxMap map[string]string) model.UmAuthContext {
	protocol := "http"
	if val, ok := authCtxMap["protocol"]; ok {
		protocol = val
	}
	port, _ := strconv.Atoi(authCtxMap["port"])

	return model.UmAuthContext{
		Protocol: protocol,
		Host:     authCtxMap["host"],
		Port:     port,
		Appid:    authCtxMap["appid"],
		Appname:  authCtxMap["appname"],
	}
}

func UmAuthenticate(authCtxMap map[string]string, credential *model.CredentialDto) (bool, string, error) {
	log.Logger.Debug("current umToken", log.JsonObj("umToken", umToken))
	umAuthCtx := convertUmAuthContext(authCtxMap)
	curTimeStamp := time.Now().Unix()
	if umToken.ExpTime == 0 || umToken.ExpTime < curTimeStamp {
		log.Logger.Info("need app authentication with UM in first.")
		log.Logger.Debug("curTimeStamp:" + strconv.Itoa(int(curTimeStamp)))
		if err := UmAppAuth(umAuthCtx); err != nil {
			return false, "", errors.New("failed to authentication app with UM:" + err.Error())
		}
	}

	client := resty.New()
	timeStamp := strconv.Itoa(int(time.Now().Unix()))
	tmp := generatePwd(credential.Username, credential.Password)
	sign := fmt.Sprintf("%x", md5.Sum([]byte(credential.Username+tmp+timeStamp)))

	reqDomain := fmt.Sprintf("http://%s:%d", umAuthCtx.Host, umAuthCtx.Port)
	var umAuthResp UmAuthResponse
	resp, err := client.R().
		SetQueryParams(map[string]string{
			"id": credential.Username,
			//"userToken": credential.UserToken,
			"sign":      sign,
			"appid":     umAuthCtx.Appid,
			"timeStamp": timeStamp,
			"style":     "6",
			"auth":      umToken.Auth,
			"token":     umToken.Token,
		}).
		SetHeader("ContentType", "application/x-www-form-urlencoded;charset=UTF-8").
		Get(reqDomain + "/um_service")

	log.Logger.Debug("UM user auth request:", log.String("url", resp.Request.URL))
	if err != nil {
		log.Logger.Error("error on remote request", log.Error(err))
		return false, "", err
	}
	if resp.IsError() {
		errStr := "error http status from um system: " + resp.Status()
		log.Logger.Error(errStr)
		return false, "", errors.New(errStr)
	}

	log.Logger.Debug("Got Um token authentication response", log.String("response", string(resp.Body())))
	if err := json.Unmarshal(resp.Body(), &umAuthResp); err != nil {
		log.Logger.Error("failed to unmarshal um reponse.", log.Error(err))
		return false, "", err
	}

	log.Logger.Info("Got UM response:", log.JsonObj("UmAuthResponse", umAuthResp))
	umUserName := umAuthResp.UserName
	if umAuthResp.Code == 0 {
		return true, umUserName, nil
	} else {
		return false, umUserName, nil
	}
}

func getLocalIpAddress() string {
	// Get all network interfaces on the machine
	ifaces, err := net.Interfaces()
	if err != nil {
		log.Logger.Error("failed to get local ip address", log.Error(err))
		return ""
	}

	// Iterate over each network interface
	for _, iface := range ifaces {
		// Get the addresses for the current interface
		addrs, err := iface.Addrs()
		if err != nil {
			log.Logger.Error("failed to get local ip address", log.Error(err))
			continue
		}
		// Iterate over each address for the current interface
		for _, addr := range addrs {
			// Check if the address is an IP address and not a loopback address
			ipnet, ok := addr.(*net.IPNet)
			if ok && !ipnet.IP.IsLoopback() {
				if ipnet.IP.To4() != nil {
					// We've found a valid IPv4 address
					return ipnet.IP.String()
				}
			}
		}
	}
	return ""
}
