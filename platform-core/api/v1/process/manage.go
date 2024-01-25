package process

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/gin-gonic/gin"
	"io/ioutil"
	"net/http"
	"sort"
	"strconv"
	"strings"
	"time"
)

const (
	ContextNameProcDefName    string = "procDefName"
	ContextNameProcDefKey     string = "procDefKey"
	ContextNameProcInstName   string = "procInstName"
	ContextNameRootEntityName string = "rootEntityName"
	ContextNameRootEntityId   string = "rootEntityId"
	ContextNameProcInstId     string = "procInstId"
	ContextNameProcInstKey    string = "procInstKey"
	PluginDataTypeString      string = "string"
	PluginParamTypeInput      string = "INPUT"
	PluginParamTypeOutput     string = "OUTPUT"
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
	if len(param.PermissionToRole.MGMT) == 0 {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err,permissionToRole MGMT is empty"))
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
	procDefId := c.Param("proc-def-id")
	if procDefId == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("proc-def-id is empty")))
		return
	}
	procDefDto, err := getProcDefDetailByProcDefId(c, procDefId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, procDefDto)
}

// CopyProcessDefinition 复制编排
func CopyProcessDefinition(c *gin.Context) {
	var pList []*models.ProcDef
	var newProcDefId string
	procDefId := c.Param("proc-def-id")
	association := c.Param("association")
	user := middleware.GetRequestUser(c)
	if procDefId == "" || association == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("proc-def-id or association is empty")))
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
	// 编排没有相关性,则重新生成key
	if association != "y" && association != "Y" {
		procDef.Key = "pdef_key_" + guid.CreateGuid()
		procDef.Name = procDef.Name + "(1)"
		procDef.Version = "v1"
	} else {
		// 校验是否 已有 草稿态数据
		pList, err = database.GetProcessDefinitionByCondition(c, models.ProcDefCondition{Key: procDef.Key, Status: string(models.Draft)})
		if err != nil {
			return
		}
		// 没有同 key并且 草稿态的编排,则可以新建编排
		if len(pList) > 0 {
			middleware.ReturnError(c, fmt.Errorf("this procDef exist draft data"))
			return
		}
		procDef.Version = calcProcDefVersion(c, procDef.Key)
	}
	newProcDefId, err = database.CopyProcessDefinition(c, procDef, user)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, newProcDefId)
}

// QueryProcessDefinitionList 查询编排列表
func QueryProcessDefinitionList(c *gin.Context) {
	var param models.QueryProcessDefinitionParam
	var list []*models.ProcDefQueryDto
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	param.UserRoles = middleware.GetRequestRoles(c)
	list, err = database.QueryProcessDefinitionList(c, param)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, list)
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
	if len(param.PermissionToRole.MGMT) == 0 {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err,permissionToRole MGMT is empty"))
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
	middleware.ReturnSuccess(c)
}

// ExportProcessDefinition 编排批量导出
func ExportProcessDefinition(c *gin.Context) {
	var param models.ProcDefIds
	var resultList []*models.ProcessDefinitionDto
	var procDefList []*models.ProcDef
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if len(param.ProcDefIds) == 0 {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("procDefIds is empty")))
		return
	}
	procDefList, err = database.GetDeployedProcessDefinitionByIds(c, param.ProcDefIds)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if len(procDefList) == 0 {
		middleware.ReturnError(c, fmt.Errorf("procDefIds need correct and deployed"))
		return
	}
	for _, procDef := range procDefList {
		if procDef.Status == string(models.Draft) {
			log.Logger.Info("procDef is draft", log.String("procDefId", procDef.Id))
			continue
		}
		procDefDto, err2 := getProcDefDetailByProcDefId(c, procDef.Id)
		if err2 != nil {
			middleware.ReturnError(c, err2)
			return
		}
		resultList = append(resultList, procDefDto)
	}
	b, jsonErr := json.Marshal(resultList)
	if jsonErr != nil {
		middleware.ReturnError(c, fmt.Errorf("Export requestTemplate config fail, json marshal object error:%s ", jsonErr.Error()))
		return
	}
	c.Writer.Header().Add("Content-Disposition", fmt.Sprintf("attachment; filename=%s_%s.json", "proc_def_"+guid.CreateGuid(), time.Now().Format("20060102150405")))
	c.Data(http.StatusOK, "application/octet-stream", b)
}

// ImportProcessDefinition 批量导入编排
func ImportProcessDefinition(c *gin.Context) {
	var importResult *models.ImportResultDto
	file, err := c.FormFile("file")
	if err != nil {
		middleware.ReturnError(c, fmt.Errorf("Http read upload file fail:"+err.Error()))
		return
	}
	f, err := file.Open()
	if err != nil {
		middleware.ReturnError(c, fmt.Errorf("File open error:"+err.Error()))
		return
	}
	var paramList []*models.ProcessDefinitionDto
	b, err := ioutil.ReadAll(f)
	defer f.Close()
	if err != nil {
		middleware.ReturnError(c, fmt.Errorf("Read content fail error:"+err.Error()))
		return
	}
	err = json.Unmarshal(b, &paramList)
	if err != nil {
		middleware.ReturnError(c, fmt.Errorf("Json unmarshal fail error:"+err.Error()))
		return
	}
	if len(paramList) == 0 {
		middleware.ReturnError(c, fmt.Errorf("import data is empty"))
		return
	}
	importResult, err = processDefinitionImport(c, paramList, middleware.GetRequestUser(c))
	if err != nil {
		middleware.ReturnError(c, err)
	}
	middleware.ReturnData(c, importResult)
	return
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
	// 检查节点的合法性
	if !checkDeployedProcDef(c, procDefId) {
		middleware.ReturnError(c, fmt.Errorf("procDef deployed node check fail"))
		return
	}
	// @todo 计算编排节点顺序
	procDef.Status = string(models.Deployed)
	procDef.UpdatedBy = middleware.GetRequestUser(c)
	procDef.UpdatedTime = time.Now()
	// 发布编排
	err = database.UpdateProcDefStatusAndVersion(c, procDef)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 发布节点
	err = database.UpdateProcDefNodeStatusByProcDefId(c, procDefId, string(models.Deployed), middleware.GetRequestUser(c))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnSuccess(c)
}

// GetProcDefRootTaskNode  获取编排根任务节点
func GetProcDefRootTaskNode(c *gin.Context) {
	var result []*models.ProcDefNode
	procDefId := c.Param("proc-def-id")
	if procDefId == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("proc-def-id is empty")))
		return
	}
	list, err := database.GetProcDefNodeModelByProcDefId(c, procDefId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if len(list) > 0 {
		for _, node := range list {
			if node.NodeType == string(models.ProcDefNodeTypeStart) || node.NodeType == string(models.ProcDefNodeTypeHuman) ||
				node.NodeType == string(models.ProcDefNodeTypeAutomatic) || node.NodeType == string(models.ProcDefNodeTypeData) {
				result = append(result, node)
			}
		}
	}
	middleware.ReturnData(c, result)
}

// AddOrUpdateProcDefTaskNodes 添加更新编排节点
func AddOrUpdateProcDefTaskNodes(c *gin.Context) {
	var param models.ProcDefNodeRequestParam
	var procDef *models.ProcDef
	var procDefNodeList []*models.ProcDefNode
	var procDefNode *models.ProcDefNode
	var nameRepeat bool
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
	// 节点名称不能为空
	if param.ProcDefNodeCustomAttrs.Name == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().ProcDefNodeNameEmptyError, nil))
		return
	}

	procDef, err = database.GetProcessDefinition(c, param.ProcDefNodeCustomAttrs.ProcDefId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if procDef == nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("procDefId is invalid")))
		return
	}
	if procDef.Status != string(models.Draft) {
		middleware.ReturnError(c, fmt.Errorf("draft procDefId:%s can edit", procDef.Id))
		return
	}
	procDefNodeList, err = database.GetProcDefNodeById(c, procDef.Id)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if len(procDefNodeList) > 0 {
		for _, node := range procDefNodeList {
			if node.NodeId == param.ProcDefNodeCustomAttrs.Id {
				procDefNode = node
			} else if param.ProcDefNodeCustomAttrs.Name == node.Name {
				nameRepeat = true
				break
			}
		}
	}
	// 节点名称不能重复
	if nameRepeat {
		middleware.ReturnError(c, exterror.Catch(exterror.New().ProcDefNodeNameRepeatError, nil))
		return
	}
	node := models.ConvertParam2ProcDefNode(user, param)
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
		err = database.DeleteProcDefNodeParam(c, node.Id)
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
		for _, info := range param.ProcDefNodeCustomAttrs.ParamInfos {
			info.Id = guid.CreateGuid()
			info.ProcDefNodeId = node.Id
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
		middleware.ReturnData(c, nodeDto)
		return
	}
	list, err = database.GetProcDefNodeParamByNodeId(c, procDefNode.Id)
	nodeDto = models.ConvertProcDefNode2Dto(procDefNode, list)
	middleware.ReturnData(c, nodeDto)
}

func GetProcDefNodePreorder(c *gin.Context) {
	var err error
	var procDefNode *models.ProcDefNode
	var nodeSimpleDtoMap = make(map[string]*models.ProcDefNodeSimpleDto)
	var nodeLinkList []*models.ProcDefNodeLink
	var targetNodeRecordMap = make(map[string]bool)
	var procDefNodeId string
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
		middleware.ReturnData(c, convertProcDefNodeSimpleMap2Dto(nodeSimpleDtoMap))
		return
	}
	targetNodeRecordMap[procDefNode.Id] = true
	for {
		if len(targetNodeRecordMap) == 0 {
			break
		}
		// 随机获取一个 nodeId
		procDefNodeId = getMapRandomKey(targetNodeRecordMap)
		// 查询 target 等于nodeId 的节点
		nodeLinkList, err = database.GetProcDefNodeLinkByProcDefIdAndTarget(c, procDefId, procDefNodeId)
		// 删除 targetNodeRecordMap 该nodeId
		delete(targetNodeRecordMap, procDefNodeId)
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
		if len(nodeLinkList) == 0 {
			continue
		}
		for _, link := range nodeLinkList {
			// 根据线的 起点节点查询节点数据
			procDefNode, err = database.GetProcDefNodeByIdAndProcDefId(c, procDefId, link.Source)
			if err != nil {
				middleware.ReturnError(c, err)
				return
			}
			if procDefNode != nil {
				// 只需要统计 人工/自动/数据节点
				if procDefNode.NodeType == string(models.ProcDefNodeTypeHuman) || procDefNode.NodeType == string(models.ProcDefNodeTypeAutomatic) ||
					procDefNode.NodeType == string(models.ProcDefNodeTypeData) {
					nodeSimpleDtoMap[procDefNode.Id] = models.ConvertProcDefNode2SimpleDto(procDefNode)
				}
				// 将节点数据 放入到targetNodeRecordMap,后续继续递归
				targetNodeRecordMap[procDefNode.Id] = true
			}
		}
	}
	middleware.ReturnData(c, convertProcDefNodeSimpleMap2Dto(nodeSimpleDtoMap))
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
		middleware.ReturnData(c, interfaceParameterList)
		return
	}

	if procDefNode.NodeType == string(models.ProcDefNodeTypeStart) {
		startEventParams := prepareNodeParameters()
		interfaceParameterList = append(interfaceParameterList, startEventParams...)
		middleware.ReturnData(c, interfaceParameterList)
		return
	}
	if strings.TrimSpace(procDefNode.ServiceName) == "" {
		middleware.ReturnError(c, fmt.Errorf("node:%s serviceName is empty", nodeId))
		return
	}
	pluginConfigInterfaces, err = fetchLatestPluginConfigInterfacesByServiceName(c, procDefNode.ServiceName)
	if err != nil {
		middleware.ReturnData(c, err)
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
	middleware.ReturnData(c, interfaceParameterList)
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
	newProcDefNodeLink := models.ConvertParam2ProcDefNodeLink(&param)
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

func GetProcDefNodeLink(c *gin.Context) {
	var dto *models.ProcDefNodeLinkDto
	procDefId := c.Param("proc-def-id")
	linkId := c.Param("node-link-id")
	if procDefId == "" || linkId == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("procDefId or node-link-id is empty")))
		return
	}
	nodeLink, err := database.GetProcDefNodeLink(c, procDefId, linkId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if nodeLink != nil {
		dto = models.ConvertProcDefNodeLink2Dto(nodeLink)
	}
	middleware.ReturnData(c, dto)
}

func prepareNodeParameters() []*models.InterfaceParameterDto {
	predefineParams := make([]*models.InterfaceParameterDto, 0)

	// 1
	predefineParams = append(predefineParams, &models.InterfaceParameterDto{
		Type:     PluginParamTypeInput,
		Name:     ContextNameProcDefName,
		DataType: PluginDataTypeString,
	})

	// 2
	predefineParams = append(predefineParams, &models.InterfaceParameterDto{
		Type:     PluginParamTypeInput,
		Name:     ContextNameProcDefKey,
		DataType: PluginDataTypeString,
	})
	// 3
	predefineParams = append(predefineParams, &models.InterfaceParameterDto{
		Type:     PluginParamTypeInput,
		Name:     ContextNameProcInstId,
		DataType: PluginDataTypeString,
	})
	// 4
	predefineParams = append(predefineParams, &models.InterfaceParameterDto{
		Type:     PluginParamTypeInput,
		Name:     ContextNameProcInstKey,
		DataType: PluginDataTypeString,
	})

	// 5
	predefineParams = append(predefineParams, &models.InterfaceParameterDto{
		Type:     PluginParamTypeInput,
		Name:     ContextNameProcInstName,
		DataType: PluginDataTypeString,
	})

	// 6
	predefineParams = append(predefineParams, &models.InterfaceParameterDto{
		Type:     PluginParamTypeInput,
		Name:     ContextNameRootEntityName,
		DataType: PluginDataTypeString,
	})

	// 7
	predefineParams = append(predefineParams, &models.InterfaceParameterDto{
		Type:     PluginParamTypeInput,
		Name:     ContextNameRootEntityId,
		DataType: PluginDataTypeString,
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
	return fetchRichPluginConfigInterfacesById(ctx, latestInterfaceEntity.Id)
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
	inputParamEntities, err := database.GetPluginConfigInterfaceParameters(ctx, configInterface.Id, PluginParamTypeInput)
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

	outputParamEntities, err := database.GetPluginConfigInterfaceParameters(ctx, configInterface.Id, PluginParamTypeOutput)
	if err != nil {
		log.Logger.Error("GetPluginConfigInterfaceParameters err", log.Error(err))
		return nil
	}
	if len(outputParamEntities) > 0 {
		for _, outputParam := range outputParamEntities {
			outputParam.PluginConfigInterface = configInterface
			configInterface.AddOutputParameters(outputParam)
			if outputParam.DataType == "object" {
				objectMeta := database.TryFetchEnrichCoreObjectMeta(ctx, outputParam)
				outputParam.ObjectMeta = objectMeta
			}
		}
	}
	return configInterface
}

func getProcDefDetailByProcDefId(ctx context.Context, procDefId string) (procDefDto *models.ProcessDefinitionDto, err error) {
	procDefDto = &models.ProcessDefinitionDto{}
	// 节点
	var nodes []*models.ProcDefNodeResultDto
	// 线
	var edges []*models.ProcDefNodeLinkDto

	procDef, err := database.GetProcessDefinition(ctx, procDefId)
	if err != nil {
		return
	}
	if procDef == nil {
		err = fmt.Errorf("procDefId is invalid")
		return
	}
	procDefDto.ProcDef = models.ConvertProcDef2Dto(procDef)
	historyList, err := database.GetProcessDefinitionByCondition(ctx, models.ProcDefCondition{Key: procDef.Key, Name: procDef.Name})
	if err != nil {
		return
	}
	if len(historyList) <= 1 {
		procDefDto.ProcDef.EnableModifyName = true
	}
	list, err := database.GetProcDefPermissionByCondition(ctx, models.ProcDefPermission{ProcDefId: procDefId})
	if err != nil {
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
	nodes, err = database.GetProcDefNodeByProcDefId(ctx, procDefId)
	if err != nil {
		return
	}
	linkList, err := database.GetProcDefNodeLinkListByProcDefId(ctx, procDefId)
	if err != nil {
		return
	}
	if len(linkList) > 0 {
		for _, link := range linkList {
			edges = append(edges, models.ConvertProcDefNodeLink2Dto(link))
		}
	}
	procDefDto.ProcDefNodeExtend = &models.ProcDefNodeExtendDto{
		Nodes: nodes,
		Edges: edges,
	}
	return
}

func convertProcDefNodeSimpleMap2Dto(hashMap map[string]*models.ProcDefNodeSimpleDto) []*models.ProcDefNodeSimpleDto {
	var list = make([]*models.ProcDefNodeSimpleDto, 0)
	if len(hashMap) > 0 {
		for _, value := range hashMap {
			list = append(list, value)
		}
	}
	return list
}

func getMapRandomKey(hashMap map[string]bool) string {
	if len(hashMap) == 0 {
		return ""
	}
	for key, _ := range hashMap {
		return key
	}
	return ""
}

func processDefinitionImport(ctx context.Context, inputList []*models.ProcessDefinitionDto, operator string) (importResult *models.ImportResultDto, err error) {
	var draftList, repeatNameList []*models.ProcDef
	var versionExist bool
	importResult = &models.ImportResultDto{
		ResultList: make([]*models.ImportResultItemDto, 0),
	}
	for _, procDefDto := range inputList {
		versionExist = false
		if procDefDto.ProcDef == nil {
			continue
		}
		draftList, err = database.GetProcessDefinitionByCondition(ctx, models.ProcDefCondition{Key: procDefDto.ProcDef.Key, Status: string(models.Draft)})
		if err != nil {
			return
		}
		// 已有草稿版本
		if len(draftList) > 0 {
			importResult.ResultList = append(importResult.ResultList, &models.ImportResultItemDto{
				ProcDefName:    procDefDto.ProcDef.Name,
				ProcDefVersion: procDefDto.ProcDef.Version,
				Code:           1,
			})
			continue
		}
		// 判断名称和版本是否重复
		repeatNameList, err = database.GetProcessDefinitionByCondition(ctx, models.ProcDefCondition{Name: procDefDto.ProcDef.Name})
		if err != nil {
			return
		}
		// 重复的名称,将重复名称Key 给导入值
		if len(repeatNameList) > 0 {
			procDefDto.ProcDef.Key = repeatNameList[0].Key
			for _, repeatProcDef := range repeatNameList {
				if repeatProcDef.Version == procDefDto.ProcDef.Version {
					versionExist = true
					importResult.ResultList = append(importResult.ResultList, &models.ImportResultItemDto{
						ProcDefName:    procDefDto.ProcDef.Name,
						ProcDefVersion: procDefDto.ProcDef.Version,
						Code:           2,
					})
					break
				}
			}
		}
		if versionExist {
			continue
		}
		_, err = database.CopyProcessDefinitionByDto(ctx, procDefDto, operator)
		if err != nil {
			importResult.ResultList = append(importResult.ResultList, &models.ImportResultItemDto{
				ProcDefName:    procDefDto.ProcDef.Name,
				ProcDefVersion: procDefDto.ProcDef.Version,
				Code:           3,
			})
			log.Logger.Error("CopyProcessDefinitionByDto err", log.Error(err))
			// 单个编排操作err,err置为空, code=3已经表示服务器错误
			err = nil
		} else {
			importResult.ResultList = append(importResult.ResultList, &models.ImportResultItemDto{
				ProcDefName:    procDefDto.ProcDef.Name,
				ProcDefVersion: procDefDto.ProcDef.Version,
			})
		}
	}
	return
}

/*
*
checkDeployedProcDefNode
发布的时候检测
1、分流必须单进多出
2、汇聚必须多进单出
3、线的两边不能都是分流或汇聚
4、除 分流、汇聚、判断 这三种节点外，其它所有节点必须有单进单出(开始和结束特殊)
5、不能有环路
6. 开始结束节点都不超过一个
7. 判断节点出的线,必须有名字并且同一个判断节点的所有线的名字不能相同
*/
func checkDeployedProcDef(ctx context.Context, procDefId string) bool {
	var startNodeCount, endNodeCount, inCount, outCount int
	var list []*models.ProcDefNode
	var linkList []*models.ProcDefNodeLink
	var err error
	var nodeTypeMap = make(map[string]string)
	// 是否有单进单出
	var hasSingleInOut bool
	list, err = database.GetSimpleProcDefNodeByProcDefId(ctx, procDefId)
	linkList, err = database.GetProcDefNodeLinkListByProcDefId(ctx, procDefId)
	if err != nil {
		return false
	}
	if len(list) == 0 {
		return true
	}

	if len(linkList) == 0 {
		linkList = make([]*models.ProcDefNodeLink, 0)
	}
	for _, node := range list {
		inCount = 0
		outCount = 0
		nodeTypeMap[node.Id] = node.NodeType
		for _, link := range linkList {
			if link.Source == node.Id {
				outCount++
			} else if link.Target == node.Id {
				inCount++
			}
		}
		switch models.ProcDefNodeType(node.NodeType) {
		case models.ProcDefNodeTypeStart:
			startNodeCount++
		case models.ProcDefNodeTypeEnd:
			endNodeCount++
		case models.ProcDefNodeTypeFork:
			// 分流必须单进多出
			if !(inCount == 1 && outCount > 1) {
				log.Logger.Info("checkDeployedProcDefNode fail,fork node is invalid", log.String("procDefId", procDefId),
					log.String("nodeId", node.NodeId))
				return false
			}
		case models.ProcDefNodeTypeMerge:
			// 汇聚必须多进单出
			if !(inCount > 1 && outCount == 1) {
				log.Logger.Info("checkDeployedProcDefNode fail,merge node is invalid", log.String("procDefId", procDefId),
					log.String("nodeId", node.NodeId))
				return false
			}
		case models.ProcDefNodeTypeDecision:
			nodeLinkList, err2 := database.GetProcDefNodeLinkByProcDefIdAndSource(ctx, procDefId, node.Id)
			if err2 != nil {
				return false
			}
			if len(nodeLinkList) > 0 {
				var tempLinkNameMap = make(map[string]bool)
				for _, link := range nodeLinkList {
					if strings.TrimSpace(link.Name) == "" {
						log.Logger.Info("checkDeployedProcDefNode fail,decision node has link name empty", log.String("procDefId", procDefId),
							log.String("nodeId", node.NodeId), log.String("linkId", link.LinkId))
						return false
					}
					if tempLinkNameMap[link.Name] {
						log.Logger.Info("checkDeployedProcDefNode fail,decision node has link name the same", log.String("procDefId", procDefId),
							log.String("nodeId", node.NodeId), log.String("linkId", link.LinkId))
						return false
					} else {
						tempLinkNameMap[link.Name] = true
					}
				}
			}
		default:
			if inCount == 1 && outCount == 1 {
				hasSingleInOut = true
			}
		}
	}
	// 开始，结束节点都不超过1个
	if startNodeCount > 1 || endNodeCount > 1 {
		log.Logger.Info("checkDeployedProcDefNode fail,startNodeCount or  endNodeCount more than one", log.String("procDefId", procDefId))
		return false
	}
	if !hasSingleInOut {
		log.Logger.Info("checkDeployedProcDefNode fail,hasSingleInOut is false", log.String("procDefId", procDefId))
		return false
	}
	// 线的两边不能都是分流或汇聚
	for _, link := range linkList {
		if v, ok := nodeTypeMap[link.Source]; ok {
			if (v == string(models.ProcDefNodeTypeMerge) || v == string(models.ProcDefNodeTypeFork)) && v == nodeTypeMap[link.Target] {
				log.Logger.Info("checkDeployedProcDefNode fail,link is invalid", log.String("procDefId", procDefId),
					log.String("linkId", link.LinkId))
				return false
			}
		}
	}
	return true
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
