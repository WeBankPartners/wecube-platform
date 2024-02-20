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
	instanceHost      = "127.0.0.1"
)

type Workflow struct {
	models.ProcRunWorkflow
	Ctx              context.Context
	Nodes            []*WorkNode
	Links            []*models.ProcRunLink
	stopChan         chan models.ProcOperation
	killChan         chan models.ProcOperation
	doneChan         chan int
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
	w.statusLock = new(sync.RWMutex)
	w.errorLock = new(sync.RWMutex)
	go w.heartbeat()
}

func (w *Workflow) Start(input *models.ProcOperation) {
	w.setStatus("running", input)
	var startIndexList []int
	for i, node := range w.Nodes {
		go node.Ready()
		if node.JobType == "start" {
			startIndexList = append(startIndexList, i)
		}
	}
	time.Sleep(500 * time.Millisecond)
	for _, index := range startIndexList {
		w.Nodes[index].StartChan <- 1
	}
	var killFlag bool
	select {
	case killInput := <-w.killChan:
		killFlag = true
		w.setStatus("kill", &killInput)
	case <-w.doneChan:
	}
	if killFlag {
		log.Logger.Info("<--workflow kill-->")
		<-w.doneChan
	} else {
		log.Logger.Info("<--workflow done-->")
		w.setStatus("success", nil)
	}
}

func (w *Workflow) nodeDoneCallback(node *WorkNode) {
	if node.JobType == "end" {
		w.Status = "success"
		w.doneChan <- 1
		return
	}
	if node.JobType == "break" {
		w.Status = "fail"
		w.doneChan <- 1
		return
	}
	decisionChose := ""
	if node.JobType == "decision" {
		decisionChose = fmt.Sprintf("%s", node.Input)
		if decisionChose == "" {
			node.Err = fmt.Errorf("decision node recive empty choose")
			w.ErrList = append(w.ErrList, &models.WorkProblemErrObj{NodeId: node.Id, NodeName: node.Name, ErrMessage: node.Err.Error()})
			w.doneChan <- 1
			return
		}
	}
	if node.Err != nil {
		w.setStatus("problem", &models.ProcOperation{NodeErr: &models.WorkProblemErrObj{NodeId: node.Id, NodeName: node.Name, ErrMessage: node.Err.Error()}})
		return
	}
	// stop 的时候要处理普通节点等待
	curStatus := w.getStatus()
	if curStatus == "kill" {
		return
	}
	if curStatus == "stop" {
		waitStopChan := make(chan int, 1)
		w.stopNodeChanList = append(w.stopNodeChanList, waitStopChan)
		<-waitStopChan
	}
	for _, ref := range w.Links {
		if decisionChose != "" {
			if ref.Name != decisionChose {
				continue
			}
		}
		if ref.Source == node.Id {
			for _, targetNode := range w.Nodes {
				if targetNode.JobType == "decision" {
					targetNode.Input = node.Output
				}
				if targetNode.Id == ref.Target {
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
	w.setStatus("running", input)
	for _, v := range w.stopNodeChanList {
		v <- 1
	}
}

func (w *Workflow) Kill(input *models.ProcOperation) {
	w.killChan <- *input
	<-w.doneChan
}

func (w *Workflow) Sleep() {

}

func (w *Workflow) setStatus(status string, op *models.ProcOperation) {
	w.statusLock.Lock()
	w.Status = status
	w.statusLock.Unlock()
	if status == "problem" {
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
	w.errorLock.Lock()
	if addFlag {
		if errorObj != nil {
			w.ErrList = append(w.ErrList, errorObj)
		}
		errBytes, _ := json.Marshal(w.ErrList)
		w.ErrorMessage = string(errBytes)
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
	}
	w.errorLock.Unlock()
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
	go nodeObj.Ready()
	time.Sleep(100 * time.Millisecond)
	w.updateErrorList(false, nodeId, nil)
	if len(w.ErrList) == 0 {
		w.setStatus("running", nil)
	}
	nodeObj.StartChan <- 1
	return
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
	nodeObj.Status = "success"
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
	return
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
	return
}

func (w *Workflow) heartbeat() {
	t := time.NewTicker(10 * time.Second).C
	for {
		if _, err := db.MysqlEngine.Exec("update proc_run_workflow set host=?,last_alive_time=? where id=?", w.Host, time.Now(), w.Id); err != nil {
			log.Logger.Error("workflow heartbeat update alive time fail", log.String("workflowId", w.Id), log.Error(err))
		}
		<-t
	}
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
	go n.start()
	if n.Timeout > 0 {
		select {
		case <-time.After(time.Duration(n.Timeout) * time.Second):
			n.ErrorMessage = fmt.Sprintf("timeout in %ds", n.Timeout)
			n.Err = errors.New(n.ErrorMessage)
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
	log.Logger.Info("---> start node", log.String("id", n.Id), log.String("type", n.JobType))
	n.Status = "running"
	updateNodeDB(&n.ProcRunNode)
	switch n.JobType {
	case "start":
		break
	case "end":
		break
	case "break":
		break
	case "auto":
		n.Output, n.Err = n.doAutoJob()
	case "data":
		n.Output, n.Err = n.doDataJob()
	case "human":
		n.Output, n.Err = n.doHumanJob()
	case "fork":
		break
	case "merge":
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
	case "time":
		n.Output, n.Err = n.doTimeJob()
	case "date":
		n.Output, n.Err = n.doDateJob()
	case "decision":
		break
	}
	if n.Err == nil {
		n.Status = "success"
		updateNodeDB(&n.ProcRunNode)
	} else {
		n.Status = "fail"
		n.ErrorMessage = n.Err.Error()
		updateNodeDB(&n.ProcRunNode)
	}
	n.DoneChan <- 1
}

func (n *WorkNode) doAutoJob() (output string, err error) {
	log.Logger.Info("do auto job", log.String("nodeId", n.Id), log.String("input", n.Input))
	err = execution.DoWorkflowAutoJob(n.Ctx, n.Id, "")
	return
}

func (n *WorkNode) doDataJob() (output string, err error) {
	log.Logger.Info("do data job", log.String("nodeId", n.Id), log.String("input", n.Input))
	return
}

func (n *WorkNode) doHumanJob() (output string, err error) {
	log.Logger.Info("do human job", log.String("nodeId", n.Id), log.String("input", n.Input))
	// call task
	// wait callback
	callbackMessage := <-n.callbackChan
	output = callbackMessage
	return
}

func (n *WorkNode) doTimeJob() (output string, err error) {
	log.Logger.Info("do time job", log.String("nodeId", n.Id), log.String("input", n.Input))
	var timeConfig models.TimeNodeParam
	if err = json.Unmarshal([]byte(n.Input), &timeConfig); err != nil {
		err = fmt.Errorf("time node param:%s json unmarshal fail,%s ", n.Input, err.Error())
		return
	}
	if timeConfig.Type != "duration" || timeConfig.Duration <= 0 {
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
	time.Sleep(timeDuration)
	return
}

func (n *WorkNode) doDateJob() (output string, err error) {
	log.Logger.Info("do date job", log.String("nodeId", n.Id), log.String("input", n.Input))
	var timeConfig models.TimeNodeParam
	if err = json.Unmarshal([]byte(n.Input), &timeConfig); err != nil {
		err = fmt.Errorf("time node param:%s json unmarshal fail,%s ", n.Input, err.Error())
		return
	}
	if timeConfig.Type != "date" || timeConfig.Date == "" {
		err = fmt.Errorf("date node param:%s config illegal ", n.Input)
		return
	}
	t, _ := time.ParseInLocation("2006-01-02 15:04:05", timeConfig.Date, time.Local)
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
	var actions []*db.ExecAction
	if w.Status == "stop" {
		actions = append(actions, &db.ExecAction{Sql: "update proc_run_workflow set stop=1,updated_time=? where id=?", Param: []interface{}{time.Now(), w.Id}})
	} else if w.Status == "sleep" {
		actions = append(actions, &db.ExecAction{Sql: "update proc_run_workflow set sleep=1,updated_time=? where id=?", Param: []interface{}{time.Now(), w.Id}})
	} else if w.Status == "running" {
		actions = append(actions, &db.ExecAction{Sql: "update proc_run_workflow set stop=0,sleep=0,status=?,updated_time=? where id=?", Param: []interface{}{w.Status, time.Now(), w.Id}})
	} else if w.Status == "problem" {
		actions = append(actions, &db.ExecAction{Sql: "update proc_run_workflow set status=?,error_message=?,updated_time=? where id=?", Param: []interface{}{w.Status, w.ErrorMessage, time.Now(), w.Id}})
	} else {
		actions = append(actions, &db.ExecAction{Sql: "update proc_run_workflow set status=?,updated_time=? where id=?", Param: []interface{}{w.Status, time.Now(), w.Id}})
	}
	actions = append(actions, &db.ExecAction{Sql: "insert into proc_run_work_record(workflow_id,host,`action`,message,created_by,created_time) values (?,?,?,?,?,?)", Param: []interface{}{w.Id, w.Host, w.Status, op.Message, op.CreatedBy, time.Now()}})
	if err := db.Transaction(actions, op.Ctx); err != nil {
		log.Logger.Error("record workflow state fail", log.String("workflowId", w.Id), log.Error(err))
	}
}

func updateNodeDB(n *models.ProcRunNode) {
	var err error
	nowTime := time.Now()
	if n.Status == "running" {
		_, err = db.MysqlEngine.Exec("update proc_run_node set status=?,start_time=?,updated_time=? where id=?", n.Status, nowTime, nowTime, n.Id)
	} else if n.Status == "fail" {
		_, err = db.MysqlEngine.Exec("update proc_run_node set status=?,error_message=?,end_time=?,updated_time=? where id=?", n.Status, n.ErrorMessage, nowTime, nowTime, n.Id)
	} else if n.Status == "success" {
		_, err = db.MysqlEngine.Exec("update proc_run_node set status=?,`output`=?,end_time=?,updated_time=? where id=?", n.Status, n.Output, nowTime, nowTime, n.Id)
	} else if n.Status == "timeout" {
		_, err = db.MysqlEngine.Exec("update proc_run_node set status=?,error_message=?,end_time=?,updated_time=? where id=?", n.Status, n.ErrorMessage, nowTime, nowTime, n.Id)
	} else {
		_, err = db.MysqlEngine.Exec("update proc_run_node set status=?,updated_time=? where id=?", n.Status, nowTime, n.Id)
	}
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
