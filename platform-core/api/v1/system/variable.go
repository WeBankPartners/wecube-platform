package system

import (
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
)

func QuerySystemVariables(c *gin.Context) {
	var param models.QueryRequestParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}

}

func GetSystemVariableScope(c *gin.Context) {

}

func CreateSystemVariable(c *gin.Context) {

}

func UpdateSystemVariable(c *gin.Context) {

}

func DeleteSystemVariable(c *gin.Context) {

}
