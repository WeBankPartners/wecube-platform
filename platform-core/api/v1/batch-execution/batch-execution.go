package batch_execution

import (
	"encoding/json"
	"fmt"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/try"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/execution"
	"github.com/gin-gonic/gin"
)

// CreateOrUpdateTemplate 创建/更新批量执行模板
func CreateOrUpdateTemplate(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Logger.Error(e.(string))
	})

	reqParam := models.BatchExecutionTemplate{}
	var err error
	if err = c.ShouldBindJSON(&reqParam); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if len(reqParam.PermissionToRole.MGMT) == 0 {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err, MGMT permission role can not be empty"))
		middleware.ReturnError(c, err)
		return
	}

	retData, err := database.CreateOrUpdateBatchExecTemplate(c, &reqParam)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, retData)
	}
	return
}

// CollectTemplate 收藏批量执行模板
func CollectTemplate(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Logger.Error(e.(string))
	})

	reqParam := models.BatchExecutionTemplateCollect{}
	var err error
	if err = c.ShouldBindJSON(&reqParam); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}

	if reqParam.BatchExecutionTemplateId == "" {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err, batchExecutionTemplateId can not be empty"))
		middleware.ReturnError(c, err)
		return
	}

	err = database.CollectBatchExecTemplate(c, &reqParam)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnSuccess(c)
	return
}

// UncollectTemplate 取消收藏批量执行模板
func UncollectTemplate(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Logger.Error(e.(string))
	})

	reqParam := models.BatchExecutionTemplateCollect{}
	var err error
	if err = c.ShouldBindJSON(&reqParam); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}

	if reqParam.BatchExecutionTemplateId == "" {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err, batchExecutionTemplateId can not be empty"))
		middleware.ReturnError(c, err)
		return
	}

	err = database.UncollectBatchExecTemplate(c, &reqParam)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnSuccess(c)
	return
}

// CheckCollectTemplate 检查是否收藏了批量执行模板
func CheckCollectTemplate(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Logger.Error(e.(string))
	})

	reqParam := models.BatchExecutionTemplateCollect{}
	var err error
	if err = c.ShouldBindJSON(&reqParam); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}

	if reqParam.BatchExecutionTemplateId == "" {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err, batchExecutionTemplateId can not be empty"))
		middleware.ReturnError(c, err)
		return
	}

	retData, err := database.CheckCollectBatchExecTemplate(c, &reqParam)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, retData)
	}
	return
}

// RetrieveTemplate 查询批量执行模板列表
func RetrieveTemplate(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Logger.Error(e.(string))
	})

	var param models.QueryRequestParam
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}

	if len(param.Sorting) == 0 {
		param.Sorting = append(param.Sorting, &models.QueryRequestSorting{Field: "updatedTimeT", Asc: false})
	}
	retData, err := database.RetrieveTemplate(c, &param)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, retData)
	}
	return
}

// GetTemplate 查询批量执行模板详情
func GetTemplate(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Logger.Error(e.(string))
	})

	templateId := c.Param("templateId")
	if templateId == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("templateId cannot be empty")))
		return
	}
	retData, err := database.GetTemplate(c, templateId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, retData)
	}
	return
}

// DeleteTemplate 删除批量执行模板
func DeleteTemplate(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Logger.Error(e.(string))
	})

	templateId := c.Param("templateId")
	if templateId == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("templateId cannot be empty")))
		return
	}
	err := database.DeleteTemplate(c, templateId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnSuccess(c)
	}
	return
}

// UpdateTemplatePermission 更新批量执行模板的权限
func UpdateTemplatePermission(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Logger.Error(e.(string))
	})

	reqParam := models.BatchExecutionTemplate{}
	var err error
	if err = c.ShouldBindJSON(&reqParam); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if reqParam.Id == "" {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err, id can not be empty"))
		middleware.ReturnError(c, err)
		return
	}
	if len(reqParam.PermissionToRole.MGMT) == 0 {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err, MGMT permission role can not be empty"))
		middleware.ReturnError(c, err)
		return
	}

	retData, err := database.UpdateTemplatePermission(c, &reqParam)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, retData)
	}
	return
}

// RetrieveBatchExec 查询批量执行结果列表
func RetrieveBatchExec(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Logger.Error(e.(string))
	})

	var param models.QueryRequestParam
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}

	userId := middleware.GetRequestUser(c)
	param.Filters = append(param.Filters, &models.QueryRequestFilterObj{
		Name:     "createdBy",
		Operator: "eq",
		Value:    userId,
	})

	if len(param.Sorting) == 0 {
		param.Sorting = append(param.Sorting, &models.QueryRequestSorting{Field: "updatedTimeT", Asc: false})
		param.Sorting = append(param.Sorting, &models.QueryRequestSorting{Field: "id", Asc: true})
	}

	retData, err := database.RetrieveBatchExec(c, &param)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, retData)
	}
	return
}

// GetBatchExec 查询批量执行结果详情
func GetBatchExec(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Logger.Error(e.(string))
	})

	var err error
	batchExecId := c.Param("batchExecId")
	if batchExecId == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("batchExecId cannot be empty")))
		return
	}

	retData, err := database.GetBatchExec(c, batchExecId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, retData)
	}
	return
}

func ValidateRunJobParams(reqParam *models.BatchExecRun) (err error) {
	if reqParam.PluginConfigInterface == nil {
		err = fmt.Errorf("reqParam.PluginConfigInterface can not be nil")
		return
	}
	if reqParam.PluginConfigInterface.Id == "" {
		err = fmt.Errorf("reqParam.PluginConfigInterface.Id can not be empty")
		return
	}
	if reqParam.PluginConfigInterface.PluginConfigId == "" {
		err = fmt.Errorf("reqParam.PluginConfigInterface.PluginConfigId can not be empty")
		return
	}
	if reqParam.DataModelExpression == "" {
		err = fmt.Errorf("reqParam.DataModelExpression can not be empty")
		return
	}
	return
}

func ValidateRunJobPermission(c *gin.Context, userRoles []string, pluginConfigId string) (err error) {
	userRolesMap := make(map[string]struct{})
	for _, role := range userRoles {
		userRolesMap[role] = struct{}{}
	}

	pluginConfigData, tmpErr := database.GetPluginConfigsById(c, pluginConfigId)
	if tmpErr != nil {
		err = tmpErr
		return
	}

	pluginConfigRolesData, tmpErr := database.GetPluginConfigRoles(c, pluginConfigId)
	if tmpErr != nil {
		err = tmpErr
		return
	}

	isAuthorized := false
	for _, pluginCfgRole := range pluginConfigRolesData {
		if pluginCfgRole.PermType == models.PermissionTypeUSE {
			if _, isExisted := userRolesMap[pluginCfgRole.RoleName]; isExisted {
				isAuthorized = true
				break
			}
		}
	}
	if !isAuthorized {
		err = exterror.New().BatchExecPluginAuthError.WithParam(pluginConfigData.Name)
		return
	}
	return
}

// RunJob 批量执行
func RunJob(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Logger.Error(e.(string))
	})

	reqParam := models.BatchExecRun{}
	var err error
	if err = c.ShouldBindJSON(&reqParam); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}

	err = ValidateRunJobParams(&reqParam)
	if err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}

	err = ValidateRunJobPermission(c, middleware.GetRequestRoles(c), reqParam.PluginConfigInterface.PluginConfigId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}

	retData, err := doRunJob(c, &reqParam)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		if retData.DangerousCheckResult != nil {
			middleware.ReturnDataWithStatus(c, retData, models.DefaultHttpConfirmCode)
		} else {
			middleware.ReturnData(c, retData)
		}
	}
	return
}

func doRunJob(c *gin.Context, reqParam *models.BatchExecRun) (result *models.BatchExecRunResp, err error) {
	result = &models.BatchExecRunResp{}
	operator := middleware.GetRequestUser(c)
	authToken := c.GetHeader(models.AuthorizationHeader)
	continueToken := c.DefaultQuery(models.ContinueToken, "")
	pluginInterfaceId := reqParam.PluginConfigInterface.Id
	entityType := reqParam.DataModelExpression
	// 组装插件服务参数
	var entityInstances []*models.BatchExecutionPluginExecEntityInstances
	for _, resourceData := range reqParam.ResourceDatas {
		entityIns := &models.BatchExecutionPluginExecEntityInstances{
			Id:               resourceData.Id,
			BusinessKeyValue: resourceData.BusinessKeyValue,
		}
		entityInstances = append(entityInstances, entityIns)
	}

	var inputParamConstants []*models.BatchExecutionPluginDefInputParams
	for _, inputParam := range reqParam.InputParameterDefinitions {
		pluginDefInputParams := &models.BatchExecutionPluginDefInputParams{
			ParamId:     inputParam.InputParameter.Id,
			ParameValue: inputParam.InputParameterValue,
		}
		inputParamConstants = append(inputParamConstants, pluginDefInputParams)
	}

	var batchExecId string
	var tmpErr error
	if continueToken == "" {
		// record batch execution，continueToken 为空，写入批量执行记录
		batchExecId, tmpErr = database.InsertBatchExec(c, reqParam)
		if tmpErr != nil {
			err = tmpErr
			log.Logger.Error("insert batch execution record failed", log.Error(err))
			return
		}
	} else {
		// continueToken 不为空，获取批量执行记录详情
		batchExecId = reqParam.BatchExecId
		queryBatchExecData, tmpErr := database.GetBatchExec(c, batchExecId)
		if tmpErr != nil {
			err = tmpErr
			log.Logger.Error(fmt.Sprintf("validate batchExecId: %s failed", batchExecId), log.Error(err))
			return
		}
		if queryBatchExecData.ErrorCode != models.BatchExecErrorCodeDangerousBlock {
			errMsg := fmt.Sprintf("batchExecId: %s has been finished", batchExecId)
			err = fmt.Errorf(errMsg)
			log.Logger.Error(errMsg)
			return
		}
	}
	result.BatchExecId = batchExecId

	execTime := time.Now()
	errCode := models.BatchExecErrorCodeSucceed
	errMsg := ""
	batchExecRunResult, dangerousCheckResult, pluginCallParam, err := execution.BatchExecutionCallPluginService(c, operator, authToken,
		pluginInterfaceId, entityType, entityInstances, inputParamConstants, continueToken)
	if err != nil {
		errCode = models.BatchExecErrorCodeFailed
		errMsg = fmt.Sprintf("plugin call error: %s", err.Error())
		log.Logger.Error(errMsg)
		// update batch exec record，更新批量执行记录
		updateData := make(map[string]interface{})
		updateData["error_code"] = errCode
		updateData["error_message"] = errMsg
		updateData["updated_by"] = middleware.GetRequestUser(c)
		updateData["updated_time"] = time.Now()
		tmpErr = database.UpdateBatchExec(c, batchExecId, updateData)
		if tmpErr != nil {
			err = tmpErr
			log.Logger.Error("update batch execution record failed", log.Error(err), log.String("batchExecErrMsg", errMsg))
			return
		}
		return
	}

	if dangerousCheckResult != nil {
		result.DangerousCheckResult = dangerousCheckResult
		log.Logger.Warn("dangerous check result existed", log.JsonObj("dangerousCheckResult", dangerousCheckResult))
		// if reqParam.IsDangerousBlock {
		// update batch exec errorCode record，更新批量执行记录
		errCode = models.BatchExecErrorCodeDangerousBlock
		errMsg = "dangerous block"
		updateData := make(map[string]interface{})
		updateData["error_code"] = errCode
		updateData["error_message"] = errMsg
		updateData["updated_by"] = middleware.GetRequestUser(c)
		updateData["updated_time"] = time.Now()
		tmpErr = database.UpdateBatchExec(c, batchExecId, updateData)
		if tmpErr != nil {
			err = tmpErr
			log.Logger.Error("plugin call succeed and dangerous block, but update batch execution record failed", log.Error(err))
			return
		}
		// }
		return
	}

	err = database.InsertBatchExecJobs(c, batchExecId, &execTime, reqParam, pluginCallParam, batchExecRunResult)
	if err != nil {
		// update batch exec record，更新批量执行记录
		errCode = models.BatchExecErrorCodeFailed
		errMsg = fmt.Sprintf("plugin call succeed, but insert batch execution jobs record failed: %s", err.Error())
		batchExecRunResultByte, tmpErr := json.Marshal(batchExecRunResult)
		if tmpErr != nil {
			errMsg += fmt.Sprintf(" marshal batchExecRunResult failed: %s", tmpErr.Error())
		} else {
			errMsg += fmt.Sprintf(" batchExecRunResult: %s", string(batchExecRunResultByte))
		}
		updateData := make(map[string]interface{})
		updateData["error_code"] = errCode
		updateData["error_message"] = errMsg
		updateData["updated_by"] = middleware.GetRequestUser(c)
		updateData["updated_time"] = time.Now()
		tmpErr = database.UpdateBatchExec(c, batchExecId, updateData)
		if tmpErr != nil {
			err = tmpErr
			log.Logger.Error("update batch execution record failed", log.Error(err), log.String("batchExecErrMsg", errMsg))
			return
		}
		log.Logger.Error(fmt.Sprintf("batchExecErrMsg: %s", errMsg))
		return
	} else {
		errCode = models.BatchExecErrorCodeSucceed
		errMsg = ""
		updateData := make(map[string]interface{})
		updateData["error_code"] = errCode
		updateData["error_message"] = errMsg
		updateData["updated_by"] = middleware.GetRequestUser(c)
		updateData["updated_time"] = time.Now()
		tmpErr = database.UpdateBatchExec(c, batchExecId, updateData)
		if tmpErr != nil {
			err = tmpErr
			log.Logger.Error("plugin call succeed, but update batch execution record failed", log.Error(err))
			return
		}
	}

	result.BatchExecRunResult = batchExecRunResult
	result.DangerousCheckResult = dangerousCheckResult
	return
}
