package data_trans

import (
	"context"
	"encoding/json"
	"encoding/xml"
	"fmt"
	"os"
	"sort"
	"strconv"
	"strings"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-core/api/v1/plugin"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/execution"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/workflow"
	"go.uber.org/zap"

	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/bash"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
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
	param.Action = string(models.TransImportStatusStart)
	if err = StartTransImport(c, param); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, param.TransImportId)
}

// ExecImportRetry 重试导入
func ExecImportRetry(c *gin.Context) {
	var param models.ExecImportParam
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	param.Operator = middleware.GetRequestUser(c)
	param.Token = c.GetHeader("Authorization")
	param.Language = c.GetHeader("Accept-Language")
	param.Action = string(models.TransImportActionRetry)
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
				if detail.Step <= int(models.TransImportStepWebBaseImportSuccess) && detail.Status == string(models.TransImportStatusSuccess) {
					count++
				}
			}
			if count == int(models.TransImportStepWebBaseImportSuccess) {
				// 1-10 步都执行成功,web不应该传递第2步
				return true
			}
		case int(models.ImportWebDisplayStepThree):
			for _, detail := range transImportDetailList {
				if detail.Step <= int(models.TransImportStepWebBaseImportSuccess) && detail.Status != string(models.TransImportStatusSuccess) {
					return true
				}
				if detail.Step == int(models.TransImportStepModifyNewEnvData) && detail.Status == string(models.TransImportStatusSuccess) {
					return true
				}
			}
		case int(models.ImportWebDisplayStepFour):
			for _, detail := range transImportDetailList {
				if detail.Step <= int(models.TransImportStepInitWorkflow) && detail.Status != string(models.TransImportStatusSuccess) {
					return true
				}
				if detail.Step == int(models.TransImportStepMonitorBusiness) && detail.Status == string(models.TransImportStatusSuccess) {
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

	// 默认精简 cmdbCI 数据，减少传输量
	simplifyImportCmdbCIData(detail)

	middleware.ReturnData(c, detail)
}

// simplifyImportCmdbCIData 精简导入 cmdbCI 数据，只保留必要字段
func simplifyImportCmdbCIData(detail *models.TransImportDetail) {
	for _, ci := range detail.CmdbCI {
		if ci.Data != nil {
			// 将完整的 CI 数据转换为精简版本
			simplifiedData := convertToSimplifiedCIData(ci.Data)
			ci.Data = simplifiedData
		}
	}
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

func UpdateImportStatus(c *gin.Context) {
	var param models.UpdateImportStatusParam
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if param.Id == "" || param.Status == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("param id or status is empty")))
		return
	}
	param.Operator = middleware.GetRequestUser(c)
	if err = database.ModifyImportStatus(c, param); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnSuccess(c)
}

// 2.导入cmdb数据
func importCmdbConfig(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	// 导入cmdb数据
	err = database.DataTransImportCMDBData(ctx, transImportParam.DirPath+"/wecmdb_data.sql")
	if err != nil {
		err = fmt.Errorf("import cmdb data fail,%s ", err.Error())
		return
	}
	transImportConfig, getImportConfigErr := database.GetDataTransImportConfig(ctx)
	if getImportConfigErr != nil {
		err = fmt.Errorf("get import config fail,%s ", getImportConfigErr.Error())
		return
	}
	if len(transImportConfig.AutoConfirmCiList) > 0 {
		for _, confirmCiType := range transImportConfig.AutoConfirmCiList {
			if confirmCiType == "" {
				continue
			}
			var confirmGuidList []string
			tmpQueryCiDataRows, tmpErr := remote.RequestPluginModelData(ctx, "wecmdb", confirmCiType, remote.GetToken(), []*models.EntityQueryObj{})
			if tmpErr != nil {
				err = fmt.Errorf("try to confirm ci data,query ciType:%s data fail,%s ", confirmCiType, tmpErr.Error())
				break
			}
			for _, row := range tmpQueryCiDataRows {
				tmpRowGuid := fmt.Sprintf("%s", row["id"])
				if tmpRowGuid != "" {
					confirmGuidList = append(confirmGuidList, tmpRowGuid)
				}
			}
			if len(confirmGuidList) > 0 {
				err = remote.ConfirmCMDBDataList(ctx, confirmCiType, confirmGuidList)
				if err != nil {
					err = fmt.Errorf("try to do confirm ciType:%s data action fail,%s ", confirmCiType, err.Error())
					break
				}
			}
		}
		if err != nil {
			return
		}
	}
	if len(transImportConfig.AutoConfirmViewList) > 0 {
		for _, viewId := range transImportConfig.AutoConfirmViewList {
			if err = remote.AutoConfirmCMDBView(ctx, viewId); err != nil {
				log.Error(nil, log.LOGGER_APP, "confirm view fail", zap.String("viewId", viewId), zap.Error(err))
				err = fmt.Errorf("confirm view:%s fail,%s ", viewId, err.Error())
				break
			}
		}
		if err != nil {
			return
		}
	}
	if len(transImportConfig.AutoUpdateCiList) > 0 {
		for _, updateCiType := range transImportConfig.AutoUpdateCiList {
			if updateCiType == "" {
				continue
			}
			var updateGuidMapList []map[string]interface{}
			tmpQueryCiDataRows, tmpErr := remote.RequestPluginModelData(ctx, "wecmdb", updateCiType, remote.GetToken(), []*models.EntityQueryObj{})
			if tmpErr != nil {
				err = fmt.Errorf("try to update ci data,query ciType:%s data fail,%s ", updateCiType, tmpErr.Error())
				break
			}
			for _, row := range tmpQueryCiDataRows {
				tmpRowGuid := fmt.Sprintf("%s", row["id"])
				if tmpRowGuid != "" {
					tmpRowMap := make(map[string]interface{})
					tmpRowMap["id"] = tmpRowGuid
					updateGuidMapList = append(updateGuidMapList, tmpRowMap)
				}
			}
			if len(updateGuidMapList) > 0 {
				_, err = remote.UpdatePluginModelData(ctx, "wecmdb", updateCiType, remote.GetToken(), "", updateGuidMapList)
				if err != nil {
					err = fmt.Errorf("try to do update ciType:%s data action fail,%s ", updateCiType, err.Error())
					break
				}
			}
		}
		if err != nil {
			return
		}
	}
	return
}

// 3、导入cmdb插件服务
func importPluginConfig(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	ctx = BuildContext(ctx, &models.BuildContextParam{UserId: transImportParam.Operator, Token: remote.GetToken()})
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
			if err = RestartPluginInstance(ctx, tmpPluginPackageId); err != nil {
				break
			}
		}
	}
	return
}

func RestartPluginInstance(ctx context.Context, pluginPackageId string) (err error) {
	pluginInstanceObj, getPluginInstanceErr := database.GetPluginInstance("", "", "", pluginPackageId, false)
	if getPluginInstanceErr != nil {
		err = getPluginInstanceErr
		return
	}
	if pluginInstanceObj.Id == "" {
		// 没有运行的实例，不用重启
		return
	}
	err = plugin.RemovePluginInstanceFunc(ctx, pluginInstanceObj.Id)
	if err != nil {
		err = fmt.Errorf("remove plugin:%s fail,%s ", pluginInstanceObj.InstanceName, err.Error())
		return
	}
	err = plugin.LaunchPluginFunc(ctx, pluginPackageId, pluginInstanceObj.Host, "system", pluginInstanceObj.Port)
	if err != nil {
		err = fmt.Errorf("launch plugin:%s fail,%s ", pluginInstanceObj.InstanceName, err.Error())
	}
	return
}

// 6、导入物料包
func importArtifactPackage(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	var input, detailOutput string
	input, err = database.GetTransImportDetailInput(ctx, transImportParam.CurrentDetail.Id)
	if err != nil {
		return
	}
	detailOutput, err = database.GetTransImportDetailOutput(ctx, transImportParam.CurrentDetail.Id)
	if err != nil {
		return
	}
	// 获取nexus配置
	nexusConfig, getNexusConfigErr := database.GetDataTransImportConfig(ctx)
	if getNexusConfigErr != nil {
		err = getNexusConfigErr
		return
	}
	var artifactDataList []*models.AnalyzeArtifactDisplayData
	if err = json.Unmarshal([]byte(input), &artifactDataList); err != nil {
		err = fmt.Errorf("json unmarshal artifact import detail data fail,%s ", err.Error())
		log.Error(nil, log.LOGGER_APP, "importArtifactPackageFunc", zap.String("inputData", input), zap.Error(err))
		return
	}
	fileMd5Map, listErr := database.ListImportNexusArtifactPackages(ctx, transImportParam.TransImport.InputUrl)
	if listErr != nil {
		err = fmt.Errorf("try to list import nexus artifact dir file list fail,%s ", listErr.Error())
		return
	}
	artifactOutputList := []*models.ArtifactPackageImportOutputData{}
	packageOutputMap := make(map[string]*models.ArtifactPackageImportOutputData)
	if detailOutput != "" {
		if tmpErr := json.Unmarshal([]byte(detailOutput), &artifactOutputList); tmpErr != nil {
			log.Warn(nil, log.LOGGER_APP, "importArtifactPackageFunc", zap.String("outputData", detailOutput), zap.Error(tmpErr))
		} else {
			for _, v := range artifactOutputList {
				packageOutputMap[v.Guid] = v
			}
		}
	}
	for _, artifactData := range artifactDataList {
		for _, artifactRow := range artifactData.ArtifactRows {
			tmpPackageName := artifactRow[models.TransArtifactNewPackageName]
			if tmpPackageName == "" {
				continue
			}
			artifactOutputObj := &models.ArtifactPackageImportOutputData{}
			if existRecord, existStatus := packageOutputMap[artifactRow["guid"]]; existStatus {
				if existRecord.Status == "success" {
					// 之前已经导入成功过的包
					continue
				}
				artifactOutputObj = existRecord
			} else {
				artifactOutputObj.Guid = artifactRow["guid"]
				artifactOutputObj.Name = artifactRow["name"]
				artifactOutputObj.KeyName = artifactRow["key_name"]
				artifactOutputObj.UnitDesign = artifactData.UnitDesign
				artifactOutputObj.UnitDesignName = artifactData.UnitDesignName
				artifactOutputObj.ExpectMd5 = fileMd5Map[tmpPackageName]
				artifactOutputObj.Status = "notStart"
				artifactOutputList = append(artifactOutputList, artifactOutputObj)
			}
			tmpImportFilePath, tmpImportDirPath, downloadErr := database.DownloadImportArtifactPackage(ctx, nexusConfig, transImportParam.TransImport.InputUrl, transImportParam.TransImport.Id, tmpPackageName, artifactOutputObj.ExpectMd5)
			if downloadErr != nil {
				err = fmt.Errorf("download artifact package from nexus fail,package name:%s ,error:%s ", tmpPackageName, downloadErr.Error())
				artifactOutputObj.Status = "fail"
				break
			}
			// tmpImportFilePath := fmt.Sprintf(models.TransImportTmpDir, transImportParam.TransImport.Id) + "/" + models.TransArtifactPackageDirName + "/" + tmpPackageName
			tmpDeployPackageGuid, tmpErr := remote.UploadArtifactPackageNew(ctx, remote.GetToken(), artifactData.UnitDesign, tmpImportFilePath)
			if tmpErr != nil {
				err = fmt.Errorf("upload artifact package to artifacts plugin fail,tmpPath:%s ,error:%s ", tmpImportFilePath, tmpErr.Error())
				artifactOutputObj.Status = "fail"
				os.RemoveAll(tmpImportDirPath)
				break
			} else {
				log.Info(nil, log.LOGGER_APP, "upload artifact package to artifacts plugin done", zap.String("packageName", tmpPackageName), zap.String("deployPackageGuid", tmpDeployPackageGuid))
			}
			os.RemoveAll(tmpImportDirPath)
			artifactOutputObj.Status = "success"
		}
		tmpOutputBytes, _ := json.Marshal(&artifactOutputList)
		if tmpUpdateOutputErr := database.UpdateTransImportDetailOutput(ctx, transImportParam.TransImport.Id, models.TransImportStep(transImportParam.CurrentDetail.Step), string(tmpOutputBytes)); tmpUpdateOutputErr != nil {
			log.Error(nil, log.LOGGER_APP, "update trans import artifact output status fail", zap.String("unitDesign", artifactData.UnitDesignName), zap.Error(tmpUpdateOutputErr))
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

// 12、开始执行编排(创建资源、初始化资源、应用部署)
func execWorkflow(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	var workflowDetailId, workflowInitDetailId, workflowDetailInput, workflowInitDetailInput string
	for _, v := range transImportParam.Details {
		if v.Name == "workflow" {
			workflowDetailId = v.Id
		}
		if v.Name == "workflow_init" {
			workflowInitDetailId = v.Id
		}
	}
	workflowDetailInput, err = database.GetTransImportDetailInput(ctx, workflowDetailId)
	if err != nil {
		return
	}
	workflowInitDetailInput, err = database.GetTransImportDetailInput(ctx, workflowInitDetailId)
	if err != nil {
		return
	}
	transImportConfig, transImportErr := database.GetDataTransImportConfig(ctx)
	if transImportErr != nil {
		err = fmt.Errorf("get trans import config fail,%s ", transImportErr.Error())
		return
	}
	var procDefList []*models.ProcDefDto
	if err = json.Unmarshal([]byte(workflowDetailInput), &procDefList); err != nil {
		err = fmt.Errorf("json unmarshal workflow proc def list fail,%s ", err.Error())
		return
	}
	var workflowInitList models.TransExportWorkflowList
	if err = json.Unmarshal([]byte(workflowInitDetailInput), &workflowInitList); err != nil {
		err = fmt.Errorf("json unmarshal workflow_init data to export workflow list fail,%s ", err.Error())
		return
	}
	var procExecList []*models.TransImportProcExecTable
	nowTime := time.Now().Format(models.DateTimeFormat)
	sort.Sort(workflowInitList)
	for _, workflowObj := range workflowInitList {
		if !workflowObj.Executable {
			continue
		}
		for _, v := range procDefList {
			if v.Id != workflowObj.WorkflowId {
				continue
			}
			exprList, analyzeErr := remote.AnalyzeExpression(v.RootEntity)
			if analyzeErr != nil || len(exprList) == 0 {
				log.Warn(nil, log.LOGGER_APP, "workflow proc def rootEntity analyze fail", zap.String("name", v.Name), zap.String("rootEntity", v.RootEntity), zap.Error(analyzeErr))
				continue
			}
			queryEntityRows, queryEntityDataErr := remote.QueryPluginData(ctx, exprList, []*models.QueryExpressionDataFilter{}, remote.GetToken())
			if queryEntityDataErr != nil {
				err = queryEntityDataErr
				break
			}
			batchSortAttr := ""
			if exprList[0].Entity == transImportConfig.SystemCiType {
				batchSortAttr = transImportConfig.SystemDeployBatchAttr
			}
			importEntityRows := parseQueryImportEntityRows(queryEntityRows, batchSortAttr)
			sort.Sort(importEntityRows)
			if batchSortAttr != "" {
				log.Info(nil, log.LOGGER_APP, "workflow batchSortAttr", zap.String("batchSortAttr", batchSortAttr), log.JsonObj("importEntityRows", importEntityRows))
			}
			for _, row := range importEntityRows {
				tmpProcExecRow := models.TransImportProcExecTable{
					Id:                "tm_exec_" + guid.CreateGuid(),
					TransImportDetail: transImportParam.CurrentDetail.Id,
					ProcDef:           v.Id,
					ProcDefKey:        v.Key,
					ProcDefName:       v.Name,
					RootEntity:        v.RootEntity,
					EntityDataId:      row.Id,
					EntityDataName:    row.DisplayName,
					Status:            models.JobStatusReady,
					CreatedUser:       transImportParam.Operator,
					CreatedTime:       nowTime,
				}
				procExecList = append(procExecList, &tmpProcExecRow)
			}
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
	transactionId := fmt.Sprintf("import_exec_job_%d", time.Now().Unix())
	ctx := db.NewDBCtx(transactionId)
	procExecList, err := database.GetTransImportProcExecList(ctx)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "doExecWorkflowDaemonJob fail with get proc exec list", zap.Error(err))
		return
	}
	if len(procExecList) == 0 {
		log.Debug(nil, log.LOGGER_APP, "done doExecWorkflowDaemonJob with empty procExecList", zap.String("id", transactionId))
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
				if v.ProcInsStatus != "" {
					if v.ProcInsStatus == models.JobStatusKill {
						v.ProcInsStatus = models.JobStatusFail
					}
					if v.ProcInsStatus == models.WorkflowStatusStop {
						v.ProcInsStatus = models.JobStatusRunning
					}
					if v.Status != v.ProcInsStatus {
						v.Status = v.ProcInsStatus
						if _, updateStatusErr := database.UpdateTransImportProcExec(ctx, v); updateStatusErr != nil {
							log.Error(nil, log.LOGGER_APP, "doExecWorkflowDaemonJob update running proc exec status fail", zap.String("detailId", v.Id), zap.String("status", v.Status), zap.Error(updateStatusErr))
						}
					}
				}
				if v.Status == models.JobStatusSuccess {
					successRowCount = successRowCount + 1
					continue
				} else {
					break
				}
			}
			if v.Status == models.TransImportInPreparationStatus {
				break
			}
			if v.Status == models.JobStatusReady {
				needStartExec = v
				break
			}
		}
		if needStartExec.Id != "" {
			// 需要开始执行编排
			needStartExec.Status = models.TransImportInPreparationStatus
			rowAffect, execErr := database.UpdateTransImportProcExec(ctx, needStartExec)
			if execErr != nil {
				log.Error(nil, log.LOGGER_APP, "doExecWorkflowDaemonJob update proc exec status fail", zap.String("detailId", transImportDetailId), zap.String("status", needStartExec.Status), zap.Error(execErr))
				continue
			}
			if !rowAffect {
				log.Warn(nil, log.LOGGER_APP, "doExecWorkflowDaemonJob update proc exec status affect now row", zap.String("detailId", transImportDetailId))
				continue
			}
			tmpProcInsId, tmpErr := startExecWorkflow(ctx, needStartExec)
			if tmpErr != nil {
				log.Error(nil, log.LOGGER_APP, "doExecWorkflowDaemonJob start exec workflow fail", zap.String("detailId", transImportDetailId), zap.Error(tmpErr))
				needStartExec.Status = models.JobStatusReady
				database.UpdateTransImportProcExec(ctx, needStartExec)
				continue
			}
			needStartExec.Status = models.JobStatusRunning
			needStartExec.ProcIns = tmpProcInsId
			if _, tmpErr = database.UpdateTransImportProcExec(ctx, needStartExec); tmpErr != nil {
				log.Error(nil, log.LOGGER_APP, "doExecWorkflowDaemonJob update proc exec status fail", zap.String("detailId", transImportDetailId), zap.String("status", needStartExec.Status), zap.Error(tmpErr))
				continue
			}
			continue
		}
		if successRowCount == len(detailProcExecList) {
			// 需要更新trans import detail 状态
			tmpErr := database.UpdateTransImportDetailStatus(ctx, "", transImportDetailId, "success", "", "")
			if tmpErr != nil {
				log.Error(nil, log.LOGGER_APP, "doExecWorkflowDaemonJob update trans import detail status fail", zap.String("detailId", transImportDetailId), zap.String("status", models.JobStatusSuccess), zap.Error(tmpErr))
			}
		}
	}
}

func startExecWorkflow(ctx context.Context, procExecRow *models.TransImportProcExecTable) (retProcInsId string, err error) {
	procDefId, getProcErr := database.GetTransImportProcDefId(ctx, procExecRow.ProcDef, procExecRow.ProcDefKey)
	if getProcErr != nil {
		err = fmt.Errorf("try to get proc def with exec data fail,%s ", getProcErr.Error())
		return
	}
	// preview
	previewData, previewErr := execution.BuildProcPreviewData(ctx, procDefId, procExecRow.EntityDataId, procExecRow.CreatedUser)
	if previewErr != nil {
		err = previewErr
		log.Error(nil, log.LOGGER_APP, "startExecWorkflow fail with build proc preview data", zap.String("procExecId", procExecRow.Id), zap.Error(previewErr))
		return
	}
	// proc instance start
	procStartParam := models.ProcInsStartParam{
		EntityDataId:      procExecRow.EntityDataId,
		EntityDisplayName: procExecRow.EntityDataName,
		ProcDefId:         procDefId,
		ProcessSessionId:  previewData.ProcessSessionId,
	}
	// 新增 proc_ins,proc_ins_node,proc_data_binding 纪录
	procInsId, workflowRow, workNodes, workLinks, createInsErr := database.CreateProcInstance(ctx, &procStartParam, procExecRow.CreatedUser)
	if createInsErr != nil {
		err = createInsErr
		log.Error(nil, log.LOGGER_APP, "startExecWorkflow fail with create proc instance data", zap.String("procExecId", procExecRow.Id), zap.String("sessionId", previewData.ProcessSessionId), zap.Error(createInsErr))
		return
	}
	retProcInsId = procInsId
	// 初始化workflow并开始
	workObj := workflow.Workflow{ProcRunWorkflow: *workflowRow}
	workObj.Init(context.Background(), workNodes, workLinks)
	go workObj.Start(&models.ProcOperation{CreatedBy: procExecRow.CreatedUser})
	return
}

func parseQueryImportEntityRows(input []map[string]interface{}, orderAttr string) (result models.QueryImportEntityRows) {
	result = models.QueryImportEntityRows{}
	for _, v := range input {
		tmpRow := models.QueryImportEntityRow{Id: fmt.Sprintf("%s", v["id"]), DisplayName: fmt.Sprintf("%s", v["displayName"]), FirstOrder: 0, SecondOrder: 0}
		if orderAttr != "" {
			ignoreOrderFlag := false
			orderAttrList := strings.Split(orderAttr, ",")
			for i, attr := range orderAttrList {
				tmpOrderValue := 0
				if orderValue, ok := v[attr]; ok {
					tmpOrderValue, _ = strconv.Atoi(fmt.Sprintf("%s", orderValue))
					if tmpOrderValue < 0 {
						ignoreOrderFlag = true
						break
					}
				}
				if tmpOrderValue > 0 {
					if len(orderAttrList) == 1 {
						tmpRow.SecondOrder = tmpOrderValue
					} else {
						if i == 0 {
							tmpRow.FirstOrder = tmpOrderValue
						} else if i == 1 {
							tmpRow.SecondOrder = tmpOrderValue
						}
					}
				}
			}
			if ignoreOrderFlag {
				continue
			}
			if tmpRow.FirstOrder == 0 {
				tmpRow.FirstOrder = 100000
			}
			if tmpRow.SecondOrder == 0 {
				tmpRow.SecondOrder = 100000
			}
		}
		result = append(result, &tmpRow)
	}
	return
}
