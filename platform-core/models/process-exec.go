package models

import (
	"fmt"
	"strings"
)

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

type ProcPreviewEntityNode struct {
	Id            string   `json:"id"`
	PackageName   string   `json:"packageName"`
	EntityName    string   `json:"entityName"`
	EntityData    string   `json:"entityData"`
	DataId        string   `json:"dataId"`
	DisplayName   string   `json:"displayName"`
	FullDataId    string   `json:"fullDataId"`
	PreviousIds   []string `json:"previousIds"`
	SucceedingIds []string `json:"succeedingIds"`
}

type ProcPreviewData struct {
	ProcessSessionId string                   `json:"processSessionId"`
	EntityTreeNodes  []*ProcPreviewEntityNode `json:"entityTreeNodes"`
}

func (p *ProcPreviewEntityNode) Parse(packageName, entityName string, input map[string]interface{}) {
	p.PackageName = packageName
	p.EntityName = entityName
	if v, b := input["id"]; b {
		p.DataId = v.(string)
	}
	if v, b := input["displayName"]; b {
		p.DisplayName = v.(string)
	}
	p.Id = fmt.Sprintf("%s:%s:%s", p.PackageName, p.EntityName, p.DataId)
	p.PreviousIds, p.SucceedingIds = []string{}, []string{}
}

func (p *ProcPreviewData) AnalyzeRefIds() {
	if len(p.EntityTreeNodes) <= 1 {
		return
	}
	nodePreviousMap := make(map[string][]string)
	nodeSucceedingMap := make(map[string][]string)
	nodeIdMap := make(map[string]string)
	for _, v := range p.EntityTreeNodes {
		nodePreviousMap[v.DataId] = []string{}
		nodeSucceedingMap[v.DataId] = []string{}
		nodeIdMap[v.DataId] = v.Id
	}
	for _, v := range p.EntityTreeNodes {
		for _, subFullDataId := range strings.Split(v.FullDataId, "::") {
			if subFullDataId == "" || subFullDataId == v.DataId {
				continue
			}
			if existList, ok := nodeSucceedingMap[subFullDataId]; ok {
				nodeSucceedingMap[subFullDataId] = append(existList, v.DataId)
				nodePreviousMap[v.DataId] = append(nodePreviousMap[v.DataId], subFullDataId)
			}
		}
	}
	for _, v := range p.EntityTreeNodes {
		for _, sucId := range nodeSucceedingMap[v.DataId] {
			v.SucceedingIds = append(v.SucceedingIds, nodeIdMap[sucId])
		}
		for _, preId := range nodePreviousMap[v.DataId] {
			v.PreviousIds = append(v.PreviousIds, nodeIdMap[preId])
		}
	}
}

type TaskNodeBindingObj struct {
	Bound        string `json:"bound"`
	EntityDataId string `json:"entityDataId"`
	EntityTypeId string `json:"entityTypeId"`
	NodeDefId    string `json:"nodeDefId"`
	OrderedNo    string `json:"orderedNo"`
}
