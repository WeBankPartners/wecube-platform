package models

import "fmt"

const (
	GlobalProjectName = "platform"
	DateTimeFormat    = "2006-01-02 15:04:05"
	NewDateTimeFormat = "20060102150405"
	DefaultLanguage   = "en"

	// header key
	AuthorizationHeader    = "Authorization"
	TransactionIdHeader    = "transactionId"
	RequestIdHeader        = "requestId"
	DefaultHttpErrorCode   = "ERROR"
	DefaultHttpSuccessCode = "OK"
	DefaultHttpConfirmCode = "CONFIRM"
	ContinueToken          = "continueToken"
	OperationHeader        = "x-operation"
	AcceptLanguageHeader   = "Accept-Language"
	// context key
	ContextRequestBody  = "requestBody"
	ContextResponseBody = "responseBody"
	ContextOperator     = "operator"
	ContextRoles        = "roles"
	ContextAuth         = "auth"
	ContextAuthorities  = "authorities"
	ContextErrorCode    = "errorCode"
	ContextErrorKey     = "errorKey"
	ContextErrorMessage = "errorMessage"
	ContextUserId       = "userId"
	ContextApiCode      = "apiCode"

	JwtSignKey = "authJwtSecretKey"
	AESPrefix  = "{AES}"

	// table name
	TableNameBatchExec                       = "batch_execution"
	TableNameBatchExecJobs                   = "batch_exec_jobs"
	TableNameBatchExecTemplate               = "batch_execution_template"
	TableNameBatchExecTemplateRole           = "batch_execution_template_role"
	TableNameBatchExecTemplateCollect        = "batch_execution_template_collect"
	TableNamePluginConfigRoles               = "plugin_config_roles"
	TableNamePluginConfigs                   = "plugin_configs"
	TableNamePluginPackages                  = "plugin_packages"
	TableNamePluginConfigInterfaces          = "plugin_config_interfaces"
	TableNamePluginConfigInterfaceParameters = "plugin_config_interface_parameters"
	TableNameSystemVariables                 = "system_variables"
	TableNamePluginObjectMeta                = "plugin_object_meta"
	TableNamePluginObjectPropertyMeta        = "plugin_object_property_meta"

	// batch execution
	BatchExecTemplateStatusAvailable    = "available"
	BatchExecTemplateStatusUnauthorized = "unauthorized"
	BatchExecTmplPublishStatusDraft     = "draft"
	BatchExecTmplPublishStatusPublished = "published"
	BatchExecErrorCodeSucceed           = "0"
	BatchExecErrorCodeFailed            = "1"
	BatchExecErrorCodePending           = "2"
	BatchExecErrorCodeDangerousBlock    = "3"
	DefaultKeepBatchExecDays            = 365
	BatchExecEncryptPrefix              = "encrypt "

	// permission type
	PermissionTypeMGMT = "MGMT"
	PermissionTypeUSE  = "USE"

	// plugin status
	PluginStatusDisabled = "DISABLED"
	PluginStatusEnabled  = "ENABLED"

	// id prefix
	IdPrefixSysVar             = "sys_var_"
	IdPrefixPluCfgRol          = "p_conf_rol_"
	IdPrefixPluCfg             = "p_config_"
	IdPrefixPluCfgItf          = "p_conf_inf_"
	IdPrefixPluCfgItfPar       = "p_conf_inf_param_"
	IdPrefixPluObjMeta         = "p_obj_meta_"
	IdPrefixPluObjPropertyMeta = "p_obj_prop_meta_"
	NewOidDataPrefix           = "OID_"

	// system variable
	SysVarMailSender   = "PLATFORM_MAIL_SENDER"
	SysVarMailServer   = "PLATFORM_MAIL_SERVER"
	SysVarMailPassword = "PLATFORM_MAIL_PWD"
	SysVarMailSSL      = "PLATFORM_MAIL_SSL"
	SysVarSystemSource = "system"
	SysVarEncryptSeed  = "ENCRYPT_SEED"

	// proc event status
	ProcEventStatusCreated = "created"
	ProcEventStatusPending = "pending"
	ProcEventStatusDone    = "done"
	ProcEventStatusFail    = "fail"

	SensitiveDisplay = "******"

	HEADER_BUSINESS_ID = "BusinessId" // 业务流水号
	HEADER_REQUEST_ID  = "RequestId"  // 交易流水号
)

var (
	UrlPrefix = fmt.Sprintf("/%s", GlobalProjectName)
)

// TransExportStatus 导出状态枚举
type TransExportStatus string

const (
	TransExportStatusNotStart TransExportStatus = "notStart"
	TransExportStatusStart    TransExportStatus = "start"
	TransExportStatusDoing    TransExportStatus = "doing"
	TransExportStatusSuccess  TransExportStatus = "success"
	TransExportStatusFail     TransExportStatus = "fail"
)

// TransImportStatus 导入状态枚举
type TransImportStatus string

const (
	TransImportStatusNotStart TransImportStatus = "notStart"
	TransImportStatusStart    TransImportStatus = "start"
	TransImportStatusDoing    TransImportStatus = "doing"
	TransImportStatusSuccess  TransImportStatus = "success"
	TransImportStatusFail     TransImportStatus = "fail"
	TransImportStatusExit     TransImportStatus = "exit"
	TransImportStatusSuspend  TransImportStatus = "suspend"
)

// TransExportStep 导出步骤
type TransExportStep int

const (
	TransExportStepRole                TransExportStep = 1  // 导出角色
	TransExportStepRequestTemplate     TransExportStep = 2  // 导出请求模版
	TransExportStepComponentLibrary    TransExportStep = 3  // 导出表单组件库
	TransExportStepWorkflow            TransExportStep = 4  // 导出编排
	TransExportStepBatchExecution      TransExportStep = 5  // 导出批量执行
	TransExportStepPluginConfig        TransExportStep = 6  // 导出插件配置
	TransExportStepCmdb                TransExportStep = 7  // 导出CMDB
	TransExportStepArtifacts           TransExportStep = 8  // 导出物料包
	TransExportStepMonitor             TransExportStep = 9  // 导出监控
	TransExportUIData                  TransExportStep = 10 // 导出环境产品等页面展示数据
	TransExportStepCreateAndUploadFile TransExportStep = 11 // 生成文件上传
)

// TransExportAnalyzeSource 分析来源
type TransExportAnalyzeSource string

const (
	TransExportAnalyzeSourceWeCmdb        TransExportAnalyzeSource = "wecmdb"         // CMDB
	TransExportAnalyzeSourceWeCmdbGroup   TransExportAnalyzeSource = "wecmdb_group"   // CMDB Group
	TransExportAnalyzeSourceWeCmdbReport  TransExportAnalyzeSource = "wecmdb_report"  // report
	TransExportAnalyzeSourceWeCmdbView    TransExportAnalyzeSource = "wecmdb_view"    // view
	TransExportAnalyzeSourceMonitor       TransExportAnalyzeSource = "monitor"        // monitor
	TransExportAnalyzeSourceArtifact      TransExportAnalyzeSource = "artifact"       // artifact
	TransExportAnalyzeSourcePluginPackage TransExportAnalyzeSource = "plugin_package" // plugin_package
)

type TransExportAnalyzeMonitorDataType string

const (
	TransExportAnalyzeMonitorDataTypeMonitorType               TransExportAnalyzeMonitorDataType = "monitor_type"
	TransExportAnalyzeMonitorDataTypeEndpointGroup             TransExportAnalyzeMonitorDataType = "endpoint_group"               // 对象组
	TransExportAnalyzeMonitorDataTypeCustomMetricServiceGroup  TransExportAnalyzeMonitorDataType = "custom_metric_service_group"  // 指标列表-层级对象
	TransExportAnalyzeMonitorDataTypeCustomMetricEndpointGroup TransExportAnalyzeMonitorDataType = "custom_metric_endpoint_group" // 指标列表-对象组
	TransExportAnalyzeMonitorDataTypeCustomMetricMonitorType   TransExportAnalyzeMonitorDataType = "custom_metric_monitor_type"   // 指标列表-基础指标
	TransExportAnalyzeMonitorDataTypeLogMonitorServiceGroup    TransExportAnalyzeMonitorDataType = "log_monitor_service_group"
	TransExportAnalyzeMonitorDataTypeLogMonitorTemplate        TransExportAnalyzeMonitorDataType = "log_monitor_template"
	TransExportAnalyzeMonitorDataTypeStrategyServiceGroup      TransExportAnalyzeMonitorDataType = "strategy_service_group"
	TransExportAnalyzeMonitorDataTypeStrategyEndpointGroup     TransExportAnalyzeMonitorDataType = "strategy_endpoint_group"
	TransExportAnalyzeMonitorDataTypeLogKeywordServiceGroup    TransExportAnalyzeMonitorDataType = "logKeyword_service_group"
	TransExportAnalyzeMonitorDataTypeDashboard                 TransExportAnalyzeMonitorDataType = "dashboard"
	TransExportAnalyzeMonitorDataTypeServiceGroup              TransExportAnalyzeMonitorDataType = "service_group" // 层级对象
)

// TransImportStep 导入步骤
type TransImportStep int

const (
	TransImportStepRole                 TransImportStep = 1  // 导入角色
	TransImportStepCmdb                 TransImportStep = 2  // 导入CMDB
	TransImportStepPluginConfig         TransImportStep = 3  // 导入插件配置
	TransImportStepWorkflow             TransImportStep = 4  // 导入编排
	TransImportStepBatchExecution       TransImportStep = 5  // 导入批量执行
	TransImportStepArtifacts            TransImportStep = 6  // 导入物料包
	TransImportStepMonitorBase          TransImportStep = 7  // 导入监控基础配置
	TransImportStepComponentLibrary     TransImportStep = 8  // 导入表单组件库
	TransImportStepRequestTemplate      TransImportStep = 9  // 导入请求模版
	TransImportStepWebBaseImportSuccess TransImportStep = 10 // web第二步导入成功,主要记录web步骤用,后台不做业务
	TransImportStepModifyNewEnvData     TransImportStep = 11 // 修改新环境数据
	TransImportStepInitWorkflow         TransImportStep = 12 // 执行基础编排初始化
	TransImportStepMonitorBusiness      TransImportStep = 13 // 导入监控业务配置
)

// TransImportAction 导入操作
type TransImportAction string

const (
	TransImportActionExit    TransImportAction = "exit"
	TransImportActionSuspend TransImportAction = "suspend"
	TransImportActionRestore TransImportAction = "restore"
	TransImportActionRetry   TransImportAction = "retry"
	TransImportActionStart   TransImportAction = "start"
)

const (
	TransImportTmpDir           = "/tmp/trans_import/%s"
	TransArtifactPackageDirName = "artifact_packages"
	TransArtifactNewPackageName = "pushReturnName"
)

// ImportWebDisplayStep  web导入步骤
type ImportWebDisplayStep int

const (
	ImportWebDisplayStepTwo   ImportWebDisplayStep = 2
	ImportWebDisplayStepThree ImportWebDisplayStep = 3
	ImportWebDisplayStepFour  ImportWebDisplayStep = 4
	ImportWebDisplayStepFive  ImportWebDisplayStep = 5
)
