package workflow

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
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
		log.WorkflowLogger.Info("<--workflow kill-->", log.String("wid", w.Id))
		<-w.doneChan
	} else if sleepFlag {
		log.WorkflowLogger.Info("<--workflow sleep-->", log.String("wid", w.Id))
	} else {
		log.WorkflowLogger.Info("<--workflow done-->", log.String("wid", w.Id))
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
		if node.Input == "" {
			node.Input = getNodeInputData(node.Id)
		}
		decisionChose = node.Input
		log.WorkflowLogger.Info("decision node receive choose", log.String("wid", w.Id), log.String("decisionChose", decisionChose))
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
	if curStatus == models.WorkflowStatusStop {
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
	w.setStatus(models.WorkflowStatusStop, input)
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
	db.WorkflowMysqlEngine.Exec("update proc_run_workflow set error_message=?,updated_time=? where id=?", errorMesg, time.Now(), w.Id)
}

func (w *Workflow) RetryNode(nodeId string, confirmFlag bool) {
	// check node status fail
	var nodeObj *WorkNode
	for _, node := range w.Nodes {
		if node.Id == nodeId {
			nodeObj = node
			break
		}
	}
	if nodeObj == nil {
		log.WorkflowLogger.Error("can not find node in workflow", log.String("node", nodeId), log.String("workflowId", w.Id))
		return
	}
	nodeObj.Input = ""
	nodeObj.Status = models.JobStatusRunning
	nodeObj.RetryFlag = true
	nodeObj.ConfirmFlag = confirmFlag
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
		log.WorkflowLogger.Error("can not find node in workflow", log.String("node", nodeId), log.String("workflowId", w.Id))
		return
	}
	if nodeObj.JobType == models.JobTimeType || nodeObj.JobType == models.JobDateType {
		nodeObj.ContinueChan <- 1
		return
	}
	nodeObj.Err = nil
	nodeObj.Status = models.JobStatusSuccess
	updateNodeDB(&nodeObj.ProcRunNode)
	w.updateErrorList(false, nodeId, nil)
	w.nodeDoneCallback(nodeObj)
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
		log.WorkflowLogger.Error("can not find node in workflow", log.String("node", nodeId), log.String("workflowId", w.Id))
		return
	}
	nodeObj.Callback(message)
}

func (w *Workflow) heartbeat() {
	t := time.NewTicker(10 * time.Second).C
	for {
		if w.ProcRunWorkflow.Sleep {
			log.WorkflowLogger.Info("workflow heartbeat get quit with sleep flag", log.String("workflowId", w.Id))
			break
		}
		wStatus := w.getStatus()
		if wStatus == models.JobStatusSuccess || wStatus == models.JobStatusKill || wStatus == models.JobStatusFail {
			log.WorkflowLogger.Info("workflow heartbeat get quit status", log.String("workflowId", w.Id), log.String("status", wStatus))
			break
		}
		if _, err := db.WorkflowMysqlEngine.Exec("update proc_run_workflow set host=?,last_alive_time=? where id=?", instanceHost, time.Now(), w.Id); err != nil {
			log.WorkflowLogger.Error("workflow heartbeat update alive time fail", log.String("workflowId", w.Id), log.Error(err))
		}
		<-t
	}
	log.WorkflowLogger.Info("workflow heartbeat quit", log.String("workflowId", w.Id))
}

type WorkNode struct {
	models.ProcRunNode
	Ctx          context.Context
	workflow     *Workflow
	StartChan    chan int
	DoneChan     chan int
	Err          error
	callbackChan chan string
	ContinueChan chan int
	RetryFlag    bool
	ConfirmFlag  bool
}

func (n *WorkNode) Init(w *Workflow) {
	n.workflow = w
	n.StartChan = make(chan int, 1)
	n.DoneChan = make(chan int, 1)
	n.callbackChan = make(chan string, 1)
	n.ContinueChan = make(chan int, 1)
}

func (n *WorkNode) Ready() {
	log.WorkflowLogger.Info("init job", log.String("nodeId", n.Id))
	select {
	case <-n.Ctx.Done():
		log.WorkflowLogger.Info("job cancel", log.String("nodeId", n.Id))
		return
	case <-n.StartChan:
		log.WorkflowLogger.Info("ready job ", log.String("nodeId", n.Id))
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
			log.WorkflowLogger.Info("<--- done node", log.String("id", n.Id), log.String("type", n.JobType))
		}
	} else {
		<-n.DoneChan
		log.WorkflowLogger.Info("<--- done node", log.String("id", n.Id), log.String("type", n.JobType))
	}
	if n.Err != nil {
		log.WorkflowLogger.Error("node error", log.String("id", n.Id), log.Error(n.Err))
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
	log.WorkflowLogger.Info("---> start node", log.String("id", n.Id), log.String("type", n.JobType), log.String("input", n.Input))
	if !retryFlag {
		n.Status = models.JobStatusRunning
		updateNodeDB(&n.ProcRunNode)
	}
	// 区分是重试还是编排重新加载，如果n.RetryFlag是true的话就是重试
	recoverFlag := false
	if retryFlag && !n.RetryFlag {
		recoverFlag = true
	}
	n.RetryFlag = false
	switch n.JobType {
	case models.JobStartType:
		break
	case models.JobEndType:
		break
	case models.JobBreakType:
		break
	case models.JobAutoType:
		n.Output, n.Err = n.doAutoJob()
	case models.JobDataType:
		n.Output, n.Err = n.doDataJob()
	case models.JobHumanType:
		n.Output, n.Err = n.doHumanJob(recoverFlag)
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
			log.WorkflowLogger.Info("merge wait other signal", log.Int("wait signal num", needStartCount-1))
			n.Status = "wait"
			updateNodeDB(&n.ProcRunNode)
			for needStartCount > 1 {
				<-n.StartChan
				needStartCount = needStartCount - 1
				log.WorkflowLogger.Info("merge get another signal", log.Int("wait signal num", needStartCount-1))
			}
		}
	case models.JobTimeType:
		n.Output, n.Err = n.doTimeJob(recoverFlag)
	case models.JobDateType:
		n.Output, n.Err = n.doDateJob(recoverFlag)
	case models.JobDecisionType:
		n.Input = getNodeInputData(n.Id)
		if n.Input == "" {
			for _, tmpLink := range n.workflow.Links {
				if tmpLink.Target == n.Id {
					n.Input = getNodeOutputData(tmpLink.Source)
					break
				}
			}
			if n.Input == "" {
				n.Input = <-n.callbackChan
				//n.Err = fmt.Errorf("dicision type receive empty choose")
			}
			if tmpErr := updateNodeInputData(n.Id, n.Input); tmpErr != nil {
				log.WorkflowLogger.Error("updateNodeInputData for decision job fail", log.Error(tmpErr))
			}
		}
	case models.JobSubProcType:
		n.Output, n.Err = n.doSubProcessJob(recoverFlag)
	case models.JobDecisionMergeType:
		break
	}
	if n.Err == nil {
		n.Status = models.JobStatusSuccess
		updateNodeDB(&n.ProcRunNode)
	} else {
		if n.Status != models.JobStatusRisky {
			n.Status = models.JobStatusFail
			n.ErrorMessage = n.Err.Error()
			if customErr, ok := n.Err.(exterror.CustomError); ok {
				if customErr.DetailErr != nil {
					n.ErrorMessage = fmt.Sprintf("%s (%s)", customErr.Error(), customErr.DetailErr.Error())
				}
			}
		}
		updateNodeDB(&n.ProcRunNode)
		if n.workflow.ParentRunNodeId != "" {
			updateNodeDB(&models.ProcRunNode{Id: n.workflow.ParentRunNodeId, Status: models.JobStatusFail, ErrorMessage: n.ErrorMessage})
		}
	}
	n.DoneChan <- 1
}

func (n *WorkNode) doAutoJob() (output string, err error) {
	log.WorkflowLogger.Info("do auto job", log.String("nodeId", n.Id), log.String("input", n.Input))
	continueToken := ""
	if n.ConfirmFlag {
		continueToken = "Y"
	}
	var risky bool
	risky, err = execution.DoWorkflowAutoJob(n.Ctx, n.Id, continueToken)
	if risky {
		n.Status = models.JobStatusRisky
		n.ErrorMessage = err.Error()
	}
	if err != nil {
		log.WorkflowLogger.Error("do auto job error", log.Error(err))
	}
	return
}

func (n *WorkNode) doDataJob() (output string, err error) {
	log.WorkflowLogger.Info("do data job", log.String("nodeId", n.Id), log.String("input", n.Input))
	err = execution.DoWorkflowDataJob(n.Ctx, n.Id)
	if err != nil {
		log.WorkflowLogger.Error("do data job error", log.Error(err))
	}
	return
}

func (n *WorkNode) doHumanJob(recoverFlag bool) (output string, err error) {
	log.WorkflowLogger.Info("do human job", log.String("nodeId", n.Id), log.String("input", n.Input))
	// call task
	//if recoverFlag {
	//	if n.ErrorMessage != "" {
	//		// 区分是重试还是编排重新加载，重试(有报错的情况下)的话要发过请求，加载的话不需要
	//		recoverFlag = false
	//	}
	//}
	err = execution.DoWorkflowHumanJob(n.Ctx, n.Id, recoverFlag)
	if err != nil {
		log.WorkflowLogger.Error("do human job error", log.Error(err))
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
	log.WorkflowLogger.Info("do time job", log.String("nodeId", n.Id), log.String("input", n.Input))
	var timeConfig models.TimeNodeParam
	if err = json.Unmarshal([]byte(n.Input), &timeConfig); err != nil {
		err = fmt.Errorf("time node param:%s json unmarshal fail,%s ", n.Input, err.Error())
		return
	}
	if timeConfig.Unit == "" || timeConfig.Duration < 0 {
		err = fmt.Errorf("time node param:%s config illegal ", n.Input)
		return
	}
	var timeDuration time.Duration
	var waitSec int
	switch timeConfig.Unit {
	case "sec":
		timeDuration = time.Duration(timeConfig.Duration) * time.Second
		waitSec = timeConfig.Duration
	case "min":
		timeDuration = time.Duration(timeConfig.Duration) * time.Minute
		waitSec = timeConfig.Duration * 60
	case "hour":
		timeDuration = time.Duration(timeConfig.Duration) * time.Hour
		waitSec = timeConfig.Duration * 3600
	case "day":
		timeDuration = time.Duration(timeConfig.Duration) * time.Hour * 24
		waitSec = timeConfig.Duration * 3600 * 24
	default:
		err = fmt.Errorf("time node param:%s unit illegal ", n.Input)
	}
	if err != nil {
		return
	}
	if recoverFlag {
		nowSubSec := time.Since(n.StartTime).Seconds()
		if nowSubSec > timeDuration.Seconds() {
			log.WorkflowLogger.Info("time job already start,now time match done", log.String("startTime", n.StartTime.Format(models.DateTimeFormat)), log.Float64("waitSec", timeDuration.Seconds()))
		} else {
			newTimeDuration := time.Duration(timeDuration.Seconds()-nowSubSec) * time.Second
			select {
			case <-time.After(newTimeDuration):
				log.WorkflowLogger.Info("time job success done", log.String("nodeId", n.Id))
			case <-n.ContinueChan:
				log.WorkflowLogger.Info("time job continue before done", log.String("nodeId", n.Id))
			}
		}
	} else {
		endDate := time.Unix(time.Now().Unix()+int64(waitSec), 0).Format(models.DateTimeFormat)
		n.Output = endDate
		if _, updateTimeOutputErr := db.WorkflowMysqlEngine.Exec("update proc_run_node set `output`=?,updated_time=? where id=?", endDate, time.Now(), n.Id); updateTimeOutputErr != nil {
			log.WorkflowLogger.Error("update time job output fail", log.String("nodeId", n.Id), log.String("endDate", endDate), log.Error(updateTimeOutputErr))
		}
		select {
		case <-time.After(timeDuration):
			log.WorkflowLogger.Info("time job success done", log.String("nodeId", n.Id))
		case <-n.ContinueChan:
			log.WorkflowLogger.Info("time job continue before done", log.String("nodeId", n.Id))
		}
	}
	return
}

func (n *WorkNode) doDateJob(recoverFlag bool) (output string, err error) {
	log.WorkflowLogger.Info("do date job", log.String("nodeId", n.Id), log.String("input", n.Input), log.Bool("recover", recoverFlag))
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
	n.Output = timeConfig.Date
	if _, updateTimeOutputErr := db.WorkflowMysqlEngine.Exec("update proc_run_node set `output`=?,updated_time=? where id=?", timeConfig.Date, time.Now(), n.Id); updateTimeOutputErr != nil {
		log.WorkflowLogger.Error("update date job output fail", log.String("nodeId", n.Id), log.String("endDate", timeConfig.Date), log.Error(updateTimeOutputErr))
	}
	timeSub := t.Unix() - time.Now().Unix()
	if timeSub < 0 {
		return
	} else {
		select {
		case <-time.After(time.Duration(timeSub) * time.Second):
			log.WorkflowLogger.Info("date job success done", log.String("nodeId", n.Id))
		case <-n.ContinueChan:
			log.WorkflowLogger.Info("date job continue before done", log.String("nodeId", n.Id))
		}
	}
	return
}

func waitSubProc(ctx context.Context, workNodeId string) {
	t := time.NewTicker(5 * time.Second).C
	for {
		<-t
		runningRows, tmpErr := database.CheckProcSubRunning(ctx, workNodeId)
		if tmpErr != nil {
			log.WorkflowLogger.Error("doSubProcessJob recover check fail", log.String("procRunNodeId", workNodeId), log.Error(tmpErr))
			continue
		}
		if len(runningRows) > 0 {
			log.WorkflowLogger.Debug("doSubProcessJob recover check continue", log.String("procRunNodeId", workNodeId), log.Int("runningSubProcessNum", len(runningRows)))
			continue
		}
		break
	}
	log.WorkflowLogger.Info("doSubProcessJob recover done", log.String("procRunNodeId", workNodeId))
}

func (n *WorkNode) doSubProcessJob(recoverFlag bool) (output string, err error) {
	log.WorkflowLogger.Info("do sub process job", log.String("nodeId", n.Id), log.String("input", n.Input))
	ctx := context.WithValue(n.Ctx, models.TransactionIdHeader, n.Id)
	if recoverFlag {
		waitSubProc(ctx, n.Id)
	} else {
		// 查proc def node定义和proc ins绑定数据
		procInsNode, procDefNode, _, dataBindings, getNodeDataErr := database.GetProcExecNodeData(ctx, n.Id)
		if getNodeDataErr != nil {
			err = getNodeDataErr
			return
		}
		if procDefNode.DynamicBind == 1 {
			dataBindings, err = database.GetDynamicBindNodeData(ctx, procInsNode.ProcInsId, procDefNode.ProcDefId, procDefNode.BindNodeId)
			if err != nil {
				err = fmt.Errorf("get node dynamic bind data fail,%s ", err.Error())
				return
			}
			if len(dataBindings) > 0 {
				err = database.UpdateDynamicNodeBindData(ctx, procInsNode.ProcInsId, procInsNode.Id, procDefNode.ProcDefId, procDefNode.Id, dataBindings)
				if err != nil {
					err = fmt.Errorf("try to update dynamic node binding data fail,%s ", err.Error())
					return
				}
			}
		} else if procDefNode.DynamicBind == 2 {
			dataBindings, err = execution.DynamicBindNodeInRuntime(ctx, procInsNode, procDefNode)
			if err != nil {
				err = fmt.Errorf("get runtime dynamic bind data fail,%s ", err.Error())
				return
			}
			if len(dataBindings) > 0 {
				err = database.UpdateDynamicNodeBindData(ctx, procInsNode.ProcInsId, procInsNode.Id, procDefNode.ProcDefId, procDefNode.Id, dataBindings)
				if err != nil {
					err = fmt.Errorf("try to update runtime dynamic node binding data fail,%s ", err.Error())
					return
				}
			}
		}
		if len(dataBindings) == 0 {
			log.WorkflowLogger.Warn("sub process job return with empty binding data", log.String("procIns", procInsNode.ProcInsId), log.String("procInsNode", procInsNode.Id))
			// 无数据，空跑
			return
		}
		// 先查询一下当前节点有无正在跑的子编排，要过滤掉这些，不能重复创建
		existSubProcResultList, subProcQueryErr := database.GetSubProcResult(ctx, n.Id)
		if subProcQueryErr != nil {
			err = fmt.Errorf("Query exists sub process running result fail,%s ", subProcQueryErr)
			return
		}
		if len(existSubProcResultList) > 0 {
			newDataBinding := []*models.ProcDataBinding{}
			for _, dataRow := range dataBindings {
				existFlag := false
				for _, row := range existSubProcResultList {
					if row.EntityDataId == dataRow.EntityDataId {
						existFlag = true
						break
					}
				}
				if !existFlag {
					newDataBinding = append(newDataBinding, dataRow)
				}
			}
			dataBindings = newDataBinding
		}
		if len(dataBindings) > 0 {
			operator := procInsNode.CreatedBy
			if procInsNode.UpdatedBy != "" {
				operator = procInsNode.UpdatedBy
			}
			var subWorkflowList []*Workflow
			var subWorkNodeList [][]*models.ProcRunNode
			var subProcWorkflowList []*models.ProcRunNodeSubProc
			for _, dataRow := range dataBindings {
				if dataRow.SubSessionId == "" {
					subPreviewResult, subPreviewErr := execution.BuildProcPreviewData(ctx, procDefNode.SubProcDefId, dataRow.EntityDataId, operator)
					if subPreviewErr != nil {
						err = fmt.Errorf("try to build sub proc preview data fail,%s ", subPreviewErr.Error())
						return
					}
					dataRow.SubSessionId = subPreviewResult.ProcessSessionId
				}
			}
			for i, dataRow := range dataBindings {
				if dataRow.SubSessionId == "" {
					continue
				}
				tmpCreateInsParam := models.ProcInsStartParam{
					ProcessSessionId:  dataRow.SubSessionId,
					EntityDataId:      dataRow.EntityDataId,
					EntityDisplayName: dataRow.EntityDataName,
					EntityTypeId:      dataRow.EntityTypeId,
					ProcDefId:         procDefNode.SubProcDefId,
					ParentInsNodeId:   procInsNode.Id,
					ParentRunNodeId:   n.Id,
				}
				log.WorkflowLogger.Debug("doSubProcessJob", log.Int("i", i), log.JsonObj("tmpCreateInsParam", tmpCreateInsParam))
				// 新增 proc_ins,proc_ins_node,proc_data_binding 纪录
				subProcInsId, subWorkflowRow, subWorkNodes, subWorkLinks, tmpCreateInsErr := database.CreateProcInstance(context.WithValue(n.Ctx, models.TransactionIdHeader, fmt.Sprintf("%s_%d", n.Id, i)), &tmpCreateInsParam, operator)
				if tmpCreateInsErr != nil {
					err = tmpCreateInsErr
					return
				}
				// 初始化workflow并开始
				workObj := Workflow{ProcRunWorkflow: *subWorkflowRow}
				workObj.ProcInsId = subProcInsId
				dataRow.SubProcInsId = subProcInsId
				workObj.Links = subWorkLinks
				subWorkflowList = append(subWorkflowList, &workObj)
				subWorkNodeList = append(subWorkNodeList, subWorkNodes)
				subProcWorkflowList = append(subProcWorkflowList, &models.ProcRunNodeSubProc{WorkflowId: subWorkflowRow.Id, EntityTypeId: dataRow.EntityTypeId, EntityDataId: dataRow.EntityDataId, ProcInsId: subProcInsId})
			}
			if err = database.UpdateProcRunNodeSubProc(ctx, n.Id, subProcWorkflowList, dataBindings, procInsNode.Id); err != nil {
				err = fmt.Errorf("UpdateProcRunNodeSubProc fail,%s ", err.Error())
				return
			}
			log.WorkflowLogger.Debug("start sub proc")
			//wg := sync.WaitGroup{}
			//for i, workObj := range subWorkflowList {
			//	wg.Add(1)
			//	go func(wo *Workflow, nl []*models.ProcRunNode) {
			//		wo.Init(context.Background(), nl, wo.Links)
			//		wo.Start(&models.ProcOperation{CreatedBy: operator})
			//		wg.Done()
			//		log.WorkflowLogger.Debug("sub proc done", log.String("subWorkflowId", wo.Id))
			//	}(workObj, subWorkNodeList[i])
			//}
			//wg.Wait()
			for i, workObj := range subWorkflowList {
				go func(wo *Workflow, nl []*models.ProcRunNode) {
					wo.Init(context.Background(), nl, wo.Links)
					wo.Start(&models.ProcOperation{CreatedBy: operator})
					log.WorkflowLogger.Debug("sub proc done", log.String("subWorkflowId", wo.Id))
				}(workObj, subWorkNodeList[i])
			}
			waitSubProc(ctx, n.Id)
			log.WorkflowLogger.Info("sub proc wait done")
		}
	}
	// 获取子编排的结果
	subProcResultList, subProcQueryErr := database.GetSubProcResult(ctx, n.Id)
	if subProcQueryErr != nil {
		err = fmt.Errorf("Query sub process running result fail,%s ", subProcQueryErr)
		return
	}
	var errorMessage string
	for _, subResult := range subProcResultList {
		if subResult.Status != models.JobStatusSuccess {
			errorMessage += fmt.Sprintf("dataId:%s:%s procInsId:%s error:%s ;", subResult.EntityTypeId, subResult.EntityDataId, subResult.ProcInsId, subResult.ErrorMessage)
		}
	}
	if errorMessage != "" {
		err = fmt.Errorf(errorMessage)
	}
	return
}

func (n *WorkNode) Callback(message string) {
	n.callbackChan <- message
}

func updateWorkflowDB(w *models.ProcRunWorkflow, op *models.ProcOperation) {
	if op == nil {
		op = &models.ProcOperation{
			CreatedBy: "system",
		}
	}
	if op.Ctx == nil {
		op.Ctx = context.Background()
	}
	nowTime := time.Now()
	var actions []*db.ExecAction
	if w.Status == models.WorkflowStatusStop {
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
	actions = append(actions, &db.ExecAction{Sql: "update proc_ins set status=?,updated_by=?,updated_time=? where id=?", Param: []interface{}{w.Status, op.CreatedBy, nowTime, w.ProcInsId}})
	actions = append(actions, &db.ExecAction{Sql: "insert into proc_run_work_record(workflow_id,host,`action`,message,created_by,created_time) values (?,?,?,?,?,?)", Param: []interface{}{w.Id, instanceHost, w.Status, op.Message, op.CreatedBy, nowTime}})
	if err := db.Transaction(actions, op.Ctx); err != nil {
		log.WorkflowLogger.Error("record workflow state fail", log.String("workflowId", w.Id), log.Error(err))
	}
}

func updateNodeDB(n *models.ProcRunNode) {
	var err error
	var actions []*db.ExecAction
	nowTime := time.Now()
	if n.Id != "" && n.ProcInsNodeId == "" {
		if runNode, getErr := getRunNodeRow(n.Id); getErr != nil {
			log.WorkflowLogger.Error("record node try to get procInsNodeId fail", log.String("nodeId", n.Id), log.Error(getErr))
		} else {
			if runNode != nil {
				n.ProcInsNodeId = runNode.ProcInsNodeId
			}
		}
	}
	if n.Status == models.JobStatusRunning {
		actions = append(actions, &db.ExecAction{Sql: "update proc_run_node set status=?,start_time=?,error_message=null,updated_time=? where id=?", Param: []interface{}{n.Status, n.StartTime, nowTime, n.Id}})
		actions = append(actions, &db.ExecAction{Sql: "update proc_ins_node set status=?,error_msg=null,updated_time=? where id=?", Param: []interface{}{n.Status, nowTime, n.ProcInsNodeId}})
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
	} else if n.Status == models.JobStatusRisky {
		actions = append(actions, &db.ExecAction{Sql: "update proc_run_node set status=?,error_message=?,end_time=?,updated_time=? where id=?", Param: []interface{}{n.Status, n.ErrorMessage, nowTime, nowTime, n.Id}})
		actions = append(actions, &db.ExecAction{Sql: "update proc_ins_node set status=?,risk_check_result=?,error_msg=?,updated_time=? where id=?", Param: []interface{}{n.Status, n.ErrorMessage, n.ErrorMessage, nowTime, n.ProcInsNodeId}})
	} else {
		actions = append(actions, &db.ExecAction{Sql: "update proc_run_node set status=?,updated_time=? where id=?", Param: []interface{}{n.Status, nowTime, n.Id}})
		actions = append(actions, &db.ExecAction{Sql: "update proc_ins_node set status=?,updated_time=? where id=?", Param: []interface{}{n.Status, nowTime, n.ProcInsNodeId}})
	}
	err = db.Transaction(actions, context.Background())
	if err != nil {
		log.WorkflowLogger.Error("record node state fail", log.String("nodeId", n.Id), log.Error(err))
	}
}

func getWorkflowRow(workflowId string) (result *models.ProcRunWorkflow, err error) {
	var workflowRows []*models.ProcRunWorkflow
	err = db.WorkflowMysqlEngine.SQL("select id,name,status,`sleep`,stop,host,last_alive_time from proc_run_workflow where id=?", workflowId).Find(&workflowRows)
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

func getRunNodeRow(procRunNodeId string) (result *models.ProcRunNode, err error) {
	var runNodeRows []*models.ProcRunNode
	err = db.WorkflowMysqlEngine.SQL("select id,workflow_id,proc_ins_node_id,name,job_type,status,`input`,`output`,error_message from proc_run_node where id=?", procRunNodeId).Find(&runNodeRows)
	if err != nil {
		err = fmt.Errorf("query proc run node table fail,%s ", err.Error())
	} else {
		if len(runNodeRows) == 0 {
			err = fmt.Errorf("can not find proc run node with id:%s ", procRunNodeId)
		} else {
			result = runNodeRows[0]
		}
	}
	return
}

func getNodeOutputData(procRunNodeId string) (output string) {
	queryRows, _ := db.WorkflowMysqlEngine.QueryString("select `output` from proc_run_node where id=?", procRunNodeId)
	if len(queryRows) > 0 {
		output = queryRows[0]["output"]
	}
	return
}

func getNodeInputData(procRunNodeId string) (input string) {
	queryRows, _ := db.WorkflowMysqlEngine.QueryString("select `input` from proc_run_node where id=?", procRunNodeId)
	if len(queryRows) > 0 {
		input = queryRows[0]["input"]
	}
	return
}

func updateNodeInputData(procRunNodeId, input string) (err error) {
	_, err = db.WorkflowMysqlEngine.Exec("update proc_run_node set `input`=?,updated_time=? where id=?", input, time.Now(), procRunNodeId)
	if err != nil {
		err = fmt.Errorf("update proc run node %s input data %s fail,%s ", procRunNodeId, input, err.Error())
	}
	return
}

func setWorkflowSleepDB(workflowId string, sleepFlag bool) (err error) {
	if sleepFlag {
		_, err = db.WorkflowMysqlEngine.Exec("update proc_run_workflow set `sleep`=1 where id=?", workflowId)
	} else {
		_, err = db.WorkflowMysqlEngine.Exec("update proc_run_workflow set `sleep`=0 where id=?", workflowId)
	}
	if err != nil {
		err = fmt.Errorf("update proc workflow:%s sleep true fail,%s ", workflowId, err.Error())
	}
	return
}
