package execution

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/cipher"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"go.uber.org/zap"
	"reflect"
	"strconv"
	"strings"
	"time"

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
	// 数据是否失败，失败的话回写
	FailFlag bool
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
	pluginInterface, errGet := database.GetPluginConfigInterfaceById(pluginInterfaceId, false)
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
	if errAnalyze1 != nil {
		err = errAnalyze1
		return
	}
	if len(rootExprList) == 0 {
		err = fmt.Errorf("invalid input entity type %s", entityType)
		return
	}
	rootExpr := rootExprList[len(rootExprList)-1]
	// 构造输入参数
	inputParamDatas, errHandle := handleInputData(ctx, authToken, continueToken, entityInstances, pluginInterface.InputParameters, rootExpr, inputConstantMap, nil, &models.ProcInsNodeReq{})
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
	//subsysToken := remote.GetToken()
	dangerousResult, errDangerous := performBatchDangerousCheck(ctx, itsdangerousCallParam, continueToken, remote.GetToken())
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
	pluginCallParam.RequestId = "batchexec_" + guid.CreateGuid()
	pluginCallResult, _, errCall := remote.PluginInterfaceApi(ctx, remote.GetToken(), pluginInterface, pluginCallParam)
	if errCall != nil {
		err = errCall
		return
	}
	// 处理output param(比如类型转换，数据模型写入), handleOutputData主要是用于格式化为output param定义的字段
	_, errHandle = handleOutputData(ctx, remote.GetToken(), pluginCallResult.Outputs, pluginInterface.OutputParameters, &models.ProcInsNodeReq{}, false)
	if errHandle != nil {
		err = errHandle
		return
	}
	// 批量执行需要返回原始插件结果，而不是格式化output字段的值
	result = pluginCallResult
	return
}

// normalizePluginInterfaceParamData
// 根据参数定义的 dataType和multiple 来做值转换
// dataType -> int | string | object | list
// multiple -> Y | N
func normalizePluginInterfaceParamData(ctxParam *models.HandleProcessInputDataParam, inputParamDef *models.PluginConfigInterfaceParameters, value interface{}) (interface{}, error) {
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
	log.Debug(nil, log.LOGGER_APP, "normalizePluginInterfaceParamData", zap.String("key", inputParamDef.Name), zap.String("valueType", t.Kind().String()), zap.String("value", fmt.Sprintf("%v", value)))
	// value的kind可能是[]{int, string, float, object}, int, string, float, object
	if t.Kind() == reflect.Slice {
		if inputParamDef.DataType == models.PluginParamDataTypeObject {
			if inputParamDef.RefObjectName == "" {
				if inputParamDef.Multiple == "Y" {
					result = value
				} else {
					tv := reflect.ValueOf(value)
					if tv.Len() > 0 {
						result = tv.Index(0).Interface()
					}
				}
				return result, nil
			}
			if inputParamDef.Multiple == "Y" {
				if inputParamDef.RefObjectMeta != nil {
					tv := reflect.ValueOf(value)
					var resultList []interface{}
					var err error
					for i := 0; i < tv.Len(); i++ {
						tmpResult, tmpErr := recursiveObjectParamData(ctxParam, inputParamDef.RefObjectMeta, tv.Index(i).Interface())
						if tmpErr != nil {
							err = fmt.Errorf("recursiveObjectParamData field:%s fail,%s ", inputParamDef.Name, tmpErr.Error())
							break
						} else {
							resultList = append(resultList, tmpResult)
						}
					}
					return resultList, err
				} else {
					return nil, fmt.Errorf("field:%s dataType is object but can not find refOject:%s ", inputParamDef.Name, inputParamDef.RefObjectName)
				}
			} else {
				return nil, fmt.Errorf("field:%s dataType is not multiple object but get multiple value", inputParamDef.Name)
			}
		}
		if inputParamDef.Multiple == "Y" || inputParamDef.DataType == models.PluginParamDataTypeList {
			// FIXME： 列表元素类型转换
			result = value
		} else {
			tv := reflect.ValueOf(value)
			if tv.Len() == 0 && inputParamDef.Required == "Y" {
				return nil, fmt.Errorf("field:%s input data is empty list but required", inputParamDef.Name)
			}
			if tv.Len() == 0 {
				result = nil
				return result, nil
			}
			if tv.Len() != 1 {
				return nil, fmt.Errorf("field:%s input data len=%d but trying to convert to single value,inputValue:%v ", inputParamDef.Name, tv.Len(), value)
			}
			// 列表转单值
			valueToSingle := tv.Index(0).Interface()
			if valueToSingle == nil {
				result = valueToSingle
			} else {
				if valueToSingleStr, ok := valueToSingle.(string); ok {
					if inputParamDef.Required == "Y" && strings.TrimSpace(valueToSingleStr) == "" {
						return nil, fmt.Errorf("field:%s can not be empty value", inputParamDef.Name)
					}
				}
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
					result = value
					//return nil, fmt.Errorf("field:%s can not convert %v to object", inputParamDef.Name, tToSingle)
				} else if inputParamDef.DataType == models.PluginParamDataTypeList {
					result = value
					//return nil, fmt.Errorf("field:%s can not convert %v to list", inputParamDef.Name, tToSingle)
				}
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
		if valueToSingleStr, ok := value.(string); ok {
			if inputParamDef.Required == "Y" && strings.TrimSpace(valueToSingleStr) == "" {
				return nil, fmt.Errorf("field:%s can not be empty value", inputParamDef.Name)
			}
		}
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
			result = value
			//return nil, fmt.Errorf("field:%s can not convert %v to object", inputParamDef.Name, t)
		} else if inputParamDef.DataType == models.PluginParamDataTypeList {
			result = value
			//return nil, fmt.Errorf("field:%s can not convert %v to list", inputParamDef.Name, t)
		}
	}
	return result, nil
}

func recursiveObjectParamData(ctxParam *models.HandleProcessInputDataParam, refObjectMeta *models.CoreObjectMeta, inputRowData interface{}) (result map[string]interface{}, err error) {
	result = make(map[string]interface{})
	for _, inputDef := range refObjectMeta.PropertyMetas {
		var inputCalResult interface{}
		switch inputDef.MappingType {
		case models.PluginParamMapTypeConstant:
			if inputDef.MappingVal != "" {
				inputCalResult = inputDef.MappingVal
			} else {
				inputCalResult = ctxParam.InputConstantMap[inputDef.Id]
			}
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
			err = fmt.Errorf("input param %s is map to %s, not supported", inputDef.Name, inputDef.MappingType)
			return
		case models.PluginParamMapTypeEntity:
			entityRowDataId := ""
			if rowDataId, ok := inputRowData.(string); !ok {
				err = fmt.Errorf("input param %s entity type can not find row data id,rowData:%v ", inputDef.Name, inputRowData)
				return
			} else {
				entityRowDataId = rowDataId
			}
			// 从数据模型获取
			if inputDef.MapExpr != "" {
				inputDef.MappingEntityExpression = inputDef.MapExpr
			}
			if inputDef.MappingEntityExpression == "" {
				err = fmt.Errorf("input param %s is map to %s, but entity expression is empty", inputDef.Name, inputDef.MappingType)
				return
			}
			execExprList, errAnalyze2 := remote.AnalyzeExpression(inputDef.MappingEntityExpression)
			if err != nil {
				err = errAnalyze2
				return
			}
			if len(execExprList) == 0 {
				err = fmt.Errorf("input param %s entity expression %s illegal", inputDef.Name, inputDef.MappingEntityExpression)
				return
			}
			execExprFilterList := make([]*models.QueryExpressionDataFilter, 0)
			execExprFilter := &models.QueryExpressionDataFilter{
				PackageName:      execExprList[0].Package,
				EntityName:       execExprList[0].Entity,
				AttributeFilters: make([]*models.QueryExpressionDataAttrFilter, 0),
			}
			execExprFilter.AttributeFilters = append(execExprFilter.AttributeFilters, &models.QueryExpressionDataAttrFilter{
				Name:     "id",
				Operator: "eq",
				Value:    entityRowDataId,
			})
			execExprFilterList = append(execExprFilterList, execExprFilter)
			execExprResult, errExec := remote.QueryPluginData(ctxParam.Ctx, execExprList, execExprFilterList, ctxParam.AuthToken)
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
		transInputDef := &models.PluginConfigInterfaceParameters{
			Name:                      inputDef.Name,
			DataType:                  inputDef.DataType,
			MappingType:               inputDef.MappingType,
			MappingEntityExpression:   inputDef.MappingEntityExpression,
			MappingSystemVariableName: inputDef.MappingSystemVariableName,
			SensitiveData:             inputDef.SensitiveData,
			MappingVal:                inputDef.MappingVal,
			Multiple:                  inputDef.Multiple,
			RefObjectName:             inputDef.RefObjectName,
			RefObjectMeta:             inputDef.RefObjectMeta,
		}
		result[inputDef.Name], err = normalizePluginInterfaceParamData(ctxParam, transInputDef, inputCalResult)
		if err != nil {
			return
		}
	}
	return
}

// handle inputData
func normalizeInputParamData(
	inputParamDef *models.PluginConfigInterfaceParameters,
	value interface{},
	ctx context.Context,
	inputConstantMap map[string]string,
	entityInstance *models.BatchExecutionPluginExecEntityInstances,
	inputContextMap map[string]interface{},
	rootExpr *models.ExpressionObj,
	authToken string) (interface{}, error) {
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
	log.Debug(nil, log.LOGGER_APP, "normalizeInputParamData", zap.String("key", inputParamDef.Name), zap.String("valueType", t.Kind().String()), zap.String("value", fmt.Sprintf("%v", value)))
	// value的kind可能是[]{int, string, float, object}, int, string, float, object
	if t.Kind() == reflect.Slice {
		if inputParamDef.Multiple == "Y" && inputParamDef.DataType == models.PluginParamDataTypeList {
			// FIXME： 列表元素类型转换
			result = value
		} else {
			tv := reflect.ValueOf(value)
			if tv.Len() == 0 && inputParamDef.Required == "Y" {
				return nil, fmt.Errorf("field:%s input data is empty list but required", inputParamDef.Name)
			}
			if tv.Len() == 0 {
				result = nil
				return result, nil
			}
			if tv.Len() != 1 && inputParamDef.Multiple != "Y" {
				return nil, fmt.Errorf("field:%s input data len=%d but trying to convert to single value,inputValue:%v ", inputParamDef.Name, tv.Len(), value)
			}

			// 列表转单值
			valueToSingle := tv.Index(0).Interface()
			if valueToSingle == nil {
				result = valueToSingle
			} else {
				if valueToSingleStr, ok := valueToSingle.(string); ok {
					if inputParamDef.Required == "Y" && strings.TrimSpace(valueToSingleStr) == "" {
						return nil, fmt.Errorf("field:%s can not be empty value", inputParamDef.Name)
					}
				}
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
					// result = value
					convValue, err := convertToDatatypeObjectForInputData(ctx, inputParamDef.Name, value, inputParamDef.RefObjectMeta, inputConstantMap, entityInstance, inputContextMap, rootExpr, authToken)
					if err != nil {
						return nil, err
					}
					result = convValue
					//return nil, fmt.Errorf("field:%s can not convert %v to object", inputParamDef.Name, tToSingle)
				} else if inputParamDef.DataType == models.PluginParamDataTypeList {
					result = value
					//return nil, fmt.Errorf("field:%s can not convert %v to list", inputParamDef.Name, tToSingle)
				}
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
		if valueToSingleStr, ok := value.(string); ok {
			if inputParamDef.Required == "Y" && strings.TrimSpace(valueToSingleStr) == "" {
				return nil, fmt.Errorf("field:%s can not be empty value", inputParamDef.Name)
			}
		}
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
			// result = value
			convValue, err := convertToDatatypeObjectForInputData(ctx, inputParamDef.Name, value, inputParamDef.RefObjectMeta, inputConstantMap, entityInstance, inputContextMap, rootExpr, authToken)
			if err != nil {
				return nil, err
			}
			result = convValue
			//return nil, fmt.Errorf("field:%s can not convert %v to object", inputParamDef.Name, t)
		} else if inputParamDef.DataType == models.PluginParamDataTypeList {
			result = value
			//return nil, fmt.Errorf("field:%s can not convert %v to list", inputParamDef.Name, t)
		}
	}
	return result, nil
}

func convertToDatatypeObjectForInputData(
	ctx context.Context,
	name string,
	value interface{},
	refObjectMeta *models.CoreObjectMeta,
	inputConstantMap map[string]string,
	entityInstance *models.BatchExecutionPluginExecEntityInstances,
	inputContextMap map[string]interface{},
	rootExpr *models.ExpressionObj,
	authToken string) (result []map[string]interface{}, err error) {
	if refObjectMeta == nil {
		err = fmt.Errorf("field:%s can not convert %v to object, refObjectMeta is nil", name, value)
		return
	}

	if len(refObjectMeta.PropertyMetas) == 0 {
		err = fmt.Errorf("field:%s can not convert %v to object, refObjectMeta.PropertyMetas is empty", name, value)
		return
	}

	objectParamDefs := make([]*models.PluginConfigInterfaceParameters, 0, len(refObjectMeta.PropertyMetas))
	for _, propertyMeta := range refObjectMeta.PropertyMetas {
		objParamDef := &models.PluginConfigInterfaceParameters{
			Name:                    propertyMeta.Name,
			DataType:                propertyMeta.DataType,
			Multiple:                propertyMeta.Multiple,
			SensitiveData:           propertyMeta.SensitiveData,
			MappingType:             propertyMeta.MappingType,
			MappingEntityExpression: propertyMeta.MapExpr,
			RefObjectMeta:           propertyMeta.RefObjectMeta,
			RefObjectName:           propertyMeta.RefObjectName,
		}
		objectParamDefs = append(objectParamDefs, objParamDef)
	}

	var valueList []string
	if tmpValList, isOk := value.([]interface{}); isOk {
		for _, val := range tmpValList {
			valueList = append(valueList, val.(string))
		}
	} else {
		valueList = append(valueList, value.(string))
	}

	for _, inputDef := range objectParamDefs {
		// procReqParamObj := models.ProcInsNodeReqParam{ParamDefId: inputDef.Id, ReqId: procInsNodeReq.Id, DataIndex: dataIndex, FromType: "input", Name: inputDef.Name, DataType: inputDef.DataType, MappingType: inputDef.MappingType}
		// if inputDef.SensitiveData == "Y" {
		// 	procReqParamObj.IsSensitive = true
		// }
		// if inputDef.Multiple == "Y" {
		// 	procReqParamObj.Multiple = true
		// }
		var inputCalResult interface{}
		switch inputDef.MappingType {
		case models.PluginParamMapTypeConstant:
			if inputDef.MappingVal != "" {
				inputCalResult = inputDef.MappingVal
			} else {
				inputCalResult = inputConstantMap[inputDef.Id]
			}
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
			tmpCtxDataMatchFlag := false
			if entityInstance.ContextMap != nil {
				if tmpCtxValue, ctxOk := entityInstance.ContextMap[inputDef.Name]; ctxOk {
					tmpCtxDataMatchFlag = true
					inputCalResult = tmpCtxValue
				}
			}
			if !tmpCtxDataMatchFlag {
				if inputContextMap == nil {
					err = fmt.Errorf("input param %s is map to %s, which batch execution is not supported", inputDef.Name, inputDef.MappingType)
					return
				}
				inputCalResult = inputContextMap[inputDef.Name]
			}
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
			// lastExprObj := execExprList[len(execExprList)-1]
			// procReqParamObj.EntityTypeId = fmt.Sprintf("%s:%s", lastExprObj.Package, lastExprObj.Entity)
			// procReqParamObj.EntityDataId, procReqParamObj.FullDataId = getExprDataIdString(execExprResult, entityInstance.Id)
			inputCalResult = remote.ExtractExpressionResultColumn(execExprList, execExprResult)
		case models.PluginParamMapTypeObject:
			// TODO: 从指定对象获取暂不支持(k8s)
			err = fmt.Errorf("input param %s is map to %s, which batch execution is not supported", inputDef.Name, inputDef.MappingType)
			return
		}
		// inputParamData[inputDef.Name], err = normalizePluginInterfaceParamData(inputDef, inputCalResult, ctx, inputConstantMap, entityInstance, inputContextMap, rootExpr, authToken)
		// if err != nil {
		// 	return
		// }
		// if procReqParamObj.IsSensitive {
		// 	inputParamData[inputDef.Name] = buildSensitiveData(inputParamData[inputDef.Name], entityInstance.Id)
		// }
		// procReqParamObj.DataValue = fmt.Sprintf("%v", inputParamData[inputDef.Name])
		// procReqParamObj.CallbackId = entityInstance.Id
		// procInsNodeReq.Params = append(procInsNodeReq.Params, &procReqParamObj)

		/*
			result[inputDef.Name], err = normalizeInputParamData(inputDef, inputCalResult, ctx, inputConstantMap, entityInstance, inputContextMap, rootExpr, authToken)
			if err != nil {
				return
			}
			if inputDef.SensitiveData == "Y" {
				result[inputDef.Name] = buildSensitiveData(result[inputDef.Name], entityInstance.Id)
			}
		*/
		_ = inputCalResult
	}
	return
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

func performBatchDangerousCheck(ctx context.Context, pluginCallParam interface{}, continueToken string, authToken string) (result *models.ItsdangerousBatchCheckResultData, err error) {
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
	rootExpr *models.ExpressionObj,
	inputConstantMap map[string]string,
	inputContextMap map[string]interface{}, procInsNodeReq *models.ProcInsNodeReq) (inputParamDatas []models.BatchExecutionPluginExecInputParams, err error) {
	inputParamDatas = make([]models.BatchExecutionPluginExecInputParams, 0)
	ctxParam := models.HandleProcessInputDataParam{Ctx: ctx, AuthToken: authToken, InputConstantMap: inputConstantMap, InputContextMap: inputContextMap}
	for dataIndex, entityInstance := range entityInstances {
		// batch inputParamData = {"callbackParameter":"entity instance id", "confirmToken":"Y", xml props}
		inputParamData := models.BatchExecutionPluginExecInputParams{}
		for _, inputDef := range inputParamDefs {
			procReqParamObj := models.ProcInsNodeReqParam{ParamDefId: inputDef.Id, ReqId: procInsNodeReq.Id, DataIndex: dataIndex, FromType: "input", Name: inputDef.Name, DataType: inputDef.DataType, MappingType: inputDef.MappingType}
			if inputDef.SensitiveData == "Y" {
				procReqParamObj.IsSensitive = true
			}
			if inputDef.Multiple == "Y" {
				procReqParamObj.Multiple = true
			}
			var inputCalResult interface{}
			switch inputDef.MappingType {
			case models.PluginParamMapTypeConstant:
				if inputDef.MappingVal != "" {
					inputCalResult = inputDef.MappingVal
				} else {
					inputCalResult = inputConstantMap[inputDef.Id]
				}
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
				tmpCtxDataMatchFlag := false
				if entityInstance.ContextMap != nil {
					if tmpCtxValue, ctxOk := entityInstance.ContextMap[inputDef.Name]; ctxOk {
						tmpCtxDataMatchFlag = true
						inputCalResult = tmpCtxValue
					}
				}
				if !tmpCtxDataMatchFlag {
					if inputContextMap == nil {
						err = fmt.Errorf("input param %s is map to %s, which batch execution is not supported", inputDef.Name, inputDef.MappingType)
						return
					}
					inputCalResult = inputContextMap[inputDef.Name]
				}
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
				if len(execExprList) == 0 {
					err = fmt.Errorf("entity expr is empty")
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
				lastExprObj := execExprList[len(execExprList)-1]
				procReqParamObj.EntityTypeId = fmt.Sprintf("%s:%s", lastExprObj.Package, lastExprObj.Entity)
				procReqParamObj.EntityDataId, procReqParamObj.FullDataId = getExprDataIdString(execExprResult, entityInstance.Id)
				inputCalResult = remote.ExtractExpressionResultColumn(execExprList, execExprResult)
			case models.PluginParamMapTypeObject:
				// TODO: 从指定对象获取暂不支持(k8s)
				err = fmt.Errorf("input param %s is map to %s, which batch execution is not supported", inputDef.Name, inputDef.MappingType)
				return
			}
			inputParamData[inputDef.Name], err = normalizePluginInterfaceParamData(&ctxParam, inputDef, inputCalResult)
			if err != nil {
				return
			}
			//if procReqParamObj.IsSensitive {
			//	inputParamData[inputDef.Name] = buildSensitiveData(inputParamData[inputDef.Name], entityInstance.Id)
			//}
			if inputDef.DataType == models.PluginParamDataTypeObject {
				objectJsonBytes, marshalErr := json.Marshal(inputParamData[inputDef.Name])
				if marshalErr == nil {
					procReqParamObj.DataValue = string(objectJsonBytes)
				} else {
					log.Warn(nil, log.LOGGER_APP, "json marshal object data value fail", zap.Error(marshalErr))
					procReqParamObj.DataValue = fmt.Sprintf("%v", inputParamData[inputDef.Name])
				}
			} else {
				procReqParamObj.DataValue = fmt.Sprintf("%v", inputParamData[inputDef.Name])
			}
			procReqParamObj.CallbackId = entityInstance.Id
			procInsNodeReq.Params = append(procInsNodeReq.Params, &procReqParamObj)
		}
		// NOTE: 仅批量执行的PluginCallParamPresetCallback是行ID，编排插件调用时是随机ID与行数据没有关联
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
	outputParamDefs []*models.PluginConfigInterfaceParameters, procInsNodeReq *models.ProcInsNodeReq, failFlag bool) (result *models.PluginInterfaceApiResultData, err error) {
	tmpResult := &models.PluginInterfaceApiResultData{Outputs: make([]map[string]interface{}, 0)}
	tmpResultForEntity := &OutputEntityData{Data: make([]*OutputEntityRootData, 0)}
	reqInputParamIndexMap := make(map[string]int)
	ctxParam := models.HandleProcessInputDataParam{Ctx: ctx, AuthToken: authToken}
	for _, v := range procInsNodeReq.Params {
		if v.FromType == "input" {
			reqInputParamIndexMap[v.CallbackId] = v.DataIndex
		}
	}
	for dataIndex, output := range outputs {
		tmpResultOutput := make(map[string]interface{})
		var tmpResultOutputForEntity *OutputEntityRootData
		tmpResultForPackageName := ""
		tmpResultForEntityName := ""
		defKeyMap := make(map[string]int)
		for _, outputDef := range outputParamDefs {
			procReqParamObj := models.ProcInsNodeReqParam{ParamDefId: outputDef.Id, ReqId: procInsNodeReq.Id, DataIndex: dataIndex, FromType: "output", Name: outputDef.Name, DataType: outputDef.DataType, MappingType: outputDef.MappingType}
			if outputDef.SensitiveData == "Y" {
				procReqParamObj.IsSensitive = true
			}
			if outputDef.Multiple == "Y" {
				procReqParamObj.Multiple = true
			}
			defKeyMap[outputDef.Name] = 1
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
					outputCalResultConv, errConv := normalizePluginInterfaceParamData(&ctxParam, outputDef, outputCalResult)
					if errConv != nil {
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
				lastExprObj := execExprList[len(execExprList)-1]
				procReqParamObj.EntityTypeId = fmt.Sprintf("%s:%s", lastExprObj.Package, lastExprObj.Entity)
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
					outputCalResultConv, errConv := normalizePluginInterfaceParamData(&ctxParam, outputDef, outputCalResult)
					if errConv != nil {
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
			tmpResultOutput[outputDef.Name], err = normalizePluginInterfaceParamData(&ctxParam, outputDef, outputCalResult)
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
				procReqParamObj.CallbackId = fmt.Sprintf("%s", v)
				if outputIndex, matchOutputIndex := reqInputParamIndexMap[procReqParamObj.CallbackId]; matchOutputIndex {
					procReqParamObj.DataIndex = outputIndex
				}
			}
			if v, ok := output[models.PluginCallResultPresetErrorCode]; ok {
				tmpResultOutput[models.PluginCallResultPresetErrorCode] = v
				if fmt.Sprintf("%s", v) != "0" {
					if tmpResultOutputForEntity == nil {
						tmpResultOutputForEntity = &OutputEntityRootData{Data: make(map[string]interface{}), SubBranchs: make([]*OutputEntityBranchData, 0)}
					}
					tmpResultOutputForEntity.FailFlag = true
				}
			}
			if v, ok := output[models.PluginCallResultPresetErrorMsg]; ok {
				tmpResultOutput[models.PluginCallResultPresetErrorMsg] = v
			}
			//if procReqParamObj.IsSensitive {
			//	tmpResultOutput[outputDef.Name] = buildSensitiveData(tmpResultOutput[outputDef.Name], procReqParamObj.CallbackId)
			//}
			procReqParamObj.DataValue = fmt.Sprintf("%v", tmpResultOutput[outputDef.Name])
			procInsNodeReq.Params = append(procInsNodeReq.Params, &procReqParamObj)
		}
		for outKey, outVal := range output {
			if _, isDefOk := defKeyMap[outKey]; !isDefOk {
				procReqParamObj := models.ProcInsNodeReqParam{ReqId: procInsNodeReq.Id, DataIndex: dataIndex, FromType: "output", Name: outKey, DataType: "string", MappingType: "constant"}
				procReqParamObj.DataValue = fmt.Sprintf("%v", outVal)
				if v, ok := output[models.PluginCallResultPresetCallback]; ok {
					procReqParamObj.CallbackId = fmt.Sprintf("%s", v)
					if outputIndex, matchOutputIndex := reqInputParamIndexMap[procReqParamObj.CallbackId]; matchOutputIndex {
						procReqParamObj.DataIndex = outputIndex
					}
				}
				procInsNodeReq.Params = append(procInsNodeReq.Params, &procReqParamObj)
			}
		}
		tmpResult.Outputs = append(tmpResult.Outputs, tmpResultOutput)
		if tmpResultForPackageName != "" && tmpResultForEntityName != "" {
			tmpResultForEntity.Package = tmpResultForPackageName
			tmpResultForEntity.Entity = tmpResultForEntityName
			if tmpResultOutputForEntity != nil {
				if v, ok := tmpResultOutputForEntity.Data["id"]; ok {
					if v != nil {
						if reflect.TypeOf(v).String() == "string" {
							tmpResultOutputForEntity.Id = v.(string)
						}
					}
				} else {
					if guidV, guidOk := tmpResultOutputForEntity.Data["guid"]; guidOk {
						if guidV != nil {
							if reflect.TypeOf(guidV).String() == "string" {
								tmpResultOutputForEntity.Id = guidV.(string)
							}
						}
					}
				}
				tmpResultForEntity.Data = append(tmpResultForEntity.Data, tmpResultOutputForEntity)
			}
		}
	}
	// 处理entity写入
	for _, rootData := range tmpResultForEntity.Data {
		if rootData.FailFlag {
			continue
		}
		if rootData.Id == "" {
			ret, errCreate := remote.CreateEntityData(ctx, authToken, tmpResultForEntity.Package, tmpResultForEntity.Entity, rootData.Data)
			if errCreate != nil {
				log.Error(nil, log.LOGGER_APP, fmt.Sprintf("failed to create %s entity %s %v", tmpResultForEntity.Package, tmpResultForEntity.Entity, rootData.Data))
				err = errCreate
				return
			} else {
				// 成功需要回写ID，以便后续Branch数据更新使用
				rootData.Id = ret["id"].(string)
				log.Debug(nil, log.LOGGER_APP, fmt.Sprintf("create %s entity %s[%s] %v", tmpResultForEntity.Package, tmpResultForEntity.Entity, rootData.Id, rootData.Data))
			}
		} else {
			// 除了ID以外有其他值才需要写入
			if len(rootData.Data) > 1 {
				_, errUpdate := remote.UpdateEntityData(ctx, authToken, tmpResultForEntity.Package, tmpResultForEntity.Entity, rootData.Data)
				if errUpdate != nil {
					log.Error(nil, log.LOGGER_APP, fmt.Sprintf("failed to update %s entity %s[%s] %v", tmpResultForEntity.Package, tmpResultForEntity.Entity, rootData.Id, rootData.Data))
					err = errUpdate
					return
				} else {
					log.Debug(nil, log.LOGGER_APP, fmt.Sprintf("update %s entity %s[%s] %v", tmpResultForEntity.Package, tmpResultForEntity.Entity, rootData.Id, rootData.Data))
				}
			}
		}
		// 处理多级表达式数据更新
		for _, branchData := range rootData.SubBranchs {
			errUpdate := remote.UpdatentityDataWithExpr(ctx, authToken, tmpResultForEntity.Package, tmpResultForEntity.Entity, rootData.Id, branchData.Exprs, branchData.Data)
			if errUpdate != nil {
				log.Error(nil, log.LOGGER_APP, fmt.Sprintf("failed to update %s entity %s[%s] with expression %s %v", tmpResultForEntity.Package, tmpResultForEntity.Entity, rootData.Id, branchData.OriginExpr, branchData.Data))
				err = errUpdate
				return
			} else {
				log.Debug(nil, log.LOGGER_APP, fmt.Sprintf("update %s entity %s[%s] with expression %s %v", tmpResultForEntity.Package, tmpResultForEntity.Entity, rootData.Id, branchData.OriginExpr, branchData.Data))
			}
		}
	}
	result = tmpResult
	return
}

func getExprDataIdString(queryResult []map[string]interface{}, parentId string) (entityDataId, fullDataId string) {
	idList := []string{}
	fullIdList := []string{}
	for _, row := range queryResult {
		if v, b := row["id"]; b {
			tmpId := v.(string)
			idList = append(idList, tmpId)
			if tmpId != parentId {
				fullIdList = append(fullIdList, fmt.Sprintf("%s::%s", parentId, tmpId))
			} else {
				fullIdList = append(fullIdList, tmpId)
			}
		}
	}
	entityDataId = strings.Join(idList, ",")
	fullDataId = strings.Join(fullIdList, ",")
	return
}

func buildSensitiveData(inputValue interface{}, dataId string) (output string) {
	ctx := db.DBCtx(fmt.Sprintf("%d", time.Now().Unix()))
	inputString := fmt.Sprintf("%v", inputValue)
	output = inputString
	if inputString == "" {
		return
	}
	seed, err := database.GetEncryptSeed(ctx)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "buildSensitiveData fail with get seed error", zap.Error(err))
		return
	}
	output, err = cipher.AesEnPasswordByGuid(dataId, seed, inputString, "")
	if err != nil {
		output = inputString
		log.Error(nil, log.LOGGER_APP, "buildSensitiveData aes encrypt fail", zap.Error(err))
	}
	return
}
