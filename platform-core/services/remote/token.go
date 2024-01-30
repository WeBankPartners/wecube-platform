package remote

import (
	"github.com/WeBankPartners/go-common-lib/token"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

const (
	DefaultSystemCode = "SYS_PLATFORM"
)

var coreToken token.CoreToken

// InitToken 初始化token
func InitToken() {
	coreToken.InitCoreToken()
}

// GetToken 获取鉴权token
func GetToken() string {
	coreToken.BaseUrl = models.Config.Auth.Url
	coreToken.SubSystemCode = DefaultSystemCode
	coreToken.SubSystemKey = models.Config.Auth.SubSystemPrivateKey
	return coreToken.GetCoreToken()
}
