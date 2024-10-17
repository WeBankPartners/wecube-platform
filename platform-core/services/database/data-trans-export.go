package database

import (
	"context"
	"encoding/json"
	"fmt"
	"os"
	"strconv"
	"strings"
	"time"

	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/tools"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote/monitor"
)

// transExportDetailMap 导出map
var transExportDetailMap = map[models.TransExportStep]string{
	models.TransExportStepRole:                "role",
	models.TransExportStepWorkflow:            "workflow",
	models.TransExportStepComponentLibrary:    "component_library",
	models.TransExportStepBatchExecution:      "batch_execution",
	models.TransExportStepRequestTemplate:     "request_template",
	models.TransExportStepCmdb:                "wecmdb",
	models.TransExportStepArtifacts:           "artifacts",
	models.TransExportStepMonitor:             "monitor",
	models.TransExportStepPluginConfig:        "plugin_config",
	models.TransExportStepCreateAndUploadFile: "create_and_upload_file",
	models.TransExportUIData:                  "ui_data",
}

const (
	zipFile           = "export.zip"
	tempWeCubeDataDir = "/tmp/wecube/%s"
	tempWeCubeZipDir  = "/tmp/wecube/zip"
)

func CreateExport(c context.Context, param models.CreateExportParam, operator string) (transExportId string, err error) {
	var actions, addTransExportActions, addTransExportDetailActions, analyzeDataActions []*db.ExecAction
	transExportId = fmt.Sprintf("tp_%s", guid.CreateGuid())
	transExport := models.TransExportTable{
		Id:              transExportId,
		Environment:     param.Env,
		EnvironmentName: param.EnvName,
		Business:        strings.Join(param.PIds, ","),
		BusinessName:    strings.Join(param.PNames, ","),
		Status:          string(models.TransExportStatusStart),
		CreatedUser:     operator,
		UpdatedUser:     operator,
		LastConfirmTime: param.LastConfirmTime,
	}
	// 新增导出记录
	if addTransExportActions = getInsertTransExport(transExport); len(addTransExportActions) > 0 {
		actions = append(actions, addTransExportActions...)
	}
	// 新增导出记录详情
	if addTransExportDetailActions = getInsertTransExportDetail(transExportId); len(addTransExportDetailActions) > 0 {
		actions = append(actions, addTransExportDetailActions...)
	}
	dataTransParam := &models.AnalyzeDataTransParam{
		TransExportId:   transExportId,
		Business:        param.PIds,
		Env:             param.Env,
		LastConfirmTime: param.LastConfirmTime,
	}
	pluginExportActions, analyzePluginErr := AnalyzePluginConfigDataExport(c, transExportId)
	if analyzePluginErr != nil {
		err = analyzePluginErr
		return
	}
	actions = append(actions, pluginExportActions...)
	if analyzeDataActions, err = AnalyzeCMDBDataExport(c, dataTransParam); err != nil {
		return
	}
	actions = append(actions, analyzeDataActions...)
	err = db.Transaction(actions, c)
	return
}

func UpdateExport(c context.Context, param models.UpdateExportParam, operator string) (err error) {
	var actions, addTransExportActions, deleteAnalyzeDataActions, analyzeDataActions []*db.ExecAction
	transExport := models.TransExportTable{
		Id:              param.TransExportId,
		Environment:     param.Env,
		EnvironmentName: param.EnvName,
		Business:        strings.Join(param.PIds, ","),
		BusinessName:    strings.Join(param.PNames, ","),
		Status:          string(models.TransExportStatusStart),
		UpdatedUser:     operator,
		LastConfirmTime: param.LastConfirmTime,
		UpdatedTime:     time.Now().Format(models.DateTimeFormat),
	}
	// 更新导出记录
	if addTransExportActions = getUpdateTransExport(transExport); len(addTransExportActions) > 0 {
		actions = append(actions, addTransExportActions...)
	}
	// 先删除分析数据
	if deleteAnalyzeDataActions = getDeleteTransExportAnalyzeDataActions(param.TransExportId); len(deleteAnalyzeDataActions) > 0 {
		actions = append(actions, deleteAnalyzeDataActions...)
	}
	pluginExportActions, analyzePluginErr := AnalyzePluginConfigDataExport(c, param.TransExportId)
	if analyzePluginErr != nil {
		err = analyzePluginErr
		return
	}
	actions = append(actions, pluginExportActions...)
	dataTransParam := &models.AnalyzeDataTransParam{
		TransExportId:   param.TransExportId,
		Business:        param.PIds,
		Env:             param.Env,
		LastConfirmTime: param.LastConfirmTime,
	}
	if analyzeDataActions, err = AnalyzeCMDBDataExport(c, dataTransParam); err != nil {
		return
	}
	actions = append(actions, analyzeDataActions...)
	err = db.Transaction(actions, c)
	return
}

func GetAllTransExportOptions(ctx context.Context) (options models.TransExportHistoryOptions, err error) {
	var BusinessHashMap = make(map[string]string)
	var operatorHashMap = make(map[string]bool)
	var list []*models.TransExportTable
	options = models.TransExportHistoryOptions{
		BusinessList: make([]*models.Business, 0),
		Operators:    []string{},
	}
	if list, err = GetAllTransExport(ctx); err != nil {
		return
	}
	if len(list) > 0 {
		for _, transExport := range list {
			strArr := strings.Split(transExport.Business, ",")
			strArr2 := strings.Split(transExport.BusinessName, ",")
			if len(strArr) > 0 && len(strArr2) > 0 && len(strArr) == len(strArr2) {
				for i, s2 := range strArr {
					BusinessHashMap[s2] = strArr2[i]
				}
			}
			operatorHashMap[transExport.UpdatedUser] = true
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

func GetAllTransExport(ctx context.Context) (list []*models.TransExportTable, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from trans_export").Find(&list)
	return
}

func QueryTransExportByCondition(ctx context.Context, param models.TransExportHistoryParam) (pageInfo models.PageInfo, list []*models.TransExportTable, err error) {
	var sql = "select * from trans_export where 1=1"
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

func AutoAppendExportParam(ctx context.Context, userToken, language string, param models.DataTransExportParam) (newParam models.DataTransExportParam, err error) {
	var monitorWorkflowList []*monitor.WorkflowDto
	var procDefDto *models.ProcDefDto
	newParam = param
	// 拼接导出看板map
	if newParam.ExportDashboardMap, err = GetExportDashboardMap(ctx, param.TransExportId, userToken); err != nil {
		return
	}
	// 根据指标阈值&关键字 追加导出编排
	if monitorWorkflowList, err = monitor.GetMonitorWorkflow(userToken); err != nil {
		return
	}
	for _, workflow := range monitorWorkflowList {
		if procDefDto, err = GetProcessDefinitionByName(ctx, workflow.Name, workflow.Version); err != nil {
			return
		}
		newParam.WorkflowIds = append(newParam.WorkflowIds, procDefDto.Id)
	}
	return
}

func GetDashboardTransExportAnalyzeData(ctx context.Context, transExportId string) (dashboardAnalyze models.TransExportAnalyzeDataTable, err error) {
	_, err = db.MysqlEngine.Context(ctx).SQL("select * from trans_export_analyze_data where trans_export=? and data_type=? limit 1",
		transExportId, "dashboard").Get(&dashboardAnalyze)
	return
}

func GetSimpleTranExport(ctx context.Context, transExportId string) (transExport models.TransExportTable, err error) {
	_, err = db.MysqlEngine.Context(ctx).SQL("select * from trans_export where id=?", transExportId).Get(&transExport)
	return
}

func GetTransExportDetail(ctx context.Context, transExportId string) (detail *models.TransExportDetail, err error) {
	var transExport models.TransExportTable
	var dataTransVariableConfig *models.TransDataVariableConfig
	var transExportDetailList []*models.TransExportDetailTable
	var systemAnalyzeData, productAnalyzeData, ciGroupAnalyzeData models.TransExportAnalyzeDataTable
	var systemAnalyzeDataMap, productAnalyzeDataMap map[string]map[string]string
	var transExportAnalyzeDataList []*models.TransExportAnalyzeDataTable
	var monitorList []*models.CommonNameCount
	var ciGroup = make(map[string]string)
	if _, err = db.MysqlEngine.Context(ctx).SQL("select * from trans_export where id=?", transExportId).Get(&transExport); err != nil {
		return
	}
	if err = db.MysqlEngine.Context(ctx).SQL("select * from trans_export_detail where trans_export=?", transExportId).Find(&transExportDetailList); err != nil {
		return
	}
	if dataTransVariableConfig, err = getDataTransVariableMap(ctx); err != nil {
		return
	}
	if dataTransVariableConfig == nil {
		return
	}
	// 查询关联底座产品
	if productAnalyzeData, err = GetTransExportAnalyzeDataByCond(ctx, transExportId, dataTransVariableConfig.TechProductCiType); err != nil {
		return
	}
	if productAnalyzeData.Data != "" {
		if err = json.Unmarshal([]byte(productAnalyzeData.Data), &productAnalyzeDataMap); err != nil {
			return
		}
		if len(productAnalyzeDataMap) > 0 {
			transExport.AssociationTechProducts = []string{}
			for _, productAnalyzeItemMap := range productAnalyzeDataMap {
				for key, value := range productAnalyzeItemMap {
					if key == "key_name" {
						transExport.AssociationTechProducts = append(transExport.AssociationTechProducts, value)
					}
				}
			}
		}
	}

	// 查询关联系统
	if systemAnalyzeData, err = GetTransExportAnalyzeDataByCond(ctx, transExportId, dataTransVariableConfig.SystemCiType); err != nil {
		return
	}
	if systemAnalyzeData.Data != "" {
		if err = json.Unmarshal([]byte(systemAnalyzeData.Data), &systemAnalyzeDataMap); err != nil {
			return
		}
		if len(systemAnalyzeDataMap) > 0 {
			transExport.AssociationSystems = []string{}
			for _, systemAnalyzeItemMap := range systemAnalyzeDataMap {
				for key, value := range systemAnalyzeItemMap {
					if key == "key_name" {
						transExport.AssociationSystems = append(transExport.AssociationSystems, value)
					}
				}
			}
		}
	}
	detail = &models.TransExportDetail{
		TransExport:    &transExport,
		CmdbCI:         make([]*models.CommonNameCount, 0),
		CmdbView:       make([]*models.CommonNameCreator, 0),
		CmdbReportForm: make([]*models.CommonNameCreator, 0),
		Monitor:        &models.CommonOutput{},
		Plugins:        &models.CommonOutput{},
	}
	// 查询CMDB CI group
	if ciGroupAnalyzeData, err = GetTransExportAnalyzeDataBySource(ctx, transExportId, string(models.TransExportAnalyzeSourceWeCmdbGroup)); err != nil {
		return
	}
	if ciGroupAnalyzeData.Data != "" {
		json.Unmarshal([]byte(ciGroupAnalyzeData.Data), &ciGroup)
	}
	for _, transExportDetail := range transExportDetailList {
		var tempArr []string
		var data interface{}
		if strings.TrimSpace(transExportDetail.Input) != "" {
			if err = json.Unmarshal([]byte(transExportDetail.Input), &tempArr); err != nil {
				log.Logger.Error("json Unmarshal Input err", log.Error(err))
			}
		}
		if strings.TrimSpace(transExportDetail.Output) != "" {
			if err = json.Unmarshal([]byte(transExportDetail.Output), &data); err != nil {
				log.Logger.Error("json Unmarshal Output err", log.Error(err))
			}
		}
		output := &models.CommonOutput{
			Ids:    tempArr,
			Status: transExportDetail.Status,
			Output: data,
			ErrMsg: transExportDetail.ErrorMsg,
		}
		switch models.TransExportStep(transExportDetail.Step) {
		case models.TransExportStepRole:
			detail.Roles = output
		case models.TransExportStepRequestTemplate:
			detail.RequestTemplates = output
		case models.TransExportStepComponentLibrary:
			detail.ComponentLibrary = output
			if transExportDetail.Status != string(models.TransExportStatusNotStart) {
				detail.ExportComponentLibrary = true
			}
		case models.TransExportStepWorkflow:
			detail.Workflows = output
		case models.TransExportStepBatchExecution:
			detail.BatchExecution = output
		case models.TransExportStepCreateAndUploadFile:
			detail.CreateAndUploadFile = output
		case models.TransExportStepPluginConfig:
			detail.Plugins = output
		case models.TransExportStepCmdb:
			detail.Cmdb = output
		case models.TransExportStepArtifacts:
			detail.Artifacts = output
		case models.TransExportStepMonitor:
			detail.Monitor = output
		}
	}
	// 查询CMDB CI信息
	if transExportAnalyzeDataList, err = QuerySimpleTransExportAnalyzeDataByTransExport(ctx, transExportId); err != nil {
		return
	}
	for _, transExportAnalyze := range transExportAnalyzeDataList {
		switch models.TransExportAnalyzeSource(transExportAnalyze.Source) {
		case models.TransExportAnalyzeSourceWeCmdb:
			detail.CmdbCI = append(detail.CmdbCI, &models.CommonNameCount{
				Name:  transExportAnalyze.DataTypeName,
				Count: transExportAnalyze.DataLen,
				Group: ciGroup[transExportAnalyze.DataType],
			})
		case models.TransExportAnalyzeSourceWeCmdbReport:
			var tempArr []map[string]interface{}
			if transExportAnalyze.Data != "" {
				json.Unmarshal([]byte(transExportAnalyze.Data), &tempArr)
				if len(tempArr) > 0 {
					for _, dataMap := range tempArr {
						detail.CmdbReportForm = append(detail.CmdbReportForm, &models.CommonNameCreator{
							Name:    fmt.Sprintf("%v", dataMap["name"]),
							Creator: fmt.Sprintf("%v", dataMap["createUser"]),
						})
					}
				}
			}
			detail.CmdbReportFormCount = transExportAnalyze.DataLen
		case models.TransExportAnalyzeSourceWeCmdbView:
			var tempArr []map[string]interface{}
			if transExportAnalyze.Data != "" {
				json.Unmarshal([]byte(transExportAnalyze.Data), &tempArr)
				if len(tempArr) > 0 {
					for _, dataMap := range tempArr {
						detail.CmdbView = append(detail.CmdbView, &models.CommonNameCreator{
							Name:    fmt.Sprintf("%v", dataMap["name"]),
							Creator: fmt.Sprintf("%v", dataMap["createUser"]),
						})
					}
				}
			}
			detail.CmdbViewCount = transExportAnalyze.DataLen
		case models.TransExportAnalyzeSourceMonitor:
			monitorList = append(monitorList, &models.CommonNameCount{
				Name:  transExportAnalyze.DataTypeName,
				Count: transExportAnalyze.DataLen,
			})
		case models.TransExportAnalyzeSourceArtifact:
			var dataList []*models.AnalyzeArtifactDisplayData
			if transExportAnalyze.Data != "" {
				json.Unmarshal([]byte(transExportAnalyze.Data), &dataList)
				detail.Artifacts.Output = &dataList
			}
		case models.TransExportAnalyzeSourcePluginPackage:
			var tempArr []map[string]interface{}
			if transExportAnalyze.Data != "" {
				json.Unmarshal([]byte(transExportAnalyze.Data), &tempArr)
				if len(tempArr) > 0 {
					var pluginPackageCount []*models.PluginPackageCount
					for _, dataMap := range tempArr {
						pluginInterfaceNum := 0
						systemVariableNum := 0
						switch dataMap["PluginInterfaceNum"].(type) {
						case int, int32, int64:
							pluginInterfaceNum = dataMap["PluginInterfaceNum"].(int)
						case float64, float32:
							pluginInterfaceNum = int(dataMap["PluginInterfaceNum"].(float64))
						}
						switch dataMap["SystemVariableNum"].(type) {
						case int, int32, int64:
							systemVariableNum = dataMap["SystemVariableNum"].(int)
						case float64, float32:
							systemVariableNum = int(dataMap["SystemVariableNum"].(float64))
						}
						pluginPackageCount = append(pluginPackageCount, &models.PluginPackageCount{
							Name:               fmt.Sprintf("%v", dataMap["PluginPackageName"]),
							PluginInterfaceNum: pluginInterfaceNum,
							SystemVariableNum:  systemVariableNum,
						})
					}
					detail.Plugins.Output = pluginPackageCount
				}
			}
		}
	}
	detail.Monitor.Output = monitorList
	return
}

func GetTransExportAnalyzeDataByCond(ctx context.Context, transExportId, dataType string) (analyzeData models.TransExportAnalyzeDataTable, err error) {
	_, err = db.MysqlEngine.Context(ctx).SQL("select * from trans_export_analyze_data where trans_export=? and data_type=? limit 1", transExportId, dataType).Get(&analyzeData)
	return
}

func GetTransExportAnalyzeDataBySource(ctx context.Context, transExportId, source string) (analyzeData models.TransExportAnalyzeDataTable, err error) {
	_, err = db.MysqlEngine.Context(ctx).SQL("select * from trans_export_analyze_data where trans_export=? and source=? limit 1", transExportId, source).Get(&analyzeData)
	return
}

func QuerySimpleTransExportAnalyzeDataByTransExport(ctx context.Context, transExportId string) (result []*models.TransExportAnalyzeDataTable, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from trans_export_analyze_data where trans_export=?", transExportId).Find(&result)
	return
}

// ExecTransExport 执行导出
func ExecTransExport(ctx context.Context, param models.DataTransExportParam, userToken, language string) {
	var queryRolesResponse models.QueryRolesResponse
	var queryRequestTemplatesResponse models.QueryRequestTemplatesResponse
	var queryComponentLibraryResponse models.QueryComponentLibraryResponse
	var procDefDto *models.ProcessDefinitionDto
	var procDefExportList, procDefExportMainList, procDefExportSubList []*models.ProcessDefinitionDto
	var procDefDataList []*models.ProcDefDto
	var batchExecutionTemplateList []*models.BatchExecutionTemplate
	var transDataVariableConfig *models.TransDataVariableConfig
	var subProcDefIds []string
	var uploadUrl, path, zipPath, pluginPath, monitorPath, exportDataPath string
	var err error
	var step models.TransExportStep
	var roleDisplayNameMap = make(map[string]string)
	path = fmt.Sprintf(tempWeCubeDataDir, param.TransExportId)
	if zipPath, err = tools.GetPath(tempWeCubeZipDir); err != nil {
		log.Logger.Error("getPath error", log.Error(err))
		return
	}
	if pluginPath, err = tools.GetPath(fmt.Sprintf("%s/plugin-config", path)); err != nil {
		return
	}
	if monitorPath, err = tools.GetPath(fmt.Sprintf("%s/monitor", path)); err != nil {
		return
	}
	if exportDataPath, err = tools.GetPath(fmt.Sprintf("%s/export", path)); err != nil {
		return
	}
	if transDataVariableConfig, err = getDataTransVariableMap(ctx); err != nil {
		return
	}
	// 如果有报错,更新导出记录状态失败
	defer func(step *models.TransExportStep) {
		if err != nil {
			// 查询是哪一步报错
			updateTransExportDetailFail(ctx, param.TransExportId, *step, err.Error())
			updateTransExportStatus(ctx, param.TransExportId, models.TransExportStatusFail)
		}
		// 删除导出目录
		if err = os.RemoveAll(path); err != nil {
			log.Logger.Error("delete fail", log.String("path", path), log.Error(err))
		}
		// 删除导出压缩包
		if err = os.Remove(zipPath + "/" + zipFile); err != nil {
			log.Logger.Error("delete fail", log.String("filePath", zipPath), log.Error(err))
		}
	}(&step)
	// 更新迁移导出表记录状态为执行中
	if err = updateTransExportStatus(ctx, param.TransExportId, models.TransExportStatusDoing); err != nil {
		log.Logger.Error("updateTransExportStatus error", log.Error(err))
		return
	}
	// 1. 导出选中角色
	log.Logger.Info("1. export role start!!!!")
	step = models.TransExportStepRole
	exportRoleStartTime := time.Now().Format(models.DateTimeFormat)
	if queryRolesResponse, err = remote.RetrieveAllLocalRoles("Y", userToken, language, false); err != nil {
		log.Logger.Error("remote retrieveAllLocalRoles error", log.Error(err))
		return
	}
	if len(queryRolesResponse.Data) > 0 {
		for _, role := range queryRolesResponse.Data {
			roleDisplayNameMap[role.Name] = role.DisplayName
		}
	}
	exportRoleParam := models.StepExportParam{
		Ctx:           ctx,
		Path:          path,
		TransExportId: param.TransExportId,
		StartTime:     exportRoleStartTime,
		Step:          step,
		Input:         param.Roles,
		Data:          buildRoleResultData(param.Roles, queryRolesResponse.Data),
	}
	if err = execStepExport(exportRoleParam); err != nil {
		return
	}
	log.Logger.Info("1. export role end!!!!")
	// 2.导出请求模版&组件库
	if len(param.RequestTemplateIds) > 0 {
		log.Logger.Info("2. export requestTemplate start!!!!")
		step = models.TransExportStepRequestTemplate
		exportRequestTemplateStartTime := time.Now().Format(models.DateTimeFormat)
		if queryRequestTemplatesResponse, err = remote.GetRequestTemplates(models.GetRequestTemplatesDto{RequestTemplateIds: param.RequestTemplateIds}, userToken, language); err != nil {
			log.Logger.Error("remote GetRequestTemplates error", log.Error(err))
			return
		}
		exportRequestTemplateParam := models.StepExportParam{
			Ctx:           ctx,
			Path:          path,
			TransExportId: param.TransExportId,
			StartTime:     exportRequestTemplateStartTime,
			Step:          step,
			Input:         param.RequestTemplateIds,
			ExportData:    queryRequestTemplatesResponse.Data,
			Data:          convertRequestTemplateExportDto2List(queryRequestTemplatesResponse.Data, queryRolesResponse.Data),
		}
		if err = execStepExport(exportRequestTemplateParam); err != nil {
			return
		}
		// 请求模版关联的编排 自动加入 导出编排
		/*for _, requestTemplateExport := range queryRequestTemplatesResponse.Data {
			if strings.TrimSpace(requestTemplateExport.RequestTemplate.ProcDefId) != "" && !contains(param.WorkflowIds, requestTemplateExport.RequestTemplate.ProcDefId) {
				// 调用编排查询下数据是否真实存在
				if procDefDto, err = GetProcDefDetailByProcDefId(ctx, requestTemplateExport.RequestTemplate.ProcDefId); err != nil {
					continue
				}
				if procDefDto != nil && procDefDto.ProcDef != nil && procDefDto.ProcDef.Id != "" {
					param.WorkflowIds = append(param.WorkflowIds, requestTemplateExport.RequestTemplate.ProcDefId)
				}
			}
		}*/
		log.Logger.Info("2. export requestTemplate end!!!!")
	} else {
		updateTransExportDetailSuccess(ctx, param.TransExportId, models.TransExportStepRequestTemplate)
	}

	// 3. 导出组件库
	if param.ExportComponentLibrary {
		step = models.TransExportStepComponentLibrary
		log.Logger.Info("3. export componentLibrary start!!!!")
		exportComponentLibraryStartTime := time.Now().Format(models.DateTimeFormat)
		if queryComponentLibraryResponse, err = remote.GetComponentLibrary(userToken, language); err != nil {
			log.Logger.Error("remote GetComponentLibrary error", log.Error(err))
			return
		}
		exportComponentLibraryParam := models.StepExportParam{
			Ctx:           ctx,
			Path:          path,
			TransExportId: param.TransExportId,
			StartTime:     exportComponentLibraryStartTime,
			Step:          step,
			Data:          queryComponentLibraryResponse.Data,
		}
		if err = execStepExport(exportComponentLibraryParam); err != nil {
			return
		}
		log.Logger.Info("3. export componentLibrary start!!!!")
	} else {
		updateTransExportDetailSuccess(ctx, param.TransExportId, models.TransExportStepComponentLibrary)
	}

	// 4. 导出编排
	if len(param.WorkflowIds) > 0 {
		step = models.TransExportStepWorkflow
		log.Logger.Info("4. export workflow start!!!!")
		exportWorkflowStartTime := time.Now().Format(models.DateTimeFormat)
		// 编排ID数据去重复
		param.WorkflowIds = filterRepeatWorkflowId(param.WorkflowIds)
		for _, procDefId := range param.WorkflowIds {
			if procDefDto, err = GetProcDefDetailByProcDefId(ctx, procDefId); err != nil {
				log.Logger.Error("GetProcDefDetailByProcDefId error", log.Error(err), log.String("procDefId", procDefId))
				continue
			}
			if procDefDto != nil && procDefDto.ProcDef != nil {
				procDefDataList = append(procDefDataList, buildProcDefDto(procDefDto, roleDisplayNameMap))
				procDefExportMainList = append(procDefExportMainList, procDefDto)
			}
			// 导出编排节点里面如果关联子编排,子编排也需要导出
			if procDefDto.ProcDefNodeExtend != nil && len(procDefDto.ProcDefNodeExtend.Nodes) > 0 {
				for _, node := range procDefDto.ProcDefNodeExtend.Nodes {
					if node.ProcDefNodeCustomAttrs != nil && node.ProcDefNodeCustomAttrs.SubProcDefId != "" {
						subProcDefIds = append(subProcDefIds, node.ProcDefNodeCustomAttrs.SubProcDefId)
					}
				}
			}
			/*for _, subProcDefId := range subProcDefIds {
				if !contains(param.WorkflowIds, subProcDefId) {
					if procDefDto, err = GetProcDefDetailByProcDefId(ctx, subProcDefId); err != nil {
						log.Logger.Error("GetProcDefDetailByProcDefId error", log.Error(err))
						continue
					}
					param.WorkflowIds = append(param.WorkflowIds, subProcDefId)
					if procDefDto != nil && procDefDto.ProcDef != nil {
						procDefDataList = append(procDefDataList, buildProcDefDto(procDefDto, roleDisplayNameMap))
						procDefExportSubList = append(procDefExportSubList, procDefDto)
					}
				}
			}*/
		}
		// 汇总导出 编排,注意子编排需要放在前面导出,因为导入时候需要先导入子编排,主编排依赖子编排
		procDefExportList = append(procDefExportList, procDefExportSubList...)
		procDefExportList = append(procDefExportList, procDefExportMainList...)

		exportWorkflowParam := models.StepExportParam{
			Ctx:           ctx,
			Path:          path,
			TransExportId: param.TransExportId,
			StartTime:     exportWorkflowStartTime,
			Step:          step,
			Input:         param.WorkflowIds,
			Data:          procDefDataList,
			ExportData:    procDefExportList,
		}
		if err = execStepExport(exportWorkflowParam); err != nil {
			return
		}
		log.Logger.Info("4. export workflow end!!!!")
	}

	// 5.导出批量执行
	if len(param.BatchExecutionIds) > 0 {
		step = models.TransExportStepBatchExecution
		log.Logger.Info("5. export batchExecution start!!!!")
		exportBatchExecutionStartTime := time.Now().Format(models.DateTimeFormat)
		if batchExecutionTemplateList, err = ExportTemplate(ctx, userToken, &models.ExportBatchExecTemplateReqParam{BatchExecTemplateIds: param.BatchExecutionIds}); err != nil {
			log.Logger.Error("ExportTemplate error", log.Error(err))
			return
		}
		exportBatchExecutionParam := models.StepExportParam{
			Ctx:           ctx,
			Path:          path,
			TransExportId: param.TransExportId,
			StartTime:     exportBatchExecutionStartTime,
			Step:          step,
			Input:         param.BatchExecutionIds,
			Data:          batchExecutionTemplateList,
		}
		if err = execStepExport(exportBatchExecutionParam); err != nil {
			return
		}
		log.Logger.Info("5. export batchExecution end!!!!")
	}

	// 6. 导出插件配置
	step = models.TransExportStepPluginConfig
	log.Logger.Info("6. export pluginConfig start!!!!")
	exportPluginConfigStartTime := time.Now().Format(models.DateTimeFormat)
	// 插件配置导出文件比较多,创建plugin-config子目录导出
	if err = DataTransExportPluginConfig(ctx, param.TransExportId, pluginPath); err != nil {
		log.Logger.Error("DataTransExportPluginConfig error", log.Error(err))
		return
	}
	transExportPluginConfigDetail := models.TransExportDetailTable{
		TransExport: &param.TransExportId,
		Step:        int(step),
		StartTime:   exportPluginConfigStartTime,
		Status:      string(models.TransExportStatusSuccess),
		EndTime:     time.Now().Format(models.DateTimeFormat),
	}
	updateTransExportDetail(ctx, transExportPluginConfigDetail)
	log.Logger.Info("6. export pluginConfig end!!!!")
	// 7. 导出cmdb
	step = models.TransExportStepCmdb
	log.Logger.Info("7. export cmdb start!!!!")
	exportCmdbDataStartTime := time.Now().Format(models.DateTimeFormat)
	if err = DataTransExportCMDBData(ctx, param.TransExportId, path); err != nil {
		log.Logger.Error("DataTransExportCMDBData error", log.Error(err))
		return
	}
	transExportCmdbDataDetail := models.TransExportDetailTable{
		TransExport: &param.TransExportId,
		Step:        int(step),
		StartTime:   exportCmdbDataStartTime,
		Status:      string(models.TransExportStatusSuccess),
		EndTime:     time.Now().Format(models.DateTimeFormat),
	}
	updateTransExportDetail(ctx, transExportCmdbDataDetail)
	log.Logger.Info("7. export cmdb end!!!!")
	// 8.导出物料包
	log.Logger.Info("8. export artifact start!!!!")
	step = models.TransExportStepArtifacts
	exportArtifactStartTime := time.Now().Format(models.DateTimeFormat)
	if err = DataTransExportArtifactData(ctx, param.TransExportId); err != nil {
		log.Logger.Error("DataTransExportArtifactData error", log.Error(err))
		return
	}
	transExportArtifactDetail := models.TransExportDetailTable{
		TransExport: &param.TransExportId,
		Step:        int(step),
		StartTime:   exportArtifactStartTime,
		Status:      string(models.TransExportStatusSuccess),
		EndTime:     time.Now().Format(models.DateTimeFormat),
	}
	updateTransExportDetail(ctx, transExportArtifactDetail)
	log.Logger.Info("8. export artifact end!!!!")
	// 9. 导出监控
	step = models.TransExportStepMonitor
	log.Logger.Info("9. export monitor start!!!!")
	exportMonitorStartTime := time.Now().Format(models.DateTimeFormat)
	if err = exportMonitor(ctx, param.TransExportId, monitorPath, userToken, param.ExportDashboardMap); err != nil {
		return
	}
	transExportMonitorDetail := models.TransExportDetailTable{
		TransExport: &param.TransExportId,
		Step:        int(step),
		StartTime:   exportMonitorStartTime,
		Status:      string(models.TransExportStatusSuccess),
		EndTime:     time.Now().Format(models.DateTimeFormat),
	}
	updateTransExportDetail(ctx, transExportMonitorDetail)
	log.Logger.Info("9. export monitor end!!!!")

	// 10.导出系统变量参数
	step = models.TransExportUIData
	exportUIDataStartTime := time.Now().Format(models.DateTimeFormat)
	//  导出在导入时候需要展示的页面数据
	log.Logger.Info(" export ui show data start!!!!")
	if err = exportImportShowData(ctx, transDataVariableConfig, param.TransExportId, exportDataPath, userToken, language); err != nil {
		return
	}
	transExportUIDataDetail := models.TransExportDetailTable{
		TransExport: &param.TransExportId,
		Step:        int(step),
		StartTime:   exportUIDataStartTime,
		Status:      string(models.TransExportStatusSuccess),
		EndTime:     time.Now().Format(models.DateTimeFormat),
	}
	updateTransExportDetail(ctx, transExportUIDataDetail)
	log.Logger.Info(" export ui show data end!!!!")
	// 11. json文件压缩并上传nexus
	step = models.TransExportStepCreateAndUploadFile
	log.Logger.Info("11. create and upload file start!!!!")
	exportCreateAndUploadFileStartTime := time.Now().Format(models.DateTimeFormat)
	uploadReqParam := &tools.NexusReqParam{
		UserName:   transDataVariableConfig.NexusUser,
		Password:   transDataVariableConfig.NexusPwd,
		RepoUrl:    transDataVariableConfig.NexusUrl,
		Repository: transDataVariableConfig.NexusRepo,
		TimeoutSec: 60,
		FileParams: []*tools.NexusFileParam{
			{
				SourceFilePath: fmt.Sprintf("%s/%s", zipPath, zipFile),
				DestFilePath:   fmt.Sprintf("%s/%s", param.TransExportId, zipFile),
			},
		},
	}
	// CreateZipCompress 和 UploadFile分开调用,需要等创建zip文件关闭流再上传
	if err = tools.CreateZipCompress(zipPath, path, zipFile); err != nil {
		log.Logger.Error("CreateZipCompress error", log.Error(err))
		return
	}
	var result []*tools.NexusUploadFileRet
	if result, err = tools.UploadFile(uploadReqParam); err != nil {
		return
	}
	if len(result) > 0 {
		uploadUrl = result[0].StorePath
	}
	transExportCreateAndUploadFile := models.TransExportDetailTable{
		TransExport: &param.TransExportId,
		Step:        int(step),
		StartTime:   exportCreateAndUploadFileStartTime,
		Status:      string(models.TransExportStatusSuccess),
		EndTime:     time.Now().Format(models.DateTimeFormat),
	}
	updateTransExportDetail(ctx, transExportCreateAndUploadFile)
	log.Logger.Info("11. create and upload file end!!!!")
	updateTransExportSuccess(ctx, param.TransExportId, uploadUrl)
	return
}

func filterRepeatWorkflowId(ids []string) []string {
	var hashMap = make(map[string]bool)
	var newIds []string
	for _, id := range ids {
		hashMap[id] = true
	}
	for key, _ := range hashMap {
		newIds = append(newIds, key)
	}
	return newIds
}

func GetExportDashboardMap(ctx context.Context, transExportId, userToken string) (exportDashboardMap map[int][]string, err error) {
	var dashboardAnalyze models.TransExportAnalyzeDataTable
	var dashboardList []string
	var chartIds []string
	var dashboardIntId int
	var dashboard *monitor.CustomDashboardDto
	exportDashboardMap = make(map[int][]string)
	// 从导出分析表中获取导出看板信息
	if dashboardAnalyze, err = GetDashboardTransExportAnalyzeData(ctx, transExportId); err != nil {
		return
	}
	if dashboardAnalyze.Data != "" {
		if err = json.Unmarshal([]byte(dashboardAnalyze.Data), &dashboardList); err != nil {
			log.Logger.Error("json Unmarshal dashboard analyze err", log.Error(err))
			return
		}
		for _, dashboardId := range dashboardList {
			if dashboardIntId, err = strconv.Atoi(dashboardId); err != nil {
				log.Logger.Error("dashboardId  string convert int err", log.Error(err))
				return
			}
			if dashboard, err = monitor.QueryCustomDashboard(dashboardIntId, userToken); err != nil {
				log.Logger.Error("QueryCustomDashboard err", log.Error(err))
				return
			}
			if dashboard != nil {
				for _, chart := range dashboard.Charts {
					chartIds = append(chartIds, chart.Id)
				}
				log.Logger.Info("add dashboard", log.Int("dashboard", dashboardIntId))
				exportDashboardMap[dashboardIntId] = chartIds
			}
		}
	}
	return
}

// exportImportShowData 后面导入展示数据用
func exportImportShowData(ctx context.Context, dataTransVariableConfig *models.TransDataVariableConfig, transExportId, path, userToken, language string) (err error) {
	var transExport *models.TransExportTable
	var query models.QueryBusinessListParam
	var result []map[string]interface{}
	var environmentMap = make(map[string]string)
	var detail *models.TransExportDetail
	if transExport, err = GetTransExport(ctx, transExportId); err != nil {
		return
	}
	if transExport == nil {
		log.Logger.Error("exportImportShowData transExportId is invalid", log.String("transExportId", transExportId))
		return
	}
	environmentMap["env_id"] = transExport.Environment
	environmentMap["env_name"] = transExport.EnvironmentName
	if strings.TrimSpace(transExport.Business) != "" {
		query = models.QueryBusinessListParam{
			PackageName: "wecmdb",
			UserToken:   userToken,
			Language:    language,
			Entity:      dataTransVariableConfig.BusinessCiType,
			EntityQueryParam: models.EntityQueryParam{
				AdditionalFilters: make([]*models.EntityQueryObj, 0),
			},
		}
		businessIdArr := strings.Split(transExport.Business, ",")
		for _, id := range businessIdArr {
			var temp []map[string]interface{}
			query.EntityQueryParam = models.EntityQueryParam{
				AdditionalFilters: make([]*models.EntityQueryObj, 0),
			}
			query.EntityQueryParam.AdditionalFilters = append(query.EntityQueryParam.AdditionalFilters, &models.EntityQueryObj{
				AttrName:  "id",
				Op:        "eq",
				Condition: id,
			})
			if temp, err = remote.QueryBusinessList(query); err != nil {
				log.Logger.Error("remote QueryBusinessList err", log.Error(err))
				return
			}
			if len(temp) > 0 {
				result = append(result, temp...)
			}
		}
	}
	// 导出环境
	if err = tools.WriteJsonData2File(fmt.Sprintf("%s/env.json", path), environmentMap); err != nil {
		log.Logger.Error("WriteJsonData2File env.json err", log.Error(err))
		return
	}
	// 导出产品
	if err = tools.WriteJsonData2File(fmt.Sprintf("%s/product.json", path), result); err != nil {
		log.Logger.Error("WriteJsonData2File product.json err", log.Error(err))
		return
	}
	if detail, err = GetTransExportDetail(ctx, transExportId); err != nil {
		log.Logger.Error("GetTransExportDetail err", log.Error(err))
		return
	}
	// 导出ui数据,导入回显用
	if err = tools.WriteJsonData2File(fmt.Sprintf("%s/ui-data.json", path), detail); err != nil {
		log.Logger.Error("WriteJsonData2File ui-data err", log.Error(err))
	}
	return
}

func buildRoleResultData(roles []string, data []*models.SimpleLocalRoleDto) []*models.SimpleLocalRoleDto {
	var result []*models.SimpleLocalRoleDto
	for _, item := range data {
		for _, role := range roles {
			if item.Name == role {
				result = append(result, item)
				break
			}
		}
	}
	return result
}

// execStepExport 执行每步导出
func execStepExport(param models.StepExportParam) (err error) {
	if param.Data == nil {
		return
	}
	var exportData = param.Data
	transExportDetail := models.TransExportDetailTable{
		TransExport: &param.TransExportId,
		Step:        int(param.Step),
		StartTime:   param.StartTime,
		Status:      string(models.TransExportStatusSuccess),
	}
	if param.Input != nil {
		inputByteArr, _ := json.Marshal(param.Input)
		if string(inputByteArr) != "null" {
			transExportDetail.Input = string(inputByteArr)
		}
	}
	if param.Data != nil {
		outputByteArr, _ := json.Marshal(param.Data)
		if string(outputByteArr) != "null" {
			transExportDetail.Output = string(outputByteArr)
		}
	}
	if param.ExportData != nil {
		exportData = param.ExportData
	}
	if err = tools.WriteJsonData2File(getExportJsonFile(param.Path, transExportDetailMap[param.Step]), exportData); err != nil {
		log.Logger.Error("WriteJsonData2File error", log.String("name", transExportDetailMap[param.Step]), log.Error(err))
		return
	}
	transExportDetail.EndTime = time.Now().Format(models.DateTimeFormat)
	updateTransExportDetail(param.Ctx, transExportDetail)
	return
}

// exportMonitor 导出监控
func exportMonitor(ctx context.Context, transExportId, path, token string, exportDashboardMap map[int][]string) (err error) {
	var analyzeList []*models.TransExportAnalyzeDataTable
	var exportDataKeyList []string
	var finalExportDataList []interface{}
	var filePathList, monitorTypeMetricList, serviceGroupMetricList, endpointGroupMetricList []string
	var allMonitorEndpointGroupList, exportMonitorEndpointGroupList []*monitor.EndpointGroupTable
	var responseBytes []byte
	err = db.MysqlEngine.Context(ctx).SQL("select * from trans_export_analyze_data where trans_export=? and source=?",
		transExportId, models.TransExportAnalyzeSourceMonitor).Find(&analyzeList)
	if err != nil {
		return
	}
	// 监控分目录导出
	metricPath := fmt.Sprintf("%s/metric", path)
	serviceGroupPath := fmt.Sprintf("%s/service_group", metricPath)
	endpointGroupPath := fmt.Sprintf("%s/endpoint_group", metricPath)

	strategyPath := fmt.Sprintf("%s/strategy", path)
	dashboardPath := fmt.Sprintf("%s/dashboard", path)
	keywordPath := fmt.Sprintf("%s/keyword", path)
	logMonitorPath := fmt.Sprintf("%s/log_monitor", path)
	if err = os.MkdirAll(serviceGroupPath, 0755); err != nil {
		return
	}
	if err = os.MkdirAll(endpointGroupPath, 0755); err != nil {
		return
	}
	if err = os.MkdirAll(strategyPath, 0755); err != nil {
		return
	}
	if err = os.MkdirAll(dashboardPath, 0755); err != nil {
		return
	}
	if err = os.MkdirAll(keywordPath, 0755); err != nil {
		return
	}
	if err = os.MkdirAll(logMonitorPath, 0755); err != nil {
		return
	}
	for _, monitorAnalyzeData := range analyzeList {
		filePathList = []string{}
		finalExportDataList = []interface{}{}
		exportDataKeyList = []string{}
		if strings.TrimSpace(monitorAnalyzeData.Data) == "" {
			log.Logger.Info("analyze monitor data empty")
			continue
		}
		if err = json.Unmarshal([]byte(monitorAnalyzeData.Data), &exportDataKeyList); err != nil {
			err = fmt.Errorf("dataTypeName:%s,%+v", monitorAnalyzeData.DataTypeName, err.Error())
			log.Logger.Error("monitor json Unmarshal err", log.String("dataTypeName", monitorAnalyzeData.DataTypeName), log.Error(err))
			return
		}
		if len(exportDataKeyList) == 0 {
			log.Logger.Info("analyze monitor data empty")
			continue
		}
		switch models.TransExportAnalyzeMonitorDataType(monitorAnalyzeData.DataType) {
		case models.TransExportAnalyzeMonitorDataTypeMonitorType:
			// 导出基础类型
			filePathList = []string{fmt.Sprintf("%s/%s.json", path, monitorAnalyzeData.DataType)}
			finalExportDataList = []interface{}{exportDataKeyList}
		case models.TransExportAnalyzeMonitorDataTypeEndpointGroup:
			// 导出对象组
			filePathList = []string{fmt.Sprintf("%s/%s.json", path, monitorAnalyzeData.DataType)}
			if allMonitorEndpointGroupList, err = monitor.GetMonitorEndpointGroup(token); err != nil {
				log.Logger.Error("GetMonitorEndpointGroup  err", log.Error(err))
				return
			}
			for _, endpointGroupObj := range allMonitorEndpointGroupList {
				for _, eg := range exportDataKeyList {
					if endpointGroupObj.DisplayName == eg {
						exportMonitorEndpointGroupList = append(exportMonitorEndpointGroupList, endpointGroupObj)
					}
				}
			}
			finalExportDataList = []interface{}{exportMonitorEndpointGroupList}
		case models.TransExportAnalyzeMonitorDataTypeLogMonitorServiceGroup:
			// 导出指标的业务配置
			for _, serviceGroup := range exportDataKeyList {
				if responseBytes, err = monitor.ExportLogMetric(serviceGroup, token); err != nil {
					log.Logger.Error("ExportLogMetric err", log.JsonObj("serviceGroup", serviceGroup), log.Error(err))
					return
				}
				if isEffectiveJson(responseBytes) {
					var temp interface{}
					json.Unmarshal(responseBytes, &temp)
					filePathList = append(filePathList, fmt.Sprintf("%s/%s.json", logMonitorPath, serviceGroup))
					finalExportDataList = append(finalExportDataList, temp)
				}
			}
		case models.TransExportAnalyzeMonitorDataTypeLogMonitorTemplate:
			// 导出业务日志模版
			if responseBytes, err = monitor.ExportLogMonitorTemplate(exportDataKeyList, token); err != nil {
				log.Logger.Error("ExportLogMonitorTemplate err", log.StringList("LogMonitorTemplates", exportDataKeyList), log.Error(err))
				return
			}
			if isEffectiveJson(responseBytes) {
				var temp interface{}
				json.Unmarshal(responseBytes, &temp)
				filePathList = []string{fmt.Sprintf("%s/%s.json", path, models.TransExportAnalyzeMonitorDataTypeLogMonitorTemplate)}
				finalExportDataList = []interface{}{temp}
			}
		case models.TransExportAnalyzeMonitorDataTypeStrategyServiceGroup:
			// 导出指标阈值,层级对象
			for _, key := range exportDataKeyList {
				if responseBytes, err = monitor.ExportAlarmStrategy("service", key, token); err != nil {
					log.Logger.Error("ExportAlarmStrategy err", log.JsonObj("service", key), log.Error(err))
					return
				}
				if isEffectiveJson(responseBytes) {
					var temp interface{}
					json.Unmarshal(responseBytes, &temp)
					filePathList = append(filePathList, fmt.Sprintf("%s/%s_%s.json", strategyPath, models.TransExportAnalyzeMonitorDataTypeStrategyServiceGroup, key))
					finalExportDataList = append(finalExportDataList, temp)
				}
			}
		case models.TransExportAnalyzeMonitorDataTypeStrategyEndpointGroup:
			// 导出指标阈值,组
			for _, key := range exportDataKeyList {
				if responseBytes, err = monitor.ExportAlarmStrategy("group", key, token); err != nil {
					log.Logger.Error("ExportAlarmStrategy err", log.JsonObj("group", key), log.Error(err))
					return
				}
				if isEffectiveJson(responseBytes) {
					var temp interface{}
					json.Unmarshal(responseBytes, &temp)
					filePathList = append(filePathList, fmt.Sprintf("%s/%s_%s.json", strategyPath, models.TransExportAnalyzeMonitorDataTypeStrategyEndpointGroup, key))
					finalExportDataList = append(finalExportDataList, temp)
				}
			}
		case models.TransExportAnalyzeMonitorDataTypeLogKeywordServiceGroup:
			// 导出关键字
			for _, serviceGroup := range exportDataKeyList {
				if responseBytes, err = monitor.ExportKeyword(serviceGroup, token); err != nil {
					log.Logger.Error("ExportKeyword err", log.JsonObj("serviceGroup", serviceGroup), log.Error(err))
					return
				}
				if isEffectiveJson(responseBytes) {
					var temp interface{}
					json.Unmarshal(responseBytes, &temp)
					filePathList = append(filePathList, fmt.Sprintf("%s/%s.json", keywordPath, serviceGroup))
					finalExportDataList = append(finalExportDataList, temp)
				}
			}
		case models.TransExportAnalyzeMonitorDataTypeDashboard:
			// 导出看板
			for dashboardId, chartIds := range exportDashboardMap {
				log.Logger.Info("export dashboard", log.Int("dashboardId", dashboardId))
				if responseBytes, err = monitor.ExportCustomDashboard(dashboardId, chartIds, token); err != nil {
					log.Logger.Error("ExportCustomDashboard err", log.JsonObj("dashboardId", dashboardId), log.Error(err))
					return
				}
				if isEffectiveJson(responseBytes) {
					var temp interface{}
					json.Unmarshal(responseBytes, &temp)
					filePathList = append(filePathList, fmt.Sprintf("%s/%d.json", dashboardPath, dashboardId))
					finalExportDataList = append(finalExportDataList, temp)
				}
			}
		case models.TransExportAnalyzeMonitorDataTypeCustomMetricMonitorType:
			// 导出指标列表基础类型
			monitorTypeMetricList = exportDataKeyList
		case models.TransExportAnalyzeMonitorDataTypeCustomMetricServiceGroup:
			// 导出指标列表层级对象
			serviceGroupMetricList = exportDataKeyList
		case models.TransExportAnalyzeMonitorDataTypeCustomMetricEndpointGroup:
			// 导出指标列表对象组
			endpointGroupMetricList = exportDataKeyList
		case models.TransExportAnalyzeMonitorDataTypeServiceGroup:
			// 导出 层级对象
			filePathList = []string{fmt.Sprintf("%s/%s.json", path, monitorAnalyzeData.DataType)}
			finalExportDataList = []interface{}{exportDataKeyList}
		default:
		}
		for i, filePath := range filePathList {
			if err = tools.WriteJsonData2File(filePath, finalExportDataList[i]); err != nil {
				log.Logger.Error("WriteJsonData2File error", log.String("dataType", monitorAnalyzeData.DataType), log.Error(err))
				return
			}
		}
	}
	// 导出指标配置(包括基础指标、层级对象、对象组指标列表)&同环比指标也需要导出
	dto := models.ExportMetricListDto{
		MonitorTypeMetricList:   monitorTypeMetricList,
		ServiceGroupMetricList:  serviceGroupMetricList,
		EndpointGroupMetricList: endpointGroupMetricList,
		MetricPath:              metricPath,
		ServiceGroupPath:        serviceGroupPath,
		EndpointGroupPath:       endpointGroupPath,
		Token:                   token,
	}
	if err = exportMetricList(dto); err != nil {
		return
	}
	return
}

func exportMetricList(param models.ExportMetricListDto) (err error) {
	var responseBytes []byte
	var requestParam []monitor.ExportMetricParam

	for _, s := range param.MonitorTypeMetricList {
		requestParam = append(requestParam, []monitor.ExportMetricParam{{MonitorType: s, Comparison: "N", FilePath: fmt.Sprintf("%s/%s.json", param.MetricPath, s)},
			{MonitorType: s, Comparison: "Y", FilePath: fmt.Sprintf("%s/%s_comparison.json", param.MetricPath, s)}}...)
	}
	for _, s := range param.ServiceGroupMetricList {
		requestParam = append(requestParam, []monitor.ExportMetricParam{{ServiceGroup: s, MonitorType: "process", Comparison: "N", FilePath: fmt.Sprintf("%s/%s.json", param.ServiceGroupPath, s)},
			{ServiceGroup: s, MonitorType: "process", Comparison: "Y", FilePath: fmt.Sprintf("%s/%s_comparison.json", param.ServiceGroupPath, s)}}...)
	}
	for _, s := range param.EndpointGroupMetricList {
		requestParam = append(requestParam, []monitor.ExportMetricParam{{EndpointGroup: s, Comparison: "N", FilePath: fmt.Sprintf("%s/%s.json", param.EndpointGroupPath, s)},
			{EndpointGroup: s, Comparison: "Y", FilePath: fmt.Sprintf("%s/%s_comparison.json", param.EndpointGroupPath, s)}}...)
	}
	for _, requestParam := range requestParam {
		if responseBytes, err = monitor.ExportMetricList(requestParam, param.Token); err != nil {
			log.Logger.Error("ExportMetricList err", log.JsonObj("requestParam", requestParam), log.Error(err))
			return
		}
		if isEffectiveJson(responseBytes) {
			var temp interface{}
			json.Unmarshal(responseBytes, &temp)
			if err = tools.WriteJsonData2File(requestParam.FilePath, temp); err != nil {
				log.Logger.Error("WriteJsonData2File error", log.JsonObj("requestParam", requestParam), log.Error(err))
				return
			}
		}
	}
	return
}

func updateTransExportStatus(ctx context.Context, id string, status models.TransExportStatus) (err error) {
	_, err = db.MysqlEngine.Context(ctx).Exec("update trans_export set status=?,updated_time=? where id=?", status, time.Now().Format(models.DateTimeFormat), id)
	return
}

func updateTransExportSuccess(ctx context.Context, id, uploadUrl string) (err error) {
	_, err = db.MysqlEngine.Context(ctx).Exec("update trans_export set status=?,output_url=?,updated_time=? where id=?",
		models.TransExportStatusSuccess, uploadUrl, time.Now().Format(models.DateTimeFormat), id)
	return
}

func updateTransExportDetail(ctx context.Context, transExportDetail models.TransExportDetailTable) (err error) {
	if transExportDetail.TransExport == nil || transExportDetail.Step == 0 {
		return
	}
	_, err = db.MysqlEngine.Context(ctx).Where("trans_export=? and step=?", transExportDetail.TransExport, transExportDetail.Step).Update(transExportDetail)
	return
}

func updateTransExportDetailSuccess(ctx context.Context, transExportId string, step models.TransExportStep) (err error) {
	if transExportId == "" || step == 0 {
		return
	}
	updateData := make(map[string]interface{})
	updateData["status"] = string(models.TransExportStatusSuccess)
	_, err = db.MysqlEngine.Context(ctx).Where("trans_export=? and step=?", transExportId, step).Update(updateData)
	return
}

func updateTransExportDetailFail(ctx context.Context, transExportId string, step models.TransExportStep, errMsg string) (err error) {
	_, err = db.MysqlEngine.Context(ctx).Exec("update trans_export_detail set status=?,error_msg=?,end_time=? where trans_export=? and step =?",
		models.TransExportStatusFail, errMsg, time.Now().Format(models.DateTimeFormat), transExportId, step)
	return
}

func GetTransExport(ctx context.Context, transExportId string) (transExport *models.TransExportTable, err error) {
	transExport = &models.TransExportTable{}
	_, err = db.MysqlEngine.Context(ctx).SQL("select * from trans_export where id=?", transExportId).Get(transExport)
	return
}

func getDeleteTransExportAnalyzeDataActions(transExportId string) (actions []*db.ExecAction) {
	actions = []*db.ExecAction{}
	actions = append(actions, &db.ExecAction{Sql: "delete from trans_export_analyze_data where trans_export=?", Param: []interface{}{transExportId}})
	return
}

func getInsertTransExportDetail(transExportId string) (actions []*db.ExecAction) {
	actions = []*db.ExecAction{}
	guids := guid.CreateGuidList(len(transExportDetailMap))
	i := 0
	for step, name := range transExportDetailMap {
		var dataSource string
		if step == models.TransExportStepCmdb || step == models.TransExportStepArtifacts || step == models.TransExportStepMonitor {
			dataSource = name
		}
		actions = append(actions, &db.ExecAction{Sql: "insert into trans_export_detail(id,trans_export,name,analyze_data_source,step,status) values (?,?,?,?,?,?)", Param: []interface{}{
			guids[i], transExportId, name, dataSource, step, models.TransExportStatusNotStart,
		}})
		i++
	}
	return
}

func convertRequestTemplateExportDto2List(exportList []models.RequestTemplateExport, allRoleList []*models.SimpleLocalRoleDto) []*models.RequestTemplateSimpleQuery {
	var list []*models.RequestTemplateSimpleQuery
	var mgmtRoles, useRoles []*models.RoleTable
	var allRoleMap = make(map[string]*models.SimpleLocalRoleDto)
	var displayName string
	for _, roleDto := range allRoleList {
		allRoleMap[roleDto.Name] = roleDto
	}
	for _, export := range exportList {
		mgmtRoles = []*models.RoleTable{}
		useRoles = []*models.RoleTable{}
		for _, templateRole := range export.RequestTemplateRole {
			if v, ok := allRoleMap[templateRole.Role]; ok {
				displayName = v.DisplayName
			} else {
				displayName = ""
			}
			if templateRole.RoleType == string(models.MGMT) {
				mgmtRoles = append(mgmtRoles, &models.RoleTable{Id: templateRole.Role, DisplayName: displayName})
			} else if templateRole.RoleType == string(models.USE) {
				useRoles = append(useRoles, &models.RoleTable{Id: templateRole.Role, DisplayName: displayName})
			}
		}
		list = append(list, &models.RequestTemplateSimpleQuery{
			RequestTemplateDto: export.RequestTemplate,
			MGMTRoles:          mgmtRoles,
			USERoles:           useRoles,
		})
	}
	return list
}

func getExportJsonFile(path, name string) string {
	return fmt.Sprintf("%s/%s.json", path, name)
}

func getSQL(status []string) string {
	var sql string
	for i := 0; i < len(status); i++ {
		if i == len(status)-1 {
			sql = sql + "'" + status[i] + "'"
		} else {
			sql = sql + "'" + status[i] + "',"
		}
	}
	return sql
}

func convertMap2Array(hashMap map[string]bool) []string {
	var options []string
	if len(hashMap) == 0 {
		return options
	}
	for s, _ := range hashMap {
		options = append(options, s)
	}
	return options
}

func contains(arr []string, val string) bool {
	elementMap := make(map[string]bool)
	for _, s := range arr {
		elementMap[s] = true
	}
	return elementMap[val]
}

func isEffectiveJson(responseBytes []byte) bool {
	str := string(responseBytes)
	if str == "" || str == "[]" || str == "{}" {
		return false
	}
	return true
}
