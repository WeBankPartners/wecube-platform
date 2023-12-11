package plugin

import (
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/bash"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/gin-gonic/gin"
	"os"
	"strings"
)

func GetPackages(c *gin.Context) {
	allPackageFlag := strings.ToLower(c.Query("all"))
	allFlag := false
	if allPackageFlag == "yes" || allPackageFlag == "y" || allPackageFlag == "true" {
		allFlag = true
	}
	result, err := database.GetPackages(c, allFlag)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func UploadPackage(c *gin.Context) {
	// 接收插件zip文件
	fileName, fileBytes, err := middleware.ReadFormFile(c, "zip-file")
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 解压插件zip包
	var tmpFilePath, tmpFileDir string
	if tmpFilePath, tmpFileDir, err = bash.SaveTmpFile(fileName, fileBytes); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	defer func() {
		if removeTmpDirErr := os.RemoveAll(tmpFileDir); removeTmpDirErr != nil {
			log.Logger.Error("Try to remove package upload tmp dir fail", log.String("tmpDir", tmpFileDir), log.Error(removeTmpDirErr))
		}
	}()

	// 上传解压后的文件到s3

	// 解析xml文件

	// 写数据库
}

func GetPluginDependencies(c *gin.Context) {
	pluginPackageId := c.Param("pluginPackage")
	result, err := database.GetPluginDependencies(c, pluginPackageId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func GetPluginMenus(c *gin.Context) {
	pluginPackageId := c.Param("pluginPackage")
	result, err := database.GetPluginMenus(c, pluginPackageId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func GetPluginModels(c *gin.Context) {

}

func GetPluginSystemParameters(c *gin.Context) {
	pluginPackageId := c.Param("pluginPackage")
	result, err := database.GetPluginSystemParameters(c, pluginPackageId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func GetPluginAuthorities(c *gin.Context) {
	pluginPackageId := c.Param("pluginPackage")
	result, err := database.GetPluginAuthorities(c, pluginPackageId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func GetPluginRuntimeResources(c *gin.Context) {
	pluginPackageId := c.Param("pluginPackage")
	result, err := database.GetPluginRuntimeResources(c, pluginPackageId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func GetAvailableContainerHost(c *gin.Context) {
	result, err := database.GetAvailableContainerHost()
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func RegisterPackage(c *gin.Context) {
	// 把s3上的ui.zip下下来放到本地

	// 把ui.zip用ssh传到静态资源服务器上并解压

	// 把对应插件版本的系统变量置为active
}

func GetHostAvailablePort(c *gin.Context) {

}

func LaunchPlugin(c *gin.Context) {

}
