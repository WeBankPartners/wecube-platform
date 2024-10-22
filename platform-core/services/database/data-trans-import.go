package database

import (
	"context"
	"database/sql"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/cipher"
	"io"
	"os"
	"strings"
	"time"
	"xorm.io/xorm"

	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/tools"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/bash"
)

var baseMonitorList = []string{"monitor_type", "endpoint_group", "custom_metric_endpoint_group",
	"custom_metric_monitor_type", "log_monitor_template", "strategy_endpoint_group"}

// transImportDetailMap 导入
var transImportDetailMap = map[models.TransImportStep]string{
	models.TransImportStepRole:                 "role",
	models.TransImportStepWorkflow:             "workflow",
	models.TransImportStepPluginConfig:         "plugin_config",
	models.TransImportStepComponentLibrary:     "component_library",
	models.TransImportStepBatchExecution:       "batch_execution",
	models.TransImportStepRequestTemplate:      "request_template",
	models.TransImportStepCmdb:                 "wecmdb",
	models.TransImportStepArtifacts:            "artifacts",
	models.TransImportStepMonitorBase:          "monitor_base",
	models.TransImportStepWebBaseImportSuccess: "web_base_import_success",
	models.TransImportStepModifyNewEnvData:     "modify_new_env_data",
	models.TransImportStepInitWorkflow:         "workflow_init",
	models.TransImportStepMonitorBusiness:      "monitor_business",
}

const (
	TempTransImportDir = "/tmp/trans_import/%s"
)

// DecompressExportZip 导出压缩文件解压
func DecompressExportZip(ctx context.Context, nexusUrl, transImportId string) (localPath string, err error) {
	// 获取nexus配置
	nexusConfig, getNexusConfigErr := GetDataTransImportConfig(ctx)
	if getNexusConfigErr != nil {
		err = getNexusConfigErr
		return
	}
	// 建临时目录
	var exportFileName, localExportFilePath string
	if lastPathIndex := strings.LastIndex(nexusUrl, "/"); lastPathIndex > 0 {
		exportFileName = nexusUrl[lastPathIndex+1:]
	}
	tmpImportDir := fmt.Sprintf(TempTransImportDir, transImportId)
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
	if transImportDetailList, err = GetTransImportDetail(ctx, transImportId); err != nil {
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
				detail.ComponentLibrary = &models.ExportComponentLibrary{
					CommonOutput: models.CommonOutput{
						Status: transImportDetail.Status,
						ErrMsg: transImportDetail.ErrorMsg,
					},
					ExportComponentLibrary: true,
				}
			} else {
				detail.ComponentLibrary = &models.ExportComponentLibrary{
					CommonOutput: models.CommonOutput{
						Status: string(models.TransImportStatusSuccess),
					},
					ExportComponentLibrary: false,
				}
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
		case models.TransImportStepModifyNewEnvData:
			detail.ModifyNewEnvData = &models.CommonOutput{
				Status: transImportDetail.Status,
				Output: data,
				ErrMsg: transImportDetail.ErrorMsg,
			}
		case models.TransImportStepWebBaseImportSuccess:
			detail.WebImportBaseData = &models.CommonOutput{
				Status: transImportDetail.Status,
			}
		case models.TransImportStepMonitorBusiness:
			detail.MonitorBusiness = &models.CommonOutput{
				Status: transImportDetail.Status,
				Output: data,
				ErrMsg: transImportDetail.ErrorMsg,
			}
		case models.TransImportStepInitWorkflow:
			var ids []string
			var workflowList, procInsList []*models.ProcInsDetail
			var transImportExecList []*models.TransImportProcExecTable
			var procDef *models.ProcDef
			var procName, version string
			if transImportExecList, err = GetTransImportProcExecByDetailId(ctx, transImportDetail.Id); err != nil {
				return
			}
			for _, transImport := range transImportExecList {
				procName = ""
				version = ""
				if transImport.ProcIns != "" {
					ids = append(ids, transImport.ProcIns)
				} else {
					if procDef, err = GetProcessDefinition(ctx, transImport.ProcDef); err != nil {
						return
					}
					if procDef != nil {
						procName = procDef.Name
						version = procDef.Version
					}
					workflowList = append(workflowList, &models.ProcInsDetail{
						Id:                transImport.Id,
						ProcDefId:         transImport.ProcDef,
						ProcInstName:      procName,
						EntityDataId:      transImport.EntityDataId,
						EntityDisplayName: transImport.EntityDataName,
						Status:            transImport.Status,
						Operator:          transImport.CreatedUser,
						CreatedTime:       transImport.CreatedTime,
						UpdatedTime:       transImport.CreatedTime,
						UpdatedBy:         transImport.CreatedUser,
						Version:           version,
					})
				}
			}
			if procInsList, err = QueryProcInstanceByIds(ctx, ids); err != nil {
				log.Logger.Error("QueryProcInstanceByIds err", log.Error(err))
				return
			}
			workflowList = append(workflowList, procInsList...)
			detail.ProcInstance = &models.CommonOutput{
				Status: transImportDetail.Status,
				Output: workflowList,
				ErrMsg: transImportDetail.ErrorMsg,
			}
		}
	}
	// 计算web跳转到哪一步
	if detail.TransImport != nil && (transImport.Status == string(models.TransImportStatusDoing) || transImport.Status == string(models.TransImportStatusFail) || transImport.Status == string(models.TransImportStatusExit)) {
		detail.TransImport.WebStep = int(CalcWebDisplayStep(transImportDetailList))
	}
	return
}

// CalcWebDisplayStep 第二步 导入基础数据, 第三步 修改新环境数据,第四步 执行自动化编排,第五步 配置监控
func CalcWebDisplayStep(detailList []*models.TransImportDetailTable) models.ImportWebDisplayStep {
	var hashMap = make(map[models.TransImportStep]*models.TransImportDetailTable)
	for _, detail := range detailList {
		hashMap[models.TransImportStep(detail.Step)] = detail
	}
	// 监控业务配置是否完成
	if v, ok := hashMap[models.TransImportStepMonitorBusiness]; ok {
		if v.Status == string(models.TransImportStatusDoing) || v.Status == string(models.TransImportStatusSuccess) || v.Status == string(models.TransImportStatusFail) {
			return models.ImportWebDisplayStepFive
		}
	}
	// 执行自动化是否完成
	if v, ok := hashMap[models.TransImportStepInitWorkflow]; ok {
		if v.Status == string(models.TransImportStatusDoing) || v.Status == string(models.TransImportStatusSuccess) || v.Status == string(models.TransImportStatusFail) {
			return models.ImportWebDisplayStepFour
		}
	}
	// 修改新环境数据是否完成
	if v, ok := hashMap[models.TransImportStepModifyNewEnvData]; ok {
		if v.Status == string(models.TransImportStatusDoing) || v.Status == string(models.TransImportStatusSuccess) || v.Status == string(models.TransImportStatusFail) {
			return models.ImportWebDisplayStepThree
		}
	}
	return models.ImportWebDisplayStepTwo
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
		Id:          "t_imp_action_" + guid.CreateGuid(),
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

func GetTransImportDetail(ctx context.Context, transImportId string) (result []*models.TransImportDetailTable, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from trans_import_detail where trans_import=? order by step", transImportId).Find(&result)
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
	baseMonitorMap := convertArray2Map(baseMonitorList)
	i := 0
	var allMonitorList, baseMonitorList, businessMonitorList []*models.CommonNameCount
	for step, name := range transImportDetailMap {
		input = ""
		switch step {
		case models.TransImportStepRole:
			if detail.Roles != nil && detail.Roles.Output != nil {
				tempByte, _ := json.Marshal(detail.Roles.Output)
				input = string(tempByte)
			}
		case models.TransImportStepRequestTemplate:
			if detail.RequestTemplates != nil && detail.RequestTemplates.Output != nil {
				tempByte, _ := json.Marshal(detail.RequestTemplates.Output)
				input = string(tempByte)
			}
		case models.TransImportStepComponentLibrary:
			input = fmt.Sprintf("%t", detail.ExportComponentLibrary)
		case models.TransImportStepWorkflow:
			if detail.Workflows != nil && detail.Workflows.Output != nil {
				tempByte, _ := json.Marshal(detail.Workflows.Output)
				input = string(tempByte)
			}
		case models.TransImportStepBatchExecution:
			if detail.BatchExecution != nil && detail.BatchExecution.Output != nil {
				tempByte, _ := json.Marshal(detail.BatchExecution.Output)
				input = string(tempByte)
			}
		case models.TransImportStepPluginConfig:
			if detail.Plugins != nil && detail.Plugins.Output != nil {
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
			if detail.Artifacts != nil && detail.Artifacts.Output != nil {
				tempByte, _ := json.Marshal(detail.Artifacts.Output)
				input = string(tempByte)
			}
		case models.TransImportStepMonitorBase:
			if detail.Monitor != nil && detail.Monitor.Output != nil {
				tempByte, _ := json.Marshal(detail.Monitor.Output)
				json.Unmarshal(tempByte, &allMonitorList)
			}
			if len(allMonitorList) > 0 {
				for _, monitor := range allMonitorList {
					if baseMonitorMap[monitor.Name] {
						baseMonitorList = append(baseMonitorList, monitor)
					}
				}
				tempByte, _ := json.Marshal(baseMonitorList)
				input = string(tempByte)
			}
		case models.TransImportStepInitWorkflow:
			if detail.Workflows != nil {
				tempByte, _ := json.Marshal(detail.Workflows.WorkflowList)
				input = string(tempByte)
			}
		case models.TransImportStepMonitorBusiness:
			if detail.Monitor != nil && detail.Monitor.Output != nil {
				tempByte, _ := json.Marshal(detail.Monitor.Output)
				json.Unmarshal(tempByte, &allMonitorList)
			}
			if len(allMonitorList) > 0 {
				for _, monitor := range allMonitorList {
					if !baseMonitorMap[monitor.Name] {
						businessMonitorList = append(businessMonitorList, monitor)
					}
				}
				tempByte, _ := json.Marshal(businessMonitorList)
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
	_, err = db.MysqlEngine.Context(ctx).SQL("select * from trans_import_action where trans_import=? order by updated_time desc limit 1 ", transImportId).Get(transImportAction)
	return
}

func updateImportStatus(ctx context.Context, id, status, operator string) (err error) {
	_, err = db.MysqlEngine.Context(ctx).Exec("update trans_import set status=?,updated_user=?,updated_time=? where id=? ", status, operator, time.Now().Format(models.DateTimeFormat), id)
	return
}

// ModifyImportStatus 更新导入状态
func ModifyImportStatus(ctx context.Context, param models.UpdateImportStatusParam) (err error) {
	var transImportDetailList []*models.TransImportDetailTable
	var transImport *models.TransImportTable
	var actions []*db.ExecAction
	nowTime := time.Now().Format(models.DateTimeFormat)
	if transImport, err = GetTransImport(ctx, param.Id); err != nil {
		return
	}
	if transImport == nil {
		err = fmt.Errorf("invalid param id")
		return
	}
	if transImport.Status == string(models.TransImportStatusSuccess) {
		err = fmt.Errorf("import has finish")
		return
	}
	if transImportDetailList, err = GetTransImportDetail(ctx, param.Id); err != nil {
		return
	}
	switch param.Status {
	case "exit":
		// 添加终止指令
		transImportAction := models.TransImportActionTable{
			Id:          "t_imp_action_" + guid.CreateGuid(),
			TransImport: &param.Id,
			Action:      string(models.TransImportActionExit),
			CreatedUser: param.Operator,
		}
		actions = append(actions, &db.ExecAction{Sql: "insert into trans_import_action(id,trans_import,action,created_user,updated_time) values (?,?,?,?,?)",
			Param: []interface{}{transImportAction.Id, transImportAction.TransImport, transImportAction.Action, transImportAction.CreatedUser, nowTime}})
		// 修改导入状态为终止
		actions = append(actions, &db.ExecAction{Sql: "update trans_import set status=?,updated_user=?,updated_time=? where id=?", Param: []interface{}{
			models.TransImportStatusExit, param.Operator, nowTime, param.Id,
		}})
		err = db.Transaction(actions, ctx)
	case "completed":
		for _, transImportDetail := range transImportDetailList {
			if transImportDetail.Status != string(models.TransImportStatusSuccess) {
				err = fmt.Errorf("import %s err,update status fail", transImportDetail.Name)
				return
			}
		}
		err = updateImportStatus(ctx, param.Id, string(models.TransImportStatusSuccess), param.Operator)
	}
	return
}

func GetDataTransImportConfig(ctx context.Context) (result *models.TransDataImportConfig, err error) {
	result = &models.TransDataImportConfig{}
	var sysVarRows []*models.SystemVariables
	err = db.MysqlEngine.Context(ctx).SQL("select name,value,default_value from system_variables where status='active' and name like 'PLATFORM_IMPORT_%'").Find(&sysVarRows)
	if err != nil {
		err = fmt.Errorf("query system variable table fail,%s ", err.Error())
		return
	}
	for _, row := range sysVarRows {
		tmpValue := row.DefaultValue
		if row.Value != "" {
			tmpValue = row.Value
		}
		switch row.Name {
		case "PLATFORM_IMPORT_NEXUS_URL":
			result.NexusUrl = tmpValue
		case "PLATFORM_IMPORT_NEXUS_USER":
			result.NexusUser = tmpValue
		case "PLATFORM_IMPORT_NEXUS_PWD":
			result.NexusPwd = tmpValue
		case "PLATFORM_IMPORT_NEXUS_REPO":
			result.NexusRepo = tmpValue
		case "PLATFORM_IMPORT_NZ_CIDR":
			result.NetworkZoneCIDR = tmpValue
		case "PLATFORM_IMPORT_NSZ_CIDR":
			result.NetworkSubZoneCIDR = tmpValue
		case "PLATFORM_IMPORT_RT_CODE":
			result.RouteTableCode = tmpValue
		case "PLATFORM_IMPORT_BSG_NAME":
			result.BasicSecurityGroupKeyName = tmpValue
		case "PLATFORM_IMPORT_DC_REGION_NAME":
			result.DataCenterRegionKeyName = tmpValue
		case "PLATFORM_IMPORT_DC_AZ1_NAME":
			result.DataCenterAZ1KeyName = tmpValue
		case "PLATFORM_IMPORT_DC_AZ2_NAME":
			result.DataCenterAZ2KeyName = tmpValue
		case "PLATFORM_IMPORT_WECUBE_HOST_CODE":
			result.WecubeHostCode = tmpValue
		}
	}
	return
}

func GetTransImportWithDetail(ctx context.Context, transImportId string, withDetailData bool) (result *models.TransImportJobParam, err error) {
	var transImportRows []*models.TransImportTable
	err = db.MysqlEngine.Context(ctx).SQL("select * from trans_import where id=?", transImportId).Find(&transImportRows)
	if err != nil {
		err = fmt.Errorf("query trans import table fail,%s ", err.Error())
		return
	}
	if len(transImportRows) == 0 {
		err = fmt.Errorf("can not find trans import with id:%s ", transImportId)
		return
	}
	result = &models.TransImportJobParam{TransImport: transImportRows[0], Details: []*models.TransImportDetailTable{}}
	if withDetailData {
		err = db.MysqlEngine.Context(ctx).SQL("select * from trans_import_detail where trans_import=? order by step", transImportId).Find(&result.Details)
	} else {
		err = db.MysqlEngine.Context(ctx).SQL("select id,trans_import,name,step,status,error_msg,start_time,end_time from trans_import_detail where trans_import=? order by step", transImportId).Find(&result.Details)
	}
	if err != nil {
		err = fmt.Errorf("query trans import detail table fail,%s ", err.Error())
	}
	return
}

func UpdateTransImportDetailInput(ctx context.Context, transImportId string, step models.TransImportStep, input string) (err error) {
	_, err = db.MysqlEngine.Context(ctx).Exec("update trans_import_detail set input=?,status=?,end_time=? where trans_import=? and step=?", input,
		models.TransImportStatusSuccess, time.Now().Format(models.DateTimeFormat), transImportId, step)
	return
}

func UpdateTransImportDetailStatus(ctx context.Context, transImportId, transImportDetailId, status, output, errorMsg string) (err error) {
	var actions []*db.ExecAction
	nowTime := time.Now()
	if status == "success" {
		actions = append(actions, &db.ExecAction{Sql: "update trans_import_detail set status=?,`output`=?,end_time=? where id=?", Param: []interface{}{
			status, output, nowTime, transImportDetailId,
		}})
	} else if status == "fail" {
		actions = append(actions, &db.ExecAction{Sql: "update trans_import_detail set status=?,error_msg=?,end_time=? where id=?", Param: []interface{}{
			status, errorMsg, nowTime, transImportDetailId,
		}})
		actions = append(actions, &db.ExecAction{Sql: "update trans_import set status=?,updated_time=? where id=?", Param: []interface{}{
			status, nowTime, transImportId,
		}})
	} else if status == "doing" {
		actions = append(actions, &db.ExecAction{Sql: "update trans_import_detail set status=?,start_time=? where id=?", Param: []interface{}{
			status, nowTime, transImportDetailId,
		}})
	}
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = fmt.Errorf("update trans import detail status fail,%s ", err.Error())
		log.Logger.Error("UpdateTransImportDetailStatus fail", log.String("transImportId", transImportId), log.String("detailId", transImportDetailId), log.Error(err))
	}
	return
}

func RecordTransImportAction(ctx context.Context, callParam *models.CallTransImportActionParam) (err error) {
	if callParam.ActionId == "" {
		callParam.ActionId = "t_imp_action_" + guid.CreateGuid()
		_, err = db.MysqlEngine.Context(ctx).Exec("insert into trans_import_action(id,trans_import,trans_import_detail,`action`,created_user,updated_time) values (?,?,?,?,?,?)",
			callParam.ActionId, callParam.TransImportId, callParam.TransImportDetailId, callParam.Action, callParam.Operator, time.Now())
	} else {
		_, err = db.MysqlEngine.Context(ctx).Exec("update trans_import_action set error_msg=?,updated_time=? where id=?", callParam.ErrorMsg, time.Now(), callParam.ActionId)
	}
	return
}

func GetTransImportProcExecList(ctx context.Context) (result []*models.TransImportProcExecTable, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select t1.*,t2.status as `proc_ins_status` from trans_import_proc_exec t1 left join proc_ins t2 on t1.proc_ins=t2.id where t1.trans_import_detail in (select id from trans_import_detail where status='doing' and trans_import in (select id from trans_import where status='doing')) order by t1.trans_import_detail,t1.exec_order").Find(&result)
	if err != nil {
		err = fmt.Errorf("query trans import proc exec table fail,%s ", err.Error())
	}
	return
}

func DownloadImportArtifactPackages(ctx context.Context, nexusUrl, transImportId string) (localDir string, fileNameList []string, err error) {
	// 获取nexus配置
	nexusConfig, getNexusConfigErr := GetDataTransImportConfig(ctx)
	if getNexusConfigErr != nil {
		err = getNexusConfigErr
		return
	}
	// 提取导出id来拼物料包的url路径
	var nexusUrlPrefix, transExportId string
	urlSplitList := strings.Split(nexusUrl, "/")
	if len(urlSplitList) > 2 {
		transExportId = urlSplitList[len(urlSplitList)-2]
	}
	if lastIndex := strings.LastIndex(nexusUrl, "/"); lastIndex > 0 {
		nexusUrlPrefix = nexusUrl[:lastIndex]
	}
	// 查nexus目录下的文件列表
	fileNameList, err = tools.ListFilesInRepo(&tools.NexusReqParam{
		UserName:   nexusConfig.NexusUser,
		Password:   nexusConfig.NexusPwd,
		RepoUrl:    nexusConfig.NexusUrl,
		Repository: nexusConfig.NexusRepo,
		TimeoutSec: 60,
		DirPath:    fmt.Sprintf("/%s/%s", transExportId, models.TransArtifactPackageDirName),
	})
	if err != nil {
		err = fmt.Errorf("list nexus artifact dir file list fail,%s ", err.Error())
		return
	}
	if len(fileNameList) == 0 {
		return
	}
	// 建临时目录
	tmpImportDir := fmt.Sprintf(models.TransImportTmpDir, transImportId) + "/" + models.TransArtifactPackageDirName
	if err = os.MkdirAll(tmpImportDir, 0755); err != nil {
		err = fmt.Errorf("make tmp import dir fail,%s ", err.Error())
		return
	}
	for _, remoteFileName := range fileNameList {
		// 从nexus下载
		downloadParam := tools.NexusReqParam{
			UserName:   nexusConfig.NexusUser,
			Password:   nexusConfig.NexusPwd,
			RepoUrl:    nexusConfig.NexusUrl,
			Repository: nexusConfig.NexusRepo,
			TimeoutSec: 60,
			FileParams: []*tools.NexusFileParam{{SourceFilePath: fmt.Sprintf("%s/%s/%s", nexusUrlPrefix, models.TransArtifactPackageDirName, remoteFileName), DestFilePath: fmt.Sprintf("%s/%s", tmpImportDir, remoteFileName)}},
		}
		if err = tools.DownloadFile(&downloadParam); err != nil {
			err = fmt.Errorf("donwload nexus artifact file:%s fail,%s ", remoteFileName, err.Error())
			break
		}
	}
	if err != nil {
		if clearErr := os.RemoveAll(tmpImportDir); clearErr != nil {
			log.Logger.Error("download nexus artifact fail,try to clear artifact tmp dir fail ", log.Error(clearErr))
		}
	}
	return
}

func GetTransImportDetailInput(ctx context.Context, transImportDetailId string) (result string, err error) {
	queryRows, queryErr := db.MysqlEngine.Context(ctx).QueryString("select `input` from trans_import_detail where id=?", transImportDetailId)
	if queryErr != nil {
		err = fmt.Errorf("query trans import detail input data fail,%s ", queryErr.Error())
		return
	}
	if len(queryRows) > 0 {
		result = queryRows[0]["input"]
	}
	return
}

func CreateTransImportProcExecData(ctx context.Context, procExecList []*models.TransImportProcExecTable) (err error) {
	var actions []*db.ExecAction
	for _, v := range procExecList {
		actions = append(actions, &db.ExecAction{Sql: "insert into trans_import_proc_exec(id,trans_import_detail,proc_def,proc_def_key,proc_def_name,root_entity,entity_data_id,entity_data_name,exec_order,status,created_user,created_time) values (?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
			v.Id, v.TransImportDetail, v.ProcDef, v.ProcDefKey, v.ProcDefName, v.RootEntity, v.EntityDataId, v.EntityDataName, v.ExecOrder, v.Status, v.CreatedUser, v.CreatedTime,
		}})
	}
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = fmt.Errorf("create trans import proc exec data fail,%s ", err.Error())
	}
	return
}

func UpdateTransImportProcExec(ctx context.Context, param *models.TransImportProcExecTable) (affectRow bool, err error) {
	var execResult sql.Result
	if param.Status == models.TransImportInPreparationStatus {
		execResult, err = db.MysqlEngine.Context(ctx).Exec("update trans_import_proc_exec set status=? where id=? and status=?", models.TransImportInPreparationStatus, param.Id, models.JobStatusReady)
	} else if param.Status == models.JobStatusRunning {
		execResult, err = db.MysqlEngine.Context(ctx).Exec("update trans_import_proc_exec set status=?,proc_ins=?,start_time=? where id=?", models.JobStatusRunning, param.ProcIns, time.Now(), param.Id)
	} else if param.Status == models.JobStatusFail {
		execResult, err = db.MysqlEngine.Context(ctx).Exec("update trans_import_proc_exec set status=?,error_msg=? where id=?", models.JobStatusFail, param.ErrorMsg, param.Id)
	} else if param.Status == models.JobStatusReady {
		execResult, err = db.MysqlEngine.Context(ctx).Exec("update trans_import_proc_exec set status=? where id=? and status=?", models.JobStatusReady, param.Id, models.TransImportInPreparationStatus)
	} else if param.Status == models.JobStatusSuccess {
		execResult, err = db.MysqlEngine.Context(ctx).Exec("update trans_import_proc_exec set status=? where id=?", models.JobStatusSuccess, param.Id)
	} else {
		err = fmt.Errorf("status:%s illegal with proc exec update", param.Status)
		return
	}
	if err != nil {
		err = fmt.Errorf("update trans import proc exec status fail,%s ", err.Error())
		return
	}
	if execResult == nil {
		return
	}
	if rowAffectNum, _ := execResult.RowsAffected(); rowAffectNum > 0 {
		affectRow = true
	}
	return
}

func GetTransImportProcExecByDetailId(ctx context.Context, detailId string) (result []*models.TransImportProcExecTable, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from trans_import_proc_exec where trans_import_detail=?", detailId).Find(&result)
	return
}

func GetTransImportProcDefId(ctx context.Context, procDefId, procDefKey string) (resultProcDefId string, err error) {
	var procDefRows []*models.ProcDef
	err = db.MysqlEngine.Context(ctx).SQL("select id,`key`,`version` from proc_def where id=? or `key`=?", procDefId, procDefKey).Find(&procDefRows)
	if err != nil {
		err = fmt.Errorf("query proc def table fail,%s ", err.Error())
		return
	}
	if len(procDefRows) == 0 {
		err = fmt.Errorf("can not find proc def with id:%s or key:%s ", procDefId, procDefKey)
		return
	}
	for _, row := range procDefRows {
		if row.Id == procDefId {
			resultProcDefId = procDefId
			break
		}
	}
	if resultProcDefId != "" {
		return
	}
	var currentVersion string
	for _, row := range procDefRows {
		if tools.CompareVersion(row.Version, currentVersion) {
			resultProcDefId = row.Id
			currentVersion = row.Version
		}
	}
	return
}

func UpdateTransImportCMDBData(ctx context.Context, transImportParam *models.TransImportJobParam) (err error) {
	if transImportParam.ImportCustomFormData == nil {
		return
	}
	transImportConfig, getConfigErr := GetDataTransImportConfig(ctx)
	if getConfigErr != nil {
		err = fmt.Errorf("get trans import config fail,%s ", getConfigErr.Error())
		return
	}
	encryptSeed, getSeedErr := GetEncryptSeed(ctx)
	if getSeedErr != nil {
		err = fmt.Errorf("get encrypt seed fail,%s ", getSeedErr.Error())
		return
	}
	cmdbEngine, getDBErr := getCMDBPluginDBResource(ctx)
	if getDBErr != nil {
		err = getDBErr
		return
	}
	session := cmdbEngine.NewSession()
	session.Begin()
	err = execTransImportCMDBData(session, transImportParam, transImportConfig, encryptSeed)
	if err != nil {
		fmt.Printf("error:%s \n", err.Error())
		if rollbackErr := session.Rollback(); rollbackErr != nil {
			fmt.Printf("rollback err:%s \n", rollbackErr.Error())
		} else {
			fmt.Println("rollback done")
		}
	} else {
		if commitErr := session.Commit(); commitErr != nil {
			fmt.Printf("commit err:%s \n", commitErr.Error())
		} else {
			fmt.Println("commit done")
		}
	}
	session.Close()
	return
}

func execTransImportCMDBData(session *xorm.Session, transImportParam *models.TransImportJobParam, transImportConfig *models.TransDataImportConfig, encryptSeed string) (err error) {
	if transImportConfig.WecubeHostCode != "" {
		queryRows, queryErr := session.QueryString("select guid from host_resource where code=?", transImportConfig.WecubeHostCode)
		if queryErr != nil {
			err = queryErr
			return
		}
		if len(queryRows) > 0 {
			rowGuid := queryRows[0]["guid"]
			encryptPwd, encryptErr := cipher.AesEnPasswordByGuid(rowGuid, encryptSeed, transImportParam.ImportCustomFormData.WecubeHostPwd, "")
			if encryptErr != nil {
				err = fmt.Errorf("encrypt password fail,%s ", encryptErr.Error())
				return
			}
			if _, err = session.Exec("update host_resource set asset_id=?,root_user_password=? where guid=?", transImportParam.ImportCustomFormData.WecubeHostAssetId, encryptPwd, rowGuid); err != nil {
				return
			}
		}
	}
	if _, err = session.Exec("update network_zone set asset_id=? where cidr=?", transImportParam.ImportCustomFormData.NetworkZoneAssetId, transImportConfig.NetworkZoneCIDR); err != nil {
		return
	}
	if _, err = session.Exec("update network_subzone set asset_id=? where cidr=?", transImportParam.ImportCustomFormData.NetworkSubZoneAssetId, transImportConfig.NetworkSubZoneCIDR); err != nil {
		return
	}
	if _, err = session.Exec("update route_table set asset_id=? where code=?", transImportParam.ImportCustomFormData.RouteTableAssetId, transImportConfig.RouteTableCode); err != nil {
		return
	}
	if _, err = session.Exec("update basic_security_group set asset_id=? where key_name=?", transImportParam.ImportCustomFormData.BasicSecurityGroupAssetId, transImportConfig.BasicSecurityGroupKeyName); err != nil {
		return
	}
	if _, err = session.Exec("update data_center set asset_id=? where key_name=?", transImportParam.ImportCustomFormData.DataCenterRegionAssetId, transImportConfig.DataCenterRegionKeyName); err != nil {
		return
	}
	if _, err = session.Exec("update data_center set asset_id=? where key_name=?", transImportParam.ImportCustomFormData.DataCenterAZ1AssetId, transImportConfig.DataCenterAZ1KeyName); err != nil {
		return
	}
	if _, err = session.Exec("update data_center set asset_id=? where key_name=?", transImportParam.ImportCustomFormData.DataCenterAZ2AssetId, transImportConfig.DataCenterAZ2KeyName); err != nil {
		return
	}
	return
}
