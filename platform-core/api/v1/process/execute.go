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
	"github.com/WeBankPartners/wecube-platform/platform-core/services/execution"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/workflow"
	"github.com/gin-gonic/gin"
	"strings"
	"time"
)

func ProcDefList(c *gin.Context) {
	includeDraft := c.Query("includeDraft") // 0 | 1
	permission := c.Query("permission")     // USE | MGMT
	tag := c.Query("tag")
	plugin := c.Query("plugin")
	subProc := c.Query("subProc")
	rootEntity := c.Query("rootEntity")
	if includeDraft == "" {
		includeDraft = "0"
	}
	if permission == "" {
		permission = "USE"
	}
	if subProc == "" {
		subProc = "all"
	}
	log.Logger.Debug("procDefList", log.String("includeDraft", includeDraft), log.String(permission, "permission"), log.String("tag", tag), log.StringList("roleList", middleware.GetRequestRoles(c)))
	result, err := database.ProcDefList(c, includeDraft, permission, tag, plugin, subProc, middleware.GetRequestUser(c), rootEntity, middleware.GetRequestRoles(c))
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func PublicProcDefList(c *gin.Context) {
	permission := c.Query("permission") // USE | MGMT
	tag := c.Query("tag")
	plugin := c.Query("plugin")
	withAll := c.Query("all")
	subProc := c.Query("subProc")
	rootEntity := c.Query("rootEntity")
	rootEntityGuid := c.Query("rootEntityGuid")
	var exprList []*models.ExpressionObj
	var queryResult []map[string]interface{}
	if permission == "" {
		permission = "USE"
	}
	if withAll == "" {
		withAll = "N"
	}
	if subProc == "" {
		subProc = "main"
	}
	// 如果插件属主不传则返回空
	if plugin == "" {
		middleware.ReturnData(c, []*models.PublicProcDefObj{})
		return
	}
	if strings.TrimSpace(rootEntity) != "" {
		rootEntity = strings.Split(rootEntity, "{")[0]
	}
	log.Logger.Debug("public procDefList", log.String(permission, "permission"), log.String("tag", tag))
	procList, err := database.ProcDefList(c, "0", permission, tag, plugin, subProc, middleware.GetRequestUser(c), rootEntity, middleware.GetRequestRoles(c))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if withAll == "N" {
		var newProcList []*models.ProcDefListObj
		procKeyMap := make(map[string]int)
		for _, row := range procList {
			if _, ok := procKeyMap[row.ProcDefKey]; ok {
				continue
			}
			newProcList = append(newProcList, row)
			procKeyMap[row.ProcDefKey] = 1
		}
		procList = newProcList
	}
	var result, newResult []*models.PublicProcDefObj
	entityMap := make(map[string]*models.ProcEntity)
	for _, row := range procList {
		resultObj := models.PublicProcDefObj{
			ProcDefId:            row.ProcDefId,
			ProcDefKey:           row.ProcDefKey,
			ProcDefName:          row.ProcDefName,
			Status:               row.Status,
			CreatedTime:          row.CreatedTime,
			RootEntity:           &models.ProcEntity{},
			ProcDefVersion:       row.ProcDefVersion,
			RootEntityExpression: row.RootEntity,
		}
		rootExpression := row.RootEntity
		if tmpIndex := strings.Index(rootExpression, "{"); tmpIndex > 0 {
			rootExpression = rootExpression[:tmpIndex]
		}
		entityMsg := strings.Split(rootExpression, ":")
		if len(entityMsg) != 2 {
			result = append(result, &resultObj)
			continue
		}
		if entityDef, ok := entityMap[rootExpression]; ok {
			resultObj.RootEntity = entityDef
		} else {
			tmpEntityDef, tmpErr := database.GetEntityModel(c, entityMsg[0], entityMsg[1], true)
			if tmpErr != nil {
				err = tmpErr
				break
			}
			tmpRootEntity := models.ProcEntity{
				Id:          tmpEntityDef.Id,
				PackageName: tmpEntityDef.PackageName,
				Name:        tmpEntityDef.Name,
				Description: tmpEntityDef.Description,
				DisplayName: tmpEntityDef.DisplayName,
			}
			tmpRootEntity.ParseAttr(tmpEntityDef.Attributes)
			resultObj.RootEntity = &tmpRootEntity
			entityMap[rootExpression] = &tmpRootEntity
		}
		result = append(result, &resultObj)
	}
	if strings.TrimSpace(rootEntityGuid) != "" {
		// 调用编排分析,获取操作对象列表
		for _, procDef := range result {
			if exprList, err = remote.AnalyzeExpression(procDef.RootEntityExpression); err != nil {
				return
			}
			if queryResult, err = remote.QueryPluginData(c, exprList, []*models.QueryExpressionDataFilter{}, remote.GetToken()); err != nil {
				return
			}
			for _, data := range queryResult {
				if v, ok := data["id"]; ok && v == rootEntityGuid {
					result = append(result, procDef)
				}
			}
		}
	} else {
		newResult = result
	}
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, newResult)
	}
}

func ProcDefOutline(c *gin.Context) {
	procDefId := c.Param("proc-def-id")
	log.Logger.Debug("ProcDefOutline", log.String("procDefId", procDefId))
	result, err := database.ProcDefOutline(c, procDefId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func ProcDefRootEntities(c *gin.Context) {
	procDefId := c.Param("proc-def-id")
	log.Logger.Debug("ProcDefRootEntities", log.String("procDefId", procDefId))
	procDefObj, err := database.GetSimpleProcDefRow(c, procDefId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	exprList, analyzeErr := remote.AnalyzeExpression(procDefObj.RootEntity)
	if analyzeErr != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, analyzeErr))
		return
	}
	result, queryErr := remote.QueryPluginData(c, exprList, []*models.QueryExpressionDataFilter{}, c.GetHeader(models.AuthorizationHeader))
	if queryErr != nil {
		middleware.ReturnError(c, queryErr)
	} else {
		middleware.ReturnData(c, result)
	}
}

func ProcDefPreview(c *gin.Context) {
	procDefId := c.Param("proc-def-id")
	entityDataId := c.Param("entityDataId")
	sessionId := c.Query("sessionId")
	if sessionId != "" {
		result, err := database.GetProcPreviewBySession(c, sessionId)
		if err != nil {
			middleware.ReturnError(c, err)
		} else {
			middleware.ReturnData(c, result)
		}
		return
	}
	result, err := execution.BuildProcPreviewData(c, procDefId, entityDataId, middleware.GetRequestUser(c))
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func PublicProcDefPreview(c *gin.Context) {
	procDefId := c.Param("proc-def-id")
	entityDataId := c.Param("entityDataId")
	log.Logger.Debug("ProcDefPreview", log.String("procDefId", procDefId), log.String("entityDataId", entityDataId))
	procOutlineData, err := database.ProcDefOutline(c, procDefId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	rootExprList, analyzeErr := remote.AnalyzeExpression(procOutlineData.RootEntity)
	if analyzeErr != nil {
		middleware.ReturnError(c, analyzeErr)
		return
	}
	rootLastExprObj := rootExprList[len(rootExprList)-1]
	rootFilter := models.QueryExpressionDataFilter{
		Index:       len(rootExprList) - 1,
		PackageName: rootLastExprObj.Package,
		EntityName:  rootLastExprObj.Entity,
		AttributeFilters: []*models.QueryExpressionDataAttrFilter{{
			Name:     "id",
			Operator: "eq",
			Value:    entityDataId,
		}},
	}
	rootDataList, getRootDataErr := remote.QueryPluginData(c, rootExprList, []*models.QueryExpressionDataFilter{&rootFilter}, remote.GetToken())
	if getRootDataErr != nil {
		middleware.ReturnError(c, getRootDataErr)
		return
	}
	if len(rootDataList) != 1 {
		middleware.ReturnError(c, fmt.Errorf("root data match %d rows,illegal ", len(rootDataList)))
		return
	}
	entityNodeMap := make(map[string]*models.ProcPreviewEntityNode)
	rootData := rootDataList[0]
	previewResult := models.ProcPreviewData{ProcessSessionId: fmt.Sprintf("proc_session_" + guid.CreateGuid()), EntityTreeNodes: []*models.ProcPreviewEntityNode{}}
	log.Logger.Debug("rootData", log.String("entityDataId", entityDataId), log.JsonObj("data", rootData))
	rootEntityNode := models.ProcPreviewEntityNode{}
	rootEntityNode.Parse(rootFilter.PackageName, rootFilter.EntityName, rootData)
	rootEntityNode.FullDataId = rootEntityNode.DataId
	rootEntityNode.EntityData = rootData
	entityNodeMap[rootEntityNode.Id] = &rootEntityNode
	previewResult.EntityTreeNodes = append(previewResult.EntityTreeNodes, &rootEntityNode)
	for _, node := range procOutlineData.FlowNodes {
		if node.OrderedNo == "" || node.RoutineExpression == "" {
			continue
		}
		nodeExpressionList := []string{}
		if node.NodeType == "data" {
			tmpExprObjList, tmpErr := database.GetProcDataNodeExpression(node.RoutineExpression)
			if tmpErr != nil {
				err = tmpErr
				break
			}
			for _, tmpExprObj := range tmpExprObjList {
				nodeExpressionList = append(nodeExpressionList, tmpExprObj.Expression)
			}
		} else {
			nodeExpressionList = append(nodeExpressionList, node.RoutineExpression)
		}
		if err != nil {
			break
		}
		interfaceFilters := []*models.Filter{}
		if node.ServiceId != "" {
			interfaceObj, getInterfaceErr := database.GetSimpleLastPluginInterface(c, node.ServiceId)
			if getInterfaceErr != nil {
				err = fmt.Errorf("get node plugin interface:%s fail,%s ", node.ServiceId, getInterfaceErr.Error())
				break
			}
			if interfaceObj.FilterRule != "" {
				if interfaceFilters, err = remote.AnalyzeExprFilters(interfaceObj.FilterRule); err != nil {
					err = fmt.Errorf("analyze expr filters:%s fail,%s ", interfaceObj.FilterRule, err.Error())
					break
				}
			}
		}
		nodeDataList := []*models.ProcPreviewEntityNode{}
		for _, nodeExpression := range nodeExpressionList {
			if nodeExpression == procOutlineData.RootEntity {
				nodeDataList = append(nodeDataList, &rootEntityNode)
			} else {
				tmpQueryDataParam := models.QueryExpressionDataParam{DataModelExpression: nodeExpression, Filters: []*models.QueryExpressionDataFilter{&rootFilter}}
				tmpNodeDataList, tmpErr := execution.QueryProcPreviewNodeData(c, &tmpQueryDataParam, &rootEntityNode, true, interfaceFilters)
				if tmpErr != nil {
					err = tmpErr
					break
				}
				nodeDataList = append(nodeDataList, tmpNodeDataList...)
			}
		}
		if err != nil {
			break
		}
		log.Logger.Debug("nodeData", log.String("node", node.NodeId), log.JsonObj("data", nodeDataList))
		for _, nodeDataObj := range nodeDataList {
			if existEntityNodeObj, ok := entityNodeMap[nodeDataObj.Id]; !ok {
				entityNodeMap[nodeDataObj.Id] = nodeDataObj
				previewResult.EntityTreeNodes = append(previewResult.EntityTreeNodes, nodeDataObj)
			} else {
				existEntityNodeObj.PreviousIds = append(existEntityNodeObj.PreviousIds, nodeDataObj.PreviousIds...)
				existEntityNodeObj.SucceedingIds = append(existEntityNodeObj.SucceedingIds, nodeDataObj.SucceedingIds...)
			}
		}
	}
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	previewResult.AnalyzeRefIds()
	middleware.ReturnData(c, previewResult)
}

func GetProcInsPreview(c *gin.Context) {
	procInsId := c.Param("procInsId")
	result, err := database.GetProcPreviewEntityNode(c, procInsId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func ProcInsTaskNodeBindings(c *gin.Context) {
	sessionId := c.Param("sessionId")
	if sessionId == "" {
		middleware.ReturnError(c, exterror.New().RequestParamValidateError)
		return
	}
	taskNodeId := c.Param("taskNodeId")
	result, err := database.ProcInsTaskNodeBindings(c, sessionId, taskNodeId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func GetInstanceTaskNodeBindings(c *gin.Context) {
	procInsId := c.Param("procInsId")
	result, err := database.GetInstanceTaskNodeBindings(c, procInsId, "")
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func UpdateProcNodeBindingData(c *gin.Context) {
	var param []*models.TaskNodeBindingObj
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	sessionId := c.Param("sessionId")
	if sessionId == "" {
		middleware.ReturnError(c, exterror.New().RequestParamValidateError)
		return
	}
	taskNodeId := c.Param("taskNodeId")
	err := database.UpdateProcNodeBindingData(c, param, sessionId, taskNodeId, middleware.GetRequestUser(c))
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnSuccess(c)
	}
}

func ProcInsStart(c *gin.Context) {
	var param models.ProcInsStartParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	operator := middleware.GetRequestUser(c)
	// 检测编排定义状态是否合法
	if err := database.CheckProcDefStatus(c, param.ProcDefId); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 检测是不是子编排
	isSubSession, queryErr := database.CheckSubProcStart(c, param.ProcessSessionId)
	if queryErr != nil {
		middleware.ReturnError(c, queryErr)
		return
	}
	if isSubSession {
		middleware.ReturnError(c, fmt.Errorf("subProcess can not start"))
		return
	}
	// 新增 proc_ins,proc_ins_node,proc_data_binding 纪录
	procInsId, workflowRow, workNodes, workLinks, err := database.CreateProcInstance(c, &param, operator)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 初始化workflow并开始
	workObj := workflow.Workflow{ProcRunWorkflow: *workflowRow}
	workObj.Init(context.Background(), workNodes, workLinks)
	//workflow.GlobalWorkflowMap.Store(workObj.Id, &workObj)
	go workObj.Start(&models.ProcOperation{CreatedBy: operator})
	// 查询 detail 返回
	detail, queryErr := database.GetProcInstance(c, procInsId)
	if queryErr != nil {
		middleware.ReturnError(c, queryErr)
	} else {
		middleware.ReturnData(c, detail)
	}
}

func ProcInsList(c *gin.Context) {
	withCronIns := strings.ToLower(c.Query("withCronIns"))
	withSubProc := strings.ToLower(c.Query("withSubProc"))
	mgmtRole := c.Query("mgmtRole")
	search := c.Query("search")
	status := c.Query("status")
	result, err := database.ListProcInstance(c, middleware.GetRequestRoles(c), withCronIns, withSubProc, mgmtRole, search, status)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func ProcInsDetail(c *gin.Context) {
	procInsId := c.Param("procInsId")
	detail, queryErr := database.GetProcInstance(c, procInsId)
	if queryErr != nil {
		middleware.ReturnError(c, queryErr)
	} else {
		middleware.ReturnData(c, detail)
	}
}

func GetProcInsNodeContext(c *gin.Context) {
	procInsId := c.Param("procInsId")
	procInsNodeId := c.Param("procInsNodeId")
	if procInsId == "" || procInsNodeId == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("path param can not empty")))
		return
	}
	result, err := database.GetProcInsNodeContext(c, procInsId, procInsNodeId, "")
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func PublicProcInsStart(c *gin.Context) {
	var param models.RequestProcessData
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	// result -> StartInstanceResultData
	operator := middleware.GetRequestUser(c)
	// 新增 proc_ins,proc_ins_node,proc_data_binding 纪录
	procInsId, workflowRow, workNodes, workLinks, err := database.CreatePublicProcInstance(c, &param, operator)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 初始化workflow并开始
	workObj := workflow.Workflow{ProcRunWorkflow: *workflowRow}
	workObj.Init(context.Background(), workNodes, workLinks)
	//workflow.GlobalWorkflowMap.Store(workObj.Id, &workObj)
	go workObj.Start(&models.ProcOperation{CreatedBy: operator})
	// 查询 detail 返回
	detail, queryErr := database.GetProcInstance(c, procInsId)
	if queryErr != nil {
		middleware.ReturnError(c, queryErr)
	} else {
		if detail.Id != "" && detail.Status == "NotStarted" {
			detail.Status = "InProgress"
		}
		middleware.ReturnData(c, detail)
	}
}

func ProcInsOperation(c *gin.Context) {
	var param models.ProcInsOperationParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	permissionLegal, checkPermissionErr := database.CheckProcInsUserPermission(c, middleware.GetRequestRoles(c), param.ProcInstId)
	if checkPermissionErr != nil {
		middleware.ReturnError(c, checkPermissionErr)
		return
	}
	if !permissionLegal {
		middleware.ReturnError(c, exterror.New().DataPermissionDeny)
		return
	}
	procInsObj, getProcInsErr := database.GetSimpleProcInsRow(c, param.ProcInstId)
	if getProcInsErr != nil {
		middleware.ReturnError(c, getProcInsErr)
		return
	}
	workflowId, nodeId, err := database.GetProcWorkByInsId(c, param.ProcInstId, param.NodeInstId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	nodeObj := &models.ProcInsNode{}
	if param.NodeInstId != "" {
		nodeObj, err = database.GetSimpleProcInsNode(c, param.NodeInstId, "")
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
	}
	if param.Act == "skip" {
		if procInsObj.Status != models.JobStatusRunning {
			middleware.ReturnError(c, exterror.New().ProcStatusOperationError)
			//middleware.ReturnError(c, fmt.Errorf("ProcIns status:%s ,operation %s illegal", procInsObj.Status, param.Act))
			return
		}
		operationObj := models.ProcRunOperation{WorkflowId: workflowId, NodeId: nodeId, Operation: "ignore", Status: "wait", CreatedBy: middleware.GetRequestUser(c)}
		operationObj.Id, err = database.AddWorkflowOperation(c, &operationObj)
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
		go workflow.HandleProOperation(&operationObj)
		if nodeObj.NodeType == models.JobSubProcType {
			killSubProc(c, param.ProcInstId, param.NodeInstId, middleware.GetRequestUser(c))
		}
	} else if param.Act == "choose" {
		if procInsObj.Status != models.JobStatusRunning {
			middleware.ReturnError(c, exterror.New().ProcStatusOperationError)
			//middleware.ReturnError(c, fmt.Errorf("ProcIns status:%s ,operation %s illegal", procInsObj.Status, param.Act))
			return
		}
		if param.Message == "" {
			middleware.ReturnError(c, fmt.Errorf("param message can not empty with choose action"))
			return
		}
		operationObj := models.ProcRunOperation{WorkflowId: workflowId, NodeId: nodeId, Operation: "approve", Status: "wait", CreatedBy: middleware.GetRequestUser(c), Message: param.Message}
		operationObj.Id, err = database.AddWorkflowOperation(c, &operationObj)
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
		go workflow.HandleProOperation(&operationObj)
	} else if param.Act == "stop" {
		if procInsObj.Status != models.JobStatusRunning {
			middleware.ReturnError(c, exterror.New().ProcStatusOperationError)
			//middleware.ReturnError(c, fmt.Errorf("ProcIns status:%s ,operation %s illegal", procInsObj.Status, param.Act))
			return
		}
		operationObj := models.ProcRunOperation{WorkflowId: workflowId, Operation: "stop", Status: "wait", CreatedBy: middleware.GetRequestUser(c)}
		operationObj.Id, err = database.AddWorkflowOperation(c, &operationObj)
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
		go workflow.HandleProOperation(&operationObj)
		time.Sleep(2500 * time.Millisecond)
	} else if param.Act == "recover" {
		if procInsObj.Status != models.WorkflowStatusStop {
			middleware.ReturnError(c, exterror.New().ProcStatusOperationError)
			//middleware.ReturnError(c, fmt.Errorf("ProcIns status:%s ,operation %s illegal", procInsObj.Status, param.Act))
			return
		}
		operationObj := models.ProcRunOperation{WorkflowId: workflowId, Operation: "continue", Status: "wait", CreatedBy: middleware.GetRequestUser(c)}
		operationObj.Id, err = database.AddWorkflowOperation(c, &operationObj)
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
		go workflow.HandleProOperation(&operationObj)
		time.Sleep(2500 * time.Millisecond)
	}
	middleware.ReturnSuccess(c)
}

func GetProcInsTaskNodeBindings(c *gin.Context) {
	procInsId := c.Param("procInsId")
	procInsNodeId := c.Param("procInsNodeId")
	result, err := database.GetInstanceTaskNodeBindings(c, procInsId, procInsNodeId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func ProcInsNodeRetry(c *gin.Context) {
	var param []*models.TaskNodeBindingObj
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	procInsId := c.Param("procInsId")
	if procInsId == "" {
		middleware.ReturnError(c, exterror.New().RequestParamValidateError)
		return
	}
	permissionLegal, checkPermissionErr := database.CheckProcInsUserPermission(c, middleware.GetRequestRoles(c), procInsId)
	if checkPermissionErr != nil {
		middleware.ReturnError(c, checkPermissionErr)
		return
	}
	if !permissionLegal {
		middleware.ReturnError(c, exterror.New().DataPermissionDeny)
		return
	}
	procInsNodeId := c.Param("procInsNodeId")
	procInsNodeObj, getProcInsNodeErr := database.GetSimpleProcInsNode(c, procInsNodeId, "")
	if getProcInsNodeErr != nil {
		middleware.ReturnError(c, getProcInsNodeErr)
		return
	}
	operator := middleware.GetRequestUser(c)
	err := database.UpdateProcInsNodeBindingData(c, param, procInsId, procInsNodeId, operator)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if procInsNodeObj.Status == models.JobStatusReady {
		middleware.ReturnSuccess(c)
		return
	}
	if procInsNodeObj.NodeType == models.JobSubProcType {
		killSubProc(c, procInsId, procInsNodeId, operator)
	}
	workflowId, nodeId, err := database.GetProcWorkByInsId(c, procInsId, procInsNodeId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	operationObj := models.ProcRunOperation{WorkflowId: workflowId, NodeId: nodeId, Operation: "retry", Status: "wait", CreatedBy: operator}
	operationObj.Id, err = database.AddWorkflowOperation(c, &operationObj)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	go workflow.HandleProOperation(&operationObj)
	middleware.ReturnSuccess(c)
}

func ProcEntityDataQuery(c *gin.Context) {
	packageName := c.Param("pluginPackageId")
	entityName := c.Param("entityName")
	var param models.ProcEntityDataQueryParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	result, err := remote.RequestPluginModelData(c, packageName, entityName, remote.GetToken(), param.AdditionalFilters)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func ProcInstanceCallback(c *gin.Context) {
	var param models.PluginTaskCreateResp
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	runNodeRow, err := database.GetWorkflowNodeByReq(c, param.Results.RequestId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if runNodeRow.IsCompleted {
		middleware.ReturnError(c, fmt.Errorf("req:%s already completed", param.Results.RequestId))
		return
	}
	operationObj := models.ProcRunOperation{WorkflowId: runNodeRow.WorkflowId, NodeId: runNodeRow.WorkNodeId, Operation: "approve", Status: "wait", CreatedBy: middleware.GetRequestUser(c)}
	resultBytes, _ := json.Marshal(param)
	operationObj.Message = string(resultBytes)
	operationObj.Id, err = database.AddWorkflowOperation(c, &operationObj)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	go workflow.HandleProOperation(&operationObj)
	middleware.ReturnSuccess(c)
}

func QueryProcInsPageData(c *gin.Context) {
	var param models.QueryProcPageParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	result, err := database.QueryProcInsPage(c, &param, middleware.GetRequestRoles(c))
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func ProcTermination(c *gin.Context) {
	procInsId := c.Param("procInsId")
	if procInsId == "" {
		middleware.ReturnError(c, exterror.New().RequestParamValidateError)
		return
	}
	permissionLegal, checkPermissionErr := database.CheckProcInsUserPermission(c, middleware.GetRequestRoles(c), procInsId)
	if checkPermissionErr != nil {
		middleware.ReturnError(c, checkPermissionErr)
		return
	}
	if !permissionLegal {
		middleware.ReturnError(c, exterror.New().DataPermissionDeny)
		return
	}
	procInsObj, getErr := database.GetSimpleProcInsRow(c, procInsId)
	if getErr != nil {
		middleware.ReturnError(c, getErr)
		return
	}
	if procInsObj.Status == models.JobStatusFail || procInsObj.Status == models.JobStatusSuccess {
		middleware.ReturnError(c, exterror.New().ProcStatusOperationError)
		//middleware.ReturnError(c, fmt.Errorf("procIns:%s[%s] status is %s ,can not termination", procInsObj.ProcDefName, procInsObj.Id, procInsObj.Status))
		return
	}
	workflowId, _, err := database.GetProcWorkByInsId(c, procInsId, "")
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	operationObj := models.ProcRunOperation{WorkflowId: workflowId, Operation: "kill", Status: "wait", CreatedBy: middleware.GetRequestUser(c)}
	operationObj.Id, err = database.AddWorkflowOperation(c, &operationObj)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	go workflow.HandleProOperation(&operationObj)
	killSubProc(c, procInsId, "", middleware.GetRequestUser(c))
	time.Sleep(2500 * time.Millisecond)
	middleware.ReturnSuccess(c)
}

func BatchProcTermination(c *gin.Context) {
	var procInsList []*models.ProcIns
	if err := c.ShouldBindJSON(&procInsList); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	for _, procInsParam := range procInsList {
		permissionLegal, checkPermissionErr := database.CheckProcInsUserPermission(c, middleware.GetRequestRoles(c), procInsParam.Id)
		if checkPermissionErr != nil {
			middleware.ReturnError(c, checkPermissionErr)
			return
		}
		if !permissionLegal {
			middleware.ReturnError(c, exterror.New().DataPermissionDeny)
			return
		}
		procInsObj, getErr := database.GetSimpleProcInsRow(c, procInsParam.Id)
		if getErr != nil {
			middleware.ReturnError(c, getErr)
			return
		}
		if procInsObj.Status == models.JobStatusFail || procInsObj.Status == models.JobStatusSuccess {
			middleware.ReturnError(c, exterror.New().ProcStatusOperationError)
			//middleware.ReturnError(c, fmt.Errorf("procIns:%s[%s] status is %s ,can not termination", procInsObj.ProcDefName, procInsObj.Id, procInsObj.Status))
			return
		}
	}
	for _, procInsParam := range procInsList {
		workflowId, _, err := database.GetProcWorkByInsId(c, procInsParam.Id, "")
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
		operationObj := models.ProcRunOperation{WorkflowId: workflowId, Operation: "kill", Status: "wait", CreatedBy: middleware.GetRequestUser(c)}
		operationObj.Id, err = database.AddWorkflowOperation(c, &operationObj)
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
		go workflow.HandleProOperation(&operationObj)
		killSubProc(c, procInsParam.Id, "", middleware.GetRequestUser(c))
	}
	time.Sleep(2500 * time.Millisecond)
	middleware.ReturnSuccess(c)
}

func killSubProc(ctx context.Context, mainProcInsId, procNodeId, operator string) {
	var err error
	var subWorkflowIdList []string
	subWorkflowIdList, err = database.GetRunningProcInsSubWorkflow(ctx, mainProcInsId, procNodeId)
	if err != nil {
		log.Logger.Error("Try to kill sub proc fail with query sub workflow list", log.String("mainProcIns", mainProcInsId), log.Error(err))
		return
	}
	if len(subWorkflowIdList) == 0 {
		return
	}
	for _, subWorkflowId := range subWorkflowIdList {
		subOperationObj := models.ProcRunOperation{WorkflowId: subWorkflowId, Operation: "kill", Status: "wait", CreatedBy: operator}
		subOperationObj.Id, err = database.AddWorkflowOperation(ctx, &subOperationObj)
		if err != nil {
			log.Logger.Error("Try to kill sub proc fail", log.String("mainProcIns", mainProcInsId), log.String("subWorkflowId", subWorkflowId), log.Error(err))
		} else {
			go workflow.HandleProOperation(&subOperationObj)
		}
	}
	return
}

func ProcStartEvents(c *gin.Context) {
	var param models.ProcStartEventParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	procDef, err := database.GetLatestProcDefByKey(c, param.OperationKey)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	newProcEventId, createErr := database.CreateProcInsEvent(c, &param, procDef)
	if createErr != nil {
		middleware.ReturnError(c, createErr)
	} else {
		result := models.ProcStartEventResultData{ProcInstId: fmt.Sprintf("%d", newProcEventId), Status: models.JobStatusReady}
		middleware.ReturnData(c, result)
	}
}

func GetProcNodeAllowOptions(c *gin.Context) {
	procDefId := c.Param("proc-def-id")
	procNodeDefId := c.Param("proc-node-def-id")
	options, err := database.GetProcNodeAllowOptions(c, procDefId, procNodeDefId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, options)
	}
}

func GetProcNodeEndTime(c *gin.Context) {
	procInsNodeId := c.Param("procInsNodeId")
	result, err := database.GetProcNodeEndTime(c, procInsNodeId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func GetProcNodeNextChoose(c *gin.Context) {
	procInsNodeId := c.Param("procInsNodeId")
	result, err := database.GetProcNodeNextChoose(c, procInsNodeId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func SubProcDefList(c *gin.Context) {
	var param models.SubProcDefListParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	result, err := database.ProcDefList(c, "0", "USE", "", "", "sub", middleware.GetRequestUser(c), "", middleware.GetRequestRoles(c))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if param.EntityExpr != "" {
		exprList, analyzeErr := remote.AnalyzeExpression(param.EntityExpr)
		if analyzeErr != nil {
			middleware.ReturnError(c, analyzeErr)
			return
		}
		if len(exprList) > 0 {
			lastExpr := exprList[len(exprList)-1]
			matchRootEntity := fmt.Sprintf("%s:%s", lastExpr.Package, lastExpr.Entity)
			newResultList := []*models.ProcDefListObj{}
			for _, v := range result {
				tmpRootEntity := v.RootEntity
				if filterIndex := strings.Index(v.RootEntity, "{"); filterIndex > 0 {
					tmpRootEntity = v.RootEntity[:filterIndex]
				}
				if tmpRootEntity == matchRootEntity {
					newResultList = append(newResultList, v)
				}
			}
			result = newResultList
		}
	}
	middleware.ReturnData(c, result)
}
