package execution

import (
	"context"
	"fmt"
	"reflect"
	"strconv"

	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
)

// 插件调用结果，若有输出到Entity的数据，则使用OutputEntityData进行保存
type OutputEntityData struct {
	// 根CI
	Package string
	Entity  string
	Data    []*OutputEntityRootData
}

type OutputEntityRootData struct {
	// Id为空则创建，不为空则更新
	Id string
	// 相对于Id的创建或更新的数据
	Data map[string]interface{}
	// CI写入不止是根CI，还有基于根CI表达式分支数据更新(不可能是创建)
	SubBranchs []*OutputEntityBranchData
}

type OutputEntityBranchData struct {
	// 基于根CI表达式分支数据
	OriginExpr string
	Exprs      []*models.ExpressionObj
	Data       map[string]interface{}
}

/**
 * Func: BatchExecutionCallPluginService 批量执行调用插件接口
 *
 * @params ctx 上下文数据，需要是gin ctx
 * @params operator 操作人用户名
 * @params authToken 用户token
 * @params pluginInterfaceId 要调用的插件接口ID
 * @params entityType 输入entity的表达式
 * @params entityInstances 输入entity的数据
 * @params inputParamConstants 输入数据(常量，即用户输入的)
 * @params continueToken 是否进行高危检测(有值则跳过)
 *
 * @return 调用结果, 高危结果, 错误
 */

func BatchExecutionCallPluginService(ctx context.Context, operator, authToken, pluginInterfaceId string, entityType string,
	entityInstances []*models.BatchExecutionPluginExecEntityInstances,
	inputParamConstants []*models.BatchExecutionPluginDefInputParams,
	continueToken string) (result *models.PluginInterfaceApiResultData, dangerousCheckResult *models.ItsdangerousBatchCheckResultData, pluginCallParam *models.BatchExecutionPluginExecParam, err error) {
	pluginInterface, errGet := database.GetPluginConfigInterfaceById(pluginInterfaceId, true)
	if errGet != nil {
		err = errGet
		return
	}
	if pluginInterface == nil {
		err = fmt.Errorf("invalid plugin interface %s", pluginInterfaceId)
		return
	}
	if pluginInterface.Type != models.PluginInterfaceTypeExecution {
		err = fmt.Errorf("unsupported plugin interface type %s", pluginInterface.Type)
		return
	}
	inputConstantMap := make(map[string]string)
	for _, inputConst := range inputParamConstants {
		inputConstantMap[inputConst.ParamId] = inputConst.ParameValue
	}
	rootExprList, errAnalyze1 := remote.AnalyzeExpression(entityType)
	if err != nil {
		err = errAnalyze1
		return
	}
	if len(rootExprList) != 1 {
		err = fmt.Errorf("invalid input entity type %s", entityType)
		return
	}
	rootExpr := rootExprList[0]
	// 构造输入参数
	inputParamDatas, errHandle := handleInputData(ctx, authToken, continueToken, entityInstances, pluginInterface.InputParameters, rootExpr, inputConstantMap)
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
	// 获取subsystem token
	subsysToken := remote.GetToken()
	dangerousResult, errDangerous := performDangerousCheck(ctx, itsdangerousCallParam, continueToken, subsysToken)
	if errDangerous != nil {
		err = errDangerous
		return
	}
	if dangerousResult != nil && len(dangerousResult.Data) > 0 {
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
	}
	if pluginInterface.IsAsyncProcessing == "Y" {
		pluginCallParam.RequestId = "batchexec_" + guid.CreateGuid()
	}
	pluginCallResult, errCall := remote.PluginInterfaceApi(ctx, authToken, pluginInterface, pluginCallParam)
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

func normalizePluginInterfaceParamData(inputParamDef *models.PluginConfigInterfaceParameters, value interface{}) (interface{}, error) {
	// Required 空默认是N
	// Multiple 空默认是N
	// 如果value是nil，但required，则报错
	if value == nil {
		if inputParamDef.Required == "Y" {
			return nil, fmt.Errorf("field:%s input data is nil but required", inputParamDef.Name)
		}
		// nil值不需要规格化
		return nil, nil
	}
	// 此时value可能是nil以外的任意值
	var result interface{}
	t := reflect.TypeOf(value)
	// value的kind可能是[]{int, string, float, object}, int, string, float, object
	if t.Kind() == reflect.Slice {
		if inputParamDef.Multiple == "Y" || inputParamDef.DataType == models.PluginParamDataTypeList {
			// FIXME： 列表元素类型转换
			result = value
		} else {
			tv := reflect.ValueOf(value)
			if tv.Len() == 0 && inputParamDef.Required == "Y" {
				return nil, fmt.Errorf("field:%s input data is empty list but required", inputParamDef.Name)
			}
			if tv.Len() != 1 {
				return nil, fmt.Errorf("field:%s input data len=%d but trying to convert to single value", inputParamDef.Name, tv.Len())
			}
			// 列表转单值
			valueToSingle := tv.Index(0).Interface()
			tToSingle := reflect.TypeOf(valueToSingle)
			if inputParamDef.DataType == models.PluginParamDataTypeInt {
				convValue, err := convertToDatatypeInt(inputParamDef.Name, valueToSingle, tToSingle)
				if err != nil {
					return nil, err
				}
				result = convValue
				// 转换为列表
				if inputParamDef.Multiple == "Y" {
					result = []interface{}{convValue}
				}
			} else if inputParamDef.DataType == models.PluginParamDataTypeString {
				convValue, err := convertToDatatypeString(inputParamDef.Name, valueToSingle, tToSingle)
				if err != nil {
					return nil, err
				}
				result = convValue
				// 转换为列表
				if inputParamDef.Multiple == "Y" {
					result = []interface{}{convValue}
				}
			} else if inputParamDef.DataType == models.PluginParamDataTypeObject {
				return nil, fmt.Errorf("field:%s can not convert %v to object", inputParamDef.Name, tToSingle)
			} else if inputParamDef.DataType == models.PluginParamDataTypeList {
				return nil, fmt.Errorf("field:%s can not convert %v to list", inputParamDef.Name, tToSingle)
			}
		}
	} else if t.Kind() == reflect.Map {
		// 如果value是map，datatype不是object，则报错
		if inputParamDef.DataType != models.PluginParamDataTypeObject {
			return nil, fmt.Errorf("field:%s input data is %v but datatype is %s", inputParamDef.Name, t, inputParamDef.DataType)
		}
		result = value
		// 转换为列表
		if inputParamDef.Multiple == "Y" {
			result = []interface{}{result}
		}
	} else {
		if inputParamDef.DataType == models.PluginParamDataTypeInt {
			convValue, err := convertToDatatypeInt(inputParamDef.Name, value, t)
			if err != nil {
				return nil, err
			}
			result = convValue
			// 转换为列表
			if inputParamDef.Multiple == "Y" {
				result = []interface{}{convValue}
			}
		} else if inputParamDef.DataType == models.PluginParamDataTypeString {
			convValue, err := convertToDatatypeString(inputParamDef.Name, value, t)
			if err != nil {
				return nil, err
			}
			result = convValue
			// 转换为列表
			if inputParamDef.Multiple == "Y" {
				result = []interface{}{convValue}
			}
		} else if inputParamDef.DataType == models.PluginParamDataTypeObject {
			return nil, fmt.Errorf("field:%s can not convert %v to object", inputParamDef.Name, t)
		} else if inputParamDef.DataType == models.PluginParamDataTypeList {
			return nil, fmt.Errorf("field:%s can not convert %v to list", inputParamDef.Name, t)
		}
	}
	return result, nil
}

func convertToDatatypeInt(name string, value interface{}, valueType reflect.Type) (result int, err error) {
	// int转换
	switch valueType.Kind() {
	case reflect.Float64:
		result = int(value.(float64))
	case reflect.Float32:
		result = int(value.(float32))
	case reflect.Int:
		result = int(value.(int))
	case reflect.Int32:
		result = int(value.(int32))
	case reflect.Int64:
		result = int(value.(int64))
	case reflect.String:
		rv, errConv := strconv.ParseInt(value.(string), 10, 64)
		if errConv != nil {
			return 0, errConv
		}
		result = int(rv)
	default:
		return 0, fmt.Errorf("field:%s can not convert %v to int", name, valueType)
	}
	return
}

func convertToDatatypeString(name string, value interface{}, valueType reflect.Type) (result string, err error) {
	// string转换
	switch valueType.Kind() {
	case reflect.Float64:
		result = fmt.Sprintf("%f", value.(float64))
	case reflect.Float32:
		result = fmt.Sprintf("%f", value.(float32))
	case reflect.Int:
		result = fmt.Sprintf("%d", value.(int))
	case reflect.Int32:
		result = fmt.Sprintf("%d", value.(int32))
	case reflect.Int64:
		result = fmt.Sprintf("%d", value.(int64))
	case reflect.String:
		result = value.(string)
	default:
		return "", fmt.Errorf("field:%s can not convert %v to string", name, valueType)
	}
	return
}

func performDangerousCheck(ctx context.Context, pluginCallParam interface{}, continueToken string, authToken string) (result *models.ItsdangerousBatchCheckResultData, err error) {
	// 是否有continueToken，有则跳过
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
	result, err = remote.DangerousBatchCheck(ctx, authToken, pluginCallParam)
	return
}

func handleInputData(
	ctx context.Context,
	authToken string,
	continueToken string,
	entityInstances []*models.BatchExecutionPluginExecEntityInstances,
	inputParamDefs []*models.PluginConfigInterfaceParameters,
	rootExpr *models.ExpressionObj, inputConstantMap map[string]string) (inputParamDatas []models.BatchExecutionPluginExecInputParams, err error) {
	inputParamDatas = make([]models.BatchExecutionPluginExecInputParams, 0)
	for _, entityInstance := range entityInstances {
		inputParamData := models.BatchExecutionPluginExecInputParams{}
		for _, inputDef := range inputParamDefs {
			var inputCalResult interface{}
			switch inputDef.MappingType {
			case models.PluginParamMapTypeConstant:
				inputCalResult = inputConstantMap[inputDef.Id]
			case models.PluginParamMapTypeSystemVar:
				if inputDef.MappingSystemVariableName == "" {
					err = fmt.Errorf("input param %s is map to %s, but variable name is empty", inputDef.Name, inputDef.MappingType)
					return
				}
				inputCalResult, err = database.GetSystemVariable(context.Background(), inputDef.MappingSystemVariableName)
				if err != nil {
					return
				}
			case models.PluginParamMapTypeContext:
				// 上下文参数获取不支持
				err = fmt.Errorf("input param %s is map to %s, which batch execution is not supported", inputDef.Name, inputDef.MappingType)
				return
			case models.PluginParamMapTypeEntity:
				// 从数据模型获取
				if inputDef.MappingEntityExpression == "" {
					err = fmt.Errorf("input param %s is map to %s, but entity expression is empty", inputDef.Name, inputDef.MappingType)
					return
				}
				execExprList, errAnalyze2 := remote.AnalyzeExpression(inputDef.MappingEntityExpression)
				if err != nil {
					err = errAnalyze2
					return
				}
				execExprFilterList := make([]*models.QueryExpressionDataFilter, 0)
				execExprFilter := &models.QueryExpressionDataFilter{
					PackageName:      rootExpr.Package,
					EntityName:       rootExpr.Entity,
					AttributeFilters: make([]*models.QueryExpressionDataAttrFilter, 0),
				}
				execExprFilter.AttributeFilters = append(execExprFilter.AttributeFilters, &models.QueryExpressionDataAttrFilter{
					Name:     "id",
					Operator: "eq",
					Value:    entityInstance.Id,
				})
				execExprFilterList = append(execExprFilterList, execExprFilter)
				execExprResult, errExec := remote.QueryPluginData(ctx, execExprList, execExprFilterList, authToken)
				if errExec != nil {
					err = errExec
					return
				}
				inputCalResult = remote.ExtractExpressionResultColumn(execExprList, execExprResult)
			case models.PluginParamMapTypeObject:
				// TODO: 从指定对象获取暂不支持(k8s)
				err = fmt.Errorf("input param %s is map to %s, which batch execution is not supported", inputDef.Name, inputDef.MappingType)
				return
			}
			inputParamData[inputDef.Name], err = normalizePluginInterfaceParamData(inputDef, inputCalResult)
			if err != nil {
				return
			}
		}
		// NOTE: 仅批量执行的PluginCallParamPresetCallback是行ID，插件调用时是随机ID与行数据没有关联
		inputParamData[models.PluginCallParamPresetCallback] = entityInstance.Id
		inputParamData[models.PluginCallParamPresetConfirm] = continueToken
		inputParamDatas = append(inputParamDatas, inputParamData)
	}
	return
}

func handleOutputData(
	ctx context.Context,
	authToken string,
	outputs []map[string]interface{},
	outputParamDefs []*models.PluginConfigInterfaceParameters) (result *models.PluginInterfaceApiResultData, err error) {
	tmpResult := &models.PluginInterfaceApiResultData{Outputs: make([]map[string]interface{}, 0)}
	tmpResultForEntity := &OutputEntityData{Data: make([]*OutputEntityRootData, 0)}
	for _, output := range outputs {
		tmpResultOutput := make(map[string]interface{})
		var tmpResultOutputForEntity *OutputEntityRootData
		tmpResultForPackageName := ""
		tmpResultForEntityName := ""
		for _, outputDef := range outputParamDefs {
			var tmpResultOutputForEntityBranch *OutputEntityBranchData
			var entityKeyName string
			var outputCalResult interface{}
			switch outputDef.MappingType {
			case models.PluginParamMapTypeContext:
				outputCalResult = output[outputDef.Name]
			case models.PluginParamMapTypeEntity:
				outputCalResult = output[outputDef.Name]
				// 分析表达式
				if outputDef.MappingEntityExpression == "" {
					err = fmt.Errorf("input param %s is map to %s, but entity expression is empty", outputDef.Name, outputDef.MappingType)
					return
				}
				execExprList, errAnalyze := remote.AnalyzeExpression(outputDef.MappingEntityExpression)
				if err != nil {
					err = errAnalyze
					return
				}
				if len(execExprList) == 1 {
					entityKeyName = execExprList[0].ResultColumn
				}
				tmpResultForPackageName = execExprList[0].Package
				tmpResultForEntityName = execExprList[0].Entity
				if len(execExprList) > 1 {
					branchResultOutput := make(map[string]interface{})
					// outputCalResult规格化后保存进branchResultOutput
					outputCalResultConv, errConv := normalizePluginInterfaceParamData(outputDef, outputCalResult)
					if err != nil {
						err = errConv
						return
					}
					branchResultOutput[execExprList[len(execExprList)-1].ResultColumn] = outputCalResultConv
					tmpResultOutputForEntityBranch = &OutputEntityBranchData{
						OriginExpr: outputDef.MappingEntityExpression,
						Exprs:      execExprList,
						Data:       branchResultOutput,
					}
				}
			case models.PluginParamMapTypeAssign:
				// 分析表达式
				if outputDef.MappingEntityExpression == "" {
					err = fmt.Errorf("input param %s is map to %s, but entity expression is empty", outputDef.Name, outputDef.MappingType)
					return
				}
				execExprList, errAnalyze := remote.AnalyzeExpression(outputDef.MappingEntityExpression)
				if err != nil {
					err = errAnalyze
					return
				}
				outputCalResult = outputDef.MappingVal
				if len(execExprList) == 1 {
					entityKeyName = execExprList[0].ResultColumn
				}
				tmpResultForPackageName = execExprList[0].Package
				tmpResultForEntityName = execExprList[0].Entity
				if len(execExprList) > 1 {
					branchResultOutput := make(map[string]interface{})
					// outputCalResult规格化后保存进branchResultOutput
					outputCalResultConv, errConv := normalizePluginInterfaceParamData(outputDef, outputCalResult)
					if err != nil {
						err = errConv
						return
					}
					branchResultOutput[execExprList[len(execExprList)-1].ResultColumn] = outputCalResultConv
					tmpResultOutputForEntityBranch = &OutputEntityBranchData{
						OriginExpr: outputDef.MappingEntityExpression,
						Exprs:      execExprList,
						Data:       branchResultOutput,
					}
				}
			default:
				err = fmt.Errorf("input param %s is map to %s, which batch execution is not supported", outputDef.Name, outputDef.MappingType)
				return
			}
			tmpResultOutput[outputDef.Name], err = normalizePluginInterfaceParamData(outputDef, outputCalResult)
			if err != nil {
				return
			}
			if entityKeyName != "" {
				if tmpResultOutputForEntity == nil {
					tmpResultOutputForEntity = &OutputEntityRootData{Data: make(map[string]interface{}), SubBranchs: make([]*OutputEntityBranchData, 0)}
				}
				tmpResultOutputForEntity.Data[entityKeyName] = tmpResultOutput[outputDef.Name]
				if tmpResultOutputForEntityBranch != nil {
					tmpResultOutputForEntity.SubBranchs = append(tmpResultOutputForEntity.SubBranchs, tmpResultOutputForEntityBranch)
				}
			}
			// 补充预设参数
			if v, ok := output[models.PluginCallResultPresetCallback]; ok {
				tmpResultOutput[models.PluginCallResultPresetCallback] = v
			}
			if v, ok := output[models.PluginCallResultPresetErrorCode]; ok {
				tmpResultOutput[models.PluginCallResultPresetErrorCode] = v
			}
			if v, ok := output[models.PluginCallResultPresetErrorMsg]; ok {
				tmpResultOutput[models.PluginCallResultPresetErrorMsg] = v
			}
		}
		tmpResult.Outputs = append(tmpResult.Outputs, tmpResultOutput)
		if tmpResultForPackageName != "" && tmpResultForEntityName != "" {
			tmpResultForEntity.Package = tmpResultForPackageName
			tmpResultForEntity.Entity = tmpResultForEntityName
			if tmpResultOutputForEntity != nil {
				if v, ok := tmpResultOutputForEntity.Data["id"]; ok {
					tmpResultOutputForEntity.Id = v.(string)
				}
				tmpResultForEntity.Data = append(tmpResultForEntity.Data, tmpResultOutputForEntity)
			}
		}
	}
	// 处理entity写入
	for _, rootData := range tmpResultForEntity.Data {
		if rootData.Id == "" {
			ret, errCreate := remote.CreateEntityData(ctx, authToken, tmpResultForEntity.Package, tmpResultForEntity.Entity, rootData.Data)
			if errCreate != nil {
				log.Logger.Error(fmt.Sprintf("failed to create %s entity %s %v", tmpResultForEntity.Package, tmpResultForEntity.Entity, rootData.Data))
			} else {
				// 成功需要回写ID，以便后续Branch数据更新使用
				rootData.Id = ret["id"].(string)
				log.Logger.Debug(fmt.Sprintf("create %s entity %s[%s] %v", tmpResultForEntity.Package, tmpResultForEntity.Entity, rootData.Id, rootData.Data))
			}
		} else {
			// 除了ID以外有其他值才需要写入
			if len(rootData.Data) > 1 {
				_, errUpdate := remote.UpdateEntityData(ctx, authToken, tmpResultForEntity.Package, tmpResultForEntity.Entity, rootData.Data)
				if errUpdate != nil {
					log.Logger.Error(fmt.Sprintf("failed to update %s entity %s[%s] %v", tmpResultForEntity.Package, tmpResultForEntity.Entity, rootData.Id, rootData.Data))
				} else {
					log.Logger.Debug(fmt.Sprintf("update %s entity %s[%s] %v", tmpResultForEntity.Package, tmpResultForEntity.Entity, rootData.Id, rootData.Data))
				}
			}
		}
		// 处理多级表达式数据更新
		for _, branchData := range rootData.SubBranchs {
			errUpdate := remote.UpdatentityDataWithExpr(ctx, authToken, tmpResultForEntity.Package, tmpResultForEntity.Entity, rootData.Id, branchData.Exprs, branchData.Data)
			if errUpdate != nil {
				log.Logger.Error(fmt.Sprintf("failed to update %s entity %s[%s] with expression %s %v", tmpResultForEntity.Package, tmpResultForEntity.Entity, rootData.Id, branchData.OriginExpr, branchData.Data))
			} else {
				log.Logger.Debug(fmt.Sprintf("update %s entity %s[%s] with expression %s %v", tmpResultForEntity.Package, tmpResultForEntity.Entity, rootData.Id, branchData.OriginExpr, branchData.Data))
			}
		}
	}
	result = tmpResult
	return
}
