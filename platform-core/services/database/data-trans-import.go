package database

import (
	"context"
	"database/sql"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/tools"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"os"
	"strings"
	"time"
)

func GetDataTransImportNexusConfig(ctx context.Context) (result *models.TransDataImportNexusConfig, err error) {
	result = &models.TransDataImportNexusConfig{}
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
	nexusConfig, getNexusConfigErr := GetDataTransImportNexusConfig(ctx)
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
		execResult, err = db.MysqlEngine.Context(ctx).Exec("update trans_import_proc_exec set status=?,proc_ins=?,start_time=? where id=?", models.JobStatusSuccess, param.ProcIns, time.Now(), param.Id)
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
