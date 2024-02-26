package models

import (
	"fmt"
	"time"
)

var ProcStatusTransMap = map[string]string{"running": "InProgress", "success": "Completed", "fail": "Faulted", "problem": "Faulted", "kill": "InternallyTerminated", "ready": "NotStarted", "timeout": "Timeouted"}

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
	Id            string                 `json:"id"`
	PackageName   string                 `json:"packageName"`
	EntityName    string                 `json:"entityName"`
	EntityData    map[string]interface{} `json:"entityData"`
	DataId        string                 `json:"dataId"`
	DisplayName   string                 `json:"displayName"`
	FullDataId    string                 `json:"fullDataId"`
	PreviousIds   []string               `json:"previousIds"`
	SucceedingIds []string               `json:"succeedingIds"`
	EntityDataOp  string                 `json:"entityDataOp"`
	LastFlag      bool                   `json:"-"`
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
		for _, preId := range v.PreviousIds {
			if existPre, ok := nodePreviousMap[v.DataId]; ok {
				nodePreviousMap[v.DataId] = append(existPre, preId)
			} else {
				nodePreviousMap[v.DataId] = []string{preId}
			}
			if existSuc, ok := nodeSucceedingMap[preId]; ok {
				nodeSucceedingMap[preId] = append(existSuc, v.DataId)
			} else {
				nodeSucceedingMap[preId] = []string{v.DataId}
			}
		}
		for _, sucId := range v.SucceedingIds {
			if existSuc, ok := nodeSucceedingMap[v.DataId]; ok {
				nodeSucceedingMap[v.DataId] = append(existSuc, sucId)
			} else {
				nodeSucceedingMap[v.DataId] = []string{sucId}
			}
			if existPre, ok := nodePreviousMap[sucId]; ok {
				nodePreviousMap[sucId] = append(existPre, v.DataId)
			} else {
				nodePreviousMap[sucId] = []string{v.DataId}
			}
		}
	}
	for _, v := range p.EntityTreeNodes {
		nodePreviousMap[v.DataId] = DistinctStringList(nodePreviousMap[v.DataId], []string{v.DataId})
		nodeSucceedingMap[v.DataId] = DistinctStringList(nodeSucceedingMap[v.DataId], []string{v.DataId})
		nodeIdMap[v.DataId] = v.Id
		v.PreviousIds = []string{}
		v.SucceedingIds = []string{}
	}
	//for _, v := range p.EntityTreeNodes {
	//	for _, subFullDataId := range strings.Split(v.FullDataId, "::") {
	//		if subFullDataId == "" || subFullDataId == v.DataId {
	//			continue
	//		}
	//		if existList, ok := nodeSucceedingMap[subFullDataId]; ok {
	//			nodeSucceedingMap[subFullDataId] = append(existList, v.DataId)
	//			nodePreviousMap[v.DataId] = append(nodePreviousMap[v.DataId], subFullDataId)
	//		}
	//	}
	//}
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
	Bound             string `json:"bound"`
	EntityDataId      string `json:"entityDataId"`
	EntityTypeId      string `json:"entityTypeId"`
	NodeDefId         string `json:"nodeDefId"`
	OrderedNo         string `json:"orderedNo"`
	Id                string `json:"id"`
	PackageName       string `json:"packageName"`
	EntityName        string `json:"entityName"`
	EntityDisplayName string `json:"entityDisplayName"`
	NodeInstId        string `json:"nodeInstId"`
	ProcInstId        string `json:"procInstId"`
}

type ProcInsStartParam struct {
	EntityDataId      string                `json:"entityDataId"`
	EntityDisplayName string                `json:"entityDisplayName"`
	EntityTypeId      string                `json:"entityTypeId"`
	ProcDefId         string                `json:"procDefId"`
	ProcessSessionId  string                `json:"processSessionId"`
	TaskNodeBinds     []*TaskNodeBindingObj `json:"taskNodeBinds"`
}

type ProcInsDetail struct {
	Id                string               `json:"id"`
	ProcDefId         string               `json:"procDefId"`
	ProcDefKey        string               `json:"procDefKey"`
	ProcInstKey       string               `json:"procInstKey"`
	ProcInstName      string               `json:"procInstName"`
	EntityDataId      string               `json:"entityDataId"`
	EntityTypeId      string               `json:"entityTypeId"`
	EntityDisplayName string               `json:"entityDisplayName"`
	Status            string               `json:"status"`
	Operator          string               `json:"operator"`
	CreatedTime       string               `json:"createdTime"`
	TaskNodeInstances []*ProcInsNodeDetail `json:"taskNodeInstances"`
}

type ProcInsNodeDetail struct {
	Id                string   `json:"id"`
	NodeId            string   `json:"nodeId"`
	NodeName          string   `json:"nodeName"`
	NodeDefId         string   `json:"nodeDefId"`
	NodeType          string   `json:"nodeType"`
	Description       string   `json:"description"`
	OrderedNo         string   `json:"orderedNo"`
	ProcDefId         string   `json:"procDefId"`
	ProcDefKey        string   `json:"procDefKey"`
	ProcInstId        string   `json:"procInstId"`
	ProcInstKey       string   `json:"procInstKey"`
	RoutineExpression string   `json:"routineExpression"`
	Status            string   `json:"status"`
	PreviousNodeIds   []string `json:"previousNodeIds"`
	SucceedingNodeIds []string `json:"succeedingNodeIds"`
}

type ProcCallPluginServiceFuncParam struct {
	PluginInterface   *PluginConfigInterfaces
	EntityType        string
	EntityInstances   []*BatchExecutionPluginExecEntityInstances
	InputConstantMap  map[string]string
	InputParamContext map[string]interface{}
	ContinueToken     string
	DueDate           string
	AllowedOptions    []string
	RiskCheck         bool
	Operator          string
	ProcInsNode       *ProcInsNode
}

type ProcNodeContextReq struct {
	BeginTime      string                     `json:"beginTime"`
	EndTime        string                     `json:"endTime"`
	NodeDefId      string                     `json:"nodeDefId"`
	NodeExpression string                     `json:"nodeExpression"`
	NodeId         string                     `json:"nodeId"`
	NodeInstId     string                     `json:"nodeInstId"`
	NodeName       string                     `json:"nodeName"`
	NodeType       string                     `json:"nodeType"`
	PluginInfo     string                     `json:"pluginInfo"`
	RequestId      string                     `json:"requestId"`
	ErrorMessage   string                     `json:"errorMessage,omitempty"`
	RequestObjects []ProcNodeContextReqObject `json:"requestObjects"`
}

type ProcNodeContextReqObject struct {
	CallbackParameter string                   `json:"callbackParameter"`
	Inputs            []map[string]interface{} `json:"inputs"`
	Outputs           []map[string]interface{} `json:"outputs"`
}

type ProcNodeContextQueryObj struct {
	Id                string    `json:"id" xorm:"id"`
	Name              string    `json:"name" xorm:"name"`
	ProcDefNodeId     string    `json:"procDefNodeId" xorm:"proc_def_node_id"`
	ErrorMsg          string    `json:"errorMsg" xorm:"error_msg"`
	RoutineExpression string    `json:"routineExpression" xorm:"routine_expression"`
	ServiceName       string    `json:"serviceName" xorm:"service_name"`
	StartTime         time.Time `json:"startTime" xorm:"start_time"`
	EndTime           time.Time `json:"endTime" xorm:"end_time"`
	ReqId             string    `json:"reqId" xorm:"req_id"`
	NodeType          string    `json:"nodeType" xorm:"node_type"`
}

type RequestProcessData struct {
	ProcDefId     string                           `json:"procDefId"`
	ProcDefKey    string                           `json:"procDefKey"`
	RootEntityOid string                           `json:"rootEntityOid"`
	Entities      []*RequestCacheEntityValue       `json:"entities"`
	Bindings      []*RequestProcessTaskNodeBindObj `json:"bindings"`
}

type RequestProcessTaskNodeBindObj struct {
	NodeId       string `json:"nodeId"`
	NodeDefId    string `json:"nodeDefId"`
	Oid          string `json:"oid"`
	EntityDataId string `json:"entityDataId"`
	BindFlag     string `json:"bindFlag"`
}

type RequestCacheEntityValue struct {
	AttrValues        []*RequestCacheEntityAttrValue `json:"attrValues"`
	BindFlag          string                         `json:"bindFlag"`
	EntityDataId      string                         `json:"entityDataId"`
	EntityDataOp      string                         `json:"entityDataOp"`
	EntityDataState   string                         `json:"entityDataState"`
	EntityDefId       string                         `json:"entityDefId"`
	EntityName        string                         `json:"entityName"`
	EntityDisplayName string                         `json:"entityDisplayName"`
	FullEntityDataId  interface{}                    `json:"fullEntityDataId"`
	Oid               string                         `json:"oid"`
	PackageName       string                         `json:"packageName"`
	PreviousOids      []string                       `json:"previousOids"`
	Processed         bool                           `json:"processed"`
	SucceedingOids    []string                       `json:"succeedingOids"`
}

type RequestCacheEntityAttrValue struct {
	DataOid   string      `json:"-"`
	AttrDefId string      `json:"attrDefId"`
	AttrName  string      `json:"attrName"`
	DataType  string      `json:"dataType"`
	DataValue interface{} `json:"dataValue"`
}

type StartInstanceResultData struct {
	Id          int    `json:"id"`
	ProcInstKey string `json:"procInstKey"`
	ProcDefId   string `json:"procDefId"`
	ProcDefKey  string `json:"procDefKey"`
	Status      string `json:"status"`
}

type ProcInsOperationParam struct {
	Act        string `json:"act"`
	ProcInstId string `json:"procInstId"`
	NodeInstId string `json:"nodeInstId"`
}

func DistinctStringList(input, excludeList []string) (output []string) {
	if len(input) == 0 {
		return
	}
	existMap := make(map[string]int)
	for _, v := range excludeList {
		existMap[v] = 1
	}
	for _, v := range input {
		if _, ok := existMap[v]; !ok {
			output = append(output, v)
			existMap[v] = 1
		}
	}
	return
}

type ProcEntityDataQueryParam struct {
	AdditionalFilters []*EntityQueryObj `json:"additionalFilters"`
	ProcInstId        string            `json:"procInstId"`
}
