package middleware

import (
	"fmt"
	"github.com/WeBankPartners/go-common-lib/token"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
	"io"
	"regexp"
	"strings"
)

var (
	ApiMenuMap = make(map[string][]string) // key -> apiCode  value -> menuList
	// taskman需要调用编排,根据ApiCode放行
	whiteCodeMap = map[string]bool{
		"get-all-models":                         true,
		"process-definition-list":                true,
		"plugin-process-definition-list":         true,
		"process-def-outline":                    true,
		"process-ins-detail":                     true,
		"process-def-root-entity":                true,
		"process-ins-node-context":               true,
		"query-expr-entities":                    true,
		"get-entity-model":                       true,
		"proc-ins-callback":                      true,
		"check-collect-batch-execution-template": true,
		"import-batch-execution-template":        true,
		"proc-start-events":                      true,
		"export-batch-execution-template":        true,
		"get-process-definition-node-link":       true,
		"get-user":                               true,
		"change-user-password":                   true,
		"get-packages":                           true,
		"query-roles":                            true,
		"get-all-user":                           true,
		"get-role-user":                          true,
		"get-user-roles":                         true,
		"get-process-ins-by-session-id":          true,
		"get-web-running-packages":               true,
	}
)

func GetRequestUser(c *gin.Context) string {
	return c.GetString(models.ContextUserId)
}

func GetRequestRoles(c *gin.Context) []string {
	return c.GetStringSlice(models.ContextRoles)
}

func AuthToken(c *gin.Context) {
	err := authRequest(c)
	if err != nil {
		ReturnAuthError(c, exterror.Catch(exterror.New().RequestTokenValidateError, err), c.GetHeader(models.AuthorizationHeader))
		c.Abort()
	} else {
		// 首页接口& 子系统直接放行, public资源直接放行
		if strings.Contains(strings.Join(GetRequestRoles(c), ","), "SUB_SYSTEM") || strings.HasSuffix(c.Request.URL.Path, "/resource-files") ||
			strings.HasSuffix(c.Request.URL.Path, "/appinfo/version") || strings.HasSuffix(c.Request.URL.Path, "/my-menus") ||
			strings.HasPrefix(c.Request.URL.Path, models.UrlPrefix+"/v1/public") || strings.HasPrefix(c.Request.URL.Path, models.UrlPrefix+"/v2/public") ||
			strings.HasSuffix(c.Request.URL.Path, "/health-check") {
			c.Next()
			return
		}
		if models.Config.MenuApiMap.Enable == "true" || strings.TrimSpace(models.Config.MenuApiMap.Enable) == "" || strings.ToUpper(models.Config.MenuApiMap.Enable) == "Y" {
			// 白名单URL code直接放行
			for code, _ := range whiteCodeMap {
				if code == c.GetString(models.ContextApiCode) {
					c.Next()
					return
				}
			}
			legal := false
			if allowMenuList, ok := ApiMenuMap[c.GetString(models.ContextApiCode)]; ok {
				legal = compareStringList(GetRequestRoles(c), allowMenuList)
			} else {
				legal = validateMenuApi(GetRequestRoles(c), c.Request.URL.Path, c.Request.Method)
			}
			if legal {
				c.Next()
			} else {
				ReturnApiPermissionError(c)
				c.Abort()
			}
		} else {
			c.Next()
		}
	}
}

func authRequest(c *gin.Context) error {
	if !models.Config.Auth.Enable {
		return nil
	}
	authHeader := c.GetHeader(models.AuthorizationHeader)
	if authHeader == "" {
		return fmt.Errorf("can not find Request Header Authorization ")
	}
	authToken, err := token.DecodeJwtToken(authHeader, models.Config.Auth.JwtSigningKey)
	if err != nil {
		return err
	}
	if authToken.User == "" {
		return fmt.Errorf("token content is illegal,main message is empty ")
	}
	c.Set(models.ContextUserId, authToken.User)
	c.Set(models.ContextRoles, authToken.Roles)
	return nil
}

func ReadFormFile(c *gin.Context, fileKey string) (fileName string, fileBytes []byte, err error) {
	file, getFileErr := c.FormFile(fileKey)
	if getFileErr != nil {
		err = getFileErr
		return
	}
	fileName = file.Filename
	fileHandler, openFileErr := file.Open()
	if openFileErr != nil {
		err = openFileErr
		return
	}
	fileBytes, err = io.ReadAll(fileHandler)
	fileHandler.Close()
	return
}

func validateMenuApi(roles []string, path, method string) (legal bool) {
	// 防止ip 之类数据配置不上
	path = strings.ReplaceAll(path, ".", "")
	path = strings.ReplaceAll(path, "_", "")
	path = strings.ReplaceAll(path, "-", "")
	for _, menuApi := range models.MenuApiGlobalList {
		for _, role := range roles {
			if strings.ToLower(menuApi.Menu) == strings.ToLower(role) {
				for _, item := range menuApi.Urls {
					if strings.TrimSpace(item.Url) == "" {
						continue
					}
					if strings.ToLower(item.Method) == strings.ToLower(method) {
						re := regexp.MustCompile(buildRegexPattern(item.Url))
						if re.MatchString(path) {
							legal = true
							return
						}
					}
				}
			}
		}
	}
	return
}

func buildRegexPattern(template string) string {
	// 将 ${variable} 替换为 (\w+) ,并且严格匹配
	return "^" + regexp.MustCompile(`\$\{[\w.-]+\}`).ReplaceAllString(template, `([\w.-]+)`) + "$"
}

func InitApiMenuMap(apiMenuCodeMap map[string]string) {
	var exist bool
	matchUrlMap := make(map[string]int)
	for k, code := range apiMenuCodeMap {
		exist = false
		re := regexp.MustCompile("^" + regexp.MustCompile(":[\\w\\-]+").ReplaceAllString(strings.ToLower(k), "([\\w\\.\\-\\$\\{\\}:\\[\\]]+)") + "$")
		for _, menuApi := range models.MenuApiGlobalList {
			for _, item := range menuApi.Urls {
				key := strings.ToLower(item.Method + "_" + item.Url)
				if re.MatchString(key) {
					exist = true
					if existList, existFlag := ApiMenuMap[code]; existFlag {
						ApiMenuMap[code] = append(existList, menuApi.Menu)
					} else {
						ApiMenuMap[code] = []string{menuApi.Menu}
					}
					matchUrlMap[item.Method+"_"+item.Url] = 1
				}
			}
		}
		if !exist {
			log.Info(nil, log.LOGGER_APP, "InitApiMenuMap menu-api-json lack url", zap.String("path", k))
		}
	}
	for _, menuApi := range models.MenuApiGlobalList {
		for _, item := range menuApi.Urls {
			if _, ok := matchUrlMap[item.Method+"_"+item.Url]; !ok {
				log.Info(nil, log.LOGGER_APP, "InitApiMenuMap can not match menuUrl", zap.String("menu", menuApi.Menu), zap.String("method", item.Method), zap.String("url", item.Url))
			}
		}
	}
	for k, v := range ApiMenuMap {
		if len(v) > 1 {
			ApiMenuMap[k] = models.DistinctStringList(v, []string{})
		}
	}
	log.Debug(nil, log.LOGGER_APP, "InitApiMenuMap done", log.JsonObj("ApiMenuMap", ApiMenuMap))
}

func compareStringList(from, target []string) bool {
	match := false
	for _, f := range from {
		for _, t := range target {
			if f == t {
				match = true
				break
			}
		}
		if match {
			break
		}
	}
	return match
}
