package data_trans

import (
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
	if result, err = database.QueryBusinessList(c, c.GetHeader("Authorization"), c.GetHeader("Accept-Language"), param); err != nil {
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

	if transExportId, err = database.CreateExport(c, param, middleware.GetRequestUser(c)); err != nil {
		middleware.ReturnError(c, err)
	}
	middleware.ReturnData(c, transExportId)
	return
}

// ExecExport 执行底座导出
func ExecExport(c *gin.Context) {
	var param models.DataTransExportParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
}

func ExportDetail(c *gin.Context) {

}

func ExportList(c *gin.Context) {

}

func GetExportMonitor(c *gin.Context) {

}

func GetExportPlugin(c *gin.Context) {

}
