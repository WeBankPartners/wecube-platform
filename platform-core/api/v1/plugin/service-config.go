package plugin

import (
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/gin-gonic/gin"
)

// GetPluginConfigs 服务注册 - 当前插件服务配置查询
func GetPluginConfigs(c *gin.Context) {
	pluginPackageId := c.Param("pluginPackageId")
	result, err := database.GetPluginConfigs(c, pluginPackageId, middleware.GetRequestRoles(c))
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

// GetConfigInterfaces 服务注册 - 查询指定服务的接口详情
func GetConfigInterfaces(c *gin.Context) {
	pluginPackageId := c.Param("pluginPackageId")
	result, err := database.GetConfigInterfaces(c, pluginPackageId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

// UpdatePluginConfigRoles 服务注册 - 配置服务管理使用权限
func UpdatePluginConfigRoles(c *gin.Context) {

}

// DisablePluginConfig 服务注册 - 服务注销
func DisablePluginConfig(c *gin.Context) {

}

// EnablePluginConfig 服务注册 - 服务注册
func EnablePluginConfig(c *gin.Context) {

}

// SavePluginConfig 服务注册 - 服务配置保存
func SavePluginConfig(c *gin.Context) {

}

// DeletePluginConfig 服务注册 - 服务配置删除
func DeletePluginConfig(c *gin.Context) {

}

// GetBatchPluginConfigs 服务注册 - 批量注册查询
func GetBatchPluginConfigs(c *gin.Context) {

}

// BatchEnablePluginConfig 服务注册 - 批量注册
func BatchEnablePluginConfig(c *gin.Context) {

}

// ExportPluginConfigs 插件配置导出
func ExportPluginConfigs(c *gin.Context) {

}

// ImportPluginConfigs 插件配置导入
func ImportPluginConfigs(c *gin.Context) {

}

// DeletePlugin 插件删除
func DeletePlugin(c *gin.Context) {

}
