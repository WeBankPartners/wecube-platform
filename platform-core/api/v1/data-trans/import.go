package data_trans

import (
	"context"
	"encoding/json"
	"encoding/xml"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/bash"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"os"
	"sort"
	"strings"
	"time"

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
	if strings.TrimSpace(param.ExportNexusUrl) == "" && strings.TrimSpace(param.TransImportId) == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("ExportNexusUrl or transImportId is empty")))
		return
	}
	if checkWebStepInvalid(c, param) {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("param step is invalid")))
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

// checkWebStepInvalid 校验web传递 step是否合法
func checkWebStepInvalid(ctx context.Context, param models.ExecImportParam) bool {
	var transImportDetailList []*models.TransImportDetailTable
	var transImport *models.TransImportTable
	var err error
	if param.WebStep == 0 {
		return true
	}
	if param.ExportNexusUrl != "" && param.WebStep > int(models.ImportWebDisplayStepTwo) {
		return true
	}
	if strings.TrimSpace(param.TransImportId) != "" {
		if transImport, err = database.GetTransImport(ctx, param.TransImportId); err != nil {
			return true
		}
		if transImport.Status == string(models.TransImportStatusSuccess) {
			// 导入状态已完成
			return true
		}
		if transImportDetailList, err = database.GetTransImportDetail(ctx, param.TransImportId); err != nil {
			return true
		}
		switch param.WebStep {
		case int(models.ImportWebDisplayStepTwo):
			count := 0
			for _, detail := range transImportDetailList {
				if detail.Step <= int(models.TransImportStepRequestTemplate) && detail.Status == string(models.TransImportStatusSuccess) {
					count++
				}
			}
			if count == int(models.TransImportStepRequestTemplate) {
				// 1-9步都执行成功,web不应该传递第2步
				return true
			}
			return false
		case int(models.ImportWebDisplayStepThree):
			for _, detail := range transImportDetailList {
				if detail.Step <= int(models.TransImportStepRequestTemplate) && detail.Status != string(models.TransImportStatusSuccess) {
					return true
				}
				if detail.Step == int(models.TransImportStepInitWorkflow) && detail.Status == string(models.TransImportStatusSuccess) {
					return true
				}
			}
		case int(models.ImportWebDisplayStepFour):
			for _, detail := range transImportDetailList {
				if detail.Step <= int(models.TransImportStepInitWorkflow) && detail.Status != string(models.TransImportStatusSuccess) {
					return true
				}
			}
		}
	}
	return false
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

// 6、导入物料包
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
			tmpPackageName := artifactRow[models.TransArtifactNewPackageName]
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

// 10、开始执行编排(创建资源、初始化资源、应用部署)
func execWorkflow(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	var workflowImportDetailId, input string
	for _, v := range transImportParam.Details {
		if v.Name == "workflow" {
			workflowImportDetailId = v.Id
			break
		}
	}
	input, err = database.GetTransImportDetailInput(ctx, workflowImportDetailId)
	if err != nil {
		return
	}
	var procDefList []*models.ProcDefDto
	if err = json.Unmarshal([]byte(input), &procDefList); err != nil {
		err = fmt.Errorf("json unmarshal workflow proc def list fail,%s ", err.Error())
		return
	}
	var procExecList []*models.TransImportProcExecTable
	nowTime := time.Now().Format(models.DateTimeFormat)
	for _, v := range procDefList {
		exprList, analyzeErr := remote.AnalyzeExpression(v.RootEntity)
		if analyzeErr != nil || len(exprList) == 0 {
			log.Logger.Warn("workflow proc def rootEntity analyze fail", log.String("name", v.Name), log.String("rootEntity", v.RootEntity), log.Error(analyzeErr))
			continue
		}
		queryEntityRows, queryEntityDataErr := remote.RequestPluginModelData(ctx, exprList[0].Package, exprList[0].Entity, remote.GetToken(), []*models.EntityQueryObj{})
		if queryEntityDataErr != nil {
			err = queryEntityDataErr
			break
		}
		for _, row := range queryEntityRows {
			tmpProcExecRow := models.TransImportProcExecTable{
				Id:                "tm_exec_" + guid.CreateGuid(),
				TransImportDetail: transImportParam.CurrentDetail.Id,
				ProcDef:           v.Id,
				RootEntity:        v.RootEntity,
				EntityDataId:      fmt.Sprintf("%s", row["id"]),
				EntityDataName:    fmt.Sprintf("%s", row["displayName"]),
				Status:            models.JobStatusReady,
				CreatedUser:       transImportParam.Operator,
				CreatedTime:       nowTime,
			}
			procExecList = append(procExecList, &tmpProcExecRow)
		}
	}
	if err != nil {
		return
	}
	if len(procExecList) == 0 {
		return
	}
	for i, v := range procExecList {
		v.ExecOrder = i + 1
	}
	if err = database.CreateTransImportProcExecData(ctx, procExecList); err != nil {
		return
	}
	return
}

func StartExecWorkflowCron() {
	t := time.NewTicker(5 * time.Second).C
	for {
		<-t
		doExecWorkflowDaemonJob()
	}
}

func doExecWorkflowDaemonJob() {
	ctx := db.NewDBCtx(fmt.Sprintf("import_exec_job_%d", time.Now().Unix()))
	procExecList, err := database.GetTransImportProcExecList(ctx)
	if err != nil {
		log.Logger.Error("doExecWorkflowDaemonJob fail with get proc exec list", log.Error(err))
		return
	}
	log.Logger.Debug("doExecWorkflowDaemonJob", log.JsonObj("procExecList", procExecList))
	if len(procExecList) == 0 {
		return
	}
	importExecMap := make(map[string][]*models.TransImportProcExecTable)
	for _, procExecRow := range procExecList {
		if existList, ok := importExecMap[procExecRow.TransImportDetail]; ok {
			importExecMap[procExecRow.TransImportDetail] = append(existList, procExecRow)
		} else {
			importExecMap[procExecRow.TransImportDetail] = []*models.TransImportProcExecTable{procExecRow}
		}
	}
	for transImportDetailId, detailProcExecList := range importExecMap {
		needStartExec := &models.TransImportProcExecTable{}
		successRowCount := 0
		for _, v := range detailProcExecList {
			if v.Status == models.JobStatusSuccess {
				successRowCount = successRowCount + 1
				continue
			}
			if v.Status == models.JobStatusRunning || v.Status == models.JobStatusFail {
				break
			}
			if v.Status == models.JobStatusReady {
				needStartExec = v
				break
			}
		}
		if needStartExec.Id != "" {
			// 需要开始执行编排
			tmpErr := startExecWorkflow(ctx, needStartExec)
			if tmpErr != nil {
				log.Logger.Error("doExecWorkflowDaemonJob start exec workflow fail", log.String("detailId", transImportDetailId), log.Error(tmpErr))
			}
			continue
		}
		if successRowCount == len(detailProcExecList) {
			// 需要更新trans import detail 状态
			tmpErr := database.UpdateTransImportDetailStatus(ctx, "", transImportDetailId, models.JobStatusSuccess, "", "")
			if tmpErr != nil {
				log.Logger.Error("doExecWorkflowDaemonJob update trans import detail status fail", log.String("detailId", transImportDetailId), log.String("status", models.JobStatusSuccess), log.Error(tmpErr))
			}
		}
	}
}

func startExecWorkflow(ctx context.Context, procExecRow *models.TransImportProcExecTable) (err error) {

	return
}
