package api

import (
	"bytes"
	"fmt"
	"go.uber.org/zap"
	"io/ioutil"
	"net/http"
	"net/http/httputil"
	"strings"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-auth-server/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/constant"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
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
	router := gin.Default()

	if model.Config.Log.AccessLogEnable {
		router.Use(HttpLogHandle())
	}
	router.Use(gin.CustomRecovery(RecoverHandle))

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
		apiCodeMap[fmt.Sprintf("%s_%s%s", funcObj.Method, constant.UrlPrefix, funcObj.Url)] = funcObj.ApiCode
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

func HttpLogHandle() gin.HandlerFunc {
	return func(c *gin.Context) {
		start := time.Now()
		bodyBytes, _ := ioutil.ReadAll(c.Request.Body)
		c.Request.Body.Close()
		c.Request.Body = ioutil.NopCloser(bytes.NewReader(bodyBytes))
		c.Set("requestBody", string(bodyBytes))
		log.Info(nil, log.LOGGER_APP, "Received request ", zap.String("url", c.Request.RequestURI), zap.String("method", c.Request.Method), zap.String("body", c.GetString("requestBody")))
		if strings.EqualFold(model.Config.Log.Level, "debug") {
			requestDump, _ := httputil.DumpRequest(c.Request, true)
			log.Debug(nil, log.LOGGER_APP, "Received request: "+string(requestDump))
		}
		apiCode := apiCodeMap[c.Request.Method+"_"+c.FullPath()]
		c.Writer.Header().Add("Api-Code", apiCode)
		c.Next()
		costTime := time.Since(start).Seconds() * 1000
		log.Info(nil, log.LOGGER_ACCESS, "Got request -", zap.String("url", c.Request.RequestURI), zap.String("method", c.Request.Method), zap.Int("code", c.Writer.Status()), zap.String("operator", c.GetString("user")), zap.String("ip", getRemoteIp(c)), zap.Float64("cost_ms", costTime), zap.String("body", c.GetString("responseBody")))
	}
}

func getRemoteIp(c *gin.Context) string {
	netIp := c.RemoteIP()
	if len(netIp) > 0 {
		return netIp
	}
	return c.ClientIP()
}

func RecoverHandle(c *gin.Context, err interface{}) {
	var errorMessage string
	if err != nil {
		errorMessage = err.(error).Error()
	}
	log.Error(nil, log.LOGGER_APP, "Handle error", zap.Int("errorCode", 1), zap.String("message", errorMessage))
	c.JSON(http.StatusInternalServerError, model.ResponseWrap{ErrorCode: 1, Status: model.ResponseStatusError})
}

func init() {
	httpHandlerFuncList = []*handlerFuncObj{
		{Url: constant.UriLogin, Method: http.MethodPost, HandlerFunc: Login,
			ApiCode: "login"},
		{Url: constant.UriTaskLogin, Method: http.MethodPost, HandlerFunc: TaskLogin,
			ApiCode: "task-login"},
		{Url: "/v1/api/token", Method: http.MethodGet, HandlerFunc: RefreshToken,
			ApiCode: "api-token"},
		{Url: constant.UriGetLoginSeed, Method: http.MethodGet, HandlerFunc: GetLoginSeed,
			ApiCode: "get-login-seed"},

		{Url: "/v1/authorities", Method: http.MethodPost, HandlerFunc: RegisterLocalAuthority,
			ApiCode: "authorities-post"},
		{Url: "/v1/authorities", Method: http.MethodGet, HandlerFunc: RetrieveAllLocalAuthorities,
			ApiCode: "authorities-get"},

		{Url: "/v1/roles", Method: http.MethodPost, HandlerFunc: RegisterLocalRole,
			ApiCode: "roles-post"},
		{Url: "/v1/roles/update", Method: http.MethodPost, HandlerFunc: UpdateLocalRole,
			ApiCode: "roles-update"},
		{Url: constant.UriRoles, Method: http.MethodGet, HandlerFunc: RetrieveAllLocalRoles,
			ApiCode: "roles-get"},
		{Url: "/v1/roles/:role-id", Method: http.MethodGet, HandlerFunc: RetrieveRoleInfo,
			ApiCode: "roles-role-id"},
		{Url: "/v1/roles/name/:role-name", Method: http.MethodGet, HandlerFunc: RetrieveRoleInfoByRoleName,
			ApiCode: "roles-name-role-name"},
		{Url: "/v1/roles/:role-id", Method: http.MethodDelete, HandlerFunc: UnregisterLocalRoleById,
			ApiCode: "roles-role-id-delete"},
		{Url: "/v1/roles/:role-id/authorities", Method: http.MethodGet, HandlerFunc: RetrieveAllAuthoritiesByRoleId,
			ApiCode: "roles-role-id-authorities-get"},
		{Url: "/v1/roles/:role-id/authorities", Method: http.MethodPost, HandlerFunc: ConfigureRoleWithAuthoritiesById,
			ApiCode: "roles-role-id-authorities-post"},
		{Url: "/v1/roles/authorities-grant", Method: http.MethodPost, HandlerFunc: ConfigureRoleWithAuthorities,
			ApiCode: "roles-authorities-grant"},
		{Url: "/v1/roles/authorities-revocation", Method: http.MethodPost, HandlerFunc: RevokeRoleWithAuthorities,
			ApiCode: "roles-authorities-revocation"},
		{Url: "/v1/roles/:role-id/authorities/revoke", Method: http.MethodPost, HandlerFunc: RevokeRoleAuthoritiesById,
			ApiCode: "roles-role-id-authorities-revoke"},
		{Url: "/v1/sub-systems", Method: http.MethodPost, HandlerFunc: RegisterSubSystem,
			ApiCode: "sub-systems"},
		{Url: "/v1/sub-systems/tokens", Method: http.MethodPost, HandlerFunc: RegisterSubSystemAccessToken,
			ApiCode: "sub-systems-tokens", Authorities: []string{"SUPER_ADMIN"}},
		{Url: "/v1/sub-systems", Method: http.MethodGet, HandlerFunc: RetrieveAllSubSystems,
			ApiCode: "sub-systems-get", Authorities: []string{"SUPER_ADMIN"}},
		{Url: "/v1/sub-systems/names/:name", Method: http.MethodGet, HandlerFunc: RetrieveAllSubSystemByName,
			ApiCode: "sub-systems-names-name"},
		{Url: "/v1/sub-systems/:system-code/apikey", Method: http.MethodGet, HandlerFunc: RetrieveAllSubSystemsBySystemCode,
			ApiCode: "sub-systems-system-code-apikey"},

		{Url: "/v1/users", Method: http.MethodPost, HandlerFunc: RegisterLocalUser,
			ApiCode: "users-post"},
		{Url: "/v1/users/reset-password", Method: http.MethodPost, HandlerFunc: ResetLocalUserPassword,
			ApiCode: "users-reset-password", Authorities: []string{"SUPER_ADMIN"}},
		{Url: "/v1/users/change-password", Method: http.MethodPost, HandlerFunc: ModifyLocalUserPassword,
			ApiCode: "users-change-password"},
		{Url: "/v1/users/usernames/:username", Method: http.MethodPost, HandlerFunc: ModifyLocalUserInfomation,
			ApiCode: "users-usernames-username"},
		{Url: "/v1/users", Method: http.MethodGet, HandlerFunc: RetrieveAllUsers,
			ApiCode: "users-get"},
		{Url: "/v1/users/query", Method: http.MethodPost, HandlerFunc: QueryUser,
			ApiCode: "users-query-post"},
		{Url: "/v1/users/:user-id", Method: http.MethodGet, HandlerFunc: RetrieveUserByUserId,
			ApiCode: "users-user-id-get"},
		{Url: "/v1/user-message/:username", Method: http.MethodGet, HandlerFunc: RetrieveUserByUsername,
			ApiCode: "user-message-username-get"},
		{Url: "/v1/users/:user-id", Method: http.MethodDelete, HandlerFunc: UnregisterLocalUser,
			ApiCode: "users-user-id-delete"},
		{Url: "/v1/roles/:role-id/users", Method: http.MethodGet, HandlerFunc: GetUsersByRoleId,
			ApiCode: "roles-role-id-users-get"},
		//TODO: /v1/users/:username/roles
		{Url: "/v1/users/roles-by-name/:username", Method: http.MethodGet, HandlerFunc: GetRolesByUsername,
			ApiCode: "users-roles-by-name-username-get"},
		{Url: "/v1/roles/:role-id/users", Method: http.MethodPost, HandlerFunc: ConfigureRoleForUsers,
			ApiCode: "roles-role-id-users-post"},
		{Url: "/v1/users/:user-id/roles", Method: http.MethodPost, HandlerFunc: ConfigureUserWithRoles,
			ApiCode: "users-user-id-roles-post"},
		{Url: "/v1/roles/:role-id/users/revoke", Method: http.MethodPost, HandlerFunc: RevokeRoleFromUsers,
			ApiCode: "roles-role-id-users-revoke-post"},
		{Url: "/v1/users/:user-id/roles/revoke", Method: http.MethodPost, HandlerFunc: RevokeRolesFromUser,
			ApiCode: "users-user-id-roles-revoke-post"},

		{Url: constant.UriUsersRegister, Method: http.MethodPost, HandlerFunc: RegisterUmUser,
			ApiCode: "users-register-post"},
		{Url: "/v1/roles/apply", Method: http.MethodPost, HandlerFunc: CreateRoleApply,
			ApiCode: "roles-apply-post"},
		{Url: "/v1/roles/apply", Method: http.MethodDelete, HandlerFunc: DeleteRoleApply,
			ApiCode: "roles-apply-delete"},
		{Url: constant.UriApplyByApplier, Method: http.MethodPost, HandlerFunc: ListRoleApply,
			ApiCode: "apply-by-applier-post"},
		{Url: constant.UriListApplyByApplier, Method: http.MethodPost, HandlerFunc: ListRoleApplyByApplier,
			ApiCode: "list-apply-by-applier-post"},
		{Url: "/v1/roles/apply", Method: http.MethodPut, HandlerFunc: UpdateRoleApply,
			ApiCode: "roles-apply-put"},
	}
}
