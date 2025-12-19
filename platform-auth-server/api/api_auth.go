package api

import (
	"encoding/json"
	"errors"
	"github.com/WeBankPartners/go-common-lib/cipher"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/api/support"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/constant"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/service"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
	"strings"
)

func Login(c *gin.Context) {
	var credential model.CredentialDto
	if c.ShouldBindJSON(&credential) == nil {
		if authResp, err := service.AuthServiceInstance.Login(&credential, false); err == nil {
			if len(authResp.Tokens) > 0 {
				setupTokenHeaders(authResp.Tokens, c)
				support.ReturnData(c, authResp.Tokens)
				return
			}
			if len(authResp.TempToken) > 0 && len(authResp.QrCodeUrl) > 0 {
				support.ReturnData(c, gin.H{
					"qrCodeUrl":   authResp.QrCodeUrl,
					"qrCodeImage": authResp.QrCodeImage,
					"tempToken":   authResp.TempToken,
				})
				return
			}
			if len(authResp.TempToken) > 0 && authResp.NeedMfaCode {
				support.ReturnData(c, gin.H{
					"needMfaCode": true,
					"tempToken":   authResp.TempToken,
				})
				return
			}
			support.ReturnData(c, authResp.Tokens)
		} else {
			support.ReturnError(c, err)
		}
	} else {
		support.ReturnError(c, errors.New("invalid request"))
	}
}

func VerifyMfaCode(c *gin.Context) {
	var request model.MfaVerifyRequest
	if c.ShouldBindJSON(&request) == nil {
		if jwts, err := service.AuthServiceInstance.VerifyMfaCode(&request); err == nil {
			setupTokenHeaders(jwts, c)

			// 记录最终返回结果（校验成功）
			responseData := model.ResponseWrap{
				Status:  model.ResponseStatusOk,
				Message: model.ResponseMessageOk,
				Data:    jwts,
			}
			responseBytes, _ := json.Marshal(responseData)
			log.Info(nil, log.LOGGER_APP, "[MFA_VERIFY] API handler: MFA verification succeeded, returning tokens",
				zap.String("username", request.Username),
				zap.Int("tokenCount", len(jwts)),
				zap.String("responseBody", string(responseBytes)))

			support.ReturnData(c, jwts)
		} else {
			// 记录校验失败的结果
			log.Info(nil, log.LOGGER_APP, "[MFA_VERIFY] API handler: MFA verification failed",
				zap.String("username", request.Username),
				zap.String("code", request.Code),
				zap.Error(err))
			support.ReturnError(c, err)
		}
	} else {
		log.Info(nil, log.LOGGER_APP, "[MFA_VERIFY] API handler: Invalid request body",
			zap.String("username", request.Username))
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
