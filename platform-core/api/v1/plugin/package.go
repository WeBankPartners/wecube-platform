package plugin

import (
	"context"
	"encoding/json"
	"encoding/xml"
	"fmt"
	"os"
	"regexp"
	"strconv"
	"strings"
	"time"

	"go.uber.org/zap"

	"github.com/WeBankPartners/go-common-lib/cipher"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/encrypt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/tools"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/try"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/bash"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"github.com/gin-gonic/gin"
)

// GetPackages 插件列表查询
func GetPackages(c *gin.Context) {
	//allPackageFlag := strings.ToLower(c.Query("all"))
	//allFlag := false
	//if allPackageFlag == "yes" || allPackageFlag == "y" || allPackageFlag == "true" {
	//	allFlag = true
	//}
	//result, err := database.GetPackages(c, allFlag)
	queryParam := models.PluginPackageQueryParam{}
	queryParam.Id = c.Query("id")
	queryParam.Name = c.Query("name")
	queryParam.UpdatedBy = c.Query("updatedBy")
	queryParam.WithRunningInstance = strings.ToLower(c.Query("running"))
	if c.Query("withDelete") == "yes" {
		queryParam.WithDelete = true
	}
	result, err := database.QueryPluginPackages(c, &queryParam)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func GetWebRunningPackages(c *gin.Context) {
	result, err := database.QueryWebRunningPluginPackages(c)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

// UploadPackage 上传插件
func UploadPackage(c *gin.Context) {
	var tmpFilePath, tmpFileDir string
	var err error
	localFilePath := c.GetHeader("Local-File-Path")
	if localFilePath != "" {
		tmpFilePath, tmpFileDir, err = bash.CopyTmpFile(localFilePath)
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
	} else {
		// 接收插件zip文件
		fileName, fileBytes, readFileErr := middleware.ReadFormFile(c, "zip-file")
		if readFileErr != nil {
			middleware.ReturnError(c, readFileErr)
			return
		}
		log.Debug(nil, log.LOGGER_APP, "zip-file", zap.String("name", fileName))
		// 解压插件zip包
		if tmpFilePath, tmpFileDir, err = bash.SaveTmpFile(fileName, fileBytes); err != nil {
			middleware.ReturnError(c, err)
			return
		}
	}
	// fileBytes = []byte{}
	log.Debug(nil, log.LOGGER_APP, "tmpFile", zap.String("tmpFilePath", tmpFilePath))
	defer func() {
		if removeTmpDirErr := os.RemoveAll(tmpFileDir); removeTmpDirErr != nil {
			log.Error(nil, log.LOGGER_APP, "Try to remove package upload tmp dir fail", zap.String("tmpDir", tmpFileDir), zap.Error(removeTmpDirErr))
		}
	}()
	if _, err = bash.DecompressFile(tmpFilePath, ""); err != nil {
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
	var samePackageNameNum int
	samePackageNameNum, err = database.GetPluginPackageNum(c, pluginPackageObj.Name)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if samePackageNameNum >= 3 {
		middleware.ReturnError(c, fmt.Errorf("Package num limit 3 "))
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
	if registerConfig.ResourceDependencies.S3.BucketName != "" {
		// 尝试创建定义的bucket
		if err = bash.MakeBucket(registerConfig.ResourceDependencies.S3.BucketName); err != nil {
			middleware.ReturnError(c, err)
			return
		}
		if len(registerConfig.ResourceDependencies.S3.FileSet.File) > 0 {
			for _, fileSetObj := range registerConfig.ResourceDependencies.S3.FileSet.File {
				s3FileMap[fmt.Sprintf("%s/%s", tmpFileDir, fileSetObj.Source)] = s3Prefix + fileSetObj.Source
			}
		}
	}
	if err = bash.UploadPluginPackage(models.Config.S3.ServerAddress, models.Config.S3.AccessKey, models.Config.S3.SecretKey, models.Config.S3.PluginPackageBucket, s3FileMap); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 写数据库
	enterprise := false
	if registerConfig.Edition == "enterprise" {
		enterprise = true
	}
	var pluginPackageId string
	pluginPackageId, err = database.UploadPackage(c, &registerConfig, withUi, enterprise, "", middleware.GetRequestUser(c))
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		if registerConfig.Authorities.Authority.SystemRoleName != "" {
			var menuCodeList []string
			for _, v := range registerConfig.Authorities.Authority.Menu {
				menuCodeList = append(menuCodeList, v.Code)
			}
			if len(menuCodeList) > 0 {
				updateRoleErr := updateRoleMenuWithPackage(c, registerConfig.Authorities.Authority.SystemRoleName, menuCodeList)
				if updateRoleErr != nil {
					log.Error(nil, log.LOGGER_APP, "updateRoleMenuWithPackage fail", zap.String("pluginPackageId", pluginPackageId), zap.Error(updateRoleErr))
				} else {
					log.Info(nil, log.LOGGER_APP, "updateRoleMenuWithPackage done", zap.String("pluginPackageId", pluginPackageId), zap.Strings("menuCodeList", menuCodeList))
				}
			}
		}
		resultData := models.PackageIdRespData{Id: pluginPackageId}
		middleware.ReturnData(c, resultData)
	}
}

func updateRoleMenuWithPackage(ctx *gin.Context, roleName string, menuCodeList []string) (err error) {
	language := ctx.GetHeader(middleware.AcceptLanguageHeader)
	respData, getRolesErr := remote.RetrieveAllLocalRoles("Y", remote.GetToken(), language, false)
	if getRolesErr != nil {
		err = fmt.Errorf("get all roles fail,%s ", getRolesErr.Error())
		return
	}
	var roleId string
	for _, v := range respData.Data {
		if v.Name == roleName {
			roleId = v.ID
			break
		}
	}
	if roleId == "" {
		err = fmt.Errorf("can not find role id with name:%s ", roleName)
		return
	}
	var needAddAuthoritiesToGrantList []*models.SimpleAuthorityDto
	for _, v := range menuCodeList {
		needAddAuthoritiesToGrantList = append(needAddAuthoritiesToGrantList, &models.SimpleAuthorityDto{Code: v})
	}
	err = remote.ConfigureRoleWithAuthoritiesById(roleId, remote.GetToken(), language, needAddAuthoritiesToGrantList)
	if err != nil {
		err = fmt.Errorf("ConfigureRoleWithAuthoritiesById fail,%s ", err.Error())
	} else {
		for _, code := range menuCodeList {
			roleMenu := models.RoleMenu{
				Id:       guid.CreateGuid(),
				RoleName: roleName,
				MenuCode: code,
			}
			if err = database.AddRoleMenu(ctx, roleMenu); err != nil {
				break
			}
		}
	}
	return
}

// ListOnliePackage 获取在线插件列表
func ListOnliePackage(c *gin.Context) {
	results, err := remote.GetOnliePluginPackageList(c)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, results)
	}
}

// PullOnliePackage 注册在线插件
func PullOnliePackage(c *gin.Context) {
	reqParam := models.PullOnliePackageRequest{}
	if err := c.ShouldBindJSON(&reqParam); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	pullId := "pluginPull_" + guid.CreateGuid()
	_, err := database.CreatePluginPackagePullReq(c, &models.PluginArtifactPullReq{
		Id:      pullId,
		KeyName: reqParam.KeyName,
		State:   "InProgress",
	}, c.GetString(models.ContextUserId))
	log.Debug(nil, log.LOGGER_APP, "pull plugin package,create plugin package pull req", log.JsonObj("pullId", pullId))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	go doPullPackageBackground(c, pullId, reqParam.KeyName)
	middleware.ReturnData(c, models.PullOnliePackageResponse{KeyName: reqParam.KeyName, RequestId: pullId, State: "InProgress"})
}

func doPullPackageBackground(c *gin.Context, pullId, fileName string) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		database.UpdatePluginPackagePullReq(c, pullId, "", "Faulted", retErr.Error(), "", 0)
		log.Error(nil, log.LOGGER_APP, e.(string))
	})
	tmpFile, err := remote.GetOnlinePluginPackageFile(c, fileName)
	log.Debug(nil, log.LOGGER_APP, "pull plugin package,get online plugin package archive file", log.JsonObj("fileName", fileName))
	if err != nil {
		// update failed
		database.UpdatePluginPackagePullReq(c, pullId, "", "Faulted", err.Error(), "", 0)
		log.Error(nil, log.LOGGER_APP, "pull plugin package,get online plugin package archive file failed", log.JsonObj("fileName", fileName), log.JsonObj("errMsg", err.Error()))
		return
	}
	tmpFileSize := 0
	if tmpFile != nil {
		fileInfo, _ := tmpFile.Stat()
		if fileInfo != nil {
			tmpFileSize = int(fileInfo.Size())
		}
		log.Debug(nil, log.LOGGER_APP, "pull plugin package,get online plugin package file size", log.JsonObj("fileName", fileName), log.JsonObj("fileSize", tmpFileSize))
	} else {
		// update failed
		database.UpdatePluginPackagePullReq(c, pullId, "", "Faulted", "failed to download file", "", 0)
		log.Error(nil, log.LOGGER_APP, "pull plugin package,get online plugin package archive file failed", log.JsonObj("fileName", fileName), log.JsonObj("errMsg", "failed to download file"))
		return
	}
	archiveFilePath := tmpFile.Name()
	pkgId, err := doUploadPackage(c, archiveFilePath, middleware.GetRequestUser(c))
	if err != nil {
		// update failed
		database.UpdatePluginPackagePullReq(c, pullId, "", "Faulted", err.Error(), "", tmpFileSize)
		log.Error(nil, log.LOGGER_APP, "pull plugin package,upload plugin package archive file failed", log.JsonObj("fileName", fileName), log.JsonObj("errMsg", err.Error()))
	} else {
		// update ok
		database.UpdatePluginPackagePullReq(c, pullId, pkgId, "Completed", "", "", tmpFileSize)
		log.Debug(nil, log.LOGGER_APP, "pull plugin package,upload online plugin package archive file ok", log.JsonObj("fileName", fileName))

	}
	// 清理
	os.Remove(archiveFilePath)
	log.Debug(nil, log.LOGGER_APP, "pull plugin package,clean up plugin package tmp file", log.JsonObj("fileName", fileName), log.JsonObj("archiveFilePath", archiveFilePath))
}

func doUploadPackage(c context.Context, archiveFilePath, operator string) (pluginPkgId string, err error) {
	tmpFileDir := fmt.Sprintf("/tmp/%d", time.Now().UnixNano())
	if err = os.MkdirAll(tmpFileDir, 0700); err != nil {
		err = fmt.Errorf("make tmp dir fail,%s ", err.Error())
		return
	}
	// 上传包
	log.Debug(nil, log.LOGGER_APP, "tmpFile", zap.String("tmpFileDir", tmpFileDir))
	defer func() {
		if removeTmpDirErr := os.RemoveAll(tmpFileDir); removeTmpDirErr != nil {
			log.Error(nil, log.LOGGER_APP, "Try to remove package upload tmp dir fail", zap.String("tmpDir", tmpFileDir), zap.Error(removeTmpDirErr))
		}
	}()
	if _, err = bash.DecompressFile(archiveFilePath, tmpFileDir); err != nil {
		return
	}
	packageFiles, readErr := bash.ListDirFiles(tmpFileDir)
	if readErr != nil {
		err = readErr
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
		err = fmt.Errorf("register xml and image tar can not empty")
		return
	}
	var registerConfig models.RegisterXML
	registerConfigBytes, readRegisterConfigErr := os.ReadFile(fmt.Sprintf("%s/%s", tmpFileDir, registerFile))
	if readRegisterConfigErr != nil {
		err = fmt.Errorf("read register xml file fail,%s ", readRegisterConfigErr.Error())
		return
	}
	if err = xml.Unmarshal(registerConfigBytes, &registerConfig); err != nil {
		err = fmt.Errorf("xml unmarshal regisger xml fail,%s ", err.Error())
		return
	}
	pluginPackageObj := models.PluginPackages{Name: registerConfig.Name, Version: registerConfig.Version}
	if err = database.GetSimplePluginPackage(c, &pluginPackageObj, false); err != nil {
		return
	}
	if pluginPackageObj.Id != "" {
		err = fmt.Errorf("plugin %s:%s already existed", registerConfig.Name, registerConfig.Version)
		return
	}
	if registerConfig.ResourceDependencies.Mysql.InitFileName != "" {
		initSql = registerConfig.ResourceDependencies.Mysql.InitFileName
		upgradeSql = registerConfig.ResourceDependencies.Mysql.UpgradeFileName
		if !bash.ListContains(packageFiles, initSql) {
			err = fmt.Errorf("init sql file:%s can not find in package", initSql)
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
	if err = bash.UploadPluginPackage(models.Config.S3.ServerAddress, models.Config.S3.AccessKey, models.Config.S3.SecretKey, models.Config.S3.PluginPackageBucket, s3FileMap); err != nil {
		return
	}
	if registerConfig.ResourceDependencies.S3.BucketName != "" {
		if err = bash.MakeBucket(registerConfig.ResourceDependencies.S3.BucketName); err != nil {
			return
		}
	}
	// 写数据库
	pkgId := "plugin_" + guid.CreateGuid()
	_, err = database.UploadPackage(c, &registerConfig, withUi, false, pkgId, operator)
	if err != nil {
		return
	}
	return pkgId, nil
}

// PullOnliePackageStatus 注册在线插件状态
func PullOnliePackageStatus(c *gin.Context) {
	pullId := c.Param("pullId")
	result, err := database.GetPluginPackagePullReq(c, pullId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		data := make(map[string]interface{})
		if result != nil {
			data["keyName"] = result.KeyName
			data["state"] = result.State
			data["requestId"] = pullId
			data["errorMessage"] = result.ErrMsg
			data["pluginPackageId"] = result.PkgId
		}
		middleware.ReturnData(c, data)
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
		if len(models.Config.StaticResources) == 0 {
			middleware.ReturnError(c, fmt.Errorf("static resource config empty"))
			return
		}
		// 把s3上的ui.zip下下来放到本地
		log.Debug(nil, log.LOGGER_APP, "register plugin,start download ui.zip")
		var uiFileLocalPath, uiDir string
		if uiFileLocalPath, err = bash.DownloadPackageFile(models.Config.S3.PluginPackageBucket, fmt.Sprintf("%s/%s/ui.zip", pluginPackageObj.Name, pluginPackageObj.Version)); err != nil {
			middleware.ReturnError(c, err)
			return
		}
		defer bash.RemoveTmpFile(uiFileLocalPath)
		log.Debug(nil, log.LOGGER_APP, "register plugin,start decompress ui.zip", zap.String("uiFileLocalPath", uiFileLocalPath))
		// 本地解压ui.zip
		if uiDir, err = bash.DecompressFile(uiFileLocalPath, ""); err != nil {
			middleware.ReturnError(c, err)
			return
		}
		defer bash.RemoveTmpFile(uiDir)
		// 把ui.zip用ssh传到静态资源服务器上并解压，如果有两台服务器，则每台都要上传与解压
		for _, staticResourceObj := range models.Config.StaticResources {
			targetPath := fmt.Sprintf("%s/%s/%s/ui.zip", staticResourceObj.Path, pluginPackageObj.Name, pluginPackageObj.Version)
			unzipCmd := fmt.Sprintf("cd %s/%s/%s && unzip -o ui.zip && rm -f ui.zip", staticResourceObj.Path, pluginPackageObj.Name, pluginPackageObj.Version)
			log.Debug(nil, log.LOGGER_APP, "register plugin,start scp ui.zip to remote host", zap.String("server", staticResourceObj.Server), zap.String("targetPath", targetPath))
			if err = bash.RemoteSCP(staticResourceObj.Server, staticResourceObj.User, staticResourceObj.Password, staticResourceObj.Port, uiFileLocalPath, targetPath); err != nil {
				break
			}
			log.Debug(nil, log.LOGGER_APP, "register plugin,start unzip ui.zip in remote host", zap.String("server", staticResourceObj.Server), zap.String("unzipCmd", unzipCmd))
			if err = bash.RemoteSSHCommand(staticResourceObj.Server, staticResourceObj.User, staticResourceObj.Password, staticResourceObj.Port, unzipCmd); err != nil {
				break
			}
		}
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
		// 把ui.zip里的静态文件读出来
		var fileNameList []string
		indexPath, matchIndexFlag, findErr := bash.GetDirIndexPath(uiDir)
		if findErr != nil {
			middleware.ReturnError(c, findErr)
			return
		}
		if !matchIndexFlag {
			middleware.ReturnError(c, fmt.Errorf("can not find index.html in ui package"))
			return
		}
		log.Debug(nil, log.LOGGER_APP, "match index path", zap.String("indexPath", indexPath))
		indexPath = strings.TrimSuffix(indexPath, "/")
		dirPrefix := uiDir
		if indexPath != "" {
			dirPrefix = uiDir + "/" + indexPath
		}
		fileNameList, err = bash.ListDirAllFiles(dirPrefix)
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
		uiStaticPath := models.Config.StaticResources[0].Path
		if pathIndex := strings.LastIndex(uiStaticPath, "/"); pathIndex >= 0 {
			uiStaticPath = uiStaticPath[pathIndex:]
		}
		uiStaticPath = fmt.Sprintf("%s/%s/%s", uiStaticPath, pluginPackageObj.Name, pluginPackageObj.Version)
		if indexPath != "" {
			uiStaticPath = uiStaticPath + "/" + indexPath
		}
		resourceFileList := []*models.PluginPackageResourceFiles{}
		for _, v := range fileNameList {
			tmpResourceObj := models.PluginPackageResourceFiles{PluginPackageId: pluginPackageId, PackageName: pluginPackageObj.Name, PackageVersion: pluginPackageObj.Version, Source: "ui.zip", RelatedPath: strings.ReplaceAll(v, dirPrefix, uiStaticPath)}
			resourceFileList = append(resourceFileList, &tmpResourceObj)
		}
		if len(resourceFileList) > 0 {
			log.Debug(nil, log.LOGGER_APP, "register plugin,start update plugin static resource file data", log.JsonObj("resourceFileList", resourceFileList))
			if err = database.UpdatePluginStaticResourceFiles(c, pluginPackageId, pluginPackageObj.Name, resourceFileList, middleware.GetRequestUser(c)); err != nil {
				middleware.ReturnError(c, err)
				return
			}
		}
	}
	// 把对应插件版本的系统变量置为active
	if err = database.RegisterPlugin(c, pluginPackageObj.Name, pluginPackageObj.Version, middleware.GetRequestUser(c)); err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, models.PackageIdRespData{Id: pluginPackageId})
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
		log.Error(nil, log.LOGGER_APP, "check server port running fail", zap.Error(err))
		middleware.ReturnError(c, err)
		return
	} else {
		if running {
			middleware.ReturnError(c, fmt.Errorf("server:%s port:%d already in running", hostIp, port))
			return
		}
	}
	err := LaunchPluginFunc(c, pluginPackageId, hostIp, middleware.GetRequestUser(c), port)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnSuccess(c)
	}
}

func LaunchPluginFunc(ctx context.Context, pluginPackageId, hostIp, operator string, port int) (err error) {
	pluginPackageObj := models.PluginPackages{Id: pluginPackageId}
	if err = database.GetSimplePluginPackage(ctx, &pluginPackageObj, true); err != nil {
		log.Error(nil, log.LOGGER_APP, "GetSimplePluginPackage fail", zap.Error(err))
		return
	}
	existPluginInstance, getExistErr := database.GetPluginInstance("", pluginPackageObj.Name, hostIp, "", false)
	if getExistErr != nil {
		err = getExistErr
		return
	}
	if existPluginInstance.Id != "" {
		err = fmt.Errorf("Host:%s already running plugin:%s ", hostIp, pluginPackageObj.Name)
		return
	}
	log.Debug(nil, log.LOGGER_APP, "pluginPackage", log.JsonObj("data", pluginPackageObj))
	resources, getResourceErr := database.GetPluginRuntimeResources(ctx, pluginPackageId)
	if getResourceErr != nil {
		log.Error(nil, log.LOGGER_APP, "GetPluginRuntimeResources fail", zap.Error(getResourceErr))
		err = getResourceErr
		return
	}
	if len(resources.Docker) == 0 {
		err = fmt.Errorf("plugin must contain docker resource")
		return
	}
	if len(resources.S3) > 0 {
		s3Resource := resources.S3[0]
		if s3Resource.AdditionalProperties != "" && s3Resource.AdditionalProperties != "[]" {
			var fileAdditionList []*models.PluginS3ResourceFileObj
			if tmpErr := json.Unmarshal([]byte(s3Resource.AdditionalProperties), &fileAdditionList); tmpErr == nil {
				if len(fileAdditionList) > 0 {
					s3ResourceServer, getS3ResourceErr := database.GetResourceServer(ctx, "s3", "", "", "")
					if getS3ResourceErr != nil {
						err = getS3ResourceErr
						return
					}
					for _, fileSetObj := range fileAdditionList {
						tmpS3UploadFile, downloadS3UploadFileErr := bash.DownloadPackageFile(models.Config.S3.PluginPackageBucket, fmt.Sprintf("%s/%s/%s", pluginPackageObj.Name, pluginPackageObj.Version, fileSetObj.Source))
						if downloadS3UploadFileErr != nil {
							err = downloadS3UploadFileErr
							break
						}
						tmpUploadFileMap := make(map[string]string)
						tmpUploadFileMap[tmpS3UploadFile] = fileSetObj.Target
						if err = bash.UploadPluginPackage(fmt.Sprintf("%s:%s", s3ResourceServer.Host, s3ResourceServer.Port), s3ResourceServer.LoginUsername, s3ResourceServer.LoginPassword, s3Resource.BucketName, tmpUploadFileMap); err != nil {
							break
						}
					}
				}
			} else {
				log.Warn(nil, log.LOGGER_APP, "Try to json unmarshal s3 resource addition fail", zap.String("additionalProperties", s3Resource.AdditionalProperties), zap.Error(tmpErr))
			}
			if err != nil {
				return
			}
		}
	}
	var mysqlInstance *models.PluginMysqlInstances
	var mysqlServer *models.ResourceServer
	pluginInstance := models.PluginInstances{
		Id:              "p_docker_" + guid.CreateGuid(),
		Host:            hostIp,
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
		mysqlInstance, resourceDbErr = database.GetPluginMysqlInstance(ctx, pluginPackageObj.Name)
		if resourceDbErr != nil {
			err = resourceDbErr
			return
		}
		if mysqlInstance != nil {
			mysqlServer, resourceDbErr = database.GetResourceServer(ctx, "", "", "", mysqlInstance.ResourceItemId)
			if resourceDbErr != nil {
				err = resourceDbErr
				return
			}
		} else {
			// 判断有没有以以插件名为类型的资源实例
			resourceItemList, getItemErr := database.GetResourceItem(ctx, "mysql_database", mysqlResource.SchemaName, true)
			if getItemErr != nil {
				err = getItemErr
				return
			}
			if len(resourceItemList) > 0 {
				mysqlServer, resourceDbErr = database.GetResourceServer(ctx, "", "", "", resourceItemList[0].Id)
				if resourceDbErr != nil {
					err = resourceDbErr
					return
				}
				mysqlInstance = &models.PluginMysqlInstances{
					Id:              "p_mysql_" + guid.CreateGuid(),
					Password:        resourceItemList[0].Password,
					PluginPackageId: pluginPackageId,
					ResourceItemId:  resourceItemList[0].Id,
					SchemaName:      mysqlResource.SchemaName,
					Username:        resourceItemList[0].Username,
				}
				if err = database.NewPluginMysqlInstance(ctx, mysqlServer, mysqlInstance, operator, false); err != nil {
					return
				}
			} else {
				// 如果连纪录都没有，第一次要创建数据库
				mysqlServer, resourceDbErr = database.GetResourceServer(ctx, "mysql", "", "", "")
				if resourceDbErr != nil {
					err = resourceDbErr
					return
				}
				if mysqlInstance == nil {
					mysqlUsername := strings.ReplaceAll(pluginPackageObj.Name, "-", "_")
					if dbPass, createDBErr := bash.CreatePluginDatabase(ctx, mysqlUsername, mysqlResource, mysqlServer); createDBErr != nil {
						err = createDBErr
						return
					} else {
						mysqlInstance = &models.PluginMysqlInstances{
							Id:              "p_mysql_" + guid.CreateGuid(),
							Password:        dbPass,
							PluginPackageId: pluginPackageId,
							ResourceItemId:  "rs_item_" + guid.CreateGuid(),
							SchemaName:      mysqlResource.SchemaName,
							Username:        mysqlUsername,
						}
						log.Debug(nil, log.LOGGER_APP, "database pwd", zap.String("pass", dbPass))
						if err = database.NewPluginMysqlInstance(ctx, mysqlServer, mysqlInstance, operator, true); err != nil {
							return
						}
					}
				}
			}
		}
		if tools.CompareVersion(pluginPackageObj.Version, mysqlInstance.PreVersion) {
			// 把s3上的init.sql下载来到本地
			var intiSqlFile, upgradeSqlFile string
			if mysqlResource.InitFileName != "" {
				tmpFile, downloadErr := bash.DownloadPackageFile(models.Config.S3.PluginPackageBucket, fmt.Sprintf("%s/%s/%s", pluginPackageObj.Name, pluginPackageObj.Version, mysqlResource.InitFileName))
				if downloadErr != nil {
					err = downloadErr
					return
				}
				defer bash.RemoveTmpFile(tmpFile)
				intiSqlFile = tmpFile
			}
			if mysqlResource.UpgradeFileName != "" {
				tmpFile, downloadErr := bash.DownloadPackageFile(models.Config.S3.PluginPackageBucket, fmt.Sprintf("%s/%s/%s", pluginPackageObj.Name, pluginPackageObj.Version, mysqlResource.UpgradeFileName))
				if downloadErr != nil {
					log.Warn(nil, log.LOGGER_APP, "plugin have no upgrade sql", zap.String("plugin", pluginPackageObj.Name), zap.String("version", pluginPackageObj.Version))
				} else {
					defer bash.RemoveTmpFile(tmpFile)
					upgradeSqlFile = tmpFile
				}
			}
			// 检查数据库脚本是否有更新
			outputSqlFile, buildErr := bash.BuildPluginUpgradeSqlFile(intiSqlFile, upgradeSqlFile, mysqlInstance.PreVersion)
			if buildErr != nil {
				err = buildErr
				return
			}
			// 执行数据库脚本并更新纪录
			if outputSqlFile != "" {
				if err = bash.ExecPluginUpgradeSql(ctx, mysqlInstance, mysqlServer, outputSqlFile); err != nil {
					return
				}
			}
			if err = database.UpdatePluginMysqlInstancePreVersion(ctx, mysqlInstance.Id, pluginPackageObj.Version); err != nil {
				return
			}
		}
	}
	dockerResource := resources.Docker[0]
	pluginInstance.ContainerName = dockerResource.ContainerName
	dockerServer, getDockerServerErr := database.GetResourceServer(ctx, "docker", hostIp, "", "")
	if getDockerServerErr != nil {
		err = getDockerServerErr
		return
	}
	envMap := make(map[string]string)
	portBindList := getEnvMap(dockerResource.PortBindings, envMap)
	volumeBindList := getEnvMap(dockerResource.VolumeBindings, envMap)
	envBindList := getEnvMap(dockerResource.EnvVariables, envMap)
	envMap["ALLOCATE_PORT"] = fmt.Sprintf("%d", port)
	envMap["ALLOCATE_HOST"] = hostIp
	envMap["BASE_MOUNT_PATH"] = models.Config.Plugin.BaseMountPath
	envMap["MONITOR_PORT"] = fmt.Sprintf("%d", port+10000)
	if mysqlInstance != nil {
		envMap["DB_SCHEMA"] = mysqlInstance.SchemaName
		envMap["DB_USER"] = mysqlInstance.Username
		if models.Config.Plugin.PasswordPubKeyContent != "" {
			if encryptDBPwd, enErr := cipher.EncryptRsa(mysqlInstance.Password, models.Config.Plugin.PasswordPubKeyContent); enErr != nil {
				log.Error(nil, log.LOGGER_APP, "Try to encrypt plugin password fail", zap.Error(enErr))
				envMap["DB_PWD"] = mysqlInstance.Password
			} else {
				envMap["DB_PWD"] = encryptDBPwd
			}
		} else {
			envMap["DB_PWD"] = mysqlInstance.Password
		}
		if mysqlServer != nil {
			envMap["DB_HOST"] = mysqlServer.Host
			envMap["DB_PORT"] = mysqlServer.Port
		}
	}
	// 向auth server注册插件并返回插件认证的code和pubKey,插件会拿着这两个东西去获取插件专属的token来访问platform
	subSystemCode, subSystemKey, subSystemPubKey, registerAuthErr := remote.RegisterSubSystem(&pluginPackageObj)
	if registerAuthErr != nil {
		err = registerAuthErr
		return
	}
	envMap["SUB_SYSTEM_CODE"] = subSystemCode
	envMap["SUB_SYSTEM_KEY"] = subSystemKey
	if models.Config.Auth.JwtSigningKey != "" {
		if models.Config.Plugin.PasswordPubKeyContent != "" {
			if encryptJwtKey, enErr := cipher.EncryptRsa(models.Config.Auth.JwtSigningKey, models.Config.Plugin.PasswordPubKeyContent); enErr != nil {
				envMap["JWT_SIGNING_KEY"] = models.Config.Auth.JwtSigningKey
			} else {
				envMap["JWT_SIGNING_KEY"] = encryptJwtKey
			}
		} else {
			envMap["JWT_SIGNING_KEY"] = models.Config.Auth.JwtSigningKey
		}
	}
	// 企业版的认证信息环境变量
	if pluginPackageObj.Edition == "enterprise" {
		licCode, licPk, licData, licSign, getLicenceErr := database.GeneratePluginEnv(subSystemPubKey, subSystemKey, pluginPackageObj.Name)
		if getLicenceErr != nil {
			err = getLicenceErr
			return
		}
		envBindList = append(envBindList, "LICENSE_CODE="+licCode)
		envBindList = append(envBindList, "LICENSE_PK="+licPk)
		envBindList = append(envBindList, "LICENSE_DATA="+licData)
		envBindList = append(envBindList, "LICENSE_SIGNATURE="+licSign)
	}
	// 替换容器参数差异化变量
	replaceMap, buildEnvErr := database.BuildDockerEnvMap(ctx, envMap, pluginPackageObj.Name, pluginPackageObj.Version)
	if buildEnvErr != nil {
		err = buildEnvErr
		return
	}
	portBindList = replaceEnvMap(portBindList, replaceMap)
	volumeBindList = replaceEnvMap(volumeBindList, replaceMap)
	envBindList = replaceEnvMap(envBindList, replaceMap)
	// 先检查目标机器上有没有相关版本容器镜像，如果有的话就跳过下面两个下载和传镜像的操作
	// 把s3上的image.tar下载来到本地？可否直接让目标机器下载image.tar
	tmpImageFile, downloadImageErr := bash.DownloadPackageFile(models.Config.S3.PluginPackageBucket, fmt.Sprintf("%s/%s/image.tar", pluginPackageObj.Name, pluginPackageObj.Version))
	if downloadImageErr != nil {
		err = downloadImageErr
		return
	}
	defer bash.RemoveTmpFile(tmpImageFile)
	// 把image.tar传到目标机器
	targetImagePath := fmt.Sprintf("%s/%s_%s_image.tar", models.Config.Plugin.DeployPath, pluginPackageObj.Name, pluginPackageObj.Version)
	if err = bash.RemoteSCP(dockerServer.Host, dockerServer.LoginUsername, dockerServer.LoginPassword, dockerServer.Port, tmpImageFile, targetImagePath); err != nil {
		return
	}
	log.Info(nil, log.LOGGER_APP, "scp plugin image file", zap.String("targetHost", dockerServer.Host), zap.String("tmpFile", tmpImageFile), zap.String("targetPath", targetImagePath))
	if err = bash.RemoteSSHCommand(dockerServer.Host, dockerServer.LoginUsername, dockerServer.LoginPassword, dockerServer.Port, fmt.Sprintf("docker load --input %s && rm -f %s", targetImagePath, targetImagePath)); err != nil {
		return
	}
	time.Sleep(1 * time.Second)
	// 去目标机器上docker run起来，或使用docker-compose
	dockerCmd := fmt.Sprintf("docker run -d --name %s --restart=always ", dockerResource.ContainerName)
	for _, v := range volumeBindList {
		dockerCmd += fmt.Sprintf("--volume %s ", v)
	}
	for _, v := range portBindList {
		if !strings.Contains(v, ":") {
			continue
		}
		dockerCmd += fmt.Sprintf("-p %s:%s ", dockerServer.Host, v)
	}
	for _, v := range envBindList {
		tmpV := v
		if eqIndex := strings.Index(v, "="); eqIndex > 0 {
			tmpV = v[:eqIndex+1] + "'" + v[eqIndex+1:] + "'"
		}
		dockerCmd += fmt.Sprintf("-e %s ", tmpV)
	}
	dockerCmd += dockerResource.ImageName
	log.Info(nil, log.LOGGER_APP, "docker run command", zap.String("cmd", dockerCmd))
	if err = bash.RemoteSSHCommand(dockerServer.Host, dockerServer.LoginUsername, dockerServer.LoginPassword, dockerServer.Port, dockerCmd); err != nil {
		// 清理启动失败的docker
		if rmDockerErr := bash.RemoteSSHCommand(dockerServer.Host, dockerServer.LoginUsername, dockerServer.LoginPassword, dockerServer.Port, fmt.Sprintf("docker rm -f %s", dockerResource.ContainerName)); rmDockerErr != nil {
			log.Error(nil, log.LOGGER_APP, "Try to remove failed docker container", zap.String("containerName", dockerResource.ContainerName), zap.Error(rmDockerErr))
		}
		return
	}
	// 更新插件注册的菜单状态和更新插件实例数据
	resourceItemProperties := models.ResourceItemProperties{
		ImageName:      dockerResource.ImageName,
		PortBindings:   strings.Join(portBindList, ","),
		VolumeBindings: strings.Join(volumeBindList, ","),
		EnvVariables:   strings.Join(envBindList, ","),
	}
	resourceItemPropertiesBytes, _ := json.Marshal(&resourceItemProperties)
	resourceItem := models.ResourceItem{
		Id:                   "rs_item_" + guid.CreateGuid(),
		ResourceServerId:     dockerServer.Id,
		AdditionalProperties: string(resourceItemPropertiesBytes),
		CreatedBy:            operator,
		CreatedDate:          time.Now(),
		Name:                 dockerResource.ContainerName,
	}
	pluginInstance.DockerInstanceResourceId = resourceItem.Id
	err = database.LaunchPlugin(ctx, &pluginInstance, &resourceItem, operator)
	if err != nil {
		return
	}
	// 向gateway注册插件路由
	err = remote.RegisterPluginRoute(pluginPackageObj.Name, hostIp, fmt.Sprintf("%d", port))
	if err != nil {
		return
	}
	return
}

// RemovePlugin 运行管理 - 插件实例销毁
func RemovePlugin(c *gin.Context) {
	pluginInstanceId := c.Param("pluginInstanceId")
	err := RemovePluginInstanceFunc(c, pluginInstanceId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnSuccess(c)
	}
}

func RemovePluginInstanceFunc(ctx context.Context, pluginInstanceId string) (err error) {
	pluginInstanceObj, getPluginErr := database.GetPluginInstance(pluginInstanceId, "", "", "", true)
	if getPluginErr != nil {
		err = getPluginErr
		return
	}
	pluginPackageObj := models.PluginPackages{Id: pluginInstanceObj.PackageId}
	if err = database.GetSimplePluginPackage(ctx, &pluginPackageObj, true); err != nil {
		return
	}
	// 查询容器资源信息
	resourceServer, getServerErr := database.GetPluginDockerRunningResource(pluginInstanceObj.DockerInstanceResourceId)
	if getServerErr != nil {
		err = getServerErr
		return
	}
	// 查询容器运行信息
	imageName, containerName, getErr := database.GetPluginDockerRuntimeMessage(pluginInstanceObj.PackageId)
	if getErr != nil {
		err = getErr
		return
	}
	// 销毁容器
	if strings.HasPrefix(resourceServer.LoginPassword, models.AESPrefix) {
		resourceServer.LoginPassword = encrypt.DecryptWithAesECB(resourceServer.LoginPassword[5:], models.Config.Plugin.ResourcePasswordSeed, resourceServer.Name)
	}
	removeCmd := fmt.Sprintf("docker rm -f %s && docker rmi %s", containerName, imageName)
	if err = bash.RemoteSSHCommand(resourceServer.Host, resourceServer.LoginUsername, resourceServer.LoginPassword, resourceServer.Port, removeCmd); err != nil {
		return
	}
	// 更新插件注册的菜单状态和更新插件实例数据
	err = database.RemovePlugin(ctx, pluginPackageObj.Id, pluginInstanceId, pluginInstanceObj.DockerInstanceResourceId)
	return
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

// TODO
// func buildPluginProCertification(envMap map[string]string, pluginPackageObj *models.PluginPackages, subSystemKey string) (err error) {

// 	return
// }

func GetPackageNames(c *gin.Context) {
	result, err := database.GetPackageNames(c)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func GetPluginResourceFiles(c *gin.Context) {
	result, err := database.GetPluginResourceFiles(c)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func GetPluginS3Files(c *gin.Context) {
	pluginPackageId := c.Param("pluginPackageId")
	resource, err := database.GetPluginRuntimeResources(c, pluginPackageId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	} else {
		var result interface{}
		result = make([]string, 0)
		if len(resource.S3) > 0 {
			result, err = bash.ListBucketFiles(resource.S3[0].BucketName)
			if err != nil {
				middleware.ReturnError(c, err)
				return
			}
		}
		middleware.ReturnData(c, result)
	}
}

// UIRegisterPackage 插件配置 - 注册插件包UI资源
func UIRegisterPackage(c *gin.Context) {
	var param models.PluginPackages
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	pluginPackageId := param.Id
	pluginPackageObj := models.PluginPackages{Id: pluginPackageId}
	if err := database.GetSimplePluginPackage(c, &pluginPackageObj, true); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	var err error
	if !pluginPackageObj.UiPackageIncluded {
		middleware.ReturnSuccess(c)
		return
	}
	if len(models.Config.StaticResources) == 0 {
		middleware.ReturnError(c, fmt.Errorf("static resource config empty"))
		return
	}
	// 把s3上的ui.zip下下来放到本地
	log.Debug(nil, log.LOGGER_APP, "register plugin,start download ui.zip")
	var uiFileLocalPath, uiDir string
	if uiFileLocalPath, err = bash.DownloadPackageFile(models.Config.S3.PluginPackageBucket, fmt.Sprintf("%s/%s/ui.zip", pluginPackageObj.Name, pluginPackageObj.Version)); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	defer bash.RemoveTmpFile(uiFileLocalPath)
	log.Debug(nil, log.LOGGER_APP, "register plugin,start decompress ui.zip", zap.String("uiFileLocalPath", uiFileLocalPath))
	// 本地解压ui.zip
	if uiDir, err = bash.DecompressFile(uiFileLocalPath, ""); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	defer bash.RemoveTmpFile(uiDir)
	// 把ui.zip用ssh传到静态资源服务器上并解压，如果有两台服务器，则每台都要上传与解压
	for _, staticResourceObj := range models.Config.StaticResources {
		targetPath := fmt.Sprintf("%s/%s/%s/ui.zip", staticResourceObj.Path, pluginPackageObj.Name, pluginPackageObj.Version)
		unzipCmd := fmt.Sprintf("cd %s/%s/%s && unzip -o ui.zip", staticResourceObj.Path, pluginPackageObj.Name, pluginPackageObj.Version)
		log.Debug(nil, log.LOGGER_APP, "register plugin,start scp ui.zip to remote host", zap.String("server", staticResourceObj.Server), zap.String("targetPath", targetPath))
		if err = bash.RemoteSCP(staticResourceObj.Server, staticResourceObj.User, staticResourceObj.Password, staticResourceObj.Port, uiFileLocalPath, targetPath); err != nil {
			break
		}
		log.Debug(nil, log.LOGGER_APP, "register plugin,start unzip ui.zip in remote host", zap.String("server", staticResourceObj.Server), zap.String("unzipCmd", unzipCmd))
		if err = bash.RemoteSSHCommand(staticResourceObj.Server, staticResourceObj.User, staticResourceObj.Password, staticResourceObj.Port, unzipCmd); err != nil {
			break
		}
	}
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 把ui.zip里的静态文件读出来
	var fileNameList []string
	indexPath, matchIndexFlag, findErr := bash.GetDirIndexPath(uiDir)
	if findErr != nil {
		middleware.ReturnError(c, findErr)
		return
	}
	if !matchIndexFlag {
		middleware.ReturnError(c, fmt.Errorf("can not find index.html in ui package"))
		return
	}
	log.Debug(nil, log.LOGGER_APP, "match index path", zap.String("indexPath", indexPath))
	indexPath = strings.TrimSuffix(indexPath, "/")
	dirPrefix := uiDir
	if indexPath != "" {
		dirPrefix = uiDir + "/" + indexPath
	}
	fileNameList, err = bash.ListDirAllFiles(dirPrefix)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	uiStaticPath := models.Config.StaticResources[0].Path
	if pathIndex := strings.LastIndex(uiStaticPath, "/"); pathIndex >= 0 {
		uiStaticPath = uiStaticPath[pathIndex:]
	}
	uiStaticPath = fmt.Sprintf("%s/%s/%s", uiStaticPath, pluginPackageObj.Name, pluginPackageObj.Version)
	if indexPath != "" {
		uiStaticPath = uiStaticPath + "/" + indexPath
	}
	resourceFileList := []*models.PluginPackageResourceFiles{}
	for _, v := range fileNameList {
		tmpResourceObj := models.PluginPackageResourceFiles{PluginPackageId: pluginPackageId, PackageName: pluginPackageObj.Name, PackageVersion: pluginPackageObj.Version, Source: "ui.zip", RelatedPath: strings.ReplaceAll(v, dirPrefix, uiStaticPath)}
		resourceFileList = append(resourceFileList, &tmpResourceObj)
	}
	if len(resourceFileList) > 0 {
		log.Debug(nil, log.LOGGER_APP, "register plugin,start update plugin static resource file data", log.JsonObj("resourceFileList", resourceFileList))
		if err = database.UpdatePluginStaticResourceFiles(c, pluginPackageId, pluginPackageObj.Name, resourceFileList, middleware.GetRequestUser(c)); err != nil {
			middleware.ReturnError(c, err)
			return
		}
	}
	middleware.ReturnSuccess(c)
}

// RegisterPackageDone 插件配置 - 完成注册插件包
func RegisterPackageDone(c *gin.Context) {
	var param models.PluginPackages
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	pluginPackageObj := models.PluginPackages{Id: param.Id}
	if err := database.GetSimplePluginPackage(c, &pluginPackageObj, true); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if pluginPackageObj.Status != "REGISTERED" {
		middleware.ReturnError(c, fmt.Errorf("pluginPackage status:%s illegal", pluginPackageObj.Status))
		return
	}
	dynamicModel, err := database.SetPluginPackageRegisterDone(c, param.Id, middleware.GetRequestUser(c))
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		if dynamicModel {
			pluginModels, syncDynamicErr := remote.GetPluginDataModels(c, pluginPackageObj.Name, c.GetHeader(models.AuthorizationHeader))
			if syncDynamicErr != nil {
				log.Error(nil, log.LOGGER_APP, "syncDynamic fail with get plugin data model", zap.String("package", pluginPackageObj.Name), zap.Error(syncDynamicErr))
			} else {
				if syncDynamicErr = database.SyncPluginDataModels(c, pluginPackageObj.Name, pluginModels); syncDynamicErr != nil {
					log.Error(nil, log.LOGGER_APP, "syncDynamic fail with update plugin data model", zap.String("package", pluginPackageObj.Name), zap.Error(syncDynamicErr))
				}
			}
		}
		middleware.ReturnSuccess(c)
	}
}

func GetPluginConfigVersionList(c *gin.Context) {
	pluginPackageId := c.Query("id")
	pluginPackageObj := models.PluginPackages{Id: pluginPackageId}
	if err := database.GetSimplePluginPackage(c, &pluginPackageObj, true); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	result, err := database.GetPluginConfigVersionList(c, pluginPackageId, pluginPackageObj.Name)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func InheritPluginConfig(c *gin.Context) {
	var param models.InheritPluginConfigParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	err := database.InheritPluginConfig(c, &param, middleware.GetRequestUser(c))
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnSuccess(c)
	}
}
