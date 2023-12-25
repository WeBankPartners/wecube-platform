package api

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/api/support"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/utils"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/service"
	"github.com/gin-gonic/gin"
)

func RegisterLocalRole(c *gin.Context) {
	var roleDto model.SimpleLocalRoleDto
	if err := c.ShouldBindJSON(&roleDto); err != nil {
		support.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("invalid request: %s", err.Error())))
		return
	}

	operator := middleware.GetRequestUser(c)
	result, err := service.RoleManagementServiceInstance.RegisterLocalRole(&roleDto, operator)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}

func UpdateLocalRole(c *gin.Context) {
	var roleDto model.SimpleLocalRoleDto
	if err := c.ShouldBindJSON(&roleDto); err != nil {
		support.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("invalid request: %s", err.Error())))
		return
	}

	operator := middleware.GetRequestUser(c)
	result, err := service.RoleManagementServiceInstance.UpdateLocalRole(&roleDto, operator)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}

func RetrieveAllLocalRoles(c *gin.Context) {
	needAll, _ := c.GetQuery("all")
	if len(needAll) == 0 {
		needAll = "N"
	}

	requiredAll := false
	if utils.EqualsIgnoreCase("Y", needAll) {
		requiredAll = true
	}

	result, err := service.RoleManagementServiceInstance.RetrieveAllLocalRoles(requiredAll)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}

func RetrieveRoleInfo(c *gin.Context) {
	roleId := c.Param("role-id")
	result, err := service.RoleManagementServiceInstance.RetriveLocalRoleByRoleId(roleId)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}

func RetrieveRoleInfoByRoleName(c *gin.Context) {
	roleName := c.Param("role-name")
	result, err := service.RoleManagementServiceInstance.RetrieveLocalRoleByRoleName(roleName)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}

func UnregisterLocalRoleById(c *gin.Context) {
	roleId := c.Param("role-id")
	operator := middleware.GetRequestUser(c)
	err := service.RoleManagementServiceInstance.UnregisterLocalRoleById(roleId, operator)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnSuccess(c)
	}
}

func RetrieveAllAuthoritiesByRoleId(c *gin.Context) {
	roleId := c.Param("role-id")
	result, err := service.RoleManagementServiceInstance.RetrieveAllAuthoritiesByRoleId(roleId)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}

func ConfigureRoleWithAuthoritiesById(c *gin.Context) {
	roleId := c.Param("role-id")

	authorityDtos := make([]*model.SimpleAuthorityDto, 0)
	if err := c.ShouldBindJSON(&authorityDtos); err != nil {
		support.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("invalid request: %s", err.Error())))
		return
	}

	operator := middleware.GetRequestUser(c)
	err := service.RoleManagementServiceInstance.ConfigureRoleWithAuthoritiesById(roleId, authorityDtos, operator)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnSuccess(c)
	}
}

func ConfigureRoleWithAuthorities(c *gin.Context) {
	var roleAuthoritiesGrantDto model.RoleAuthoritiesDto
	if err := c.ShouldBindJSON(&roleAuthoritiesGrantDto); err != nil {
		support.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("invalid request: %s", err.Error())))
		return
	}

	operator := middleware.GetRequestUser(c)
	err := service.RoleManagementServiceInstance.ConfigureRoleWithAuthorities(&roleAuthoritiesGrantDto, operator)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnSuccess(c)
	}
}

func RevokeRoleWithAuthorities(c *gin.Context) {
	var roleAuthoritiesRevocationDto model.RoleAuthoritiesDto
	if err := c.ShouldBindJSON(&roleAuthoritiesRevocationDto); err != nil {
		support.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("invalid request: %s", err.Error())))
		return
	}

	operator := middleware.GetRequestUser(c)
	err := service.RoleManagementServiceInstance.RevokeRoleAuthorities(&roleAuthoritiesRevocationDto, operator)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnSuccess(c)
	}
}

func RevokeRoleAuthoritiesById(c *gin.Context) {
	roleId := c.Param("role-id")
	authorityDtos := make([]*model.SimpleAuthorityDto, 0)
	if err := c.ShouldBindJSON(&authorityDtos); err != nil {
		support.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("invalid request: %s", err.Error())))
		return
	}

	operator := middleware.GetRequestUser(c)
	err := service.RoleManagementServiceInstance.RevokeRoleAuthoritiesById(roleId, authorityDtos, operator)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnSuccess(c)
	}
}
