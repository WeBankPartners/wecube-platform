package models

const (
	TransImportInPreparationStatus = "InPreparation"
)

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
	ProcDef           string `json:"procDef" xorm:"proc_def"`
	ProcDefKey        string `json:"procDefKey" xorm:"proc_def_key"`
	ProcDefName       string `json:"procDefName" xorm:"proc_def_name"`
	RootEntity        string `json:"rootEntity" xorm:"root_entity"`
	EntityDataId      string `json:"entityDataId" xorm:"entity_data_id"`
	EntityDataName    string `json:"entityDataName" xorm:"entity_data_name"`
	ExecOrder         int    `json:"execOrder" xorm:"exec_order"`
	Status            string `json:"status" xorm:"status"` // 状态->NotStarted(未开始)|InPreparation(准备启动中)|InProgress(执行中)|Completed(成功)|Faulted(失败)
	Input             string `json:"input" xorm:"input"`
	Output            string `json:"output" xorm:"output"`
	ErrorMsg          string `json:"errorMsg" xorm:"error_msg"`
	StartTime         string `json:"startTime" xorm:"start_time"`
	EndTime           string `json:"endTime" xorm:"end_time"`
	CreatedUser       string `json:"createdUser" xorm:"created_user"`
	CreatedTime       string `json:"createdTime" xorm:"created_time"`
	ProcInsStatus     string `json:"-" xorm:"proc_ins_status"` // 关联的编排实例状态
}

type GetBusinessListRes struct {
	Environment  map[string]string        `json:"environment"`
	BusinessList []map[string]interface{} `json:"businessList"`
}

type TransImportJobParam struct {
	TransImport          *TransImportTable         `json:"transImport"`
	Details              []*TransImportDetailTable `json:"details"`
	CurrentDetail        *TransImportDetailTable   `json:"currentDetail"`
	ImportCustomFormData *ImportCustomFormData     `json:"importCustomFormData"`
	DirPath              string                    `json:"dirPath"`  // 解压文件目录路径
	Token                string                    `json:"token"`    // token
	Language             string                    `json:"language"` // language
	Operator             string                    `json:"operator"`
}

type CallTransImportActionParam struct {
	TransImportId        string                `json:"transImportId"`
	TransImportDetailId  string                `json:"transImportDetailId"`
	Action               string                `json:"action"` // 操作-> start(开始)|stop(暂停)|retry(重试)|exit(终止)
	Operator             string                `json:"-"`
	ActionId             string                `json:"-"`
	ErrorMsg             string                `json:"-"`
	DirPath              string                `json:"-"`        // 解压文件目录路径
	Token                string                `json:"token"`    // token
	Language             string                `json:"language"` // language
	WebStep              int                   `json:"-"`        // 前端页面第一步
	ImportCustomFormData *ImportCustomFormData `json:"importCustomFormData"`
}

type ExecImportParam struct {
	ExportNexusUrl       string                `json:"exportNexusUrl"`
	TransImportId        string                `json:"transImportId"` // 导入Id
	Operator             string                `json:"-"`
	Token                string                `json:"token"`    // token
	Language             string                `json:"language"` // language
	WebStep              int                   `json:"step"`     // web 第几步,2,3,4,5
	ImportCustomFormData *ImportCustomFormData `json:"importCustomFormData"`
}

type CmdbData struct {
	CmdbCI              []*CommonNameCount   `json:"cmdbCI"`
	CmdbView            []*CommonNameCreator `json:"cmdbView"`
	CmdbViewCount       int                  `json:"cmdbViewCount"`
	CmdbReportForm      []*CommonNameCreator `json:"cmdbReportForm"`
	CmdbReportFormCount int                  `json:"cmdbReportFormCount"`
}

type TransImportDetail struct {
	TransImport         *TransImportTable       `json:"transExport"`
	CmdbCI              []*CommonNameCount      `json:"cmdbCI"`
	CmdbView            []*CommonNameCreator    `json:"cmdbView"`
	CmdbViewCount       int                     `json:"cmdbViewCount"`
	CmdbReportForm      []*CommonNameCreator    `json:"cmdbReportForm"`
	CmdbReportFormCount int                     `json:"cmdbReportFormCount"`
	Roles               *CommonOutput           `json:"roles"`
	Workflows           *CommonOutput           `json:"workflows"`
	BatchExecution      *CommonOutput           `json:"batchExecutions"`
	RequestTemplates    *CommonOutput           `json:"requestTemplates"`
	ComponentLibrary    *ExportComponentLibrary `json:"componentLibrary"` // 组件库
	Artifacts           *CommonOutput           `json:"artifacts"`
	MonitorBase         *CommonOutput           `json:"monitorBase"`     // 监控基础配置
	MonitorBusiness     *CommonOutput           `json:"monitorBusiness"` // 监控业务配置
	Plugins             *CommonOutput           `json:"plugins"`
	Cmdb                *CommonOutput           `json:"cmdb"`
	ProcInstance        *CommonOutput           `json:"procInstance"`     // 编排执行
	ModifyNewEnvData    *CommonOutput           `json:"modifyNewEnvData"` // 修改新环境数据
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

type TaskManResponseJson struct {
	StatusCode    string      `json:"statusCode"`
	StatusMessage string      `json:"statusMessage"`
	Data          interface{} `json:"data"`
}

type UpdateImportStatusParam struct {
	Id       string `json:"transImportId"`
	Status   string `json:"status"` // exit 终止,completed 完成
	Operator string `json:"-"`
}

type CMDBViewStructQueryResp struct {
	StatusCode    string                   `json:"statusCode"`
	StatusMessage string                   `json:"statusMessage"`
	Data          *CMDBViewStructQueryData `json:"data"`
}

type CMDBViewStructQueryData struct {
	CiType         string `json:"ciType"`
	Editable       string `json:"editable"`
	Report         string `json:"report"`
	SupportVersion string `json:"suportVersion"`
	FilterAttr     string `json:"filterAttr"`
	FilterValue    string `json:"filterValue"`
}

type QueryRequestDialect struct {
	AssociatedData map[string]string `json:"associatedData"`
	QueryMode      string            `json:"queryMode"`
}

type QueryCiDataRequestParam struct {
	Filters       []*QueryRequestFilterObj `json:"filters"`
	Dialect       *QueryRequestDialect     `json:"dialect"`
	Paging        bool                     `json:"paging"`
	Pageable      *PageInfo                `json:"pageable"`
	Sorting       *QueryRequestSorting     `json:"sorting"`
	ResultColumns []string                 `json:"resultColumns"`
}

type QueryCiDataResp struct {
	StatusCode    string               `json:"statusCode"`
	StatusMessage string               `json:"statusMessage"`
	Data          *QueryCiDataRespData `json:"data"`
}

type QueryCiDataRespData struct {
	PageInfo *PageInfo                `json:"pageInfo"`
	Contents []map[string]interface{} `json:"contents"`
}

type ConfirmCMDBViewParam struct {
	ViewId      string `json:"viewId"`
	RootCi      string `json:"rootCi"`
	ConfirmTime string `json:"confirmTime"`
}

type ConfirmCMDBViewResp struct {
	StatusCode    string      `json:"statusCode"`
	StatusMessage string      `json:"statusMessage"`
	Data          interface{} `json:"data"`
}

type ImportCustomFormData struct {
	NetworkZoneAssetId        string `json:"networkZoneAssetId"`        //网络区域-资产ID
	NetworkSubZoneAssetId     string `json:"networkSubZoneAssetId"`     //网络子区域 MGMT_APP -资产ID
	RouteTableAssetId         string `json:"routeTableAssetId"`         //路由表 默认路由表-资产ID
	BasicSecurityGroupAssetId string `json:"basicSecurityGroupAssetId"` //基础安全组 MGMT-APP -资产ID
	DataCenterRegionAssetId   string `json:"dataCenterRegionAssetId"`   //地域数据中心资产ID
	DataCenterAZ1AssetId      string `json:"dataCenterAZ1AssetId"`      //地域数据中心可用区1资产ID
	DataCenterAZ2AssetId      string `json:"dataCenterAZ2AssetId"`      //地域数据中心可用区2资产ID
	WecubeHostAssetId         string `json:"wecubeHostAssetId"`         //wecube主机的资产ID
	WecubeHostPassword        string `json:"wecubeHostPassword"`        //wecube主机的管理员密码
	WecubeHostPwd             string `json:"-"`
}

type TransDataImportConfig struct {
	NexusUrl                  string   `json:"nexusUrl"`
	NexusUser                 string   `json:"nexusUser"`
	NexusPwd                  string   `json:"nexusPwd"`
	NexusRepo                 string   `json:"nexusRepo"`
	NetworkZoneCIDR           string   `json:"networkZoneCIDR"`
	NetworkSubZoneCIDR        string   `json:"networkSubZoneCIDR"`
	RouteTableCode            string   `json:"routeTableCode"`
	BasicSecurityGroupKeyName string   `json:"basicSecurityGroupKeyName"`
	DataCenterRegionKeyName   string   `json:"dataCenterRegionKeyName"`
	DataCenterAZ1KeyName      string   `json:"dataCenterAZ1KeyName"`
	DataCenterAZ2KeyName      string   `json:"dataCenterAZ2KeyName"`
	WecubeHostCode            string   `json:"wecubeHostCode"`
	SystemCiType              string   `json:"systemCiType"`
	SystemDeployBatchAttr     string   `json:"systemDeployBatchAttr"`
	AutoConfirmViewList       []string `json:"autoConfirmViewList"`
}

type QueryImportEntityRow struct {
	Id          string `json:"id"`
	DisplayName string `json:"displayName"`
	Order       int    `json:"order"`
}

type QueryImportEntityRows []*QueryImportEntityRow

func (q QueryImportEntityRows) Len() int {
	return len(q)
}

func (q QueryImportEntityRows) Less(i, j int) bool {
	if q[i].Order == 0 && q[j].Order == 0 {
		return q[i].DisplayName < q[j].DisplayName
	}
	if q[i].Order == 0 {
		return false
	}
	return q[i].Order < q[j].Order
}

func (q QueryImportEntityRows) Swap(i, j int) {
	q[i], q[j] = q[j], q[i]
}
