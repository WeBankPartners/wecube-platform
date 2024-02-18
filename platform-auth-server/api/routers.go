package api

import (
	"fmt"
	"net/http"

	"github.com/WeBankPartners/wecube-platform/platform-auth-server/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/constant"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"

	"github.com/gin-gonic/gin"
)

type handlerFuncObj struct {
	HandlerFunc  func(c *gin.Context)
	Method       string
	Url          string
	LogOperation bool
	PreHandle    func(c *gin.Context)
	ApiCode      string
	Authorities  []string
	LogAction    bool
	//FeatureCode  string
}

var (
	httpHandlerFuncList []*handlerFuncObj
	apiCodeMap          = make(map[string]string)
)

// NewRouter returns a new router.
func NewRouter() *gin.Engine {
	//redirectRoutes := buildRedirectRoutes()
	//httpHandlerFuncList = append(httpHandlerFuncList, redirectRoutes...)
	router := gin.Default()

	if model.Config.Log.AccessLogEnable {
		router.Use(middleware.HttpLogHandle())
	}
	router.Use(gin.CustomRecovery(middleware.RecoverHandle))

	authMap := buildAuthMap()
	authoritiesFetcher := func(path string, method string) []string {
		return authMap[middleware.BuildRequestKey(path[len(constant.UrlPrefix):], method)]
	}

	//router.Use(middleware.AuthApi)
	group := router.Group(constant.UrlPrefix)
	group.Use(middleware.AuthApi(authoritiesFetcher))

	for _, funcObj := range httpHandlerFuncList {
		switch funcObj.Method {
		case http.MethodGet:
			group.GET(funcObj.Url, funcObj.HandlerFunc)
		case http.MethodPost:
			group.POST(funcObj.Url, funcObj.HandlerFunc)
		case http.MethodPut:
			group.PUT(funcObj.Url, funcObj.HandlerFunc)
		case http.MethodPatch:
			group.PATCH(funcObj.Url, funcObj.HandlerFunc)
		case http.MethodDelete:
			group.DELETE(funcObj.Url, funcObj.HandlerFunc)
		}
		apiCodeMap[fmt.Sprintf("%s_%s", funcObj.Url, funcObj.Method)] = funcObj.ApiCode
	}

	return router
}

func buildAuthMap() map[string][]string {
	authMap := make(map[string][]string)
	for _, handlerFunc := range httpHandlerFuncList {
		requestKey := middleware.BuildRequestKey(handlerFunc.Url, handlerFunc.Method)
		authMap[requestKey] = handlerFunc.Authorities
	}
	return authMap
}

func init() {
	httpHandlerFuncList = append(httpHandlerFuncList, // contract instance
		&handlerFuncObj{Url: constant.UriLogin, Method: http.MethodPost, HandlerFunc: Login,
			ApiCode: "Login"},
		&handlerFuncObj{Url: "/v1/api/token", Method: http.MethodGet, HandlerFunc: RefreshToken,
			ApiCode: "RefreshToken"},

		&handlerFuncObj{Url: "/v1/authorities", Method: http.MethodPost, HandlerFunc: RegisterLocalAuthority,
			ApiCode: "RegisterLocalAuthority"},
		&handlerFuncObj{Url: "/v1/authorities", Method: http.MethodGet, HandlerFunc: RetrieveAllLocalAuthorities,
			ApiCode: "RetrieveAllLocalAuthorities"},

		&handlerFuncObj{Url: "/v1/roles", Method: http.MethodPost, HandlerFunc: RegisterLocalRole,
			ApiCode: "RegisterLocalRole"},
		&handlerFuncObj{Url: "/v1/roles/update", Method: http.MethodPost, HandlerFunc: UpdateLocalRole,
			ApiCode: "UpdateLocalRole"},
		&handlerFuncObj{Url: "/v1/roles", Method: http.MethodGet, HandlerFunc: RetrieveAllLocalRoles,
			ApiCode: "RetrieveAllLocalRoles"},
		&handlerFuncObj{Url: "/v1/roles/:role-id", Method: http.MethodGet, HandlerFunc: RetrieveRoleInfo,
			ApiCode: "RetrieveRoleInfo"},
		&handlerFuncObj{Url: "/v1/roles/name/:role-name", Method: http.MethodGet, HandlerFunc: RetrieveRoleInfoByRoleName,
			ApiCode: "RetrieveRoleInfoByRoleName"},
		&handlerFuncObj{Url: "/v1/roles/:role-id", Method: http.MethodDelete, HandlerFunc: UnregisterLocalRoleById,
			ApiCode: "UnregisterLocalRoleById"},
		&handlerFuncObj{Url: "/v1/roles/:role-id/authorities", Method: http.MethodGet, HandlerFunc: RetrieveAllAuthoritiesByRoleId,
			ApiCode: "RetrieveAllAuthoritiesByRoleId"},
		&handlerFuncObj{Url: "/v1/roles/:role-id/authorities", Method: http.MethodPost, HandlerFunc: ConfigureRoleWithAuthoritiesById,
			ApiCode: "ConfigureRoleWithAuthoritiesById"},
		&handlerFuncObj{Url: "/v1/roles/authorities-grant", Method: http.MethodPost, HandlerFunc: ConfigureRoleWithAuthorities,
			ApiCode: "ConfigureRoleWithAuthorities"},
		&handlerFuncObj{Url: "/v1/roles/authorities-revocation", Method: http.MethodPost, HandlerFunc: RevokeRoleWithAuthorities,
			ApiCode: "RevokeRoleWithAuthorities"},
		&handlerFuncObj{Url: "/v1/roles/:role-id/authorities/revoke", Method: http.MethodPost, HandlerFunc: RevokeRoleAuthoritiesById,
			ApiCode: "RevokeRoleAuthoritiesById"},
		&handlerFuncObj{Url: "/v1/sub-systems", Method: http.MethodPost, HandlerFunc: RegisterSubSystem,
			ApiCode: "RegisterSubSystem"},
		&handlerFuncObj{Url: "/v1/sub-systems/tokens", Method: http.MethodPost, HandlerFunc: RegisterSubSystemAccessToken,
			ApiCode: "RegisterSubSystem", Authorities: []string{"SUPER_ADMIN"}},
		&handlerFuncObj{Url: "/v1/sub-systems", Method: http.MethodGet, HandlerFunc: RetrieveAllSubSystems,
			ApiCode: "RetrieveAllSubSystems", Authorities: []string{"SUPER_ADMIN"}},
		&handlerFuncObj{Url: "/v1/sub-systems/names/:name", Method: http.MethodGet, HandlerFunc: RetrieveAllSubSystemByName,
			ApiCode: "RetrieveAllSubSystemByName"},
		&handlerFuncObj{Url: "/v1/sub-systems/:system-code/apikey", Method: http.MethodGet, HandlerFunc: RetrieveAllSubSystemsBySystemCode,
			ApiCode: "RetrieveAllSubSystemsBySystemCode"},

		&handlerFuncObj{Url: "/v1/users", Method: http.MethodPost, HandlerFunc: RegisterLocalUser,
			ApiCode: "RegisterLocalUser"},
		&handlerFuncObj{Url: "/v1/users/reset-password", Method: http.MethodPost, HandlerFunc: ResetLocalUserPassword,
			ApiCode: "ResetLocalUserPassword", Authorities: []string{"SUPER_ADMIN"}},
		&handlerFuncObj{Url: "/v1/users/change-password", Method: http.MethodPost, HandlerFunc: ModifyLocalUserPassword,
			ApiCode: "ModifyLocalUserPassword"},
		&handlerFuncObj{Url: "/v1/users/usernames/:username", Method: http.MethodPost, HandlerFunc: ModifyLocalUserInfomation,
			ApiCode: "ModifyLocalUserInfomation"},
		&handlerFuncObj{Url: "/v1/users", Method: http.MethodGet, HandlerFunc: RetrieveAllUsers,
			ApiCode: "RetrieveAllUsers"},
		&handlerFuncObj{Url: "/v1/users/:user-id", Method: http.MethodGet, HandlerFunc: RetrieveUserByUserId,
			ApiCode: "RetrieveUserByUserId"},
		&handlerFuncObj{Url: "/v1/users/:user-id", Method: http.MethodDelete, HandlerFunc: UnregisterLocalUser,
			ApiCode: "UnregisterLocalUser"},
		&handlerFuncObj{Url: "/v1/roles/:role-id/users", Method: http.MethodGet, HandlerFunc: GetUsersByRoleId,
			ApiCode: "GetUsersByRoleId"},
		//TODO: /v1/users/:username/roles
		&handlerFuncObj{Url: "/v1/users/roles-by-name/:username", Method: http.MethodGet, HandlerFunc: GetRolesByUsername,
			ApiCode: "GetRolesByUsername"},
		&handlerFuncObj{Url: "/v1/roles/:role-id/users", Method: http.MethodPost, HandlerFunc: ConfigureRoleForUsers,
			ApiCode: "ConfigureRoleForUsers"},
		&handlerFuncObj{Url: "/v1/users/:user-id/roles", Method: http.MethodPost, HandlerFunc: ConfigureUserWithRoles,
			ApiCode: "ConfigureUserWithRoles"},
		&handlerFuncObj{Url: "/v1/roles/:role-id/users/revoke", Method: http.MethodPost, HandlerFunc: RevokeRoleFromUsers,
			ApiCode: "RevokeRoleFromUsers"},
		&handlerFuncObj{Url: "/v1/users/:user-id/roles/revoke", Method: http.MethodPost, HandlerFunc: RevokeRolesFromUser,
			ApiCode: "RevokeRolesFromUser"},
	)
}
