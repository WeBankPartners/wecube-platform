package models

type TransImportTable struct {
	Id              string `json:"id" xorm:"id"`
	ImportUrl       string `json:"importUrl" xorm:"import_url"`
	Business        string `json:"business" xorm:"business"`
	BusinessName    string `json:"businessName" xorm:"business_name"`
	Environment     string `json:"environment" xorm:"environment"`
	EnvironmentName string `json:"environmentName" xorm:"environment_name"`
	Status          string `json:"status" xorm:"status"`
	OutputUrl       string `json:"outputUrl" xorm:"output_url"`
	CreatedUser     string `json:"createdUser" xorm:"created_user"`
	CreatedTime     string `json:"createdTime" xorm:"created_time"`
	UpdatedUser     string `json:"updatedUser" xorm:"updated_user"`
	UpdatedTime     string `json:"updatedTime" xorm:"updated_time"`
}

type TransImportAction struct {
	Id                string  `json:"id" xorm:"id"`
	TransImport       *string `json:"transImport" xorm:"trans_import"`
	TransImportDetail string  `json:"transImportDetail" xorm:"trans_import_detail"`
	Action            string  `json:"action" xorm:"action"`
	ErrorMsg          string  `json:"errorMsg" xorm:"error_msg"`
	CreatedUser       string  `json:"createdUser" xorm:"created_user"`
	CreatedTime       string  `json:"createdTime" xorm:"created_time"`
}

type TransImportDetail struct {
	Id          string  `json:"id" xorm:"id"`
	TransImport *string `json:"transImport" xorm:"trans_import"`
	Name        string  `json:"name" xorm:"name"`
	Step        int     `json:"step" xorm:"step"`
	Status      string  `json:"status" xorm:"status"`
	Input       string  `json:"input" xorm:"input"`
	Output      string  `json:"output" xorm:"output"`
	ErrorMsg    string  `json:"errorMsg" xorm:"error_msg"`
	StartTime   string  `json:"startTime" xorm:"start_time"`
	EndTime     string  `json:"endTime" xorm:"end_time"`
}

type TransImportProcExec struct {
	Id                string `json:"id" xorm:"id"`
	TransImportDetail string `json:"transImportDetail" xorm:"trans_import_detail"`
	ProcIns           string `json:"procIns" xorm:"proc_ins"`
	ExecOrder         int    `json:"execOrder" xorm:"exec_order"`
	Status            string `json:"status" xorm:"status"`
	Input             string `json:"input" xorm:"input"`
	Output            string `json:"output" xorm:"output"`
	ErrorMsg          string `json:"errorMsg" xorm:"error_msg"`
	StartTime         string `json:"startTime" xorm:"start_time"`
	EndTime           string `json:"endTime" xorm:"end_time"`
}
