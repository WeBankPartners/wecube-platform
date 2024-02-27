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
	if includeDraft == "" {
		includeDraft = "0"
	}
	if permission == "" {
		permission = "USE"
	}
	log.Logger.Debug("procDefList", log.String("includeDraft", includeDraft), log.String(permission, "permission"), log.String("tag", tag))
	result, err := database.ProcDefList(c, includeDraft, permission, tag, middleware.GetRequestRoles(c))
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
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
	result := models.ProcPreviewData{ProcessSessionId: fmt.Sprintf("proc_session_" + guid.CreateGuid()), EntityTreeNodes: []*models.ProcPreviewEntityNode{}}
	log.Logger.Debug("rootData", log.String("entityDataId", entityDataId), log.JsonObj("data", rootData))
	rootEntityNode := models.ProcPreviewEntityNode{}
	rootEntityNode.Parse(rootFilter.PackageName, rootFilter.EntityName, rootData)
	rootEntityNode.FullDataId = rootEntityNode.DataId
	entityNodeMap[rootEntityNode.Id] = &rootEntityNode
	result.EntityTreeNodes = append(result.EntityTreeNodes, &rootEntityNode)
	operator := middleware.GetRequestUser(c)
	nowTime := time.Now()
	var previewRows []*models.ProcDataPreview
	rootPreviewRow := models.ProcDataPreview{
		EntityDataId:   rootEntityNode.DataId,
		EntityTypeId:   procOutlineData.RootEntity,
		ProcDefId:      procDefId,
		BindType:       "process",
		IsBound:        true,
		ProcSessionId:  result.ProcessSessionId,
		EntityDataName: rootEntityNode.DisplayName,
		FullDataId:     rootEntityNode.FullDataId,
		CreatedBy:      operator,
		CreatedTime:    nowTime,
	}
	previewRows = append(previewRows, &rootPreviewRow)
	for _, node := range procOutlineData.FlowNodes {
		if node.OrderedNo != "" && node.RoutineExpression != "" {
			if node.RoutineExpression == procOutlineData.RootEntity {
				// 表达式和根一样，不用解析，但同样要处理绑定数据
				tmpPreviewRow := models.ProcDataPreview{
					EntityDataId:   rootPreviewRow.EntityDataId,
					EntityTypeId:   fmt.Sprintf("%s:%s", rootLastExprObj.Package, rootLastExprObj.Entity),
					ProcDefId:      rootPreviewRow.ProcDefId,
					BindType:       "taskNode",
					IsBound:        true,
					ProcSessionId:  result.ProcessSessionId,
					EntityDataName: rootEntityNode.DisplayName,
					FullDataId:     rootEntityNode.FullDataId,
					ProcDefNodeId:  node.NodeId,
					OrderedNo:      node.OrderedNo,
					CreatedBy:      operator,
					CreatedTime:    nowTime,
				}
				previewRows = append(previewRows, &tmpPreviewRow)
				continue
			}
			tmpQueryDataParam := models.QueryExpressionDataParam{DataModelExpression: node.RoutineExpression, Filters: []*models.QueryExpressionDataFilter{&rootFilter}}
			nodeDataList, nodeDataErr := queryProcPreviewNodeData(c, &tmpQueryDataParam, &rootEntityNode, false)
			if nodeDataErr != nil {
				err = nodeDataErr
				break
			}
			log.Logger.Debug("nodeData", log.String("node", node.NodeId), log.JsonObj("data", nodeDataList))
			for _, nodeDataObj := range nodeDataList {
				if nodeDataObj.LastFlag {
					tmpPreviewRow := models.ProcDataPreview{
						EntityDataId:   nodeDataObj.DataId,
						EntityTypeId:   fmt.Sprintf("%s:%s", nodeDataObj.PackageName, nodeDataObj.EntityName),
						ProcDefId:      rootPreviewRow.ProcDefId,
						BindType:       "taskNode",
						IsBound:        true,
						ProcSessionId:  result.ProcessSessionId,
						EntityDataName: nodeDataObj.DisplayName,
						FullDataId:     nodeDataObj.FullDataId,
						ProcDefNodeId:  node.NodeId,
						OrderedNo:      node.OrderedNo,
						CreatedBy:      operator,
						CreatedTime:    nowTime,
					}
					previewRows = append(previewRows, &tmpPreviewRow)
				}
				if existEntityNodeObj, ok := entityNodeMap[nodeDataObj.Id]; !ok {
					entityNodeMap[nodeDataObj.Id] = nodeDataObj
					result.EntityTreeNodes = append(result.EntityTreeNodes, nodeDataObj)
				} else {
					existEntityNodeObj.PreviousIds = append(existEntityNodeObj.PreviousIds, nodeDataObj.PreviousIds...)
					existEntityNodeObj.SucceedingIds = append(existEntityNodeObj.SucceedingIds, nodeDataObj.SucceedingIds...)
				}
			}
		}
	}
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	result.AnalyzeRefIds()
	var graphRows []*models.ProcInsGraphNode
	for _, v := range result.EntityTreeNodes {
		graphRows = append(graphRows, &models.ProcInsGraphNode{
			DataId:        v.DataId,
			DisplayName:   v.DisplayName,
			EntityName:    v.EntityName,
			GraphNodeId:   v.Id,
			PkgName:       v.PackageName,
			ProcSessionId: result.ProcessSessionId,
			PrevIds:       strings.Join(v.PreviousIds, ","),
			SuccIds:       strings.Join(v.SucceedingIds, ","),
			FullDataId:    v.FullDataId,
		})
	}
	if err = database.CreateProcPreview(c, previewRows, graphRows); err != nil {
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
	//operator := middleware.GetRequestUser(c)
	//nowTime := time.Now()
	//var previewRows []*models.ProcDataPreview
	//rootPreviewRow := models.ProcDataPreview{
	//	EntityDataId:   rootEntityNode.DataId,
	//	EntityTypeId:   procOutlineData.RootEntity,
	//	ProcDefId:      procDefId,
	//	BindType:       "process",
	//	IsBound:        true,
	//	ProcSessionId:  previewResult.ProcessSessionId,
	//	EntityDataName: rootEntityNode.DisplayName,
	//	FullDataId:     rootEntityNode.FullDataId,
	//	CreatedBy:      operator,
	//	CreatedTime:    nowTime,
	//}
	//previewRows = append(previewRows, &rootPreviewRow)
	for _, node := range procOutlineData.FlowNodes {
		if node.OrderedNo != "" && node.RoutineExpression != "" {
			if node.RoutineExpression == procOutlineData.RootEntity {
				// 表达式和根一样，不用解析，但同样要处理绑定数据
				//tmpPreviewRow := models.ProcDataPreview{
				//	EntityDataId:   rootPreviewRow.EntityDataId,
				//	EntityTypeId:   fmt.Sprintf("%s:%s", rootLastExprObj.Package, rootLastExprObj.Entity),
				//	ProcDefId:      rootPreviewRow.ProcDefId,
				//	BindType:       "taskNode",
				//	IsBound:        true,
				//	ProcSessionId:  previewResult.ProcessSessionId,
				//	EntityDataName: rootEntityNode.DisplayName,
				//	FullDataId:     rootEntityNode.FullDataId,
				//	ProcDefNodeId:  node.NodeId,
				//	OrderedNo:      node.OrderedNo,
				//	CreatedBy:      operator,
				//	CreatedTime:    nowTime,
				//}
				//previewRows = append(previewRows, &tmpPreviewRow)
				continue
			}
			tmpQueryDataParam := models.QueryExpressionDataParam{DataModelExpression: node.RoutineExpression, Filters: []*models.QueryExpressionDataFilter{&rootFilter}}
			nodeDataList, nodeDataErr := queryProcPreviewNodeData(c, &tmpQueryDataParam, &rootEntityNode, true)
			if nodeDataErr != nil {
				err = nodeDataErr
				break
			}
			log.Logger.Debug("nodeData", log.String("node", node.NodeId), log.JsonObj("data", nodeDataList))
			for _, nodeDataObj := range nodeDataList {
				//if nodeDataObj.LastFlag {
				//	tmpPreviewRow := models.ProcDataPreview{
				//		EntityDataId:   nodeDataObj.DataId,
				//		EntityTypeId:   fmt.Sprintf("%s:%s", nodeDataObj.PackageName, nodeDataObj.EntityName),
				//		ProcDefId:      rootPreviewRow.ProcDefId,
				//		BindType:       "taskNode",
				//		IsBound:        true,
				//		ProcSessionId:  previewResult.ProcessSessionId,
				//		EntityDataName: nodeDataObj.DisplayName,
				//		FullDataId:     nodeDataObj.FullDataId,
				//		ProcDefNodeId:  node.NodeId,
				//		OrderedNo:      node.OrderedNo,
				//		CreatedBy:      operator,
				//		CreatedTime:    nowTime,
				//	}
				//	previewRows = append(previewRows, &tmpPreviewRow)
				//}
				if existEntityNodeObj, ok := entityNodeMap[nodeDataObj.Id]; !ok {
					entityNodeMap[nodeDataObj.Id] = nodeDataObj
					previewResult.EntityTreeNodes = append(previewResult.EntityTreeNodes, nodeDataObj)
				} else {
					existEntityNodeObj.PreviousIds = append(existEntityNodeObj.PreviousIds, nodeDataObj.PreviousIds...)
					existEntityNodeObj.SucceedingIds = append(existEntityNodeObj.SucceedingIds, nodeDataObj.SucceedingIds...)
				}
			}
		}
	}
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	previewResult.AnalyzeRefIds()
	middleware.ReturnData(c, previewResult)
	//if err = database.CreateProcPreview(c, previewRows, graphRows); err != nil {
	//	middleware.ReturnError(c, err)
	//} else {
	//	middleware.ReturnData(c, result)
	//}
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

func queryProcPreviewNodeData(ctx context.Context, param *models.QueryExpressionDataParam, rootEntityNode *models.ProcPreviewEntityNode, withEntityData bool) (dataList []*models.ProcPreviewEntityNode, err error) {
	exprList, analyzeErr := remote.AnalyzeExpression(param.DataModelExpression)
	if analyzeErr != nil {
		err = analyzeErr
		return
	}
	dataList, err = remote.QueryPluginFullData(ctx, exprList, param.Filters[0], rootEntityNode, remote.GetToken(), withEntityData)
	return
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
	// 新增 proc_ins,proc_ins_node,proc_data_binding 纪录
	procInsId, workflowRow, workNodes, workLinks, err := database.CreateProcInstance(c, &param, operator)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 初始化workflow并开始
	workObj := workflow.Workflow{ProcRunWorkflow: *workflowRow}
	workObj.Init(context.Background(), workNodes, workLinks)
	workflow.GlobalWorkflowMap.Store(workObj.Id, &workObj)
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
	result, err := database.ListProcInstance(c)
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
	result, err := database.GetProcInsNodeContext(c, procInsId, procInsNodeId)
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
	workflow.GlobalWorkflowMap.Store(workObj.Id, &workObj)
	go workObj.Start(&models.ProcOperation{CreatedBy: operator})
	// 查询 detail 返回
	detail, queryErr := database.GetProcInstance(c, procInsId)
	if queryErr != nil {
		middleware.ReturnError(c, queryErr)
	} else {
		middleware.ReturnData(c, detail)
	}
}

func ProcInsOperation(c *gin.Context) {
	var param models.ProcInsOperationParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	workflowId, nodeId, err := database.GetProcWorkByInsId(c, param.ProcInstId, param.NodeInstId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if param.Act == "skip" {
		operationObj := models.ProcRunOperation{WorkflowId: workflowId, NodeId: nodeId, Operation: "ignore", Status: "wait", CreatedBy: middleware.GetRequestUser(c)}
		operationObj.Id, err = database.AddWorkflowOperation(c, &operationObj)
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
		go workflow.HandleProOperation(&operationObj)
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
	procInsNodeId := c.Param("procInsNodeId")
	operator := middleware.GetRequestUser(c)
	err := database.UpdateProcInsNodeBindingData(c, param, procInsId, procInsNodeId, operator)
	if err != nil {
		middleware.ReturnError(c, err)
		return
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
	resultBytes, _ := json.Marshal(param.Results)
	operationObj.Message = string(resultBytes)
	operationObj.Id, err = database.AddWorkflowOperation(c, &operationObj)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	go workflow.HandleProOperation(&operationObj)
	middleware.ReturnSuccess(c)
}
