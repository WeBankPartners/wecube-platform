package models

import "fmt"

const (
	GlobalProjectName = "platform"
	DateTimeFormat    = "2006-01-02 15:04:05"

	// header key
	AuthorizationHeader    = "Authorization"
	TransactionIdHeader    = "transactionId"
	RequestIdHeader        = "requestId"
	DefaultHttpErrorCode   = "ERROR"
	DefaultHttpSuccessCode = "OK"
	ContinueTokenHeader    = "continueToken"
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

	JwtSignKey = "authJwtSecretKey"
	AESPrefix  = "{AES}"

	// table name
	TableNameBatchExec                = "batch_execution"
	TableNameBatchExecJobs            = "batch_exec_jobs"
	TableNameBatchExecTemplate        = "batch_execution_template"
	TableNameBatchExecTemplateRole    = "batch_execution_template_role"
	TableNameBatchExecTemplateCollect = "batch_execution_template_collect"

	// batch execution
	BatchExecTemplateStatusAvailable = "available"
	BatchExecErrorCodeSucceed        = "0"
	BatchExecErrorCodeFailed         = "1"
	BatchExecErrorCodePending        = "2"
	BatchExecErrorCodeDangerousBlock = "3"

	// permission type
	PermissionTypeMGMT = "MGMT"
	PermissionTypeUSE  = "USE"
)

var (
	UrlPrefix = fmt.Sprintf("/%s", GlobalProjectName)
)
