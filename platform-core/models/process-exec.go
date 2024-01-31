package models

type ProcDefListObj struct {
	ProcDefId      string             `json:"procDefId"`
	ProcDefKey     string             `json:"procDefKey"`
	ProcDefName    string             `json:"procDefName"`
	ProcDefVersion string             `json:"procDefVersion"`
	ProcDefData    string             `json:"procDefData"`
	RootEntity     string             `json:"rootEntity"`
	Status         string             `json:"status"`
	Tags           string             `json:"tags"`
	ExcludeMode    string             `json:"excludeMode"`
	CreatedTime    string             `json:"createdTime"`
	Scene          string             `json:"scene"`
	FlowNodes      []*ProcDefFlowNode `json:"flowNodes"`
}

func (p *ProcDefListObj) Parse(input *ProcDef) {
	p.ProcDefId = input.Id
	p.ProcDefKey = input.Key
	p.ProcDefName = input.Name
	p.ProcDefVersion = input.Version
	p.RootEntity = input.RootEntity
	p.Status = input.Status
	p.Tags = input.Tags
	p.CreatedTime = input.CreatedTime.Format(DateTimeFormat)
	p.Scene = input.Scene
	p.ExcludeMode = "N"
	if input.ConflictCheck {
		p.ExcludeMode = "Y"
	}
}

type ProcDefFlowNode struct {
	NodeId            string   `json:"nodeId"`
	NodeDefId         string   `json:"nodeDefId"`
	NodeName          string   `json:"nodeName"`
	NodeType          string   `json:"nodeType"`
	ProcDefId         string   `json:"procDefId"`
	ProcDefKey        string   `json:"procDefKey"`
	RoutineExpression string   `json:"routineExpression"`
	ServiceId         string   `json:"serviceId"`
	Status            string   `json:"status"`
	Description       string   `json:"description"`
	DynamicBind       string   `json:"dynamicBind"`
	PreviousNodeIds   []string `json:"previousNodeIds"`
	SucceedingNodeIds []string `json:"succeedingNodeIds"`
	OrderedNo         string   `json:"orderedNo"`
}
