package execution

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"strings"
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
	if err != nil {
		err = errAnalyze1
		return
	}
	if len(rootExprList) == 0 {
		err = fmt.Errorf("invalid input entity type %s", param.EntityType)
		return
	}
	rootExpr := rootExprList[len(rootExprList)-1]
	// 获取subsystem token
	subsysToken := remote.GetToken()
	// 构造输入参数
	inputParamDatas, errHandle := handleInputData(ctx, subsysToken, param.ContinueToken, param.EntityInstances, param.PluginInterface.InputParameters, rootExpr, param.InputConstantMap, param.InputParamContext, &procInsNodeReq)
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
		dangerousResult, errDangerous := performWorkflowDangerousCheck(ctx, itsdangerousCallParam, param.ContinueToken, subsysToken)
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
	pluginCallResult, errCode, errCall := remote.PluginInterfaceApi(ctx, subsysToken, param.PluginInterface, pluginCallParam)
	if errCall != nil {
		if errCode != "" && errCode != "0" {
			_, errHandle = handleOutputData(ctx, subsysToken, pluginCallResult.Outputs, param.PluginInterface.OutputParameters, &procInsNodeReq)
			if errHandle != nil {
				log.Logger.Error("handle error output data fail", log.Error(errHandle))
			}
		}
		err = errCall
		procInsNodeReq.ErrorMsg = err.Error()
		database.RecordProcCallReq(ctx, &procInsNodeReq, false)
		return
	}
	// 处理output param(比如类型转换，数据模型写入), handleOutputData主要是用于格式化为output param定义的字段
	_, errHandle = handleOutputData(ctx, subsysToken, pluginCallResult.Outputs, param.PluginInterface.OutputParameters, &procInsNodeReq)
	if errHandle != nil {
		err = errHandle
		return
	}
	if err = database.RecordProcCallReq(ctx, &procInsNodeReq, false); err != nil {
		return
	}
	// 批量执行需要返回原始插件结果，而不是格式化output字段的值
	result = pluginCallResult
	return
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
	if procDefNode.DynamicBind {
		dataBindings, err = database.GetDynamicBindNodeData(ctx, procInsNode.ProcInsId, procDefNode.ProcDefId, procDefNode.BindNodeId)
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
	if err = database.AddProcCacheData(ctx, procInsNode.ProcInsId, dataBindings); err != nil {
		return
	}
	var entityInstances []*models.BatchExecutionPluginExecEntityInstances
	for _, bindingObj := range dataBindings {
		entityInstances = append(entityInstances, &models.BatchExecutionPluginExecEntityInstances{
			Id:               bindingObj.EntityId,
			BusinessKeyValue: "",
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
	var dataConfigList []*models.ProcDataNodeExprObj
	for _, subExpr := range strings.Split(procDefNode.RoutineExpression, "#DME#") {
		subSplit := strings.Split(subExpr, "#DMEOP#")
		if len(subSplit) != 2 {
			err = fmt.Errorf("data nodeType expression:%s illegal", subExpr)
			return
		}
		dataConfigList = append(dataConfigList, &models.ProcDataNodeExprObj{Expression: subSplit[0], Operation: subSplit[1]})
	}
	if len(dataConfigList) == 0 {
		return
	}
	//procDef, getProcDefErr := database.GetSimpleProcDefRow(ctx, procDefNode.ProcDefId)
	//if getProcDefErr != nil {
	//	err = getProcDefErr
	//	return
	//}
	//procIns,getProcInsErr := database.GetSimpleProcInsRow(ctx, procInsNode.ProcInsId)
	//if getProcInsErr != nil {
	//	err = getProcInsErr
	//	return
	//}
	//rootExprList, analyzeErr := remote.AnalyzeExpression(procDef.RootEntity)
	//if analyzeErr != nil {
	//	return
	//}
	//rootLastExprObj := rootExprList[len(rootExprList)-1]
	//rootFilter := models.QueryExpressionDataFilter{
	//	Index:       len(rootExprList) - 1,
	//	PackageName: rootLastExprObj.Package,
	//	EntityName:  rootLastExprObj.Entity,
	//	AttributeFilters: []*models.QueryExpressionDataAttrFilter{{
	//		Name:     "id",
	//		Operator: "eq",
	//		Value:    procIns.EntityDataId,
	//	}},
	//}
	//for _,dataConfigObj := range dataConfigList {
	//	tmpExprAnalyzeResult,tmpErr := remote.AnalyzeExpression(dataConfigObj.Expression)
	//	if tmpErr != nil {
	//		err = fmt.Errorf("analyze expression:%s error:%s ", dataConfigObj.Expression, tmpErr.Error())
	//		return
	//	}
	//	tmpQueryPluginResult, tmpQueryErr := remote.QueryPluginData(ctx, tmpExprAnalyzeResult, []*models.QueryExpressionDataFilter{&rootFilter}, remote.GetToken())
	//	if tmpQueryErr != nil {
	//
	//	}
	//}
	return
}

func DoWorkflowHumanJob(ctx context.Context, procRunNodeId string) (err error) {
	ctx = context.WithValue(ctx, models.TransactionIdHeader, procRunNodeId)
	// 查proc def node定义和proc ins绑定数据
	procInsNode, procDefNode, procDefNodeParams, dataBindings, getNodeDataErr := database.GetProcExecNodeData(ctx, procRunNodeId)
	if getNodeDataErr != nil {
		err = getNodeDataErr
		return
	}
	if procDefNode.DynamicBind {
		dataBindings, err = database.GetDynamicBindNodeData(ctx, procInsNode.ProcInsId, procDefNode.ProcDefId, procDefNode.BindNodeId)
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
	var entityInstances []*models.BatchExecutionPluginExecEntityInstances
	entityInstances = []*models.BatchExecutionPluginExecEntityInstances{{Id: procIns.EntityDataId}}
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
			bindNodeType, getTypeErr := database.GetProcContextBindNodeType(ctx, procDefNode.ProcDefId, v.CtxBindNode)
			if getTypeErr != nil {
				err = getTypeErr
				return
			}
			if bindNodeType == "start" {
				inputContextMap["procInstId"] = procIns.Id
				inputContextMap["procDefName"] = procIns.ProcDefName
				inputContextMap["procDefKey"] = procIns.ProcDefKey
				inputContextMap["procInstKey"] = procIns.ProcDefName
				inputContextMap["procInstName"] = procIns.ProcDefName
				inputContextMap["rootEntityId"] = procIns.EntityDataId
				inputContextMap["rootEntityName"] = procIns.EntityDataName
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
	if pluginInterface.Type == "DYNAMICFORM" {
		err = CallDynamicFormReq(ctx, &callPluginServiceParam)
	} else if pluginInterface.Type == "APPROVAL" {

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
	if err != nil {
		err = errAnalyze1
		return
	}
	if len(rootExprList) == 0 {
		err = fmt.Errorf("invalid input entity type %s", param.EntityType)
		return
	}
	rootExpr := rootExprList[len(rootExprList)-1]
	// 获取subsystem token
	subsysToken := remote.GetToken()
	// 构造输入参数
	for _, paramObj := range param.PluginInterface.InputParameters {
		if paramObj.Name == "taskFormInput" {
			// 请求taskman拿表单结构
			taskFormMeta, getFormMetaErr := remote.GetInputFormMeta(ctx, param.ProcInsNode.ProcInsId, param.ProcInsNode.ProcDefNodeId, param.PluginInterface)
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
	inputParamDatas, errHandle := handleInputData(ctx, subsysToken, param.ContinueToken, param.EntityInstances, param.PluginInterface.InputParameters, rootExpr, param.InputConstantMap, param.InputParamContext, &procInsNodeReq)
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
	pluginCallResult, _, errCall := remote.PluginInterfaceApi(ctx, subsysToken, param.PluginInterface, pluginCallParam)
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
		TaskNodeDefId:  param.ProcDefNode.Id,
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

func HandleCallbackHumanJob(ctx context.Context, procRunNodeId string, callbackData *models.PluginTaskCreateOutput) (choseOption string, err error) {
	if len(callbackData.AllowedOptions) != 1 {
		err = fmt.Errorf("callback allowe options length illegal,%s ", callbackData.AllowedOptions)
		return
	}
	choseOption = callbackData.AllowedOptions[0]
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
	outputBytes, _ := json.Marshal(callbackData.Outputs)
	if err = json.Unmarshal(outputBytes, &pluginCallOutput); err != nil {
		err = fmt.Errorf("json unmarshal call output data to []map[string]interface{} fail,%s", err.Error())
		return
	}
	procInsNodeReq := models.ProcInsNodeReq{Id: callbackData.RequestId}
	// 处理output param(比如类型转换，数据模型写入), handleOutputData主要是用于格式化为output param定义的字段
	_, errHandle := handleOutputData(ctx, remote.GetToken(), pluginCallOutput, pluginInterface.OutputParameters, &procInsNodeReq)
	if errHandle != nil {
		err = errHandle
		return
	}
	if err = database.RecordProcCallReq(ctx, &procInsNodeReq, false); err != nil {
		return
	}
	// 更新cache data
	var taskFormList []*models.PluginTaskFormDto
	for _, output := range callbackData.Outputs {
		if output.ErrorCode == "0" {
			tmpTaskFormObj := models.PluginTaskFormDto{}
			if tmpUnmarshalErr := json.Unmarshal([]byte(output.TaskFormOutput), &tmpTaskFormObj); tmpUnmarshalErr != nil {
				log.Logger.Error("human job callback output json unmarshal taskFormOutput fail", log.Error(tmpUnmarshalErr), log.String("reqId", callbackData.RequestId), log.JsonObj("outputData", output))
			} else {
				taskFormList = append(taskFormList, &tmpTaskFormObj)
			}
		} else {
			log.Logger.Warn("human job callback output fail", log.String("reqId", callbackData.RequestId), log.JsonObj("outputData", output))
		}
	}
	if len(taskFormList) > 0 {
		err = database.UpdateProcCacheData(ctx, procInsNode.ProcInsId, taskFormList)
	}
	return
}
