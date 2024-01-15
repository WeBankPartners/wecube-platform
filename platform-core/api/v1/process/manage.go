package process

import (
	"encoding/json"
	"fmt"
	"strings"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/gin-gonic/gin"
)

// AddOrUpdateProcessDefinition 添加或者更新编排
func AddOrUpdateProcessDefinition(c *gin.Context) {
	var param models.ProcessDefinitionParam
	var entity *models.ProcDef
	var err error
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if param.Name == "" || param.Version == "" {
		err = fmt.Errorf("name & version not empty")
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if param.Id == "" {
		entity, err = database.AddProcessDefinition(c, middleware.GetRequestUser(c), param)
	} else {
		entity = &models.ProcDef{
			Id:            param.Id,
			Name:          param.Name,
			Version:       param.Version,
			RootEntity:    param.RootEntity,
			Tags:          param.Tags,
			ForPlugin:     strings.Join(param.AuthPlugins, ","),
			Scene:         param.Scene,
			ConflictCheck: param.ConflictCheck,
			UpdatedBy:     middleware.GetRequestUser(c),
			UpdatedTime:   time.Now(),
		}
		err = database.UpdateProcDef(c, entity)
	}
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, models.ConvertProcDef2Dto(entity))
}

// GetProcessDefinition 获取编排
func GetProcessDefinition(c *gin.Context) {
	var procDefDto models.ProcessDefinitionDto
	procDefId := c.Param("proc-def-id")
	if procDefId == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("proc-def-id is empty")))
		return
	}
	procDef, err := database.GetProcessDefinition(c, procDefId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	procDefDto.ProcDef = models.ConvertProcDef2Dto(procDef)
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
	procDefDto.ProcDefNodeList, err = database.GetProcDefNodeByProcDefId(c, procDefId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, procDefDto)
}

// AddOrUpdateProcessDefinitionTaskNodes 添加更新编排节点
func AddOrUpdateProcessDefinitionTaskNodes(c *gin.Context) {
	var param models.ProcDefNodeDto
	var procDefNode *models.ProcDefNode
	var err error

	user := middleware.GetRequestUser(c)
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if param.ProcDefNodeCustomAttrs == nil || param.ProcDefNodeCustomAttrs.Id == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("param id is empty")))
		return
	}
	procDefNode, err = database.GetProcDefNode(c, param.ProcDefNodeCustomAttrs.Id)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	node := convertParam2ProcDefNode(user, param)
	if procDefNode == nil {
		err = database.InsertProcDefNode(c, node)
	} else {
		node.Status = procDefNode.Status
		node.CreatedBy = procDefNode.CreatedBy
		node.CreatedTime = procDefNode.CreatedTime
		err = database.UpdateProcDefNode(c, procDefNode)
	}
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 处理节点参数,先删除然后插入
	if len(param.ProcDefNodeCustomAttrs.ParamInfos) > 0 {
		for _, info := range param.ProcDefNodeCustomAttrs.ParamInfos {
			err = database.DeleteProcDefNodeParam(c, info.Id)
			if err != nil {
				middleware.ReturnError(c, err)
				return
			}
			err = database.InsertProcDefNodeParam(c, info)
			if err != nil {
				middleware.ReturnError(c, err)
				return
			}
		}
	}
	middleware.ReturnSuccess(c)
}

func AddOrUpdateProcDefNodeLink(c *gin.Context) {
	var param models.ProcDefNodeLinkDto
	var procDefNodeLink *models.ProcDefNodeLink
	var err error

	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if param.ProcDefNodeLinkCustomAttrs == nil || param.ProcDefNodeLinkCustomAttrs.Id == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("param id is empty")))
		return
	}
	procDefNodeLink, err = database.GetProcDefNodeLink(c, param.ProcDefNodeLinkCustomAttrs.Id)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	newProcDefNodeLink := models.ConvertParam2ProcDefNodeLink(param)
	if procDefNodeLink == nil {
		err = database.InsertProcDefNodeLink(c, newProcDefNodeLink)
	} else {
		err = database.UpdateProcDefNodeLink(c, newProcDefNodeLink)
	}
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnSuccess(c)
}

func convertParam2ProcDefNode(user string, param models.ProcDefNodeDto) *models.ProcDefNode {
	now := time.Now()
	byteArr, _ := json.Marshal(param.NodeAttrs)
	procDefNodeAttr := param.ProcDefNodeCustomAttrs
	node := &models.ProcDefNode{
		Id:                procDefNodeAttr.Id,
		ProcDefId:         procDefNodeAttr.ProcDefId,
		Name:              procDefNodeAttr.Name,
		Description:       procDefNodeAttr.Description,
		Status:            string(models.Draft),
		NodeType:          procDefNodeAttr.NodeType,
		ServiceName:       procDefNodeAttr.ServiceName,
		DynamicBind:       procDefNodeAttr.DynamicBind,
		BindNodeId:        procDefNodeAttr.BindNodeId,
		RiskCheck:         procDefNodeAttr.RiskCheck,
		RoutineExpression: procDefNodeAttr.RoutineExpression,
		ContextParamNodes: procDefNodeAttr.ContextParamNodes,
		Timeout:           procDefNodeAttr.Timeout,
		OrderedNo:         procDefNodeAttr.OrderedNo,
		UiStyle:           string(byteArr),
		CreatedBy:         user,
		CreatedTime:       now,
		UpdatedBy:         user,
		UpdatedTime:       now,
	}
	return node
}
