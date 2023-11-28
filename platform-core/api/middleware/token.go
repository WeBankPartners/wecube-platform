package middleware

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
)

func GetRequestUser(c *gin.Context) string {
	return c.GetString(models.ContextUserId)
}

func GetRequestRoles(c *gin.Context) []string {
	return c.GetStringSlice(models.ContextRoles)
}

func AuthToken(c *gin.Context) {
	err := authRequest(c)
	if err != nil {
		//ReturnTokenValidateError(c, err)
		c.Abort()
	} else {
		c.Next()
	}
}

func authRequest(c *gin.Context) error {
	if !models.Config.Auth.Enable {
		return nil
	}
	authHeader := c.GetHeader(models.AuthorizationHeader)
	if authHeader == "" {
		return fmt.Errorf("Can not find Request Header Authorization ")
	}
	//authToken, err := token.DecodeJwtToken(authHeader, models.Config.Auth.JwtSigningKey)
	//if err != nil {
	//	return err
	//}
	//if authToken.User == "" {
	//	return fmt.Errorf("Token content is illegal,main message is empty ")
	//}
	//c.Set(models.ContextUserId, authToken.User)
	//c.Set(models.ContextRoles, authToken.Roles)
	return nil
}
