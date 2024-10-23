package execution

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/tools"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"strconv"
	"strings"
	"time"
)

/**
 * Func: BatchExecutionCallPluginService 批量执行调用插件接口
 *
 * @params ctx 上下文数据，需要是gin ctx
 * @params operator 操作人用户名
 * @params authToken 用户token
 * @params pluginInterfaceId 要调用的插件接口
 * @params entityType 输入entity的表达式
 * @params entityInstances 输入entity的数据
 * @params inputParamConstants 输入数据(常量，即用户输入的)
 * @params continueToken 是否进行高危检测(有值则跳过)
 * @params dueDate 任务超时控制
 * @params allowedOptions 任务选项
 *
 * @return 调用结果, 高危结果, 错误
 */

func WorkflowExecutionCallPluginService(ctx context.Context, param *models.ProcCallPluginServiceFuncParam) (result *models.PluginInterfaceApiResultData, dangerousCheckResult *models.ItsdangerousWorkflowCheckResultData, pluginCallParam *models.BatchExecutionPluginExecParam, err error) {
	procInsNodeReq := models.ProcInsNodeReq{
		Id:            "proc_req_" + guid.CreateGuid(),
		ProcInsNodeId: param.ProcInsNode.Id,
		ReqUrl:        fmt.Sprintf("%s%s", models.Config.Gateway.Url, param.PluginInterface.Path),
	}
	rootExprList, errAnalyze1 := remote.AnalyzeExpression(param.EntityType)
	if errAnalyze1 != nil {
		err = errAnalyze1
		return
	}
	if len(rootExprList) == 0 {
		err = fmt.Errorf("invalid input entity type %s", param.EntityType)
		return
	}
	rootExpr := rootExprList[len(rootExprList)-1]
	// 获取subsystem token
	//subsysToken := remote.GetToken()
	// 构造输入参数
	inputParamDatas, errHandle := handleInputData(ctx, remote.GetToken(), param.ContinueToken, param.EntityInstances, param.PluginInterface.InputParameters, rootExpr, param.InputConstantMap, param.InputParamContext, &procInsNodeReq)
	if errHandle != nil {
		err = errHandle
		return
	}
	procInsNodeReq.ReqDataAmount = len(inputParamDatas)
	// 调用高危插件
	if param.RiskCheck {
		itsdangerousCallParam := &models.BatchExecutionItsdangerousExecParam{
			Operator:        param.Operator,
			ServiceName:     param.PluginInterface.ServiceName,
			ServicePath:     param.PluginInterface.ServiceDisplayName,
			EntityType:      param.EntityType,
			EntityInstances: param.EntityInstances,
			InputParams:     inputParamDatas,
		}
		// 需要有运行时的高危插件
		dangerousResult, errDangerous := performWorkflowDangerousCheck(ctx, itsdangerousCallParam, param.ContinueToken, remote.GetToken())
		if errDangerous != nil {
			err = errDangerous
			return
		}
		if dangerousResult != nil {
			dangerousCheckResult = dangerousResult
			return
		}
	}
	// 调用插件接口
	pluginCallParam = &models.BatchExecutionPluginExecParam{
		RequestId:       procInsNodeReq.Id,
		Operator:        param.Operator,
		ServiceName:     param.PluginInterface.ServiceName,
		ServicePath:     param.PluginInterface.ServiceDisplayName,
		EntityInstances: param.EntityInstances,
		Inputs:          inputParamDatas,
		DueDate:         param.DueDate,
		AllowedOptions:  param.AllowedOptions,
	}
	// 纪录参数
	if err = database.RecordProcCallReq(ctx, &procInsNodeReq, true); err != nil {
		return
	}
	pluginCallResult, errCode, errCall := remote.PluginInterfaceApi(ctx, remote.GetToken(), param.PluginInterface, pluginCallParam)
	if errCall != nil {
		if errCode != "" && errCode != "0" {
			if pluginCallResult != nil && len(pluginCallResult.Outputs) > 0 {
				_, errHandle = handleOutputData(ctx, remote.GetToken(), pluginCallResult.Outputs, param.PluginInterface.OutputParameters, &procInsNodeReq, true)
				if errHandle != nil {
					log.Logger.Error("handle error output data fail", log.Error(errHandle))
				}
			}
		}
		err = errCall
		procInsNodeReq.ErrorMsg = err.Error()
		database.RecordProcCallReq(ctx, &procInsNodeReq, false)
		return
	}
	// 处理output param(比如类型转换，数据模型写入), handleOutputData主要是用于格式化为output param定义的字段
	_, errHandle = handleOutputData(ctx, remote.GetToken(), pluginCallResult.Outputs, param.PluginInterface.OutputParameters, &procInsNodeReq, false)
	if errHandle != nil {
		err = errHandle
		return
	}
	// 记录调用 taskman 插件请求信息
	RecordPluginRequestInfo(ctx, param.ProcIns, pluginCallResult.Outputs)
	if err = database.RecordProcCallReq(ctx, &procInsNodeReq, false); err != nil {
		return
	}
	// 批量执行需要返回原始插件结果，而不是格式化output字段的值
	result = pluginCallResult
	return
}

// RecordPluginRequestInfo 记录调用插件请求信息,taskman插件创建请求调用生成的请求和模版信息,需要在 proc_ins表中记录下来详情展示用(此处没有考虑并发创建taskman请求)
func RecordPluginRequestInfo(ctx context.Context, procIns *models.ProcIns, outputs []map[string]interface{}) {
	if len(outputs) > 0 && procIns != nil {
		outputMap := outputs[0]
		var requestId, requestName, requestTemplate string
		var requestTemplateType int
		var err error
		if v, ok := outputMap["requestId"]; ok {
			requestId = fmt.Sprintf("%v", v)
		}
		if v, ok := outputMap["requestName"]; ok {
			requestName = fmt.Sprintf("%v", v)
		}
		if v, ok := outputMap["requestTemplate"]; ok {
			requestTemplate = fmt.Sprintf("%v", v)
		}
		if v, ok := outputMap["requestTemplateType"]; ok {
			requestTemplateType, _ = strconv.Atoi(fmt.Sprintf("%v", v))
		}
		if requestId != "" && requestTemplate != "" {
			var requestList []*models.SimpleRequestDto
			var newRequestInfo []byte
			if procIns.RequestInfo != "" {
				if err = json.Unmarshal([]byte(procIns.RequestInfo), &requestList); err != nil {
					log.Logger.Error("json Unmarshal requestInfo err", log.Error(err))
				}
			}
			requestList = append(requestList, &models.SimpleRequestDto{
				Id:              requestId,
				Name:            requestName,
				RequestTemplate: requestTemplate,
				Type:            requestTemplateType,
			})
			newRequestInfo, _ = json.Marshal(requestList)
			if err = database.UpdateProcInstanceRequestInfo(ctx, procIns.Id, string(newRequestInfo)); err != nil {
				log.Logger.Error("UpdateProcInstanceRequestInfo err", log.Error(err))
			}
		}
	}
}

func performWorkflowDangerousCheck(ctx context.Context, pluginCallParam interface{}, continueToken string, authToken string) (result *models.ItsdangerousWorkflowCheckResultData, err error) {
	if continueToken != "" {
		return
	}
	// 是否有运行时的高危插件，否则直接返回(跳过检查)
	if instances, errQuery := database.GetPluginRunningInstancesByName(ctx, models.PluginNameItsdangerous); errQuery != nil {
		err = errQuery
		return
	} else if len(instances) == 0 {
		return
	}
	// 调用检查
	result, err = remote.DangerousWorkflowCheck(ctx, authToken, pluginCallParam)
	return
}

func DoWorkflowAutoJob(ctx context.Context, procRunNodeId, continueToken string) (err error) {
	ctx = context.WithValue(ctx, models.TransactionIdHeader, procRunNodeId)
	// 查proc def node定义和proc ins绑定数据
	procInsNode, procDefNode, procDefNodeParams, dataBindings, getNodeDataErr := database.GetProcExecNodeData(ctx, procRunNodeId)
	if getNodeDataErr != nil {
		err = getNodeDataErr
		return
	}
	if procDefNode.DynamicBind == 1 {
		dataBindings, err = database.GetDynamicBindNodeData(ctx, procInsNode.ProcInsId, procDefNode.ProcDefId, procDefNode.BindNodeId)
		if err != nil {
			err = fmt.Errorf("get node dynamic bind data fail,%s ", err.Error())
			return
		}
		if len(dataBindings) > 0 {
			err = database.UpdateDynamicNodeBindData(ctx, procInsNode.ProcInsId, procInsNode.Id, procDefNode.ProcDefId, procDefNode.Id, dataBindings)
			if err != nil {
				err = fmt.Errorf("try to update dynamic node binding data fail,%s ", err.Error())
				return
			}
		}
	} else if procDefNode.DynamicBind == 2 {
		dataBindings, err = DynamicBindNodeInRuntime(ctx, procInsNode, procDefNode)
		if err != nil {
			err = fmt.Errorf("get runtime dynamic bind data fail,%s ", err.Error())
			return
		}
		if len(dataBindings) > 0 {
			err = database.UpdateDynamicNodeBindData(ctx, procInsNode.ProcInsId, procInsNode.Id, procDefNode.ProcDefId, procDefNode.Id, dataBindings)
			if err != nil {
				err = fmt.Errorf("try to update runtime dynamic node binding data fail,%s ", err.Error())
				return
			}
		}
	}
	if len(dataBindings) == 0 {
		log.Logger.Warn("auto job return with empty binding data", log.String("procIns", procInsNode.ProcInsId), log.String("procInsNode", procInsNode.Id))
		// 无数据，空跑
		return
	}
	pluginInterface, getIntErr := database.GetLastEnablePluginInterface(ctx, procDefNode.ServiceName)
	if getIntErr != nil {
		err = getIntErr
		return
	}
	procIns, getProcInsErr := database.GetSimpleProcInsRow(ctx, procInsNode.ProcInsId)
	if getProcInsErr != nil {
		err = getProcInsErr
		return
	}
	if err = database.AddProcCacheData(ctx, procInsNode.ProcInsId, dataBindings); err != nil {
		return
	}
	var entityInstances []*models.BatchExecutionPluginExecEntityInstances
	for _, bindingObj := range dataBindings {
		entityInstances = append(entityInstances, &models.BatchExecutionPluginExecEntityInstances{
			Id:               bindingObj.EntityDataId,
			BusinessKeyValue: "",
			ContextMap:       make(map[string]interface{}),
		})
	}
	inputConstantMap := make(map[string]string)
	inputContextMap := make(map[string]interface{})
	interfaceParamIdMap := make(map[string]string)
	for _, v := range pluginInterface.InputParameters {
		interfaceParamIdMap[v.Name] = v.Id
	}
	for _, v := range pluginInterface.OutputParameters {
		interfaceParamIdMap[v.Name] = v.Id
	}
	for _, v := range procDefNodeParams {
		if v.BindType == "constant" {
			inputConstantMap[interfaceParamIdMap[v.Name]] = v.Value
		} else if v.BindType == "context" {
			bindNodeDef, getTypeErr := database.GetProcContextBindNodeType(ctx, procDefNode.ProcDefId, v.CtxBindNode)
			if getTypeErr != nil {
				err = getTypeErr
				return
			}
			if bindNodeDef.NodeType == models.JobStartType {
				buildStartNodeContextMap(inputContextMap, procIns, v)
			} else if bindNodeDef.NodeType == models.JobAutoType || bindNodeDef.NodeType == models.JobHumanType {
				if err = buildAutoNodeContextMap(ctx, entityInstances, procIns, bindNodeDef, dataBindings, v.CtxBindType, v.CtxBindName, v.Name); err != nil {
					err = fmt.Errorf("buildAutoNodeContextMap fail with param:%s,error:%s", v.Name, err.Error())
					return
				}
			}
		}
	}
	log.Logger.Debug("DoWorkflowAutoJob data", log.String("procInsNode", procInsNode.Id), log.String("procDefNode", procDefNode.Id), log.String("interfaceId", pluginInterface.Id), log.JsonObj("inputConstantMap", inputConstantMap), log.JsonObj("inputContextMap", inputContextMap))
	callPluginServiceParam := models.ProcCallPluginServiceFuncParam{
		PluginInterface:   pluginInterface,
		EntityType:        procDefNode.RoutineExpression,
		EntityInstances:   entityInstances,
		InputConstantMap:  inputConstantMap,
		InputParamContext: inputContextMap,
		ContinueToken:     continueToken,
		RiskCheck:         procDefNode.RiskCheck,
		Operator:          "SYSTEM",
		ProcInsNode:       procInsNode,
		ProcIns:           procIns,
	}
	callOutput, dangerousCheckResult, pluginCallParam, callErr := WorkflowExecutionCallPluginService(ctx, &callPluginServiceParam)
	if callErr != nil {
		err = callErr
		return
	}
	if dangerousCheckResult != nil {
		dangerousCheckResultBytes, _ := json.Marshal(dangerousCheckResult)
		database.UpdateProcInsNodeData(ctx, procInsNode.Id, "", "", string(dangerousCheckResultBytes))
	}
	log.Logger.Debug("WorkflowExecutionCallPluginService", log.JsonObj("output", callOutput), log.JsonObj("pluginCallParam", pluginCallParam))
	return
}

func DoWorkflowDataJob(ctx context.Context, procRunNodeId string) (err error) {
	ctx = context.WithValue(ctx, models.TransactionIdHeader, procRunNodeId)
	// 查proc def node定义和proc ins绑定数据
	procInsNode, procDefNode, _, dataBindings, getNodeDataErr := database.GetProcExecNodeData(ctx, procRunNodeId)
	if getNodeDataErr != nil {
		err = getNodeDataErr
		return
	}
	if len(dataBindings) == 0 {
		return
	}
	exprObjList, parseErr := database.GetProcDataNodeExpression(procDefNode.RoutineExpression)
	if parseErr != nil {
		err = parseErr
		return
	}
	if len(exprObjList) == 0 {
		return
	}
	cacheDataList, getCacheErr := database.GetProcCacheData(ctx, procInsNode.ProcInsId)
	if getCacheErr != nil {
		err = getCacheErr
		return
	}
	var createEntityIdList, updateEntityIdList []string
	for _, exprObj := range exprObjList {
		exprAnalyzeList, analyzeErr := remote.AnalyzeExpression(exprObj.Expression)
		if analyzeErr != nil {
			err = analyzeErr
			return
		}
		lastExprEntity := exprAnalyzeList[len(exprAnalyzeList)-1]
		lastEntityType := fmt.Sprintf("%s:%s", lastExprEntity.Package, lastExprEntity.Entity)
		createEntityIdList, updateEntityIdList = []string{}, []string{}
		for _, dataBindObj := range dataBindings {
			if dataBindObj.EntityTypeId == lastEntityType {
				if strings.HasPrefix(dataBindObj.EntityId, models.NewOidDataPrefix) {
					createEntityIdList = append(createEntityIdList, dataBindObj.EntityId)
				} else {
					updateEntityIdList = append(updateEntityIdList, dataBindObj.EntityId)
				}
			}
		}
		if len(createEntityIdList) > 0 {
			for len(createEntityIdList) > 0 {
				tmpDataOid, tmpDataId, newIdList, createDataObj, tmpErr := findDataWriteObj(cacheDataList, createEntityIdList)
				if tmpErr != nil {
					err = tmpErr
					break
				}
				createEntityIdList = newIdList
				createDataResult, createDataErr := remote.CreatePluginModelData(ctx, lastExprEntity.Package, lastExprEntity.Entity, remote.GetToken(), exprObj.Operation, []map[string]interface{}{createDataObj})
				if createDataErr != nil {
					err = fmt.Errorf("try to create plugin model data %s:%s %s fail,%s", lastExprEntity.Package, lastExprEntity.Entity, tmpDataOid, createDataErr.Error())
					return
				}
				if len(createDataResult) > 0 {
					rewriteObj := models.RewriteEntityDataObj{Oid: tmpDataOid, Nid: fmt.Sprintf("%s", createDataResult[0]["id"]), DisplayName: fmt.Sprintf("%s", createDataResult[0]["displayName"])}
					for _, tmpCacheData := range cacheDataList {
						if strings.Contains(tmpCacheData.DataValue, tmpDataId) {
							tmpCacheData.DataValue = strings.ReplaceAll(tmpCacheData.DataValue, tmpDataId, rewriteObj.Nid)
							rewriteObj.ProcDataCacheList = append(rewriteObj.ProcDataCacheList, tmpCacheData)
						}
					}
					if err = database.RewriteProcInsEntityDataNew(ctx, procInsNode.ProcInsId, &rewriteObj); err != nil {
						err = fmt.Errorf("try to rewrite new entity data %s to procIns:%s fail,%s ", rewriteObj.Oid, procInsNode.ProcInsId, err.Error())
						return
					}
				}
			}
			if err != nil {
				return
			}
		}
		if len(updateEntityIdList) > 0 {
			_, err = remote.UpdatePluginModelData(ctx, lastExprEntity.Package, lastExprEntity.Entity, remote.GetToken(), exprObj.Operation, buildDataWriteObj(cacheDataList, updateEntityIdList))
			if err != nil {
				err = fmt.Errorf("try to update plugin model data %s:%s fail,%s", lastExprEntity.Package, lastExprEntity.Entity, err.Error())
				return
			}
		}
	}
	return
}

func buildDataWriteObj(cacheDataList []*models.ProcDataCache, ids []string) (dataList []map[string]interface{}) {
	for _, id := range ids {
		matchDataValue := ""
		for _, cacheData := range cacheDataList {
			if cacheData.EntityDataId == id {
				matchDataValue = cacheData.DataValue
				break
			}
		}
		if matchDataValue == "" {
			matchDataValue = "{}"
		}
		tmpDataObj := make(map[string]interface{})
		if tmpErr := json.Unmarshal([]byte(matchDataValue), &tmpDataObj); tmpErr != nil {
			log.Logger.Error("buildDataWriteObj json unmarshal data fail", log.String("matchDataValue", matchDataValue), log.Error(tmpErr))
		}
		tmpDataObj["id"] = id
		dataList = append(dataList, tmpDataObj)
	}
	return
}

func findDataWriteObj(cacheDataList []*models.ProcDataCache, ids []string) (oid, tmpId string, newIds []string, dataObj map[string]interface{}, err error) {
	var tmpIds []string
	for _, dataId := range ids {
		if strings.HasPrefix(dataId, models.NewOidDataPrefix) {
			tmpIds = append(tmpIds, dataId[4:])
		} else {
			tmpIds = append(tmpIds, dataId)
		}
	}
	for _, dataId := range ids {
		for _, cacheData := range cacheDataList {
			if cacheData.EntityDataId == dataId {
				refId := ""
				for _, targetId := range tmpIds {
					if strings.Contains(cacheData.DataValue, targetId) {
						refId = targetId
						break
					}
				}
				if refId == "" {
					oid = dataId
					if cacheData.DataValue == "" {
						cacheData.DataValue = "{}"
					}
					dataObj = make(map[string]interface{})
					if tmpErr := json.Unmarshal([]byte(cacheData.DataValue), &dataObj); tmpErr != nil {
						err = fmt.Errorf("buildDataWriteObj json unmarshal data fail,cacheDataValue:%s err:%s ", cacheData.DataValue, tmpErr.Error())
						return
					}
					dataObj["id"] = oid
				}
				break
			}
		}
		if oid != "" {
			break
		}
	}
	if oid != "" {
		for _, dataId := range ids {
			if dataId == oid {
				continue
			}
			newIds = append(newIds, dataId)
		}
		if strings.HasPrefix(oid, models.NewOidDataPrefix) {
			tmpId = oid[4:]
		} else {
			tmpId = oid
		}
	} else {
		err = fmt.Errorf("can not find write data job legal data to write")
	}
	return
}

func DoWorkflowHumanJob(ctx context.Context, procRunNodeId string, recoverFlag bool) (err error) {
	ctx = context.WithValue(ctx, models.TransactionIdHeader, procRunNodeId)
	if recoverFlag {
		existReq, getReqErr := database.GetSimpleProcNodeReq(ctx, "", "", procRunNodeId)
		if getReqErr != nil {
			err = getReqErr
			return
		}
		if existReq != nil && !existReq.IsCompleted && existReq.ErrorMsg == "" {
			// 人工任务请求已经发出，不用再发
			return
		}
	}
	// 查proc def node定义和proc ins绑定数据
	procInsNode, procDefNode, procDefNodeParams, dataBindings, getNodeDataErr := database.GetProcExecNodeData(ctx, procRunNodeId)
	if getNodeDataErr != nil {
		err = getNodeDataErr
		return
	}
	if procDefNode.DynamicBind == 1 {
		dataBindings, err = database.GetDynamicBindNodeData(ctx, procInsNode.ProcInsId, procDefNode.ProcDefId, procDefNode.BindNodeId)
		if err != nil {
			err = fmt.Errorf("get dynamic bind data fail,%s ", err.Error())
			return
		}
		if len(dataBindings) > 0 {
			err = database.UpdateDynamicNodeBindData(ctx, procInsNode.ProcInsId, procInsNode.Id, procDefNode.ProcDefId, procDefNode.Id, dataBindings)
			if err != nil {
				err = fmt.Errorf("try to update dynamic node binding data fail,%s ", err.Error())
				return
			}
		}
	} else if procDefNode.DynamicBind == 2 {
		dataBindings, err = DynamicBindNodeInRuntime(ctx, procInsNode, procDefNode)
		if err != nil {
			err = fmt.Errorf("get runtime dynamic bind data fail,%s ", err.Error())
			return
		}
		if len(dataBindings) > 0 {
			err = database.UpdateDynamicNodeBindData(ctx, procInsNode.ProcInsId, procInsNode.Id, procDefNode.ProcDefId, procDefNode.Id, dataBindings)
			if err != nil {
				err = fmt.Errorf("try to update runtime dynamic node binding data fail,%s ", err.Error())
				return
			}
			err = database.UpdateProcCacheData(ctx, procInsNode.ProcInsId, dataBindings)
			if err != nil {
				err = fmt.Errorf("try to udpate runtime proc data cache fail,%s ", err.Error())
				return
			}
		}
	}
	pluginInterface, getIntErr := database.GetLastEnablePluginInterface(ctx, procDefNode.ServiceName)
	if getIntErr != nil {
		err = getIntErr
		return
	}
	if pluginInterface.IsAsyncProcessing == "N" {
		err = fmt.Errorf("can not support human job with interface sync defind")
		return
	}
	procIns, getProcInsErr := database.GetSimpleProcInsRow(ctx, procInsNode.ProcInsId)
	if getProcInsErr != nil {
		err = getProcInsErr
		return
	}
	entityInstances := []*models.BatchExecutionPluginExecEntityInstances{{Id: procIns.EntityDataId}}
	inputConstantMap := make(map[string]string)
	inputContextMap := make(map[string]interface{})
	interfaceParamIdMap := make(map[string]string)
	for _, v := range pluginInterface.InputParameters {
		interfaceParamIdMap[v.Name] = v.Id
	}
	for _, v := range pluginInterface.OutputParameters {
		interfaceParamIdMap[v.Name] = v.Id
	}
	for _, v := range procDefNodeParams {
		if v.BindType == "constant" {
			inputConstantMap[interfaceParamIdMap[v.Name]] = v.Value
		} else if v.BindType == "context" {
			bindNodeDef, getTypeErr := database.GetProcContextBindNodeType(ctx, procDefNode.ProcDefId, v.CtxBindNode)
			if getTypeErr != nil {
				err = getTypeErr
				return
			}
			if bindNodeDef.NodeType == models.JobStartType {
				buildStartNodeContextMap(inputContextMap, procIns, v)
			} else if bindNodeDef.NodeType == models.JobAutoType || bindNodeDef.NodeType == models.JobHumanType {
				if err = buildAutoNodeContextMap(ctx, entityInstances, procIns, bindNodeDef, dataBindings, v.CtxBindType, v.CtxBindName, v.Name); err != nil {
					err = fmt.Errorf("buildAutoNodeContextMap fail with param:%s,error:%s", v.Name, err.Error())
					return
				}
			}
		}
	}
	callPluginServiceParam := models.ProcCallPluginServiceFuncParam{
		PluginInterface:   pluginInterface,
		EntityType:        procDefNode.RoutineExpression,
		EntityInstances:   entityInstances,
		InputConstantMap:  inputConstantMap,
		InputParamContext: inputContextMap,
		RiskCheck:         procDefNode.RiskCheck,
		Operator:          "SYSTEM",
		ProcInsNode:       procInsNode,
		ProcDefNode:       procDefNode,
		DataBinding:       dataBindings,
		ProcIns:           procIns,
	}
	callPluginServiceParam.AllowedOptions, err = database.GetProcNodeAllowOptions(ctx, procDefNode.ProcDefId, procDefNode.Id)
	if err != nil {
		return
	}
	if pluginInterface.Type == "DYNAMICFORM" {
		err = CallDynamicFormReq(ctx, &callPluginServiceParam)
	} else if pluginInterface.Type == "APPROVAL" {
		err = CallDynamicFormReq(ctx, &callPluginServiceParam)
	}
	return
}

func CallDynamicFormReq(ctx context.Context, param *models.ProcCallPluginServiceFuncParam) (err error) {
	procInsNodeReq := models.ProcInsNodeReq{
		Id:            "proc_req_" + guid.CreateGuid(),
		ProcInsNodeId: param.ProcInsNode.Id,
		ReqUrl:        fmt.Sprintf("%s%s", models.Config.Gateway.Url, param.PluginInterface.Path),
	}
	rootExprList, errAnalyze1 := remote.AnalyzeExpression(param.EntityType)
	if errAnalyze1 != nil {
		err = errAnalyze1
		return
	}
	if len(rootExprList) == 0 {
		err = fmt.Errorf("invalid input entity type %s", param.EntityType)
		return
	}
	rootExpr := rootExprList[len(rootExprList)-1]
	// 获取subsystem token
	//subsysToken := remote.GetToken()
	// 构造输入参数
	for _, paramObj := range param.PluginInterface.InputParameters {
		if paramObj.Name == "taskFormInput" {
			// 请求taskman拿表单结构
			taskFormMeta, getFormMetaErr := remote.GetInputFormMeta(ctx, param.ProcInsNode.ProcInsId, param.ProcDefNode.NodeId, param.PluginInterface)
			if getFormMetaErr != nil {
				err = getFormMetaErr
				break
			}
			// 拿到结构后组表单数据赋值给taskFormInput
			tmpFormData, buildFormErr := buildTaskFormInput(ctx, taskFormMeta, param)
			if buildFormErr != nil {
				err = buildFormErr
				break
			}
			tmpFormBytes, _ := json.Marshal(tmpFormData)
			param.InputConstantMap[paramObj.Id] = string(tmpFormBytes)
		}
	}
	if err != nil {
		return
	}
	inputParamDatas, errHandle := handleInputData(ctx, remote.GetToken(), param.ContinueToken, param.EntityInstances, param.PluginInterface.InputParameters, rootExpr, param.InputConstantMap, param.InputParamContext, &procInsNodeReq)
	if errHandle != nil {
		err = errHandle
		return
	}
	procInsNodeReq.ReqDataAmount = len(inputParamDatas)
	// 调用插件接口
	pluginCallParam := &models.BatchExecutionPluginExecParam{
		RequestId:       procInsNodeReq.Id,
		Operator:        param.Operator,
		ServiceName:     param.PluginInterface.ServiceName,
		ServicePath:     param.PluginInterface.ServiceDisplayName,
		EntityInstances: param.EntityInstances,
		Inputs:          inputParamDatas,
		DueDate:         param.DueDate,
		AllowedOptions:  param.AllowedOptions,
	}
	// 纪录参数
	if err = database.RecordProcCallReq(ctx, &procInsNodeReq, true); err != nil {
		return
	}
	pluginCallResult, _, errCall := remote.PluginInterfaceApi(ctx, remote.GetToken(), param.PluginInterface, pluginCallParam)
	log.Logger.Info("human job call plugin api response", log.JsonObj("result", pluginCallResult), log.String("error", fmt.Sprintf("%v", errCall)))
	if errCall != nil {
		err = errCall
		procInsNodeReq.ErrorMsg = err.Error()
		if recordErr := database.RecordProcCallReq(ctx, &procInsNodeReq, false); recordErr != nil {
			log.Logger.Error("try to record proc call req to database fail", log.Error(recordErr), log.JsonObj("req", procInsNodeReq))
		}
		return
	}
	// 处理output param(比如类型转换，数据模型写入), handleOutputData主要是用于格式化为output param定义的字段
	//_, errHandle = handleOutputData(ctx, subsysToken, pluginCallResult.Outputs, param.PluginInterface.OutputParameters, &procInsNodeReq)
	//if errHandle != nil {
	//	err = errHandle
	//	return
	//}
	//if err = database.RecordProcCallReq(ctx, &procInsNodeReq, false); err != nil {
	//	return
	//}
	return
}

func buildTaskFormInput(ctx context.Context, taskFormMeta *models.TaskMetaResultData, param *models.ProcCallPluginServiceFuncParam) (formData *models.PluginTaskFormDto, err error) {
	cacheDataRows, getCacheErr := database.GetProcCacheData(ctx, param.ProcInsNode.ProcInsId)
	if getCacheErr != nil {
		err = getCacheErr
		return
	}
	formData = &models.PluginTaskFormDto{
		FormMetaId:     taskFormMeta.FormMetaId,
		ProcDefId:      param.ProcIns.ProcDefId,
		ProcDefKey:     param.ProcIns.ProcDefKey,
		ProcInstId:     param.ProcInsNode.ProcInsId,
		ProcInstKey:    param.ProcInsNode.ProcInsId,
		TaskNodeDefId:  param.ProcDefNode.NodeId,
		TaskNodeInstId: param.ProcInsNode.Id,
	}
	for _, dataBind := range param.DataBinding {
		entityMsg := strings.Split(dataBind.EntityTypeId, ":")
		if len(entityMsg) != 2 {
			log.Logger.Warn("bind data entity type illegal", log.String("entityTypeId", dataBind.EntityTypeId), log.String("procInsId", param.ProcInsNode.ProcInsId))
			continue
		}
		entityDataObj := models.PluginTaskFormEntity{
			FormMetaId:       formData.FormMetaId,
			PackageName:      entityMsg[0],
			EntityName:       entityMsg[1],
			Oid:              dataBind.EntityId,
			EntityDataId:     dataBind.EntityDataId,
			FullEntityDataId: dataBind.FullDataId,
			BindFlag:         "Y",
		}
		entityDataObj.FormItemValues = getTaskFormItemValues(ctx, taskFormMeta, cacheDataRows, &entityDataObj, dataBind.EntityTypeId)
		formData.FormDataEntities = append(formData.FormDataEntities, &entityDataObj)
	}
	return
}

func getTaskFormItemValues(ctx context.Context, taskFormMeta *models.TaskMetaResultData, cacheRows []*models.ProcDataCache, entityDataObj *models.PluginTaskFormEntity, entityTypeId string) (itemValues []*models.PluginTaskFormValue) {
	cacheObj := &models.ProcDataCache{}
	for _, row := range cacheRows {
		if row.EntityTypeId == entityTypeId && row.EntityDataId == entityDataObj.EntityDataId {
			cacheObj = row
			break
		}
	}
	if cacheObj.EntityDataId == "" {
		return
	}
	dataValueMap := make(map[string]interface{})
	if cacheObj.DataValue != "" {
		if err := json.Unmarshal([]byte(cacheObj.DataValue), &dataValueMap); err != nil {
			log.Logger.Error("getTaskFormItemValues,json unmarshal cache data value fail", log.String("entityTypeId", entityTypeId), log.String("entityDataId", entityDataObj.EntityDataId), log.String("dataValue", cacheObj.DataValue), log.Error(err))
			return
		}
	} else {
		queryFilter := []*models.EntityQueryObj{{AttrName: "id", Op: "eq", Condition: entityDataObj.EntityDataId}}
		queryResult, queryErr := remote.RequestPluginModelData(ctx, entityDataObj.PackageName, entityDataObj.EntityName, remote.GetToken(), queryFilter)
		if queryErr != nil {
			log.Logger.Error("getTaskFormItemValues,query entity data fail", log.String("entityTypeId", entityTypeId), log.String("entityDataId", entityDataObj.EntityDataId), log.Error(queryErr))
			return
		}
		if len(queryResult) != 1 {
			log.Logger.Warn("getTaskFormItemValues,query entity data num illegal", log.Int("dataNum", len(queryResult)), log.String("entityTypeId", entityTypeId), log.String("entityDataId", entityDataObj.EntityDataId), log.JsonObj("filter", queryFilter), log.JsonObj("result", queryResult))
			return
		}
		dataValueMap = queryResult[0]
	}
	for _, item := range taskFormMeta.FormItemMetas {
		if item.EntityName != entityDataObj.EntityName {
			continue
		}
		itemValueObj := models.PluginTaskFormValue{
			FormItemMetaId: item.FormItemMetaId,
			AttrName:       item.AttrName,
			AttrValue:      dataValueMap[item.AttrName],
			PackageName:    entityDataObj.PackageName,
			EntityName:     entityDataObj.EntityName,
			Oid:            entityDataObj.Oid,
			EntityDataId:   entityDataObj.EntityDataId,
		}
		itemValues = append(itemValues, &itemValueObj)
	}
	return
}

func HandleCallbackHumanJob(ctx context.Context, procRunNodeId string, callbackData *models.PluginTaskCreateResp) (choseOption string, err error) {
	choseOption = callbackData.ResultCode
	// 纪录req output
	procInsNode, getInsNodeErr := database.GetSimpleProcInsNode(ctx, "", procRunNodeId)
	if getInsNodeErr != nil {
		err = getInsNodeErr
		return
	}
	procDefNode, getDefNodeErr := database.GetSimpleProcDefNode(ctx, procInsNode.ProcDefNodeId)
	if getDefNodeErr != nil {
		err = getDefNodeErr
		return
	}
	pluginInterface, getIntErr := database.GetLastEnablePluginInterface(ctx, procDefNode.ServiceName)
	if getIntErr != nil {
		err = getIntErr
		return
	}
	pluginCallOutput := []map[string]interface{}{}
	outputBytes, _ := json.Marshal(callbackData.Results.Outputs)
	if err = json.Unmarshal(outputBytes, &pluginCallOutput); err != nil {
		err = fmt.Errorf("json unmarshal call output data to []map[string]interface{} fail,%s", err.Error())
		return
	}
	procInsNodeReq := models.ProcInsNodeReq{Id: callbackData.Results.RequestId}
	// 处理output param(比如类型转换，数据模型写入), handleOutputData主要是用于格式化为output param定义的字段
	_, errHandle := handleOutputData(ctx, remote.GetToken(), pluginCallOutput, pluginInterface.OutputParameters, &procInsNodeReq, false)
	if errHandle != nil {
		err = errHandle
		return
	}
	if err = database.RecordProcCallReq(ctx, &procInsNodeReq, false); err != nil {
		return
	}
	// 更新cache data
	var taskFormList []*models.PluginTaskFormDto
	for _, output := range callbackData.Results.Outputs {
		if output.ErrorCode == "0" {
			tmpTaskFormObj := models.PluginTaskFormDto{}
			if tmpUnmarshalErr := json.Unmarshal([]byte(output.TaskFormOutput), &tmpTaskFormObj); tmpUnmarshalErr != nil {
				log.Logger.Error("human job callback output json unmarshal taskFormOutput fail", log.Error(tmpUnmarshalErr), log.String("reqId", callbackData.Results.RequestId), log.JsonObj("outputData", output))
			} else {
				taskFormList = append(taskFormList, &tmpTaskFormObj)
			}
		} else {
			log.Logger.Warn("human job callback output fail", log.String("reqId", callbackData.Results.RequestId), log.JsonObj("outputData", output))
		}
	}
	if len(taskFormList) > 0 {
		err = database.UpdateProcCacheDataByTaskForm(ctx, procInsNode.ProcInsId, taskFormList)
	}
	return
}

func buildStartNodeContextMap(inputContextMap map[string]interface{}, procIns *models.ProcIns, procDefNodeParam *models.ProcDefNodeParam) {
	inputContextMap["procInstId"] = procIns.Id
	inputContextMap["procDefName"] = procIns.ProcDefName
	inputContextMap["procDefKey"] = procIns.ProcDefKey
	inputContextMap["procInstKey"] = procIns.ProcDefName
	inputContextMap["procInstName"] = procIns.ProcDefName
	inputContextMap["rootEntityId"] = procIns.EntityDataId
	inputContextMap["rootEntityName"] = procIns.EntityDataName
	if procDefNodeParam.CtxBindName != procDefNodeParam.Name {
		if v, ok := inputContextMap[procDefNodeParam.CtxBindName]; ok {
			inputContextMap[procDefNodeParam.Name] = v
		}
	}
}

func buildAutoNodeContextMap(ctx context.Context,
	entityInstances []*models.BatchExecutionPluginExecEntityInstances,
	procIns *models.ProcIns,
	bindNodeDef *models.ProcDefNode,
	targetNodeDataBindings []*models.ProcDataBinding,
	bindParamType, bindParamName, paramName string) (err error) {
	log.Logger.Debug("buildAutoNodeContextMap start", log.JsonObj("entityInstances", entityInstances), log.JsonObj("bindNodeDef", bindNodeDef), log.JsonObj("targetNodeDataBindings", targetNodeDataBindings), log.String("bindParamType", bindParamType), log.String("bindParamName", bindParamName), log.String("paramName", paramName))
	var sourceNodeDataBindings []*models.ProcDataBinding
	//if bindNodeDef.DynamicBind {
	//	sourceNodeDataBindings, err = database.GetDynamicBindNodeData(ctx, procIns.Id, procIns.ProcDefId, bindNodeDef.BindNodeId)
	//} else {
	//	sourceNodeDataBindings, err = database.GetDynamicBindNodeData(ctx, procIns.Id, procIns.ProcDefId, bindNodeDef.NodeId)
	//}
	// 所有已跑过的节点都会有绑定数据，包括动态绑定节点
	sourceNodeDataBindings, err = database.GetDynamicBindNodeData(ctx, procIns.Id, procIns.ProcDefId, bindNodeDef.NodeId)
	if err != nil {
		return
	}
	if len(sourceNodeDataBindings) == 0 {
		return
	}
	// 找两份dataBinding的关系，通过判断目标的full_data_id里有没有源的entity_data_id来确定目标是不是源关联出来的数据
	entityDataMap := make(map[string][]*models.ProcDataBinding) // key -> 目标的entityDataId  value -> 源的绑定数据
	for _, targetData := range targetNodeDataBindings {
		fullDataList := strings.Split(targetData.FullDataId, "::")
		for _, sourceData := range sourceNodeDataBindings {
			if tools.StringListContains(fullDataList, sourceData.EntityDataId) {
				if existSourceData, ok := entityDataMap[targetData.EntityDataId]; ok {
					entityDataMap[targetData.EntityDataId] = append(existSourceData, sourceData)
				} else {
					entityDataMap[targetData.EntityDataId] = []*models.ProcDataBinding{sourceData}
				}
			}
		}
	}
	if len(entityDataMap) == 0 {
		return
	}
	sourceReq, getSourceReqErr := database.GetProcInsNodeContext(ctx, procIns.Id, "", bindNodeDef.Id)
	if getSourceReqErr != nil {
		err = getSourceReqErr
		return
	}
	for _, entityInstance := range entityInstances {
		if sourceBindList, ok := entityDataMap[entityInstance.Id]; ok {
			var sourceBindValues []interface{}
			for _, sourceBindObj := range sourceBindList {
				for _, sourceReqData := range sourceReq.RequestObjects {
					if sourceReqData.CallbackParameter == sourceBindObj.EntityDataId {
						if bindParamType == "INPUT" {
							if len(sourceReqData.Inputs) > 0 {
								sourceBindValues = append(sourceBindValues, sourceReqData.Inputs[0][bindParamName])
							}
						} else if bindParamType == "OUTPUT" {
							if len(sourceReqData.Outputs) > 0 {
								sourceBindValues = append(sourceBindValues, sourceReqData.Outputs[0][bindParamName])
							}
						}
					}
				}
			}
			if len(sourceBindValues) == 1 {
				entityInstance.ContextMap[paramName] = sourceBindValues[0]
			} else if len(sourceBindValues) > 1 {
				entityInstance.ContextMap[paramName] = sourceBindValues
			}
		}
	}
	log.Logger.Debug("buildAutoNodeContextMap done", log.JsonObj("entityInstances", entityInstances))
	return
}

func BuildProcPreviewData(c context.Context, procDefId, entityDataId, operator string) (result *models.ProcPreviewData, err error) {
	log.Logger.Debug("build procDefPreview data", log.String("procDefId", procDefId), log.String("entityDataId", entityDataId), log.String("operator", operator))
	procOutlineData, getOutlineDataErr := database.ProcDefOutline(c, procDefId)
	if getOutlineDataErr != nil {
		err = getOutlineDataErr
		return
	}
	rootExprList, analyzeErr := remote.AnalyzeExpression(procOutlineData.RootEntity)
	if analyzeErr != nil {
		err = analyzeErr
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
		err = getRootDataErr
		return
	}
	if len(rootDataList) != 1 {
		err = fmt.Errorf("root data match %d rows,illegal ", len(rootDataList))
		return
	}
	entityNodeMap := make(map[string]*models.ProcPreviewEntityNode)
	rootData := rootDataList[0]
	result = &models.ProcPreviewData{ProcessSessionId: fmt.Sprintf("proc_session_" + guid.CreateGuid()), EntityTreeNodes: []*models.ProcPreviewEntityNode{}}
	log.Logger.Debug("rootData", log.String("entityDataId", entityDataId), log.JsonObj("data", rootData))
	rootEntityNode := models.ProcPreviewEntityNode{LastFlag: true}
	rootEntityNode.Parse(rootFilter.PackageName, rootFilter.EntityName, rootData)
	rootEntityNode.FullDataId = rootEntityNode.DataId
	entityNodeMap[rootEntityNode.Id] = &rootEntityNode
	result.EntityTreeNodes = append(result.EntityTreeNodes, &rootEntityNode)
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
		if node.OrderedNo == "" || node.RoutineExpression == "" {
			continue
		}
		nodeExpressionList := []string{}
		if node.NodeType == models.JobDataType {
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
			if nodeExpression == procOutlineData.RootEntity && len(interfaceFilters) == 0 {
				nodeDataList = append(nodeDataList, &rootEntityNode)
			} else {
				tmpQueryDataParam := models.QueryExpressionDataParam{DataModelExpression: nodeExpression, Filters: []*models.QueryExpressionDataFilter{&rootFilter}}
				tmpNodeDataList, tmpErr := QueryProcPreviewNodeData(c, &tmpQueryDataParam, &rootEntityNode, false, interfaceFilters)
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
				// 子编排试算
				if node.NodeType == models.JobSubProcType && node.SubProcDefId != "" {
					subPreviewResult, subPreviewErr := BuildProcPreviewData(c, node.SubProcDefId, tmpPreviewRow.EntityDataId, operator)
					if subPreviewErr != nil {
						err = fmt.Errorf("build sub process preview data fail,node:%s dataId:%s err:%s ", node.NodeName, tmpPreviewRow.EntityDataId, subPreviewErr.Error())
						return
					}
					tmpPreviewRow.SubSessionId = subPreviewResult.ProcessSessionId
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
	if err != nil {
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
	err = database.CreateProcPreview(c, previewRows, graphRows)
	return
}

func QueryProcPreviewNodeData(ctx context.Context, param *models.QueryExpressionDataParam, rootEntityNode *models.ProcPreviewEntityNode, withEntityData bool, lastEntityFilters []*models.Filter) (dataList []*models.ProcPreviewEntityNode, err error) {
	exprList, analyzeErr := remote.AnalyzeExpression(param.DataModelExpression)
	if analyzeErr != nil {
		err = analyzeErr
		return
	}
	exprLen := len(exprList)
	if exprLen > 0 && len(lastEntityFilters) > 0 {
		exprList[exprLen-1].Filters = append(exprList[exprLen-1].Filters, lastEntityFilters...)
	}
	dataList, err = remote.QueryPluginFullData(ctx, exprList, param.Filters[0], rootEntityNode, remote.GetToken(), withEntityData)
	return
}

func DynamicBindNodeInRuntime(ctx context.Context, procInsNode *models.ProcInsNode, procDefNode *models.ProcDefNode) (dataBinding []*models.ProcDataBinding, err error) {
	interfaceFilters := []*models.Filter{}
	if procDefNode.ServiceName != "" {
		interfaceObj, getInterfaceErr := database.GetSimpleLastPluginInterface(ctx, procDefNode.ServiceName)
		if getInterfaceErr != nil {
			err = fmt.Errorf("get node plugin interface:%s fail,%s ", procDefNode.ServiceName, getInterfaceErr.Error())
			return
		}
		if interfaceObj.FilterRule != "" {
			if interfaceFilters, err = remote.AnalyzeExprFilters(interfaceObj.FilterRule); err != nil {
				err = fmt.Errorf("analyze expr filters:%s fail,%s ", interfaceObj.FilterRule, err.Error())
				return
			}
		}
	}
	var rootEntityDataId, rootExpr string
	rootEntityDataId, _, rootExpr, err = database.GetProcInsRootEntityData(ctx, procInsNode.ProcInsId)
	if err != nil {
		return
	}
	nodeDataList := []*models.ProcPreviewEntityNode{}
	nodeExpressionList := []string{procDefNode.RoutineExpression}
	rootExprList, analyzeErr := remote.AnalyzeExpression(rootExpr)
	if analyzeErr != nil {
		err = analyzeErr
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
			Value:    rootEntityDataId,
		}},
	}
	rootEntityNode := models.ProcPreviewEntityNode{DataId: rootEntityDataId, FullDataId: rootEntityDataId}
	for _, nodeExpression := range nodeExpressionList {
		tmpQueryDataParam := models.QueryExpressionDataParam{DataModelExpression: nodeExpression, Filters: []*models.QueryExpressionDataFilter{&rootFilter}}
		tmpNodeDataList, tmpErr := QueryProcPreviewNodeData(ctx, &tmpQueryDataParam, &rootEntityNode, true, interfaceFilters)
		if tmpErr != nil {
			err = tmpErr
			break
		}
		nodeDataList = append(nodeDataList, tmpNodeDataList...)
	}
	if err != nil {
		return
	}
	log.Logger.Debug("dynamicBindNodeInRuntime nodeData", log.String("node", procInsNode.Id), log.JsonObj("data", nodeDataList))
	nowTime := time.Now()
	for _, nodeDataObj := range nodeDataList {
		if nodeDataObj.LastFlag {
			tmpPreviewRow := models.ProcDataBinding{
				EntityId:       nodeDataObj.DataId,
				EntityDataId:   nodeDataObj.DataId,
				EntityTypeId:   fmt.Sprintf("%s:%s", nodeDataObj.PackageName, nodeDataObj.EntityName),
				ProcDefId:      procDefNode.ProcDefId,
				BindType:       "taskNode",
				BindFlag:       true,
				EntityDataName: nodeDataObj.DisplayName,
				FullDataId:     nodeDataObj.FullDataId,
				ProcDefNodeId:  procDefNode.NodeId,
				CreatedBy:      "system",
				CreatedTime:    nowTime,
				EntityData:     nodeDataObj.EntityData,
			}
			dataBinding = append(dataBinding, &tmpPreviewRow)
		}
	}
	return
}
