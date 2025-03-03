package system

import (
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

func AppVersion(c *gin.Context) {
	log.Debug(nil, log.LOGGER_APP, "UserRequest ver api", zap.String("user", middleware.GetRequestUser(c)), zap.Strings("roles", middleware.GetRequestRoles(c)))
	middleware.ReturnData(c, models.Config.Version)
}
