package database

import (
	"context"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"strings"
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
		Services:    strings.Join(param.PIds, ","),
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
	if err = AnalyzeCMDBDataExport(c, dataTransParam); err != nil {
		return
	}
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
			strArr := strings.Split(transExport.Services, ",")
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
