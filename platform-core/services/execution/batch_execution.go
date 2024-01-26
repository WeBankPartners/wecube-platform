package execution

import (
	"context"
	"fmt"
	"reflect"
	"strconv"

	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
)

func BatchExecutionCallPluginService(ctx context.Context, operator, authToken, pluginInterfaceId string, entityType string,
	entityInstances []*models.BatchExecutionPluginExecEntityInstances,
	inputDefinitions []*models.BatchExecutionPluginDefInputParams, continueToken string) (result *models.PluginInterfaceApiResultData, dangerousCheckResult *models.ItsdangerousCheckResultData, err error) {
	pluginInterface, errGet := database.GetPluginConfigInterfaceById(pluginInterfaceId)
	if errGet != nil {
		err = errGet
		return
	}
	inputDefinitionMap := make(map[string]string)
	for _, inputDef := range inputDefinitions {
		inputDefinitionMap[inputDef.ParamId] = inputDef.ParameValue
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
	inputParamDatas := make([]models.BatchExecutionPluginExecInputParams, 0)
	for _, entityInstance := range entityInstances {
		inputParamData := models.BatchExecutionPluginExecInputParams{}
		for _, input := range pluginInterface.InputParameters {
			var inputCalResult interface{}
			switch input.MappingType {
			case models.PluginParamMapTypeConstant:
				inputCalResult = inputDefinitionMap[input.Id]
			case models.PluginParamMapTypeSystemVar:
				if input.MappingSystemVariableName == "" {
					err = fmt.Errorf("input param %s is map to %s, but variable name is empty", input.Name, input.MappingType)
					return
				}
				database.GetSystemVariable(context.Background(), input.MappingSystemVariableName)
			case models.PluginParamMapTypeContext:
				// TODO: 从上下文参数获取是否支持???
				err = fmt.Errorf("input param %s is map to %s, which batch execution is not supported", input.Name, input.MappingType)
				return
			case models.PluginParamMapTypeEntity:
				// 从数据模型获取
				if input.MappingEntityExpression == "" {
					err = fmt.Errorf("input param %s is map to %s, but entity expression is empty", input.Name, input.MappingType)
					return
				}
				execExprList, errAnalyze2 := remote.AnalyzeExpression(input.MappingEntityExpression)
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
				// TODO: 从指定对象获取是否支持???
				err = fmt.Errorf("input param %s is map to %s, which batch execution is not supported", input.Name, input.MappingType)
				return
			}
			inputParamData[input.Name], err = normalizePluginInterfaceParamData(input, inputCalResult)
			if err != nil {
				return
			}
		}
		inputParamDatas = append(inputParamDatas)
	}
	// 调用高危插件
	pluginCallParam := &models.BatchExecutionPluginExecParam{
		Operator:        operator,
		ServiceName:     pluginInterface.ServiceName,
		ServicePath:     pluginInterface.ServiceDisplayName,
		EntityType:      entityType,
		EntityInstances: entityInstances,
		InputParams:     inputParamDatas,
	}
	// 需要有运行时的高危插件
	dangerousResult, errDangerous := performDangerousCheck(ctx, pluginCallParam, continueToken)
	if errDangerous != nil {
		err = errDangerous
		return
	}
	if dangerousResult != nil {
		dangerousCheckResult = dangerousResult
		return
	}
	// 调用插件接口
	result, err = remote.PluginInterfaceApi(ctx, authToken, pluginInterface)
	// TODO: 处理output param(比如类型转换，assign)
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
			if t.Len() == 0 && inputParamDef.Required == "Y" {
				return nil, fmt.Errorf("field:%s input data is empty list but required", inputParamDef.Name)
			}
			if t.Len() != 1 {
				return nil, fmt.Errorf("field:%s input data len=%d but trying to convert to single value", t.Len())
			}
			tv := reflect.ValueOf(value)
			// 列表转单值
			valueToSingle := tv.Index(0).Interface()
			tToSingle := reflect.TypeOf(valueToSingle)
			if inputParamDef.DataType == models.PluginParamDataTypeInt {
				convValue, err := convertToDatatypeInt(inputParamDef.Name, valueToSingle, tToSingle)
				if err != nil {
					return nil, err
				}
				// 转换为列表
				if inputParamDef.Multiple == "Y" {
					result = []interface{}{convValue}
				}
			} else if inputParamDef.DataType == models.PluginParamDataTypeString {
				convValue, err := convertToDatatypeString(inputParamDef.Name, valueToSingle, tToSingle)
				if err != nil {
					return nil, err
				}
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
			// 转换为列表
			if inputParamDef.Multiple == "Y" {
				result = []interface{}{convValue}
			}
		} else if inputParamDef.DataType == models.PluginParamDataTypeString {
			convValue, err := convertToDatatypeString(inputParamDef.Name, value, t)
			if err != nil {
				return nil, err
			}
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

func performDangerousCheck(ctx context.Context, pluginCallParam interface{}, continueToken string) (result *models.ItsdangerousCheckResultData, err error) {
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
	// TODO: 调用高危命令需要获取subsystem token
	token := ""
	// 调用检查
	result, err = remote.DangerousBatchCheck(ctx, token)
	return
}
