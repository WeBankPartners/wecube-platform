package process

import (
	"context"
	"encoding/json"
	"fmt"
	"sort"
	"strconv"
	"strings"
	"time"

	"github.com/WeBankPartners/go-common-lib/guid"
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
	if param.Name == "" {
		err = fmt.Errorf("name & version not empty")
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	// 1.权限参数校验
	if len(param.PermissionToRole.USE) == 0 || len(param.PermissionToRole.MGMT) == 0 {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err,permissionToRole is empty"))
		return
	}
	if param.Id == "" {
		entity, err = database.AddProcessDefinition(c, middleware.GetRequestUser(c), param)
	} else {
		result, err := database.GetProcessDefinition(c, param.Id)
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
		if result == nil {
			middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("param id is invalid")))
			return
		}
		if result.Status == string(models.Deployed) {
			middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("this procDef has deployed")))
			return
		}
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
	// 新增删除角色
	err = database.BatchAddProcDefPermission(c, entity.Id, param.PermissionToRole)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, models.ConvertProcDef2Dto(entity))
}

// GetProcessDefinition 获取编排
func GetProcessDefinition(c *gin.Context) {
	procDefDto := &models.ProcessDefinitionDto{}
	// 节点
	var nodes []*models.ProcDefNodeDto
	// 线
	var edges []*models.ProcDefNodeLinkDto
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
	if procDef == nil {
		middleware.ReturnError(c, fmt.Errorf("proc-def-id is invalid"))
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
	nodes, err = database.GetProcDefNodeByProcDefId(c, procDefId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if len(nodes) > 0 {
		for _, node := range nodes {
			dtoList, err := database.GetProcDefNodeLinkBySource(c, node.ProcDefNodeCustomAttrs.Id)
			if err != nil {
				middleware.ReturnError(c, err)
				return
			}
			if dtoList != nil {
				edges = append(edges, dtoList...)
			}
		}
	}
	procDefDto.ProcDefNodeExtend = &models.ProcDefNodeExtendDto{
		Nodes: nodes,
		Edges: edges,
	}
	middleware.ReturnData(c, procDefDto)
}

// CheckProcDefNameExist 判断编排名称是否存在
func CheckProcDefNameExist(c *gin.Context) {
	var param models.CheckProcDefNameParam
	var err error
	var procDefList []*models.ProcDef
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if param.Name == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("param name is empty")))
		return
	}
	procDefList, err = database.GetProcessDefinitionByCondition(c, models.ProcDefCondition{Key: param.Key, Name: param.Name})
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if len(procDefList) > 0 {
		middleware.ReturnData(c, "yes")
		return
	}
	middleware.ReturnData(c, "no")
}

// BatchUpdateProcessDefinitionStatus 批量更新编排状态
func BatchUpdateProcessDefinitionStatus(c *gin.Context) {
	var param models.BatchUpdateProcDefStatusParam
	var procDef *models.ProcDef
	var err error
	var finalUpdateProcDefList, finalDeleteProcDefList []*models.ProcDef
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if len(param.ProcDefIds) == 0 {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("procDefIds is empty")))
		return
	}
	if param.Status != string(models.Disabled) || param.Status != string(models.Deployed) || param.Status != string(models.Deleted) {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("status param is invalid")))
		return
	}
	for _, procDefId := range param.ProcDefIds {
		procDef, err = database.GetProcessDefinition(c, procDefId)
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
		if procDef == nil {
			continue
		}
		if procDef.Status == string(models.Draft) {
			finalDeleteProcDefList = append(finalDeleteProcDefList, procDef)
		} else if procDef.Status == string(models.Deployed) || procDef.Status == string(models.Disabled) {
			finalUpdateProcDefList = append(finalUpdateProcDefList, procDef)
		}
	}
	switch param.Status {
	case string(models.Deleted):
		for _, procDef := range finalDeleteProcDefList {
			err = database.DeleteProcDef(c, procDef.Id)
			if err != nil {
				middleware.ReturnError(c, err)
				return
			}
		}
	default:
		for _, procDef := range finalUpdateProcDefList {
			// 节点状态改成待更新状态
			procDef.Status = param.Status
			err = database.UpdateProcDefStatus(c, procDef)
			if err != nil {
				middleware.ReturnError(c, err)
				return
			}
		}
	}
	middleware.ReturnSuccess(c)
}

// BatchUpdateProcessDefinitionPermission 批量更新编排权限
func BatchUpdateProcessDefinitionPermission(c *gin.Context) {
	var param models.BatchUpdateProcDefPermission
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if len(param.ProcDefIds) == 0 {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("procDefIds is empty")))
		return
	}
	// 1.权限参数校验
	if len(param.PermissionToRole.USE) == 0 || len(param.PermissionToRole.MGMT) == 0 {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err,permissionToRole USE or MGMT is empty"))
		return
	}
	for _, procDefId := range param.ProcDefIds {
		// 新增删除角色
		procDef, err := database.GetProcessDefinition(c, procDefId)
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
		if procDef.Id == "" {
			continue
		}
		err = database.BatchAddProcDefPermission(c, procDefId, param.PermissionToRole)
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
	}
}

// DeployProcessDefinition 编排定义发布
func DeployProcessDefinition(c *gin.Context) {
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
	if procDef == nil {
		middleware.ReturnError(c, fmt.Errorf("proc-def-id is invalid"))
		return
	}
	// 草稿态才能发布
	if procDef.Status != string(models.Draft) {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("this procDef status is not draft")))
		return
	}
	// @todo 计算编排节点顺序
	procDef.Status = string(models.Deployed)
	procDef.UpdatedBy = middleware.GetRequestUser(c)
	procDef.UpdatedTime = time.Now()
	// 计算编排的版本
	procDef.Version = calcProcDefVersion(c, procDef.Key)
	// 发布编排
	err = database.UpdateProcDefStatusAndVersion(c, procDef)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 发布节点
	err = database.UpdateProcDefNodeStatusByProcDefId(c, procDefId, string(models.Deployed))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnSuccess(c)
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
	procDefNode, err = database.GetProcDefNodeByNodeId(c, param.ProcDefNodeCustomAttrs.Id)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	node := convertParam2ProcDefNode(user, param)
	if procDefNode == nil {
		node.Id = guid.CreateGuid()
		err = database.InsertProcDefNode(c, node)
	} else {
		node.Id = procDefNode.Id
		node.Status = procDefNode.Status
		node.CreatedBy = procDefNode.CreatedBy
		node.CreatedTime = procDefNode.CreatedTime
		err = database.UpdateProcDefNode(c, node)
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

// GetProcDefNode 获取编排节点
func GetProcDefNode(c *gin.Context) {
	var err error
	var procDefNode *models.ProcDefNode
	var list []*models.ProcDefNodeParam
	var nodeDto *models.ProcDefNodeDto
	nodeId := c.Param("node-id")
	if nodeId == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("node-is is empty")))
		return
	}
	procDefNode, err = database.GetProcDefNodeByNodeId(c, nodeId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	list, err = database.GetProcDefNodeParamByNodeId(c, nodeId)
	if procDefNode != nil {
		nodeDto = models.ConvertProcDefNode2Dto(procDefNode, list)
	}
	middleware.Return(c, nodeDto)
}

// DeleteProcDefNode 删除编排节点,同时需要删除线&节点参数
func DeleteProcDefNode(c *gin.Context) {
	var err error
	nodeId := c.Param("node-id")
	if nodeId == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("node-id is empty")))
		return
	}
	err = database.DeleteProcDefNode(c, nodeId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	err = database.DeleteProcDefNodeLinkByNode(c, nodeId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	err = database.DeleteProcDefNodeParamByNodeId(c, nodeId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
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
	procDefNodeLink, err = database.GetProcDefNodeLinkByLinkId(c, param.ProcDefNodeLinkCustomAttrs.Id)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	newProcDefNodeLink := models.ConvertParam2ProcDefNodeLink(param)
	if procDefNodeLink == nil {
		newProcDefNodeLink.Id = guid.CreateGuid()
		err = database.InsertProcDefNodeLink(c, newProcDefNodeLink)
	} else {
		newProcDefNodeLink.Id = procDefNodeLink.Id
		err = database.UpdateProcDefNodeLink(c, newProcDefNodeLink)
	}
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnSuccess(c)
}

func GetProcDefNodeLink(c *gin.Context) {
	var dto *models.ProcDefNodeLinkDto
	nodeLinkId := c.Param("node-link-id")
	if nodeLinkId == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("node-link-id is empty")))
		return
	}
	nodeLink, err := database.GetProcDefNodeLinkByLinkId(c, nodeLinkId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if nodeLink != nil {
		dto = models.ConvertProcDefNodeLink2Dto(nodeLink)
	}
	middleware.Return(c, dto)
}

func DeleteProcDefNodeLink(c *gin.Context) {
	nodeId := c.Param("node-link-id")
	if nodeId == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("node-link-id is empty")))
		return
	}
	err := database.DeleteProcDefNodeLinkByLinkId(c, nodeId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnSuccess(c)
}

// calcProcDefVersion 计算编排版本
func calcProcDefVersion(ctx context.Context, key string) string {
	var version int
	list, err := database.GetProcessDefinitionByCondition(ctx, models.ProcDefCondition{Key: key})
	if err != nil {
		return ""
	}
	if len(list) == 1 && list[0].Version == "" {
		return "v1"
	}
	sort.Sort(models.ProcDefSort(list))
	version, _ = strconv.Atoi(list[len(list)-1].Version[1:])
	return fmt.Sprintf("v%d", version+1)
}

func convertParam2ProcDefNode(user string, param models.ProcDefNodeDto) *models.ProcDefNode {
	now := time.Now()
	byteArr, _ := json.Marshal(param.NodeAttrs)
	procDefNodeAttr := param.ProcDefNodeCustomAttrs
	byteArr2, _ := json.Marshal(procDefNodeAttr.TimeConfig)
	node := &models.ProcDefNode{
		NodeId:            procDefNodeAttr.Id,
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
		TimeConfig:        string(byteArr2),
		OrderedNo:         procDefNodeAttr.OrderedNo,
		UiStyle:           string(byteArr),
		CreatedBy:         user,
		CreatedTime:       now,
		UpdatedBy:         user,
		UpdatedTime:       now,
	}
	return node
}
