package models

import (
	"encoding/json"
	"strings"
	"time"
)

type ProcDefStatus string //定义编排定义状态
type PermissionType string

const (
	Draft    ProcDefStatus  = "draft"    //草稿
	Deployed ProcDefStatus  = "deployed" //部署状态
	Disabled ProcDefStatus  = "disabled" //禁用
	Deleted  ProcDefStatus  = "deleted"  //删除
	MGMT     PermissionType = "MGMT"     //管理权限
	USE      PermissionType = "USE"      //使用权限
)

type ProcDef struct {
	Id            string    `json:"id" xorm:"id"`                        // 唯一标识
	Key           string    `json:"key" xorm:"key"`                      // 编排key
	Name          string    `json:"name" xorm:"name"`                    // 编排名称
	Version       string    `json:"version" xorm:"version"`              // 版本
	RootEntity    string    `json:"rootEntity" xorm:"root_entity"`       // 根节点
	Status        string    `json:"status" xorm:"status"`                // 状态
	Tags          string    `json:"tags" xorm:"tags"`                    // 标签
	ForPlugin     string    `json:"forPlugin" xorm:"for_plugin"`         // 授权插件
	Scene         string    `json:"scene" xorm:"scene"`                  // 使用场景
	ConflictCheck bool      `json:"conflictCheck" xorm:"conflict_check"` // 冲突检测
	CreatedBy     string    `json:"createdBy" xorm:"created_by"`         // 创建人
	CreatedTime   time.Time `json:"createdTime" xorm:"created_time"`     // 创建时间
	UpdatedBy     string    `json:"updatedBy" xorm:"updated_by"`         // 更新人
	UpdatedTime   time.Time `json:"updatedTime" xorm:"updated_time"`     // 更新时间
}

type ProcDefNode struct {
	Id                string    `json:"id" xorm:"id"`                                 // 唯一标识
	NodeId            string    `json:"nodeId" xorm:"node_id"`                        // 前端nodeID
	ProcDefId         string    `json:"procDefId" xorm:"proc_def_id"`                 // 编排id
	Name              string    `json:"name" xorm:"name"`                             // 节点名称
	Description       string    `json:"description" xorm:"description"`               // 节点描述
	Status            string    `json:"status" xorm:"status"`                         // 状态
	NodeType          string    `json:"nodeType" xorm:"node_type"`                    // 节点类型
	ServiceName       string    `json:"serviceName" xorm:"service_name"`              // 插件服务名
	DynamicBind       bool      `json:"dynamicBind" xorm:"dynamic_bind"`              // 是否动态绑定
	BindNodeId        string    `json:"bindNodeId" xorm:"bind_node_id"`               // 动态绑定节点
	RiskCheck         bool      `json:"riskCheck" xorm:"risk_check"`                  // 是否高危检测
	RoutineExpression string    `json:"routineExpression" xorm:"routine_expression"`  // 定位规则
	ContextParamNodes string    `json:"contextParamNodes" xorm:"context_param_nodes"` // 上下文参数节点
	Timeout           int       `json:"timeout" xorm:"timeout"`                       // 超时时间分钟
	TimeConfig        string    `json:"timeConfig" xorm:"time_config"`                // 节点配置
	OrderedNo         int       `json:"orderedNo" xorm:"ordered_no"`                  // 节点顺序
	UiStyle           string    `json:"uiStyle" xorm:"ui_style"`                      // 前端样式
	CreatedBy         string    `json:"createdBy" xorm:"created_by"`                  // 创建人
	CreatedTime       time.Time `json:"createdTime" xorm:"created_time"`              // 创建时间
	UpdatedBy         string    `json:"updatedBy" xorm:"updated_by"`                  // 更新人
	UpdatedTime       time.Time `json:"updatedTime" xorm:"updated_time"`              // 更新时间
}

type ProcDefNodeParam struct {
	Id            string `json:"id" xorm:"id"`                          // 唯一标识
	ProcDefNodeId string `json:"procDefNodeId" xorm:"proc_def_node_id"` // 编排节点id
	ParamId       string `json:"paramId" xorm:"param_id"`               // 编排节点参数id
	Name          string `json:"name" xorm:"name"`                      // 参数名
	BindType      string `json:"bindType" xorm:"bind_type"`             // 参数类型->context(上下文) | constant(静态值)
	Value         string `json:"value" xorm:"value"`                    // 参数值
	CtxBindNode   string `json:"ctxBindNode" xorm:"ctx_bind_node"`      // 上下文节点
	CtxBindType   string `json:"ctxBindType" xorm:"ctx_bind_type"`      // 上下文出入参->input(入参) | output(出参)
	CtxBindName   string `json:"ctxBindName" xorm:"ctx_bind_name"`      // 上下文参数名
}

type ProcDefNodeLink struct {
	Id      string `json:"id" xorm:"id"`            // 唯一标识(source__target)
	LinkId  string `json:"LinkId" xorm:"link_id"`   // 前端线id
	Source  string `json:"source" xorm:"source"`    // 源节点
	Target  string `json:"target" xorm:"target"`    // 目标节点
	Name    string `json:"name" xorm:"name"`        // 连接名称
	UiStyle string `json:"uiStyle" xorm:"ui_style"` // 前端样式
}

type ProcDefPermission struct {
	Id         string `json:"id" xorm:"id"`                 // 唯一标识
	ProcDefId  string `json:"procDefId" xorm:"proc_def_id"` // 编排id
	RoleId     string `json:"roleId" xorm:"role_id"`        // 角色id
	RoleName   string `json:"roleName" xorm:"role_name"`    // 角色名称
	Permission string `json:"permission" xorm:"permission"` // 权限->MGMT(管理) | USE(使用)
}

type ProcDefCollect struct {
	Id          string    `json:"id" xorm:"id"`                    // 唯一标识
	ProcDefId   string    `json:"procDefId" xorm:"proc_def_id"`    // 编排id
	RoleId      string    `json:"roleId" xorm:"role_id"`           // 角色id
	UserId      string    `json:"userId" xorm:"user_id"`           // 用户id
	CreatedTime time.Time `json:"createdTime" xorm:"created_time"` // 创建时间
	UpdatedTime time.Time `json:"updatedTime" xorm:"updated_time"` // 更新时间
}

// ProcessDefinitionParam 添加编排参数
type ProcessDefinitionParam struct {
	Id               string           `json:"id"`               // 唯一标识
	Key              string           `json:"Key"`              // key
	Name             string           `json:"name"`             // 编排名称
	Version          string           `json:"version"`          // 编排版本
	Scene            string           `json:"scene"`            // 使用场景
	AuthPlugins      []string         `json:"authPlugins"`      // 授权插件列表
	Tags             string           `json:"tags"`             // 标签
	ConflictCheck    bool             `json:"conflictCheck"`    // 冲突检测
	RootEntity       string           `json:"rootEntity"`       // 根节点
	PermissionToRole PermissionToRole `json:"permissionToRole"` // 角色
}

type CheckProcDefNameParam struct {
	Key  string `json:"Key"`  // key
	Name string `json:"name"` // 编排名称
}

type BatchUpdateProcDefStatusParam struct {
	ProcDefIds []string `json:"procDefIds"` // 编排id列表
	Status     string   `json:"status"`     // 更新状态 disabled 禁用 deleted 删除  deployed 部署状态
}

type BatchUpdateProcDefPermission struct {
	ProcDefIds       []string         `json:"procDefIds"`       // 编排id列表
	PermissionToRole PermissionToRole `json:"permissionToRole"` // 角色
}

type ProcDefCondition struct {
	Key  string `json:"Key"`  // key
	Name string `json:"name"` // 编排名称
}

// ProcDefIds 编排ids
type ProcDefIds struct {
	ProcDefIds []string `json:"procDefIds"` // 编排id列表
}

// ProcDefNodeDto 编排节点dto
type ProcDefNodeDto struct {
	ProcDefNodeCustomAttrs *ProcDefNodeCustomAttrs `json:"customAttrs"` // 节点数据
	NodeAttrs              interface{}             `json:"selfAttrs"`   // 节点属性,前端使用,保存即可
}

// ProcDefNodeExtendDto node&link dto
type ProcDefNodeExtendDto struct {
	Nodes []*ProcDefNodeDto     `json:"nodes"` // 节点
	Edges []*ProcDefNodeLinkDto `json:"edges"` // 线
}

type ProcDefNodeCustomAttrs struct {
	Id                string              `json:"id"`                // 节点Id
	Name              string              `json:"name"`              // 节点名称
	Status            string              `json:"status"`            // 状态
	NodeType          string              `json:"nodeType"`          // 节点类型
	ProcDefId         string              `json:"procDefId"`         // 编排定义id
	Timeout           int                 `json:"timeout"`           // 超时时间
	Description       string              `json:"description"`       // 描述
	DynamicBind       bool                `json:"dynamicBind"`       // 是否动态绑定
	BindNodeId        string              `json:"bindNodeId"`        // 动态绑定节点
	RoutineExpression string              `json:"routineExpression"` // 定位规则
	ServiceId         string              `json:"serviceId"`         // 插件服务ID
	ServiceName       string              `json:"serviceName"`       // 插件服务名
	RiskCheck         bool                `json:"riskCheck"`         // 是否高危检测
	ParamInfos        []*ProcDefNodeParam `json:"ParamInfos"`        // 节点参数
	ContextParamNodes string              `json:"contextParamNodes"` // 上下文参数节点
	TimeConfig        interface{}         `json:"timeConfig"`        // 节点配置
	OrderedNo         int                 `json:"orderedNo"`         // 节点顺序
	CreatedBy         string              `json:"createdBy" `        // 创建人
	CreatedTime       string              `json:"createdTime" `      // 创建时间
	UpdatedBy         string              `json:"updatedBy" `        // 更新人
	UpdatedTime       string              `json:"updatedTime" `      // 更新时间
}

// ProcDefNodeLinkDto 编排线
type ProcDefNodeLinkDto struct {
	ProcDefId                  string                      `json:"procDefId"`   // 编排Id
	ProcDefNodeLinkCustomAttrs *ProcDefNodeLinkCustomAttrs `json:"customAttrs"` // 节点数据
	SelfAttrs                  interface{}                 `json:"selfAttrs"`   // 节点属性,前端使用,保存即可
}

type ProcDefNodeLinkParam struct {
	ProcDefId string `json:"procDefId"` // 编排Id
	NodeId    string `json:"nodeId"`    // 节点id
	LinkId    string `json:"linkId"`    // 线id
}

// ProcessDefinitionDto  编排dto
type ProcessDefinitionDto struct {
	ProcDef           *ProcDefDto           `json:"procDef"`          // 编排
	PermissionToRole  PermissionToRole      `json:"permissionToRole"` // 角色
	ProcDefNodeExtend *ProcDefNodeExtendDto `json:"taskNodeInfos"`    // 编排节点&线集合
}

type ProcDefNodeLinkCustomAttrs struct {
	Id     string `json:"id"`     // Id
	Name   string `json:"name"`   // 线名称
	Source string `json:"source"` // 源
	Target string `json:"target"` // 目标
}

type ProcDefDto struct {
	Id            string   `json:"id"`            // 唯一标识
	Key           string   `json:"key"`           // 编排key
	Name          string   `json:"name"`          // 编排名称
	Version       string   `json:"version"`       // 版本
	RootEntity    string   `json:"rootEntity"`    // 根节点
	Status        string   `json:"status"`        // 状态
	Tags          string   `json:"tags"`          // 标签
	AuthPlugins   []string `json:"authPlugins"`   // 授权插件
	Scene         string   `json:"scene"`         // 使用场景
	ConflictCheck bool     `json:"conflictCheck"` // 冲突检测
	CreatedBy     string   `json:"createdBy"`     // 创建人
	CreatedTime   string   `json:"createdTime"`   // 创建时间
	UpdatedBy     string   `json:"updatedBy"`     // 更新人
	UpdatedTime   string   `json:"updatedTime"`   // 更新时间
}

type PermissionToRole struct {
	MGMT []string `json:"MGMT"` // 属主角色
	USE  []string `json:"USE"`  // 使用角色
}

type ProcDefSort []*ProcDef

func (q ProcDefSort) Len() int {
	return len(q)
}

func (q ProcDefSort) Less(i, j int) bool {
	t := strings.Compare(q[i].Version, q[j].Version)
	if t < 0 {
		return true
	}
	return false
}

func (q ProcDefSort) Swap(i, j int) {
	q[i], q[j] = q[j], q[i]
}

func ConvertProcDef2Dto(procDef *ProcDef) *ProcDefDto {
	var authPlugins = make([]string, 0)
	if procDef == nil {
		return nil
	}
	if len(procDef.ForPlugin) > 0 {
		authPlugins = strings.Split(procDef.ForPlugin, ",")
	}
	dto := &ProcDefDto{
		Id:            procDef.Id,
		Key:           procDef.Key,
		Name:          procDef.Name,
		Version:       procDef.Version,
		RootEntity:    procDef.RootEntity,
		Status:        procDef.Status,
		Tags:          procDef.Tags,
		AuthPlugins:   authPlugins,
		Scene:         procDef.Scene,
		ConflictCheck: procDef.ConflictCheck,
		CreatedBy:     procDef.CreatedBy,
		CreatedTime:   procDef.CreatedTime.Format(DateTimeFormat),
		UpdatedBy:     procDef.UpdatedBy,
		UpdatedTime:   procDef.UpdatedTime.Format(DateTimeFormat),
	}
	return dto
}

func ConvertProcDefNode2Dto(procDefNode *ProcDefNode, list []*ProcDefNodeParam) *ProcDefNodeDto {
	if procDefNode == nil {
		return nil
	}
	dto := &ProcDefNodeDto{
		ProcDefNodeCustomAttrs: &ProcDefNodeCustomAttrs{
			Id:                procDefNode.NodeId,
			Name:              procDefNode.Name,
			Status:            procDefNode.Status,
			NodeType:          procDefNode.NodeType,
			ProcDefId:         procDefNode.ProcDefId,
			Timeout:           procDefNode.Timeout,
			Description:       procDefNode.Description,
			DynamicBind:       procDefNode.DynamicBind,
			BindNodeId:        procDefNode.BindNodeId,
			RoutineExpression: procDefNode.RoutineExpression,
			ServiceId:         procDefNode.ServiceName,
			ServiceName:       procDefNode.ServiceName,
			RiskCheck:         procDefNode.RiskCheck,
			ParamInfos:        list,
			ContextParamNodes: procDefNode.ContextParamNodes,
			TimeConfig:        procDefNode.TimeConfig,
			OrderedNo:         procDefNode.OrderedNo,
			CreatedBy:         procDefNode.CreatedBy,
			CreatedTime:       procDefNode.CreatedTime.Format(DateTimeFormat),
			UpdatedBy:         procDefNode.UpdatedBy,
			UpdatedTime:       procDefNode.UpdatedTime.Format(DateTimeFormat),
		},
		NodeAttrs: procDefNode.UiStyle,
	}
	return dto
}

func ConvertParam2ProcDefNodeLink(param ProcDefNodeLinkDto) *ProcDefNodeLink {
	byteArr, _ := json.Marshal(param.SelfAttrs)
	nodeLinkAttr := param.ProcDefNodeLinkCustomAttrs
	return &ProcDefNodeLink{
		LinkId:  nodeLinkAttr.Id,
		Source:  nodeLinkAttr.Source,
		Target:  nodeLinkAttr.Target,
		Name:    nodeLinkAttr.Name,
		UiStyle: string(byteArr),
	}
}

func ConvertProcDefNodeLink2Dto(nodeLink *ProcDefNodeLink) *ProcDefNodeLinkDto {
	dto := &ProcDefNodeLinkDto{
		ProcDefNodeLinkCustomAttrs: &ProcDefNodeLinkCustomAttrs{
			Id:     nodeLink.LinkId,
			Name:   nodeLink.Name,
			Source: nodeLink.Source,
			Target: nodeLink.Target,
		},
		SelfAttrs: nodeLink.UiStyle,
	}
	return dto
}
