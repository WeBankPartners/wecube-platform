package plugin

import (
	"encoding/xml"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/bash"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/gin-gonic/gin"
	"os"
	"strconv"
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
	if err = bash.DecompressFile(tmpFilePath, ""); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	packageFiles, readErr := bash.ListDirFiles(tmpFileDir)
	if readErr != nil {
		middleware.ReturnError(c, readErr)
		return
	}
	// 解析xml文件
	var registerFile, imageFile, uiFile, initSql, upgradeSql string
	withUi := false
	for _, v := range packageFiles {
		if v == "register.xml" {
			registerFile = v
			continue
		}
		if v == "image.tar" {
			imageFile = v
			continue
		}
		if v == "ui.zip" {
			uiFile = v
			withUi = true
			continue
		}
	}
	if registerFile == "" || imageFile == "" {
		middleware.ReturnError(c, fmt.Errorf("register xml and image tar can not empty"))
		return
	}
	var registerConfig models.RegisterXML
	registerConfigBytes, readRegisterConfigErr := os.ReadFile(fmt.Sprintf("%s/%s", tmpFileDir, registerFile))
	if readRegisterConfigErr != nil {
		middleware.ReturnError(c, fmt.Errorf("read register xml file fail,%s ", readRegisterConfigErr.Error()))
		return
	}
	if err = xml.Unmarshal(registerConfigBytes, &registerConfig); err != nil {
		middleware.ReturnError(c, fmt.Errorf("xml unmarshal regisger xml fail,%s ", err.Error()))
		return
	}
	pluginPackageObj := models.PluginPackages{Name: registerConfig.Name, Version: registerConfig.Version}
	if err = database.GetSimplePluginPackage(c, &pluginPackageObj, false); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if pluginPackageObj.Id != "" {
		middleware.ReturnError(c, fmt.Errorf("plugin %s:%s already existed", registerConfig.Name, registerConfig.Version))
		return
	}
	if registerConfig.ResourceDependencies.Mysql.InitFileName != "" {
		initSql = registerConfig.ResourceDependencies.Mysql.InitFileName
		upgradeSql = registerConfig.ResourceDependencies.Mysql.UpgradeFileName
		if !bash.ListContains(packageFiles, initSql) {
			middleware.ReturnError(c, fmt.Errorf("init sql file:%s can not find in package", initSql))
			return
		}
		if !bash.ListContains(packageFiles, upgradeSql) {
			upgradeSql = ""
		}
	}
	// 上传解压后的文件到s3
	s3Prefix := fmt.Sprintf("%s/%s/", registerConfig.Name, registerConfig.Version)
	s3FileMap := make(map[string]string)
	s3FileMap[fmt.Sprintf("%s/%s", tmpFileDir, registerFile)] = s3Prefix + registerFile
	s3FileMap[fmt.Sprintf("%s/%s", tmpFileDir, imageFile)] = s3Prefix + imageFile
	if uiFile != "" {
		s3FileMap[fmt.Sprintf("%s/%s", tmpFileDir, uiFile)] = s3Prefix + uiFile
	}
	if initSql != "" {
		s3FileMap[fmt.Sprintf("%s/%s", tmpFileDir, initSql)] = s3Prefix + initSql
	}
	if upgradeSql != "" {
		s3FileMap[fmt.Sprintf("%s/%s", tmpFileDir, upgradeSql)] = s3Prefix + upgradeSql
	}
	if err = bash.UploadPluginPackage(models.Config.S3.PluginPackageBucket, s3FileMap); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if registerConfig.ResourceDependencies.S3.BucketName != "" {
		if err = bash.MakeBucket(registerConfig.ResourceDependencies.S3.BucketName); err != nil {
			middleware.ReturnError(c, err)
			return
		}
	}
	// 写数据库
	err = database.UploadPackage(c, &registerConfig, withUi, false)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnSuccess(c)
	}
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
	pluginPackageId := c.Param("pluginPackage")
	pluginPackageObj := models.PluginPackages{Id: pluginPackageId}
	if err := database.GetSimplePluginPackage(c, &pluginPackageObj, true); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 依赖包检测
	depOk, err := database.CheckPluginPackageDependence(c, pluginPackageId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if !depOk {
		middleware.ReturnError(c, exterror.New().PluginDependencyIllegal)
		return
	}
	if pluginPackageObj.UiPackageIncluded {
		// 把s3上的ui.zip下下来放到本地
		var uiFileLocalPath string
		if uiFileLocalPath, err = bash.DownloadPackageFile(models.Config.S3.PluginPackageBucket, fmt.Sprintf("%s/%s/ui.zip", pluginPackageObj.Name, pluginPackageObj.Version)); err != nil {
			middleware.ReturnError(c, err)
			return
		}
		// 把ui.zip用ssh传到静态资源服务器上并解压，如果有两台服务器，则每台都要上传与解压
		targetPath := fmt.Sprintf("%s/%s/%s/ui.zip", models.Config.StaticResource.Path, pluginPackageObj.Name, pluginPackageObj.Version)
		unzipCmd := fmt.Sprintf("cd %s/%s/%s && unzip -o ui.zip", models.Config.StaticResource.Path, pluginPackageObj.Name, pluginPackageObj.Version)
		for _, staticServerIp := range strings.Split(models.Config.StaticResource.Servers, ",") {
			if err = bash.RemoteSCP(staticServerIp, uiFileLocalPath, targetPath); err != nil {
				break
			}
			if err = bash.RemoteSSHCommand(staticServerIp, unzipCmd); err != nil {
				break
			}
		}
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
	}
	// 把对应插件版本的系统变量置为active
	if err = database.ActivePluginSystemVariable(c, pluginPackageObj.Name, pluginPackageObj.Version); err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnSuccess(c)
	}
}

func GetHostAvailablePort(c *gin.Context) {
	hostIP := c.Param("hostIp")
	port, err := bash.GetRemoteHostAvailablePort(hostIP)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, port)
	}
}

func LaunchPlugin(c *gin.Context) {
	pluginPackageId := c.Param("pluginPackage")
	hostIp := c.Param("hostIp")
	portValue := c.Param("port")
	port, _ := strconv.Atoi(portValue)
	if port < 20000 {
		middleware.ReturnError(c, fmt.Errorf("param port %s illegal", portValue))
		return
	}
	if running, err := database.CheckServerPortRunning(c, hostIp, port); err != nil {
		middleware.ReturnError(c, err)
		return
	} else {
		if running {
			middleware.ReturnError(c, fmt.Errorf("server:%s port:%d already in running", hostIp, port))
			return
		}
	}
	pluginPackageObj := models.PluginPackages{Id: pluginPackageId}
	if err := database.GetSimplePluginPackage(c, &pluginPackageObj, true); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	resources, getResourceErr := database.GetPluginRuntimeResources(c, pluginPackageId)
	if getResourceErr != nil {
		middleware.ReturnError(c, getResourceErr)
		return
	}
	if len(resources.Mysql) > 0 {
		mysqlResource := resources.Mysql[0]
		// 先检查数据库脚本执行纪录的版本，如果执行过了就跳过下面数据库相关操作
		mysqlInstance, getMysqlInsErr := database.GetPluginMysqlInstance(c, pluginPackageObj.Name)
		if getMysqlInsErr != nil {
			middleware.ReturnError(c, getMysqlInsErr)
			return
		}
		// 如果连纪录都没有，第一次要创建数据库
		mysqlServer, getMysqlServerErr := database.GetResourceServer(c, "mysql", hostIp)
		if getMysqlServerErr != nil {
			middleware.ReturnError(c, getMysqlServerErr)
			return
		}
		if mysqlInstance == nil {
			if dbPass, err := bash.CreatePluginDatabase(c, pluginPackageObj.Name, mysqlResource, mysqlServer); err != nil {
				middleware.ReturnError(c, err)
				return
			} else {
				mysqlInstance = &models.PluginMysqlInstances{
					Id:              "p_mysql_" + guid.CreateGuid(),
					Password:        dbPass,
					PluginPackageId: pluginPackageId,
					ResourceItemId:  mysqlResource.Id,
					SchemaName:      mysqlResource.SchemaName,
					Username:        pluginPackageObj.Name,
				}
				if err = database.NewPluginMysqlInstance(c, mysqlInstance); err != nil {
					middleware.ReturnError(c, err)
					return
				}
			}
		}
		// 把s3上的init.sql下载来到本地
		var intiSqlFile, upgradeSqlFile string
		if mysqlResource.InitFileName != "" {
			tmpFile, downloadErr := bash.DownloadPackageFile(models.Config.S3.PluginPackageBucket, fmt.Sprintf("%s/%s/%s", pluginPackageObj.Name, pluginPackageObj.Version, mysqlResource.InitFileName))
			if downloadErr != nil {
				middleware.ReturnError(c, downloadErr)
				return
			}
			intiSqlFile = tmpFile
		}
		if mysqlResource.UpgradeFileName != "" {
			tmpFile, downloadErr := bash.DownloadPackageFile(models.Config.S3.PluginPackageBucket, fmt.Sprintf("%s/%s/%s", pluginPackageObj.Name, pluginPackageObj.Version, mysqlResource.UpgradeFileName))
			if downloadErr != nil {
				middleware.ReturnError(c, downloadErr)
				return
			}
			upgradeSqlFile = tmpFile
		}
		// 检查数据库脚本是否有更新
		outputSqlFile, buildErr := bash.BuildPluginUpgradeSqlFile(intiSqlFile, upgradeSqlFile, mysqlInstance.PreVersion)
		if buildErr != nil {
			middleware.ReturnError(c, buildErr)
			return
		}
		// 执行数据库脚本并更新纪录
		if outputSqlFile != "" {
			if err := bash.ExecPluginUpgradeSql(c, mysqlInstance, mysqlServer, outputSqlFile); err != nil {
				middleware.ReturnError(c, err)
				return
			}
		}
	}
	dockerServer, getDockerServerErr := database.GetResourceServer(c, "docker", hostIp)
	if getDockerServerErr != nil {
		middleware.ReturnError(c, getDockerServerErr)
		return
	}
	// 替换容器参数差异化变量
	// 先检查目标机器上有没有相关版本容器镜像，如果有的话就跳过下面两个下载和传镜像的操作
	// 把s3上的image.tar下载来到本地
	// 把image.tar传到目标机器
	// 去目标机器上docker run起来，或使用docker-compose
	// 更新插件注册的菜单状态

}

func RemovePlugin(c *gin.Context) {
	// 销毁容器
	// 更新插件注册的菜单状态
}
