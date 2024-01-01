package plugin

import (
	"encoding/xml"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/tools"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/bash"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/gin-gonic/gin"
	"os"
	"regexp"
	"strconv"
	"strings"
)

// GetPackages 插件列表查询
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

// UploadPackage 上传插件
func UploadPackage(c *gin.Context) {
	// 接收插件zip文件
	fileName, fileBytes, err := middleware.ReadFormFile(c, "zip-file")
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	log.Logger.Debug("zip-file", log.String("name", fileName), log.Int("len", len(fileBytes)))
	// 解压插件zip包
	var tmpFilePath, tmpFileDir string
	if tmpFilePath, tmpFileDir, err = bash.SaveTmpFile(fileName, fileBytes); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	log.Logger.Debug("tmpFile", log.String("tmpFilePath", tmpFilePath))
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

// GetPluginDependencies 插件配置 - 依赖分析
func GetPluginDependencies(c *gin.Context) {
	pluginPackageId := c.Param("pluginPackageId")
	result, err := database.GetPluginDependencies(c, pluginPackageId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

// GetPluginMenus 插件配置 - 菜单注入
func GetPluginMenus(c *gin.Context) {
	pluginPackageId := c.Param("pluginPackageId")
	result, err := database.GetPluginMenus(c, pluginPackageId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

// GetPluginSystemParameters 插件配置 - 系统参数
func GetPluginSystemParameters(c *gin.Context) {
	pluginPackageId := c.Param("pluginPackageId")
	result, err := database.GetPluginSystemParameters(c, pluginPackageId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

// GetPluginAuthorities 插件配置 - 权限设定
func GetPluginAuthorities(c *gin.Context) {
	pluginPackageId := c.Param("pluginPackageId")
	result, err := database.GetPluginAuthorities(c, pluginPackageId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

// GetPluginRuntimeResources 插件配置 - 运行资源
func GetPluginRuntimeResources(c *gin.Context) {
	pluginPackageId := c.Param("pluginPackageId")
	result, err := database.GetPluginRuntimeResources(c, pluginPackageId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

// GetAvailableContainerHost 运行管理 - 可用容器主机查询
func GetAvailableContainerHost(c *gin.Context) {
	result, err := database.GetAvailableContainerHost()
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

// RegisterPackage 插件配置 - 注册插件包
func RegisterPackage(c *gin.Context) {
	pluginPackageId := c.Param("pluginPackageId")
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

// GetHostAvailablePort 运行管理 - 主机可用端口查询
func GetHostAvailablePort(c *gin.Context) {
	hostIP := c.Param("hostIp")
	resourceServer, err := database.GetResourceServerByIp(hostIP)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	port, getPortErr := bash.GetRemoteHostAvailablePort(resourceServer)
	if getPortErr != nil {
		middleware.ReturnError(c, getPortErr)
	} else {
		middleware.ReturnData(c, port)
	}
}

// LaunchPlugin 运行管理 - 插件实例创建
func LaunchPlugin(c *gin.Context) {
	pluginPackageId := c.Param("pluginPackageId")
	hostIp := c.Param("hostIp")
	portValue := c.Param("port")
	port, _ := strconv.Atoi(portValue)
	if port < 20000 {
		middleware.ReturnError(c, fmt.Errorf("param port %s illegal", portValue))
		return
	}
	if running, err := database.CheckServerPortRunning(c, hostIp, port); err != nil {
		log.Logger.Error("check server port running fail", log.Error(err))
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
		log.Logger.Error("GetSimplePluginPackage fail", log.Error(err))
		middleware.ReturnError(c, err)
		return
	}
	log.Logger.Debug("pluginPackage", log.JsonObj("data", pluginPackageObj))
	resources, getResourceErr := database.GetPluginRuntimeResources(c, pluginPackageId)
	if getResourceErr != nil {
		log.Logger.Error("GetPluginRuntimeResources fail", log.Error(getResourceErr))
		middleware.ReturnError(c, getResourceErr)
		return
	}
	if len(resources.Docker) == 0 {
		middleware.ReturnError(c, fmt.Errorf("plugin must contain docker resource"))
		return
	}
	operator := middleware.GetRequestUser(c)
	var mysqlInstance *models.PluginMysqlInstances
	var mysqlServer *models.ResourceServer
	pluginInstance := models.PluginInstances{
		Id:              "p_docker_" + guid.CreateGuid(),
		Host:            hostIp,
		ContainerName:   fmt.Sprintf("%s-%s", pluginPackageObj.Name, pluginPackageObj.Version),
		Port:            port,
		ContainerStatus: "RUNNING",
		PackageId:       pluginPackageId,
		InstanceName:    pluginPackageObj.Name,
	}
	if len(resources.Mysql) > 0 {
		mysqlResource := resources.Mysql[0]
		pluginInstance.PluginMysqlInstanceResourceId = mysqlResource.Id
		// 先检查数据库脚本执行纪录的版本，如果执行过了就跳过下面数据库相关操作
		var resourceDbErr error
		mysqlInstance, resourceDbErr = database.GetPluginMysqlInstance(c, pluginPackageObj.Name)
		if resourceDbErr != nil {
			middleware.ReturnError(c, resourceDbErr)
			return
		}
		// 如果连纪录都没有，第一次要创建数据库
		mysqlServer, resourceDbErr = database.GetResourceServer(c, "mysql", hostIp)
		if resourceDbErr != nil {
			middleware.ReturnError(c, resourceDbErr)
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
				log.Logger.Debug("database pwd", log.String("pass", dbPass))
				if err = database.NewPluginMysqlInstance(c, mysqlServer, mysqlInstance, operator); err != nil {
					middleware.ReturnError(c, err)
					return
				}
			}
		}
		if tools.CompareVersion(pluginPackageObj.Version, mysqlInstance.PreVersion) {
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
					log.Logger.Warn("plugin have no upgrade sql", log.String("plugin", pluginPackageObj.Name), log.String("version", pluginPackageObj.Version))
				} else {
					upgradeSqlFile = tmpFile
				}
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
			if err := database.UpdatePluginMysqlInstancePreVersion(c, mysqlInstance.Id, pluginPackageObj.Version); err != nil {
				middleware.ReturnError(c, err)
				return
			}
		}
	}
	dockerResource := resources.Docker[0]
	pluginInstance.DockerInstanceResourceId = dockerResource.Id
	dockerServer, getDockerServerErr := database.GetResourceServer(c, "docker", hostIp)
	if getDockerServerErr != nil {
		middleware.ReturnError(c, getDockerServerErr)
		return
	}
	envMap := make(map[string]string)
	portBindList := getEnvMap(dockerResource.PortBindings, envMap)
	volumeBindList := getEnvMap(dockerResource.VolumeBindings, envMap)
	envBindList := getEnvMap(dockerResource.EnvVariables, envMap)
	envMap["ALLOCATE_PORT"] = portValue
	envMap["BASE_MOUNT_PATH"] = models.Config.Plugin.BaseMountPath
	if mysqlInstance != nil {
		envMap["DB_SCHEMA"] = mysqlInstance.SchemaName
		envMap["DB_USER"] = mysqlInstance.Username
		envMap["DB_PWD"] = mysqlInstance.Password
		if mysqlServer != nil {
			envMap["DB_HOST"] = mysqlServer.Host
			envMap["DB_PORT"] = mysqlServer.Port
		}
	}
	// 替换容器参数差异化变量
	replaceMap, err := database.BuildDockerEnvMap(c, envMap)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	portBindList = replaceEnvMap(portBindList, replaceMap)
	volumeBindList = replaceEnvMap(volumeBindList, replaceMap)
	envBindList = replaceEnvMap(envBindList, replaceMap)
	// 先检查目标机器上有没有相关版本容器镜像，如果有的话就跳过下面两个下载和传镜像的操作
	// 把s3上的image.tar下载来到本地？可否直接让目标机器下载image.tar
	tmpImageFile, downloadImageErr := bash.DownloadPackageFile(models.Config.S3.PluginPackageBucket, fmt.Sprintf("%s/%s/image.tar", pluginPackageObj.Name, pluginPackageObj.Version))
	if downloadImageErr != nil {
		middleware.ReturnError(c, downloadImageErr)
		return
	}
	// 把image.tar传到目标机器
	targetImagePath := fmt.Sprintf("%s/%s_%s_image.tar", models.Config.Plugin.DeployPath, pluginPackageObj.Name, pluginPackageObj.Version)
	if err = bash.RemoteSCP(dockerServer.Host, tmpImageFile, targetImagePath); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	log.Logger.Info("scp plugin image file", log.String("targetHost", dockerServer.Host), log.String("tmpFile", tmpImageFile), log.String("targetPath", targetImagePath))
	if err = bash.RemoteSSHCommand(dockerServer.Host, fmt.Sprintf("docker load --input %s && rm -f %s", targetImagePath, targetImagePath)); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 去目标机器上docker run起来，或使用docker-compose
	dockerCmd := fmt.Sprintf("docker run -d --name %s ", pluginInstance.ContainerName)
	for _, v := range volumeBindList {
		dockerCmd += fmt.Sprintf("--volume %s ", v)
	}
	for _, v := range portBindList {
		dockerCmd += fmt.Sprintf("-p %s ", v)
	}
	for _, v := range envBindList {
		dockerCmd += fmt.Sprintf("-e %s ", v)
	}
	dockerCmd += fmt.Sprintf(" %s:%s ", pluginPackageObj.Name, pluginPackageObj.Version)
	if err = bash.RemoteSSHCommand(dockerServer.Host, dockerCmd); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 更新插件注册的菜单状态和更新插件实例数据
	if len(resources.S3) > 0 {
		pluginInstance.S3bucketResourceId = resources.S3[0].Id
	}
	err = database.LaunchPlugin(c, &pluginInstance)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnSuccess(c)
	}
}

// RemovePlugin 运行管理 - 插件实例销毁
func RemovePlugin(c *gin.Context) {
	pluginInstanceId := c.Param("pluginInstanceId")
	pluginInstanceObj, err := database.GetPluginInstance(pluginInstanceId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	pluginPackageObj := models.PluginPackages{Id: pluginInstanceObj.PackageId}
	if err = database.GetSimplePluginPackage(c, &pluginPackageObj, true); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 销毁容器
	removeCmd := fmt.Sprintf("docker rm -f %s && docker rmi %s:%s", pluginInstanceObj.ContainerName, pluginPackageObj.Name, pluginPackageObj.Version)
	if err = bash.RemoteSSHCommand(pluginInstanceObj.Host, removeCmd); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 更新插件注册的菜单状态和更新插件实例数据
	err = database.RemovePlugin(c, pluginPackageObj.Id, pluginInstanceId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnSuccess(c)
	}
}

func getEnvMap(input string, envMap map[string]string) (inputList []string) {
	re, _ := regexp.Compile(".*{{(.*)}}.*")
	inputList = strings.Split(input, ",")
	for _, v := range inputList {
		for i, matchEnv := range re.FindStringSubmatch(v) {
			if i == 0 {
				continue
			}
			envMap[matchEnv] = ""
		}
	}
	return
}

func replaceEnvMap(inputList []string, replaceMap map[string]string) (outputList []string) {
	for _, input := range inputList {
		inputV := input
		if strings.Contains(input, "{{") {
			for k, v := range replaceMap {
				inputV = strings.ReplaceAll(inputV, k, v)
			}
		}
		outputList = append(outputList, inputV)
	}
	return
}

func GetPluginRunningInstances(c *gin.Context) {
	pluginPackageId := c.Param("pluginPackageId")
	result, err := database.GetPluginRunningInstances(c, pluginPackageId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}
