package models

import "time"

type ProcDef struct {
	Id            string    `json:"id" xorm:"id"`                        // 唯一标识
	Key           string    `json:"key" xorm:"key"`                      // 编排key
	Name          string    `json:"name" xorm:"name"`                    // 编排名称
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
