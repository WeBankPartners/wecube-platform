package api

import (
	"fmt"

	"github.com/WeBankPartners/wecube-platform/platform-auth-server/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/api/support"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/service"
	"github.com/gin-gonic/gin"
)

func RegisterUmUser(c *gin.Context) {
	var param model.RoleApplyParam
	if err := c.ShouldBindJSON(&param); err != nil {
		support.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("invalid request: %s", err.Error())))
		return
	}

	curUser := middleware.GetRequestUser(c)
	err := service.UserManagementServiceInstance.RegisterUmUser(&param, curUser)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnSuccess(c)
	}
}
