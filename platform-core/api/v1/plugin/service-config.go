package plugin

import (
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/gin-gonic/gin"
	"sort"
	"strings"
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

// QueryPluginByTargetEntity 根据目标对象过滤插件
func QueryPluginByTargetEntity(c *gin.Context) {
	var param models.TargetEntityFilterRuleDto
	var resultPluginConfigInterfaceDtoList = make([]*models.PluginConfigInterfaceDto, 0)
	var finalResultPluginConfigInterfaceDtoList = make([]*models.PluginConfigInterfaceDto, 0)
	var err error
	var dataModelEntity *models.PluginPackageDataModel
	var roles = middleware.GetRequestRoles(c)
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	dataModelEntity, err = database.TryFetchLatestAvailableDataModelEntity(c, param.PkgName)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if dataModelEntity == nil {
		log.Logger.Info("No data model found for package", log.String("package", param.PkgName))
		middleware.ReturnData(c, resultPluginConfigInterfaceDtoList)
		return
	}
	if isEmpty(param) {
		plugConfigInterfaceDtoList, err := database.QueryAllEnablePluginConfigInterfaceByCondition(c, param, roles)
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
		if len(plugConfigInterfaceDtoList) > 0 {
			resultPluginConfigInterfaceDtoList = append(resultPluginConfigInterfaceDtoList, plugConfigInterfaceDtoList...)
		}
		if strings.TrimSpace(param.TaskCategory) != "" {
			if param.TaskCategory == "SUTN" {
				for _, interfaceDto := range resultPluginConfigInterfaceDtoList {
					if strings.EqualFold(interfaceDto.Type, "APPROVAL") || strings.EqualFold(interfaceDto.Type, "DYNAMICFORM") {
						finalResultPluginConfigInterfaceDtoList = append(finalResultPluginConfigInterfaceDtoList, interfaceDto)
					}
				}
			} else if param.TaskCategory == "SSTN" {
				for _, interfaceDto := range resultPluginConfigInterfaceDtoList {
					if strings.EqualFold(interfaceDto.Type, "EXECUTION") {
						finalResultPluginConfigInterfaceDtoList = append(finalResultPluginConfigInterfaceDtoList, interfaceDto)
					}
				}
			} else {
				finalResultPluginConfigInterfaceDtoList = append(finalResultPluginConfigInterfaceDtoList, resultPluginConfigInterfaceDtoList...)
			}
		} else {
			finalResultPluginConfigInterfaceDtoList = append(finalResultPluginConfigInterfaceDtoList, resultPluginConfigInterfaceDtoList...)
		}
	}
	// 排序
	sort.Sort(models.PluginConfigInterfaceDtoSort(finalResultPluginConfigInterfaceDtoList))
	tryCalculateConfigurableInputParameters(finalResultPluginConfigInterfaceDtoList)
	middleware.ReturnData(c, finalResultPluginConfigInterfaceDtoList)
}

func tryCalculateConfigurableInputParameters(list []*models.PluginConfigInterfaceDto) {
	for _, dto := range list {
		inputParameters := dto.InputParameters
		if len(inputParameters) == 0 {
			return
		}
		for _, paramDto := range inputParameters {
			if strings.EqualFold(paramDto.MappingType, "context") || strings.EqualFold(paramDto.MappingType, "constant") {
				if strings.EqualFold(paramDto.MappingType, "constant") && strings.TrimSpace(paramDto.MappingValue) == "" {
					continue
				}
				configParamDto := &models.PluginConfigInterfaceParameterDto{
					Id:                        paramDto.Id,
					PluginConfigInterfaceId:   paramDto.PluginConfigInterfaceId,
					Type:                      paramDto.Type,
					Name:                      paramDto.Name,
					DataType:                  paramDto.DataType,
					MappingType:               paramDto.MappingType,
					MappingEntityExpression:   paramDto.MappingEntityExpression,
					MappingSystemVariableName: paramDto.MappingSystemVariableName,
					Required:                  paramDto.Required,
					SensitiveData:             paramDto.SensitiveData,
					Description:               paramDto.Description,
					MappingValue:              paramDto.MappingValue,
					Multiple:                  paramDto.Multiple,
				}
				dto.AddConfigurableInputParameters(configParamDto)
			} else {
				refObjectMeta := paramDto.RefObjectMeta
				if refObjectMeta != nil && len(refObjectMeta.PropertyMetas) > 0 {
					objectMetaConfigParamDtoList := tryCalculateConfigurableParameters(refObjectMeta)
					if len(objectMetaConfigParamDtoList) > 0 {
						for _, p := range objectMetaConfigParamDtoList {
							dto.AddConfigurableInputParameters(p)
						}
					}
				}
			}
		}
	}
}

func tryCalculateConfigurableParameters(refObjectMeta *models.CoreObjectMetaDto) []*models.PluginConfigInterfaceParameterDto {
	var objectConfigParamDtoList = make([]*models.PluginConfigInterfaceParameterDto, 0)
	for _, propMetaDto := range refObjectMeta.PropertyMetas {
		if strings.EqualFold(propMetaDto.MappingType, "context") || strings.EqualFold(propMetaDto.MappingType, "constant") {
			if strings.EqualFold(propMetaDto.MappingType, "constant") && strings.TrimSpace(propMetaDto.MappingEntityExpression) == "" {
				continue
			}
			propMetaParamDto := &models.PluginConfigInterfaceParameterDto{
				Id:                      propMetaDto.Id,
				PluginConfigInterfaceId: propMetaDto.DataType,
				Type:                    "INPUT",
				Name:                    propMetaDto.Name,
				DataType:                propMetaDto.DataType,
				MappingType:             propMetaDto.MappingType,
				MappingEntityExpression: propMetaDto.MappingEntityExpression,
				Required:                "Y",
				SensitiveData:           propMetaDto.SensitiveData,
				MappingValue:            propMetaDto.MappingEntityExpression,
				Multiple:                propMetaDto.Multiple,
			}
			objectConfigParamDtoList = append(objectConfigParamDtoList, propMetaParamDto)
		} else {
			if propMetaDto.RefObjectMeta != nil {
				pluginConfigInterfaceParameterDtoList := tryCalculateConfigurableParameters(propMetaDto.RefObjectMeta)
				if len(pluginConfigInterfaceParameterDtoList) > 0 {
					objectConfigParamDtoList = append(objectConfigParamDtoList, pluginConfigInterfaceParameterDtoList...)
				}
			}
		}
	}
	return objectConfigParamDtoList
}

func isEmpty(param models.TargetEntityFilterRuleDto) bool {
	if param.PkgName == "" && param.EntityName == "" && param.TargetEntityFilterRule == "" || param.TaskCategory == "" {
		return true
	}
	return false
}
