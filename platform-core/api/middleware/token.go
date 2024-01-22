package middleware

import (
	"fmt"
	"github.com/WeBankPartners/go-common-lib/token"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
	"io"
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
		ReturnError(c, exterror.Catch(exterror.New().RequestTokenValidateError, err))
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
	authToken, err := token.DecodeJwtToken(authHeader, models.Config.Auth.JwtSigningKey)
	if err != nil {
		return err
	}
	if authToken.User == "" {
		return fmt.Errorf("Token content is illegal,main message is empty ")
	}
	c.Set(models.ContextUserId, authToken.User)
	c.Set(models.ContextRoles, authToken.Roles)
	return nil
}

func ReadFormFile(c *gin.Context, fileKey string) (fileName string, fileBytes []byte, err error) {
	file, getFileErr := c.FormFile(fileKey)
	if getFileErr != nil {
		err = getFileErr
		return
	}
	fileName = file.Filename
	fileHandler, openFileErr := file.Open()
	if openFileErr != nil {
		err = openFileErr
		return
	}
	fileBytes, err = io.ReadAll(fileHandler)
	fileHandler.Close()
	return
}
