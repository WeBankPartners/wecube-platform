package plugin

import (
	"fmt"
	"net/http"
	"reflect"
	"sort"
	"strings"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"github.com/gin-gonic/gin"
)

// GetPluginModels 插件配置 - 数据模型
func GetPluginModels(c *gin.Context) {
	pluginPackageId := c.Param("pluginPackageId")
	if pluginPackageId == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("pluginPackageId can not empty")))
		return
	}
	withAttrString := strings.ToLower(c.Query("withAttr"))
	withAttr := true
	if withAttrString == "n" || withAttrString == "no" || withAttrString == "false" {
		withAttr = false
	}
	pluginPackageObj := models.PluginPackages{Id: pluginPackageId}
	if err := database.GetSimplePluginPackage(c, &pluginPackageObj, true); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	result, err := database.GetDataModels(c, pluginPackageObj.Name, withAttr)
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
	var filterResult []map[string]interface{}
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
		return
	}
	// query不为空,则表示需要全部数据模糊匹配
	if strings.TrimSpace(param.Query) != "" {
		for _, item := range result {
			for _, value := range item {
				if reflect.TypeOf(value).Name() == "string" {
					if strings.Contains(strings.ToLower(value.(string)), strings.ToLower(param.Query)) {
						filterResult = append(filterResult, item)
						break
					}
				}
			}
		}
	} else {
		filterResult = result
	}
	// 对 filterResult 进行排序,默认按guid 或者id倒序
	if param.Sorting == nil {
		param.Sorting = []*models.QueryRequestSorting{
			{
				Asc:   false,
				Field: "guid",
			},
			{
				Asc:   false,
				Field: "id",
			},
		}
	}
	sort.SliceStable(filterResult, func(i, j int) bool {
		for _, sorting := range param.Sorting {
			valueI := filterResult[i][sorting.Field]
			valueJ := filterResult[j][sorting.Field]
			if valueJ == nil || valueJ == nil {
				continue
			}
			switch vI := valueI.(type) {
			case string:
				vJ, ok := valueJ.(string)
				if !ok {
					return false
				}
				// 时间类型
				if ti, err := time.Parse(models.DateTimeFormat, vI); err == nil {
					tj, _ := time.Parse(models.DateTimeFormat, vJ)
					if (sorting.Asc && ti.Before(tj)) || (!sorting.Asc && ti.After(tj)) {
						return true
					} else {
						return false
					}
				}
				if sorting.Asc {
					return vI < vJ
				}
				return vI > vJ
			case int:
				vJ, ok := valueJ.(int)
				if !ok {
					return false
				}
				if sorting.Asc {
					return vI < vJ
				}
				return vI > vJ
			case int64:
				vJ, ok := valueJ.(int64)
				if !ok {
					return false
				}
				if sorting.Asc {
					return vI < vJ
				}
				return vI > vJ
			case float64:
				vJ, ok := valueJ.(float64)
				if !ok {
					return false
				}
				if sorting.Asc {
					return vI < vJ
				}
				return vI > vJ
			default:
				return false
			}
		}
		return false
	})

	// 数据分页
	if param.PageSize > 0 {
		pageInfo := models.PageInfo{
			StartIndex: param.StartIndex,
			PageSize:   param.PageSize,
			TotalRows:  len(filterResult),
		}
		endIndex := param.StartIndex + param.PageSize
		if len(filterResult) < endIndex {
			endIndex = len(filterResult)
		}
		filterResult = filterResult[param.StartIndex:endIndex]
		middleware.ReturnPageData(c, pageInfo, filterResult)
		return
	}
	middleware.ReturnData(c, filterResult)
}

// QueryExpressionDataForPlugin 给插件提供的表达式数据查询
func QueryExpressionDataForPlugin(c *gin.Context) {
	var param models.PluginQueryExpressionDataParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	exprList, err := remote.AnalyzeExpression(param.DataModelExpression)
	if err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if len(exprList) == 0 {
		middleware.ReturnError(c, fmt.Errorf("expression analyze result list empty"))
		return
	}
	filters := []*models.QueryExpressionDataFilter{{Index: 0, PackageName: exprList[0].Package, EntityName: exprList[0].Entity, AttributeFilters: []*models.QueryExpressionDataAttrFilter{{
		Name:     "id",
		Operator: "eq",
		Value:    param.RootDataId,
	}}}}
	if param.Token == "" {
		param.Token = remote.GetToken()
	}
	result, queryErr := remote.QueryPluginData(c, exprList, filters, param.Token)
	if queryErr != nil {
		middleware.ReturnError(c, queryErr)
	} else {
		middleware.ReturnData(c, result)
	}
}

func QueryRoleEntity(c *gin.Context) {
	var param models.EntityQueryParam
	result := models.RoleEntityResp{}
	if err := c.ShouldBindJSON(&param); err != nil {
		result.Status = "ERROR"
		result.Message = fmt.Sprintf("Request body json unmarshal failed: %s", err.Error())
		c.JSON(http.StatusOK, result)
		return
	}
	respData, err := remote.RetrieveAllLocalRoles("Y", c.GetHeader(models.AuthorizationHeader), c.GetHeader(models.AcceptLanguageHeader), false)
	if err != nil {
		result.Status = "ERROR"
		result.Message = err.Error()
	} else {
		result.Data = []*models.RoleEntityObj{}
		for _, v := range respData.Data {
			if v.Status == "NotDeleted" {
				result.Data = append(result.Data, &models.RoleEntityObj{
					Id:          v.Name,
					DisplayName: v.DisplayName,
					Email:       v.Email,
				})
			}
		}
		result.Status = "OK"
		result.Message = "Success"
	}
	c.JSON(http.StatusOK, result)
}
