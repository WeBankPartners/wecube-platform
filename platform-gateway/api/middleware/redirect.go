package middleware

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/api/support"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/model"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
	"math/rand"
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

// var redirectRuleMap map[string]*RedirectRule
var redirectRuleMap sync.Map

// from configuration
var platformRedirectRuleMap = make(map[string]RedirectRule)
var lock sync.Mutex

//var mapLock sync.Mutex

// var RedirectRules []RedirectRule
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
				for _, i := range genRandIntList(len(rules)) {
					targetUrl := rules[i].TargetPath + uri
					invoke := support.RedirectInvoke{
						TargetUrl: targetUrl,
					}
					err := invoke.Do(c)
					if err != nil {
						log.Warn(nil, log.LOGGER_APP, "failed to request", zap.String("targetUrl", targetUrl), zap.Error(err))
					} else {
						break
					}
				}

				if len(rules) > 0 {
				} else {
					log.Warn(nil, log.LOGGER_APP, "can not find redirect rule for context:"+context)
				}
			}
		}

		c.Next()
	}
}

func AddRedirectRule(context string, rules []RedirectRule) {
	log.Info(nil, log.LOGGER_APP, "add redirect route context: "+context)

	key := BuildRequestKey(context)

	redirectRuleMap.Store(key, rules)
}

func RemoveRule(context string) {
	lock.Lock()
	defer lock.Unlock()

	log.Info(nil, log.LOGGER_APP, "remove redirect route context: "+context)

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

func genRandIntList(length int) (output []int) {
	var input []int
	for i := 0; i < length; i++ {
		input = append(input, i)
	}
	for len(input) > 0 {
		tmpLength := len(input)
		index := rand.Intn(tmpLength)
		output = append(output, input[index])
		if index > 0 {
			input = append(input[:index], input[index+1:]...)
		} else if index == tmpLength-1 {
			input = input[:index]
		} else {
			input = input[index+1:]
		}
	}
	return
}
