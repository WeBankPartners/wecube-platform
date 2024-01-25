package database

import (
	"context"
	"strings"

	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

// TryFetchLatestAvailableDataModelEntity 尝试获取最新版本包插件
func TryFetchLatestAvailableDataModelEntity(ctx context.Context, packageName string) (latestDataModelEntity *models.PluginPackageDataModel, err error) {
	var dataModelEntities []*models.PluginPackageDataModel
	var pluginPackageEntities []*models.PluginPackageEntities
	var targetDataModel *models.PluginPackageDataModel
	latestDataModelEntity, err = getLatestAvailableDataModelEntity(ctx, packageName)
	if err != nil {
		return
	}
	if latestDataModelEntity == nil {
		return
	}
	if latestDataModelEntity.IsDynamic {
		dataModelEntities, err = getDataModelsByPackageName(ctx, latestDataModelEntity.PackageName)
		if err != nil {
			return
		}
		for _, dataModel := range dataModelEntities {
			pluginPackageEntities, err = getAllByDataModel(ctx, dataModel.Id)
			if len(pluginPackageEntities) > 0 {
				targetDataModel = dataModel
				break
			}
		}
	}
	if targetDataModel != nil {
		latestDataModelEntity = targetDataModel
	}
	return
}

func QueryAllEnablePluginConfigInterfaceByCondition(ctx context.Context, param models.TargetEntityFilterRuleDto, roles []string) (plugConfigInterfaceDtoList []*models.PluginConfigInterfaceDto, err error) {
	var authEnableInterfaceEntities []*models.AuthLatestEnabledInterfaces
	var allAuthEnableInterfaceEntities []*models.AuthLatestEnabledInterfaces
	var filteredAuthEnabledInterfaceEntities = make([]*models.AuthLatestEnabledInterfaces, 0)
	plugConfigInterfaceDtoList = make([]*models.PluginConfigInterfaceDto, 0)
	// 根据模板条件查询
	authEnableInterfaceEntities, err = getAllAuthEnableInterfacesByCondition(ctx, models.AuthEnableInterfacesQueryDto{TargetPackage: param.PkgName, TargetEntity: param.EntityName,
		PluginConfigStatus: "ENABLED", PermissionType: "USE", RoleNames: roles, PluginPackageStatuses: []string{"REGISTERED", "RUNNING", "STOPPED"}, TargetEntityFilterRule: param.TargetEntityFilterRule})
	if err != nil {
		return
	}
	// 查询所有
	allAuthEnableInterfaceEntities, err = getAllAuthEnabledInterfacesByNullTargetInfo(ctx, "ENABLED", "USE", roles, []string{"REGISTERED", "RUNNING", "STOPPED"})
	if len(allAuthEnableInterfaceEntities) > 0 {
		authEnableInterfaceEntities = append(authEnableInterfaceEntities, allAuthEnableInterfaceEntities...)
	}
	filteredAuthEnabledInterfaceEntities = filterLatestPluginConfigInterfaces(authEnableInterfaceEntities)
	if len(filteredAuthEnabledInterfaceEntities) > 0 {
		for _, authInterfaceEntity := range filteredAuthEnabledInterfaceEntities {
			configInterface := convertToPluginConfigInterfaces(ctx, authInterfaceEntity)
			plugConfigInterfaceDtoList = append(plugConfigInterfaceDtoList, buildPluginConfigInterfaceDto(configInterface))
		}
	}
	return
}

func GetAllByServiceNameAndConfigStatus(ctx context.Context, serviceName, status string) (list []*models.RichPluginConfigInterfaces, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("SELECT t1.id, t1.plugin_config_id,t1.action, t1.service_name,t1.service_display_name,t1.path,"+
		"t1.http_method,t1.is_async_processing,t1.type,t1.filter_rule,t2.id AS plugin_config_id,t2.status AS plugin_config_status,"+
		"t3.id AS plugin_package_id,t3.status AS plugin_package_status,t3.version AS plugin_package_version FROM plugin_config_interfaces t1,"+
		"plugin_configs t2,plugin_packages t3 WHERE t1.plugin_config_id = t2.id AND t2.plugin_package_id = t3.id AND t2.status =? AND "+
		"t1.service_name =?", status, serviceName).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func filterLatestPluginConfigInterfaces(authEnableInterfaceEntities []*models.AuthLatestEnabledInterfaces) []*models.AuthLatestEnabledInterfaces {
	var filteredAuthEnabledInterfaceEntities = make([]*models.AuthLatestEnabledInterfaces, 0)
	var serviceNamedPluginConfigInterfacesMap = make(map[string]*models.AuthLatestEnabledInterfaces)
	if len(authEnableInterfaceEntities) > 0 {
		for _, entity := range authEnableInterfaceEntities {
			serviceName := generateServiceName(entity)
			if value, ok := serviceNamedPluginConfigInterfacesMap[serviceName]; ok {
				if entity.UploadTimestamp.After(value.UploadTimestamp) {
					log.Logger.Info("pluginA is later than pluginB", log.String("pluginA", entity.Id), log.String("pluginB", value.Id))
					serviceNamedPluginConfigInterfacesMap[serviceName] = entity
				}
			} else {
				serviceNamedPluginConfigInterfacesMap[serviceName] = entity
			}
		}
	}
	for _, interfacesEntities := range serviceNamedPluginConfigInterfacesMap {
		if interfacesEntities != nil {
			filteredAuthEnabledInterfaceEntities = append(filteredAuthEnabledInterfaceEntities, interfacesEntities)
		}
	}
	return filteredAuthEnabledInterfaceEntities
}

func convertToPluginConfigInterfaces(ctx context.Context, interfaces *models.AuthLatestEnabledInterfaces) *models.PluginConfigInterfaces {
	configInterfaces := &models.PluginConfigInterfaces{}
	configInterfaces.Id = interfaces.Id
	configInterfaces.Action = interfaces.Action
	configInterfaces.FilterRule = interfaces.FilterRule
	configInterfaces.HttpMethod = interfaces.HttpMethod
	configInterfaces.IsAsyncProcessing = interfaces.IsAsyncProcessing
	configInterfaces.Path = interfaces.Path
	configInterfaces.ServiceDisplayName = interfaces.ServiceDisplayName
	configInterfaces.ServiceName = interfaces.ServiceName
	configInterfaces.PluginConfigId = interfaces.PluginConfigId
	configInterfaces.Type = interfaces.Type
	if strings.TrimSpace(interfaces.PluginConfigId) != "" {
		pluginConfigsEntity, err := getPluginConfig(ctx, interfaces.PluginConfigId)
		if err != nil {
			log.Logger.Error("getPluginConfig err", log.Error(err))
		}
		if pluginConfigsEntity != nil {
			pluginPackages, err := getPluginPackages(ctx, pluginConfigsEntity.PluginPackageId)
			if err != nil {
				log.Logger.Error("getPluginPackages err", log.Error(err))
			}
			pluginConfigsEntity.PluginPackages = pluginPackages
			configInterfaces.PluginConfig = pluginConfigsEntity
		}
	}
	inputParameters, err := getAllByConfigInterfaceAddParamType(ctx, interfaces.Id, "INPUT")
	if err != nil {
		log.Logger.Error("getAllByConfigInterfaceAddParamType err", log.Error(err))
	}
	if inputParameters != nil {
		for _, paramEntity := range inputParameters {
			paramEntity.PluginConfigInterface = configInterfaces
			if paramEntity.DataType == "object" {
				paramEntity.ObjectMeta = TryFetchEnrichCoreObjectMeta(ctx, paramEntity)
			}
		}
	}
	outputParameters, err := getAllByConfigInterfaceAddParamType(ctx, interfaces.Id, "OUTPUT")
	if err != nil {
		log.Logger.Error("getAllByConfigInterfaceAddParamType err", log.Error(err))
	}
	if outputParameters != nil {
		for _, paramEntity := range outputParameters {
			paramEntity.PluginConfigInterface = configInterfaces
			if paramEntity.DataType == "object" {
				paramEntity.ObjectMeta = TryFetchEnrichCoreObjectMeta(ctx, paramEntity)
			}
		}
	}
	configInterfaces.InputParameters = inputParameters
	configInterfaces.OutputParameters = outputParameters
	return configInterfaces
}

func TryFetchEnrichCoreObjectMeta(ctx context.Context, param *models.PluginConfigInterfaceParameters) *models.CoreObjectMeta {
	pluginConfigInterface := param.PluginConfigInterface
	if pluginConfigInterface == nil {
		log.Logger.Info("Cannot find plugin_config_interface", log.String("id", param.Id))
		return nil
	}
	pluginConfig := pluginConfigInterface.PluginConfig
	if pluginConfig == nil {
		log.Logger.Info("Cannot find plugin_config", log.String("id", pluginConfigInterface.Id))
		return nil
	}
	pluginPackage := pluginConfig.PluginPackages
	if pluginPackage == nil {
		log.Logger.Info("Cannot find plugin_package", log.String("id", pluginConfig.Id))
		return nil
	}

	if strings.TrimSpace(param.RefObjectName) == "" {
		log.Logger.Info("object name value is blank", log.String("id", param.Id))
		return nil
	}
	return fetchAssembledCoreObjectMeta(ctx, pluginPackage.Name, param.RefObjectName, nil, pluginConfig.Id)
}

func fetchAssembledCoreObjectMeta(ctx context.Context, packageName, objectName string, objectMetaCachedList []*models.CoreObjectMeta, configId string) *models.CoreObjectMeta {
	if objectMetaCachedList == nil {
		objectMetaCachedList = make([]*models.CoreObjectMeta, 0)
	}
	if len(objectMetaCachedList) > 0 {
		for _, m := range objectMetaCachedList {
			if packageName == m.PackageName && objectName == m.Name {
				return m
			}
		}
	}
	objectMetaEntity, err := getOnePluginObjectMetaByCondition(ctx, packageName, objectName, configId)
	if err != nil {
		log.Logger.Error("getOnePluginObjectMetaByCondition err", log.Error(err))
		return nil
	}
	if objectMetaEntity == nil {
		return nil
	}
	objectMetaCachedList = append(objectMetaCachedList, objectMetaEntity)
	propertyMetaEntities, err := getPluginObjectPropertyMetaListByObjectMeta(ctx, objectMetaEntity.Id)
	if err != nil {
		return nil
	}
	if len(propertyMetaEntities) == 0 {
		return nil
	}
	for _, propertyMetaEntity := range propertyMetaEntities {
		if propertyMetaEntity == nil {
			continue
		}
		if propertyMetaEntity.DataType == "object" {
			refObjectMetaEntity := fetchAssembledCoreObjectMeta(ctx, packageName, propertyMetaEntity.RefObjectName, objectMetaCachedList, configId)
			propertyMetaEntity.RefObjectMeta = refObjectMetaEntity
		}
		if len(objectMetaEntity.PropertyMetas) == 0 {
			objectMetaEntity.PropertyMetas = make([]*models.CoreObjectPropertyMeta, 0)
		}
		if propertyMetaEntity.ObjectMeta == nil {
			propertyMetaEntity.ObjectMeta = objectMetaEntity
		}
		objectMetaEntity.PropertyMetas = append(objectMetaEntity.PropertyMetas, propertyMetaEntity)
	}
	return objectMetaEntity
}

func buildPluginConfigInterfaceDto(interfaceEntity *models.PluginConfigInterfaces) *models.PluginConfigInterfaceDto {
	dto := &models.PluginConfigInterfaceDto{}
	dto.Id = interfaceEntity.Id
	dto.PluginConfigId = interfaceEntity.PluginConfigId
	dto.Path = interfaceEntity.Path
	dto.ServiceName = interfaceEntity.ServiceName
	dto.ServiceDisplayName = interfaceEntity.ServiceDisplayName
	dto.Action = interfaceEntity.Action
	dto.HttpMethod = interfaceEntity.HttpMethod
	dto.IsAsyncProcessing = interfaceEntity.IsAsyncProcessing
	dto.FilterRule = interfaceEntity.FilterRule
	dto.Description = interfaceEntity.Description
	dto.Type = interfaceEntity.Type
	inputParameterEntities := interfaceEntity.InputParameters
	if inputParameterEntities != nil {
		var inputParamDtoList = make([]*models.PluginConfigInterfaceParameterDto, 0)
		for _, paramEntity := range inputParameterEntities {
			if paramEntity == nil {
				continue
			}
			paramDto := models.ConvertPluginConfigInterfaceParameter2Dto(paramEntity)
			if paramEntity.ObjectMeta != nil {
				objectMetaDto := models.ConvertCoreObjectMeta2Dto(paramEntity.ObjectMeta)
				if objectMetaDto != nil {
					paramDto.RefObjectMeta = objectMetaDto
				}
			}
			inputParamDtoList = append(inputParamDtoList, paramDto)
		}
		dto.InputParameters = inputParamDtoList
	}

	outputParameterEntities := interfaceEntity.OutputParameters
	if outputParameterEntities != nil {
		var outputParamDtoList = make([]*models.PluginConfigInterfaceParameterDto, 0)
		for _, paramEntity := range outputParameterEntities {
			if paramEntity == nil {
				continue
			}
			paramDto := models.ConvertPluginConfigInterfaceParameter2Dto(paramEntity)
			if paramEntity.ObjectMeta != nil {
				objectMetaDto := models.ConvertCoreObjectMeta2Dto(paramEntity.ObjectMeta)
				if objectMetaDto != nil {
					paramDto.RefObjectMeta = objectMetaDto
				}
			}
			outputParamDtoList = append(outputParamDtoList, paramDto)
		}
		dto.OutputParameters = outputParamDtoList
	}
	return dto
}

func generateServiceName(entity *models.AuthLatestEnabledInterfaces) string {
	var pluginConfigRegisterName = entity.PluginConfigRegisterName
	if pluginConfigRegisterName != "" {
		pluginConfigRegisterName = "(" + pluginConfigRegisterName + ")"
	}
	return entity.PluginPackageName + "/" + entity.PluginConfigName + pluginConfigRegisterName + "/" + entity.Action
}

func getAllByConfigInterfaceAddParamType(ctx context.Context, id, paramType string) (list []*models.PluginConfigInterfaceParameters, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select id,plugin_config_interface_id,type,name,data_type,mapping_type,mapping_entity_expression,"+
		"mapping_system_variable_name, required, sensitive_data,description,mapping_val,ref_object_name,multiple from plugin_config_interface_parameters"+
		" where plugin_config_interface_id =? and type = ?", id, paramType).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func getPluginConfig(ctx context.Context, id string) (result *models.PluginConfigs, err error) {
	var list []*models.PluginConfigs
	err = db.MysqlEngine.Context(ctx).SQL("select id,plugin_package_id,name,target_package,target_entity,"+
		"target_entity_filter_rule,register_name,status from plugin_configs where id=?", id).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(list) > 0 {
		result = list[0]
	}
	return
}

func getPluginPackages(ctx context.Context, id string) (result *models.PluginPackages, err error) {
	var list []*models.PluginPackages
	err = db.MysqlEngine.Context(ctx).SQL("select id,name,version,status,upload_timestamp,ui_package_included,"+
		"edition from plugin_packages where id=?", id).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(list) > 0 {
		result = list[0]
	}
	return
}

// getLatestAvailableDataModelEntity 根据包名获取最新的插件实体
func getLatestAvailableDataModelEntity(ctx context.Context, packageName string) (result *models.PluginPackageDataModel, err error) {
	if packageName == "" {
		return
	}
	var list []*models.PluginPackageDataModel
	err = db.MysqlEngine.Context(ctx).SQL("SELECT t1.id, t1.version, t1.package_name,t1.is_dynamic, t1.update_path, t1.update_method,"+
		"t1.update_source,t1.update_time FROM plugin_package_data_model t1 WHERE t1.package_name = ? AND t1.version = (SELECT max(t2.version) "+
		"FROM plugin_package_data_model t2 WHERE t2.package_name = ? GROUP BY t2.package_name )", packageName, packageName).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(list) > 0 {
		result = list[0]
	}
	return
}

func getDataModelsByPackageName(ctx context.Context, packageName string) (list []*models.PluginPackageDataModel, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("SELECT t1.id, t1.version, t1.package_name,t1.is_dynamic, t1.update_path, t1.update_method,"+
		"t1.update_source,t1.update_time FROM plugin_package_data_model t1 WHERE t1.package_name = ? order by t1.version desc limit 10",
		packageName).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func getAllByDataModel(ctx context.Context, id string) (list []*models.PluginPackageEntities, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select id, data_model_id, data_model_version, package_name,name, display_name,description "+
		"from plugin_package_entities where data_model_id =?", id).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func getAllAuthEnableInterfacesByCondition(ctx context.Context, interfacesQueryDto models.AuthEnableInterfacesQueryDto) (authEnableInterfaceEntities []*models.AuthLatestEnabledInterfaces, err error) {
	var sql = getAuthEnableInterfacesCommonQuerySQL()
	var param []interface{}
	if interfacesQueryDto.TargetEntityFilterRule == "" {
		sql = sql + "AND (t2.target_entity_filter_rule IS NULL OR t2.target_entity_filter_rule = '') "
	} else {
		sql = sql + "AND t2.target_entity_filter_rule = ? "
		param = append(param, interfacesQueryDto.TargetEntityFilterRule)
	}
	sql = sql + " AND t2.status = ? AND t2.plugin_package_id = t3.id AND t2.target_package = ? AND t2.target_entity = ? AND t4.plugin_cfg_id = t2.id AND t4.perm_type =? "
	param = append(param, []interface{}{interfacesQueryDto.PluginConfigStatus, interfacesQueryDto.TargetPackage, interfacesQueryDto.TargetEntity, interfacesQueryDto.PermissionType}...)
	if len(interfacesQueryDto.RoleNames) > 0 {
		userRolesFilterSql, userRolesFilterParam := createListParams(interfacesQueryDto.RoleNames, "")
		sql = sql + " AND t4.role_name IN (" + userRolesFilterSql + ")"
		param = append(param, userRolesFilterParam...)
	}
	if len(interfacesQueryDto.PluginPackageStatuses) > 0 {
		statusFilterSql, statusFilterParam := createListParams(interfacesQueryDto.PluginPackageStatuses, "")
		sql = sql + " AND t3.status IN (" + statusFilterSql + ")"
		param = append(param, statusFilterParam...)
	}
	err = db.MysqlEngine.Context(ctx).SQL(sql, param...).Find(&authEnableInterfaceEntities)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func getAllAuthEnabledInterfacesByNullTargetInfo(ctx context.Context, pluginConfigStatus, permissionType string, roleNames, pluginPackageStatuses []string) (authEnableInterfaceEntities []*models.AuthLatestEnabledInterfaces, err error) {
	var sql = getAuthEnableInterfacesCommonQuerySQL()
	var param []interface{}
	sql = sql + " AND t2.status = ? AND t2.plugin_package_id = t3.id AND ( t2.target_entity = '' OR t2.target_entity is null) AND t4.plugin_cfg_id = t2.id AND t4.perm_type =? "
	param = append(param, []interface{}{pluginConfigStatus, permissionType}...)
	if len(roleNames) > 0 {
		userRolesFilterSql, userRolesFilterParam := createListParams(roleNames, "")
		sql = sql + " AND t4.role_name IN (" + userRolesFilterSql + ")"
		param = append(param, userRolesFilterParam...)
	}
	if len(pluginPackageStatuses) > 0 {
		statusFilterSql, statusFilterParam := createListParams(pluginPackageStatuses, "")
		sql = sql + " AND t3.status IN (" + statusFilterSql + ")"
		param = append(param, statusFilterParam...)
	}
	err = db.MysqlEngine.Context(ctx).SQL(sql, param...).Find(&authEnableInterfaceEntities)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func getOnePluginObjectMetaByCondition(ctx context.Context, packageName, objectName, configId string) (result *models.CoreObjectMeta, err error) {
	var list []*models.CoreObjectMeta
	err = db.MysqlEngine.Context(ctx).SQL("select id,name,package_name,source,latest_source,created_by,created_time,updated_by,"+
		"updated_time,config_id from plugin_object_meta where package_name=? and name=? and config_id=?", packageName, objectName, configId).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(list) > 0 {
		result = list[0]
	}
	return
}

func getPluginObjectPropertyMetaListByObjectMeta(ctx context.Context, objectMeatId string) (list []*models.CoreObjectPropertyMeta, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select id,name,data_type,multiple,map_type,map_expr,object_meta_id,object_name,package_name,"+
		"source,created_by,created_time,updated_by,updated_time,is_sensitive,ref_object_name,config_id from plugin_object_property_meta where "+
		"object_meta_id =?", objectMeatId).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func createListParams(inputList []string, prefix string) (specSql string, paramList []interface{}) {
	if len(inputList) > 0 {
		var specList []string
		for _, v := range inputList {
			specList = append(specList, "?")
			paramList = append(paramList, prefix+v)
		}
		specSql = strings.Join(specList, ",")
	}
	return
}

func getAuthEnableInterfacesCommonQuerySQL() string {
	return "SELECT DISTINCT t1.id AS id,t1.plugin_config_id,t1.action, t1.service_name,t1.service_display_name,t1.path,t1.http_method,t1.is_async_processing,t1.type," +
		"t1.filter_rule,t2.name AS plugin_config_name,t2.register_name AS plugin_config_register_name,t2.target_entity AS plugin_config_target_entity,t2.status AS plugin_config_status," +
		"t3.id AS plugin_package_id,t3.name AS plugin_package_name,t3.status AS plugin_package_status,t3.version AS plugin_package_version,t3.upload_timestamp FROM plugin_config_interfaces t1," +
		"plugin_configs t2, plugin_packages t3,plugin_config_roles t4  WHERE  t1.plugin_config_id = t2.id "
}

func GetPluginConfigInterfaceById(id string) (result *models.PluginConfigInterfaces, err error) {
	result = &models.PluginConfigInterfaces{}
	found, errPCI := db.MysqlEngine.Table(new(models.PluginConfigInterfaces)).Where("id=?", id).Get(&result)
	if errPCI != nil {
		result = nil
		err = err
		return
	}
	if !found {
		result = nil
		return
	}
	inputParams := make([]*models.PluginConfigInterfaceParameters, 0)
	err = db.MysqlEngine.Table(new(models.PluginConfigInterfaceParameters)).Where("plugin_config_interface_id=?", id).And("type=?", models.PluginParamTypeInput).Find(&inputParams)
	if err != nil {
		result = nil
		err = err
		return
	}
	outputParams := make([]*models.PluginConfigInterfaceParameters, 0)
	err = db.MysqlEngine.Table(new(models.PluginConfigInterfaceParameters)).Where("plugin_config_interface_id=?", id).And("type=?", models.PluginParamTypeOutput).Find(&outputParams)
	if err != nil {
		result = nil
		err = err
		return
	}
	result.InputParameters = inputParams
	result.InputParameters = outputParams
	return
}
