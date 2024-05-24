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
