package data_trans

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/gin-gonic/gin"
	"strings"
)

// QueryBusinessList 查询产品、环境列表
func QueryBusinessList(c *gin.Context) {
	var param models.QueryBusinessParam
	var result []map[string]interface{}
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if result, err = database.QueryBusinessList(c, c.GetHeader("Authorization"), c.GetHeader(middleware.AcceptLanguageHeader), param); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, result)
}

// CreateExport 创建导入,返回导入Id
func CreateExport(c *gin.Context) {
	var param models.CreateExportParam
	var transExportId string
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if len(param.PIds) == 0 || strings.TrimSpace(param.Env) == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}

	if transExportId, err = database.CreateExport2(c, param, middleware.GetRequestUser(c)); err != nil {
		middleware.ReturnError(c, err)
	}
	middleware.ReturnData(c, transExportId)
	return
}

// ExecExport 执行底座导出
func ExecExport(c *gin.Context) {
	var param models.DataTransExportParam
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if strings.TrimSpace(param.TransExportId) == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("TransExportId is empty")))
		return
	}
	userToken := c.GetHeader("Authorization")
	language := c.GetHeader(middleware.AcceptLanguageHeader)
	// 1. 根据选中编排、批量执行、请求模版角色追加到模版角色
	if param.Roles, err = database.AutoAppendExportRoles(c, userToken, language, param); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 3.开始导出,采用异步导出方式
	go database.ExecTransExport(c, param, userToken, language)
	middleware.ReturnSuccess(c)
}

func ExportDetail(c *gin.Context) {
	transExportId := c.Query("transExportId")
	var detail *models.TransExportDetail
	var err error
	if strings.TrimSpace(transExportId) == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("transExportId is empty")))
		return
	}
	if detail, err = database.GetTransExportDetail(c, transExportId); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, detail)
}

func GetExportListOptions(c *gin.Context) {
	var TransExportHistoryOptions models.TransExportHistoryOptions
	var err error
	if TransExportHistoryOptions, err = database.GetAllTransExportOptions(c); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, TransExportHistoryOptions)
}

func ExportList(c *gin.Context) {
	var param models.TransExportHistoryParam
	var pageInfo models.PageInfo
	var list []*models.TransExportTable
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if param.PageSize == 0 {
		param.PageSize = 10
	}
	if pageInfo, list, err = database.QueryTransExportByCondition(c, param); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnPageData(c, pageInfo, list)
}
