package models

type TransImportTable struct {
	Id                 string `json:"id" xorm:"id"`
	InputUrl           string `json:"inputUrl" xorm:"input_url"`
	Business           string `json:"business" xorm:"business"`
	BusinessName       string `json:"businessName" xorm:"business_name"`
	Environment        string `json:"environment" xorm:"environment"`
	EnvironmentName    string `json:"environmentName" xorm:"environment_name"`
	Status             string `json:"status" xorm:"status"`
	AssociationSystem  string `json:"associationSystem" xorm:"association_system"`
	AssociationProduct string `json:"associationProduct" xorm:"association_product"`
	CreatedUser        string `json:"createdUser" xorm:"created_user"`
	CreatedTime        string `json:"createdTime" xorm:"created_time"`
	UpdatedUser        string `json:"updatedUser" xorm:"updated_user"`
	UpdatedTime        string `json:"updatedTime" xorm:"updated_time"`
	WebStep            int    `json:"step"  xorm:"-"` // 通知web执行中的哪一步,2,3,4
}

type TransImportActionTable struct {
	Id                string  `json:"id" xorm:"id"`
	TransImport       *string `json:"transImport" xorm:"trans_import"`
	TransImportDetail string  `json:"transImportDetail" xorm:"trans_import_detail"`
	Action            string  `json:"action" xorm:"action"`
	ErrorMsg          string  `json:"errorMsg" xorm:"error_msg"`
	CreatedUser       string  `json:"createdUser" xorm:"created_user"`
	CreatedTime       string  `json:"createdTime" xorm:"created_time"`
}

type TransImportDetailTable struct {
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

type TransImportProcExecTable struct {
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

type GetBusinessListRes struct {
	Environment  map[string]string        `json:"environment"`
	BusinessList []map[string]interface{} `json:"businessList"`
}

type TransImportJobParam struct {
	TransImport   *TransImportTable         `json:"transImport"`
	Details       []*TransImportDetailTable `json:"details"`
	CurrentDetail *TransImportDetailTable   `json:"currentDetail"`
	DirPath       string                    `json:"dirPath"`  // 解压文件目录路径
	Token         string                    `json:"token"`    // token
	Language      string                    `json:"language"` // language
	Operator      string                    `json:"operator"`
}

type CallTransImportActionParam struct {
	TransImportId       string `json:"transImportId"`
	TransImportDetailId string `json:"transImportDetailId"`
	Action              string `json:"action"` // 操作-> start(开始)|stop(暂停)|retry(重试)|cancel(取消)
	Operator            string `json:"-"`
	ActionId            string `json:"-"`
	ErrorMsg            string `json:"-"`
	DirPath             string `json:"-"`        // 解压文件目录路径
	Token               string `json:"token"`    // token
	Language            string `json:"language"` // language
	WebStep             int    `json:"-"`        // 前端页面第一步
}

type ExecImportParam struct {
	ExportNexusUrl string `json:"exportNexusUrl"`
	TransImportId  string `json:"transImportId"` // 导入Id
	Operator       string `json:"-"`
	Token          string `json:"token"`    // token
	Language       string `json:"language"` // language
	WebStep        int    `json:"step"`     // web 第几步,2,3,4
}

type CmdbData struct {
	CmdbCI              []*CommonNameCount   `json:"cmdbCI"`
	CmdbView            []*CommonNameCreator `json:"cmdbView"`
	CmdbViewCount       int                  `json:"cmdbViewCount"`
	CmdbReportForm      []*CommonNameCreator `json:"cmdbReportForm"`
	CmdbReportFormCount int                  `json:"cmdbReportFormCount"`
}

type TransImportDetail struct {
	TransImport            *TransImportTable    `json:"transExport"`
	CmdbCI                 []*CommonNameCount   `json:"cmdbCI"`
	CmdbView               []*CommonNameCreator `json:"cmdbView"`
	CmdbViewCount          int                  `json:"cmdbViewCount"`
	CmdbReportForm         []*CommonNameCreator `json:"cmdbReportForm"`
	CmdbReportFormCount    int                  `json:"cmdbReportFormCount"`
	Roles                  *CommonOutput        `json:"roles"`
	Workflows              *CommonOutput        `json:"workflows"`
	BatchExecution         *CommonOutput        `json:"batchExecutions"`
	RequestTemplates       *CommonOutput        `json:"requestTemplates"`
	ExportComponentLibrary bool                 `json:"exportComponentLibrary"` // 是否导出组件库
	Artifacts              *CommonOutput        `json:"artifacts"`
	MonitorBase            *CommonOutput        `json:"monitorBase"`     // 监控基础配置
	MonitorBusiness        *CommonOutput        `json:"monitorBusiness"` // 监控业务配置
	InitWorkflow           *CommonOutput        `json:"initWorkflow"`    // 初始化编排
	Plugins                *CommonOutput        `json:"plugins"`
	Cmdb                   *CommonOutput        `json:"cmdb"`
}

type TransImportHistoryParam struct {
	Id            string   `json:"id"`
	Status        []string `json:"status"`
	Business      []string `json:"business"`  //产品
	Operators     []string `json:"operators"` //操作人
	StartIndex    int      `json:"startIndex"`
	PageSize      int      `json:"pageSize"`
	ExecTimeStart string   `json:"execTimeStart" ` // 执行时间-开始时间
	ExecTimeEnd   string   `json:"ExecTimeEnd" `   // 执行时间-结束时间
}

type RequestTemplateImportResponse struct {
	StatusCode    string      `json:"statusCode"`
	StatusMessage string      `json:"statusMessage"`
	Data          interface{} `json:"data"`
}
