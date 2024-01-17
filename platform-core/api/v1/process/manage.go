package process

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
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

const (
	CONTEXT_NAME_PROC_DEF_NAME    string = "procDefName"
	CONTEXT_NAME_PROC_DEF_KEY     string = "procDefKey"
	CONTEXT_NAME_PROC_INST_NAME   string = "procInstName"
	CONTEXT_NAME_ROOT_ENTITY_NAME string = "rootEntityName"
	CONTEXT_NAME_ROOT_ENTITY_ID   string = "rootEntityId"
	CONTEXT_NAME_PROC_INST_ID     string = "procInstId"
	CONTEXT_NAME_PROC_INST_KEY    string = "procInstKey"
	PLUGIN_DATA_TYPE_STRING       string = "string"
	PLUGIN_DATA_TYPE_NUMBER       string = "number"
	PLUGIN_PARAM_TYPE_INPUT       string = "INPUT"
	PLUGIN_PARAM_TYPE_OUTPUT      string = "OUTPUT"
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
	var nodes []*models.ProcDefNodeResultDto
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
	historyList, err := database.GetProcessDefinitionByCondition(c, models.ProcDefCondition{Key: procDef.Key, Name: procDef.Name})
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if len(historyList) <= 1 {
		procDefDto.ProcDef.EnableModifyName = true
	}
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

// BatchUpdateProcessDefinitionStatus 批量更新编排状态
func BatchUpdateProcessDefinitionStatus(c *gin.Context) {
	var param models.BatchUpdateProcDefStatusParam
	var procDef *models.ProcDef
	var err error
	var user = middleware.GetRequestUser(c)
	var draftProcDefList, deployProcDefList, disabledProcDefList []*models.ProcDef
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if len(param.ProcDefIds) == 0 {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("procDefIds is empty")))
		return
	}
	if param.Status != string(models.Disabled) && param.Status != string(models.Enabled) && param.Status != string(models.Deleted) {
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
		switch procDef.Status {
		case string(models.Draft):
			// 记录草稿态编排
			draftProcDefList = append(draftProcDefList, procDef)
		case string(models.Deployed):
			// 记录发布态编排
			deployProcDefList = append(deployProcDefList, procDef)
		case string(models.Disabled):
			// 记录禁用态编排
			disabledProcDefList = append(disabledProcDefList, procDef)
		}
	}
	switch param.Status {
	case string(models.Deleted):
		for _, procDef := range draftProcDefList {
			// 删除编排
			err = database.DeleteProcDefChain(c, procDef.Id)
			if err != nil {
				middleware.ReturnError(c, err)
				return
			}
		}
	case string(models.Disabled):
		for _, procDef := range deployProcDefList {
			procDef.Status = string(models.Disabled)
			procDef.UpdatedBy = user
			procDef.UpdatedTime = time.Now()
			err = database.UpdateProcDefStatus(c, procDef)
			if err != nil {
				middleware.ReturnError(c, err)
				return
			}
		}
	case string(models.Enabled):
		for _, procDef := range disabledProcDefList {
			procDef.Status = string(models.Deployed)
			procDef.UpdatedBy = user
			procDef.UpdatedTime = time.Now()
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

// AddOrUpdateProcDefTaskNodes 添加更新编排节点
func AddOrUpdateProcDefTaskNodes(c *gin.Context) {
	var param models.ProcDefNodeRequestParam
	var procDefNode *models.ProcDefNode
	var err error

	user := middleware.GetRequestUser(c)
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if param.ProcDefNodeCustomAttrs == nil || param.ProcDefNodeCustomAttrs.Id == "" || param.ProcDefNodeCustomAttrs.ProcDefId == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("param procDefId or id is empty")))
		return
	}
	procDefNode, err = database.GetProcDefNode(c, param.ProcDefNodeCustomAttrs.ProcDefId, param.ProcDefNodeCustomAttrs.Id)
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
	if param.ProcDefNodeCustomAttrs.ParamInfos != nil {
		for _, info := range param.ProcDefNodeCustomAttrs.ParamInfos {
			err = database.DeleteProcDefNodeParam(c, node.ProcDefId, info.ParamId)
			if err != nil {
				middleware.ReturnError(c, err)
				return
			}
			info.Id = guid.CreateGuid()
			info.ParamId = node.Id
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
	var nodeDto *models.ProcDefNodeResultDto
	nodeId := c.Param("node-id")
	procDefId := c.Param("proc-def-id")
	if nodeId == "" || procDefId == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("node-is or procDefId is empty")))
		return
	}
	procDefNode, err = database.GetProcDefNode(c, procDefId, nodeId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if procDefNode == nil {
		middleware.Return(c, nodeDto)
		return
	}
	list, err = database.GetProcDefNodeParamByNodeId(c, procDefNode.Id)
	nodeDto = models.ConvertProcDefNode2Dto(procDefNode, list)
	middleware.Return(c, nodeDto)
}

// GetProcDefNodeParameters 获取节点参数
func GetProcDefNodeParameters(c *gin.Context) {
	var interfaceParameterList []*models.InterfaceParameterDto
	var pluginConfigInterfaces *models.PluginConfigInterfaces
	var err error
	var procDefNode *models.ProcDefNode
	nodeId := c.Param("node-id")
	procDefId := c.Param("proc-def-id")
	if procDefId == "" || nodeId == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("node-id or proc-def-id is empty")))
		return
	}
	procDefNode, err = database.GetProcDefNode(c, procDefId, nodeId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if procDefNode == nil {
		middleware.Return(c, interfaceParameterList)
		return
	}

	if procDefNode.NodeType == "startEvent" {
		startEventParams := prepareNodeParameters()
		interfaceParameterList = append(interfaceParameterList, startEventParams...)
		middleware.Return(c, interfaceParameterList)
	}
	pluginConfigInterfaces, err = fetchLatestPluginConfigInterfacesByServiceName(c, procDefNode.ServiceName)
	if err != nil {
		middleware.Return(c, err)
		return
	}
	if pluginConfigInterfaces != nil {
		if len(pluginConfigInterfaces.InputParameters) > 0 {
			for _, parameter := range pluginConfigInterfaces.InputParameters {
				interfaceParameterList = append(interfaceParameterList, models.BuildInterfaceParameterDto(parameter))
			}
		}
		if len(pluginConfigInterfaces.OutputParameters) > 0 {
			for _, parameter := range pluginConfigInterfaces.OutputParameters {
				interfaceParameterList = append(interfaceParameterList, models.BuildInterfaceParameterDto(parameter))
			}
		}
	}
	middleware.Return(c, interfaceParameterList)
}

// DeleteProcDefNode 删除编排节点,同时需要删除线&节点参数
func DeleteProcDefNode(c *gin.Context) {
	var err error
	var procDefNode *models.ProcDefNode
	nodeId := c.Param("node-id")
	procDefId := c.Param("proc-def-id")
	if nodeId == "" || procDefId == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("node-id or proc-def-id is empty")))
		return
	}
	procDefNode, err = database.GetProcDefNode(c, procDefId, nodeId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if procDefNode == nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("not found procDefNode")))
		return
	}
	err = database.DeleteProcDefNode(c, procDefId, nodeId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	err = database.DeleteProcDefNodeLinkByNode(c, procDefNode.Id)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	err = database.DeleteProcDefNodeParamByNodeId(c, procDefNode.Id)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnSuccess(c)
}

func AddOrUpdateProcDefNodeLink(c *gin.Context) {
	var param models.ProcDefNodeLinkDto
	var procDefNodeLink *models.ProcDefNodeLink
	var sourceNode, targetNode *models.ProcDefNode
	var err error

	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if param.ProcDefId == "" || param.ProcDefNodeLinkCustomAttrs == nil || param.ProcDefNodeLinkCustomAttrs.Id == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("param id is empty")))
		return
	}
	sourceNode, err = database.GetProcDefNode(c, param.ProcDefId, param.ProcDefNodeLinkCustomAttrs.Source)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if sourceNode == nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("sourceNode is empty")))
		return
	}
	targetNode, err = database.GetProcDefNode(c, param.ProcDefId, param.ProcDefNodeLinkCustomAttrs.Target)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if targetNode == nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("targetNode is empty")))
		return
	}
	procDefNodeLink, err = database.GetProcDefNodeLink(c, param.ProcDefId, param.ProcDefNodeLinkCustomAttrs.Id)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	param.ProcDefNodeLinkCustomAttrs.Source = sourceNode.Id
	param.ProcDefNodeLinkCustomAttrs.Target = targetNode.Id
	newProcDefNodeLink := models.ConvertParam2ProcDefNodeLink(param)
	if procDefNodeLink == nil {
		newProcDefNodeLink.Id = guid.CreateGuid()
		newProcDefNodeLink.ProcDefId = param.ProcDefId
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

func DeleteProcDefNodeLink(c *gin.Context) {
	var err error
	procDefId := c.Param("proc-def-id")
	linkId := c.Param("node-link-id")
	if procDefId == "" || linkId == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("procDefId or node-link-id is empty")))
		return
	}
	err = database.DeleteProcDefNodeLink(c, procDefId, linkId)
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

func convertParam2ProcDefNode(user string, param models.ProcDefNodeRequestParam) *models.ProcDefNode {
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

func prepareNodeParameters() []*models.InterfaceParameterDto {
	predefineParams := make([]*models.InterfaceParameterDto, 0)

	// 1
	predefineParams = append(predefineParams, &models.InterfaceParameterDto{
		Type:     PLUGIN_DATA_TYPE_STRING,
		Name:     CONTEXT_NAME_PROC_DEF_NAME,
		DataType: PLUGIN_PARAM_TYPE_INPUT,
	})

	// 2
	predefineParams = append(predefineParams, &models.InterfaceParameterDto{
		Type:     PLUGIN_DATA_TYPE_STRING,
		Name:     CONTEXT_NAME_PROC_DEF_KEY,
		DataType: PLUGIN_PARAM_TYPE_INPUT,
	})
	// 3
	predefineParams = append(predefineParams, &models.InterfaceParameterDto{
		Type:     PLUGIN_DATA_TYPE_STRING,
		Name:     CONTEXT_NAME_PROC_INST_ID,
		DataType: PLUGIN_PARAM_TYPE_INPUT,
	})
	// 4
	predefineParams = append(predefineParams, &models.InterfaceParameterDto{
		Type:     PLUGIN_DATA_TYPE_STRING,
		Name:     CONTEXT_NAME_PROC_INST_KEY,
		DataType: PLUGIN_PARAM_TYPE_INPUT,
	})

	// 5
	predefineParams = append(predefineParams, &models.InterfaceParameterDto{
		Type:     PLUGIN_DATA_TYPE_STRING,
		Name:     CONTEXT_NAME_PROC_INST_NAME,
		DataType: PLUGIN_PARAM_TYPE_INPUT,
	})

	// 6
	predefineParams = append(predefineParams, &models.InterfaceParameterDto{
		Type:     PLUGIN_DATA_TYPE_STRING,
		Name:     CONTEXT_NAME_ROOT_ENTITY_NAME,
		DataType: PLUGIN_PARAM_TYPE_INPUT,
	})

	// 7
	predefineParams = append(predefineParams, &models.InterfaceParameterDto{
		Type:     PLUGIN_DATA_TYPE_STRING,
		Name:     CONTEXT_NAME_ROOT_ENTITY_ID,
		DataType: PLUGIN_PARAM_TYPE_INPUT,
	})

	return predefineParams
}

func fetchLatestPluginConfigInterfacesByServiceName(ctx context.Context, serviceName string) (pluginConfigInterfaces *models.PluginConfigInterfaces, err error) {
	var richInterfaceList []*models.RichPluginConfigInterfaces
	richInterfaceList, err = database.GetAllByServiceNameAndConfigStatus(ctx, serviceName, "ENABLED")
	if err != nil {
		return
	}
	if len(richInterfaceList) == 0 {
		err = fmt.Errorf("plguin interface not found for serviceName [%s]", serviceName)
		return
	}
	// 版本排序
	sort.Sort(models.RichPluginConfigInterfacesSort(richInterfaceList))
	latestInterfaceEntity := richInterfaceList[len(richInterfaceList)-1]
	return fetchRichPluginConfigInterfacesById(ctx, latestInterfaceEntity.PluginConfigId)
}

func fetchRichPluginConfigInterfacesById(ctx context.Context, interfaceId string) (resultInterface *models.PluginConfigInterfaces, err error) {
	var pluginConfigInterface *models.PluginConfigInterfaces
	var pluginConfig *models.PluginConfigs
	var pluginPackage *models.PluginPackages
	pluginConfigInterface, err = database.GetConfigInterfacesById(ctx, interfaceId)
	if err != nil {
		return
	}
	if pluginConfigInterface == nil {
		return
	}
	pluginConfig, err = database.GetPluginConfigById(ctx, pluginConfigInterface.PluginConfigId)
	if err != nil {
		return
	}
	if pluginConfig != nil {
		pluginConfigInterface.PluginConfig = pluginConfig
		pluginPackage, err = database.GetPluginPackageById(ctx, pluginConfig.PluginPackageId)
		if err != nil {
			return
		}
		pluginConfig.PluginPackages = pluginPackage
	}
	resultInterface = enrichPluginConfigInterfaces(ctx, pluginConfigInterface)
	return
}

func enrichPluginConfigInterfaces(ctx context.Context, configInterface *models.PluginConfigInterfaces) *models.PluginConfigInterfaces {
	inputParamEntities, err := database.GetPluginConfigInterfaceParameters(ctx, configInterface.Id, "INPUT")
	if err != nil {
		log.Logger.Error("GetPluginConfigInterfaceParameters err", log.Error(err))
		return nil
	}
	if len(inputParamEntities) > 0 {
		for _, inputParam := range inputParamEntities {
			inputParam.PluginConfigInterface = configInterface
			configInterface.AddInputParameters(inputParam)
			if inputParam.DataType == "object" {
				objectMeta := database.TryFetchEnrichCoreObjectMeta(ctx, inputParam)
				inputParam.ObjectMeta = objectMeta
			}
		}
	}

	outputParamEntities, err := database.GetPluginConfigInterfaceParameters(ctx, configInterface.Id, "OUTPUT")
	if err != nil {
		log.Logger.Error("GetPluginConfigInterfaceParameters err", log.Error(err))
		return nil
	}
	if len(outputParamEntities) > 0 {
		for _, outputParam := range outputParamEntities {
			outputParam.PluginConfigInterface = configInterface
			configInterface.AddInputParameters(outputParam)
			if outputParam.DataType == "object" {
				objectMeta := database.TryFetchEnrichCoreObjectMeta(ctx, outputParam)
				outputParam.ObjectMeta = objectMeta
			}
		}
	}
	return configInterface
}
