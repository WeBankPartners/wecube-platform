package database

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"os"
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

func CreateExport2(c context.Context, param models.CreateExportParam, operator string) (transExportId string, err error) {
	var actions, addTransExportActions, addTransExportDetailActions []*db.ExecAction
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
	/*dataTransParam := &models.AnalyzeDataTransParam{
		TransExportId: transExportId,
		Business:      param.PIds,
		Env:           param.Env,
	}*/
	if err = db.Transaction(actions, c); err != nil {
		return
	}
	/*if err = AnalyzeCMDBDataExport(c, dataTransParam); err != nil {
		return
	}*/
	return
}

func GetAllTransExportOptions(ctx context.Context) (options models.TransExportHistoryOptions, err error) {
	var serviceHashMap = make(map[string]bool)
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
					serviceHashMap[s2] = true
				}
			}
			operatorHashMap[transExport.UpdatedUser] = true
		}
	}
	options.Services = convertMap2Array(serviceHashMap)
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
	if len(param.Services) > 0 {
		sql += " and ("
		for i, service := range param.Services {
			if i == 0 {
				sql += " services like ?"
			} else {
				sql += " or services like ?"
			}
			queryParam = append(queryParam, fmt.Sprintf("%%%s%%", service))
		}
		sql += " )"
	}
	if len(param.Operators) > 0 {
		sql += " and updated_user in (" + getSQL(param.Operators) + ")"
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
	if len(procDefPermissionList) > 0 {
		for _, permission := range procDefPermissionList {
			allCheckRoleMap[permission.Permission] = true
		}
	}
	if batchExecutionTemplateRoles, err = GetBatchExecutionTemplatePermissionByIds(ctx, param.BatchExecutionIds); err != nil {
		return
	}
	if len(batchExecutionTemplateRoles) > 0 {
		for _, templateRole := range batchExecutionTemplateRoles {
			allCheckRoleMap[templateRole.RoleName] = true
		}
	}
	if requestTemplateRoles, err = remote.GetRequestTemplateRoles(models.GetRequestTemplateRolesDto{RequestTemplateIds: param.RequestTemplateIds}, userToken, language); err != nil {
		return
	}
	if len(requestTemplateRoles.Roles) > 0 {
		for _, role := range requestTemplateRoles.Roles {
			allCheckRoleMap[role] = true
		}
	}
	if len(param.Roles) > 0 {
		newCheckRoles = append(newCheckRoles, param.Roles...)
	}
	for role, _ := range allCheckRoleMap {
		newCheckRoles = append(newCheckRoles, role)
	}
	return
}

// ExecTransExport 执行导出
func ExecTransExport(ctx context.Context, param models.DataTransExportParam, userToken, language string) {
	var queryRolesResponse models.QueryRolesResponse
	var procDefDto *models.ProcessDefinitionDto
	var procDefExportList []*models.ProcessDefinitionDto
	var err error
	if queryRolesResponse, err = remote.RetrieveAllLocalRoles("Y", userToken, language, false); err != nil {
		log.Logger.Error("remote retrieveAllLocalRoles error", log.Error(err))
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
	}()
	// 1. 导出选中角色
	if err = execStepExport(ctx, param.TransExportId, int(models.TransExportStepRole), queryRolesResponse.Data); err != nil {
		return
	}
	// 2. 导出编排
	for _, procDefId := range param.WorkflowIds {
		if procDefDto, err = GetProcDefDetailByProcDefId(ctx, procDefId); err != nil {
			log.Logger.Error("GetProcDefDetailByProcDefId error", log.Error(err))
			return
		}
		procDefExportList = append(procDefExportList, procDefDto)
	}
	if err = execStepExport(ctx, param.TransExportId, int(models.TransExportStepWorkflow), procDefExportList); err != nil {
		return
	}
	// 3.导出批量执行

}

// execStepExport 执行每步导出
func execStepExport(ctx context.Context, transExportId string, step int, data interface{}) (err error) {
	if data == nil {
		return
	}
	transExportDetail := models.TransExportDetailTable{
		TransExport: &transExportId,
		Step:        step,
		StartTime:   time.Now().Format(models.DateTimeFormat),
		Status:      string(models.TransExportStatusSuccess),
	}
	byteArr, _ := json.Marshal(data)
	transExportDetail.Output = string(byteArr)
	if err = WriteJsonData2File(fmt.Sprintf("%s_%s", transExportDetailMap[step], transExportId), data); err != nil {
		log.Logger.Error("WriteJsonData2File error", log.String("module", transExportDetailMap[step]), log.Error(err))
		transExportDetail.Status = string(models.TransExportStatusFail)
		transExportDetail.ErrorMsg = err.Error()
		transExportDetail.EndTime = time.Now().Format(models.DateTimeFormat)
		updateTransExportDetail(ctx, transExportDetail)
		return
	}
	transExportDetail.EndTime = time.Now().Format(models.DateTimeFormat)
	updateTransExportDetail(ctx, transExportDetail)
	return
}

func updateTransExportStatus(ctx context.Context, id, status string) (err error) {
	_, err = db.MysqlEngine.Context(ctx).Exec("update trans_export set status=? where id=?", status, id)
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

func WriteJsonData2File(path string, inter interface{}) (err error) {
	var file *os.File
	// 打开文件
	if file, err = os.Create(path); err != nil {
		return
	}
	defer file.Close()

	// 创建json编码器
	encoder := json.NewEncoder(file)

	// 将json实例编码到文件
	if err = encoder.Encode(inter); err != nil {
		return
	}
	return
}
