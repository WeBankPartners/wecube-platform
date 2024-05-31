package system

import (
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/gin-gonic/gin"
)

func QuerySystemVariables(c *gin.Context) {
	var param models.QueryRequestParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if !validateSystemVariablePermission(middleware.GetRequestRoles(c)) {
		middleware.ReturnError(c, exterror.New().DataPermissionDeny)
		return
	}
	result, err := database.QuerySystemVariables(c, &param)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func validateSystemVariablePermission(userRoles []string) (legalFlag bool) {
	legalFlag = false
	for _, v := range userRoles {
		if v == "ADMIN_SYSTEM_PARAMS" {
			legalFlag = true
			break
		}
	}
	return
}

func GetSystemVariableScope(c *gin.Context) {
	result, err := database.GetSystemVariableScope()
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func CreateSystemVariable(c *gin.Context) {
	var params []*models.SystemVariables
	if err := c.ShouldBindJSON(&params); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if !validateSystemVariablePermission(middleware.GetRequestRoles(c)) {
		middleware.ReturnError(c, exterror.New().DataPermissionDeny)
		return
	}
	err := database.CreateSystemVariables(c, params)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, params)
	}
}

func UpdateSystemVariable(c *gin.Context) {
	var params []*models.SystemVariables
	if err := c.ShouldBindJSON(&params); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if !validateSystemVariablePermission(middleware.GetRequestRoles(c)) {
		middleware.ReturnError(c, exterror.New().DataPermissionDeny)
		return
	}
	err := database.UpdateSystemVariables(c, params)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, params)
	}
}

func DeleteSystemVariable(c *gin.Context) {
	var params []*models.SystemVariables
	if err := c.ShouldBindJSON(&params); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if !validateSystemVariablePermission(middleware.GetRequestRoles(c)) {
		middleware.ReturnError(c, exterror.New().DataPermissionDeny)
		return
	}
	err := database.DeleteSystemVariables(c, params)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnSuccess(c)
	}
}
