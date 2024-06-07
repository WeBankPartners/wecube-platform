package models

import "fmt"

type TasknodeBindsEntityData struct {
	EntityDataId   string `json:"entityDataId" xorm:"entity_data_id"`        // 数据id
	EntityDataName string `json:"entityDisplayName" xorm:"entity_data_name"` // 数据名称
	OrderedNo      string `json:"orderedNo" xorm:"-"`
}

type Tasknode struct {
	NodeDefId   string `json:"nodeDefId" xorm:"id"`
	NodeId      string `json:"nodeId" xorm:"node_id"`
	NodeName    string `json:"nodeName" xorm:"name"`
	NodeType    string `json:"nodeType" xorm:"node_type"`
	ProcDefId   string `json:"procDefId" xorm:"proc_def_id"`
	ServiceId   string `json:"serviceId" xorm:"service_name"`
	ServiceName string `json:"serviceName" xorm:"service_name"`
}

type StatisticsProcessExecReq struct {
	StartDate  string    `json:"startDate"`
	EndDate    string    `json:"endDate"`
	Pageable   *PageInfo `json:"pageable"`
	ProcDefIds []string  `json:"procDefIds"`
}

type StatisticsProcessExecResp struct {
	ProcDefId                string `json:"procDefId"`
	ProcDefName              string `json:"procDefName"`
	TotalCompletedInstances  int    `json:"totalCompletedInstances"`
	TotalFaultedInstances    int    `json:"totalFaultedInstances"`
	TotalInProgressInstances int    `json:"totalInProgressInstances"`
	TotalInstances           int    `json:"totalInstances"`
}

type StatisticsProcExecCnt struct {
	ProcDefId string `json:"procDefId" xorm:"proc_def_id"`
	Cnt       int    `json:"cnt" xorm:"cnt"`
	Status    string `json:"status" xorm:"status"`
}

type StatisticsTasknodeExecReq struct {
	StartDate     string               `json:"startDate"`
	EndDate       string               `json:"endDate"`
	Pageable      *PageInfo            `json:"pageable"`
	ProcDefIds    []string             `json:"procDefIds"`
	EntityDataIds []string             `json:"entityDataIds"`
	TaskNodeIds   []string             `json:"taskNodeIds"`
	ServiceIds    []string             `json:"serviceIds"`
	Sorting       *QueryRequestSorting `json:"sorting"`
}

type StatisticsTasknodeExecResp struct {
	PageInfo *PageInfo                       `json:"pageInfo"`
	Contents []*StatisticsTasknodeExecResult `json:"contents"`
}

type StatisticsTasknodeExecResult struct {
	EntityDataId   string `json:"entityDataId"`
	EntityDataName string `json:"entityDataName"`
	NodeDefId      string `json:"nodeDefId"`
	NodeDefName    string `json:"nodeDefName"`
	ProcDefId      string `json:"procDefId"`
	ProcDefName    string `json:"procDefName"` // 最终返回结果: procDefName + procDefVersion
	ProcDefVersion string `json:"procDefVersion"`
	ServiceId      string `json:"serviceId"`
	FailureCount   int    `json:"failureCount"`
	SuccessCount   int    `json:"successCount"`
}

type StatisticsTasknodeExecQueryResult struct {
	EntityDataId   string `json:"entityDataId" xorm:"entity_data_id"`
	EntityDataName string `json:"entityDataName" xorm:"entity_data_name"`
	NodeDefId      string `json:"nodeDefId" xorm:"proc_def_node_id"`
	NodeDefName    string `json:"nodeDefName" xorm:"proc_def_node_name"`
	ProcDefId      string `json:"procDefId" xorm:"proc_def_id"`
	ProcDefName    string `json:"procDefName" xorm:"proc_def_name"`
	ProcDefVersion string `json:"procDefVersion" xorm:"proc_def_version"`
	DataValue      string `json:"dataValue" xorm:"data_value"`
	Cnt            int    `json:"cnt" xorm:"cnt"`
}

func (t *StatisticsTasknodeExecQueryResult) StringValForHash() string {
	return fmt.Sprintf("%s%s%s%s%s%s%s",
		t.EntityDataId, t.EntityDataName, t.NodeDefId, t.NodeDefName,
		t.ProcDefId, t.ProcDefName, t.ProcDefVersion)
}

type StatisticsPluginExecQueryResult struct {
	ServiceName    string `json:"serviceName" xorm:"service_name"`
	EntityDataId   string `json:"entityDataId" xorm:"entity_data_id"`
	EntityDataName string `json:"entityDataName" xorm:"entity_data_name"`
	DataValue      string `json:"dataValue" xorm:"data_value"`
	Cnt            int    `json:"cnt" xorm:"cnt"`
}

func (t *StatisticsPluginExecQueryResult) StringValForHash() string {
	return fmt.Sprintf("%s%s%s",
		t.ServiceName, t.EntityDataId, t.EntityDataName)
}

type StatisticsTasknodeExecDetailsReq struct {
	StartDate      string `json:"startDate"`
	EndDate        string `json:"endDate"`
	ProcDefId      string `json:"procDefId"`
	EntityDataId   string `json:"entityDataId"`
	EntityDataName string `json:"entityDataName"`
	NodeDefId      string `json:"nodeDefId"`
	ServiceId      string `json:"serviceId"`
	Status         string `json:"status"` // Faulted, Completed
}

type StatisticsTasknodeExecDetailsResp struct {
	EntityDataId string               `json:"entityDataId"`
	ExecDate     string               `json:"execDate"`
	ExecParams   []*TasknodeExecParam `json:"execParams"`
	NodeDefId    string               `json:"nodeDefId"`
	NodeDefName  string               `json:"nodeDefName"`
	NodeExecDate string               `json:"nodeExecDate"`
	NodeStatus   string               `json:"nodeStatus"`
	ProcDefId    string               `json:"procDefId"`
	ProcDefName  string               `json:"procDefName"`
	ProcExecDate string               `json:"procExecDate"`
	ProcExecOper string               `json:"procExecOper"`
	ProcStatus   string               `json:"procStatus"`
	ReqId        string               `json:"reqId"`
	ServiceId    string               `json:"serviceId"`
}

type TasknodeExecParam struct {
	EntityDataId   string `json:"entityDataId" xorm:"callback_id"`
	EntityTypeId   string `json:"entityTypeId" xorm:"entity_type_id"`
	Id             string `json:"id" xorm:"id"`
	ObjectId       string `json:"objectId" xorm:"-"` // "0"
	ParamDataType  string `json:"paramDataType" xorm:"data_type"`
	ParamDataValue string `json:"paramDataValue" xorm:"data_value"`
	ParamName      string `json:"paramName" xorm:"name"`
	ParamType      string `json:"paramType" xorm:"from_type"`
	RequestId      string `json:"requestId" xorm:"req_id"`
}

type StatisticsTasknodeExecDetailsQueryResult struct {
	ProcDefId      string `json:"procDefId" xorm:"proc_def_id"`
	ProcDefName    string `json:"procDefName" xorm:"proc_def_name"`
	ProcDefVersion string `json:"procDefVersion" xorm:"proc_def_version"`
	ProcExecDate   string `json:"procExecDate" xorm:"proc_exec_date"`
	ProcExecOper   string `json:"procExecOper" xorm:"proc_exec_oper"`
	ProcStatus     string `json:"procStatus" xorm:"proc_exec_status"`
	NodeDefId      string `json:"nodeDefId" xorm:"proc_def_node_id"`
	NodeDefName    string `json:"nodeDefName" xorm:"proc_def_node_name"`
	NodeExecDate   string `json:"nodeExecDate" xorm:"proc_node_exec_date"`
	NodeStatus     string `json:"nodeStatus" xorm:"proc_node_status"`
	EntityDataId   string `json:"entityDataId" xorm:"entity_data_id"`
	ExecDate       string `json:"execDate" xorm:"exec_date"`
	ReqId          string `json:"reqId" xorm:"req_id"`
	ServiceId      string `json:"serviceId" xorm:"service_id"`

	// execParams
	Id             string `json:"id" xorm:"pinrp_id"`
	RequestId      string `json:"requestId" xorm:"pinrp_req_id"`
	ParamType      string `json:"paramType" xorm:"pinrp_param_type"`
	ParamName      string `json:"paramName" xorm:"pinrp_name"`
	ParamDataType  string `json:"paramDataType" xorm:"pinrp_data_type"`
	ParamDataValue string `json:"paramDataValue" xorm:"pinrp_data_value"`
	CallbackId     string `json:"callbackId" xorm:"pinrp_callback_id"`
	EntityTypeId   string `json:"entityTypeId" xorm:"pinrp_entity_type_id"`
	ObjectId       string `json:"objectId"`
}
