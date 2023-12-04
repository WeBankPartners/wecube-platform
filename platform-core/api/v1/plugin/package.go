package plugin

import (
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/gin-gonic/gin"
	"strings"
)

func GetPackages(c *gin.Context) {
	allPackageFlag := strings.ToLower(c.Query("all"))
	allFlag := false
	if allPackageFlag == "yes" || allPackageFlag == "y" || allPackageFlag == "true" {
		allFlag = true
	}
	result, err := database.GetPackages(allFlag)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func UploadPackage(c *gin.Context) {

}

func GetPluginDependencies(c *gin.Context) {

}

func GetPluginMenus(c *gin.Context) {

}

func GetPluginModels(c *gin.Context) {

}

func GetPluginSystemParameters(c *gin.Context) {

}

func GetPluginAuthorities(c *gin.Context) {

}

func GetPluginRuntimeResources(c *gin.Context) {

}

func GetAvailableContainerHost(c *gin.Context) {

}

func RegisterPackage(c *gin.Context) {

}

func GetHostAvailablePort(c *gin.Context) {

}

func LaunchPlugin(c *gin.Context) {

}
