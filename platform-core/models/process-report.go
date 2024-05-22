package models

type TasknodeBindsEntityData struct {
	EntityDataId   string `json:"entityDataId" xorm:"entity_data_id"`        // 数据id
	EntityDataName string `json:"entityDisplayName" xorm:"entity_data_name"` // 数据名称
	OrderedNo      string `json:"orderedNo" xorm:"-"`
}
