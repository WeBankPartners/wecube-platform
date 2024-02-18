package middleware

import (
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"strings"

	"github.com/WeBankPartners/wecube-platform/platform-auth-server/api/support"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/constant"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/utils"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt"
)

const (
	JwtTokenPrefix = "Bearer "
	AuthClaim      = "authClaim"
)

type GetAuthorities func(path string, method string) []string

func AuthApi(authoritiesFetcher GetAuthorities) gin.HandlerFunc {
	return func(c *gin.Context) {
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

			authorities := make([]string, 0)
			if len(authClaim.Authority) > 0 {
				authorities = utils.ParseArrayString(authClaim.Authority)
				c.Set("auth", authorities)
			}

			qualifiedAuthorities := authoritiesFetcher(c.Request.URL.Path, c.Request.Method)
			if len(qualifiedAuthorities) == 0 {
				c.Next()
				return
			}

			authorized := false
			for _, authority := range qualifiedAuthorities {
				if utils.Contains(authorities, authority) {
					authorized = true
					break
				}
			}
			if authorized {
				c.Next()
			} else {
				errStr := "user don't have authority to access"
				log.Logger.Warn(errStr)
				support.ReturnErrorWithHttpCode(c, fmt.Errorf(errStr), http.StatusForbidden)
				c.Abort()
			}
		}
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

func BuildRequestKey(path string, method string) string {
	return fmt.Sprintf("%s_%s", path, method)
}
