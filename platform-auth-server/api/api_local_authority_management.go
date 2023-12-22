package api

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/api/support"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/service"
	"github.com/gin-gonic/gin"
)

func RegisterLocalAuthority(c *gin.Context) {
	//log.Logger.Info("Start RegisterLocalAuthority, ", log.String("RequestId", c.GetHeader(constant.RequestId)))
	var authorityDto model.SimpleAuthorityDto
	if err := c.ShouldBindJSON(&authorityDto); err != nil {
		support.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("invalid request: %s", err.Error())))
		return
	}
	//TODO：
	curUser := ""
	result, err := service.AuthorityManagementServiceInstance.RegisterLocalAuthority(authorityDto, curUser)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}

func RetrieveAllLocalAuthorities(c *gin.Context) {
	//log.Logger.Info("Start RegisterLocalAuthority, ", log.String("RequestId", c.GetHeader(constant.RequestId)))
	result, err := service.AuthorityManagementServiceInstance.RetrieveAllLocalAuthorities()
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}
