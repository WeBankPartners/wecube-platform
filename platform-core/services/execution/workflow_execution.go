package execution

import (
	"context"
	"fmt"

	"github.com/WeBankPartners/go-common-lib/guid"
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

func WorkflowExecutionCallPluginService(ctx context.Context, operator string, pluginInterface *models.PluginConfigInterfaces, entityType string,
	entityInstances []*models.BatchExecutionPluginExecEntityInstances,
	inputParamConstants []*models.BatchExecutionPluginDefInputParams,
	inputParamContext map[string]interface{},
	continueToken string, dueDate string, allowedOptions []string) (result *models.PluginInterfaceApiResultData, dangerousCheckResult *models.ItsdangerousWorkflowCheckResultData, pluginCallParam *models.BatchExecutionPluginExecParam, err error) {
	inputConstantMap := make(map[string]string)
	for _, inputConst := range inputParamConstants {
		inputConstantMap[inputConst.ParamId] = inputConst.ParameValue
	}
	rootExprList, errAnalyze1 := remote.AnalyzeExpression(entityType)
	if err != nil {
		err = errAnalyze1
		return
	}
	if len(rootExprList) == 0 {
		err = fmt.Errorf("invalid input entity type %s", entityType)
		return
	}
	rootExpr := rootExprList[len(rootExprList)-1]
	// 获取subsystem token
	subsysToken := remote.GetToken()
	// 构造输入参数
	inputParamDatas, errHandle := handleInputData(ctx, subsysToken, continueToken, entityInstances, pluginInterface.InputParameters, rootExpr, inputConstantMap, inputParamContext)
	if errHandle != nil {
		err = errHandle
		return
	}
	// 调用高危插件
	itsdangerousCallParam := &models.BatchExecutionItsdangerousExecParam{
		Operator:        operator,
		ServiceName:     pluginInterface.ServiceName,
		ServicePath:     pluginInterface.ServiceDisplayName,
		EntityType:      entityType,
		EntityInstances: entityInstances,
		InputParams:     inputParamDatas,
	}
	// 需要有运行时的高危插件
	dangerousResult, errDangerous := performWorkflowDangerousCheck(ctx, itsdangerousCallParam, continueToken, subsysToken)
	if errDangerous != nil {
		err = errDangerous
		return
	}
	if dangerousResult != nil {
		dangerousCheckResult = dangerousResult
		return
	}
	// 调用插件接口
	pluginCallParam = &models.BatchExecutionPluginExecParam{
		RequestId:       "",
		Operator:        operator,
		ServiceName:     pluginInterface.ServiceName,
		ServicePath:     pluginInterface.ServiceDisplayName,
		EntityInstances: entityInstances,
		Inputs:          inputParamDatas,
		DueDate:         dueDate,
		AllowedOptions:  allowedOptions,
	}
	pluginCallParam.RequestId = "flowexec_" + guid.CreateGuid()
	pluginCallResult, errCall := remote.PluginInterfaceApi(ctx, subsysToken, pluginInterface, pluginCallParam)
	if errCall != nil {
		err = errCall
		return
	}
	// 处理output param(比如类型转换，数据模型写入), handleOutputData主要是用于格式化为output param定义的字段
	_, errHandle = handleOutputData(ctx, subsysToken, pluginCallResult.Outputs, pluginInterface.OutputParameters)
	if errHandle != nil {
		err = errHandle
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
