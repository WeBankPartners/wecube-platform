package middleware

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/api/support"
	"github.com/gin-gonic/gin"
	"strings"
	"sync"
)

type RedirectRule struct {
	Context string
	//Uri     string
	//Method          string
	TargetPath string
	//TargetAddress string
	//Name          string
	//LogAction       bool
	HttpScheme string
	Host       string
	Port       string
	//RequestHandler  support.RequestHandlerFunc
	//ResponseHandler support.ResponseHandlerFunc
}

//var redirectRuleMap map[string]*RedirectRule
var redirectRuleMap sync.Map
var lock sync.Mutex

//var mapLock sync.Mutex

//var RedirectRules []RedirectRule

func Redirect() gin.HandlerFunc {
	return func(c *gin.Context) {
		uri := c.Request.URL.RequestURI()
		uriParts := strings.Split(uri, "/")
		context := ""
		if len(uriParts) > 1 { // uriParts[0] is empty string
			context = uriParts[1]
		}
		requestKey := BuildRequestKey(context)

		if val, has := redirectRuleMap.Load(requestKey); has {
			rule := val.(RedirectRule)
			targetUrl := rule.TargetPath + uri
			invoke := support.RedirectInvoke{
				TargetUrl: targetUrl,
				//RequestHandler:  rule.RequestHandler,
				//ResponseHandler: rule.ResponseHandler,
			}
			invoke.Do(c)
		}
		c.Next()
	}
}

func AddRedirectRule(rule RedirectRule) {
	key := BuildRequestKey(rule.Context)
	redirectRuleMap.Store(key, rule)
}

func RemoveRule(context string) {
	lock.Lock()
	defer lock.Unlock()

	redirectRuleMap.Range(func(key, value interface{}) bool {
		rule := value.(RedirectRule)
		if context == rule.Context {
			redirectRuleMap.Delete(key)
		}
		return true
	})

}

func GetAllRedirectRules() []RedirectRule {
	rules := make([]RedirectRule, 0)
	redirectRuleMap.Range(func(key, value interface{}) bool {
		rule := value.(RedirectRule)
		rules = append(rules, rule)
		return true
	})
	return rules
}

func BuildRequestKey(context string) string {
	//return fmt.Sprintf("%s_%s", path, method)
	return fmt.Sprintf("%s/**", context)
}
