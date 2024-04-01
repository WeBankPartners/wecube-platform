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

func RegisterSubSystem(c *gin.Context) {
	var subSystemDto model.SimpleSubSystemDto
	if err := c.ShouldBindJSON(&subSystemDto); err != nil {
		support.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("invalid request: %s", err.Error())))
		return
	}

	authUser := middleware.GetAuthenticatedUser(c)
	result, err := service.SubSystemManagementServiceInstance.RegisterSubSystem(&subSystemDto, authUser)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}

//TODO
//@PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
func RegisterSubSystemAccessToken(c *gin.Context) {
	var subSystemTokenDto model.SubSystemTokenDto
	if err := c.ShouldBindJSON(&subSystemTokenDto); err != nil {
		support.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("invalid request: %s", err.Error())))
		return
	}

	result, err := service.SubSystemManagementServiceInstance.RegisterSubSystemAccessToken(&subSystemTokenDto)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}

func RetrieveAllSubSystems(c *gin.Context) {
	authUser := middleware.GetAuthenticatedUser(c)
	result, err := service.SubSystemManagementServiceInstance.RetrieveAllSubSystems(authUser)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}

func RetrieveAllSubSystemByName(c *gin.Context) {
	name := c.Param("name")
	authUser := middleware.GetAuthenticatedUser(c)
	result, err := service.SubSystemManagementServiceInstance.RetrieveSubSystemByName(name, authUser)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}

//TODO
//@PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
func RetrieveAllSubSystemsBySystemCode(c *gin.Context) {
	systemCode := c.Param("system-code")
	//authUser := middleware.GetAuthenticatedUser(c)
	result, err := service.SubSystemManagementServiceInstance.RetrieveSubSystemApikey(systemCode)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}
