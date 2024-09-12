package models

type AnalyzeDataTransParam struct {
	TransExportId string   `json:"transExportId"`
	Business      []string `json:"business"`
	Env           string   `json:"env"`
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
}

type TransExportTable struct {
	Id          string `json:"id" xorm:"id"`
	Business    string `json:"business" xorm:"business"`
	Environment string `json:"environment" xorm:"environment"`
	Status      string `json:"status" xorm:"status"`
	OutputUrl   string `json:"outputUrl" xorm:"output_url"`
	CreatedUser string `json:"createdUser" xorm:"created_user"`
	CreatedTime string `json:"createdTime" xorm:"created_time"`
	UpdatedUser string `json:"updatedUser" xorm:"updated_user"`
	UpdatedTime string `json:"updatedTime" xorm:"updated_time"`
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

func (TransExportDetailTable) TableName() string {
	return "trans_export_detail"
}

type TransDataVariableConfig struct {
	BusinessCiType string `json:"businessCiType"`
	EnvCiType      string `json:"envCiType"`
	NexusUrl       string `json:"nexusUrl"`
	NexusUser      string `json:"nexusUser"`
	NexusPwd       string `json:"nexusPwd"`
}

type CiTypeData struct {
	CiType     *SysCiTypeTable              `json:"ciType"`
	Attributes []*SysCiTypeAttrTable        `json:"attributes"`
	DataMap    map[string]map[string]string `json:"dataMap"` // key=ciDataGuid value=ciDataColumnKV
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
	PIds []string `json:"pIds"` //产品ID
	Env  string   `json:"env"`  //环境
}

type DataTransExportParam struct {
	TransExportId      string   `json:"transExportId"`      // 导出Id
	Roles              []string `json:"roles"`              // 角色
	WorkflowIds        []string `json:"workflowIds"`        // 编排Ids
	BatchExecutionIds  []string `json:"batchExecutionIds"`  // 批量执行Ids
	RequestTemplateIds []string `json:"requestTemplateIds"` // 模版Ids
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
	Business  []string `json:"business"`  // 导出产品
	Operators []string `json:"operators"` //操作人
}
