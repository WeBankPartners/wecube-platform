package database

import (
	"context"
	"encoding/json"
	"fmt"
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

var transExportDetailMap = map[int]string{
	int(models.TransExportStepRole):             "role",
	int(models.TransExportStepWorkflow):         "workflow",
	int(models.TransExportStepComponentLibrary): "componentLibrary",
	int(models.TransExportStepBatchExecution):   "batchExecution",
	int(models.TransExportStepRequestTemplate):  "requestTemplate",
	int(models.TransExportStepCmdb):             "wecmdb",
	int(models.TransExportStepArtifacts):        "artifacts",
	int(models.TransExportStepMonitor):          "monitor",
}

const (
	zipFile = "export.zip"
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
	if deleteAnalyzeDataActions = getDeleteTransExportAnalyzeDataActions(c, param.TransExportId); len(deleteAnalyzeDataActions) > 0 {
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

// AutoAppendExportRoles 自动追加导出角色
func AutoAppendExportRoles(ctx context.Context, userToken, language string, param models.DataTransExportParam) (newCheckRoles []string, err error) {
	var procDefPermissionList []*models.ProcDefPermission
	var batchExecutionTemplateRoles []*models.BatchExecutionTemplateRole
	var requestTemplateRoles models.QueryRequestTemplateRolesResponse
	var allCheckRoleMap = make(map[string]bool)
	newCheckRoles = []string{}
	// 1.角色校验,编排、批量执行、请求模版的角色.需要的角色自动加入
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
	newCheckRoles = append(newCheckRoles, param.Roles...)
	for role, _ := range allCheckRoleMap {
		newCheckRoles = append(newCheckRoles, role)
	}
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
		TransExport: &transExport,
		CmdbCI:      make([]*models.CommonNameCount, 0),
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
		detail.CmdbCI = append(detail.CmdbCI, &models.CommonNameCount{
			Name:  transExportAnalyze.DataTypeName,
			Count: transExportAnalyze.DataLen,
		})
	}
	return
}

func GetTransExportAnalyzeDataByCond(ctx context.Context, transExportId string, dataType string) (analyzeData models.TransExportAnalyzeDataTable, err error) {
	_, err = db.MysqlEngine.Context(ctx).SQL("select * from trans_export_analyze_data where trans_export=? and data_type=? limit 1", transExportId, dataType).Get(&analyzeData)
	return
}

func QuerySimpleTransExportAnalyzeDataByTransExport(ctx context.Context, transExportId string) (result []*models.TransExportAnalyzeDataTable, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select data_type_name,data_len from trans_export_analyze_data where trans_export=?", transExportId).Find(&result)
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
	var uploadUrl, path string
	var err error
	var step models.TransExportStep
	var roleDisplayNameMap = make(map[string]string)
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
	if path, err = tools.GetPath(fmt.Sprintf("/tmp/wecube/%s", param.TransExportId)); err != nil {
		log.Logger.Error("getPath error", log.Error(err))
		return
	}
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
	if batchExecutionTemplateList, err = ExportTemplate(ctx, &models.ExportBatchExecTemplateReqParam{BatchExecTemplateIds: param.BatchExecutionIds}); err != nil {
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
	// 7. json文件压缩并上传nexus
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
				SourceFilePath: fmt.Sprintf("%s/%s", path, zipFile),
				DestFilePath:   fmt.Sprintf("%s/%s", param.TransExportId, zipFile),
			},
		},
	}
	if uploadUrl, err = tools.CreateZipCompressAndUpload(path, zipFile, uploadReqParam); err != nil {
		log.Logger.Error("CreateZipCompressAndUpload error", log.Error(err))
		return
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
	if err = tools.WriteJsonData2File(getExportJsonFile(param.Path, transExportDetailMap[int(param.Step)]), param.Data); err != nil {
		log.Logger.Error("WriteJsonData2File error", log.String("name", transExportDetailMap[int(param.Step)]), log.Error(err))
		return
	}
	transExportDetail.EndTime = time.Now().Format(models.DateTimeFormat)
	updateTransExportDetail(param.Ctx, transExportDetail)
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

func getDeleteTransExportAnalyzeDataActions(ctx context.Context, transExportId string) (actions []*db.ExecAction) {
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
		if step == int(models.TransExportStepCmdb) || step == int(models.TransExportStepArtifacts) || step == int(models.TransExportStepMonitor) {
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
