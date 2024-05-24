package process

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

// StatisticsProDefList 编排定义列表
func StatisticsProDefList(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Logger.Error(e.(string))
	})

	includeDraft := c.Query("includeDraft") // 0 | 1
	permission := c.Query("permission")     // USE | MGMT
	tag := c.Query("tag")
	plugin := c.Query("plugin")
	if includeDraft == "" {
		includeDraft = "0"
	}
	if permission == "" {
		permission = "USE"
	}
	log.Logger.Debug("procDefList", log.String("includeDraft", includeDraft), log.String("permission", permission), log.String("tag", tag), log.StringList("roleList", middleware.GetRequestRoles(c)))
	result, err := database.ProcDefList(c, includeDraft, permission, tag, plugin, middleware.GetRequestRoles(c))
	mergeProcDefNameAndVersion(result)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
	return
}

func mergeProcDefNameAndVersion(procDefList []*models.ProcDefListObj) {
	for _, data := range procDefList {
		data.ProcDefName = fmt.Sprintf("%s%s", data.ProcDefName, data.ProcDefVersion)
	}
	return
}

// StatisticsServiceIds 插件注册列表
func StatisticsServiceNames(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Logger.Error(e.(string))
	})

	result, err := database.StatisticsServiceNames(c)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
	return
}

// StatisticsBindingsEntityByService 数据对象
func StatisticsBindingsEntityByService(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Logger.Error(e.(string))
	})

	var serviceNameList []string
	var err error
	if err = c.ShouldBind(&serviceNameList); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	result, err := database.StatisticsBindingsEntityByService(c, serviceNameList)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
	return
}

// StatisticsTasknodes 任务节点查询
func StatisticsTasknodes(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Logger.Error(e.(string))
	})

	var procDefIdList []string
	var err error
	if err = c.ShouldBind(&procDefIdList); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	result, err := database.StatisticsTasknodes(c, procDefIdList)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
	return
}

// StatisticsBindingsEntityByNode 数据对象
func StatisticsBindingsEntityByNode(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Logger.Error(e.(string))
	})

	var nodeList []string
	var err error
	if err = c.ShouldBind(&nodeList); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	result, err := database.StatisticsBindingsEntityByNode(c, nodeList)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
	return
}

// StatisticsProcessExec 编排查询
func StatisticsProcessExec(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Logger.Error(e.(string))
	})

	var reqParam models.StatisticsProcessExecReq
	var err error
	if err = c.ShouldBindJSON(&reqParam); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	result, err := database.StatisticsProcessExec(c, &reqParam)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
	return
}

// StatisticsTasknodeExec 编排节点-查询
func StatisticsTasknodeExec(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Logger.Error(e.(string))
	})

	var reqParam models.StatisticsTasknodeExecReq
	var err error
	if err = c.ShouldBindJSON(&reqParam); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}

	result, err := database.StatisticsTasknodeExec(c, &reqParam)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}

	return
}
