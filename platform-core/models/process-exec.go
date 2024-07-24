package models

import (
	"encoding/json"
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
	UpdatedTime    string             `json:"updatedTime"`
	CreateUser     string             `json:"createUser"`
	UpdateUser     string             `json:"updateUser"`
	Scene          string             `json:"scene"`
	FlowNodes      []*ProcDefFlowNode `json:"flowNodes"`
	NodeLinks      []*ProcDefNodeLink `json:"nodeLinks"`
	Collected      bool               `json:"collected"`
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
	p.UpdatedTime = input.UpdatedTime.Format(DateTimeFormat)
	p.CreateUser = input.CreatedBy
	p.UpdateUser = input.UpdatedBy
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
	SubProcDefId      string   `json:"subProcDefId"`
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
	LastFlag      bool                   `json:"lastFlag"`
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
	Bound               string `json:"bound"`
	EntityDataId        string `json:"entityDataId"`
	EntityTypeId        string `json:"entityTypeId"`
	NodeDefId           string `json:"nodeDefId"`
	OrderedNo           string `json:"orderedNo"`
	Id                  string `json:"id"`
	PackageName         string `json:"packageName"`
	EntityName          string `json:"entityName"`
	EntityDisplayName   string `json:"entityDisplayName"`
	NodeInstId          string `json:"nodeInstId"`
	ProcInstId          string `json:"procInstId"`
	SubPreviewSessionId string `json:"subPreviewSessionId"`
	SubProcDefId        string `json:"subProcDefId"`
}

type ProcInsStartParam struct {
	EntityDataId      string                `json:"entityDataId"`
	EntityDisplayName string                `json:"entityDisplayName"`
	EntityTypeId      string                `json:"entityTypeId"`
	ProcDefId         string                `json:"procDefId"`
	ProcessSessionId  string                `json:"processSessionId"`
	TaskNodeBinds     []*TaskNodeBindingObj `json:"taskNodeBinds"`
	ParentInsNodeId   string                `json:"parentInsNodeId"`
	ParentRunNodeId   string                `json:"parentRunNodeId"`
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
	Version           string               `json:"version"`
	NodeLinks         []*ProcDefNodeLink   `json:"nodeLinks"`
	ParentProcIns     *ParentProcInsObj    `json:"parentProcIns"`
	UpdatedBy         string               `json:"updatedBy"`
	UpdatedTime       string               `json:"updatedTime"`
	ScheduleJobName   string               `json:"scheduleJobName"`
	SubProc           bool                 `json:"subProc"`
	DisplayStatus     string               `json:"displayStatus"`
}

type ProcInsNodeDetail struct {
	Id                  string   `json:"id"`
	NodeId              string   `json:"nodeId"`
	NodeName            string   `json:"nodeName"`
	NodeDefId           string   `json:"nodeDefId"`
	NodeType            string   `json:"nodeType"`
	Description         string   `json:"description"`
	OrderedNo           string   `json:"orderedNo"`
	ProcDefId           string   `json:"procDefId"`
	ProcDefKey          string   `json:"procDefKey"`
	ProcInstId          string   `json:"procInstId"`
	ProcInstKey         string   `json:"procInstKey"`
	RoutineExpression   string   `json:"routineExpression"`
	Status              string   `json:"status"`
	PreviousNodeIds     []string `json:"previousNodeIds"`
	SucceedingNodeIds   []string `json:"succeedingNodeIds"`
	DynamicBind         int      `json:"dynamicBind"`
	DynamicBindNodeName string   `json:"dynamicBindNodeName"`
	AllowContinue       bool     `json:"allowContinue"`
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
	ProcDefNode       *ProcDefNode
	DataBinding       []*ProcDataBinding
	ProcIns           *ProcIns
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
	Operator       string                     `json:"operator,omitempty"`
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

func (r *RequestCacheEntityValue) GetAttrDataValueString() string {
	dataValue := make(map[string]interface{})
	for _, v := range r.AttrValues {
		dataValue[v.AttrName] = v.DataValue
	}
	b, _ := json.Marshal(dataValue)
	return string(b)
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
	Message    string `json:"message"`
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

type TaskMetaResult struct {
	Status  string             `json:"status"`
	Message string             `json:"message"`
	Data    TaskMetaResultData `json:"data"`
}

type TaskMetaResultData struct {
	FormMetaId    string                `json:"formMetaId"`
	FormItemMetas []*TaskMetaResultItem `json:"formItemMetas"`
}

type TaskMetaResultItem struct {
	FormItemMetaId string `json:"formItemMetaId"`
	PackageName    string `json:"packageName"`
	EntityName     string `json:"entityName"`
	AttrName       string `json:"attrName"`
}

type PluginTaskFormDto struct {
	FormMetaId       string                  `json:"formMetaId"`
	ProcDefId        string                  `json:"procDefId"`
	ProcDefKey       string                  `json:"procDefKey"`
	ProcInstId       string                  `json:"procInstId"`
	ProcInstKey      string                  `json:"procInstKey"`
	TaskNodeDefId    string                  `json:"taskNodeDefId"`
	TaskNodeInstId   string                  `json:"taskNodeInstId"`
	FormDataEntities []*PluginTaskFormEntity `json:"formDataEntities"`
}

type PluginTaskFormEntity struct {
	FormMetaId       string                 `json:"formMetaId"`
	PackageName      string                 `json:"packageName"`
	EntityName       string                 `json:"entityName"`
	Oid              string                 `json:"oid"`
	EntityDataId     string                 `json:"entityDataId"`
	FullEntityDataId string                 `json:"fullEntityDataId"`
	EntityDataState  string                 `json:"entityDataState"`
	EntityDataOp     string                 `json:"entityDataOp"`
	BindFlag         string                 `json:"bindFlag"`
	FormItemValues   []*PluginTaskFormValue `json:"formItemValues"`
}

func (p *PluginTaskFormEntity) GetAttrDataValueString(existDataValue string) string {
	dataValue := make(map[string]interface{})
	if existDataValue != "" {
		json.Unmarshal([]byte(existDataValue), &dataValue)
	}
	for _, v := range p.FormItemValues {
		dataValue[v.AttrName] = v.AttrValue
	}
	b, _ := json.Marshal(dataValue)
	return string(b)
}

type PluginTaskFormValue struct {
	FormItemMetaId   string      `json:"formItemMetaId"`
	PackageName      string      `json:"packageName"`
	EntityName       string      `json:"entityName"`
	AttrName         string      `json:"attrName"`
	Oid              string      `json:"oid"`
	EntityDataId     string      `json:"entityDataId"`
	FullEntityDataId string      `json:"fullEntityDataId"`
	AttrValue        interface{} `json:"attrValue"`
}

type PluginTaskCreateResp struct {
	ResultCode    string                 `json:"resultCode"`
	ResultMessage string                 `json:"resultMessage"`
	Results       PluginTaskCreateOutput `json:"results"`
}

type PluginTaskCreateOutput struct {
	RequestId      string                       `json:"requestId"`
	AllowedOptions []string                     `json:"allowedOptions,omitempty"`
	Outputs        []*PluginTaskCreateOutputObj `json:"outputs"`
}

type PluginTaskCreateOutputObj struct {
	CallbackParameter string `json:"callbackParameter"`
	Comment           string `json:"comment"`
	TaskFormOutput    string `json:"taskFormOutput"`
	ErrorCode         string `json:"errorCode"`
	ErrorMessage      string `json:"errorMessage"`
	ErrorDetail       string `json:"errorDetail,omitempty"`
}

type TaskCallbackReqQuery struct {
	WorkflowId  string `xorm:"workflow_id"`
	WorkNodeId  string `xorm:"work_node_id"`
	IsCompleted bool   `xorm:"is_completed"`
}

type ProcDataNodeExprObj struct {
	Expression string `json:"expression"`
	Operation  string `json:"operation"`
}

type PublicProcDefObj struct {
	ProcDefId            string      `json:"procDefId"`
	ProcDefKey           string      `json:"procDefKey"`
	ProcDefName          string      `json:"procDefName"`
	Status               string      `json:"status"`
	RootEntity           *ProcEntity `json:"rootEntity"`
	CreatedTime          string      `json:"createdTime"`
	ProcDefVersion       string      `json:"procDefVersion"`
	RootEntityExpression string      `json:"rootEntityExpression"`
}

type RewriteEntityDataObj struct {
	Oid               string
	Nid               string
	DisplayName       string
	ProcDataCacheList []*ProcDataCache
}

type QueryProcPageParam struct {
	Pageable          *PageInfo `json:"pageable"`
	Id                string    `json:"id"`
	StartTime         string    `json:"startTime"`
	EndTime           string    `json:"endTime"`
	Status            string    `json:"status"`
	ProcInstName      string    `json:"procInstName"`
	Operator          string    `json:"operator"`
	EntityDisplayName string    `json:"entityDisplayName"`
	ProcDefId         string    `json:"procDefId"`
	SubProc           string    `json:"subProc"`
}

type QueryProcPageResponse struct {
	PageInfo *PageInfo        `json:"pageInfo"`
	Contents []*ProcInsDetail `json:"contents"`
}

type ProcStartEventParam struct {
	EventSeqNo      string `json:"eventSeqNo"`
	EventType       string `json:"eventType"`
	SourceSubSystem string `json:"sourceSubSystem"`
	OperationKey    string `json:"operationKey"`
	OperationData   string `json:"operationData"`
	NotifyRequired  string `json:"notifyRequired"`
	NotifyEndpoint  string `json:"notifyEndpoint"`
	OperationUser   string `json:"operationUser"`
}

type ProcInsEvent struct {
	Id            int       `json:"id" xorm:"id"`                        // 自增id
	EventSeqNo    string    `json:"eventSeqNo" xorm:"event_seq_no"`      // 事件序列号
	EventType     string    `json:"eventType" xorm:"event_type"`         // 事件类型
	OperationData string    `json:"operationData" xorm:"operation_data"` // 根数据
	OperationKey  string    `json:"operationKey" xorm:"operation_key"`   // 编排key
	OperationUser string    `json:"operationUser" xorm:"operation_user"` // 发起者
	ProcDefId     string    `json:"procDefId" xorm:"proc_def_id"`        // 编排定义id
	ProcInsId     string    `json:"procInsId" xorm:"proc_ins_id"`        // 编排实例id
	SourcePlugin  string    `json:"sourcePlugin" xorm:"source_plugin"`   // 来源
	Status        string    `json:"status" xorm:"status"`                // 状态->created(初始化) | pending(处理中) | done(处理完成功运行编排) | fail(处理失败)
	CreatedTime   time.Time `json:"createdTime" xorm:"created_time"`     // 创建时间
	Host          string    `json:"host" xorm:"host"`                    // 处理主机
	ErrorMessage  string    `json:"errorMessage" xorm:"error_message"`   // 错误信息
}

type CoreOperationEvent struct {
	Id               int       `json:"id" xorm:"id"`
	CreatedBy        string    `json:"createdBy" xorm:"created_by"`
	CreatedTime      time.Time `json:"createdTime" xorm:"created_time"`
	UpdatedBy        string    `json:"updatedBy" xorm:"updated_by"`
	UpdatedTime      time.Time `json:"updatedTime" xorm:"updated_time"`
	EventSeqNo       string    `json:"eventSeqNo" xorm:"event_seq_no"`
	EventType        string    `json:"eventType" xorm:"event_type"`
	IsNotified       bool      `json:"isNotified" xorm:"is_notified"`
	NotifyEndpoint   string    `json:"notifyEndpoint" xorm:"notify_endpoint"`
	IsNotifyRequired bool      `json:"isNotifyRequired" xorm:"is_notify_required"`
	OperData         string    `json:"operData" xorm:"oper_data"`
	OperKey          string    `json:"operKey" xorm:"oper_key"`
	OperUser         string    `json:"operUser" xorm:"oper_user"`
	ProcDefId        string    `json:"procDefId" xorm:"proc_def_id"`
	ProcInstId       string    `json:"procInstId" xorm:"proc_inst_id"`
	SrcSubSystem     string    `json:"srcSubSystem" xorm:"src_sub_system"`
	Status           string    `json:"status" xorm:"status"`
	EndTime          time.Time `json:"endTime" xorm:"end_time"`
	Priority         int       `json:"priority" xorm:"priority"`
	ProcInstKey      string    `json:"procInstKey" xorm:"proc_inst_key"`
	StartTime        time.Time `json:"startTime" xorm:"start_time"`
	Rev              int       `json:"rev" xorm:"rev"`
	OperMode         string    `json:"operMode" xorm:"oper_mode"`
}

type ProcStartEventResultData struct {
	ProcInstId        string                      `json:"procInstId"`
	Status            string                      `json:"status"`
	TaskNodeInstances []*ProcStartEventResultData `json:"taskNodeInstances"`
}

type ProcContextSubProcRow struct {
	EntityTypeId string    `xorm:"entity_type_id"`
	EntityDataId string    `xorm:"entity_data_id"`
	ProcInsId    string    `xorm:"proc_ins_id"`
	ProcDefId    string    `xorm:"proc_def_id"`
	ProcDefName  string    `xorm:"proc_def_name"`
	CreatedTime  time.Time `xorm:"created_time"`
	Version      string    `xorm:"version"`
	Status       string    `xorm:"status"`
	ErrorMessage string    `xorm:"error_message"`
}

type SubProcDefListParam struct {
	EntityExpr string `json:"entityExpr"`
}

type ParentProcInsObj struct {
	Id          string `json:"-" xorm:"id"`
	ProcInsId   string `json:"procInsId" xorm:"proc_ins_id"`
	ProcDefName string `json:"procDefName" xorm:"proc_def_name"`
	Version     string `json:"version" xorm:"version"`
}
