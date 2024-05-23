package models

type TasknodeBindsEntityData struct {
	EntityDataId   string `json:"entityDataId" xorm:"entity_data_id"`        // 数据id
	EntityDataName string `json:"entityDisplayName" xorm:"entity_data_name"` // 数据名称
	OrderedNo      string `json:"orderedNo" xorm:"-"`
}

type Tasknode struct {
	NodeDefId   string `json:"nodeDefId" xorm:"node_id"`
	NodeId      string `json:"nodeId" xorm:"id"`
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
