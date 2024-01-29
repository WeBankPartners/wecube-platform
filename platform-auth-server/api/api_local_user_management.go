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

func RegisterLocalUser(c *gin.Context) {
	var userDto model.SimpleLocalUserDto
	if err := c.ShouldBindJSON(&userDto); err != nil {
		support.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("invalid request: %s", err.Error())))
		return
	}

	curUser := middleware.GetRequestUser(c)
	result, err := service.UserManagementServiceInstance.RegisterLocalUser(&userDto, curUser)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}

func ModifyLocalUserPassword(c *gin.Context) {
	var userPassDto model.SimpleLocalUserPassDto
	if err := c.ShouldBindJSON(&userPassDto); err != nil {
		support.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("invalid request: %s", err.Error())))
		return
	}

	//curUser := middleware.GetRequestUser(c)
	result, err := service.UserManagementServiceInstance.ModifyLocalUserPassword(&userPassDto)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}

// TODO
// @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
func ResetLocalUserPassword(c *gin.Context) {
	var userPassDto model.SimpleLocalUserPassDto
	if err := c.ShouldBindJSON(&userPassDto); err != nil {
		support.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("invalid request: %s", err.Error())))
		return
	}

	//curUser := middleware.GetRequestUser(c)
	result, err := service.UserManagementServiceInstance.ResetLocalUserPassword(&userPassDto)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}

func ModifyLocalUserInfomation(c *gin.Context) {
	username := c.Param("username")
	var userDto model.SimpleLocalUserDto
	if err := c.ShouldBindJSON(&userDto); err != nil {
		support.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("invalid request: %s", err.Error())))
		return
	}

	curUser := middleware.GetRequestUser(c)
	result, err := service.UserManagementServiceInstance.ModifyLocalUserInfomation(username, &userDto, curUser)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}

func RetrieveAllUsers(c *gin.Context) {
	result, err := service.UserManagementServiceInstance.RetrieveAllActiveUsers()
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}

func RetrieveUserByUserId(c *gin.Context) {
	userId := c.Param("user-id")
	result, err := service.UserManagementServiceInstance.RetireveLocalUserByUserid(userId)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}

func UnregisterLocalUser(c *gin.Context) {
	userId := c.Param("user-id")
	curUser := middleware.GetRequestUser(c)
	err := service.UserManagementServiceInstance.UnregisterLocalUser(userId, curUser)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnSuccess(c)
	}
}

func GetUsersByRoleId(c *gin.Context) {
	roleId := c.Param("role-id")
	result, err := service.UserManagementServiceInstance.GetLocalUsersByRoleId(roleId)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}

func GetRolesByUsername(c *gin.Context) {
	username := c.Param("username")
	result, err := service.UserManagementServiceInstance.GetLocalRolesByUsername(username)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnData(c, result)
	}
}

func ConfigureRoleForUsers(c *gin.Context) {
	roleId := c.Param("role-id")
	var userDtos []*model.SimpleLocalUserDto
	if err := c.ShouldBindJSON(&userDtos); err != nil {
		support.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("invalid request: %s", err.Error())))
		return
	}

	curUser := middleware.GetRequestUser(c)
	err := service.UserManagementServiceInstance.ConfigureRoleForUsers(roleId, userDtos, curUser)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnSuccess(c)
	}
}

func ConfigureUserWithRoles(c *gin.Context) {
	userId := c.Param("user-id")
	var roleDtos []*model.SimpleLocalRoleDto
	if err := c.ShouldBindJSON(&roleDtos); err != nil {
		support.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("invalid request: %s", err.Error())))
		return
	}

	curUser := middleware.GetRequestUser(c)
	err := service.UserManagementServiceInstance.ConfigureUserWithRoles(userId, roleDtos, curUser)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnSuccess(c)
	}
}

func RevokeRoleFromUsers(c *gin.Context) {
	userId := c.Param("role-id")
	var userDtos []*model.SimpleLocalUserDto
	if err := c.ShouldBindJSON(&userDtos); err != nil {
		support.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("invalid request: %s", err.Error())))
		return
	}

	curUser := middleware.GetRequestUser(c)
	err := service.UserManagementServiceInstance.RevokeRoleFromUsers(userId, userDtos, curUser)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnSuccess(c)
	}
}

func RevokeRolesFromUser(c *gin.Context) {
	userId := c.Param("user-id")
	var roleDtos []*model.SimpleLocalRoleDto
	if err := c.ShouldBindJSON(&roleDtos); err != nil {
		support.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("invalid request: %s", err.Error())))
		return
	}

	curUser := middleware.GetRequestUser(c)
	err := service.UserManagementServiceInstance.RevokeRolesFromUser(userId, roleDtos, curUser)
	if err != nil {
		support.ReturnError(c, err)
	} else {
		support.ReturnSuccess(c)
	}
}
