package system

import (
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
)

func AppVersion(c *gin.Context) {
	middleware.ReturnData(c, models.Config.Version)
}
