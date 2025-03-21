package constant

const (
	SCOPE_GLOBAL = "GLOBAL"

	AuthoritySubsystem = "SUB_SYSTEM"

	SubSystemNameSysPlatform = "SYS_PLATFORM"

	AuthSourceUm    = "UM"
	AuthSourceLocal = "LOCAL"

	UrlPrefix             = "/auth"
	AuthorizationHeader   = "Authorization"
	RefreshTokenHeader    = "Authorization-Info"
	UriLogin              = "/v1/api/login"
	UriTaskLogin          = "/v1/api/taskLogin"
	UriUsersRegister      = "/v1/users/register"
	UriListApplyByApplier = "/v1/roles/apply/byapplier"
	UriApplyByApplier     = "/v1/roles/apply/byhandler"
	UriRoles              = "/v1/roles"
	UriHealthCheck        = "/v1/health-check"
	UriGetLoginSeed       = "/v1/api/seed"

	Operator = "operator"

	ClientTypeUser      = "USER"
	ClientTypeSubSystem = "SUB_SYSTEM"

	TypeAccessToken  = "accessToken"
	TypeRefreshToken = "refreshToken"

	BearerTokenPrefix    = "Bearer "
	DefaultJwtSigningKey = "Platform+Auth+Server+Secret"

	DateTimeFormat     = "2006-01-02 15:04:05"
	HEADER_BUSINESS_ID = "BusinessId" // 业务流水号
	HEADER_REQUEST_ID  = "RequestId"  // 交易流水号
)

// UserRolePermissionStatus 用户角色权限状态
type UserRolePermissionStatus string

const (
	UserRolePermissionStatusExpire    UserRolePermissionStatus = "expire"     // 已过期
	UserRolePermissionStatusForever   UserRolePermissionStatus = "forever"    // 永久
	UserRolePermissionStatusPreExpire UserRolePermissionStatus = "preExpired" // 将要过期
	UserRolePermissionStatusInEffect  UserRolePermissionStatus = "inEffect"   // 生效中
	UserRolePermissionStatusDeleted   UserRolePermissionStatus = "deleted"    // 已删除
)
