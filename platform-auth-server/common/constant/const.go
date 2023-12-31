package constant

const (
	SCOPE_GLOBAL = "GLOBAL"

	AuthoritySubsystem = "SUB_SYSTEM"

	SubSystemNameSysPlatform = "SYS_PLATFORM"

	AuthSourceUm    = "UM"
	AuthSourceLocal = "LOCAL"

	UrlPrefix           = "/auth"
	AuthorizationHeader = "Authorization"
	RefreshTokenHeader  = "Authorization-Info"
	UriLogin            = "/v1/api/login"
	UriHealthCheck      = "/v1/health-check"

	Operator = "operator"

	ClientTypeUser      = "USER"
	ClientTypeSubSystem = "SUB_SYSTEM"

	TypeAccessToken  = "accessToken"
	TypeRefreshToken = "refreshToken"

	BearerTokenPrefix = "Bearer "
)
