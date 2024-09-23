package database

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"io"
	"os"
	"strings"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/tools"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/bash"
)

// transImportDetailMap 导入
var transImportDetailMap = map[models.TransImportStep]string{
	models.TransImportStepRole:             "role",
	models.TransImportStepWorkflow:         "workflow",
	models.TransImportStepComponentLibrary: "componentLibrary",
	models.TransImportStepBatchExecution:   "batchExecution",
	models.TransImportStepRequestTemplate:  "requestTemplate",
	models.TransImportStepCmdb:             "wecmdb",
	models.TransImportStepArtifacts:        "artifacts",
	models.TransImportStepMonitor:          "monitor",
	models.TransImportStepPluginConfig:     "pluginConfig",
}

const (
	tempTransImportDir = "/tmp/trans_import/%s"
)

// DecompressExportZip 导出压缩文件解压
func DecompressExportZip(ctx context.Context, nexusUrl, transImportId string) (localPath string, err error) {
	// 获取nexus配置
	nexusConfig, getNexusConfigErr := GetDataTransImportNexusConfig(ctx)
	if getNexusConfigErr != nil {
		err = getNexusConfigErr
		return
	}
	// 建临时目录
	var exportFileName, localExportFilePath string
	if lastPathIndex := strings.LastIndex(nexusUrl, "/"); lastPathIndex > 0 {
		exportFileName = nexusUrl[lastPathIndex+1:]
	}
	tmpImportDir := fmt.Sprintf(tempTransImportDir, transImportId)
	localExportFilePath = fmt.Sprintf("%s/%s", tmpImportDir, exportFileName)
	if err = os.MkdirAll(tmpImportDir, 0755); err != nil {
		err = fmt.Errorf("make tmp import dir fail,%s ", err.Error())
		return
	}
	// 从nexus下载
	downloadParam := tools.NexusReqParam{
		UserName:   nexusConfig.NexusUser,
		Password:   nexusConfig.NexusPwd,
		RepoUrl:    nexusConfig.NexusUrl,
		Repository: nexusConfig.NexusRepo,
		TimeoutSec: 60,
		FileParams: []*tools.NexusFileParam{{SourceFilePath: nexusUrl, DestFilePath: localExportFilePath}},
	}
	if err = tools.DownloadFile(&downloadParam); err != nil {
		err = fmt.Errorf("donwload nexus import file fail,%s ", err.Error())
		return
	}
	// 解压
	localPath, err = bash.DecompressFile(localExportFilePath, tmpImportDir)
	return
}

func RemoveTempExportDir(path string) (err error) {
	// 删除导出目录
	if err = os.RemoveAll(path); err != nil {
		log.Logger.Error("delete fail", log.String("path", path), log.Error(err))
	}
	return
}

func ParseJsonFile(jsonPath string) (byteValue []byte, err error) {
	var file *os.File
	file, err = os.Open(jsonPath)
	if err != nil {
		fmt.Println("Error opening JSON file:", err)
		return
	}
	defer file.Close()
	// 读取文件内容
	byteValue, _ = io.ReadAll(file)
	return
}

func GetBusinessList(localPath string) (result models.GetBusinessListRes, err error) {
	var envByteArr, productByteArr []byte
	envFilePath := fmt.Sprintf("%s/export/env.json", localPath)
	productFilePath := fmt.Sprintf("%s/export/product.json", localPath)
	if envByteArr, err = ParseJsonFile(envFilePath); err != nil {
		return
	}
	if productByteArr, err = ParseJsonFile(productFilePath); err != nil {
		return
	}
	if err = json.Unmarshal(envByteArr, &result.Environment); err != nil {
		log.Logger.Error("Environment json Unmarshal err", log.Error(err))
		return
	}
	if err = json.Unmarshal(productByteArr, &result.BusinessList); err != nil {
		log.Logger.Error("Product json Unmarshal err", log.Error(err))
		return
	}
	return
}

// GetImportDetail 获取导入详情
func GetImportDetail(ctx context.Context, transImportId string) (detail *models.TransImportDetail, err error) {
	var transImport *models.TransImportTable
	var transImportDetailList []*models.TransImportDetailTable
	if transImport, err = GetTransImport(ctx, transImportId); err != nil {
		return
	}
	if transImportDetailList, err = getTransImportDetail(ctx, transImportId); err != nil {
		return
	}
	detail = &models.TransImportDetail{
		TransImport:         transImport,
		CmdbCI:              make([]*models.CommonNameCount, 0),
		CmdbView:            make([]*models.CommonNameCreator, 0),
		CmdbViewCount:       0,
		CmdbReportForm:      make([]*models.CommonNameCreator, 0),
		CmdbReportFormCount: 0,
	}
	for _, transImportDetail := range transImportDetailList {
		var data interface{}
		if strings.TrimSpace(transImportDetail.Input) != "" && transImportDetail.Step != int(models.TransImportStepComponentLibrary) {
			if err = json.Unmarshal([]byte(transImportDetail.Input), &data); err != nil {
				log.Logger.Error("json Unmarshal err", log.Error(err))
			}
		}
		switch models.TransImportStep(transImportDetail.Step) {
		case models.TransImportStepRole:
			detail.Roles = &models.CommonOutput{
				Status: transImportDetail.Status,
				Output: data,
				ErrMsg: transImportDetail.ErrorMsg,
			}
		case models.TransImportStepRequestTemplate:
			detail.RequestTemplates = &models.CommonOutput{
				Status: transImportDetail.Status,
				Output: data,
				ErrMsg: transImportDetail.ErrorMsg,
			}
		case models.TransImportStepComponentLibrary:
			if transImportDetail.Input == "true" {
				detail.ExportComponentLibrary = true
			}
		case models.TransImportStepWorkflow:
			detail.Workflows = &models.CommonOutput{
				Status: transImportDetail.Status,
				Output: data,
				ErrMsg: transImportDetail.ErrorMsg,
			}
		case models.TransImportStepBatchExecution:
			detail.BatchExecution = &models.CommonOutput{
				Status: transImportDetail.Status,
				Output: data,
				ErrMsg: transImportDetail.ErrorMsg,
			}
		case models.TransImportStepPluginConfig:
			detail.Plugins = &models.CommonOutput{
				Status: transImportDetail.Status,
				Output: data,
				ErrMsg: transImportDetail.ErrorMsg,
			}
		case models.TransImportStepCmdb:
			var cmdbData models.CmdbData
			json.Unmarshal([]byte(transImportDetail.Input), &cmdbData)
			detail.CmdbCI = cmdbData.CmdbCI
			detail.CmdbView = cmdbData.CmdbView
			detail.CmdbReportForm = cmdbData.CmdbReportForm
			detail.CmdbViewCount = cmdbData.CmdbViewCount
			detail.CmdbReportFormCount = cmdbData.CmdbReportFormCount
			detail.Cmdb = &models.CommonOutput{
				Status: transImportDetail.Status,
				ErrMsg: transImportDetail.ErrorMsg,
			}
		case models.TransImportStepArtifacts:
			detail.Artifacts = &models.CommonOutput{
				Status: transImportDetail.Status,
				Output: data,
				ErrMsg: transImportDetail.ErrorMsg,
			}
		case models.TransImportStepMonitor:
			detail.Monitor = &models.CommonOutput{
				Status: transImportDetail.Status,
				Output: data,
				ErrMsg: transImportDetail.ErrorMsg,
			}
		}
	}
	return
}

// ExecImport 执行导入
func ExecImport(ctx context.Context, param models.ExecImportParam) (err error) {
	var transImport *models.TransImportTable
	var localPath string
	if transImport, err = GetTransImport(ctx, param.TransImportId); err != nil {
		return
	}
	// 文件解压
	if localPath, err = DecompressExportZip(ctx, param.ExportNexusUrl, param.TransImportId); err != nil {
		return
	}
	if transImport == nil || transImport.Id == "" {
		// 初始化导入
		if err = initTransImport(ctx, param.TransImportId, param.ExportNexusUrl, localPath, param.Operator); err != nil {
			return
		}
	}
	return
}

func initTransImport(ctx context.Context, transImportId, ExportNexusUrl, localPath, operator string) (err error) {
	var actions, addTransImportActions, addTransImportDetailActions []*db.ExecAction
	var envByteArr, uiDataArr []byte
	var detail *models.TransExportDetail
	var environmentMap map[string]string
	var associationSystemList, associationProductList []string
	var business, businessName string
	envFilePath := fmt.Sprintf("%s/export/env.json", localPath)
	uiDataPath := fmt.Sprintf("%s/export/ui-data.json", localPath)
	if envByteArr, err = ParseJsonFile(envFilePath); err != nil {
		return
	}
	if uiDataArr, err = ParseJsonFile(uiDataPath); err != nil {
		return
	}
	if err = json.Unmarshal(envByteArr, &environmentMap); err != nil {
		log.Logger.Error("Environment json Unmarshal err", log.Error(err))
		return
	}
	if err = json.Unmarshal(uiDataArr, &detail); err != nil {
		log.Logger.Error("TransExportDetail json Unmarshal err", log.Error(err))
		return
	}
	if detail == nil {
		err = fmt.Errorf("get TransExportDetail empty")
		return
	}
	if len(environmentMap) == 0 {
		environmentMap = make(map[string]string)
	}
	if detail.TransExport != nil {
		associationSystemList = detail.TransExport.AssociationSystems
		associationProductList = detail.TransExport.AssociationTechProducts
		business = detail.TransExport.Business
		businessName = detail.TransExport.BusinessName
	}
	now := time.Now().Format(models.DateTimeFormat)
	// 新增导出记录
	transImport := models.TransImportTable{
		Id:                 transImportId,
		ImportUrl:          ExportNexusUrl,
		Business:           business,
		BusinessName:       businessName,
		Environment:        environmentMap["env_id"],
		EnvironmentName:    environmentMap["env_name"],
		Status:             string(models.TransImportStatusStart),
		AssociationSystem:  strings.Join(associationSystemList, ","),
		AssociationProduct: strings.Join(associationProductList, ","),
		CreatedUser:        operator,
		CreatedTime:        now,
		UpdatedUser:        operator,
		UpdatedTime:        now,
	}
	if addTransImportActions = getInsertTransImport(transImport); len(addTransImportActions) > 0 {
		actions = append(actions, addTransImportActions...)
	}
	// 新增导出记录详情
	if addTransImportDetailActions = getInsertTransImportDetail(transImportId, detail); len(addTransImportDetailActions) > 0 {
		actions = append(actions, addTransImportDetailActions...)
	}
	if len(actions) > 0 {
		if err = db.Transaction(actions, ctx); err != nil {
			log.Logger.Error("initTransImport Transaction err", log.Error(err))
		}
	}
	return
}

func GetTransImport(ctx context.Context, transImportId string) (transImport *models.TransImportTable, err error) {
	transImport = &models.TransImportTable{}
	_, err = db.MysqlEngine.Context(ctx).SQL("select * from trans_import where id=?", transImportId).Get(transImport)
	return
}

func getInsertTransImport(transImport models.TransImportTable) (actions []*db.ExecAction) {
	nowTime := time.Now()
	actions = []*db.ExecAction{}
	actions = append(actions, &db.ExecAction{Sql: "insert into trans_import(id,business,business_name,environment,environment_name,status,input_url,created_user,created_time,updated_user,updated_time) values (?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
		transImport.Id, transImport.Business, transImport.BusinessName, transImport.Environment, transImport.EnvironmentName, transImport.Status, transImport.ImportUrl, transImport.CreatedUser, nowTime, transImport.UpdatedUser, nowTime,
	}})
	return
}

func getTransImportDetail(ctx context.Context, transImportId string) (result []*models.TransImportDetailTable, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from trans_import_detail where trans_import=?", transImportId).Find(&result)
	return
}

func getInsertTransImportDetail(transImportId string, detail *models.TransExportDetail) (actions []*db.ExecAction) {
	var input string
	actions = []*db.ExecAction{}
	guids := guid.CreateGuidList(len(transImportDetailMap))
	i := 0
	for step, name := range transImportDetailMap {
		switch step {
		case models.TransImportStepRole:
			if detail.Roles != nil {
				tempByte, _ := json.Marshal(detail.Roles.Output)
				input = string(tempByte)
			}
		case models.TransImportStepRequestTemplate:
			if detail.RequestTemplates != nil {
				tempByte, _ := json.Marshal(detail.RequestTemplates.Output)
				input = string(tempByte)
			}
		case models.TransImportStepComponentLibrary:
			input = fmt.Sprintf("%t", detail.ExportComponentLibrary)
		case models.TransImportStepWorkflow:
			if detail.Workflows != nil {
				tempByte, _ := json.Marshal(detail.Workflows.Output)
				input = string(tempByte)
			}
		case models.TransImportStepBatchExecution:
			if detail.BatchExecution != nil {
				tempByte, _ := json.Marshal(detail.BatchExecution.Output)
				input = string(tempByte)
			}
		case models.TransImportStepPluginConfig:
			if detail.Plugins != nil {
				tempByte, _ := json.Marshal(detail.Plugins.Output)
				input = string(tempByte)
			}
		case models.TransImportStepCmdb:
			cmdbData := &models.CmdbData{
				CmdbCI:              detail.CmdbCI,
				CmdbView:            detail.CmdbView,
				CmdbViewCount:       detail.CmdbViewCount,
				CmdbReportForm:      detail.CmdbReportForm,
				CmdbReportFormCount: detail.CmdbReportFormCount,
			}
			tempByte, _ := json.Marshal(cmdbData)
			input = string(tempByte)
		case models.TransImportStepArtifacts:
			if detail.Artifacts != nil {
				tempByte, _ := json.Marshal(detail.Artifacts.Output)
				input = string(tempByte)
			}
		case models.TransImportStepMonitor:
			if detail.Monitor != nil {
				tempByte, _ := json.Marshal(detail.Monitor.Output)
				input = string(tempByte)
			}
		}
		actions = append(actions, &db.ExecAction{Sql: "insert into trans_import_detail(id,trans_import,name,step,status,input) values (?,?,?,?,?,?)", Param: []interface{}{
			guids[i], transImportId, name, step, models.TransExportStatusNotStart, input,
		}})
		i++
	}
	return
}
