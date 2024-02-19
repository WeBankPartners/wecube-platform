package workflow

import (
	"context"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"time"
)

func StartCronJob() {
	go startScanOperationJob()
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
	err := db.MysqlEngine.SQL("select id,workflow_id,node_id,operation,message,created_by,created_time from proc_run_operation where status='wait' and workflow_id in ("+filterSql+")", filterParam...).Find(&operationRows)
	if err != nil {
		log.Logger.Error("query proc operation fail", log.Error(err))
		return
	}
	if len(operationRows) == 0 {
		return
	}
	for _, operation := range operationRows {
		go handleProOperation(operation)
	}
}

func handleProOperation(operation *models.ProcRunOperation) {
	// 尝试抢占
	execResult, err := db.MysqlEngine.Exec("update proc_run_operation set status='doing',handle_by=?,start_time=? where id=? and status='wait'", instanceHost, time.Now(), operation.Id)
	if err != nil {
		log.Logger.Error("try to handle operation fail", log.String("host", instanceHost), log.Int64("operation", operation.Id), log.Error(err))
		return
	}
	if rowAffectNum, _ := execResult.RowsAffected(); rowAffectNum <= 0 {
		log.Logger.Warn("try to handle operation,but too late", log.String("host", instanceHost), log.Int64("operation", operation.Id))
		return
	}
	if workIf, ok := GlobalWorkflowMap.Load(operation.WorkflowId); ok {
		workObj := workIf.(*Workflow)
		opObj := models.ProcOperation{Ctx: context.Background(), WorkflowId: operation.WorkflowId, Message: operation.Message, CreatedBy: operation.CreatedBy}
		switch operation.Operation {
		case "kill":
			workObj.Kill(&opObj)
		case "retry":
			workObj.RetryNode(operation.NodeId)
		case "ignore":
			workObj.IgnoreNode(operation.NodeId)
		case "approve":
			workObj.ApproveNode(operation.NodeId, operation.Message)
		case "stop":
			workObj.Stop(&opObj)
		case "continue":
			workObj.Continue(&opObj)
		default:
			log.Logger.Error("handle operation error with illegal operation", log.String("operation", operation.Operation))
		}
		_, err = db.MysqlEngine.Exec("update proc_run_operation set status='done',end_time=? where id=?", time.Now(), operation.Id)
		if err != nil {
			log.Logger.Error("handle operation done but update database fail", log.String("host", instanceHost), log.Int64("operation", operation.Id), log.Error(err))
		}
	} else {
		// check need to wakeup
		log.Logger.Warn("handle operation message warning,can not find workflow", log.String("workflowId", operation.WorkflowId))
		workflowRow, queryErr := getWorkflowRow(operation.WorkflowId)
		if queryErr != nil {
			log.Logger.Error("handle operation fail with get workflow row", log.Error(queryErr))
			return
		}
		if !workflowRow.Sleep {
			// 如果不是sleep，应该有其它实例在处理它，如果也没有其它实例处理它，那等抢占的worker把它接管后再处理
			log.Logger.Warn("give up handle operation,workflow is not sleeping", log.String("workflowId", operation.WorkflowId))
			return
		}
		// 尝试恢复workflow
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
		doTaskOver()
	}
}

func doTaskOver() {

}
