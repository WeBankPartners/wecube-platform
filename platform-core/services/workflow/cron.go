package workflow

import (
	"context"
	"fmt"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"go.uber.org/zap"
)

func StartCronJob() {
	instanceHost = models.Config.HostIp
	go startScanOperationJob()
	go loadAllWorkflow()
	go startTakeOverJob()
	go startSleepWorkflowJob()
}

// 当前自身内存中有运行工作流的情况下，没有就跳过
// 每2s扫描工作流操作表看工作流是否有新的指示，条件是 where status=wait and workflow_id in (工作流id列表)，组合索引(status+workflow_id)
func startScanOperationJob() {
	t := time.NewTicker(2 * time.Second).C
	for {
		<-t
		doScanOperationJob()
	}
}

func doScanOperationJob() {
	var curWorkflowIds []string
	GlobalWorkflowMap.Range(func(key, value any) bool {
		curWorkflowIds = append(curWorkflowIds, key.(string))
		return true
	})
	if len(curWorkflowIds) == 0 {
		return
	}
	var operationRows []*models.ProcRunOperation
	filterSql, filterParam := db.CreateListParams(curWorkflowIds, "")
	err := db.WorkflowMysqlEngine.SQL("select id,workflow_id,node_id,operation,message,created_by,created_time from proc_run_operation where status='wait' and workflow_id in ("+filterSql+")", filterParam...).Find(&operationRows)
	if err != nil {
		log.Error(nil, log.LOGGER_WORKFOLW, "query proc operation fail", zap.Error(err))
		return
	}
	if len(operationRows) == 0 {
		return
	}
	for _, operation := range operationRows {
		go HandleProOperation(operation)
	}
}

func HandleProOperation(operation *models.ProcRunOperation) {
	// 尝试抢占
	execResult, err := db.WorkflowMysqlEngine.Exec("update proc_run_operation set status='doing',handle_by=?,start_time=? where id=? and status='wait'", instanceHost, time.Now(), operation.Id)
	if err != nil {
		log.Error(nil, log.LOGGER_WORKFOLW, "try to handle operation fail", zap.String("host", instanceHost), zap.Int64("operation", operation.Id), zap.Error(err))
		return
	}
	if rowAffectNum, _ := execResult.RowsAffected(); rowAffectNum <= 0 {
		log.Warn(nil, log.LOGGER_WORKFOLW, "try to handle operation,but too late", zap.String("host", instanceHost), zap.Int64("operation", operation.Id))
		return
	}
	doneFlag := false
	defer func() {
		if doneFlag {
			_, err = db.WorkflowMysqlEngine.Exec("update proc_run_operation set status='done',end_time=? where id=?", time.Now(), operation.Id)
		} else {
			_, err = db.WorkflowMysqlEngine.Exec("update proc_run_operation set status='wait' where id=?", operation.Id)
		}
		if err != nil {
			log.Error(nil, log.LOGGER_WORKFOLW, "handle operation update operation status fail", zap.Bool("done", doneFlag), zap.String("host", instanceHost), zap.Int64("operation", operation.Id), zap.Error(err))
		}
	}()
	if workIf, ok := GlobalWorkflowMap.Load(operation.WorkflowId); ok {
		workObj := workIf.(*Workflow)
		doWorkflowOperation(operation, workObj)
		doneFlag = true
	} else {
		// check need to wakeup
		log.Warn(nil, log.LOGGER_WORKFOLW, "handle operation message warning,can not find workflow", zap.String("workflowId", operation.WorkflowId))
		workflowRow, queryErr := getWorkflowRow(operation.WorkflowId)
		if queryErr != nil {
			log.Error(nil, log.LOGGER_WORKFOLW, "handle operation fail with get workflow row", zap.Error(queryErr))
			return
		}
		if !workflowRow.Sleep {
			// 如果不是sleep，应该有其它实例在处理它，如果也没有其它实例处理它，那等抢占的worker把它接管后再处理
			log.Warn(nil, log.LOGGER_WORKFOLW, "give up handle operation,workflow is not sleeping", zap.String("workflowId", operation.WorkflowId))
			return
		}
		// 尝试恢复workflow
		if err = setWorkflowSleepDB(operation.WorkflowId, false); err != nil {
			log.Error(nil, log.LOGGER_WORKFOLW, "handle operation fail with set workflow sleep false", zap.String("workflowId", operation.WorkflowId), zap.Error(err))
			return
		}
		if err = recoverWorkflow(operation.WorkflowId); err != nil {
			setWorkflowSleepDB(operation.WorkflowId, true)
			log.Error(nil, log.LOGGER_WORKFOLW, "handle operation fail with recover workflow from sleep", zap.String("workflowId", operation.WorkflowId), zap.Error(err))
		} else {
			time.Sleep(2 * time.Second)
			if workLoaded, loadOk := GlobalWorkflowMap.Load(operation.WorkflowId); loadOk {
				workObj := workLoaded.(*Workflow)
				doWorkflowOperation(operation, workObj)
				doneFlag = true
			} else {
				log.Warn(nil, log.LOGGER_WORKFOLW, "handle operation,recover workflow done but get GlobalWorkflowMap item fail", zap.String("workflowId", operation.WorkflowId))
			}
		}
	}
}

func doWorkflowOperation(operation *models.ProcRunOperation, workObj *Workflow) {
	opObj := models.ProcOperation{Ctx: context.Background(), WorkflowId: operation.WorkflowId, Message: operation.Message, CreatedBy: operation.CreatedBy}
	switch operation.Operation {
	case "kill":
		workObj.Kill(&opObj)
	case "retry":
		workObj.RetryNode(operation.NodeId, false)
	case "ignore":
		workObj.IgnoreNode(operation.NodeId)
	case "approve":
		workObj.ApproveNode(operation.NodeId, operation.Message)
	case "stop":
		workObj.Stop(&opObj)
	case "continue":
		workObj.Continue(&opObj)
	case "confirm":
		workObj.RetryNode(operation.NodeId, true)
	default:
		log.Error(nil, log.LOGGER_WORKFOLW, "handle operation error with illegal operation", zap.String("operation", operation.Operation))
	}
}

// 每10s扫描工作流表找是否有需要尝试接管的工作流
// 条件是 where sleep=0 and status in (running,problem) and last_alive_time<=now()-30，组合索引(sleep+status)
// 正常情况下不会扫到，扫到的情况下尝试恢复
// 恢复的话先抢占工作流表(update proc_run_workflow set host='xx',last_alive_time=now where id=wId and last_alive_time<now()-30)
// 抢占成功后内存加载该工作流
func startTakeOverJob() {
	t := time.NewTicker(10 * time.Second).C
	for {
		<-t
		doTakeOver()
	}
}

func doTakeOver() {
	var workflowRows []*models.ProcRunWorkflow
	lastTime := time.Unix(time.Now().Unix()-30, 0).Format(models.DateTimeFormat)
	err := db.WorkflowMysqlEngine.SQL("select id,status,host,updated_time,last_alive_time from proc_run_workflow where `sleep`=0 and status=? and last_alive_time<=?", models.JobStatusRunning, lastTime).Find(&workflowRows)
	if err != nil {
		log.Error(nil, log.LOGGER_WORKFOLW, "do takeover workflow fail with query workflow table error", zap.Error(err))
		return
	}
	for _, row := range workflowRows {
		if !tryTakeoverWorkflowRow(row.Id) {
			log.Warn(nil, log.LOGGER_WORKFOLW, "tryTakeoverWorkflowRow fail", zap.String("workflowId", row.Id))
			continue
		}
		log.Info(nil, log.LOGGER_WORKFOLW, "start takeoverWorkflowRow", zap.String("workflowId", row.Id))
		if tmpErr := recoverWorkflow(row.Id); tmpErr != nil {
			log.Error(nil, log.LOGGER_WORKFOLW, "end takeoverWorkflowRow,fail recover workflow", zap.String("workflowId", row.Id), zap.Error(tmpErr))
		}
	}
}

func tryTakeoverWorkflowRow(workflowId string) bool {
	ok := false
	nowTime := time.Now().Format(models.DateTimeFormat)
	lastTime := time.Unix(time.Now().Unix()-30, 0).Format(models.DateTimeFormat)
	execResult, execErr := db.WorkflowMysqlEngine.Exec("update proc_run_workflow set host=?,last_alive_time=? where id=? and last_alive_time<?", instanceHost, nowTime, workflowId, lastTime)
	if execErr != nil {
		log.Error(nil, log.LOGGER_WORKFOLW, "tryTakeoverWorkflowRow fail with exec update workflow sql", zap.Error(execErr))
		return ok
	}
	if affectNum, _ := execResult.RowsAffected(); affectNum > 0 {
		ok = true
	}
	return ok
}

func loadAllWorkflow() {
	log.Info(nil, log.LOGGER_WORKFOLW, "<<--Start load all workflow-->>")
	var workflowRows []*models.ProcRunWorkflow
	err := db.WorkflowMysqlEngine.SQL("select id,name from proc_run_workflow where status=? and `sleep`=0 and stop=0 and host=?", models.JobStatusRunning, instanceHost).Find(&workflowRows)
	if err != nil {
		log.Error(nil, log.LOGGER_WORKFOLW, "load all workflow fail with query workflow table error", zap.Error(err))
		return
	}
	for _, row := range workflowRows {
		recoverWorkflow(row.Id)
	}
	log.Info(nil, log.LOGGER_WORKFOLW, "<<--Done load all workflow-->>")
}

func recoverWorkflow(workflowId string) (err error) {
	ctx := context.WithValue(context.Background(), models.TransactionIdHeader, fmt.Sprintf("recover_%s_%d", workflowId, time.Now().Unix()))
	log.Info(nil, log.LOGGER_WORKFOLW, "<<--Start recover workflow-->>", zap.String("workflowId", workflowId))
	defer func() {
		if err != nil {
			log.Error(nil, log.LOGGER_WORKFOLW, "<<--Fail recover workflow-->>", zap.String("workflowId", workflowId), zap.Error(err))
		} else {
			log.Info(nil, log.LOGGER_WORKFOLW, "<<--Done recover workflow-->>", zap.String("workflowId", workflowId))
		}
	}()
	// 查workflow、node、link
	workflowRow, workNodes, workLinks, getWorkflowErr := getWorkflowData(ctx, workflowId)
	if getWorkflowErr != nil {
		err = getWorkflowErr
		return
	}
	if workflowRow.Status != models.JobStatusRunning {
		err = fmt.Errorf("workflow status:%s illegal", workflowRow.Status)
		return
	}
	// 初始化workflow并开始
	workObj := Workflow{ProcRunWorkflow: *workflowRow}
	workObj.Init(context.Background(), workNodes, workLinks)
	//GlobalWorkflowMap.Store(workObj.Id, &workObj)
	go workObj.Start(&models.ProcOperation{CreatedBy: "systemRecover"})
	return
}

func getWorkflowData(ctx context.Context, workflowId string) (workflowRow *models.ProcRunWorkflow, nodeList []*models.ProcRunNode, linkList []*models.ProcRunLink, err error) {
	var workflowRows []*models.ProcRunWorkflow
	err = db.WorkflowMysqlEngine.Context(ctx).SQL("select id,proc_ins_id,name,status,error_message,host,last_alive_time from proc_run_workflow where id=?", workflowId).Find(&workflowRows)
	if err != nil {
		err = fmt.Errorf("query workflow table fail,%s ", err.Error())
		return
	}
	if len(workflowRows) == 0 {
		err = fmt.Errorf("can not find workflow with id:%s ", workflowId)
		return
	}
	workflowRow = workflowRows[0]
	err = db.WorkflowMysqlEngine.Context(ctx).SQL("select * from proc_run_node where workflow_id=?", workflowId).Find(&nodeList)
	if err != nil {
		err = fmt.Errorf("query workflow node table fail,%s ", err.Error())
		return
	}
	err = db.WorkflowMysqlEngine.Context(ctx).SQL("select * from proc_run_link where workflow_id=?", workflowId).Find(&linkList)
	if err != nil {
		err = fmt.Errorf("query workflow link table fail,%s ", err.Error())
		return
	}
	return
}

// 每1小时检测有没有workflow要sleep，防止一直在内存中而且一直刷live time
func startSleepWorkflowJob() {
	t := time.NewTicker(1 * time.Hour).C
	for {
		<-t
		doSleepWorkflowJob()
	}
}

func doSleepWorkflowJob() {
	log.Info(nil, log.LOGGER_WORKFOLW, "<<--Start do sleep workflow job-->>")
	ctx := context.WithValue(context.Background(), models.TransactionIdHeader, fmt.Sprintf("sleep_workflow_%d", time.Now().Unix()))
	var workflowRows []*models.ProcRunWorkflow
	err := db.WorkflowMysqlEngine.Context(ctx).SQL("select id,proc_ins_id,name,status,stop,created_time,last_alive_time from proc_run_workflow where status=? and (last_alive_time-created_time)>7200 and `sleep`=0", models.JobStatusRunning).Find(&workflowRows)
	if err != nil {
		log.Error(nil, log.LOGGER_WORKFOLW, "query workflow table fail,", zap.Error(err))
		return
	}
	if len(workflowRows) == 0 {
		log.Info(nil, log.LOGGER_WORKFOLW, "<<--Quit do sleep workflow job-->>", zap.String("detail", "no workflow row match with sleep"))
		return
	}
	for _, row := range workflowRows {
		if isWorkflowNeedSleep(ctx, row) {
			if workIf, ok := GlobalWorkflowMap.Load(row.Id); ok {
				workObj := workIf.(*Workflow)
				if tmpErr := workObj.Sleep(); tmpErr != nil {
					log.Error(nil, log.LOGGER_WORKFOLW, "<<--Fail do sleep workflow job-->>", zap.String("workflowId", row.Id), zap.Error(tmpErr))
				} else {
					log.Info(nil, log.LOGGER_WORKFOLW, "<<--Done do sleep workflow job-->>", zap.String("workflowId", row.Id))
				}
			}
		} else {
			log.Info(nil, log.LOGGER_WORKFOLW, "<<--Ignore do sleep workflow job-->>", zap.String("workflowId", row.Id), zap.String("detail", "workflow node still running "))
		}
	}
	log.Info(nil, log.LOGGER_WORKFOLW, "<<--End do sleep workflow job-->>")
}

func isWorkflowNeedSleep(ctx context.Context, workflowRow *models.ProcRunWorkflow) (ok bool) {
	var procRunNodeRows []*models.ProcRunNode
	err := db.WorkflowMysqlEngine.Context(ctx).SQL("select * from proc_run_node where workflow_id=?", workflowRow.Id).Find(&procRunNodeRows)
	if err != nil {
		log.Error(nil, log.LOGGER_WORKFOLW, "check isWorkflowNeedSleep fail with query proc node table", zap.String("workflowId", workflowRow.Id))
		return
	}
	if len(procRunNodeRows) == 0 {
		return
	}
	currentNodes := []*models.ProcRunNode{}
	// 有失败的子编排节点的话不sleep
	failSubProcNodes := []*models.ProcRunNode{}
	for _, row := range procRunNodeRows {
		if row.Status == models.JobStatusRunning {
			currentNodes = append(currentNodes, row)
		} else if row.Status == models.JobStatusFail {
			if row.JobType == models.JobSubProcType {
				failSubProcNodes = append(failSubProcNodes, row)
			}
		}
	}
	if len(failSubProcNodes) > 0 {
		return
	}
	// 没有正在运行的节点
	if len(currentNodes) == 0 {
		ok = true
		return
	}
	// 正在运行的节点是人工节点
	allHumanTypeFlag := true
	for _, v := range currentNodes {
		if v.JobType != models.JobHumanType {
			allHumanTypeFlag = false
			break
		}
	}
	if allHumanTypeFlag {
		ok = true
		return
	}
	return
}
