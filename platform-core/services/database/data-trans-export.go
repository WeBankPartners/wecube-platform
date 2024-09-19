package database

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote/monitor"
	"os"
	"strings"
	"time"

	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/tools"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
)

// transExportDetailMap 导出map
var transExportDetailMap = map[models.TransExportStep]string{
	models.TransExportStepRole:             "role",
	models.TransExportStepWorkflow:         "workflow",
	models.TransExportStepComponentLibrary: "componentLibrary",
	models.TransExportStepBatchExecution:   "batchExecution",
	models.TransExportStepRequestTemplate:  "requestTemplate",
	models.TransExportStepCmdb:             "wecmdb",
	models.TransExportStepArtifacts:        "artifacts",
	models.TransExportStepMonitor:          "monitor",
	models.TransExportStepPluginConfig:     "pluginConfig",
}

const (
	zipFile           = "export.zip"
	tempWeCubeDataDir = "/tmp/wecube/%s"
	zip               = "zip"
)

func CreateExport2(c context.Context, param models.CreateExportParam, operator string) (transExportId string, err error) {
	var actions, addTransExportActions, addTransExportDetailActions, analyzeDataActions []*db.ExecAction
	transExportId = guid.CreateGuid()
	transExport := models.TransExportTable{
		Id:              transExportId,
		Environment:     param.Env,
		EnvironmentName: param.EnvName,
		Business:        strings.Join(param.PIds, ","),
		BusinessName:    strings.Join(param.PNames, ","),
		Status:          string(models.TransExportStatusStart),
		CreatedUser:     operator,
		UpdatedUser:     operator,
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
		TransExportId: transExportId,
		Business:      param.PIds,
		Env:           param.Env,
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
	}
	// 更新导出记录
	if addTransExportActions = getUpdateTransExport(transExport); len(addTransExportActions) > 0 {
		actions = append(actions, addTransExportActions...)
	}
	// 先删除分析数据
	if deleteAnalyzeDataActions = getDeleteTransExportAnalyzeDataActions(param.TransExportId); len(deleteAnalyzeDataActions) > 0 {
		actions = append(actions, deleteAnalyzeDataActions...)
	}
	dataTransParam := &models.AnalyzeDataTransParam{
		TransExportId: param.TransExportId,
		Business:      param.PIds,
		Env:           param.Env,
	}
	if analyzeDataActions, err = AnalyzeCMDBDataExport(c, dataTransParam); err != nil {
		return
	}
	actions = append(actions, analyzeDataActions...)
	err = db.Transaction(actions, c)
	return
}

func GetAllTransExportOptions(ctx context.Context) (options models.TransExportHistoryOptions, err error) {
	var BusinessHashMap = make(map[string]bool)
	var operatorHashMap = make(map[string]bool)
	var list []*models.TransExportTable
	if list, err = GetAllTransExport(ctx); err != nil {
		return
	}
	if len(list) > 0 {
		for _, transExport := range list {
			strArr := strings.Split(transExport.Business, ",")
			if len(strArr) > 0 {
				for _, s2 := range strArr {
					BusinessHashMap[s2] = true
				}
			}
			operatorHashMap[transExport.UpdatedUser] = true
		}
	}
	options.Business = convertMap2Array(BusinessHashMap)
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

// AutoAppendExportRoles 自动追加导出角色,并且记录需要导出的看板和图表Id
func AutoAppendExportRoles(ctx context.Context, userToken, language string, param models.DataTransExportParam) (newParam models.DataTransExportParam, err error) {
	var procDefPermissionList []*models.ProcDefPermission
	var batchExecutionTemplateRoles []*models.BatchExecutionTemplateRole
	var requestTemplateRoles models.QueryRequestTemplateRolesResponse
	var allCheckRoleMap = make(map[string]bool)
	var dashboardAnalyze models.TransExportAnalyzeDataTable
	var dashboardList []int
	var chartIds, roles, newCheckRoles []string
	var dashboard *monitor.CustomDashboardDto
	newParam = param
	newParam.ExportDashboardMap = make(map[int][]string)
	// 1.角色校验,编排、批量执行、请求模版的角色、看板&图表角色导出都需要自动加入.需要的角色自动加入
	if procDefPermissionList, err = GetProcDefPermissionByIds(ctx, param.WorkflowIds); err != nil {
		return
	}
	for _, permission := range procDefPermissionList {
		allCheckRoleMap[permission.Permission] = true
	}
	if batchExecutionTemplateRoles, err = GetBatchExecutionTemplatePermissionByIds(ctx, param.BatchExecutionIds); err != nil {
		return
	}
	for _, templateRole := range batchExecutionTemplateRoles {
		allCheckRoleMap[templateRole.RoleName] = true
	}
	if requestTemplateRoles, err = remote.GetRequestTemplateRoles(models.GetRequestTemplateRolesDto{RequestTemplateIds: param.RequestTemplateIds}, userToken, language); err != nil {
		return
	}
	for _, role := range requestTemplateRoles.Roles {
		allCheckRoleMap[role] = true
	}
	// 从导出分析表中获取导出看板信息
	if dashboardAnalyze, err = GetDashboardTransExportAnalyzeData(ctx, param.TransExportId); err != nil {
		return
	}
	if dashboardAnalyze.Data != "" {
		if err = json.Unmarshal([]byte(dashboardAnalyze.Data), &dashboardList); err != nil {
			log.Logger.Error("json Unmarshal dashboard analyze err", log.Error(err))
			return
		}
		for _, dashboardId := range dashboardList {
			chartIds = []string{}
			if dashboard, err = monitor.QueryCustomDashboard(dashboardId, userToken); err != nil {
				log.Logger.Error("QueryCustomDashboard err", log.Error(err))
				return
			}
			if dashboard != nil {
				for _, role := range dashboard.MgmtRoles {
					allCheckRoleMap[role] = true
				}
				for _, role := range dashboard.UseRoles {
					allCheckRoleMap[role] = true
				}
				for _, chart := range dashboard.Charts {
					chartIds = append(chartIds, chart.Id)
				}
				newParam.ExportDashboardMap[dashboardId] = chartIds
			}
		}
		// 查询图表的权限
		chartIds = []string{}
		for _, ids := range newParam.ExportDashboardMap {
			chartIds = append(chartIds, ids...)
		}
		if roles, err = monitor.QueryCustomChartPermissionBatch(chartIds, userToken); err != nil {
			log.Logger.Error("QueryCustomChartPermissionBatch err", log.Error(err))
			return
		}
		newCheckRoles = append(newCheckRoles, roles...)
	}
	newCheckRoles = append(newCheckRoles, param.Roles...)
	for role, _ := range allCheckRoleMap {
		newCheckRoles = append(newCheckRoles, role)
	}
	newParam.Roles = newCheckRoles
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
	var systemAnalyzeData, productAnalyzeData models.TransExportAnalyzeDataTable
	var systemAnalyzeDataMap, productAnalyzeDataMap map[string]map[string]string
	var transExportAnalyzeDataList []*models.TransExportAnalyzeDataTable
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
	if productAnalyzeData, err = GetTransExportAnalyzeDataByCond(ctx, transExportId, dataTransVariableConfig.ArtifactCiTechProduct); err != nil {
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
	if systemAnalyzeData, err = GetTransExportAnalyzeDataByCond(ctx, transExportId, dataTransVariableConfig.ArtifactCiSystem); err != nil {
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
		Monitor:        make([]*models.CommonNameCount, 0),
		Plugins:        make([]*models.PluginPackageCount, 0),
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
		}
		switch transExportDetail.Step {
		case int(models.TransExportStepRole):
			detail.Roles = output
		case int(models.TransExportStepRequestTemplate):
			detail.RequestTemplates = output
		case int(models.TransExportStepComponentLibrary):
			detail.ComponentLibrary = output
			if transExportDetail.Status != string(models.TransExportStatusNotStart) {
				detail.ExportComponentLibrary = true
			}
		case int(models.TransExportStepWorkflow):
			detail.Workflows = output
		case int(models.TransExportStepBatchExecution):
			detail.BatchExecution = output
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
						detail.CmdbView = append(detail.CmdbReportForm, &models.CommonNameCreator{
							Name:    fmt.Sprintf("%v", dataMap["name"]),
							Creator: fmt.Sprintf("%v", dataMap["createUser"]),
						})
					}
				}
			}
			detail.CmdbViewCount = transExportAnalyze.DataLen
		case models.TransExportAnalyzeSourceMonitor:
			detail.Monitor = append(detail.Monitor, &models.CommonNameCount{
				Name:  transExportAnalyze.DataTypeName,
				Count: transExportAnalyze.DataLen,
			})
		case models.TransExportAnalyzeSourceArtifact:
			var dataList []*models.AnalyzeArtifactDisplayData
			if transExportAnalyze.Data != "" {
				json.Unmarshal([]byte(transExportAnalyze.Data), &dataList)
				detail.Artifacts = dataList
			}
		case models.TransExportAnalyzeSourcePluginPackage:
			var tempArr []map[string]interface{}
			if transExportAnalyze.Data != "" {
				json.Unmarshal([]byte(transExportAnalyze.Data), &tempArr)
				if len(tempArr) > 0 {
					for _, dataMap := range tempArr {
						pluginInterfaceNum := 0
						systemVariableNum := 0
						switch dataMap["PluginInterfaceNum"].(type) {
						case int, int32, int64:
							pluginInterfaceNum = dataMap["PluginInterfaceNum"].(int)
						}
						switch dataMap["SystemVariableNum"].(type) {
						case int, int32, int64:
							systemVariableNum = dataMap["SystemVariableNum"].(int)
						}
						detail.Plugins = append(detail.Plugins, &models.PluginPackageCount{
							Name:               fmt.Sprintf("%v", dataMap["PluginPackageName"]),
							PluginInterfaceNum: pluginInterfaceNum,
							SystemVariableNum:  systemVariableNum,
						})
					}
				}
			}
		}
	}
	return
}

func GetTransExportAnalyzeDataByCond(ctx context.Context, transExportId string, dataType string) (analyzeData models.TransExportAnalyzeDataTable, err error) {
	_, err = db.MysqlEngine.Context(ctx).SQL("select * from trans_export_analyze_data where trans_export=? and data_type=? limit 1", transExportId, dataType).Get(&analyzeData)
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
	var procDefExportList []*models.ProcDefDto
	var batchExecutionTemplateList []*models.BatchExecutionTemplate
	var transDataVariableConfig *models.TransDataVariableConfig
	var subProcDefIds []string
	var uploadUrl, path, zipPath string
	var err error
	var step models.TransExportStep
	var roleDisplayNameMap = make(map[string]string)
	if path, err = tools.GetPath(fmt.Sprintf(tempWeCubeDataDir, param.TransExportId)); err != nil {
		log.Logger.Error("getPath error", log.Error(err))
		return
	}
	if zipPath, err = tools.GetPath(fmt.Sprintf("%s/%s", fmt.Sprintf(tempWeCubeDataDir, param.TransExportId), zip)); err != nil {
		log.Logger.Error("getPath error", log.Error(err))
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
	}(&step)
	// 更新迁移导出表记录状态为执行中
	if err = updateTransExportStatus(ctx, param.TransExportId, models.TransExportStatusDoing); err != nil {
		log.Logger.Error("updateTransExportStatus error", log.Error(err))
		return
	}

	// 1. 导出选中角色
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
		Data:          queryRolesResponse.Data,
	}
	if err = execStepExport(exportRoleParam); err != nil {
		return
	}

	// 2.导出请求模版&组件库
	if len(param.RequestTemplateIds) > 0 {
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
			Data:          convertRequestTemplateExportDto2List(queryRequestTemplatesResponse.Data, queryRolesResponse.Data),
		}
		if err = execStepExport(exportRequestTemplateParam); err != nil {
			return
		}
		// 请求模版关联的编排 自动加入 导出编排
		for _, requestTemplateExport := range queryRequestTemplatesResponse.Data {
			if strings.TrimSpace(requestTemplateExport.RequestTemplate.ProcDefId) != "" && !contains(param.WorkflowIds, requestTemplateExport.RequestTemplate.ProcDefId) {
				// 调用编排查询下数据是否真实存在
				if procDefDto, err = GetProcDefDetailByProcDefId(ctx, requestTemplateExport.RequestTemplate.ProcDefId); err != nil {
					continue
				}
				if procDefDto != nil && procDefDto.ProcDef != nil && procDefDto.ProcDef.Id != "" {
					param.WorkflowIds = append(param.WorkflowIds, requestTemplateExport.RequestTemplate.ProcDefId)
				}
			}
		}
	}

	// 3. 导出组件库
	if param.ExportComponentLibrary {
		step = models.TransExportStepComponentLibrary
		if queryComponentLibraryResponse, err = remote.GetComponentLibrary(userToken, language); err != nil {
			log.Logger.Error("remote GetComponentLibrary error", log.Error(err))
			return
		}
		exportComponentLibraryStartTime := time.Now().Format(models.DateTimeFormat)
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
	}

	// 4. 导出编排
	step = models.TransExportStepWorkflow
	exportWorkflowStartTime := time.Now().Format(models.DateTimeFormat)
	for _, procDefId := range param.WorkflowIds {
		if procDefDto, err = GetProcDefDetailByProcDefId(ctx, procDefId); err != nil {
			log.Logger.Error("GetProcDefDetailByProcDefId error", log.Error(err))
			return
		}
		if procDefDto != nil && procDefDto.ProcDef != nil {
			procDefExportList = append(procDefExportList, buildProcDefDto(procDefDto, roleDisplayNameMap))
		}
		// 导出编排节点里面如果关联子编排,子编排也需要导出
		if procDefDto.ProcDefNodeExtend != nil && len(procDefDto.ProcDefNodeExtend.Nodes) > 0 {
			for _, node := range procDefDto.ProcDefNodeExtend.Nodes {
				if node.ProcDefNodeCustomAttrs != nil && node.ProcDefNodeCustomAttrs.SubProcDefId != "" {
					subProcDefIds = append(subProcDefIds, node.ProcDefNodeCustomAttrs.SubProcDefId)
				}
			}
		}
		for _, subProcDefId := range subProcDefIds {
			if !contains(param.WorkflowIds, subProcDefId) {
				if procDefDto, err = GetProcDefDetailByProcDefId(ctx, subProcDefId); err != nil {
					log.Logger.Error("GetProcDefDetailByProcDefId error", log.Error(err))
					return
				}
				param.WorkflowIds = append(param.WorkflowIds, subProcDefId)
				if procDefDto != nil && procDefDto.ProcDef != nil {
					procDefExportList = append(procDefExportList, buildProcDefDto(procDefDto, roleDisplayNameMap))
				}
			}
		}
	}
	exportWorkflowParam := models.StepExportParam{
		Ctx:           ctx,
		Path:          path,
		TransExportId: param.TransExportId,
		StartTime:     exportWorkflowStartTime,
		Step:          step,
		Input:         param.WorkflowIds,
		Data:          procDefExportList,
	}
	if err = execStepExport(exportWorkflowParam); err != nil {
		return
	}

	// 5.导出批量执行
	step = models.TransExportStepBatchExecution
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

	// 6. 导出插件配置
	step = models.TransExportStepPluginConfig
	exportPluginConfigStartTime := time.Now().Format(models.DateTimeFormat)
	if err = DataTransExportPluginConfig(ctx, param.TransExportId, path); err != nil {
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

	// 7. 导出cmdb
	step = models.TransExportStepCmdb
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

	// 8. 导出监控
	step = models.TransExportStepMonitor
	exportMonitorStartTime := time.Now().Format(models.DateTimeFormat)
	if err = exportMonitor(ctx, param.TransExportId, path, userToken, param.ExportDashboardMap); err != nil {
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

	// 9. json文件压缩并上传nexus
	if transDataVariableConfig, err = getDataTransVariableMap(ctx); err != nil {
		return
	}
	if transDataVariableConfig == nil {
		err = fmt.Errorf("cmdb transVariableMap is empty")
		return
	}
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
	updateTransExportSuccess(ctx, param.TransExportId, uploadUrl)
	return
}

// execStepExport 执行每步导出
func execStepExport(param models.StepExportParam) (err error) {
	if param.Data == nil {
		return
	}
	transExportDetail := models.TransExportDetailTable{
		TransExport: &param.TransExportId,
		Step:        int(param.Step),
		StartTime:   param.StartTime,
		Status:      string(models.TransExportStatusSuccess),
	}
	if param.Input != nil {
		inputByteArr, _ := json.Marshal(param.Input)
		transExportDetail.Input = string(inputByteArr)
	}
	if param.Data != nil {
		outputByteArr, _ := json.Marshal(param.Data)
		transExportDetail.Output = string(outputByteArr)
	}
	if err = tools.WriteJsonData2File(getExportJsonFile(param.Path, transExportDetailMap[param.Step]), param.Data); err != nil {
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
	var filePathList, monitoryTypeMetricList, serviceGroupMetricList, endpointGroupMetricList []string
	var allMonitorEndpointGroupList, exportMonitorEndpointGroupList []*monitor.EndpointGroupTable
	var responseBytes []byte
	err = db.MysqlEngine.Context(ctx).SQL("select * from trans_export_analyze_data where trans_export=? and source=?",
		transExportId, models.TransExportAnalyzeSourceMonitor).Find(&analyzeList)
	if err != nil {
		return
	}
	for _, monitorAnalyzeData := range analyzeList {
		filePathList = []string{}
		finalExportDataList = []interface{}{}
		if strings.TrimSpace(monitorAnalyzeData.Data) == "" {
			log.Logger.Info("analyze monitor data empty")
			continue
		}
		if err = json.Unmarshal([]byte(monitorAnalyzeData.Data), &exportDataKeyList); err != nil {
			log.Logger.Error("monitor json Unmarshal err", log.Error(err))
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
			for i, serviceGroup := range exportDataKeyList {
				if responseBytes, err = monitor.ExportLogMetric(serviceGroup, token); err != nil {
					log.Logger.Error("ExportLogMetric err", log.JsonObj("serviceGroup", serviceGroup), log.Error(err))
					return
				}
				if string(responseBytes) != "" {
					var temp interface{}
					json.Unmarshal(responseBytes, &temp)
					filePathList = append(filePathList, fmt.Sprintf("%s/%s_%d.json", path, models.TransExportAnalyzeMonitorDataTypeLogMonitorServiceGroup, i+1))
					finalExportDataList = append(finalExportDataList, temp)
				}
			}
		case models.TransExportAnalyzeMonitorDataTypeLogMonitorTemplate:
			// 导出业务日志模版
			if responseBytes, err = monitor.ExportLogMonitorTemplate(exportDataKeyList, token); err != nil {
				log.Logger.Error("ExportLogMonitorTemplate err", log.StringList("LogMonitorTemplates", exportDataKeyList), log.Error(err))
				return
			}
			if string(responseBytes) != "" {
				var temp interface{}
				json.Unmarshal(responseBytes, &temp)
				filePathList = []string{fmt.Sprintf("%s/%s.json", path, models.TransExportAnalyzeMonitorDataTypeLogMonitorTemplate)}
				finalExportDataList = []interface{}{temp}
			}
		case models.TransExportAnalyzeMonitorDataTypeStrategyServiceGroup:
			// 导出指标阈值,层级对象
			for i, key := range exportDataKeyList {
				if responseBytes, err = monitor.ExportAlarmStrategy("service", key, token); err != nil {
					log.Logger.Error("ExportAlarmStrategy err", log.JsonObj("service", key), log.Error(err))
					return
				}
				if string(responseBytes) != "" {
					var temp interface{}
					json.Unmarshal(responseBytes, &temp)
					filePathList = append(filePathList, fmt.Sprintf("%s/%s_%d.json", path, models.TransExportAnalyzeMonitorDataTypeStrategyServiceGroup, i+1))
					finalExportDataList = append(finalExportDataList, temp)
				}
			}
		case models.TransExportAnalyzeMonitorDataTypeStrategyEndpointGroup:
			// 导出指标阈值,组
			for i, key := range exportDataKeyList {
				if responseBytes, err = monitor.ExportAlarmStrategy("group", key, token); err != nil {
					log.Logger.Error("ExportAlarmStrategy err", log.JsonObj("group", key), log.Error(err))
					return
				}
				if string(responseBytes) != "" {
					var temp interface{}
					json.Unmarshal(responseBytes, &temp)
					filePathList = append(filePathList, fmt.Sprintf("%s/%s_%d.json", path, models.TransExportAnalyzeMonitorDataTypeStrategyEndpointGroup, i+1))
					finalExportDataList = append(finalExportDataList, temp)
				}
			}
		case models.TransExportAnalyzeMonitorDataTypeLogKeywordServiceGroup:
			// 导出关键字
			for i, serviceGroup := range exportDataKeyList {
				if responseBytes, err = monitor.ExportKeyword(serviceGroup, token); err != nil {
					log.Logger.Error("ExportKeyword err", log.JsonObj("serviceGroup", serviceGroup), log.Error(err))
					return
				}
				if string(responseBytes) != "" {
					var temp interface{}
					json.Unmarshal(responseBytes, &temp)
					filePathList = append(filePathList, fmt.Sprintf("%s/%s_%d.json", path, models.TransExportAnalyzeMonitorDataTypeLogKeywordServiceGroup, i+1))
					finalExportDataList = append(finalExportDataList, temp)
				}
			}
		case models.TransExportAnalyzeMonitorDataTypeDashboard:
			// 导出看板
			i := 1
			for dashboardId, chartIds := range exportDashboardMap {
				if responseBytes, err = monitor.ExportCustomDashboard(dashboardId, chartIds, token); err != nil {
					log.Logger.Error("ExportCustomDashboard err", log.JsonObj("dashboardId", dashboardId), log.Error(err))
					return
				}
				if string(responseBytes) != "" {
					var temp interface{}
					json.Unmarshal(responseBytes, &temp)
					filePathList = append(filePathList, fmt.Sprintf("%s/%s_%d.json", path, models.TransExportAnalyzeMonitorDataTypeDashboard, i+1))
					finalExportDataList = append(finalExportDataList, temp)
					i++
				}
			}
		case models.TransExportAnalyzeMonitorDataTypeCustomMetricMonitorType:
			// 导出指标列表基础类型
			monitoryTypeMetricList = exportDataKeyList
		case models.TransExportAnalyzeMonitorDataTypeCustomMetricServiceGroup:
			// 导出指标列表层级对象
			serviceGroupMetricList = exportDataKeyList
		case models.TransExportAnalyzeMonitorDataTypeCustomMetricEndpointGroup:
			// 导出指标列表对象组
			endpointGroupMetricList = exportDataKeyList
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
	if err = exportMetricList(monitoryTypeMetricList, serviceGroupMetricList, endpointGroupMetricList, path, token); err != nil {
		return
	}
	return
}

func exportMetricList(monitoryTypeMetricList, serviceGroupMetricList, endpointGroupMetricList []string, path, token string) (err error) {
	var responseBytes []byte
	var requestParam []monitor.ExportMetricParam
	for _, s := range monitoryTypeMetricList {
		requestParam = append(requestParam, []monitor.ExportMetricParam{{MonitorType: s, Comparison: "N"}, {MonitorType: s, Comparison: "Y"}}...)
	}
	for _, s := range serviceGroupMetricList {
		requestParam = append(requestParam, []monitor.ExportMetricParam{{ServiceGroup: s, Comparison: "N"}, {ServiceGroup: s, Comparison: "Y"}}...)
	}
	for _, s := range endpointGroupMetricList {
		requestParam = append(requestParam, []monitor.ExportMetricParam{{EndpointGroup: s, Comparison: "N"}, {EndpointGroup: s, Comparison: "Y"}}...)
	}
	for index, requestParam := range requestParam {
		filePath := fmt.Sprintf("%s/metric_list_%d.json", path, index+1)
		if requestParam.Comparison == "Y" {
			// 导出同环比
			filePath = fmt.Sprintf("%s/metric_comparison_list_%d.json", path, index+1)
		}
		if responseBytes, err = monitor.ExportMetricList(requestParam, token); err != nil {
			log.Logger.Error("ExportMetricList err", log.JsonObj("requestParam", requestParam), log.Error(err))
			return
		}
		if string(responseBytes) != "" {
			var temp interface{}
			json.Unmarshal(responseBytes, &temp)
			if err = tools.WriteJsonData2File(filePath, temp); err != nil {
				log.Logger.Error("WriteJsonData2File error", log.JsonObj("requestParam", requestParam), log.Error(err))
				return
			}
		}
	}
	return
}

func updateTransExportStatus(ctx context.Context, id string, status models.TransExportStatus) (err error) {
	_, err = db.MysqlEngine.Context(ctx).Exec("update trans_export set status=? where id=?", status, id)
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

func updateTransExportDetailFail(ctx context.Context, transExportId string, step models.TransExportStep, errMsg string) (err error) {
	_, err = db.MysqlEngine.Context(ctx).Exec("update trans_export_detail set status=?,error_msg=?,end_time=? where trans_export=? and step =?",
		models.TransExportStatusFail, errMsg, time.Now().Format(models.DateTimeFormat), transExportId, step)
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
