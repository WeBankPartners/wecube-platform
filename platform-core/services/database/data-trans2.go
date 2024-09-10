package database

import (
	"context"
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
