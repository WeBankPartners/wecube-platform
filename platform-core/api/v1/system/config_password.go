package system

import (
	"fmt"

	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/encrypt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
)

func EncryptConfigPassword(c *gin.Context) {
	var param models.ConfigPasswordEncryptRequest
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	result, err := buildAESCConfigPassword(&param)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, result)
}

func buildAESCConfigPassword(param *models.ConfigPasswordEncryptRequest) (result *models.ConfigPasswordEncryptResponse, err error) {
	if param == nil || param.Password == "" {
		return nil, fmt.Errorf("password can not be empty")
	}
	if param.Password != param.ConfirmPassword {
		return nil, fmt.Errorf("passwords do not match")
	}
	if models.Config == nil || models.Config.Auth == nil || models.Config.Auth.ConfigPasswordKey == "" {
		return nil, fmt.Errorf("config password key is not configured")
	}
	cipherText, err := encrypt.EncryptWithAESC(param.Password, models.Config.Auth.ConfigPasswordKey)
	if err != nil {
		return nil, err
	}
	return &models.ConfigPasswordEncryptResponse{Ciphertext: cipherText}, nil
}
