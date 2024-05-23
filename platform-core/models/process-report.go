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
