package workflow

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/execution"

	"sync"
	"time"
)

var (
	GlobalWorkflowMap = new(sync.Map)
	instanceHost      = "-"
	timeoutErrorTpl   = "timeout in %d minute"
)

type Workflow struct {
	models.ProcRunWorkflow
	Ctx              context.Context
	Nodes            []*WorkNode
	Links            []*models.ProcRunLink
	stopChan         chan models.ProcOperation
	killChan         chan models.ProcOperation
	doneChan         chan int
	sleepChan        chan int
	ErrList          []*models.WorkProblemErrObj
	statusLock       *sync.RWMutex
	errorLock        *sync.RWMutex
	stopNodeChanList []chan int
}

func (w *Workflow) Init(ctx context.Context, nodes []*models.ProcRunNode, links []*models.ProcRunLink) {
	w.Links = links
	w.Ctx, _ = context.WithCancel(ctx)
	for _, node := range nodes {
		workNodeObj := WorkNode{ProcRunNode: *node, Ctx: w.Ctx}
		workNodeObj.Init(w)
		w.Nodes = append(w.Nodes, &workNodeObj)
	}
	w.stopChan = make(chan models.ProcOperation, 1)
	w.killChan = make(chan models.ProcOperation, 1)
	w.doneChan = make(chan int, 1)
	w.sleepChan = make(chan int, 1)
	w.statusLock = new(sync.RWMutex)
	w.errorLock = new(sync.RWMutex)
	go w.heartbeat()
}

func (w *Workflow) Start(input *models.ProcOperation) {
	GlobalWorkflowMap.Store(w.Id, w)
	if w.Status != models.JobStatusRunning {
		w.setStatus(models.JobStatusRunning, input)
	}
	var startIndexList []int
	for i, node := range w.Nodes {
		go node.Ready()
		if node.JobType == models.JobStartType {
			startIndexList = append(startIndexList, i)
		}
	}
	time.Sleep(500 * time.Millisecond)
	for _, index := range startIndexList {
		w.Nodes[index].StartChan <- 1
	}
	var killFlag, sleepFlag bool
	select {
	case killInput := <-w.killChan:
		killFlag = true
		w.setStatus(models.JobStatusKill, &killInput)
	case <-w.doneChan:
	case <-w.sleepChan:
		sleepFlag = true
	}
	if killFlag {
		log.Logger.Info("<--workflow kill-->")
		<-w.doneChan
	} else if sleepFlag {
		log.Logger.Info("<--workflow sleep-->")
	} else {
		log.Logger.Info("<--workflow done-->")
		w.setStatus(w.Status, nil)
	}
	GlobalWorkflowMap.Delete(w.Id)
}

func (w *Workflow) nodeDoneCallback(node *WorkNode) {
	if node.JobType == models.JobEndType {
		w.Status = models.JobStatusSuccess
		w.doneChan <- 1
		return
	}
	if node.JobType == models.JobBreakType {
		w.Status = models.JobStatusFail
		w.doneChan <- 1
		return
	}
	decisionChose := ""
	if node.JobType == models.JobDecisionType {
		decisionChose = node.Input
		log.Logger.Info("decision node receive choose", log.String(decisionChose, "decisionChose"))
	}
	if node.Err != nil {
		w.updateErrorList(true, "", &models.WorkProblemErrObj{NodeId: node.Id, NodeName: node.Name, ErrMessage: node.Err.Error()})
		//w.setStatus(models.JobStatusFail, &models.ProcOperation{NodeErr: &models.WorkProblemErrObj{NodeId: node.Id, NodeName: node.Name, ErrMessage: node.Err.Error()}})
		return
	}
	// stop 的时候要处理普通节点等待
	curStatus := w.getStatus()
	if curStatus == models.JobStatusKill {
		return
	}
	if curStatus == "stop" {
		waitStopChan := make(chan int, 1)
		w.stopNodeChanList = append(w.stopNodeChanList, waitStopChan)
		<-waitStopChan
	}
	// 找到节点下一跳发出start信号
	for _, ref := range w.Links {
		if decisionChose != "" {
			if ref.Name != decisionChose {
				continue
			}
		}
		if ref.Source == node.Id {
			for _, targetNode := range w.Nodes {
				if targetNode.Id == ref.Target {
					if targetNode.JobType == models.JobDecisionType {
						targetNode.Input = node.Output
					}
					targetNode.StartChan <- 1
					break
				}
			}
		}
	}
}

func (w *Workflow) Stop(input *models.ProcOperation) {
	w.setStatus("stop", input)
}

func (w *Workflow) Continue(input *models.ProcOperation) {
	w.setStatus(models.JobStatusRunning, input)
	for _, v := range w.stopNodeChanList {
		v <- 1
	}
}

func (w *Workflow) Kill(input *models.ProcOperation) {
	w.killChan <- *input
	<-w.doneChan
}

func (w *Workflow) Sleep() (err error) {
	if err = setWorkflowSleepDB(w.Id, true); err != nil {
		return
	}
	w.ProcRunWorkflow.Sleep = true
	w.sleepChan <- 1
	return
}

func (w *Workflow) setStatus(status string, op *models.ProcOperation) {
	w.statusLock.Lock()
	w.Status = status
	w.statusLock.Unlock()
	if status == models.JobStatusFail {
		if op != nil && op.NodeErr != nil {
			w.updateErrorList(true, "", op.NodeErr)
		}
	}
	updateWorkflowDB(&w.ProcRunWorkflow, op)
}

func (w *Workflow) getStatus() (status string) {
	w.statusLock.RLock()
	status = w.Status
	w.statusLock.RUnlock()
	return
}

func (w *Workflow) updateErrorList(addFlag bool, nodeId string, errorObj *models.WorkProblemErrObj) {
	var errorMesg string
	w.errorLock.Lock()
	if addFlag {
		if errorObj != nil {
			w.ErrList = append(w.ErrList, errorObj)
		}
		errBytes, _ := json.Marshal(w.ErrList)
		w.ErrorMessage = string(errBytes)
		errorMesg = w.ErrorMessage
	} else {
		newErrorList := []*models.WorkProblemErrObj{}
		for _, v := range w.ErrList {
			if v.NodeId == nodeId {
				continue
			}
			newErrorList = append(newErrorList, v)
		}
		w.ErrList = newErrorList
		if len(w.ErrList) == 0 {
			w.ErrorMessage = ""
		} else {
			errBytes, _ := json.Marshal(w.ErrList)
			w.ErrorMessage = string(errBytes)
		}
		errorMesg = w.ErrorMessage
	}
	w.errorLock.Unlock()
	db.MysqlEngine.Exec("update proc_run_workflow set error_message=?,updated_time=? where id=?", errorMesg, time.Now(), w.Id)
}

func (w *Workflow) RetryNode(nodeId string) {
	// check node status fail
	var nodeObj *WorkNode
	for _, node := range w.Nodes {
		if node.Id == nodeId {
			nodeObj = node
			break
		}
	}
	if nodeObj == nil {
		log.Logger.Error("can not find node in workflow", log.String("node", nodeId), log.String("workflowId", w.Id))
		return
	}
	nodeObj.Input = ""
	nodeObj.Status = models.JobStatusRunning
	go nodeObj.Ready()
	time.Sleep(500 * time.Millisecond)
	nodeObj.StartTime = time.Now()
	updateNodeDB(&nodeObj.ProcRunNode)
	w.updateErrorList(false, nodeId, nil)
	if len(w.ErrList) == 0 {
		w.setStatus(models.JobStatusRunning, nil)
	}
	nodeObj.StartChan <- 1
}

func (w *Workflow) IgnoreNode(nodeId string) {
	// check node status fail
	var nodeObj *WorkNode
	for _, node := range w.Nodes {
		if node.Id == nodeId {
			nodeObj = node
			break
		}
	}
	if nodeObj == nil {
		log.Logger.Error("can not find node in workflow", log.String("node", nodeId), log.String("workflowId", w.Id))
		return
	}
	nodeObj.Status = models.JobStatusSuccess
	updateNodeDB(&nodeObj.ProcRunNode)
	w.updateErrorList(false, nodeId, nil)
	for _, ref := range w.Links {
		if ref.Source == nodeId {
			for _, targetNode := range w.Nodes {
				if targetNode.Id == ref.Target {
					targetNode.StartChan <- 1
					break
				}
			}
		}
	}
}

func (w *Workflow) ApproveNode(nodeId, message string) {
	// check node status fail
	var nodeObj *WorkNode
	for _, node := range w.Nodes {
		if node.Id == nodeId {
			nodeObj = node
			break
		}
	}
	if nodeObj == nil {
		log.Logger.Error("can not find node in workflow", log.String("node", nodeId), log.String("workflowId", w.Id))
		return
	}
	nodeObj.Callback(message)
}

func (w *Workflow) heartbeat() {
	t := time.NewTicker(10 * time.Second).C
	for {
		if w.ProcRunWorkflow.Sleep {
			log.Logger.Info("workflow heartbeat get quit with sleep flag", log.String("workflowId", w.Id))
			break
		}
		wStatus := w.getStatus()
		if wStatus == models.JobStatusSuccess || wStatus == models.JobStatusKill || wStatus == models.JobStatusFail {
			log.Logger.Info("workflow heartbeat get quit status", log.String("workflowId", w.Id), log.String("status", wStatus))
			break
		}
		if _, err := db.MysqlEngine.Exec("update proc_run_workflow set host=?,last_alive_time=? where id=?", instanceHost, time.Now(), w.Id); err != nil {
			log.Logger.Error("workflow heartbeat update alive time fail", log.String("workflowId", w.Id), log.Error(err))
		}
		<-t
	}
	log.Logger.Info("workflow heartbeat quit", log.String("workflowId", w.Id))
}

type WorkNode struct {
	models.ProcRunNode
	Ctx          context.Context
	workflow     *Workflow
	StartChan    chan int
	DoneChan     chan int
	Err          error
	callbackChan chan string
}

func (n *WorkNode) Init(w *Workflow) {
	n.workflow = w
	n.StartChan = make(chan int, 1)
	n.DoneChan = make(chan int, 1)
	n.callbackChan = make(chan string, 1)
}

func (n *WorkNode) Ready() {
	log.Logger.Info("init job", log.String("nodeId", n.Id))
	select {
	case <-n.Ctx.Done():
		log.Logger.Info("job cancel", log.String("nodeId", n.Id))
		return
	case <-n.StartChan:
		log.Logger.Info("ready job ", log.String("nodeId", n.Id))
	}
	if n.StartTime.IsZero() {
		n.StartTime = time.Now()
	}
	go n.start()
	if n.Timeout > 0 {
		select {
		case <-time.After(time.Duration(n.Timeout) * time.Minute):
			n.ErrorMessage = fmt.Sprintf(timeoutErrorTpl, n.Timeout)
			n.Err = errors.New(n.ErrorMessage)
			n.Status = models.JobStatusTimeout
			updateNodeDB(&n.ProcRunNode)
		case <-n.DoneChan:
			log.Logger.Info("<--- done node", log.String("id", n.Id), log.String("type", n.JobType))
		}
	} else {
		<-n.DoneChan
		log.Logger.Info("<--- done node", log.String("id", n.Id), log.String("type", n.JobType))
	}
	if n.Err != nil {
		log.Logger.Error("node error", log.String("id", n.Id), log.Error(n.Err))
	}
	n.workflow.nodeDoneCallback(n)
}

func (n *WorkNode) start() {
	if n.Status == models.JobStatusSuccess {
		n.DoneChan <- 1
		return
	}
	if n.Status == models.JobStatusFail {
		n.Err = fmt.Errorf(n.ErrorMessage)
		n.DoneChan <- 1
		return
	}
	retryFlag := false
	if n.Status == models.JobStatusRunning {
		retryFlag = true
		if n.Timeout > 0 {
			// 如果是恢复态，自动化和数据写入任务时间太久的情况下就置为超时，不然可能时间太长一些数据都不一样了，让用户自行重试
			if n.JobType == models.JobAutoType || n.JobType == models.JobDataType {
				if time.Since(n.StartTime).Minutes() > float64(n.Timeout) {
					n.ErrorMessage = fmt.Sprintf(timeoutErrorTpl, n.Timeout)
					n.Err = errors.New(n.ErrorMessage)
					n.Status = models.JobStatusTimeout
					updateNodeDB(&n.ProcRunNode)
					n.DoneChan <- 1
					return
				}
			}
		}
	}
	log.Logger.Info("---> start node", log.String("id", n.Id), log.String("type", n.JobType), log.String("input", n.Input))
	if !retryFlag {
		n.Status = models.JobStatusRunning
		updateNodeDB(&n.ProcRunNode)
	}
	switch n.JobType {
	case models.JobStartType:
		break
	case models.JobEndType:
		break
	case models.JobBreakType:
		break
	case models.JobAutoType:
		n.Output, n.Err = n.doAutoJob(retryFlag)
	case models.JobDataType:
		n.Output, n.Err = n.doDataJob(retryFlag)
	case models.JobHumanType:
		n.Output, n.Err = n.doHumanJob(retryFlag)
	case models.JobForkType:
		break
	case models.JobMergeType:
		needStartCount := 0
		for _, ref := range n.workflow.Links {
			if ref.Target == n.Id {
				needStartCount = needStartCount + 1
			}
		}
		if needStartCount > 1 {
			log.Logger.Info("merge wait other signal", log.Int("wait signal num", needStartCount-1))
			n.Status = "wait"
			updateNodeDB(&n.ProcRunNode)
			for needStartCount > 1 {
				<-n.StartChan
				needStartCount = needStartCount - 1
				log.Logger.Info("merge get another signal", log.Int("wait signal num", needStartCount-1))
			}
		}
	case models.JobTimeType:
		n.Output, n.Err = n.doTimeJob(retryFlag)
	case models.JobDateType:
		n.Output, n.Err = n.doDateJob(retryFlag)
	case models.JobDecisionType:
		if n.Input == "" {
			for _, tmpLink := range n.workflow.Links {
				if tmpLink.Target == n.Id {
					n.Input = getNodeOutputData(tmpLink.Source)
					break
				}
			}
			if n.Input == "" {
				n.Err = fmt.Errorf("dicision type receive empty choose")
			}
		}
	}
	if n.Err == nil {
		n.Status = models.JobStatusSuccess
		updateNodeDB(&n.ProcRunNode)
	} else {
		n.Status = models.JobStatusFail
		n.ErrorMessage = n.Err.Error()
		updateNodeDB(&n.ProcRunNode)
	}
	n.DoneChan <- 1
}

func (n *WorkNode) doAutoJob(retry bool) (output string, err error) {
	log.Logger.Info("do auto job", log.String("nodeId", n.Id), log.String("input", n.Input))
	err = execution.DoWorkflowAutoJob(n.Ctx, n.Id, "", retry)
	if err != nil {
		log.Logger.Error("do auto job error", log.Error(err))
	}
	return
}

func (n *WorkNode) doDataJob(retry bool) (output string, err error) {
	log.Logger.Info("do data job", log.String("nodeId", n.Id), log.String("input", n.Input))
	err = execution.DoWorkflowDataJob(n.Ctx, n.Id, retry)
	if err != nil {
		log.Logger.Error("do data job error", log.Error(err))
	}
	return
}

func (n *WorkNode) doHumanJob(recoverFlag bool) (output string, err error) {
	log.Logger.Info("do human job", log.String("nodeId", n.Id), log.String("input", n.Input))
	// call task
	if recoverFlag {
		if n.ErrorMessage != "" {
			// 区分是重试还是编排重新加载，重试(有报错的情况下)的话要发过请求，加载的话不需要
			recoverFlag = false
		}
	}
	err = execution.DoWorkflowHumanJob(n.Ctx, n.Id, recoverFlag)
	if err != nil {
		log.Logger.Error("do human job error", log.Error(err))
		return
	}
	// wait callback
	callbackMessage := <-n.callbackChan
	var callbackData models.PluginTaskCreateResp
	if err = json.Unmarshal([]byte(callbackMessage), &callbackData); err != nil {
		err = fmt.Errorf("json unmarshal human job callback data fail,%s ", err.Error())
		return
	}
	output, err = execution.HandleCallbackHumanJob(n.Ctx, n.Id, &callbackData)
	return
}

func (n *WorkNode) doTimeJob(recoverFlag bool) (output string, err error) {
	log.Logger.Info("do time job", log.String("nodeId", n.Id), log.String("input", n.Input))
	var timeConfig models.TimeNodeParam
	if err = json.Unmarshal([]byte(n.Input), &timeConfig); err != nil {
		err = fmt.Errorf("time node param:%s json unmarshal fail,%s ", n.Input, err.Error())
		return
	}
	if timeConfig.Unit == "" || timeConfig.Duration <= 0 {
		err = fmt.Errorf("time node param:%s config illegal ", n.Input)
		return
	}
	var timeDuration time.Duration
	switch timeConfig.Unit {
	case "sec":
		timeDuration = time.Duration(timeConfig.Duration) * time.Second
	case "min":
		timeDuration = time.Duration(timeConfig.Duration) * time.Minute
	case "hour":
		timeDuration = time.Duration(timeConfig.Duration) * time.Hour
	case "day":
		timeDuration = time.Duration(timeConfig.Duration) * time.Hour * 24
	default:
		err = fmt.Errorf("time node param:%s unit illegal ", n.Input)
	}
	if err != nil {
		return
	}
	if recoverFlag {
		nowSubSec := time.Since(n.StartTime).Seconds()
		if nowSubSec > timeDuration.Seconds() {
			log.Logger.Info("time job already start,now time match done", log.String("startTime", n.StartTime.Format(models.DateTimeFormat)), log.Float64("waitSec", timeDuration.Seconds()))
		} else {
			time.Sleep(time.Duration(timeDuration.Seconds()-nowSubSec) * time.Second)
		}
	} else {
		time.Sleep(timeDuration)
	}
	return
}

func (n *WorkNode) doDateJob(recoverFlag bool) (output string, err error) {
	log.Logger.Info("do date job", log.String("nodeId", n.Id), log.String("input", n.Input), log.Bool("recover", recoverFlag))
	var timeConfig models.TimeNodeParam
	if err = json.Unmarshal([]byte(n.Input), &timeConfig); err != nil {
		err = fmt.Errorf("time node param:%s json unmarshal fail,%s ", n.Input, err.Error())
		return
	}
	t, parseErr := time.ParseInLocation("2006-01-02 15:04:05", timeConfig.Date, time.Local)
	if parseErr != nil {
		err = fmt.Errorf("date node parse time:%s fail", timeConfig.Date)
		return
	}
	timeSub := t.Unix() - time.Now().Unix()
	if timeSub < 0 {
		return
	} else {
		time.Sleep(time.Duration(timeSub) * time.Second)
	}
	return
}

func (n *WorkNode) Callback(message string) {
	n.callbackChan <- message
}

func updateWorkflowDB(w *models.ProcRunWorkflow, op *models.ProcOperation) {
	if op == nil {
		op = &models.ProcOperation{
			CreatedBy: "sys",
		}
	}
	if op.Ctx == nil {
		op.Ctx = context.Background()
	}
	nowTime := time.Now()
	var actions []*db.ExecAction
	if w.Status == "stop" {
		actions = append(actions, &db.ExecAction{Sql: "update proc_run_workflow set stop=1,updated_time=? where id=?", Param: []interface{}{nowTime, w.Id}})
	} else if w.Status == "sleep" {
		actions = append(actions, &db.ExecAction{Sql: "update proc_run_workflow set sleep=1,updated_time=? where id=?", Param: []interface{}{nowTime, w.Id}})
	} else if w.Status == models.JobStatusRunning {
		actions = append(actions, &db.ExecAction{Sql: "update proc_run_workflow set stop=0,sleep=0,status=?,updated_time=?,host=? where id=?", Param: []interface{}{w.Status, nowTime, instanceHost, w.Id}})
	} else if w.Status == models.JobStatusFail {
		actions = append(actions, &db.ExecAction{Sql: "update proc_run_workflow set status=?,error_message=?,updated_time=? where id=?", Param: []interface{}{w.Status, w.ErrorMessage, nowTime, w.Id}})
	} else {
		actions = append(actions, &db.ExecAction{Sql: "update proc_run_workflow set status=?,updated_time=? where id=?", Param: []interface{}{w.Status, nowTime, w.Id}})
	}
	actions = append(actions, &db.ExecAction{Sql: "update proc_ins set status=?,updated_time=? where id=?", Param: []interface{}{w.Status, nowTime, w.ProcInsId}})
	actions = append(actions, &db.ExecAction{Sql: "insert into proc_run_work_record(workflow_id,host,`action`,message,created_by,created_time) values (?,?,?,?,?,?)", Param: []interface{}{w.Id, instanceHost, w.Status, op.Message, op.CreatedBy, nowTime}})
	if err := db.Transaction(actions, op.Ctx); err != nil {
		log.Logger.Error("record workflow state fail", log.String("workflowId", w.Id), log.Error(err))
	}
}

func updateNodeDB(n *models.ProcRunNode) {
	var err error
	var actions []*db.ExecAction
	nowTime := time.Now()
	if n.Status == models.JobStatusRunning {
		actions = append(actions, &db.ExecAction{Sql: "update proc_run_node set status=?,start_time=?,updated_time=? where id=?", Param: []interface{}{n.Status, n.StartTime, nowTime, n.Id}})
		actions = append(actions, &db.ExecAction{Sql: "update proc_ins_node set status=?,updated_time=? where id=?", Param: []interface{}{n.Status, nowTime, n.ProcInsNodeId}})
		if n.JobType == models.JobDecisionType {
			actions = append(actions, &db.ExecAction{Sql: "update proc_run_node set input=? where id=?", Param: []interface{}{n.Input, n.Id}})
		}
	} else if n.Status == models.JobStatusFail {
		actions = append(actions, &db.ExecAction{Sql: "update proc_run_node set status=?,error_message=?,end_time=?,updated_time=? where id=?", Param: []interface{}{n.Status, n.ErrorMessage, nowTime, nowTime, n.Id}})
		actions = append(actions, &db.ExecAction{Sql: "update proc_ins_node set status=?,error_msg=?,updated_time=? where id=?", Param: []interface{}{n.Status, n.ErrorMessage, nowTime, n.ProcInsNodeId}})
	} else if n.Status == models.JobStatusSuccess {
		actions = append(actions, &db.ExecAction{Sql: "update proc_run_node set status=?,`output`=?,end_time=?,updated_time=? where id=?", Param: []interface{}{n.Status, n.Output, nowTime, nowTime, n.Id}})
		actions = append(actions, &db.ExecAction{Sql: "update proc_ins_node set status=?,updated_time=? where id=?", Param: []interface{}{n.Status, nowTime, n.ProcInsNodeId}})
	} else if n.Status == models.JobStatusTimeout {
		actions = append(actions, &db.ExecAction{Sql: "update proc_run_node set status=?,error_message=?,end_time=?,updated_time=? where id=?", Param: []interface{}{n.Status, n.ErrorMessage, nowTime, nowTime, n.Id}})
		actions = append(actions, &db.ExecAction{Sql: "update proc_ins_node set status=?,error_msg=?,updated_time=? where id=?", Param: []interface{}{n.Status, n.ErrorMessage, nowTime, n.ProcInsNodeId}})
	} else {
		actions = append(actions, &db.ExecAction{Sql: "update proc_run_node set status=?,updated_time=? where id=?", Param: []interface{}{n.Status, nowTime, n.Id}})
		actions = append(actions, &db.ExecAction{Sql: "update proc_ins_node set status=?,updated_time=? where id=?", Param: []interface{}{n.Status, nowTime, n.ProcInsNodeId}})
	}
	err = db.Transaction(actions, context.Background())
	if err != nil {
		log.Logger.Error("record node state fail", log.String("nodeId", n.Id), log.Error(err))
	}
}

func getWorkflowRow(workflowId string) (result *models.ProcRunWorkflow, err error) {
	var workflowRows []*models.ProcRunWorkflow
	err = db.MysqlEngine.SQL("select id,name,status,`sleep`,stop,host,last_alive_time from proc_run_workflow where id=?", workflowId).Find(&workflowRows)
	if err != nil {
		err = fmt.Errorf("query workflow table fail,%s ", err.Error())
	} else {
		if len(workflowRows) == 0 {
			err = fmt.Errorf("can not find workflow with id:%s ", workflowId)
		} else {
			result = workflowRows[0]
		}
	}
	return
}

func getNodeOutputData(procRunNodeId string) (output string) {
	queryRows, _ := db.MysqlEngine.QueryString("select `output` from proc_run_node where id=?", procRunNodeId)
	if len(queryRows) > 0 {
		output = queryRows[0]["output"]
	}
	return
}

func setWorkflowSleepDB(workflowId string, sleepFlag bool) (err error) {
	if sleepFlag {
		_, err = db.MysqlEngine.Exec("update proc_run_workflow set `sleep`=1 where id=?", workflowId)
	} else {
		_, err = db.MysqlEngine.Exec("update proc_run_workflow set `sleep`=0 where id=?", workflowId)
	}
	if err != nil {
		err = fmt.Errorf("update proc workflow:%s sleep true fail,%s ", workflowId, err.Error())
	}
	return
}
