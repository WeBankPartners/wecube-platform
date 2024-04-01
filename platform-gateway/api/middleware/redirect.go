package middleware

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/api/support"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/common/network"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/model"
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

//from configuration
var platformRedirectRuleMap = make(map[string]RedirectRule)
var lock sync.Mutex

//var mapLock sync.Mutex

//var RedirectRules []RedirectRule
func Init() {
	for i := range model.Config.RedirectRoutes {
		platformRedirectRuleMap[model.Config.RedirectRoutes[i].Context] = RedirectRule{
			Context:    model.Config.RedirectRoutes[i].Context,
			TargetPath: model.Config.RedirectRoutes[i].TargetPath,
		}
	}
}

func Redirect() gin.HandlerFunc {
	return func(c *gin.Context) {
		uri := c.Request.URL.RequestURI()
		uriParts := strings.Split(uri, "/")
		context := ""
		if len(uriParts) > 1 { // uriParts[0] is empty string
			context = uriParts[1]
		}

		if rule, ok := platformRedirectRuleMap[context]; ok {
			targetUrl := rule.TargetPath + uri
			invoke := support.RedirectInvoke{
				TargetUrl: targetUrl,
			}
			invoke.Do(c)
		} else {
			requestKey := BuildRequestKey(context)

			if val, has := redirectRuleMap.Load(requestKey); has {
				rules := val.([]RedirectRule)
				validIdx := -1
				for i := range rules {
					targetUrl := rules[i].TargetPath + uri
					invoke := support.RedirectInvoke{
						TargetUrl: targetUrl,
					}
					err := invoke.Do(c)
					if err != nil {
						log.Logger.Warn("failed to request", log.String("targetUrl", targetUrl), log.Error(err))
					} else {
						validIdx = i
						break
					}
				}

				if validIdx > 0 {
					tmp := rules[validIdx]
					rules[validIdx] = rules[0]
					rules[0] = tmp
					redirectRuleMap.Store(requestKey, rules)
				}

				if len(rules) > 0 {
				} else {
					log.Logger.Warn("can not find redirect rule for context:" + context)
				}
			}
		}

		c.Next()
	}
}

func isRemoteRejected(err error) bool {
	if network.IsNetworkTimeout(err) {
		return true
	}
	if network.IsConnReset(err) {
		return true
	}
	return false
}

func AddRedirectRule(context string, rules []RedirectRule) {
	log.Logger.Info("add redirect route context: " + context)

	key := BuildRequestKey(context)

	redirectRuleMap.Store(key, rules)
}

func RemoveRule(context string) {
	lock.Lock()
	defer lock.Unlock()

	log.Logger.Info("remove redirect route context: " + context)

	redirectRuleMap.Range(func(key, value interface{}) bool {
		rules := value.([]RedirectRule)
		if len(rules) > 0 {
			if context == rules[0].Context {
				redirectRuleMap.Delete(key)
			}
		} else {
			redirectRuleMap.Delete(key)
		}
		return true
	})

}

func GetAllRedirectRules() []RedirectRule {
	lock.Lock()
	defer lock.Unlock()

	rules := make([]RedirectRule, 0)
	redirectRuleMap.Range(func(key, value interface{}) bool {
		rule := value.([]RedirectRule)
		rules = append(rules, rule...)
		return true
	})
	return rules
}

func BuildRequestKey(context string) string {
	//return fmt.Sprintf("%s_%s", path, method)
	return fmt.Sprintf("%s/**", context)
}
