package models

import (
	"context"
	"time"
)

type AnalyzeDataTransParam struct {
	TransExportId   string   `json:"transExportId"`
	Business        []string `json:"business"`
	Env             string   `json:"env"`
	LastConfirmTime string   `json:"lastConfirmTime"`
}

type SysCiTypeTable struct {
	Id           string `json:"ciTypeId" xorm:"id" binding:"required"`
	DisplayName  string `json:"name" xorm:"display_name"`
	Description  string `json:"description" xorm:"description"`
	Status       string `json:"status" xorm:"status"`
	ImageFile    string `json:"imageFile" xorm:"image_file"`
	FileName     string `json:"fileName" xorm:"file_name"`
	CiGroup      string `json:"ciGroup" xorm:"ci_group"`
	CiLayer      string `json:"ciLayer" xorm:"ci_layer"`
	CiTemplate   string `json:"ciTemplate" xorm:"ci_template"`
	StateMachine string `json:"stateMachine" xorm:"state_machine"`
	SeqNo        string `json:"seqNo" xorm:"seq_no"`
}

type SysCiTypeAttrTable struct {
	Id                      string `json:"ciTypeAttrId" xorm:"id"`
	CiType                  string `json:"ciTypeId" xorm:"ci_type"`
	Name                    string `json:"propertyName" xorm:"name"`
	DisplayNameTmp          string `json:"displayName" xorm:"-"`
	DisplayName             string `json:"name" xorm:"display_name"`
	Description             string `json:"description" xorm:"description"`
	Status                  string `json:"status" xorm:"status"`
	InputType               string `json:"inputType" xorm:"input_type"`
	DataType                string `json:"propertyType" xorm:"data_type"`
	DataLength              int    `json:"length" xorm:"data_length"`
	TextValidate            string `json:"regularExpressionRule" xorm:"text_validate"`
	RefCiType               string `json:"referenceId" xorm:"ref_ci_type"`
	RefName                 string `json:"referenceName" xorm:"ref_name"`
	RefType                 string `json:"referenceType" xorm:"ref_type"`
	RefFilter               string `json:"referenceFilter" xorm:"ref_filter"`
	RefUpdateStateValidate  string `json:"refUpdateStateValidate" xorm:"ref_update_state_validate"`
	RefConfirmStateValidate string `json:"refConfirmStateValidate" xorm:"ref_confirm_state_validate"`
	SelectList              string `json:"selectList" xorm:"select_list"`
	UiSearchOrder           int    `json:"uiSearchOrder" xorm:"ui_search_order"`
	UiFormOrder             int    `json:"uiFormOrder" xorm:"ui_form_order"`
	UniqueConstraint        string `json:"uniqueConstraint" xorm:"unique_constraint"`
	UiNullable              string `json:"uiNullable" xorm:"ui_nullable"`
	Nullable                string `json:"nullable" xorm:"nullable"`
	Editable                string `json:"editable" xorm:"editable"`
	DisplayByDefault        string `json:"displayByDefault" xorm:"display_by_default"`
	PermissionUsage         string `json:"permissionUsage" xorm:"permission_usage"`
	ResetOnEdit             string `json:"resetOnEdit" xorm:"reset_on_edit"`
	Source                  string `json:"source" xorm:"source"`
	Customizable            string `json:"customizable" xorm:"customizable"`
	AutofillAble            string `json:"autofillable" xorm:"autofillable"`
	AutofillRule            string `json:"autoFillRule" xorm:"autofill_rule"`
	AutofillType            string `json:"autoFillType" xorm:"autofill_type"`
	EditGroupControl        string `json:"editGroupControl" xorm:"edit_group_control"`
	EditGroupValues         string `json:"editGroupValues" xorm:"edit_group_value"`
	ExtRefEntity            string `json:"extRefEntity" xorm:"ext_ref_entity"`
}

type TransExportTable struct {
	Id                      string   `json:"id" xorm:"id"`
	Business                string   `json:"business" xorm:"business"`
	BusinessName            string   `json:"businessName" xorm:"business_name"`
	Environment             string   `json:"environment" xorm:"environment"`
	EnvironmentName         string   `json:"environmentName" xorm:"environment_name"`
	Status                  string   `json:"status" xorm:"status"`
	OutputUrl               string   `json:"outputUrl" xorm:"output_url"`
	CreatedUser             string   `json:"createdUser" xorm:"created_user"`
	CreatedTime             string   `json:"createdTime" xorm:"created_time"`
	UpdatedUser             string   `json:"updatedUser" xorm:"updated_user"`
	UpdatedTime             string   `json:"updatedTime" xorm:"updated_time"`
	AssociationSystems      []string `json:"associationSystems" xorm:"-"`      // 关联系统
	AssociationTechProducts []string `json:"associationTechProducts" xorm:"-"` // 关联产品
	LastConfirmTime         string   `json:"lastConfirmTime" xorm:"last_confirm_time"`
}

type TransExportDetailTable struct {
	Id          string  `json:"id" xorm:"id"`
	TransExport *string `json:"transExport" xorm:"trans_export"`
	Name        string  `json:"name" xorm:"name"`
	AnalyzeData *string `json:"analyzeData" xorm:"analyze_data"`
	Step        int     `json:"step" xorm:"step"`
	Status      string  `json:"status" xorm:"status"`
	Input       string  `json:"input" xorm:"input"`
	Output      string  `json:"output" xorm:"output"`
	ErrorMsg    string  `json:"errorMsg" xorm:"error_msg"`
	StartTime   string  `json:"startTime" xorm:"start_time"`
	EndTime     string  `json:"endTime" xorm:"end_time"`
}

type TransExportAnalyzeDataTable struct {
	Id           string  `json:"id" xorm:"id"`
	TransExport  *string `json:"transExport" xorm:"trans_export"`
	Source       string  `json:"source" xorm:"source"`
	DataType     string  `json:"dataType" xorm:"data_type"`
	DataTypeName string  `json:"dataTypeName" xorm:"data_type_name"`
	Data         string  `json:"data" xorm:"data"`
	DataLen      int     `json:"dataLen" xorm:"data_len"`
	ErrorMsg     string  `json:"errorMsg" xorm:"error_msg"`
	StartTime    string  `json:"startTime" xorm:"start_time"`
	EndTime      string  `json:"endTime" xorm:"end_time"`
}

func (TransExportDetailTable) TableName() string {
	return "trans_export_detail"
}

type TransExportDetailTableSort []*TransExportDetailTable

func (q TransExportDetailTableSort) Len() int {
	return len(q)
}

func (q TransExportDetailTableSort) Less(i, j int) bool {
	return q[i].Step-q[j].Step < 0
}

func (q TransExportDetailTableSort) Swap(i, j int) {
	q[i], q[j] = q[j], q[i]
}

type TransDataVariableConfig struct {
	BusinessCiType             string   `json:"businessCiType"`
	EnvCiType                  string   `json:"envCiType"`
	NexusUrl                   string   `json:"nexusUrl"`
	NexusUser                  string   `json:"nexusUser"`
	NexusPwd                   string   `json:"nexusPwd"`
	NexusRepo                  string   `json:"nexusRepo"`
	ArtifactInstanceCiTypeList []string `json:"artifactInstanceCiTypeList"`
	ArtifactPackageCiType      string   `json:"artifactPackageCiType"`
	SystemCiType               string   `json:"systemCiType"`
	TechProductCiType          string   `json:"techProductCiType"`
	ArtifactUnitDesignCiType   string   `json:"artifactUnitDesignCiType"`
	CiGroupAppDeploy           string   `json:"ciGroupAppDeploy"`
	BusinessToSystemExpr       string   `json:"businessToSystemExpr"`
	EnvToSystemExpr            string   `json:"envToSystemExpr"`
	BackwardSearchAttrList     []string `json:"backwardSearchAttrList"`
	IgnoreSearchAttrList       []string `json:"ignoreSearchAttrList"`
	ResetEmptyAttrList         []string `json:"resetEmptyAttrList"`
}

type CiTypeData struct {
	CiType       *SysCiTypeTable              `json:"ciType"`
	Attributes   []*SysCiTypeAttrTable        `json:"attributes"`
	DataMap      map[string]map[string]string `json:"dataMap"` // key=ciDataGuid value=ciDataColumnKV
	DataChainMap map[string]string            `json:"dataChainMap"`
}

type CiTypeDataFilter struct {
	CiType    string   `json:"ciType"`
	Condition string   `json:"condition"` // in | notIn
	GuidList  []string `json:"guidList"`
}

type QueryBusinessParam struct {
	ID          string `json:"id"`          // 产品ID
	DisplayName string `json:"displayName"` // 业务名称
	QueryMode   string `json:"queryMode"`   // env 查询环境
}

type CreateExportParam struct {
	PIds            []string `json:"pIds"`    // 产品ID
	PNames          []string `json:"pNames"`  // 产品名称
	Env             string   `json:"env"`     // 环境
	EnvName         string   `json:"envName"` // 环境名称
	LastConfirmTime string   `json:"lastConfirmTime"`
}

type UpdateExportParam struct {
	TransExportId   string   `json:"transExportId"` // 导出Id
	PIds            []string `json:"pIds"`          // 产品ID
	PNames          []string `json:"pNames"`        // 产品名称
	Env             string   `json:"env"`           // 环境
	EnvName         string   `json:"envName"`       // 环境名称
	LastConfirmTime string   `json:"lastConfirmTime"`
}

type DataTransExportParam struct {
	TransExportId          string           `json:"transExportId"`          // 导出Id
	Roles                  []string         `json:"roles"`                  // 角色
	WorkflowIds            []string         `json:"workflowIds"`            // 编排Ids
	BatchExecutionIds      []string         `json:"batchExecutionIds"`      // 批量执行Ids
	RequestTemplateIds     []string         `json:"requestTemplateIds"`     // 模版Ids
	ExportComponentLibrary bool             `json:"exportComponentLibrary"` // 是否导出组件库
	ExportDashboardMap     map[int][]string // 导出看板信息
}

type TransExportHistoryParam struct {
	Id            string   `json:"id"`
	Status        []string `json:"status"`
	Business      []string `json:"business"`  //产品
	Operators     []string `json:"operators"` //操作人
	StartIndex    int      `json:"startIndex"`
	PageSize      int      `json:"pageSize"`
	ExecTimeStart string   `json:"execTimeStart" ` // 执行时间-开始时间
	ExecTimeEnd   string   `json:"ExecTimeEnd" `   // 执行时间-结束时间
}

type TransExportHistoryOptions struct {
	BusinessList []*Business `json:"businessList"` // 导出产品
	Operators    []string    `json:"operators"`    //操作人
}

type Business struct {
	BusinessId   string `json:"businessId"`   // 导出产品ID
	BusinessName string `json:"businessName"` // 导出产品名称
}

type StepExportParam struct {
	Ctx           context.Context
	Path          string
	TransExportId string
	StartTime     string
	Step          TransExportStep
	Input         interface{}
	Data          interface{}
	ExportData    interface{}
}

type RequestTemplateExport struct {
	RequestTemplate      RequestTemplateDto          `json:"requestTemplate"`
	FormTemplate         interface{}                 `json:"formTemplate"`
	FormItemTemplate     interface{}                 `json:"formItemTemplate"`
	RequestTemplateRole  []*RequestTemplateRoleTable `json:"requestTemplateRole"`
	TaskTemplate         interface{}                 `json:"taskTemplate"`
	TaskHandleTemplate   interface{}                 `json:"taskHandleTemplate"`
	RequestTemplateGroup interface{}                 `json:"requestTemplateGroup"`
}

type RequestTemplateRoleTable struct {
	Id              string `json:"id"`
	RequestTemplate string `json:"requestTemplate"`
	Role            string `json:"role"`
	RoleType        string `json:"roleType"`
}

type RequestTemplateSimpleQuery struct {
	RequestTemplateDto
	MGMTRoles []*RoleTable `json:"mgmtRoles"`
	USERoles  []*RoleTable `json:"useRoles"`
}

type RoleTable struct {
	Id          string `json:"id"`
	DisplayName string `json:"displayName"`
	UpdatedTime string `json:"updatedTime"`
	CoreId      string `json:"coreId"`
	Email       string `json:"email"`
}

type RequestTemplateDto struct {
	Id               string `json:"id"`
	Group            string `json:"group"`
	Name             string `json:"name"`
	Description      string `json:"description"`
	FormTemplate     string `json:"formTemplate"`
	Tags             string `json:"tags"`
	Status           string `json:"status"`
	RecordId         string `json:"recordId"`
	Version          string `json:"version"`
	ConfirmTime      string `json:"confirmTime"`
	PackageName      string `json:"packageName"`
	EntityName       string `json:"entityName"`
	ProcDefKey       string `json:"procDefKey"`
	ProcDefId        string `json:"procDefId"`
	ProcDefName      string `json:"procDefName"`
	ProcDefVersion   string `json:"procDefVersion"`
	CreatedBy        string `json:"createdBy"`
	CreatedTime      string `json:"createdTime"`
	UpdatedBy        string `json:"updatedBy"`
	UpdatedTime      string `json:"updatedTime"`
	EntityAttrs      string `json:"entityAttrs"`
	ExpireDay        int    `json:"expireDay"`
	Handler          string `json:"handler"`
	DelFlag          int    `json:"delFlag"`
	Type             int    `json:"type"`             // 请求类型,0表示请求,1表示发布
	OperatorObjType  string `json:"operatorObjType"`  // 操作对象类型
	ParentId         string `json:"parentId"`         // 父类ID
	ApproveBy        string `json:"approveBy"`        // 模板发布审批人
	CheckSwitch      bool   `json:"pendingSwitch"`    // 是否加入确认定版流程
	CheckRole        string `json:"pendingRole"`      // 定版角色
	CheckExpireDay   int    `json:"pendingExpireDay"` // 定版时效
	CheckHandler     string `json:"pendingHandler"`   // 定版处理人
	ConfirmSwitch    bool   `json:"confirmSwitch"`    // 是否加入确认流程
	ConfirmExpireDay int    `json:"confirmExpireDay"` // 确认过期时间
	BackDesc         string `json:"rollbackDesc"`     // 退回理由
}

type TransExportDetail struct {
	TransExport            *TransExportTable    `json:"transExport"`
	CmdbCI                 []*CommonNameCount   `json:"cmdbCI"`
	CmdbView               []*CommonNameCreator `json:"cmdbView"`
	CmdbViewCount          int                  `json:"cmdbViewCount"`
	CmdbReportForm         []*CommonNameCreator `json:"cmdbReportForm"`
	CmdbReportFormCount    int                  `json:"cmdbReportFormCount"`
	Roles                  *CommonOutput        `json:"roles"`
	Workflows              *CommonOutput        `json:"workflows"`
	BatchExecution         *CommonOutput        `json:"batchExecutions"`
	RequestTemplates       *CommonOutput        `json:"requestTemplates"`
	ComponentLibrary       *CommonOutput        `json:"componentLibrary"`
	ExportComponentLibrary bool                 `json:"exportComponentLibrary"` // 是否导出组件库
	Artifacts              *CommonOutput        `json:"artifacts"`
	Monitor                *CommonOutput        `json:"monitor"`
	Plugins                *CommonOutput        `json:"plugins"`
	Cmdb                   *CommonOutput        `json:"cmdb"`
	CreateAndUploadFile    *CommonOutput        `json:"createAndUploadFile"`
}

type CommonNameCount struct {
	Name  string `json:"name"`
	Count int    `json:"count"`
	Group string `json:"group"`
}

type PluginPackageCount struct {
	Name               string `json:"name"`
	PluginInterfaceNum int    `json:"pluginInterfaceNum"`
	SystemVariableNum  int    `json:"systemVariableNum"`
}

type CommonNameCreator struct {
	Name    string `json:"name"`
	Creator string `json:"creator"`
}

type CommonNameUser struct {
	Name        string `json:"name"`
	CreatedUser string `json:"createdUser"`
}

type CommonOutput struct {
	Ids    []string    `json:"ids"`
	Status string      `json:"status"`
	Output interface{} `json:"data"`
	ErrMsg string      `json:"errMsg"`
}

type ExportComponentLibrary struct {
	CommonOutput
	ExportComponentLibrary bool `json:"exportComponentLibrary"`
}

type AnalyzeTransParam struct {
	EndpointList     []string `json:"endpointList"`
	ServiceGroupList []string `json:"serviceGroupList"`
}

type AnalyzeTransResp struct {
	HttpResponseMeta
	Data *AnalyzeTransData `json:"data"`
}

type AnalyzeTransData struct {
	MonitorType               []string `json:"monitorType"`
	EndpointGroup             []string `json:"endpointGroup"`
	CustomMetricServiceGroup  []string `json:"customMetricServiceGroup"`
	CustomMetricEndpointGroup []string `json:"customMetricEndpointGroup"`
	CustomMetricMonitorType   []string `json:"CustomMetricMonitorType"`
	LogMonitorServiceGroup    []string `json:"logMonitorServiceGroup"`
	LogMonitorTemplate        []string `json:"logMonitorTemplate"`
	StrategyServiceGroup      []string `json:"strategyServiceGroup"`
	StrategyEndpointGroup     []string `json:"strategyEndpointGroup"`
	LogKeywordServiceGroup    []string `json:"logKeywordServiceGroup"`
	DashboardIdList           []string `json:"dashboardIdList"`
	Endpoint                  []string `json:"endpoint"`
	ServiceGroup              []string `json:"serviceGroup"`
}

type SysReportTable struct {
	Id         string `json:"id" xorm:"id"`
	Name       string `json:"name" xorm:"name"`
	CiType     string `json:"ciType" xorm:"ci_type"`
	CreateTime string `json:"createTime" xorm:"create_time"`
	CreateUser string `json:"createUser" xorm:"create_user"`
	UpdateTime string `json:"updateTime" xorm:"update_time"`
	UpdateUser string `json:"updateUser" xorm:"update_user"`
}

type SysViewTable struct {
	Id            string    `json:"viewId" xorm:"id"`
	Name          string    `json:"name" xorm:"name"`
	Report        string    `json:"report" xorm:"report"`
	Editable      string    `json:"editable" xorm:"editable"`
	SuportVersion string    `json:"suportVersion" xorm:"suport_version"`
	Multiple      string    `json:"multiple" xorm:"multiple"`
	CreateTime    time.Time `json:"createTime" xorm:"create_time"`
	CreateUser    string    `json:"createUser" xorm:"create_user"`
	UpdateTime    time.Time `json:"updateTime" xorm:"update_time"`
	UpdateUser    string    `json:"updateUser" xorm:"update_user"`
	FilterAttr    string    `json:"filterAttr" xorm:"filter_attr"`
	FilterValue   string    `json:"filterValue" xorm:"filter_value"`
}

type DataTransPluginExportData struct {
	PluginPackageName  string `json:"PluginPackageName" xorm:"-"`
	PluginPackageId    string `json:"PluginPackageId" xorm:"plugin_package_id"`
	PluginInterfaceNum int    `json:"PluginInterfaceNum" xorm:"plugin_interface_num"`
	SystemVariableNum  int    `json:"SystemVariableNum" xorm:"system_variable_num"`
	Source             string `json:"Source" json:"source"`
}

type AnalyzeArtifactDisplayData struct {
	UnitDesign     string              `json:"unitDesign"`
	UnitDesignName string              `json:"unitDesignName"`
	ArtifactRows   []map[string]string `json:"artifactRows"`
	ArtifactLen    int                 `json:"artifactLen"`
}

type TransDataImportNexusConfig struct {
	NexusUrl  string `json:"nexusUrl"`
	NexusUser string `json:"nexusUser"`
	NexusPwd  string `json:"nexusPwd"`
	NexusRepo string `json:"nexusRepo"`
}

type ExportMetricListDto struct {
	MonitorTypeMetricList   []string
	ServiceGroupMetricList  []string
	EndpointGroupMetricList []string
	MetricPath              string
	ServiceGroupPath        string
	EndpointGroupPath       string
	Token                   string
}

type SysBaseKeyCodeTable struct {
	Id          string `json:"codeId" xorm:"id"`
	CatId       string `json:"catId" xorm:"cat_id"`
	Code        string `json:"code" xorm:"code"`
	Value       string `json:"value" xorm:"value"`
	Description string `json:"codeDescription" xorm:"description"`
	SeqNo       int    `json:"seqNo" xorm:"seq_no"`
	Status      string `json:"status" xorm:"status"`
}

type CMDBHistoryTable struct {
	Id          int64     `xorm:"id"`
	FromGuid    string    `xorm:"from_guid"`
	ToGuid      string    `xorm:"to_guid"`
	HistoryToId int64     `xorm:"history_to_id"`
	HistoryTime time.Time `xorm:"history_time"`
}

type CMDBMultiRefRow struct {
	FromCiType string
	ToCiType   string
	FromGuid   string
	ToGuid     string
}
