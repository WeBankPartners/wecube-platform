package plugin

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"github.com/gin-gonic/gin"
	"strings"
)

// GetPluginModels 插件配置 - 数据模型
func GetPluginModels(c *gin.Context) {
	packageName := c.Param("pluginPackageId")
	if packageName == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("packageName can not empty")))
		return
	}
	withAttrString := strings.ToLower(c.Query("withAttr"))
	withAttr := true
	if withAttrString == "n" || withAttrString == "no" || withAttrString == "false" {
		withAttr = false
	}
	result, err := database.GetDataModels(c, packageName, withAttr)
	if len(result) == 0 {
		result = append(result, &models.DataModel{})
	}
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result[0])
	}
}

// GetAllModels 服务注册 - 所有插件模型查询
func GetAllModels(c *gin.Context) {
	withAttrString := strings.ToLower(c.Query("withAttr"))
	withAttr := true
	if withAttrString == "n" || withAttrString == "no" || withAttrString == "false" {
		withAttr = false
	}
	result, err := database.GetDataModels(c, "", withAttr)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

// GetEntityModel 服务注册 - entity模型查询
func GetEntityModel(c *gin.Context) {
	packageName := c.Param("packageName")
	entityName := c.Param("entity")
	if packageName == "" || entityName == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("packageName or eneity can not empty")))
		return
	}
	result, err := database.GetEntityModel(c, packageName, entityName, false)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

// GetEntityAttributes 批量执行 - entity模型属性查询
func GetEntityAttributes(c *gin.Context) {
	packageName := c.Param("packageName")
	entityName := c.Param("entity")
	if packageName == "" || entityName == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("packageName or eneity can not empty")))
		return
	}
	result, err := database.GetEntityModel(c, packageName, entityName, false)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result.Attributes)
	}
}

// SyncDynamicModels 插件配置 - 数据模型同步
func SyncDynamicModels(c *gin.Context) {
	packageName := c.Param("packageName")
	if packageName == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("packageName can not empty")))
		return
	}
	pluginModels, err := remote.GetPluginDataModels(c, packageName, c.GetHeader(models.AuthorizationHeader))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if err = database.SyncPluginDataModels(c, packageName, pluginModels); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	result, queryErr := database.GetDataModels(c, packageName, true)
	if queryErr != nil {
		middleware.ReturnError(c, queryErr)
	} else {
		middleware.ReturnData(c, result)
	}
}

// QueryExpressionEntities 批量执行 - 表达式entity解析和属性查询
func QueryExpressionEntities(c *gin.Context) {
	var param models.QueryExpressionDataParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	exprList, err := remote.AnalyzeExpression(param.DataModelExpression)
	if err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	result := []*models.ExpressionEntitiesRespObj{}
	for _, exprObj := range exprList {
		entityObj, queryErr := database.QueryExpressionEntityAttr(c, exprObj)
		if queryErr != nil {
			err = queryErr
			break
		}
		result = append(result, entityObj)
	}
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

// QueryExpressionData 批量执行 - 表达式解析和数据查询
func QueryExpressionData(c *gin.Context) {
	var param models.QueryExpressionDataParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	exprList, err := remote.AnalyzeExpression(param.DataModelExpression)
	if err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	result, queryErr := remote.QueryPluginData(c, exprList, param.Filters, c.GetHeader(models.AuthorizationHeader))
	if queryErr != nil {
		middleware.ReturnError(c, queryErr)
	} else {
		middleware.ReturnData(c, result)
	}
}
