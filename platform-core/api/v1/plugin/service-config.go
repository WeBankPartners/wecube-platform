package plugin

import (
	"encoding/xml"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"go.uber.org/zap"
	"net/http"
	"sort"
	"strings"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/try"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/bash"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/gin-gonic/gin"
)

// GetPluginConfigs 服务注册 - 当前插件服务配置查询
func GetPluginConfigs(c *gin.Context) {
	pluginPackageId := c.Param("pluginPackageId")
	result, err := database.GetPluginConfigs(c, pluginPackageId, middleware.GetRequestRoles(c), "")
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

// GetPluginConfigsWithInterfaces 服务注册 - 当前插件服务配置查询带interfaces
func GetPluginConfigsWithInterfaces(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Error(nil, log.LOGGER_APP, e.(string))
	})

	pluginPackageId := c.Param("pluginPackageId")
	result, err := database.GetPluginConfigsWithInterfaces(c, pluginPackageId, middleware.GetRequestRoles(c), "")
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

// GetConfigInterfaces 服务注册 - 查询指定服务的接口详情
func GetConfigInterfaces(c *gin.Context) {
	pluginConfigId := c.Param("pluginConfigId")
	result, err := database.GetConfigInterfaces(c, pluginConfigId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

// UpdatePluginConfigRoles 服务注册 - 配置服务管理使用权限
func UpdatePluginConfigRoles(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Error(nil, log.LOGGER_APP, e.(string))
	})

	pluginConfigId := c.Param("pluginConfigId")
	var err error
	if pluginConfigId == "" {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err, pluginConfigId can not be empty"))
		middleware.ReturnError(c, err)
		return
	}

	reqParam := models.UpdatePluginCfgRolesReqParam{}
	if err = c.ShouldBindJSON(&reqParam); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if len(reqParam.PermissionToRole.MGMT) == 0 {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err, MGMT permission role can not be empty"))
		middleware.ReturnError(c, err)
		return
	}

	err = database.UpdatePluginConfigRoles(c, pluginConfigId, &reqParam)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnSuccess(c)
	}
}

// DisablePluginConfig 服务注册 - 服务注销
func DisablePluginConfig(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Error(nil, log.LOGGER_APP, e.(string))
	})

	pluginConfigId := c.Param("pluginConfigId")
	var err error
	if pluginConfigId == "" {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err, pluginConfigId can not be empty"))
		middleware.ReturnError(c, err)
		return
	}

	retData, err := database.UpdatePluginConfigStatus(c, pluginConfigId, models.PluginStatusDisabled)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, retData)
	}
}

// EnablePluginConfig 服务注册 - 服务注册
func EnablePluginConfig(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Error(nil, log.LOGGER_APP, e.(string))
	})

	pluginConfigId := c.Param("pluginConfigId")
	var err error
	if pluginConfigId == "" {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err, pluginConfigId can not be empty"))
		middleware.ReturnError(c, err)
		return
	}

	retData, err := database.UpdatePluginConfigStatus(c, pluginConfigId, models.PluginStatusEnabled)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, retData)
	}
}

// SavePluginConfig 服务注册 - 服务配置保存
func SavePluginConfig(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Error(nil, log.LOGGER_APP, e.(string))
	})

	var err error
	reqParam := models.PluginConfigDto{}
	if err = c.ShouldBindJSON(&reqParam); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if len(reqParam.PermissionToRole.MGMT) == 0 {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err, MGMT permission role can not be empty"))
		middleware.ReturnError(c, err)
		return
	}
	if len(reqParam.PluginPackageId) == 0 {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err, pluginPackageId can not be empty"))
		middleware.ReturnError(c, err)
		return
	}
	retData, err := database.SavePluginConfig(c, &reqParam)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, retData)
	}
}

// DeletePluginConfig 服务注册 - 服务配置删除
func DeletePluginConfig(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Error(nil, log.LOGGER_APP, e.(string))
	})

	pluginConfigId := c.Param("pluginConfigId")
	var err error
	if pluginConfigId == "" {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err, pluginConfigId can not be empty"))
		middleware.ReturnError(c, err)
		return
	}

	err = database.DeletePluginConfig(c, pluginConfigId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnSuccess(c)
	}
}

// GetBatchPluginConfigs 服务注册 - 批量注册查询
func GetBatchPluginConfigs(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Error(nil, log.LOGGER_APP, e.(string))
	})

	pluginPackageId := c.Param("pluginPackageId")
	var err error
	if pluginPackageId == "" {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err, pluginPackageId can not be empty"))
		middleware.ReturnError(c, err)
		return
	}

	retData, err := database.GetBatchPluginConfigs(c, pluginPackageId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, retData)
	}
}

// BatchEnablePluginConfig 服务注册 - 批量注册
func BatchEnablePluginConfig(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Error(nil, log.LOGGER_APP, e.(string))
	})

	pluginPackageId := c.Param("pluginPackageId")
	var err error
	if pluginPackageId == "" {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err, pluginPackageId can not be empty"))
		middleware.ReturnError(c, err)
		return
	}

	var reqParam []*models.PluginConfigsBatchEnable
	if err = c.ShouldBindJSON(&reqParam); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	err = database.BatchEnablePluginConfig(c, reqParam, pluginPackageId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnSuccess(c)
	}
}

// ExportPluginConfigs 插件配置导出
func ExportPluginConfigs(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Error(nil, log.LOGGER_APP, e.(string))
	})

	pluginPackageId := c.Param("pluginPackageId")
	var err error
	if pluginPackageId == "" {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err, pluginPackageId can not be empty"))
		middleware.ReturnError(c, err)
		return
	}

	var reqParam []*models.PluginConfigsBatchEnable
	if c.Request.Method == http.MethodPost {
		if err = c.ShouldBindJSON(&reqParam); err != nil {
			middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
			return
		}
	}

	retData, err := database.ExportPluginConfigs(c, pluginPackageId, reqParam, middleware.GetRequestRoles(c))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	} else {
		// middleware.ReturnXMLData(c, retData)
		fileName := fmt.Sprintf("%s-%s-%s.xml", retData.Name, retData.Version, time.Now().Format("20060102150405"))
		// retDataBytes, tmpErr := json.Marshal(retData)
		retDataBytes, tmpErr := xml.MarshalIndent(retData, "", "    ")
		if tmpErr != nil {
			err = fmt.Errorf("marshal exportPluginConfigs failed: %s", tmpErr.Error())
			middleware.ReturnError(c, err)
			return
		}
		c.Header("Content-Disposition", fmt.Sprintf("attachment;filename=%s", fileName))
		c.Data(http.StatusOK, "application/octet-stream", retDataBytes)
	}
}

// ImportPluginConfigs 插件配置导入
func ImportPluginConfigs(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Error(nil, log.LOGGER_APP, e.(string))
	})

	pluginPackageId := c.Param("pluginPackageId")
	var err error
	if pluginPackageId == "" {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err, pluginPackageId can not be empty"))
		middleware.ReturnError(c, err)
		return
	}

	_, xmlFileBytes, err := middleware.ReadFormFile(c, "xml-file")
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}

	var packagePluginsData models.PackagePluginsXML
	if err = xml.Unmarshal(xmlFileBytes, &packagePluginsData); err != nil {
		middleware.ReturnError(c, fmt.Errorf("xml unmarshal failed: %s", err.Error()))
		return
	}

	retData, err := database.ImportPluginConfigs(c, pluginPackageId, &packagePluginsData)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, retData)
	}
}

// DeletePlugin 插件删除
func DeletePlugin(c *gin.Context) {
	pluginPackageId := c.Param("pluginPackageId")
	pluginPackage, err := database.GetPluginPackageById(c, pluginPackageId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if pluginPackage == nil {
		middleware.ReturnError(c, fmt.Errorf("plugin package %s not found", pluginPackageId))
		return
	}
	if pluginPackage.Status == models.PluginStatusDecommissioned {
		middleware.ReturnSuccess(c)
		return
	}
	running, err := database.IsPluginInstanceRunning(c, pluginPackageId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if running {
		middleware.ReturnError(c, fmt.Errorf("plugin package %s instance is running", pluginPackageId))
		return
	}
	err = database.DisableAllPluginConfigsByPackageId(c, pluginPackageId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	err = database.DeactivateSystemVariablesByPackage(c, pluginPackage.Name, pluginPackage.Version)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if pluginPackage.UiPackageIncluded {
		for _, staticResourceObj := range models.Config.StaticResources {
			targetCmd := fmt.Sprintf("rm -rf %s/%s/%s/", staticResourceObj.Path, pluginPackage.Name, pluginPackage.Version)
			log.Debug(nil, log.LOGGER_APP, "unregister plugin,remove ui in remote host", zap.String("server", staticResourceObj.Server), zap.String("cmd", targetCmd))
			if err = bash.RemoteSSHCommand(staticResourceObj.Server, staticResourceObj.User, staticResourceObj.Password, staticResourceObj.Port, targetCmd); err != nil {
				middleware.ReturnError(c, err)
				return
			}
		}
	}
	err = database.DecommissionPluginPackage(c, pluginPackageId, middleware.GetRequestUser(c))
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnSuccess(c)
}

// QueryPluginByTargetEntity 根据目标对象过滤插件
func QueryPluginByTargetEntity(c *gin.Context) {
	var param models.TargetEntityFilterRuleDto
	var resultPluginConfigInterfaceDtoList = make([]*models.PluginConfigInterfaceDto, 0)
	var finalResultPluginConfigInterfaceDtoList = make([]*models.PluginConfigInterfaceDto, 0)
	var err error
	var dataModelEntity *models.PluginPackageDataModel
	var roles = middleware.GetRequestRoles(c)
	var procDef *models.ProcDef
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	// 不为空,表示查询 插件参数,可能没权限,此处使用编排设计创建人的角色
	if strings.TrimSpace(param.ProcDefId) != "" {
		roles = []string{}
		var response models.QueryRolesResponse
		if procDef, err = database.GetProcessDefinition(c, param.ProcDefId); err != nil {
			middleware.ReturnError(c, err)
			return
		}
		if procDef == nil {
			middleware.ReturnError(c, fmt.Errorf("procDef %s not found", param.ProcDefId))
			return
		}
		response, err = remote.GetRolesByUsername(procDef.CreatedBy, c.GetHeader("Authorization"), c.GetHeader(middleware.AcceptLanguageHeader))
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
		for _, roleItem := range response.Data {
			if roleItem.Status == "Deleted" {
				continue
			}
			roles = append(roles, roleItem.Name)
		}
	}
	dataModelEntity, err = database.TryFetchLatestAvailableDataModelEntity(c, param.PkgName)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if dataModelEntity == nil {
		log.Info(nil, log.LOGGER_APP, "No data model found for package", zap.String("package", param.PkgName))
		middleware.ReturnData(c, resultPluginConfigInterfaceDtoList)
		return
	}
	plugConfigInterfaceDtoList, err := database.QueryAllEnablePluginConfigInterfaceByCondition(c, param, roles)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if len(plugConfigInterfaceDtoList) > 0 {
		resultPluginConfigInterfaceDtoList = append(resultPluginConfigInterfaceDtoList, plugConfigInterfaceDtoList...)
	}
	if strings.TrimSpace(param.NodeType) != "" {
		if param.NodeType == string(models.ProcDefNodeTypeHuman) {
			for _, interfaceDto := range resultPluginConfigInterfaceDtoList {
				if strings.EqualFold(interfaceDto.Type, "APPROVAL") || strings.EqualFold(interfaceDto.Type, "DYNAMICFORM") {
					finalResultPluginConfigInterfaceDtoList = append(finalResultPluginConfigInterfaceDtoList, interfaceDto)
				}
			}
		} else if param.NodeType == string(models.ProcDefNodeTypeAutomatic) {
			for _, interfaceDto := range resultPluginConfigInterfaceDtoList {
				finalResultPluginConfigInterfaceDtoList = append(finalResultPluginConfigInterfaceDtoList, interfaceDto)
			}
		} else {
			finalResultPluginConfigInterfaceDtoList = append(finalResultPluginConfigInterfaceDtoList, resultPluginConfigInterfaceDtoList...)
		}
	} else {
		finalResultPluginConfigInterfaceDtoList = append(finalResultPluginConfigInterfaceDtoList, resultPluginConfigInterfaceDtoList...)
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
				if strings.EqualFold(paramDto.MappingType, "constant") && strings.TrimSpace(paramDto.MappingValue) != "" {
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

func QueryPluginInterfaceParam(c *gin.Context) {
	var param models.PluginInterfaceParamQueryParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	result, err := database.QueryPluginInterfaceParam(c, &param)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

// GetObjectMetas 服务注册 - 查询 object 类型的 interface parameters
func GetObjectMetas(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Error(nil, log.LOGGER_APP, e.(string))
	})

	objectMetaId := c.Param("objectMetaId")
	result, err := database.GetObjectMetas(c, objectMetaId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

// UpdateObjectMetas 服务注册 - 配置 object 类型的 interface parameters
func UpdateObjectMetas(c *gin.Context) {
	defer try.ExceptionStack(func(e interface{}, err interface{}) {
		retErr := fmt.Errorf("%v", err)
		middleware.ReturnError(c, exterror.Catch(exterror.New().ServerHandleError, retErr))
		log.Error(nil, log.LOGGER_APP, e.(string))
	})

	pluginConfigId := c.Param("pluginConfigId")
	var err error
	if pluginConfigId == "" {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err, pluginConfigId can not be empty"))
		middleware.ReturnError(c, err)
		return
	}

	objectMetaId := c.Param("objectMetaId")
	if objectMetaId == "" {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err, objectMetaId can not be empty"))
		middleware.ReturnError(c, err)
		return
	}

	reqParam := models.CoreObjectMeta{}
	if err = c.ShouldBindJSON(&reqParam); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if reqParam.Id != objectMetaId {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param.Id should be equal to objectMetaId: %s", objectMetaId))
		middleware.ReturnError(c, err)
		return
	}
	if reqParam.ConfigId != pluginConfigId {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param.ConfigId should be equal to pluginConfigId: %s", pluginConfigId))
		middleware.ReturnError(c, err)
		return
	}

	err = database.UpdateObjectMetas(c, &reqParam)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnSuccess(c)
	}
}
