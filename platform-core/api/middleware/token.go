package middleware

import (
	"fmt"
	"github.com/WeBankPartners/go-common-lib/token"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
	"io"
	"regexp"
	"strings"
)

var (
	ApiMenuMap = make(map[string][]string) // key -> apiCode  value -> menuList
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
		// 首页接口& 子系统直接放行
		if strings.Contains(strings.Join(GetRequestRoles(c), ","), "SUB_SYSTEM") || strings.HasSuffix(c.Request.URL.Path, "/resource-files") ||
			strings.HasSuffix(c.Request.URL.Path, "/appinfo/version") || strings.HasSuffix(c.Request.URL.Path, "/my-menus") {
			c.Next()
			return
		}
		if models.Config.MenuApiMap.Enable == "true" || strings.TrimSpace(models.Config.MenuApiMap.Enable) == "" || strings.ToUpper(models.Config.MenuApiMap.Enable) == "Y" {
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
	matchUrlMap := make(map[string]int)
	for k, code := range apiMenuCodeMap {
		re := regexp.MustCompile("^" + regexp.MustCompile(":[\\w\\-]+").ReplaceAllString(strings.ToLower(k), "([\\w\\.\\-\\$\\{\\}:]+)") + "$")
		for _, menuApi := range models.MenuApiGlobalList {
			for _, item := range menuApi.Urls {
				key := strings.ToLower(item.Method + "_" + item.Url)
				if re.MatchString(key) {
					if existList, existFlag := ApiMenuMap[code]; existFlag {
						ApiMenuMap[code] = append(existList, menuApi.Menu)
					} else {
						ApiMenuMap[code] = []string{menuApi.Menu}
					}
					matchUrlMap[item.Method+"_"+item.Url] = 1
				}
			}
		}
	}
	for _, menuApi := range models.MenuApiGlobalList {
		for _, item := range menuApi.Urls {
			if _, ok := matchUrlMap[item.Method+"_"+item.Url]; !ok {
				log.Logger.Info("InitApiMenuMap can not match menuUrl", log.String("menu", menuApi.Menu), log.String("method", item.Method), log.String("url", item.Url))
			}
		}
	}
	for k, v := range ApiMenuMap {
		if len(v) > 1 {
			ApiMenuMap[k] = models.DistinctStringList(v, []string{})
		}
	}
	log.Logger.Debug("InitApiMenuMap done", log.JsonObj("ApiMenuMap", ApiMenuMap))
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
