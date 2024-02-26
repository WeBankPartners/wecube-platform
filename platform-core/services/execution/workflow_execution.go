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
		RequestId:       "p_req_" + guid.CreateGuid(),
		Operator:        param.Operator,
		ServiceName:     param.PluginInterface.ServiceName,
		ServicePath:     param.PluginInterface.ServiceDisplayName,
		EntityInstances: param.EntityInstances,
		Inputs:          inputParamDatas,
		DueDate:         param.DueDate,
		AllowedOptions:  param.AllowedOptions,
	}
	pluginCallParam.RequestId = "flowexec_" + guid.CreateGuid()
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
	//ctx = context.WithValue(ctx, models.TransactionIdHeader, procRunNodeId)
	//// 查proc def node定义和proc ins绑定数据
	//procInsNode, procDefNode, procDefNodeParams, dataBindings, getNodeDataErr := database.GetProcExecNodeData(ctx, procRunNodeId)
	//if getNodeDataErr != nil {
	//	err = getNodeDataErr
	//	return
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
	if err = database.AddProcCacheData(ctx, procInsNode.ProcInsId, dataBindings); err != nil {
		return
	}
	if pluginInterface.IsAsyncProcessing == "N" {
		err = fmt.Errorf("can not support human job with interface sync defind")
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
	callPluginServiceParam := models.ProcCallPluginServiceFuncParam{
		PluginInterface:   pluginInterface,
		EntityType:        procDefNode.RoutineExpression,
		EntityInstances:   entityInstances,
		InputConstantMap:  inputConstantMap,
		InputParamContext: inputContextMap,
		RiskCheck:         procDefNode.RiskCheck,
		Operator:          "SYSTEM",
		ProcInsNode:       procInsNode,
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
			buildTaskFormInput(ctx, taskFormMeta, param.ProcInsNode.ProcInsId, param.ProcInsNode.Id)
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
		RequestId:       "p_req_" + guid.CreateGuid(),
		Operator:        param.Operator,
		ServiceName:     param.PluginInterface.ServiceName,
		ServicePath:     param.PluginInterface.ServiceDisplayName,
		EntityInstances: param.EntityInstances,
		Inputs:          inputParamDatas,
		DueDate:         param.DueDate,
		AllowedOptions:  param.AllowedOptions,
	}
	pluginCallParam.RequestId = "flowexec_" + guid.CreateGuid()
	// 纪录参数
	if err = database.RecordProcCallReq(ctx, &procInsNodeReq, true); err != nil {
		return
	}
	pluginCallResult, _, errCall := remote.PluginInterfaceApi(ctx, subsysToken, param.PluginInterface, pluginCallParam)
	if errCall != nil {
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
	return
}

func buildTaskFormInput(ctx context.Context, taskFormMeta *models.TaskMetaResultData, procInsId, procInsNodeId string) (formData *models.PluginTaskFormDto, err error) {
	//cacheDataRows, getCacheErr := database.GetProcCacheData(ctx, procInsId)
	//if getCacheErr != nil {
	//	err = getCacheErr
	//	return
	//}

	return
}
