package batch_execution

import (
	"fmt"

	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/try"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/gin-gonic/gin"
)

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
		param.Sorting = append(param.Sorting, &models.QueryRequestSorting{Field: "updatedTime", Asc: false})
	}
	retData, err := database.RetrieveTemplate(c, &param)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, retData)
	}
	return
}

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
		param.Sorting = append(param.Sorting, &models.QueryRequestSorting{Field: "updatedTime", Asc: false})
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
