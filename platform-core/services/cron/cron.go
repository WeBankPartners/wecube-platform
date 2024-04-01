package cron

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/execution"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/workflow"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

func StartCronJob() {
	SetupCleanUpBatchExecTicker()
	go StartSendProcScheduleMail()
	go StartHandleProcEvent()
	go StartTransProcEvent()
}

func SetupCleanUpBatchExecTicker() {
	ticker := time.NewTicker(24 * time.Hour)
	// ticker := time.NewTicker(90 * time.Second)
	go func() {
		for t := range ticker.C {
			startTime := time.Now()
			log.Logger.Info("start clean up batch exec", log.String("ticker", fmt.Sprintf("%v", t)))
			CleanUpBatchExecRecord()
			log.Logger.Info("finish clean up batch exec", log.String("ticker", fmt.Sprintf("%v", t)),
				log.Int64("cost_ms", time.Since(startTime).Milliseconds()))
		}
	}()
	log.Logger.Info("setup clean up batch exec ticker")
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
}

func StartSendProcScheduleMail() {
	t := time.NewTicker(time.Minute).C
	for {
		<-t
		doSendProcScheduleMail()
	}
}

func doSendProcScheduleMail() {
	log.Logger.Info("start check proc schedule job mail")
	// 更新 mail status是sending状态但更新时间小于当前1分钟的，可能是之前实例占用了但没发送成功
	lastMinuteTime := time.Unix(time.Now().Unix()-60, 0)
	if _, resetErr := db.MysqlEngine.Exec("update proc_schedule_job set mail_status='wait' where mail_status='sending' and updated_time<?", lastMinuteTime); resetErr != nil {
		log.Logger.Error("sendProcScheduleMail try to reset sending status job fail", log.Error(resetErr))
	}
	var jobList []*models.ScheduleJobMailQueryObj
	err := db.MysqlEngine.SQL("select t1.id,t1.proc_ins_id,t1.schedule_config_id,t2.proc_def_name,t2.entity_data_name,t2.status,t2.created_time,t3.status as node_status,t4.name as node_name from proc_schedule_job t1 left join proc_ins t2 on t1.proc_ins_id=t2.id left join proc_ins_node t3 on t2.id=t3.proc_ins_id left join proc_def_node t4 on t3.proc_def_node_id=t4.id where t1.mail_status='wait' and (t2.status='" + models.JobStatusSuccess + "' or t3.status in ('" + models.JobStatusFail + "','" + models.JobStatusTimeout + "'))").Find(&jobList)
	if err != nil {
		log.Logger.Error("sendProcScheduleMail fail with query schedule job table", log.Error(err))
		return
	}
	var configList []*models.ProcScheduleConfig
	err = db.MysqlEngine.SQL("select id,mail_mode,created_by,`role` from proc_schedule_config where mail_mode in ('user','role')").Find(&configList)
	if err != nil {
		log.Logger.Error("sendProcScheduleMail fail with query schedule config table", log.Error(err))
		return
	}
	configMap := make(map[string]*models.ProcScheduleConfig)
	for _, row := range configList {
		configMap[row.Id] = row
	}
	uniqueMap := make(map[string]int)
	var sendJobList []*models.ScheduleJobMailQueryObj
	for _, row := range jobList {
		if row.ProcInsId == "" {
			continue
		}
		if row.Status == models.JobStatusSuccess {
			if _, ok := uniqueMap[row.ProcInsId]; !ok {
				uniqueMap[row.ProcInsId] = 1
				sendJobList = append(sendJobList, row)
			}
			continue
		}
		if row.NodeStatus == models.JobStatusFail || row.NodeStatus == models.JobStatusTimeout {
			if _, ok := uniqueMap[row.ProcInsId]; !ok {
				uniqueMap[row.ProcInsId] = 1
				sendJobList = append(sendJobList, row)
			}
		}
	}
	for _, v := range sendJobList {
		if configObj, ok := configMap[v.ScheduleConfigId]; ok {
			if tryUpdateScheduleJobMail(v) {
				tmpMail, tmpErr := buildScheduleJobMail(configObj.MailMode, configObj.CreatedBy, configObj.Role, v)
				if tmpErr != nil {
					log.Logger.Error("buildScheduleJobMail fail", log.String("jobId", v.Id), log.Error(tmpErr))
				} else {
					if tmpErr = remote.SendSmtpMail(tmpMail); tmpErr != nil {
						log.Logger.Error("proc schedule job send smtp mail fail", log.String("jobId", v.Id), log.Error(tmpErr))
					}
				}
				if tmpErr != nil {
					updateProcScheduleJobMail(v.Id, "fail", tmpErr.Error())
				} else {
					tmpMailBytes, _ := json.Marshal(&tmpMail)
					updateProcScheduleJobMail(v.Id, "done", string(tmpMailBytes))
				}
			}
		}
	}
	log.Logger.Info("done check proc schedule job mail")
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

func buildScheduleJobMail(mailMode, user, role string, jobObj *models.ScheduleJobMailQueryObj) (mailObj models.SendMailTarget, err error) {
	mailObj = models.SendMailTarget{}
	if mailMode == "role" {
		if role == "" {
			err = fmt.Errorf("mail target role empty")
		} else {
			if roleObj, roleErr := remote.RetrieveRoleByRoleName(role, remote.GetToken(), "en"); roleErr != nil {
				err = roleErr
			} else {
				if roleObj.Email != "" {
					mailObj.Accept = []string{roleObj.Email}
				}
			}
		}
	} else if mailMode == "user" {
		if user == "" {
			err = fmt.Errorf("mail target user empty")
		} else {
			if userObj, userErr := remote.RetrieveUserByUsername(user, remote.GetToken(), "en"); userErr != nil {
				err = userErr
			} else {
				if userObj.EmailAddr != "" {
					mailObj.Accept = []string{userObj.EmailAddr}
				}
			}
		}
	} else {
		err = fmt.Errorf("illegal mailMode:%s ", mailMode)
	}
	if err != nil {
		return
	}
	if len(mailObj.Accept) == 0 {
		err = fmt.Errorf("accept mail empty")
		return
	}
	if jobObj.Status == models.JobStatusSuccess {
		mailObj.Subject = fmt.Sprintf("Wecube Process Schedule Run %s,[%s][%s]", jobObj.Status, jobObj.ProcDefName, jobObj.EntityDataName)
		mailObj.Content = mailObj.Subject + fmt.Sprintf("\nProcess Instance Id:%s \nStatus:%s \nTime:%s \n", jobObj.ProcInsId, jobObj.Status, jobObj.CreatedTime)
	} else if jobObj.NodeStatus == models.JobStatusFail {
		mailObj.Subject = fmt.Sprintf("Wecube Process Schedule Run Fail,[%s][%s]", jobObj.ProcDefName, jobObj.EntityDataName)
		mailObj.Content = mailObj.Subject + fmt.Sprintf("\nProcess Instance Id:%s \nStatus:%s \nTime:%s \n", jobObj.ProcInsId, jobObj.Status, jobObj.CreatedTime) + fmt.Sprintf("\nNode [%s] %s", jobObj.NodeName, jobObj.NodeStatus)
	}
	return
}

func updateProcScheduleJobMail(jobId, mailStatus, mailMessage string) {
	_, err := db.MysqlEngine.Exec("update proc_schedule_job set mail_status=?,mail_msg=? where id=?", mailStatus, mailMessage, jobId)
	if err != nil {
		log.Logger.Error("updateProcScheduleJobMail fail", log.String("jobId", jobId), log.String("mailStatus", mailStatus), log.String("mailMsg", mailMessage), log.Error(err))
	}
}

func StartHandleProcEvent() {
	t := time.NewTicker(10 * time.Second).C
	for {
		<-t
		doHandleProcEventJob()
	}
}

// 扫事件表定时处理
func doHandleProcEventJob() {
	log.Logger.Debug("Start handle proc event job")
	var procEventRows []*models.ProcInsEvent
	err := db.MysqlEngine.SQL("select * from proc_ins_event where status=?", models.ProcEventStatusCreated).Find(&procEventRows)
	if err != nil {
		log.Logger.Error("doHandleProcEventJob fail with query proc_ins_event table", log.Error(err))
		return
	}
	if len(procEventRows) == 0 {
		log.Logger.Debug("Done handle proc event job,match empty rows")
		return
	}
	for _, row := range procEventRows {
		ctx := context.WithValue(context.Background(), models.TransactionIdHeader, fmt.Sprintf("proc_event_%d", row.Id))
		takeoverFlag, procInsId, tmpErr := handleProcEvent(ctx, row)
		if tmpErr != nil {
			log.Logger.Error("handleProcEvent fail", log.Int("procInsEvent", row.Id), log.Error(tmpErr))
			db.MysqlEngine.Context(ctx).Exec("update proc_ins_event set status=?,error_message=? where id=?", models.ProcEventStatusFail, tmpErr.Error(), row.Id)
		} else {
			if !takeoverFlag {
				continue
			}
			db.MysqlEngine.Context(ctx).Exec("update proc_ins_event set status=?,proc_ins_id=? where id=?", models.ProcEventStatusDone, procInsId, row.Id)
		}
	}
	log.Logger.Debug("Done handle proc event job")
}

func handleProcEvent(ctx context.Context, procEvent *models.ProcInsEvent) (takeoverFlag bool, procInsId string, err error) {
	execResult, execErr := db.MysqlEngine.Context(ctx).Exec("update proc_ins_event set status=?,host=? where id=? and status=?", models.ProcEventStatusPending, models.Config.HostIp, procEvent.Id, models.ProcEventStatusCreated)
	if execErr != nil {
		err = fmt.Errorf("takeover proc ins event fail,%s ", execErr.Error())
		return
	}
	if rowAffect, _ := execResult.RowsAffected(); rowAffect > 0 {
		takeoverFlag = true
	}
	if !takeoverFlag {
		return
	}
	operator := procEvent.OperationUser
	if operator == "" {
		operator = "platform"
	}
	// preview
	previewData, buildErr := execution.BuildProcPreviewData(ctx, procEvent.ProcDefId, procEvent.OperationData, operator)
	if buildErr != nil {
		err = buildErr
		return
	}
	// proc instance start
	procStartParam := models.ProcInsStartParam{
		EntityDataId:      procEvent.OperationData,
		EntityDisplayName: procEvent.OperationData,
		ProcDefId:         procEvent.ProcDefId,
		ProcessSessionId:  previewData.ProcessSessionId,
	}
	// 新增 proc_ins,proc_ins_node,proc_data_binding 纪录
	newProcInsId, workflowRow, workNodes, workLinks, createInsErr := database.CreateProcInstance(ctx, &procStartParam, operator)
	if createInsErr != nil {
		err = createInsErr
		log.Logger.Error("handleProcScheduleJob fail with create proc instance data", log.String("psConfigId", procEvent.ProcDefId), log.String("sessionId", previewData.ProcessSessionId), log.Error(createInsErr))
		return
	}
	procInsId = newProcInsId
	// 初始化workflow并开始
	workObj := workflow.Workflow{ProcRunWorkflow: *workflowRow}
	workObj.Init(context.Background(), workNodes, workLinks)
	//workflow.GlobalWorkflowMap.Store(workObj.Id, &workObj)
	go workObj.Start(&models.ProcOperation{CreatedBy: operator})
	return
}

func StartTransProcEvent() {
	t := time.NewTicker(10 * time.Second).C
	for {
		<-t
		doTransOldEventToNew()
	}
}

// 把老event表数据转到新event表
func doTransOldEventToNew() {
	log.Logger.Debug("Start trans proc event job")

}
