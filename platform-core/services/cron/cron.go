package cron

import (
	"fmt"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

func StartCronJob() {
	SetupCleanUpBatchExecTicker()
	go StartSendProcScheduleMail()
}

func SetupCleanUpBatchExecTicker() {
	ticker := time.NewTicker(24 * time.Hour)
	// ticker := time.NewTicker(90 * time.Second)
	go func() {
		for {
			select {
			case t := <-ticker.C:
				startTime := time.Now()
				log.Logger.Info("start clean up batch exec", log.String("ticker", fmt.Sprintf("%v", t)))
				CleanUpBatchExecRecord()
				log.Logger.Info("finish clean up batch exec", log.String("ticker", fmt.Sprintf("%v", t)),
					log.Int64("cost_ms", time.Now().Sub(startTime).Milliseconds()))
			}
		}
	}()
	log.Logger.Info("setup clean up batch exec ticker")
	return
}

func CleanUpBatchExecRecord() {
	keepBatchExecDays := models.Config.Cron.KeepBatchExecDays
	if keepBatchExecDays == 0 {
		keepBatchExecDays = models.DefaultKeepBatchExecDays
	}
	now := time.Now()
	keepBatchExecTime := now.Add(-time.Duration(keepBatchExecDays) * 24 * time.Hour)
	keepBatchExecTimeStr := keepBatchExecTime.String()

	transId := fmt.Sprintf("clean_up_batch_exec_record_%d", time.Now().Unix())
	var actions []*db.ExecAction
	// delete batchExecution
	action := &db.ExecAction{
		Sql:   db.CombineDBSql("DELETE FROM ", models.TableNameBatchExec, " WHERE created_time<?"),
		Param: []interface{}{keepBatchExecTimeStr},
	}
	actions = append(actions, action)

	// delete batchExecutionJobs
	action = &db.ExecAction{
		Sql:   db.CombineDBSql("DELETE FROM ", models.TableNameBatchExecJobs, " WHERE execute_time<?"),
		Param: []interface{}{keepBatchExecTimeStr},
	}
	actions = append(actions, action)

	err := db.Transaction(actions, db.DBCtx(transId))
	if err != nil {
		log.Logger.Error("clean up batch exec sql failed", log.Error(err))
	}
	return
}

func StartSendProcScheduleMail() {
	t := time.NewTicker(time.Minute).C
	for {
		<-t
		sendProcScheduleMail()
	}
}

func sendProcScheduleMail() {
	log.Logger.Info("start check proc schedule job mail")
	// 更新 mail status是sending状态但更新时间小于当前1分钟的，可能是之前实例占用了但没发送成功
	lastMinuteTime := time.Unix(time.Now().Unix()-60, 0)
	if _, resetErr := db.MysqlEngine.Exec("update proc_schedule_job set mail_status='wait' where mail_status='sending' and updated_time<?", lastMinuteTime); resetErr != nil {
		log.Logger.Error("sendProcScheduleMail try to reset sending status job fail", log.Error(resetErr))
	}
	var jobList []*models.ScheduleJobMailQueryObj
	err := db.MysqlEngine.SQL("select t1.id,t1.proc_ins_id,t2.proc_def_name,t2.entity_data_name,t2.status,t2.created_time,t3.status as node_status,t4.name as node_name from proc_schedule_job t1 left join proc_ins t2 on t1.proc_ins_id=t2.id left join proc_ins_node t3 on t2.id=t3.proc_ins_id left join proc_def_node t4 on t3.proc_def_node_id=t4.id where t1.mail_status='wait' and (t2.status='" + models.JobStatusSuccess + "' or t3.status in ('" + models.JobStatusFail + "','" + models.JobStatusTimeout + "'))").Find(&jobList)
	if err != nil {
		log.Logger.Error("sendProcScheduleMail fail with query schedule job table", log.Error(err))
		return
	}
	successMap := make(map[string]*models.ScheduleJobMailQueryObj)
	failMap := make(map[string]*models.ScheduleJobMailQueryObj)
	for _, row := range jobList {
		if row.ProcInsId == "" {
			continue
		}
		if row.Status == models.JobStatusSuccess {
			if _, ok := successMap[row.ProcInsId]; !ok {
				successMap[row.ProcInsId] = row
			}
			continue
		}
		if row.NodeStatus == models.JobStatusFail || row.NodeStatus == models.JobStatusTimeout {
			if _, ok := failMap[row.ProcInsId]; !ok {
				failMap[row.ProcInsId] = row
			}
		}
	}
	for _, v := range successMap {
		if tryUpdateScheduleJobMail(v) {

		}
	}
}

func tryUpdateScheduleJobMail(input *models.ScheduleJobMailQueryObj) bool {
	ok := false
	execResult, err := db.MysqlEngine.Exec("update proc_schedule_job set mail_status='sending' where id=? and mail_status='wait'", input.Id)
	if err != nil {
		log.Logger.Error("tryUpdateScheduleJobMail fail with exec sql", log.String("jobId", input.Id), log.Error(err))
		return ok
	}
	if affectNum, _ := execResult.RowsAffected(); affectNum > 0 {
		ok = true
	}
	return ok
}
