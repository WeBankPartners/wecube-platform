package process

import (
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/gin-gonic/gin"
)

// AddProcessDefinition 添加编排
func AddProcessDefinition(c *gin.Context) {
	var param models.ProcessDefinitionParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	err := database.AddProcessDefinition(c, middleware.GetRequestUser(c), param)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnSuccess(c)
}

// GetProcessDefinition 获取编排
func GetProcessDefinition(c *gin.Context) {
	var procDefDto models.ProcessDefinitionDto
	procDefId := c.Param("proc-def-id")
	procDef, err := database.GetProcessDefinition(c, procDefId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	procDefDto.ProcDef = procDef
	list, err := database.GetProcDefPermissionByCondition(c, models.ProcDefPermission{ProcDefId: procDefId})
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if len(list) > 0 {
		for _, procDefPermission := range list {
			if procDefPermission.Permission == string(models.MGMT) {
				procDefDto.PermissionToRole.MGMT = append(procDefDto.PermissionToRole.MGMT, procDefPermission.RoleName)
			} else if procDefPermission.Permission == string(models.USE) {
				procDefDto.PermissionToRole.USE = append(procDefDto.PermissionToRole.USE, procDefPermission.RoleName)
			}
		}
	}
	middleware.ReturnData(c, procDefDto)
}

// AddProcessDefinitionTaskNodes 添加编排节点
func AddProcessDefinitionTaskNodes(c *gin.Context) {
	var param models.ProcessDefinitionTaskNodeParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
}
