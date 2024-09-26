package data_trans

import (
	"context"
	"encoding/json"
	"encoding/xml"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/bash"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"os"
	"sort"
	"strings"

	"github.com/WeBankPartners/go-common-lib/guid"
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
	param.Token = c.GetHeader("Authorization")
	param.Language = c.GetHeader("Accept-Language")
	if err = StartTransImport(c, param); err != nil {
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

// 2.导入cmdb数据
func importCmdbConfig(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	// 导入cmdb数据
	err = database.DataTransImportCMDBData(ctx, transImportParam.DirPath+"/wecmdb_data.sql")
	if err != nil {
		err = fmt.Errorf("import cmdb data fail,%s ", err.Error())
		return
	}
	return
}

// 3、导入cmdb插件服务
func importPluginConfig(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	ctx = BuildContext(ctx, &models.BuildContextParam{UserId: transImportParam.Operator, Token: transImportParam.Token})
	// 同步cmdb数据模型
	pluginModels, getModelsErr := remote.GetPluginDataModels(ctx, "wecmdb", remote.GetToken())
	if getModelsErr != nil {
		err = fmt.Errorf("get wecmdb plugin model data fail,%s ", getModelsErr.Error())
		return
	}
	err = database.SyncPluginDataModels(ctx, "wecmdb", pluginModels)
	if err != nil {
		err = fmt.Errorf("sync wecmdb model data fail,%s ", err.Error())
		return
	}
	// 导入插件服务
	xmlFileNameList, listFileErr := bash.ListDirFiles(transImportParam.DirPath + "/plugin-config")
	if listFileErr != nil {
		err = fmt.Errorf("list plugin config dir file list fail,%s ", listFileErr.Error())
		return
	}
	sort.Strings(xmlFileNameList)
	runningPluginRows, getPluginErr := database.GetRunningPluginPackages(ctx)
	if getPluginErr != nil {
		err = getPluginErr
		return
	}
	for _, xmlFileName := range xmlFileNameList {
		tmpPluginPackageId := ""
		for _, row := range runningPluginRows {
			if strings.HasPrefix(xmlFileName, "plugin-"+row.Name+"-") {
				tmpPluginPackageId = row.Id
				break
			}
		}
		if tmpPluginPackageId != "" {
			fileBytes, readFileErr := os.ReadFile(transImportParam.DirPath + "/plugin-config/" + xmlFileName)
			if readFileErr != nil {
				err = fmt.Errorf("read plugin:%s xml file fail,%s ", xmlFileName, readFileErr.Error())
				break
			}
			packagePluginsData := models.PackagePluginsXML{}
			if err = xml.Unmarshal(fileBytes, &packagePluginsData); err != nil {
				err = fmt.Errorf("xml unmarshal plugin:%s xml fail,%s ", xmlFileName, err.Error())
				break
			}
			if _, err = database.ImportPluginConfigs(ctx, tmpPluginPackageId, &packagePluginsData); err != nil {
				err = fmt.Errorf("import plugin:%s config fail,%s ", xmlFileName, err.Error())
				break
			}
		}
	}
	return
}

// 5、导入物料包
func importArtifactPackage(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	var input string
	input, err = database.GetTransImportDetailInput(ctx, transImportParam.CurrentDetail.Id)
	if err != nil {
		return
	}
	var artifactDataList []*models.AnalyzeArtifactDisplayData
	if err = json.Unmarshal([]byte(input), &artifactDataList); err != nil {
		err = fmt.Errorf("json unmarshal artifact import detail data fail,%s ", err.Error())
		log.Logger.Error("importArtifactPackageFunc", log.String("inputData", input), log.Error(err))
		return
	}
	for _, artifactData := range artifactDataList {
		for _, artifactRow := range artifactData.ArtifactRows {
			tmpPackageName := artifactRow["key_name"]
			if tmpPackageName == "" {
				continue
			}
			tmpImportFilePath := fmt.Sprintf(models.TransImportTmpDir, transImportParam.TransImport.Id) + "/" + models.TransArtifactPackageDirName + "/" + tmpPackageName
			tmpDeployPackageGuid, tmpErr := remote.UploadArtifactPackage(ctx, remote.GetToken(), artifactData.UnitDesign, tmpImportFilePath)
			if tmpErr != nil {
				err = fmt.Errorf("upload artifact package to artifacts plugin fail,tmpPath:%s ,error:%s ", tmpImportFilePath, tmpErr.Error())
				break
			} else {
				log.Logger.Info("upload artifact package to artifacts plugin done", log.String("packageName", tmpPackageName), log.String("deployPackageGuid", tmpDeployPackageGuid))
			}
		}
		if err != nil {
			break
		}
	}
	return
}

func BuildContext(ctx context.Context, param *models.BuildContextParam) context.Context {
	if ctx.Value(models.TransactionIdHeader) == nil {
		if param.TransactionId == "" {
			param.TransactionId = "d_trans_" + guid.CreateGuid()
		}
		ctx = context.WithValue(ctx, models.TransactionIdHeader, param.TransactionId)
	}
	if ctx.Value(models.ContextUserId) == nil {
		ctx = context.WithValue(ctx, models.ContextUserId, param.UserId)
	}
	if ctx.Value(models.ContextRoles) == nil {
		ctx = context.WithValue(ctx, models.ContextRoles, param.Roles)
	}
	if ctx.Value(models.AuthorizationHeader) == nil {
		if param.Token == "" {
			param.Token = remote.GetToken()
		}
		ctx = context.WithValue(ctx, models.AuthorizationHeader, param.Token)
	}
	if ctx.Value(models.AcceptLanguageHeader) == nil {
		if param.Language == "" {
			param.Language = "en"
		}
		ctx = context.WithValue(ctx, models.AcceptLanguageHeader, param.Language)
	}
	return ctx
}
