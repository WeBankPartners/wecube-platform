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
	models.TransImportStepPluginConfig:     "pluginConfig",
	models.TransImportStepComponentLibrary: "componentLibrary",
	models.TransImportStepBatchExecution:   "batchExecution",
	models.TransImportStepRequestTemplate:  "requestTemplate",
	models.TransImportStepCmdb:             "wecmdb",
	models.TransImportStepArtifacts:        "artifacts",
	models.TransImportStepMonitorBase:      "monitorBase",
	models.TransImportStepInitWorkflow:     "workflowInit",
	models.TransImportStepMonitorBusiness:  "monitorBusiness",
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

func parseJsonFile(jsonPath string) (byteValue []byte, err error) {
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

func ParseJsonData(jsonPath string, data interface{}) (err error) {
	var byteArr []byte
	var exist bool
	// 判断文件是否存在
	if exist, err = tools.PathExist(jsonPath); err != nil {
		return err
	}
	// 不存在直接返回
	if !exist {
		return
	}
	if byteArr, err = parseJsonFile(jsonPath); err != nil {
		return
	}
	if err = json.Unmarshal(byteArr, &data); err != nil {
		log.Logger.Error("ParseJsonData json Unmarshal err", log.Error(err), log.String("jsoPath", jsonPath))
		return
	}
	return
}

func GetBusinessList(localPath string) (result models.GetBusinessListRes, err error) {
	if err = ParseJsonData(fmt.Sprintf("%s/export/env.json", localPath), &result.Environment); err != nil {
		log.Logger.Error("Environment json Unmarshal err", log.Error(err))
		return
	}
	if err = ParseJsonData(fmt.Sprintf("%s/export/product.json", localPath), &result.BusinessList); err != nil {
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
		case models.TransImportStepMonitorBase:
			detail.MonitorBase = &models.CommonOutput{
				Status: transImportDetail.Status,
				Output: data,
				ErrMsg: transImportDetail.ErrorMsg,
			}
		case models.TransImportStepMonitorBusiness:
			detail.MonitorBusiness = &models.CommonOutput{
				Status: transImportDetail.Status,
				Output: data,
				ErrMsg: transImportDetail.ErrorMsg,
			}
		case models.TransImportStepInitWorkflow:
			detail.InitWorkflow = &models.CommonOutput{
				Status: transImportDetail.Status,
				Output: data,
				ErrMsg: transImportDetail.ErrorMsg,
			}
		}
	}
	// 计算web跳转到哪一步
	if detail.TransImport != nil && (transImport.Status == string(models.TransImportStatusDoing) || transImport.Status == string(models.TransImportStatusFail)) {
		detail.TransImport.Step = calcWebDisplayStep(transImportDetailList)
	}
	return
}

// web 第二步 导入基础数据, 第三步 执行自动化编排,第四步 配置监控
func calcWebDisplayStep(detailList []*models.TransImportDetailTable) int {
	var hashMap = make(map[models.TransImportStep]*models.TransImportDetailTable)
	for _, detail := range detailList {
		hashMap[models.TransImportStep(detail.Step)] = detail
	}
	// 监控业务配置是否完成
	if v, ok := hashMap[models.TransImportStepMonitorBusiness]; ok {
		if v.Status == string(models.TransImportStatusDoing) || v.Status == string(models.TransImportStatusSuccess) || v.Status == string(models.TransImportStatusFail) {
			return 4
		}
	}
	// 执行自动化是否完成
	if v, ok := hashMap[models.TransImportStepInitWorkflow]; ok {
		if v.Status == string(models.TransImportStatusDoing) || v.Status == string(models.TransImportStatusFail) {
			return 3
		}
	}
	return 2
}

func InitTransImport(ctx context.Context, transImportId, ExportNexusUrl, localPath, operator string) (err error) {
	var actions, addTransImportActions, addTransImportDetailActions, addTransImportSubAction []*db.ExecAction
	var detail *models.TransExportDetail
	var environmentMap map[string]string
	var associationSystemList, associationProductList []string
	var business, businessName string
	if err = ParseJsonData(fmt.Sprintf("%s/export/env.json", localPath), &environmentMap); err != nil {
		log.Logger.Error("Environment json Unmarshal err", log.Error(err))
		return
	}
	if err = ParseJsonData(fmt.Sprintf("%s/export/ui-data.json", localPath), &detail); err != nil {
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
		InputUrl:           ExportNexusUrl,
		Business:           business,
		BusinessName:       businessName,
		Environment:        environmentMap["env_id"],
		EnvironmentName:    environmentMap["env_name"],
		Status:             string(models.TransImportStatusDoing),
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
	// 新增导入操作记录
	transImportAction := models.TransImportActionTable{
		Id:          fmt.Sprintf("ta_%s", guid.CreateGuid()),
		TransImport: &transImportId,
		Action:      string(models.TransImportActionStart),
		CreatedUser: operator,
	}
	if addTransImportSubAction = getInsertTransImportAction(transImportAction); len(addTransImportSubAction) > 0 {
		actions = append(actions, addTransImportSubAction...)
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
	actions = append(actions, &db.ExecAction{Sql: "insert into trans_import(id,business,business_name,environment,environment_name,status," +
		"input_url,created_user,created_time,updated_user,updated_time,association_system,association_product) values (?,?,?,?,?,?,?,?,?,?,?,?,?)",
		Param: []interface{}{transImport.Id, transImport.Business, transImport.BusinessName, transImport.Environment, transImport.EnvironmentName,
			transImport.Status, transImport.InputUrl, transImport.CreatedUser, nowTime, transImport.UpdatedUser, nowTime, transImport.AssociationSystem,
			transImport.AssociationProduct}})
	return
}

func getTransImportDetail(ctx context.Context, transImportId string) (result []*models.TransImportDetailTable, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from trans_import_detail where trans_import=?", transImportId).Find(&result)
	return
}

func getInsertTransImportAction(transImportAction models.TransImportActionTable) (actions []*db.ExecAction) {
	nowTime := time.Now()
	actions = []*db.ExecAction{}
	actions = append(actions, &db.ExecAction{Sql: "insert into trans_import_action(id,trans_import,action,created_user,updated_time) values (?,?,?,?,?)",
		Param: []interface{}{transImportAction.Id, transImportAction.TransImport, transImportAction.Action, transImportAction.CreatedUser, nowTime}})
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
		case models.TransImportStepMonitorBase:
			if detail.Monitor != nil {
				tempByte, _ := json.Marshal(detail.Monitor.Output)
				input = string(tempByte)
			}
		case models.TransImportStepInitWorkflow:

		case models.TransImportStepMonitorBusiness:

		}
		actions = append(actions, &db.ExecAction{Sql: "insert into trans_import_detail(id,trans_import,name,step,status,input) values (?,?,?,?,?,?)", Param: []interface{}{
			guids[i], transImportId, name, step, models.TransExportStatusNotStart, input,
		}})
		i++
	}
	return
}

func GetAllTransImportOptions(ctx context.Context) (options models.TransExportHistoryOptions, err error) {
	var BusinessHashMap = make(map[string]string)
	var operatorHashMap = make(map[string]bool)
	var list []*models.TransImportTable
	options = models.TransExportHistoryOptions{
		BusinessList: make([]*models.Business, 0),
		Operators:    []string{},
	}
	if list, err = GetAllTransImport(ctx); err != nil {
		return
	}
	if len(list) > 0 {
		for _, transImport := range list {
			strArr := strings.Split(transImport.Business, ",")
			strArr2 := strings.Split(transImport.BusinessName, ",")
			if len(strArr) > 0 && len(strArr2) > 0 && len(strArr) == len(strArr2) {
				for i, s2 := range strArr {
					BusinessHashMap[s2] = strArr2[i]
				}
			}
			operatorHashMap[transImport.UpdatedUser] = true
		}
		for key, value := range BusinessHashMap {
			options.BusinessList = append(options.BusinessList, &models.Business{
				BusinessId:   key,
				BusinessName: value,
			})
		}
	}
	options.Operators = convertMap2Array(operatorHashMap)
	return
}

func GetAllTransImport(ctx context.Context) (list []*models.TransImportTable, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from trans_import").Find(&list)
	return
}

func QueryTransImportByCondition(ctx context.Context, param models.TransImportHistoryParam) (pageInfo models.PageInfo, list []*models.TransImportTable, err error) {
	var sql = "select * from trans_import where 1=1"
	var queryParam []interface{}
	pageInfo = models.PageInfo{
		StartIndex: param.StartIndex,
		PageSize:   param.PageSize,
	}
	if strings.TrimSpace(param.Id) != "" {
		sql += " and  id like ?"
		queryParam = append(queryParam, fmt.Sprintf("%%%s%%", param.Id))
	}
	if len(param.Status) > 0 {
		sql += " and status in (" + getSQL(param.Status) + ")"
	}
	if len(param.Business) > 0 {
		sql += " and ("
		for i, business := range param.Business {
			if i == 0 {
				sql += " business like ?"
			} else {
				sql += " or business like ?"
			}
			queryParam = append(queryParam, fmt.Sprintf("%%%s%%", business))
		}
		sql += " )"
	}
	if len(param.Operators) > 0 {
		sql += " and updated_user in (" + getSQL(param.Operators) + ")"
	}
	if param.ExecTimeStart != "" && param.ExecTimeEnd != "" {
		sql += " and updated_time >= '" + param.ExecTimeStart + "' and updated_time <= '" + param.ExecTimeEnd + "'"
	}
	pageInfo.TotalRows = queryCount(ctx, sql, queryParam...)
	// 排序
	sql += " order by updated_time desc"
	// 分页
	pageSql, pageParam := transPageInfoToSQL(pageInfo)
	sql += pageSql
	queryParam = append(queryParam, pageParam...)
	err = db.MysqlEngine.Context(ctx).SQL(sql, queryParam...).Find(&list)
	return
}

// GetLatestTransImportAction 获取最新的导入操作
func GetLatestTransImportAction(ctx context.Context, transImportId string) (transImportAction *models.TransImportActionTable, err error) {
	transImportAction = &models.TransImportActionTable{}
	_, err = db.MysqlEngine.Context(ctx).SQL("select action from trans_import_action where trans_import=? order by updated_time desc limit 1 ", transImportId).Get(transImportAction)
	return
}
