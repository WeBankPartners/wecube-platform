package database

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/tools"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"os"
	"sort"
	"strings"
	"time"
)

var transExportDetailMap = map[int]string{
	int(models.TransExportStepRole):            "role",
	int(models.TransExportStepWorkflow):        "workflow",
	int(models.TransExportStepBatchExecution):  "batchExecution",
	int(models.TransExportStepRequestTemplate): "requestTemplate",
	int(models.TransExportStepCmdbCI):          "cmdbCI",
	int(models.TransExportStepArtifacts):       "artifacts",
	int(models.TransExportStepMonitor):         "monitor",
}

const (
	zipFile = "export.zip"
)

func CreateExport2(c context.Context, param models.CreateExportParam, operator string) (transExportId string, err error) {
	var actions, addTransExportActions, addTransExportDetailActions, analyzeDataActions []*db.ExecAction
	transExportId = guid.CreateGuid()
	transExport := models.TransExportTable{
		Id:          transExportId,
		Environment: param.Env,
		Business:    strings.Join(param.PIds, ","),
		Status:      string(models.TransExportStatusStart),
		CreatedUser: operator,
		UpdatedUser: operator,
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

func GetTransExportDetail(ctx context.Context, transExportId string) (detail *models.TransExportDetail, err error) {
	var transExport models.TransExportTable
	var transExportDetailList []*models.TransExportDetailTable
	if _, err = db.MysqlEngine.Context(ctx).SQL("select * from trans_export where id=?", transExportId).Get(&transExport); err != nil {
		return
	}
	if err = db.MysqlEngine.Context(ctx).SQL("select * from trans_export_detail where trans_export=?", transExportId).Find(&transExportDetailList); err != nil {
		return
	}
	// 按步骤排序
	sort.Sort(models.TransExportDetailTableSort(transExportDetailList))
	detail = &models.TransExportDetail{
		TransExport:       &transExport,
		TransExportDetail: transExportDetailList,
	}
	return
}

// ExecTransExport 执行导出
func ExecTransExport(ctx context.Context, param models.DataTransExportParam, userToken, language string) {
	var queryRolesResponse models.QueryRolesResponse
	var queryRequestTemplatesResponse models.QueryRequestTemplatesResponse
	var procDefDto *models.ProcessDefinitionDto
	var procDefExportList []*models.ProcessDefinitionDto
	var batchExecutionTemplateList []*models.BatchExecutionTemplate
	var transDataVariableConfig *models.TransDataVariableConfig
	var subProcDefIds []string
	var uploadUrl, path string
	var err error
	if path, err = tools.GetPath(fmt.Sprintf("/tmp/wecube/%s", param.TransExportId)); err != nil {
		log.Logger.Error("getPath error", log.Error(err))
		return
	}
	// 更新迁移导出表记录状态为执行中
	if err = updateTransExportStatus(ctx, param.TransExportId, string(models.TransExportStatusDoing)); err != nil {
		log.Logger.Error("updateTransExportStatus error", log.Error(err))
		return
	}
	// 如果有报错,更新导出记录状态失败
	defer func() {
		if err != nil {
			updateTransExportStatus(ctx, param.TransExportId, string(models.TransExportStatusFail))
		}
		// 删除导出目录
		if err = os.RemoveAll(path); err != nil {
			log.Logger.Error("delete fail", log.String("path", path), log.Error(err))
		}
	}()
	// 1. 导出选中角色
	exportRoleStartTime := time.Now().Format(models.DateTimeFormat)
	if queryRolesResponse, err = remote.RetrieveAllLocalRoles("Y", userToken, language, false); err != nil {
		log.Logger.Error("remote retrieveAllLocalRoles error", log.Error(err))
		return
	}
	exportRoleParam := models.StepExportParam{
		Ctx:           ctx,
		Path:          path,
		TransExportId: param.TransExportId,
		StartTime:     exportRoleStartTime,
		Step:          int(models.TransExportStepRole),
		Input:         param.Roles,
		Data:          queryRolesResponse.Data,
	}
	if err = execStepExport(exportRoleParam); err != nil {
		return
	}
	// 2.导出请求模版
	if len(param.RequestTemplateIds) > 0 {
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
			Step:          int(models.TransExportStepRequestTemplate),
			Input:         param.RequestTemplateIds,
			Data:          queryRequestTemplatesResponse.Data,
		}
		if err = execStepExport(exportRequestTemplateParam); err != nil {
			return
		}
		// 请求模版关联的编排 自动加入 导出编排
		for _, requestTemplateExport := range queryRequestTemplatesResponse.Data {
			if strings.TrimSpace(requestTemplateExport.RequestTemplate.ProcDefId) != "" && !contains(param.WorkflowIds, requestTemplateExport.RequestTemplate.ProcDefId) {
				param.WorkflowIds = append(param.WorkflowIds, requestTemplateExport.RequestTemplate.ProcDefId)
			}
		}
	}
	// 3. 导出编排
	exportWorkflowStartTime := time.Now().Format(models.DateTimeFormat)
	for _, procDefId := range param.WorkflowIds {
		if procDefDto, err = GetProcDefDetailByProcDefId(ctx, procDefId); err != nil {
			log.Logger.Error("GetProcDefDetailByProcDefId error", log.Error(err))
			return
		}
		procDefExportList = append(procDefExportList, procDefDto)
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
				procDefExportList = append(procDefExportList, procDefDto)
			}
		}
	}
	exportWorkflowParam := models.StepExportParam{
		Ctx:           ctx,
		Path:          path,
		TransExportId: param.TransExportId,
		StartTime:     exportWorkflowStartTime,
		Step:          int(models.TransExportStepWorkflow),
		Input:         param.WorkflowIds,
		Data:          procDefExportList,
	}
	if err = execStepExport(exportWorkflowParam); err != nil {
		return
	}
	// 4.导出批量执行
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
		Step:          int(models.TransExportStepBatchExecution),
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
		Step:        param.Step,
		StartTime:   param.StartTime,
		Status:      string(models.TransExportStatusSuccess),
	}
	inputByteArr, _ := json.Marshal(param.Input)
	transExportDetail.Input = string(inputByteArr)
	outputByteArr, _ := json.Marshal(param.Data)
	transExportDetail.Output = string(outputByteArr)
	if err = tools.WriteJsonData2File(getExportJsonFile(param.Path, transExportDetailMap[param.Step]), param.Data); err != nil {
		log.Logger.Error("WriteJsonData2File error", log.String("name", transExportDetailMap[param.Step]), log.Error(err))
		transExportDetail.Status = string(models.TransExportStatusFail)
		transExportDetail.ErrorMsg = err.Error()
		transExportDetail.EndTime = time.Now().Format(models.DateTimeFormat)
		updateTransExportDetail(param.Ctx, transExportDetail)
		return
	}
	transExportDetail.EndTime = time.Now().Format(models.DateTimeFormat)
	updateTransExportDetail(param.Ctx, transExportDetail)
	return
}

func updateTransExportStatus(ctx context.Context, id, status string) (err error) {
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

func getInsertTransExportDetail(transExportId string) (actions []*db.ExecAction) {
	actions = []*db.ExecAction{}
	guids := guid.CreateGuidList(len(transExportDetailMap))
	i := 0
	for step, name := range transExportDetailMap {
		actions = append(actions, &db.ExecAction{Sql: "insert into trans_export_detail(id,trans_export,name,step,status) values (?,?,?,?,?)", Param: []interface{}{
			guids[i], transExportId, name, step, models.TransExportStatusNotStart,
		}})
		i++
	}
	return
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
