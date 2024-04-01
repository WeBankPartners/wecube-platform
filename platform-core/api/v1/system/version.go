package system

import (
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
)

func AppVersion(c *gin.Context) {
	log.Logger.Debug("UserRequest ver api", log.String("user", middleware.GetRequestUser(c)), log.StringList("roles", middleware.GetRequestRoles(c)))
	middleware.ReturnData(c, models.Config.Version)
}
