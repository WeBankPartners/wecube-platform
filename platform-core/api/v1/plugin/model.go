package plugin

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"github.com/gin-gonic/gin"
	"strings"
)

// GetPluginModels 插件配置 - 数据模型
func GetPluginModels(c *gin.Context) {
	packageName := c.Param("pluginPackageId")
	if packageName == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("packageName can not empty")))
		return
	}
	withAttrString := strings.ToLower(c.Query("withAttr"))
	withAttr := true
	if withAttrString == "n" || withAttrString == "no" || withAttrString == "false" {
		withAttr = false
	}
	result, err := database.GetDataModels(c, packageName, withAttr)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

// GetAllModels 服务注册 - 所有插件模型查询
func GetAllModels(c *gin.Context) {
	withAttrString := strings.ToLower(c.Query("withAttr"))
	withAttr := true
	if withAttrString == "n" || withAttrString == "no" || withAttrString == "false" {
		withAttr = false
	}
	result, err := database.GetDataModels(c, "", withAttr)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

// GetEntityModel 服务注册 - entity模型属性查询
func GetEntityModel(c *gin.Context) {
	packageName := c.Param("packageName")
	entityName := c.Param("entity")
	if packageName == "" || entityName == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("packageName or eneity can not empty")))
		return
	}
	result, err := database.GetEntityModel(c, packageName, entityName)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

// SyncDynamicModels 插件配置 - 数据模型同步
func SyncDynamicModels(c *gin.Context) {
	packageName := c.Param("packageName")
	if packageName == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("packageName can not empty")))
		return
	}
	pluginModels, err := remote.GetPluginDataModels(packageName, c.GetHeader(models.AuthorizationHeader))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if err = database.SyncPluginDataModels(c, packageName, pluginModels); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	result, queryErr := database.GetDataModels(c, packageName, true)
	if queryErr != nil {
		middleware.ReturnError(c, queryErr)
	} else {
		middleware.ReturnData(c, result)
	}
}
