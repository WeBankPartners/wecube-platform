package models

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"reflect"
	"strconv"
	"strings"
	"time"
)

type ProcDefStatus string //定义编排定义状态
type PermissionType string

const (
	Draft    ProcDefStatus  = "draft"    //草稿
	Deployed ProcDefStatus  = "deployed" //部署状态
	Disabled ProcDefStatus  = "disabled" //禁用
	Enabled  ProcDefStatus  = "enabled"  //启用
	Deleted  ProcDefStatus  = "deleted"  //删除
	MGMT     PermissionType = "MGMT"     //管理权限
	USE      PermissionType = "USE"      //使用权限
)

// ProcDefNodeType 编排节点类型
type ProcDefNodeType string

const (
	ProcDefNodeTypeStart        ProcDefNodeType = "start"         //开始
	ProcDefNodeTypeEnd          ProcDefNodeType = "end"           //结束
	ProcDefNodeTypeAbnormal     ProcDefNodeType = "abnormal"      //异常
	ProcDefNodeTypeDecision     ProcDefNodeType = "decision"      //判断
	ProcDefNodeTypeFork         ProcDefNodeType = "fork"          //分流
	ProcDefNodeTypeMerge        ProcDefNodeType = "merge"         //汇聚
	ProcDefNodeTypeHuman        ProcDefNodeType = "human"         //人工节点
	ProcDefNodeTypeAutomatic    ProcDefNodeType = "automatic"     //自动节点
	ProcDefNodeTypeData         ProcDefNodeType = "data"          //数据节点
	ProcDefNodeTypeDate         ProcDefNodeType = "date"          //时间节点
	ProcDefNodeTypeTimeInterval ProcDefNodeType = "timeInterval"  //时间间隔
	ProcDefNodeSubProcess       ProcDefNodeType = "subProc"       // 子编排
	ProcDefNodeDecisionMerge    ProcDefNodeType = "decisionMerge" // 判断汇聚
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
	ManageRole    string    `json:"manageRole" xorm:"-"`                 // 属主
	SubProc       bool      `json:"subProc" xorm:"sub_proc"`             // 是否子编排
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
	DynamicBind       int       `json:"dynamicBind" xorm:"dynamic_bind"`              // 动态绑定 -> 0(启动时绑定)|1->(绑定节点)|2->(运行时)
	BindNodeId        string    `json:"bindNodeId" xorm:"bind_node_id"`               // 动态绑定节点
	RiskCheck         bool      `json:"riskCheck" xorm:"risk_check"`                  // 是否高危检测
	RoutineExpression string    `json:"routineExpression" xorm:"routine_expression"`  // 定位规则
	ContextParamNodes string    `json:"contextParamNodes" xorm:"context_param_nodes"` // 上下文参数节点
	Timeout           int       `json:"timeout" xorm:"timeout"`                       // 超时时间分钟
	TimeConfig        string    `json:"timeConfig" xorm:"time_config"`                // 节点配置
	OrderedNo         int       `json:"orderedNo" xorm:"ordered_no"`                  // 节点顺序
	UiStyle           string    `json:"uiStyle" xorm:"ui_style"`                      // 前端样式
	AllowContinue     bool      `json:"allowContinue" xorm:"allow_continue"`          // 允许跳过
	SubProcDefId      string    `json:"subProcDefId" xorm:"sub_proc_def_id"`          // 子编排定义id
	CreatedBy         string    `json:"createdBy" xorm:"created_by"`                  // 创建人
	CreatedTime       time.Time `json:"createdTime" xorm:"created_time"`              // 创建时间
	UpdatedBy         string    `json:"updatedBy" xorm:"updated_by"`                  // 更新人
	UpdatedTime       time.Time `json:"updatedTime" xorm:"updated_time"`              // 更新时间
}

type ProcDefNodeParam struct {
	Id            string `json:"-" xorm:"id"`                        // 唯一标识
	ProcDefNodeId string `json:"nodeId" xorm:"proc_def_node_id"`     // 编排节点id
	ParamId       string `json:"id" xorm:"param_id"`                 // 编排节点参数id
	Name          string `json:"paramName" xorm:"name"`              // 参数名
	BindType      string `json:"bindType" xorm:"bind_type"`          // 参数类型->context(上下文) | constant(静态值)
	Value         string `json:"bindValue" xorm:"value"`             // 参数值
	CtxBindNode   string `json:"bindNodeId" xorm:"ctx_bind_node"`    // 上下文节点
	CtxBindType   string `json:"bindParamType" xorm:"ctx_bind_type"` // 上下文出入参->input(入参) | output(出参)
	CtxBindName   string `json:"bindParamName" xorm:"ctx_bind_name"` // 上下文参数名
	Required      string `json:"required" xorm:"required"`           // 是否必填
}

type ProcDefNodeLink struct {
	Id        string `json:"id" xorm:"id"`                 // 唯一标识(source__target)
	ProcDefId string `json:"procDefId" xorm:"proc_def_id"` // 编排id
	LinkId    string `json:"LinkId" xorm:"link_id"`        // 前端线id
	Source    string `json:"source" xorm:"source"`         // 源节点
	Target    string `json:"target" xorm:"target"`         // 目标节点
	Name      string `json:"name" xorm:"name"`             // 连接名称
	UiStyle   string `json:"uiStyle" xorm:"ui_style"`      // 前端样式
}

type ProcDefPermission struct {
	Id         string `json:"id" xorm:"id"`                 // 唯一标识
	ProcDefId  string `json:"procDefId" xorm:"proc_def_id"` // 编排id
	RoleId     string `json:"roleId" xorm:"role_id"`        // 角色id
	RoleName   string `json:"roleName" xorm:"role_name"`    // 角色名称
	Permission string `json:"permission" xorm:"permission"` // 权限->MGMT(管理) | USE(使用)
}

type ProcDefCollect struct {
	Id          string    `json:"id" xorm:"id"`                                    // 唯一标识
	ProcDefId   string    `json:"procDefId" xorm:"proc_def_id" binding:"required"` // 编排id
	RoleId      string    `json:"roleId" xorm:"role_id"`                           // 角色id
	UserId      string    `json:"userId" xorm:"user_id"`                           // 用户id
	CreatedTime time.Time `json:"createdTime" xorm:"created_time"`                 // 创建时间
	UpdatedTime time.Time `json:"updatedTime" xorm:"updated_time"`                 // 更新时间
}

type SyncUseRoleParam struct {
	ProcDefId string   `json:"procDefId"` // 编排ID
	UseRoles  []string `json:"useRoles"`  // 使用角色
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
	SubProc          bool             `json:"subProc"`          // 是否子编排
}

type CheckProcDefNameParam struct {
	Key  string `json:"Key"`  // key
	Name string `json:"name"` // 编排名称
}

type BatchUpdateProcDefStatusParam struct {
	ProcDefIds []string `json:"procDefIds"` // 编排id列表
	Status     string   `json:"status"`     // 更新状态 disabled 禁用 deleted 删除  enabled 启用
}

type QueryProcessDefinitionParam struct {
	ProcDefId        string   `json:"procDefId"`        // 编排Id
	ProcDefName      string   `json:"procDefName"`      // 编排名称
	Plugins          []string `json:"plugins"`          // 授权插件
	UpdatedTimeStart string   `json:"updatedTimeStart"` // 更新时间开始
	UpdatedTimeEnd   string   `json:"updatedTimeEnd"`   // 更新时间结束
	CreatedTimeStart string   `json:"createdTimeStart"` // 创建时间开始
	CreatedTimeEnd   string   `json:"createdTimeEnd"`   // 创建时间结束
	Status           string   `json:"status"`           // disabled 禁用 draft草稿 deployed 发布状态
	CreatedBy        string   `json:"createdBy"`        // 创建人
	UpdatedBy        string   `json:"updatedBy"`        // 更新人
	Scene            string   `json:"scene"`            // 使用场景
	UserRoles        []string // 用户角色
	LastVersion      bool     `json:"lastVersion"`
	SubProc          string   `json:"subProc"` // 是否子编排 -> all(全部编排) | main(主编排)  |  sub(子编排)
	PermissionType   string   `json:"permissionType"`
	OnlyCollect      bool     `json:"onlyCollect"`
	Operator         string   `json:"operator"`
}

type BatchUpdateProcDefPermission struct {
	ProcDefIds       []string         `json:"procDefIds"`       // 编排id列表
	PermissionToRole PermissionToRole `json:"permissionToRole"` // 角色
}

type ProcDefCondition struct {
	Key     string `json:"Key"`     // key
	Name    string `json:"name"`    // 编排名称
	Status  string `json:"status"`  // 状态
	Version string `json:"version"` // 版本
}

// ProcDefIds 编排ids
type ProcDefIds struct {
	ProcDefIds []string `json:"procDefIds"` // 编排id列表
}

// ProcDefNodeResultDto 编排节点参数
type ProcDefNodeResultDto struct {
	ProcDefNodeCustomAttrs *ProcDefNodeCustomAttrsDto `json:"customAttrs"` // 节点数据
	NodeAttrs              interface{}                `json:"selfAttrs"`   // 节点属性,前端使用,保存即可
}

type ProcDefNodeSimpleDto struct {
	NodeId            string `json:"nodeId"`            // 节点id
	Name              string `json:"nodeName"`          // 节点名称
	NodeType          string `json:"nodeType"`          // 节点类型
	RoutineExpression string `json:"routineExpression"` // 定位规则
}

// ProcDefNodeRequestParam 编排节点dto
type ProcDefNodeRequestParam struct {
	ProcDefNodeCustomAttrs *ProcDefNodeCustomAttrs `json:"customAttrs"` // 节点数据
	NodeAttrs              interface{}             `json:"selfAttrs"`   // 节点属性,前端使用,保存即可
}

// ProcDefNodeExtendDto node&link dto
type ProcDefNodeExtendDto struct {
	Nodes []*ProcDefNodeResultDto `json:"nodes"` // 节点
	Edges []*ProcDefNodeLinkDto   `json:"edges"` // 线
}

type ProcDefNodeCustomAttrs struct {
	Id                string              `json:"id"`                // 节点Id
	Name              string              `json:"name"`              // 节点名称
	Status            string              `json:"status"`            // 状态
	NodeType          string              `json:"nodeType"`          // 节点类型
	ProcDefId         string              `json:"procDefId"`         // 编排定义id
	Timeout           int                 `json:"timeout"`           // 超时时间
	Description       string              `json:"description"`       // 描述
	DynamicBind       int                 `json:"dynamicBind"`       // 动态绑定 -> 0(启动时绑定)|1->(绑定节点)|2->(运行时)
	BindNodeId        string              `json:"bindNodeId"`        // 动态绑定节点
	RoutineExpression string              `json:"routineExpression"` // 定位规则
	ServiceId         string              `json:"serviceId"`         // 插件服务ID
	ServiceName       string              `json:"serviceName"`       // 插件服务名
	RiskCheck         bool                `json:"riskCheck"`         // 是否高危检测
	ParamInfos        []*ProcDefNodeParam `json:"paramInfos"`        // 节点参数
	ContextParamNodes []string            `json:"contextParamNodes"` // 上下文参数节点
	TimeConfig        interface{}         `json:"timeConfig"`        // 节点配置
	OrderedNo         int                 `json:"orderedNo"`         // 节点顺序
	CreatedBy         string              `json:"createdBy" `        // 创建人
	CreatedTime       string              `json:"createdTime" `      // 创建时间
	UpdatedBy         string              `json:"updatedBy" `        // 更新人
	UpdatedTime       string              `json:"updatedTime" `      // 更新时间
	AllowContinue     bool                `json:"allowContinue"`     // 允许跳过
	SubProcDefId      string              `json:"subProcDefId"`      // 子编排定义id
}

type ProcDefNodeCustomAttrsDto struct {
	Id                string              `json:"id"`                // 节点Id
	Name              string              `json:"name"`              // 节点名称
	Status            string              `json:"status"`            // 状态
	NodeType          string              `json:"nodeType"`          // 节点类型
	ProcDefId         string              `json:"procDefId"`         // 编排定义id
	Timeout           int                 `json:"timeout"`           // 超时时间
	Description       string              `json:"description"`       // 描述
	DynamicBind       int                 `json:"dynamicBind"`       // 动态绑定 -> 0(启动时绑定)|1->(绑定节点)|2->(运行时)
	BindNodeId        string              `json:"bindNodeId"`        // 动态绑定节点
	RoutineExpression string              `json:"routineExpression"` // 定位规则
	ServiceName       string              `json:"serviceName"`       // 插件服务名
	RiskCheck         bool                `json:"riskCheck"`         // 是否高危检测
	ParamInfos        []*ProcDefNodeParam `json:"paramInfos"`        // 节点参数
	ContextParamNodes []string            `json:"contextParamNodes"` // 上下文参数节点
	TimeConfig        interface{}         `json:"timeConfig"`        // 节点配置
	OrderedNo         int                 `json:"orderedNo"`         // 节点顺序
	CreatedBy         string              `json:"createdBy" `        // 创建人
	CreatedTime       string              `json:"createdTime" `      // 创建时间
	UpdatedBy         string              `json:"updatedBy" `        // 更新人
	UpdatedTime       string              `json:"updatedTime" `      // 更新时间
	AllowContinue     bool                `json:"allowContinue"`     // 允许跳过
	SubProcDefId      string              `json:"subProcDefId"`      // 子编排定义id
	SubProcDefName    string              `json:"subProcDefName"`    // 子编排定义名称
	SubProcDefVersion string              `json:"subProcDefVersion"` // 子编排定义版本
}

type InterfaceParameterDto struct {
	Type     string `json:"type"`     // 类型
	Name     string `json:"name"`     // 名称
	DataType string `json:"dataType"` // 数据类型
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

type ImportResultDto struct {
	ResultList []*ImportResultItemDto `json:"resultList"`
}

type ImportResultItemDto struct {
	ProcDefId      string `json:"procDefId"`      // 编排Id
	ProcDefName    string `json:"procDefName"`    // 编排名称
	ProcDefVersion string `json:"ProcDefVersion"` // 编排版本
	Code           int    `json:"code"`           // 0表示成功,1表示编排已有草稿,不允许导入  2表示版本冲突  3表示服务报错
	Message        string `json:"message"`        // 国际化词条
	ErrMsg         string `json:"errMsg"`         // 报错
}

type ProcDefNodeLinkCustomAttrs struct {
	Id     string `json:"id"`     // Id
	Name   string `json:"name"`   // 线名称
	Source string `json:"source"` // 源
	Target string `json:"target"` // 目标
}

type ProcDefDto struct {
	Id               string   `json:"id"`               // 唯一标识
	Key              string   `json:"key"`              // 编排key
	Name             string   `json:"name"`             // 编排名称
	Version          string   `json:"version"`          // 版本
	RootEntity       string   `json:"rootEntity"`       // 根节点
	Status           string   `json:"status"`           // 状态
	Tags             string   `json:"tags"`             // 标签
	AuthPlugins      []string `json:"authPlugins"`      // 授权插件
	Scene            string   `json:"scene"`            // 使用场景
	ConflictCheck    bool     `json:"conflictCheck"`    // 冲突检测
	CreatedBy        string   `json:"createdBy"`        // 创建人
	CreatedTime      string   `json:"createdTime"`      // 创建时间
	UpdatedBy        string   `json:"updatedBy"`        // 更新人
	UpdatedTime      string   `json:"updatedTime"`      // 更新时间
	EnableCreated    bool     `json:"enableCreated"`    // 能否创建新版本
	EnableModifyName bool     `json:"enableModifyName"` // 能否修改名称
	UseRoles         []string `json:"userRoles"`        // 使用角色
	UseRolesDisplay  []string `json:"userRolesDisplay"` // 使用角色-显示名
	MgmtRoles        []string `json:"mgmtRoles"`        // 管理角色
	MgmtRolesDisplay []string `json:"mgmtRolesDisplay"` // 管理角色-显示名
	SubProc          bool     `json:"subProc"`          // 是否子编排
	Collected        bool     `json:"collected"`        // 是否收藏
}

type ProcDefParentListItem struct {
	Id      string `json:"id"`      // 唯一标识
	Key     string `json:"key"`     // 编排key
	Name    string `json:"name"`    // 编排名称
	Version string `json:"version"` // 版本
	Status  string `json:"status"`  // 状态
}

type TimeConfigDto struct {
	Date     string `json:"date"`
	Duration string `json:"duration"`
	Unit     string `json:"unit"`
}

type ProcDefQueryDto struct {
	ManageRole        string        `json:"manageRole"`        //管理角色
	ManageRoleDisplay string        `json:"manageRoleDisplay"` //管理角色-显示名
	ProcDefList       []*ProcDefDto `json:"dataList"`          // 编排列表
}

type PermissionToRole struct {
	MGMT []string `json:"MGMT"` // 属主角色
	USE  []string `json:"USE"`  // 使用角色
}

type ProcNodeObj struct {
	NodeId        string        `json:"nodeId"`
	NodeName      string        `json:"nodeName"`
	NodeType      string        `json:"nodeType"`
	NodeDefId     string        `json:"nodeDefId"`
	NodeDefType   string        `json:"nodeDefType"`
	TaskCategory  string        `json:"taskCategory"`
	RoutineExp    string        `json:"routineExp"`
	ServiceId     string        `json:"serviceId"`
	ServiceName   string        `json:"serviceName"`
	OrderedNo     string        `json:"orderedNo"`
	OrderedNum    int           `json:"-"`
	DynamicBind   string        `json:"dynamicBind"`
	BoundEntities []*ProcEntity `json:"boundEntities"`
}

type ProcEntity struct {
	Id          string                    `json:"id"`
	PackageName string                    `json:"packageName"`
	Name        string                    `json:"name"`
	Description string                    `json:"description"`
	DisplayName string                    `json:"displayName"`
	Attributes  []*ProcEntityAttributeObj `json:"attributes"`
}

func (p *ProcEntity) ParseAttr(attrs []*PluginPackageAttributes) {
	for _, v := range attrs {
		p.Attributes = append(p.Attributes, &ProcEntityAttributeObj{
			Id:                v.Id,
			Name:              v.Name,
			Description:       v.Description,
			DataType:          v.DataType,
			Mandatory:         v.MandatoryString,
			RefPackageName:    v.RefPackage,
			RefEntityName:     v.RefEntity,
			RefAttrName:       v.RefAttr,
			ReferenceId:       v.ReferenceId,
			EntityId:          v.EntityId,
			EntityName:        v.Name,
			EntityDisplayName: v.Description,
			EntityPackage:     v.Package,
			Multiple:          v.Multiple,
			OrderNo:           v.OrderNo,
			Active:            true,
		})
	}
}

type ProcEntityAttributeObj struct {
	Id                string `json:"id"`
	Name              string `json:"name"`
	Description       string `json:"description"`
	DataType          string `json:"dataType"`
	Mandatory         string `json:"mandatory"`
	RefPackageName    string `json:"refPackageName"`
	RefEntityName     string `json:"refEntityName"`
	RefAttrName       string `json:"refAttrName"`
	ReferenceId       string `json:"referenceId"`
	Active            bool   `json:"active"`
	EntityId          string `json:"entityId"`
	EntityName        string `json:"entityName"`
	EntityDisplayName string `json:"entityDisplayName"`
	EntityPackage     string `json:"entityPackage"`
	Multiple          string `json:"multiple"`
	OrderNo           int    `json:"orderNo"`
}

type ProcDefImportDto struct {
	Ctx           context.Context
	InputList     []*ProcessDefinitionDto
	Operator      string
	UserToken     string
	Language      string
	IsTransImport bool // 是否为底座迁移导入
}

type UpdateProcDefHandlerParam struct {
	ProcDefId        string `json:"procDefId"`        //模板id
	LatestUpdateTime string `json:"latestUpdateTime"` //最后更新时间
}

type ProcDefSort []*ProcDef

func (q ProcDefSort) Len() int {
	return len(q)
}

func (q ProcDefSort) Less(i, j int) bool {
	var versionA, versionB int
	if len(q[i].Version) > 1 {
		versionA, _ = strconv.Atoi(q[i].Version[1:])
	}
	if len(q[j].Version) >= 1 {
		versionB, _ = strconv.Atoi(q[j].Version[1:])
	}
	return versionA < versionB
}

func (q ProcDefSort) Swap(i, j int) {
	q[i], q[j] = q[j], q[i]
}

type ProcDefDtoSort []*ProcDefDto

func (q ProcDefDtoSort) Len() int {
	return len(q)
}

func (q ProcDefDtoSort) Less(i, j int) bool {
	t1, _ := time.Parse(DateTimeFormat, q[i].UpdatedTime)
	t2, _ := time.Parse(DateTimeFormat, q[j].UpdatedTime)
	return t1.Sub(t2).Seconds() >= 0
}

func (q ProcDefDtoSort) Swap(i, j int) {
	q[i], q[j] = q[j], q[i]
}

type ImportResultItemDtoSort []*ImportResultItemDto

func (q ImportResultItemDtoSort) Len() int {
	return len(q)
}

func (q ImportResultItemDtoSort) Less(i, j int) bool {
	return q[i].Code < q[j].Code
}

func (q ImportResultItemDtoSort) Swap(i, j int) {
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
		SubProc:       procDef.SubProc,
	}
	return dto
}

func ConvertProcDefDto2Model(dto *ProcDefDto) *ProcDef {
	var authPlugins string
	var createTime, updateTime time.Time
	if dto.CreatedTime != "" {
		createTime, _ = time.Parse(DateTimeFormat, dto.CreatedTime)
	}
	if dto.UpdatedTime != "" {
		updateTime, _ = time.Parse(DateTimeFormat, dto.UpdatedTime)
	}
	if len(dto.AuthPlugins) > 0 {
		authPlugins = strings.Join(dto.AuthPlugins, ",")
	}
	return &ProcDef{
		Id:            dto.Id,
		Key:           dto.Key,
		Name:          dto.Name,
		Version:       dto.Version,
		RootEntity:    dto.RootEntity,
		Status:        dto.Status,
		Tags:          dto.Tags,
		ForPlugin:     authPlugins,
		Scene:         dto.Scene,
		ConflictCheck: dto.ConflictCheck,
		CreatedBy:     dto.CreatedBy,
		CreatedTime:   createTime,
		UpdatedBy:     dto.UpdatedBy,
		UpdatedTime:   updateTime,
		SubProc:       dto.SubProc,
	}
}

func ConvertProcDefNodeResultDto2Model(dto *ProcDefNodeResultDto) (node *ProcDefNode, list []*ProcDefNodeParam) {
	var contextParamNodes string
	var createTime, updateTime time.Time
	var timeConfig, uiStyle string
	if dto.ProcDefNodeCustomAttrs != nil {
		attr := dto.ProcDefNodeCustomAttrs
		if reflect.TypeOf(attr.TimeConfig).Name() == "string" {
			timeConfig = attr.TimeConfig.(string)
		}
		if reflect.TypeOf(dto.NodeAttrs).Name() == "string" {
			uiStyle = dto.NodeAttrs.(string)
		}
		if len(attr.ContextParamNodes) > 0 {
			contextParamNodes = strings.Join(attr.ContextParamNodes, ",")
		}
		if attr.CreatedTime != "" {
			createTime, _ = time.Parse(DateTimeFormat, attr.CreatedTime)
		}
		if attr.UpdatedTime != "" {
			updateTime, _ = time.Parse(DateTimeFormat, attr.UpdatedTime)
		}
		node = &ProcDefNode{
			Id:                attr.Id,
			NodeId:            attr.Id,
			ProcDefId:         attr.ProcDefId,
			Name:              attr.Name,
			Description:       attr.Description,
			Status:            attr.Status,
			NodeType:          attr.NodeType,
			ServiceName:       attr.ServiceName,
			DynamicBind:       attr.DynamicBind,
			BindNodeId:        attr.BindNodeId,
			RiskCheck:         attr.RiskCheck,
			RoutineExpression: attr.RoutineExpression,
			ContextParamNodes: contextParamNodes,
			Timeout:           attr.Timeout,
			TimeConfig:        timeConfig,
			OrderedNo:         attr.OrderedNo,
			UiStyle:           uiStyle,
			CreatedBy:         attr.CreatedBy,
			CreatedTime:       createTime,
			UpdatedBy:         attr.UpdatedBy,
			UpdatedTime:       updateTime,
			AllowContinue:     attr.AllowContinue,
			SubProcDefId:      attr.SubProcDefId,
		}
		if dto.ProcDefNodeCustomAttrs != nil {
			list = dto.ProcDefNodeCustomAttrs.ParamInfos
		}
	}
	return
}

func ConvertProcDefNode2Dto(procDefNode *ProcDefNode, list []*ProcDefNodeParam) *ProcDefNodeResultDto {
	var contextParamNodes []string
	if procDefNode == nil {
		return nil
	}
	if len(procDefNode.ContextParamNodes) > 0 {
		contextParamNodes = strings.Split(procDefNode.ContextParamNodes, ",")
	}
	if len(list) > 0 {
		// 节点参数中,节点id设置为前端展示nodeId
		for _, nodeParam := range list {
			nodeParam.ProcDefNodeId = procDefNode.NodeId
		}
	}
	dto := &ProcDefNodeResultDto{
		ProcDefNodeCustomAttrs: &ProcDefNodeCustomAttrsDto{
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
			ServiceName:       procDefNode.ServiceName,
			RiskCheck:         procDefNode.RiskCheck,
			ParamInfos:        list,
			ContextParamNodes: contextParamNodes,
			TimeConfig:        procDefNode.TimeConfig,
			OrderedNo:         procDefNode.OrderedNo,
			CreatedBy:         procDefNode.CreatedBy,
			CreatedTime:       procDefNode.CreatedTime.Format(DateTimeFormat),
			UpdatedBy:         procDefNode.UpdatedBy,
			UpdatedTime:       procDefNode.UpdatedTime.Format(DateTimeFormat),
			AllowContinue:     procDefNode.AllowContinue,
			SubProcDefId:      procDefNode.SubProcDefId,
		},
		NodeAttrs: procDefNode.UiStyle,
	}
	return dto
}

func ConvertParam2ProcDefNodeLink(param *ProcDefNodeLinkDto) *ProcDefNodeLink {
	byteArr, _ := json.Marshal(param.SelfAttrs)
	nodeLinkAttr := param.ProcDefNodeLinkCustomAttrs
	return &ProcDefNodeLink{
		ProcDefId: param.ProcDefId,
		LinkId:    nodeLinkAttr.Id,
		Source:    nodeLinkAttr.Source,
		Target:    nodeLinkAttr.Target,
		Name:      nodeLinkAttr.Name,
		UiStyle:   string(byteArr),
	}
}

func CovertNodeLinkDto2Model(param *ProcDefNodeLinkDto) *ProcDefNodeLink {
	var uiStyle string
	if reflect.TypeOf(param.SelfAttrs).Name() == "string" {
		uiStyle = param.SelfAttrs.(string)
	}
	nodeLinkAttr := param.ProcDefNodeLinkCustomAttrs
	return &ProcDefNodeLink{
		ProcDefId: param.ProcDefId,
		LinkId:    nodeLinkAttr.Id,
		Source:    nodeLinkAttr.Source,
		Target:    nodeLinkAttr.Target,
		Name:      nodeLinkAttr.Name,
		UiStyle:   uiStyle,
	}
}

func ConvertProcDefNodeLink2Dto(nodeLink *ProcDefNodeLink, nodeList []*ProcDefNode) *ProcDefNodeLinkDto {
	var source, target string
	nodeMap := ConvertProcDefNode2Map(nodeList)
	if nodeMap[nodeLink.Source] != nil {
		source = nodeMap[nodeLink.Source].NodeId
	}
	if nodeMap[nodeLink.Target] != nil {
		target = nodeMap[nodeLink.Target].NodeId
	}
	dto := &ProcDefNodeLinkDto{
		ProcDefId: nodeLink.ProcDefId,
		ProcDefNodeLinkCustomAttrs: &ProcDefNodeLinkCustomAttrs{
			Id:     nodeLink.LinkId,
			Name:   nodeLink.Name,
			Source: source,
			Target: target,
		},
		SelfAttrs: nodeLink.UiStyle,
	}
	return dto
}

func ConvertProcDefNode2Map(nodeList []*ProcDefNode) map[string]*ProcDefNode {
	hashmap := make(map[string]*ProcDefNode)
	for _, node := range nodeList {
		hashmap[node.Id] = node
	}
	return hashmap
}

func ConvertProcDefNode2SimpleDto(procDefNode *ProcDefNode) *ProcDefNodeSimpleDto {
	return &ProcDefNodeSimpleDto{
		NodeId:            procDefNode.NodeId,
		Name:              procDefNode.Name,
		NodeType:          procDefNode.NodeType,
		RoutineExpression: procDefNode.RoutineExpression,
	}
}

func BuildInterfaceParameterDto(p *PluginConfigInterfaceParameters) *InterfaceParameterDto {
	return &InterfaceParameterDto{
		Type:     p.Type,
		Name:     p.Name,
		DataType: p.DataType,
	}
}

func BuildProcDefDto(procDef *ProcDef, userRoles, manageRoles, userRolesDisplay, manageRolesDisplay []string, enableCreated bool, collectProcDefMap map[string]string) *ProcDefDto {
	var authPlugins = make([]string, 0)
	if len(procDef.ForPlugin) > 0 {
		authPlugins = strings.Split(procDef.ForPlugin, ",")
	}
	collected := false
	if _, ok := collectProcDefMap[procDef.Id]; ok {
		collected = true
	}
	return &ProcDefDto{
		Id:               procDef.Id,
		Key:              procDef.Key,
		Name:             procDef.Name,
		Version:          procDef.Version,
		RootEntity:       procDef.RootEntity,
		Status:           procDef.Status,
		Tags:             procDef.Tags,
		AuthPlugins:      authPlugins,
		Scene:            procDef.Scene,
		ConflictCheck:    procDef.ConflictCheck,
		CreatedBy:        procDef.CreatedBy,
		CreatedTime:      procDef.CreatedTime.Format(DateTimeFormat),
		UpdatedBy:        procDef.UpdatedBy,
		UpdatedTime:      procDef.UpdatedTime.Format(DateTimeFormat),
		EnableCreated:    enableCreated,
		UseRoles:         userRoles,
		UseRolesDisplay:  userRolesDisplay,
		MgmtRoles:        manageRoles,
		MgmtRolesDisplay: manageRolesDisplay,
		Collected:        collected,
	}
}

func ConvertParam2ProcDefNode(user string, param ProcDefNodeRequestParam) *ProcDefNode {
	var contextParamNodes string
	now := time.Now()
	byteArr, _ := json.Marshal(param.NodeAttrs)
	procDefNodeAttr := param.ProcDefNodeCustomAttrs
	byteArr2, _ := json.Marshal(procDefNodeAttr.TimeConfig)
	if len(param.ProcDefNodeCustomAttrs.ContextParamNodes) > 0 {
		contextParamNodes = strings.Join(param.ProcDefNodeCustomAttrs.ContextParamNodes, ",")
	}
	node := &ProcDefNode{
		NodeId:            procDefNodeAttr.Id,
		ProcDefId:         procDefNodeAttr.ProcDefId,
		Name:              procDefNodeAttr.Name,
		Description:       procDefNodeAttr.Description,
		Status:            string(Draft),
		NodeType:          procDefNodeAttr.NodeType,
		ServiceName:       procDefNodeAttr.ServiceName,
		DynamicBind:       procDefNodeAttr.DynamicBind,
		BindNodeId:        procDefNodeAttr.BindNodeId,
		RiskCheck:         procDefNodeAttr.RiskCheck,
		RoutineExpression: procDefNodeAttr.RoutineExpression,
		ContextParamNodes: contextParamNodes,
		Timeout:           procDefNodeAttr.Timeout,
		TimeConfig:        string(byteArr2),
		OrderedNo:         procDefNodeAttr.OrderedNo,
		UiStyle:           string(byteArr),
		CreatedBy:         user,
		CreatedTime:       now,
		UpdatedBy:         user,
		UpdatedTime:       now,
		AllowContinue:     procDefNodeAttr.AllowContinue,
		SubProcDefId:      procDefNodeAttr.SubProcDefId,
	}
	return node
}

func GenNodeId(nodeType string) string {
	nodeTypeShort := nodeType
	if len(nodeTypeShort) > 4 {
		nodeTypeShort = nodeTypeShort[:4]
	}
	return fmt.Sprintf("pdn_%s_%s", nodeTypeShort, guid.CreateGuid())
}

type ProcDefParentPageResult struct {
	Page    *PageInfo                `json:"page"`
	Content []*ProcDefParentListItem `json:"content"`
}

type ProcDefSortNodes []*ProcDefNode

func (p ProcDefSortNodes) Len() int {
	return len(p)
}

func (p ProcDefSortNodes) Swap(i, j int) {
	p[i], p[j] = p[j], p[i]
}

func (p ProcDefSortNodes) Less(i, j int) bool {
	return p[i].OrderedNo < p[j].OrderedNo
}
