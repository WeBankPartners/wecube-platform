package api

import (
	"errors"
	"github.com/WeBankPartners/go-common-lib/cipher"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/api/support"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/constant"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/service"
	"github.com/gin-gonic/gin"
	"strings"
)

func Login(c *gin.Context) {
	var credential model.CredentialDto
	if c.ShouldBindJSON(&credential) == nil {
		if authResp, err := service.AuthServiceInstance.Login(&credential, false); err == nil {
			setupTokenHeaders(authResp.Tokens, c)
			support.ReturnData(c, authResp.Tokens)
		} else {
			support.ReturnError(c, err)
		}
	} else {
		support.ReturnError(c, errors.New("invalid request"))
	}
}

func TaskLogin(c *gin.Context) {
	var credential model.CredentialDto
	if c.ShouldBindJSON(&credential) == nil {
		if authResp, err := service.AuthServiceInstance.Login(&credential, true); err == nil {
			setupTokenHeaders(authResp.Tokens, c)
			support.ReturnData(c, authResp)
		} else {
			support.ReturnError(c, err)
		}
	} else {
		support.ReturnError(c, errors.New("invalid request"))
	}
}

func setupTokenHeaders(jwtTokens []*model.Jwt, c *gin.Context) {
	for _, jwtToken := range jwtTokens {
		if jwtToken.TokenType == constant.TypeAccessToken {
			c.Header(constant.AuthorizationHeader, constant.BearerTokenPrefix+jwtToken.Token)
		} else if jwtToken.TokenType == constant.TypeRefreshToken {
			c.Header(constant.RefreshTokenHeader, constant.BearerTokenPrefix+jwtToken.Token)
		}
	}

}

func RefreshToken(c *gin.Context) {
	bearerToken := c.Request.Header.Get(constant.AuthorizationHeader)
	if len(bearerToken) == 0 || !strings.HasPrefix(bearerToken, constant.BearerTokenPrefix) {
		support.ReturnError(c, errors.New("invalid request"))
	} else {
		refreshToken := strings.TrimPrefix(bearerToken, constant.BearerTokenPrefix)
		if jwtTokens, err := service.AuthServiceInstance.RefreshToken(refreshToken); err == nil {
			setupTokenHeaders(jwtTokens, c)
			support.ReturnData(c, jwtTokens)
		} else {
			support.ReturnError(c, err)
		}
	}

}

func GetLoginSeed(c *gin.Context) {
	seed := service.GetLoginSeed()
	md5sum := cipher.Md5Encode(seed)
	support.ReturnData(c, md5sum[0:16])
}
