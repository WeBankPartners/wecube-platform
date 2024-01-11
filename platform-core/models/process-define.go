package models

import (
	"strings"
	"time"
)

type ProcDefStatus string //定义编排定义状态
type PermissionType string

const (
	Draft     ProcDefStatus  = "draft"     //草稿
	Deployed  ProcDefStatus  = "deployed"  //部署状态
	Deleted   ProcDefStatus  = "deleted"   //删除
	Templated ProcDefStatus  = "template"  //模板
	PreDeploy ProcDefStatus  = "predeploy" //预发布
	MGMT      PermissionType = "MGMT"      //管理权限
	USE       PermissionType = "USE"       //使用权限
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
	Name          string `json:"name" xorm:"name"`                      // 参数名
	BindType      string `json:"bindType" xorm:"bind_type"`             // 参数类型->context(上下文) | constant(静态值)
	Value         string `json:"value" xorm:"value"`                    // 参数值
	CtxBindNode   string `json:"ctxBindNode" xorm:"ctx_bind_node"`      // 上下文节点
	CtxBindType   string `json:"ctxBindType" xorm:"ctx_bind_type"`      // 上下文出入参->input(入参) | output(出参)
	CtxBindName   string `json:"ctxBindName" xorm:"ctx_bind_name"`      // 上下文参数名
}

type ProcDefNodeLink struct {
	Id      string `json:"id" xorm:"id"`            // 唯一标识(source__target)
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
	Name             string           `json:"name"`             // 编排名称
	Version          string           `json:"version"`          // 编排版本
	UseCase          string           `json:"useCase"`          // 使用场景
	AuthPlugins      []string         `json:"authPlugins"`      // 授权插件列表
	Tags             string           `json:"tags"`             // 标签
	ConflictCheck    bool             `json:"conflictCheck"`    // 冲突检测
	RootEntity       string           `json:"rootEntity"`       // 根节点
	PermissionToRole PermissionToRole `json:"permissionToRole"` // 角色
}

// ProcessDefinitionTaskNodeParam 添加编排节点参数
type ProcessDefinitionTaskNodeParam struct {
	Id                string              `json:"id"`                // 节点Id
	Name              string              `json:"name"`              // 节点名称
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
	OrderedNo         int                 `json:"orderedNo"`         // 节点顺序
	NodeAttrs         interface{}         `json:"nodeAttrs"`         // 节点属性,前端使用,保存即可
}

// ProcessDefinitionDto  编排dto
type ProcessDefinitionDto struct {
	ProcDef          *ProcDefDto       `json:"procDef"`          // 编排
	PermissionToRole PermissionToRole  `json:"permissionToRole"` // 角色
	ProcDefNodeList  []*ProcDefNodeDto `json:"taskNodeInfos"`    // 编排节点集合
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
	UseCase       string   `json:"scene"`         // 使用场景
	ConflictCheck bool     `json:"conflictCheck"` // 冲突检测
	CreatedBy     string   `json:"createdBy"`     // 创建人
	CreatedTime   string   `json:"createdTime"`   // 创建时间
	UpdatedBy     string   `json:"updatedBy"`     // 更新人
	UpdatedTime   string   `json:"updatedTime"`   // 更新时间
}

type ProcDefNodeDto struct {
	Id                string              `json:"id" `                // 唯一标识
	ProcDefId         string              `json:"procDefId" `         // 编排id
	Name              string              `json:"name" `              // 节点名称
	Description       string              `json:"description"`        // 节点描述
	Status            string              `json:"status"`             // 状态
	NodeType          string              `json:"nodeType"`           // 节点类型
	ServiceName       string              `json:"serviceName"`        // 插件服务名
	DynamicBind       bool                `json:"dynamicBind" `       // 是否动态绑定
	BindNodeId        string              `json:"bindNodeId" `        // 动态绑定节点
	RiskCheck         bool                `json:"riskCheck" `         // 是否高危检测
	RoutineExpression string              `json:"routineExpression" ` // 定位规则
	ContextParamNodes string              `json:"contextParamNodes" ` // 上下文参数节点
	Timeout           int                 `json:"timeout" `           // 超时时间分钟
	OrderedNo         int                 `json:"orderedNo" `         // 节点顺序
	UiStyle           string              `json:"uiStyle" `           // 前端样式
	CreatedBy         string              `json:"createdBy" `         // 创建人
	CreatedTime       string              `json:"createdTime" `       // 创建时间
	UpdatedBy         string              `json:"updatedBy" `         // 更新人
	UpdatedTime       string              `json:"updatedTime" `       // 更新时间
	ParamInfos        []*ProcDefNodeParam `json:"ParamInfos"`         // 节点参数
}

type PermissionToRole struct {
	MGMT []string `json:"MGMT"` // 属主角色
	USE  []string `json:"USE"`  // 使用角色
}

func ConvertProcDef2Dto(procDef *ProcDef) *ProcDefDto {
	var authPlugins []string
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
		UseCase:       procDef.Scene,
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
		Id:                procDefNode.Id,
		ProcDefId:         procDefNode.ProcDefId,
		Name:              procDefNode.Name,
		Description:       procDefNode.Description,
		Status:            procDefNode.Status,
		NodeType:          procDefNode.NodeType,
		ServiceName:       procDefNode.ServiceName,
		DynamicBind:       procDefNode.DynamicBind,
		BindNodeId:        procDefNode.BindNodeId,
		RiskCheck:         procDefNode.RiskCheck,
		RoutineExpression: procDefNode.RoutineExpression,
		ContextParamNodes: procDefNode.ContextParamNodes,
		Timeout:           procDefNode.Timeout,
		OrderedNo:         procDefNode.OrderedNo,
		UiStyle:           procDefNode.UiStyle,
		CreatedBy:         procDefNode.CreatedBy,
		CreatedTime:       procDefNode.CreatedTime.Format(time.DateTime),
		UpdatedBy:         procDefNode.UpdatedBy,
		UpdatedTime:       procDefNode.UpdatedTime.Format(time.DateTime),
		ParamInfos:        list,
	}
	return dto
}
