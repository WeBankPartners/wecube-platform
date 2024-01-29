package remote

import (
	"encoding/base64"
	"fmt"
	"math/rand"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

const (
	ClientTypeSubSystem = "SUB_SYSTEM"
)

// GetToken 获取鉴权token
func GetToken(systemCode, privateKey string) (tokens []models.JwtTokenDto, err error) {
	loginDto := models.CredentialDto{}
	loginDto.ClientType = ClientTypeSubSystem
	nonce := fmt.Sprintf("%d", rand.New(rand.NewSource(time.Now().UnixNano())).Intn(1000))
	loginDto.Nonce = nonce
	loginDto.Username = systemCode
	loginDto.Password = calculateLoginPassword(systemCode, privateKey, nonce)
	tokens, err = Login(loginDto)
	if err != nil {
		return
	}
	return
}

// calculateLoginPassword 计算登录密码
func calculateLoginPassword(systemCode, privateKey, nonce string) string {
	password := fmt.Sprintf("%s:%s", systemCode, nonce)
	privateKeyByteArr, _ := base64.StdEncoding.DecodeString(privateKey)
	return encryptByPrivateKey(privateKeyByteArr, []byte(password))
}

// @todo 后面补齐
func encryptByPrivateKey(data []byte, key []byte) string {
	/*	privateKeyInterface, err := x509.ParsePKCS8PrivateKey(data)
		if err != nil {
			log.Logger.Error("x509 parse private key error", log.Error(err))
			return ""
		}
		return base64.StdEncoding.EncodeToString(encryptedData) // 返回Base64编码后的结果*/
	return "123"
}
