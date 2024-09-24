package data_trans

import (
	"context"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
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
	transImportId := fmt.Sprintf("t_import_%s", guid.CreateGuid())
	if localPath, err = database.DecompressExportZip(c, exportNexusUrl, transImportId); err != nil {
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
	var param models.ExecImportParam
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if strings.TrimSpace(param.ExportNexusUrl) == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("ExportNexusUrl or step is empty")))
		return
	}
	if param.TransImportId == "" {
		param.TransImportId = fmt.Sprintf("t_import_%s", guid.CreateGuid())
	}
	param.Operator = middleware.GetRequestUser(c)
	if err = StartTransImport2(c, param); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, param.TransImportId)
}

// ImportDetail 导入详情
func ImportDetail(c *gin.Context) {
	transExportId := c.Query("transImportId")
	var detail *models.TransImportDetail
	var err error
	if strings.TrimSpace(transExportId) == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("transExportId is empty")))
		return
	}
	if detail, err = database.GetImportDetail(c, transExportId); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, detail)
}

func GetImportListOptions(c *gin.Context) {
	var TransExportHistoryOptions models.TransExportHistoryOptions
	var err error
	if TransExportHistoryOptions, err = database.GetAllTransImportOptions(c); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, TransExportHistoryOptions)
}

func ImportList(c *gin.Context) {
	var param models.TransImportHistoryParam
	var pageInfo models.PageInfo
	var list []*models.TransImportTable
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if param.PageSize == 0 {
		param.PageSize = 10
	}
	if pageInfo, list, err = database.QueryTransImportByCondition(c, param); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnPageData(c, pageInfo, list)
}

// StartTransImport 执行导入
func StartTransImport2(ctx context.Context, param models.ExecImportParam) (err error) {
	var transImport *models.TransImportTable
	var localPath string
	var transImportAction *models.TransImportActionTable
	if transImport, err = database.GetTransImport(ctx, param.TransImportId); err != nil {
		log.Logger.Error("GetTransImport err", log.Error(err))
		return
	}
	// 文件解压
	if localPath, err = database.DecompressExportZip(ctx, param.ExportNexusUrl, param.TransImportId); err != nil {
		log.Logger.Error("DecompressExportZip err", log.Error(err))
		return
	}
	if transImport == nil || transImport.Id == "" {
		// 初始化导入
		if err = database.InitTransImport(ctx, param.TransImportId, param.ExportNexusUrl, localPath, param.Operator); err != nil {
			log.Logger.Error("initTransImport err", log.Error(err))
			return
		}
	}
	if transImportAction, err = database.GetLatestTransImportAction(ctx, param.TransImportId); err != nil {
		log.Logger.Error("GetLatestTransImportAction err", log.Error(err))
		return
	}
	actionParam := &models.CallTransImportActionParam{
		TransImportId: param.TransImportId,
		Action:        string(models.TransImportStatusStart),
		Operator:      param.Operator,
		DirPath:       localPath,
		ActionId:      transImportAction.Id,
	}
	go doImportAction(ctx, actionParam)
	return
}
