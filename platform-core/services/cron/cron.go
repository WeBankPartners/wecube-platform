package cron

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/execution"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/workflow"
	"go.uber.org/zap"
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
			log.Info(nil, log.LOGGER_APP, "start clean up batch exec", zap.String("ticker", fmt.Sprintf("%v", t)))
			CleanUpBatchExecRecord()
			log.Info(nil, log.LOGGER_APP, "finish clean up batch exec", zap.String("ticker", fmt.Sprintf("%v", t)),
				zap.Int64("cost_ms", time.Since(startTime).Milliseconds()))
		}
	}()
	log.Info(nil, log.LOGGER_APP, "setup clean up batch exec ticker")
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
		log.Error(nil, log.LOGGER_APP, "clean up batch exec sql failed", zap.Error(err))
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
	log.Info(nil, log.LOGGER_APP, "start check proc schedule job mail")
	// 更新 mail status是sending状态但更新时间小于当前1分钟的，可能是之前实例占用了但没发送成功
	lastMinuteTime := time.Unix(time.Now().Unix()-60, 0)
	if _, resetErr := db.MysqlEngine.Exec("update proc_schedule_job set mail_status='wait' where mail_status='sending' and updated_time<?", lastMinuteTime); resetErr != nil {
		log.Error(nil, log.LOGGER_APP, "sendProcScheduleMail try to reset sending status job fail", zap.Error(resetErr))
	}
	var jobList []*models.ScheduleJobMailQueryObj
	err := db.MysqlEngine.SQL("select t1.id,t1.proc_ins_id,t1.schedule_config_id,t2.proc_def_name,t2.entity_data_name,t2.status,t2.created_time,t3.status as node_status,t4.name as node_name from proc_schedule_job t1 left join proc_ins t2 on t1.proc_ins_id=t2.id left join proc_ins_node t3 on t2.id=t3.proc_ins_id left join proc_def_node t4 on t3.proc_def_node_id=t4.id where t1.mail_status='wait' and (t2.status='" + models.JobStatusSuccess + "' or t3.status in ('" + models.JobStatusFail + "','" + models.JobStatusTimeout + "'))").Find(&jobList)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "sendProcScheduleMail fail with query schedule job table", zap.Error(err))
		return
	}
	var configList []*models.ProcScheduleConfig
	err = db.MysqlEngine.SQL("select id,mail_mode,created_by,`role` from proc_schedule_config where mail_mode in ('user','role')").Find(&configList)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "sendProcScheduleMail fail with query schedule config table", zap.Error(err))
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
					log.Error(nil, log.LOGGER_APP, "buildScheduleJobMail fail", zap.String("jobId", v.Id), zap.Error(tmpErr))
				} else {
					if tmpErr = remote.SendSmtpMail(tmpMail); tmpErr != nil {
						log.Error(nil, log.LOGGER_APP, "proc schedule job send smtp mail fail", zap.String("jobId", v.Id), zap.Error(tmpErr))
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
	log.Info(nil, log.LOGGER_APP, "done check proc schedule job mail")
}

func tryUpdateScheduleJobMail(input *models.ScheduleJobMailQueryObj) bool {
	ok := false
	execResult, err := db.MysqlEngine.Exec("update proc_schedule_job set mail_status='sending' where id=? and mail_status='wait'", input.Id)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "tryUpdateScheduleJobMail fail with exec sql", zap.String("jobId", input.Id), zap.Error(err))
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
		log.Error(nil, log.LOGGER_APP, "updateProcScheduleJobMail fail", zap.String("jobId", jobId), zap.String("mailStatus", mailStatus), zap.String("mailMsg", mailMessage), zap.Error(err))
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
	log.Debug(nil, log.LOGGER_APP, "Start handle proc event job")
	var procEventRows []*models.ProcInsEvent
	err := db.MysqlEngine.SQL("select * from proc_ins_event where status=?", models.ProcEventStatusCreated).Find(&procEventRows)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "doHandleProcEventJob fail with query proc_ins_event table", zap.Error(err))
		return
	}
	if len(procEventRows) == 0 {
		log.Debug(nil, log.LOGGER_APP, "Done handle proc event job,match empty rows")
		return
	}
	for _, row := range procEventRows {
		ctx := context.WithValue(context.Background(), models.TransactionIdHeader, fmt.Sprintf("proc_event_%d", row.Id))
		takeoverFlag, procInsId, tmpErr := handleProcEvent(ctx, row)
		if tmpErr != nil {
			log.Error(nil, log.LOGGER_APP, "handleProcEvent fail", zap.Int("procInsEvent", row.Id), zap.Error(tmpErr))
			db.MysqlEngine.Context(ctx).Exec("update proc_ins_event set status=?,error_message=? where id=?", models.ProcEventStatusFail, tmpErr.Error(), row.Id)
		} else {
			if !takeoverFlag {
				continue
			}
			db.MysqlEngine.Context(ctx).Exec("update proc_ins_event set status=?,proc_ins_id=? where id=?", models.ProcEventStatusDone, procInsId, row.Id)
		}
	}
	log.Debug(nil, log.LOGGER_APP, "Done handle proc event job")
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
		log.Error(nil, log.LOGGER_APP, "handleProcScheduleJob fail with create proc instance data", zap.String("psConfigId", procEvent.ProcDefId), zap.String("sessionId", previewData.ProcessSessionId), zap.Error(createInsErr))
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
	t := time.NewTicker(30 * time.Second).C
	for {
		<-t
		doTransOldEventToNew()
	}
}

// 把老event表数据转到新event表
func doTransOldEventToNew() {
	log.Debug(nil, log.LOGGER_APP, "Start trans proc event job")
	ctx := context.WithValue(context.Background(), models.TransactionIdHeader, fmt.Sprintf("trans_event_%d", time.Now().Unix()))
	var oldEventRows []*models.CoreOperationEvent
	err := db.MysqlEngine.Context(ctx).SQL("select * from core_operation_event where oper_key like 'pdef_key_%'").Find(&oldEventRows)
	if err != nil {
		log.Info(nil, log.LOGGER_APP, "doTransOldEventToNew fail with query core_operation_event table", zap.Error(err))
		return
	}
	if len(oldEventRows) == 0 {
		log.Debug(nil, log.LOGGER_APP, "Done trans proc event job with empty rows")
		return
	}
	var actions []*db.ExecAction
	nowTime := time.Now()
	for _, row := range oldEventRows {
		tmpNewRowAction := db.ExecAction{Sql: "insert into proc_ins_event(event_seq_no,event_type,operation_data,operation_key,operation_user,proc_def_id,source_plugin,status,created_time) values (?,?,?,?,?,?,?,?,?)", Param: []interface{}{
			row.EventSeqNo, row.EventType, row.OperData, row.OperKey, row.OperUser, row.ProcDefId, row.SrcSubSystem, models.ProcEventStatusCreated, nowTime,
		}}
		tmpDelAction := db.ExecAction{Sql: "delete from core_operation_event where id=?", Param: []interface{}{row.Id}}
		actions = append(actions, &tmpNewRowAction)
		actions = append(actions, &tmpDelAction)
	}
	if len(actions) > 0 {
		if err = db.Transaction(actions, ctx); err != nil {
			log.Error(nil, log.LOGGER_APP, "doTransOldEventToNew fail with do db transaction", zap.Error(err))
			return
		}
	}
	log.Debug(nil, log.LOGGER_APP, "Done trans proc event job")
}
