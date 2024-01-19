package middleware

import (
	"encoding/json"
	"errors"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/api/support"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/constant"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt"
	"net/http"
	"strings"
)

const (
	JwtTokenPrefix = "Bearer "
	AuthClaim      = "authClaim"
)

func AuthApi(c *gin.Context) {
	if !strings.HasPrefix(c.Request.URL.Path, constant.UrlPrefix) {
		c.String(http.StatusNotFound, "404 page not found")
		c.Abort()
		return
	}

	if c.Request.RequestURI == constant.UrlPrefix+constant.UriLogin {
		c.Next()
	} else {
		//apiUri := c.Request.URL.Path[len(constant.UrlPrefix):]
		authClaim, err := getTokenData(c.GetHeader(constant.AuthorizationHeader))
		if err != nil {
			log.Logger.Warn("failed to validate jwt token", log.Error(err))
			//support.ReturnErrorWithHttpCode(c, exterror.Catch(exterror.New().AuthManagerTokenError, err), http.StatusUnauthorized)
			support.ReturnErrorWithHttpCode(c, errors.New("failed to validate jwt token"), http.StatusUnauthorized)

			c.Abort()
			return
		}
		//c.Set("needAuth", true)
		c.Set(constant.Operator, fmt.Sprintf("%s", authClaim.Subject))
		c.Set(AuthClaim, authClaim)
		c.Next()
	}
}

func getTokenData(tokenString string) (authClaim *model.AuthClaims, err error) {
	if strings.HasPrefix(tokenString, JwtTokenPrefix) {
		tokenString = tokenString[7:]
	}
	// parse rsa public key
	/*	parsedKey, parsePublicKeyErr := jwt.ParseRSAPublicKeyFromPEM(jwtPublicKeyBytes)
		if parsePublicKeyErr != nil {
			err = fmt.Errorf("parse jwt public key fail,%s ", parsePublicKeyErr.Error())
			return
		}
	*/ // parse Claim
	jwtToken, parseClaimErr := jwt.ParseWithClaims(tokenString, &model.AuthClaims{}, func(token *jwt.Token) (interface{}, error) {
		return model.Config.Auth.SigningKeyBytes, nil
	})
	if parseClaimErr != nil {
		err = fmt.Errorf("parse jwt claim fail,%s ", parseClaimErr.Error())
		return
	}
	claim, ok := jwtToken.Claims.(*model.AuthClaims)
	if !ok || !jwtToken.Valid {
		err = fmt.Errorf("jwt token invalid ")
		return
	}
	authClaim = claim
	return
}

func GetRequestUser(c *gin.Context) string {
	return c.GetString(constant.Operator)
}

func GetAuthenticatedUser(c *gin.Context) *model.AuthenticatedUser {
	if authClaim, existed := c.Get(AuthClaim); existed {
		claim := authClaim.(*model.AuthClaims)
		authorities := make([]string, 0)
		json.Unmarshal([]byte(claim.Authority), &authorities)

		return &model.AuthenticatedUser{
			Username:           claim.Subject,
			GrantedAuthorities: authorities,
		}
	}
	return nil
}
