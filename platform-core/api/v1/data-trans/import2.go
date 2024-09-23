package data_trans

import (
	"fmt"
	"strings"

	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/gin-gonic/gin"
)

// GetBusinessList 获取环境业务配置
func GetBusinessList(c *gin.Context) {
	var err error
	var localPath string
	var result models.GetBusinessListRes
	exportNexusUrl := c.Query("exportNexusUrl")
	if strings.TrimSpace(exportNexusUrl) == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("exportNexusUrl is empty")))
		return
	}
	// 解压文件
	if localPath, err = database.DecompressExportZip(c, exportNexusUrl); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 获取环境业务配置
	if result, err = database.GetBusinessList(localPath); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 获取完数据,删除解压文件
	if err = database.RemoveTempExportDir(localPath); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, result)
}

// ExecImport 执行导入
func ExecImport(c *gin.Context) {

}

// ImportDetail 导入详情
func ImportDetail(c *gin.Context) {
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

func GetImportListOptions(c *gin.Context) {
	var TransExportHistoryOptions models.TransExportHistoryOptions
	var err error
	if TransExportHistoryOptions, err = database.GetAllTransExportOptions(c); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, TransExportHistoryOptions)
}

func ImportList(c *gin.Context) {
	var TransExportHistoryOptions models.TransExportHistoryOptions
	var err error
	if TransExportHistoryOptions, err = database.GetAllTransExportOptions(c); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, TransExportHistoryOptions)
}
