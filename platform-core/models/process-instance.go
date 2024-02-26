package models

import "time"

type ProcDataPreview struct {
	Id             int       `json:"id" xorm:"id"`                           // 自增id
	ProcDefId      string    `json:"procDefId" xorm:"proc_def_id"`           // 编排定义id
	ProcSessionId  string    `json:"procSessionId" xorm:"proc_session_id"`   // 试算任务id
	ProcDefNodeId  string    `json:"procDefNodeId" xorm:"proc_def_node_id"`  // 编排节点id
	EntityDataId   string    `json:"entityDataId" xorm:"entity_data_id"`     // 数据id
	EntityDataName string    `json:"entityDataName" xorm:"entity_data_name"` // 数据名称
	EntityTypeId   string    `json:"entityTypeId" xorm:"entity_type_id"`     // 数据entity
	OrderedNo      string    `json:"orderedNo" xorm:"ordered_no"`            // 节点排序
	BindType       string    `json:"bindType" xorm:"bind_type"`              // 编排(tasknode)还是节点(process)
	FullDataId     string    `json:"fullDataId" xorm:"full_data_id"`         // 数据全路径
	IsBound        bool      `json:"isBound" xorm:"is_bound"`                // 是否绑定
	CreatedBy      string    `json:"createdBy" xorm:"created_by"`            // 创建人
	CreatedTime    time.Time `json:"createdTime" xorm:"created_time"`        // 创建时间
	UpdatedBy      string    `json:"updatedBy" xorm:"updated_by"`            // 更新人
	UpdatedTime    time.Time `json:"updatedTime" xorm:"updated_time"`        // 更新时间
}

type ProcInsGraphNode struct {
	Id            int       `json:"id" xorm:"id"`                         // 自增id
	ProcSessionId string    `json:"procSessionId" xorm:"proc_session_id"` // 试算任务id
	ProcInsId     string    `json:"procInsId" xorm:"proc_ins_id"`         // 编排实例id
	DataId        string    `json:"dataId" xorm:"data_id"`                // 数据id
	DisplayName   string    `json:"displayName" xorm:"display_name"`      // 数据显示名
	EntityName    string    `json:"entityName" xorm:"entity_name"`        // entity显示名
	GraphNodeId   string    `json:"graphNodeId" xorm:"graph_node_id"`     // 图形节点id
	PkgName       string    `json:"pkgName" xorm:"pkg_name"`              // 数据所属包
	PrevIds       string    `json:"prevIds" xorm:"prev_ids"`              // 上游图形节点id列表
	SuccIds       string    `json:"succIds" xorm:"succ_ids"`              // 下游图形节点id列表
	FullDataId    string    `json:"fullDataId" xorm:"full_data_id"`       // 数据全路径
	CreatedBy     string    `json:"createdBy" xorm:"created_by"`          // 创建人
	CreatedTime   time.Time `json:"createdTime" xorm:"created_time"`      // 创建时间
	UpdatedBy     string    `json:"updatedBy" xorm:"updated_by"`          // 更新人
	UpdatedTime   time.Time `json:"updatedTime" xorm:"updated_time"`      // 更新时间
}

type ProcDataBinding struct {
	Id             string    `json:"id" xorm:"id"`                           // 唯一标识
	ProcDefId      string    `json:"procDefId" xorm:"proc_def_id"`           // 编排定义id
	ProcInsId      string    `json:"procInsId" xorm:"proc_ins_id"`           // 编排实例id
	ProcDefNodeId  string    `json:"procDefNodeId" xorm:"proc_def_node_id"`  // 编排节点id
	ProcInsNodeId  string    `json:"procInsNodeId" xorm:"proc_ins_node_id"`  // 编排实例节点id
	EntityId       string    `json:"entityId" xorm:"entity_id"`              // 编排数据id
	EntityDataId   string    `json:"entityDataId" xorm:"entity_data_id"`     // 数据id
	EntityDataName string    `json:"entityDataName" xorm:"entity_data_name"` // 数据名称
	EntityTypeId   string    `json:"entityTypeId" xorm:"entity_type_id"`     // 数据entity
	BindFlag       bool      `json:"bindFlag" xorm:"bind_flag"`              // 是否绑定
	BindType       string    `json:"bindType" xorm:"bind_type"`              // 编排(tasknode)还是节点(process)
	FullDataId     string    `json:"fullDataId" xorm:"full_data_id"`         // 数据全路径
	CreatedBy      string    `json:"createdBy" xorm:"created_by"`            // 创建人
	CreatedTime    time.Time `json:"createdTime" xorm:"created_time"`        // 创建时间
	UpdatedBy      string    `json:"updatedBy" xorm:"updated_by"`            // 更新人
	UpdatedTime    time.Time `json:"updatedTime" xorm:"updated_time"`        // 更新时间
}

type ProcIns struct {
	Id             string    `json:"id" xorm:"id"`                           // 唯一标识
	ProcDefId      string    `json:"procDefId" xorm:"proc_def_id"`           // 编排定义id
	ProcDefKey     string    `json:"procDefKey" xorm:"proc_def_key"`         // 编排定义key
	ProcDefName    string    `json:"procDefName" xorm:"proc_def_name"`       // 编排定义名称
	Status         string    `json:"status" xorm:"status"`                   // 状态->ready(初始化
	EntityDataId   string    `json:"entityDataId" xorm:"entity_data_id"`     // 根数据id
	EntityTypeId   string    `json:"entityTypeId" xorm:"entity_type_id"`     // 根数据类型
	EntityDataName string    `json:"entityDataName" xorm:"entity_data_name"` // 根数据名称
	ProcSessionId  string    `json:"procSessionId" xorm:"proc_session_id"`   // 试算session
	CreatedBy      string    `json:"createdBy" xorm:"created_by"`            // 创建人
	CreatedTime    time.Time `json:"createdTime" xorm:"created_time"`        // 创建时间
	UpdatedBy      string    `json:"updatedBy" xorm:"updated_by"`            // 更新人
	UpdatedTime    time.Time `json:"updatedTime" xorm:"updated_time"`        // 更新时间
}

type ProcInsNode struct {
	Id              string    `json:"id" xorm:"id"`                             // 唯一标识
	ProcInsId       string    `json:"procInsId" xorm:"proc_ins_id"`             // 编排实例id
	ProcDefNodeId   string    `json:"procDefNodeId" xorm:"proc_def_node_id"`    // 编排节点定义id
	Name            string    `json:"name" xorm:"name"`                         // 编排定义名称
	NodeType        string    `json:"nodeType" xorm:"node_type"`                // 任务类型->start(开始
	Status          string    `json:"status" xorm:"status"`                     // 状态->ready(初始化
	RiskCheckResult string    `json:"riskCheckResult" xorm:"risk_check_result"` // 高危检测结果
	ErrorMsg        string    `json:"errorMsg" xorm:"error_msg"`                // 报错信息
	OrderedNo       int       `json:"orderedNo" xorm:"ordered_no"`              // 节点排序
	CreatedBy       string    `json:"createdBy" xorm:"created_by"`              // 创建人
	CreatedTime     time.Time `json:"createdTime" xorm:"created_time"`          // 创建时间
	UpdatedBy       string    `json:"updatedBy" xorm:"updated_by"`              // 更新人
	UpdatedTime     time.Time `json:"updatedTime" xorm:"updated_time"`          // 更新时间
}

type ProcInsNodeReq struct {
	Id              string                 `json:"id" xorm:"id"`                             // 唯一标识
	ProcInsNodeId   string                 `json:"procInsNodeId" xorm:"proc_ins_node_id"`    // 编排实例节点id
	ReqUrl          string                 `json:"reqUrl" xorm:"req_url"`                    // 请求url
	IsCompleted     bool                   `json:"isCompleted" xorm:"is_completed"`          // 是否完成
	ErrorCode       string                 `json:"errorCode" xorm:"error_code"`              // 错误码
	ErrorMsg        string                 `json:"errorMsg" xorm:"error_msg"`                // 错误信息
	WithContextData bool                   `json:"withContextData" xorm:"with_context_data"` // 是否有上下文数据
	ReqDataAmount   int                    `json:"reqDataAmount" xorm:"req_data_amount"`     // 有多少组数据
	CreatedTime     time.Time              `json:"createdTime" xorm:"created_time"`          // 创建时间
	UpdatedTime     time.Time              `json:"updatedTime" xorm:"updated_time"`          // 更新时间
	Params          []*ProcInsNodeReqParam `json:"params" xorm:"-"`
}

type ProcInsNodeReqParam struct {
	Id           int       `json:"id" xorm:"id"`                       // 自增id
	ReqId        string    `json:"reqId" xorm:"req_id"`                // 请求id
	DataIndex    int       `json:"dataIndex" xorm:"data_index"`        // 第几组数据
	FromType     string    `json:"fromType" xorm:"from_type"`          // inpu
	Name         string    `json:"name" xorm:"name"`                   // 参数名
	DataType     string    `json:"dataType" xorm:"data_type"`          // 参数数据类型
	DataValue    string    `json:"dataValue" xorm:"data_value"`        // 参数数据值
	EntityDataId string    `json:"entityDataId" xorm:"entity_data_id"` // 数据id
	EntityTypeId string    `json:"entityTypeId" xorm:"entity_type_id"` // 数据entity
	IsSensitive  bool      `json:"isSensitive" xorm:"is_sensitive"`    // 是否敏感
	FullDataId   string    `json:"fullDataId" xorm:"full_data_id"`     // 数据全路径
	Multiple     bool      `json:"multiple" xorm:"multiple"`           // 是否数组
	ParamDefId   string    `json:"paramDefId" xorm:"param_def_id"`     // 插件服务参数id
	MappingType  string    `json:"mappingType" xorm:"mapping_type"`    // 数据来源
	CallbackId   string    `json:"callbackId" xorm:"callback_id"`      // 回调id
	CreatedTime  time.Time `json:"createdTime" xorm:"created_time"`    // 创建时间
}

type ProcDataCache struct {
	Id             string    `json:"id" xorm:"id"`                           // 唯一标识
	ProcInsId      string    `json:"procInsId" xorm:"proc_ins_id"`           // 编排实例id
	EntityId       string    `json:"entityId" xorm:"entity_id"`              // 编排数据id
	EntityDataId   string    `json:"entityDataId" xorm:"entity_data_id"`     // 数据id
	EntityDataName string    `json:"entityDataName" xorm:"entity_data_name"` // 数据名称
	EntityTypeId   string    `json:"entityTypeId" xorm:"entity_type_id"`     // 数据entity
	FullDataId     string    `json:"fullDataId" xorm:"full_data_id"`         // 数据全路径
	DataValue      string    `json:"dataValue" xorm:"data_value"`            // 数据值
	CreatedTime    time.Time `json:"createdTime" xorm:"created_time"`        // 创建时间
	UpdatedTime    time.Time `json:"updatedTime" xorm:"updated_time"`        // 更新时间
}
