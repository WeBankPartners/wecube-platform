package middleware

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/api/support"
	"github.com/gin-gonic/gin"
	"sync"
)

type RedirectRule struct {
	Context string
	//Method          string
	TargetPath string
	//TargetAddress string
	//Name          string
	//LogAction       bool
	HttpScheme      string
	Host            string
	Port            string
	RequestHandler  support.RequestHandlerFunc
	ResponseHandler support.ResponseHandlerFunc
}

//var redirectRuleMap map[string]*RedirectRule
var redirectRuleMap sync.Map

//var mapLock sync.Mutex

//var RedirectRules []RedirectRule

func Redirect() gin.HandlerFunc {
	return func(c *gin.Context) {
		requestKey := BuildRequestKey(c.Request.URL.Path)

		if val, has := redirectRuleMap.Load(requestKey); has {
			rule := val.(RedirectRule)
			targetUrl := rule.TargetPath
			invoke := support.RedirectInvoke{
				TargetUrl:       targetUrl,
				RequestHandler:  rule.RequestHandler,
				ResponseHandler: rule.ResponseHandler,
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

func RemoveRule(rule RedirectRule) {
	key := BuildRequestKey(rule.Context)
	redirectRuleMap.Delete(key)
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
	return fmt.Sprintf("/%s/**", context)
}
