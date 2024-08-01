package database

import (
	"context"
	"fmt"
	"strings"
	"time"

	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"github.com/gin-gonic/gin"
)

func GetPluginConfigs(ctx context.Context, pluginPackageId string, roles []string, pluginConfigId string) (result []*models.PluginConfigQueryObj, err error) {
	var pluginConfigRows []*models.PluginConfigs
	if pluginConfigId != "" {
		err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_configs where id = ?", pluginConfigId).Find(&pluginConfigRows)
	} else {
		err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_configs where plugin_package_id = ? order by name ASC, id ASC", pluginPackageId).Find(&pluginConfigRows)
	}
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	result = []*models.PluginConfigQueryObj{}
	if len(pluginConfigRows) == 0 {
		return
	}
	var pluginConfigIds []string
	for _, v := range pluginConfigRows {
		pluginConfigIds = append(pluginConfigIds, v.Id)
	}
	pluginConfigFilter, pluginConfigParams := db.CreateListParams(pluginConfigIds, "")
	var pluginConfigRolesRows []*models.PluginConfigRoles
	err = db.MysqlEngine.Context(ctx).SQL("select plugin_cfg_id,perm_type,role_id,role_name from plugin_config_roles where plugin_cfg_id in ("+pluginConfigFilter+")", pluginConfigParams...).Find(&pluginConfigRolesRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	permissionMap := make(map[string]*models.PermissionRoleData)
	for _, row := range pluginConfigRolesRows {
		if v, b := permissionMap[row.PluginCfgId]; b {
			if row.PermType == "MGMT" {
				v.MGMT = append(v.MGMT, row.RoleName)
			} else {
				v.USE = append(v.USE, row.RoleName)
			}
		} else {
			newPermObj := models.PermissionRoleData{MGMT: []string{}, USE: []string{}}
			if row.PermType == "MGMT" {
				newPermObj.MGMT = append(newPermObj.MGMT, row.RoleName)
			} else {
				newPermObj.USE = append(newPermObj.USE, row.RoleName)
			}
			permissionMap[row.PluginCfgId] = &newPermObj
		}
	}
	for k, v := range permissionMap {
		matchFlag := false
		for _, legalRole := range append(v.MGMT, v.USE...) {
			for _, userRole := range roles {
				if userRole == legalRole {
					matchFlag = true
					break
				}
			}
			if matchFlag {
				break
			}
		}
		if !matchFlag {
			delete(permissionMap, k)
		}
	}
	pluginConfigNameMapIndex := make(map[string]int)
	for _, row := range pluginConfigRows {
		row.TargetEntityWithFilterRule = fmt.Sprintf("%s:%s%s", row.TargetPackage, row.TargetEntity, row.TargetEntityFilterRule)
		if permObj, ok := permissionMap[row.Id]; ok {
			tmpObj := models.PluginConfigDto{PluginConfigs: *row, PermissionToRole: permObj, FilterRule: row.TargetEntityFilterRule}
			if nameIndex, existFlag := pluginConfigNameMapIndex[row.Name]; existFlag {
				result[nameIndex].PluginConfigDtoList = append(result[nameIndex].PluginConfigDtoList, &tmpObj)
			} else {
				pluginConfigNameMapIndex[row.Name] = len(result)
				result = append(result, &models.PluginConfigQueryObj{PluginConfigName: row.Name, PluginConfigDtoList: []*models.PluginConfigDto{&tmpObj}})
			}
		} else {
			// 添加根节点
			if row.TargetPackage == "" && row.TargetEntity == "" &&
				row.TargetEntityFilterRule == "" && row.RegisterName == "" {
				tmpObj := models.PluginConfigDto{PluginConfigs: *row, PermissionToRole: permObj, FilterRule: row.TargetEntityFilterRule}
				if nameIndex, existFlag := pluginConfigNameMapIndex[row.Name]; existFlag {
					result[nameIndex].PluginConfigDtoList = append(result[nameIndex].PluginConfigDtoList, &tmpObj)
				} else {
					pluginConfigNameMapIndex[row.Name] = len(result)
					result = append(result, &models.PluginConfigQueryObj{PluginConfigName: row.Name, PluginConfigDtoList: []*models.PluginConfigDto{&tmpObj}})
				}
			}
		}
	}
	return
}

func QueryPluginConfigInfo(ctx context.Context, pluginConfigIds []string) (result []*models.PluginConfigs, err error) {
	result = []*models.PluginConfigs{}
	if len(pluginConfigIds) == 0 {
		return
	}

	pluginCfgFilter, pluginCfgParams := db.CreateListParams(pluginConfigIds, "")
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_configs where id in ("+pluginCfgFilter+") ", pluginCfgParams...).Find(&result)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(result) == 0 {
		return
	}

	// query plugin package
	pluginPackageIdsMap := make(map[string]struct{})
	for _, pluginCfgInfo := range result {
		pluginPackageIdsMap[pluginCfgInfo.PluginPackageId] = struct{}{}
	}

	var pluginPackageIds []string
	for packageId := range pluginPackageIdsMap {
		pluginPackageIds = append(pluginPackageIds, packageId)
	}
	if len(pluginPackageIds) == 0 {
		return
	}

	var pluginPackageRows []*models.PluginPackages
	pluginPackageFilter, pluginPackageParams := db.CreateListParams(pluginPackageIds, "")
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_packages where id in ("+pluginPackageFilter+") ", pluginPackageParams...).Find(&pluginPackageRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(pluginPackageRows) == 0 {
		return
	}

	pluginPackageIdMapInfo := make(map[string]*models.PluginPackages)
	for i, packageInfo := range pluginPackageRows {
		pluginPackageIdMapInfo[packageInfo.Id] = pluginPackageRows[i]
	}

	for i, pluginCfgInfo := range result {
		if _, isExisted := pluginPackageIdMapInfo[pluginCfgInfo.PluginPackageId]; isExisted {
			result[i].PluginPackages = pluginPackageIdMapInfo[pluginCfgInfo.PluginPackageId]
		}
	}

	return
}

func QueryCoreObjectMeta(ctx context.Context, packageName, objectName string, configId string) *models.CoreObjectMeta {
	objectMetaEntity, err := getOnePluginObjectMetaByCondition(ctx, packageName, objectName, configId)
	if err != nil {
		log.Logger.Error("getOnePluginObjectMetaByCondition err", log.Error(err))
		return nil
	}
	if objectMetaEntity == nil {
		return nil
	}
	objectMetaEntity.MappingEntityExpression = objectMetaEntity.MapExpr

	propertyMetaEntities, err := getPluginObjectPropertyMetaListByObjectMeta(ctx, objectMetaEntity.Id)
	if err != nil {
		log.Logger.Error("getPluginObjectPropertyMetaListByObjectMeta err", log.Error(err))
		return nil
	}
	if len(propertyMetaEntities) == 0 {
		return nil
	}
	for _, propertyMetaEntity := range propertyMetaEntities {
		if propertyMetaEntity == nil {
			continue
		}
		propertyMetaEntity.SensitiveData = "N"
		if propertyMetaEntity.Sensitive {
			propertyMetaEntity.SensitiveData = "Y"
		}
		propertyMetaEntity.MappingEntityExpression = propertyMetaEntity.MapExpr
		if propertyMetaEntity.DataType == models.PluginParamDataTypeObject {
			refObjectMetaEntity := QueryCoreObjectMeta(ctx, packageName, propertyMetaEntity.RefObjectName, configId)
			propertyMetaEntity.RefObjectMeta = refObjectMetaEntity
		}
	}
	objectMetaEntity.PropertyMetas = propertyMetaEntities
	return objectMetaEntity
}

func QueryCoreObjectMetaV2(ctx context.Context, pluginConfigId string, objectName string) (result *models.CoreObjectMeta, err error) {
	var packageName string
	existed, tmpErr := db.MysqlEngine.Context(ctx).SQL("select pkg.name from plugin_configs cfg left join plugin_packages pkg on cfg.plugin_package_id=pkg.id where cfg.id=?", pluginConfigId).Get(&packageName)
	if tmpErr != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, tmpErr)
		return
	}
	if !existed {
		err = fmt.Errorf("can not find packageName by pluginConfigId: %s", pluginConfigId)
		return
	}

	result = QueryCoreObjectMeta(ctx, packageName, objectName, pluginConfigId)
	if result == nil {
		err = fmt.Errorf("query coreObjectMeta by packageName: %s, objectName: %s, pluginConfigId: %s empty", packageName, objectName, pluginConfigId)
		return
	}
	return
}

func GetConfigInterfaces(ctx context.Context, pluginConfigId string) (result []*models.PluginInterfaceQueryObj, err error) {
	var interfaceRows []*models.PluginConfigInterfaces
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_config_interfaces where plugin_config_id=?", pluginConfigId).Find(&interfaceRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	result = []*models.PluginInterfaceQueryObj{}
	if len(interfaceRows) == 0 {
		return
	}
	var interfaceIds []string
	pluginConfigIdsMap := make(map[string]struct{})
	for _, v := range interfaceRows {
		interfaceIds = append(interfaceIds, v.Id)
		pluginConfigIdsMap[v.PluginConfigId] = struct{}{}
		result = append(result, &models.PluginInterfaceQueryObj{PluginConfigInterfaces: *v, InputParameters: []*models.PluginConfigInterfaceParameters{}, OutputParameters: []*models.PluginConfigInterfaceParameters{}})
	}

	var pluginConfigIds []string
	for cfgId := range pluginConfigIdsMap {
		pluginConfigIds = append(pluginConfigIds, cfgId)
	}
	pluginConfigList, err := QueryPluginConfigInfo(ctx, pluginConfigIds)
	if err != nil {
		err = fmt.Errorf("QueryPluginConfigInfo failed: %s", err.Error())
		return
	}
	pluginConfigIdMapInfo := make(map[string]*models.PluginConfigs)
	for i, pluginConfigInfo := range pluginConfigList {
		pluginConfigIdMapInfo[pluginConfigInfo.Id] = pluginConfigList[i]
	}

	interfaceIdMapPackageName := make(map[string]string)
	interfaceIdMapPluginCfgId := make(map[string]string)
	for i := range result {
		if pluginConfigInfo, isExisted := pluginConfigIdMapInfo[result[i].PluginConfigId]; isExisted {
			result[i].PluginConfig = pluginConfigInfo
			interfaceIdMapPluginCfgId[result[i].Id] = result[i].PluginConfigId

			if result[i].PluginConfig.PluginPackages != nil {
				interfaceIdMapPackageName[result[i].Id] = result[i].PluginConfig.PluginPackages.Name
			}
		}
	}

	interfaceFilter, interfaceParams := db.CreateListParams(interfaceIds, "")
	var interfaceParamRows []*models.PluginConfigInterfaceParameters
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_config_interface_parameters where plugin_config_interface_id in ("+interfaceFilter+") order by name", interfaceParams...).Find(&interfaceParamRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	inputMap := make(map[string][]*models.PluginConfigInterfaceParameters)
	outputMap := make(map[string][]*models.PluginConfigInterfaceParameters)
	for _, row := range interfaceParamRows {
		if row.Type == "INPUT" {
			if v, b := inputMap[row.PluginConfigInterfaceId]; b {
				inputMap[row.PluginConfigInterfaceId] = append(v, row)
			} else {
				inputMap[row.PluginConfigInterfaceId] = []*models.PluginConfigInterfaceParameters{row}
			}
		} else {
			if v, b := outputMap[row.PluginConfigInterfaceId]; b {
				outputMap[row.PluginConfigInterfaceId] = append(v, row)
			} else {
				outputMap[row.PluginConfigInterfaceId] = []*models.PluginConfigInterfaceParameters{row}
			}
		}

		if row.DataType == models.PluginParamDataTypeObject {
			if packageName, isExisted := interfaceIdMapPackageName[row.PluginConfigInterfaceId]; isExisted {
				configId := interfaceIdMapPluginCfgId[row.PluginConfigInterfaceId]
				row.RefObjectMeta = QueryCoreObjectMeta(ctx, packageName, row.RefObjectName, configId)
			}
		}
	}
	for _, v := range result {
		if inputParams, b := inputMap[v.Id]; b {
			v.InputParameters = inputParams
		}
		if outputParams, b := outputMap[v.Id]; b {
			v.OutputParameters = outputParams
		}
	}
	return
}

func GetConfigInterfacesById(ctx context.Context, id string) (result *models.PluginConfigInterfaces, err error) {
	var list []*models.PluginConfigInterfaces
	err = db.MysqlEngine.Context(ctx).SQL("select id, plugin_config_id, action, service_name,service_display_name, path, http_method,"+
		"is_async_processing,type, filter_rule,description from  plugin_config_interfaces where id= ?", id).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(list) > 0 {
		result = list[0]
	}
	return
}

func GetPluginConfigById(ctx context.Context, id string) (result *models.PluginConfigs, err error) {
	var list []*models.PluginConfigs
	err = db.MysqlEngine.Context(ctx).SQL("select id,plugin_package_id, name, target_package,target_entity,"+
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

func GetPluginPackageById(ctx context.Context, id string) (result *models.PluginPackages, err error) {
	var list []*models.PluginPackages
	err = db.MysqlEngine.Context(ctx).SQL("select id,name,version,status,upload_timestamp,ui_package_included,edition from plugin_packages where id=?", id).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(list) > 0 {
		result = list[0]
	}
	return
}

func GetPluginConfigInterfaceParameters(ctx context.Context, pluginConfigInterfaceId, parameterType string) (list []*models.PluginConfigInterfaceParameters, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select id,plugin_config_interface_id, type, name,data_type,mapping_type,mapping_entity_expression,"+
		"mapping_system_variable_name,required,sensitive_data,description,mapping_val,ref_object_name,multiple from plugin_config_interface_parameters"+
		" where plugin_config_interface_id = ? and type =?", pluginConfigInterfaceId, parameterType).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func UpdatePluginConfigRoles(c *gin.Context, pluginConfigId string, reqParam *models.UpdatePluginCfgRolesReqParam) (err error) {
	var actions []*db.ExecAction
	now := time.Now()
	reqUser := middleware.GetRequestUser(c)
	// check whether pluginConfigId is valid
	pluginConfigData := &models.PluginConfigs{}
	var exists bool
	// todo!!
	/*
		exists, err = db.MysqlEngine.Context(c).Table(models.TableNamePluginConfigs).
			Where("id = ?", pluginConfigId).
			Get(pluginConfigData)
	*/
	exists, err = db.MysqlEngine.Context(c).SQL(fmt.Sprintf("select * from %s where id=?", models.TableNamePluginConfigs), pluginConfigId).Get(pluginConfigData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if !exists {
		err = fmt.Errorf("pluginConfigId: %s is invalid", pluginConfigId)
		return
	}

	userToken := c.GetHeader(models.AuthorizationHeader)
	language := c.GetHeader(middleware.AcceptLanguageHeader)
	respData, err := remote.RetrieveAllLocalRoles("Y", userToken, language, false)
	if err != nil {
		err = fmt.Errorf("retrieve all local roles failed: %s", err.Error())
		return
	}

	roleNameMapId := make(map[string]string)
	if len(respData.Data) > 0 {
		for _, roleDto := range respData.Data {
			roleNameMapId[roleDto.Name] = roleDto.ID
		}
	} else {
		log.Logger.Error("retrieve all local roles empty")
	}

	// firstly delete original pluginConfigRole and then create new pluginConfigRole
	action := &db.ExecAction{
		Sql:   db.CombineDBSql("DELETE FROM ", models.TableNamePluginConfigRoles, " WHERE plugin_cfg_id=?"),
		Param: []interface{}{pluginConfigId},
	}
	actions = append(actions, action)

	pluginConfigRolesList := []*models.PluginConfigRoles{}
	mgmtRoleNameMap := make(map[string]struct{})
	for _, roleName := range reqParam.PermissionToRole.MGMT {
		if _, isExisted := mgmtRoleNameMap[roleName]; !isExisted {
			mgmtRoleNameMap[roleName] = struct{}{}
			pluginConfigRolesList = append(pluginConfigRolesList, &models.PluginConfigRoles{
				Id:          models.IdPrefixPluCfgRol + guid.CreateGuid(),
				IsActive:    true,
				PermType:    models.PermissionTypeMGMT,
				PluginCfgId: pluginConfigId,
				RoleId:      roleNameMapId[roleName],
				RoleName:    roleName,
				CreatedBy:   reqUser,
				CreatedTime: now,
				UpdatedTime: now,
			})
		}
	}

	useRoleNameMap := make(map[string]struct{})
	for _, roleName := range reqParam.PermissionToRole.USE {
		if _, isExisted := useRoleNameMap[roleName]; !isExisted {
			useRoleNameMap[roleName] = struct{}{}
			pluginConfigRolesList = append(pluginConfigRolesList, &models.PluginConfigRoles{
				Id:          models.IdPrefixPluCfgRol + guid.CreateGuid(),
				IsActive:    true,
				PermType:    models.PermissionTypeUSE,
				PluginCfgId: pluginConfigId,
				RoleId:      roleNameMapId[roleName],
				RoleName:    roleName,
				CreatedBy:   reqUser,
				CreatedTime: now,
				UpdatedTime: now,
			})
		}
	}

	for i := range pluginConfigRolesList {
		action, tmpErr := db.GetInsertTableExecAction(models.TableNamePluginConfigRoles, *pluginConfigRolesList[i], nil)
		if tmpErr != nil {
			err = fmt.Errorf("get insert sql failed: %s", tmpErr.Error())
			return
		}
		actions = append(actions, action)
	}

	err = db.Transaction(actions, c)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	return
}

func GetBatchPluginConfigs(c *gin.Context, pluginPackageId string) (result []*models.PluginConfigsOutlines, err error) {
	result = []*models.PluginConfigsOutlines{}
	pluginPackageData := &models.PluginPackages{}
	var exists bool
	// todo!!
	/*
		exists, err = db.MysqlEngine.Context(c).Table(models.TableNamePluginPackages).
			Where("id = ?", pluginPackageId).
			Get(pluginPackageData)
	*/
	exists, err = db.MysqlEngine.Context(c).SQL(fmt.Sprintf("select * from %s where id=?", models.TableNamePluginPackages), pluginPackageId).Get(pluginPackageData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if !exists {
		err = fmt.Errorf("pluginPackageId: %s is invalid", pluginPackageId)
		return
	}

	var pluginConfigsList []*models.PluginConfigsOutlines
	// todo!!
	/*
		err = db.MysqlEngine.Context(c).Table(models.TableNamePluginConfigs).
			Where("plugin_package_id = ?", pluginPackageId).
			Asc("id").
			Find(&pluginConfigsList)
	*/
	err = db.MysqlEngine.Context(c).SQL(fmt.Sprintf("select * from %s where plugin_package_id=? order by id asc", models.TableNamePluginConfigs), pluginPackageId).Find(&pluginConfigsList)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	if len(pluginConfigsList) == 0 {
		return
	}

	pluginCfgIdMap := make(map[string]struct{})
	pluginConfigsOutlinesMap := make(map[string]*models.PluginConfigsOutlines)
	for _, pluginCfgData := range pluginConfigsList {
		pluginCfgData.TargetEntityWithFilterRule = fmt.Sprintf("%s:%s%s", pluginCfgData.TargetPackage, pluginCfgData.TargetEntity, pluginCfgData.TargetEntityFilterRule)
		pluginCfgData.HasMgmtPermission = true

		if pluginCfgData.TargetPackage == "" && pluginCfgData.TargetEntity == "" &&
			pluginCfgData.TargetEntityFilterRule == "" && pluginCfgData.RegisterName == "" {
			// root node
			pluginCfgData.PluginConfigsOutlines = []*models.PluginConfigsOutlines{}
			pluginConfigsOutlinesMap[pluginCfgData.Name] = pluginCfgData
			pluginCfgIdMap[pluginCfgData.Id] = struct{}{}
			result = append(result, pluginCfgData)
		}
	}

	for _, pluginCfgData := range pluginConfigsList {
		if _, isExisted := pluginCfgIdMap[pluginCfgData.Id]; !isExisted {
			if pluginCfgOutlines, isOk := pluginConfigsOutlinesMap[pluginCfgData.Name]; isOk {
				pluginCfgOutlines.PluginConfigsOutlines = append(pluginCfgOutlines.PluginConfigsOutlines, pluginCfgData)
			}
		}
	}

	return
}

func BatchEnablePluginConfig(c *gin.Context, reqParam []*models.PluginConfigsBatchEnable, pluginPackageId string) (err error) {
	var actions []*db.ExecAction
	// check whether pluginPackageId is valid
	pluginPackageData := &models.PluginPackages{}
	var exists bool
	// todo!!
	/*
		exists, err = db.MysqlEngine.Context(c).Table(models.TableNamePluginPackages).
			Where("id = ?", pluginPackageId).
			Get(pluginPackageData)
	*/
	exists, err = db.MysqlEngine.Context(c).SQL(fmt.Sprintf("select * from %s where id=?", models.TableNamePluginPackages), pluginPackageId).Get(pluginPackageData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if !exists {
		err = fmt.Errorf("pluginPackageId: %s is invalid", pluginPackageId)
		return
	}

	for _, param := range reqParam {
		for _, pluginCfg := range param.PluginConfigs {
			status := models.PluginStatusDisabled
			if pluginCfg.Checked {
				status = models.PluginStatusEnabled
			}
			action := &db.ExecAction{
				Sql:   db.CombineDBSql("UPDATE ", models.TableNamePluginConfigs, " SET status=? WHERE id=? AND plugin_package_id=?"),
				Param: []interface{}{status, pluginCfg.Id, pluginPackageId},
			}
			actions = append(actions, action)
		}
	}

	err = db.Transaction(actions, c)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	return
}

func UpdatePluginConfigStatus(c *gin.Context, pluginConfigId string, status string) (result *models.PluginConfigDto, err error) {
	result = &models.PluginConfigDto{}

	// check whether pluginConfigId is valid
	pluginConfigsData := &models.PluginConfigs{}
	var exists bool
	/*
		exists, err = db.MysqlEngine.Context(c).Table(models.TableNamePluginConfigs).
			Where("id = ?", pluginConfigId).
			Get(pluginConfigsData)
	*/
	exists, err = db.MysqlEngine.Context(c).SQL(fmt.Sprintf("select * from %s where id=?", models.TableNamePluginConfigs), pluginConfigId).Get(pluginConfigsData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if !exists {
		err = fmt.Errorf("pluginConfigId: %s is invalid", pluginConfigId)
		return
	}

	var actions []*db.ExecAction
	action := &db.ExecAction{
		Sql:   db.CombineDBSql("UPDATE ", models.TableNamePluginConfigs, " SET status=? WHERE id=?"),
		Param: []interface{}{status, pluginConfigId},
	}
	actions = append(actions, action)
	err = db.Transaction(actions, c)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}

	// query plugin configs by id
	/*
		exists, err = db.MysqlEngine.Context(c).Table(models.TableNamePluginConfigs).
			Where("id = ?", pluginConfigId).
			Get(pluginConfigsData)
	*/
	exists, err = db.MysqlEngine.Context(c).SQL(fmt.Sprintf("select * from %s where id=?", models.TableNamePluginConfigs), pluginConfigId).Get(pluginConfigsData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if !exists {
		err = fmt.Errorf("pluginConfigId: %s is invalid", pluginConfigId)
		return
	}
	result.PluginConfigs = *pluginConfigsData
	return
}

func SavePluginConfig(c *gin.Context, reqParam *models.PluginConfigDto) (result *models.PluginConfigDto, err error) {
	var actions []*db.ExecAction
	// check whether pluginPackageId is valid
	pluginPackageId := reqParam.PluginPackageId
	pluginPackageData := &models.PluginPackages{}
	var exists bool
	// todo!!
	/*
		exists, err = db.MysqlEngine.Context(c).Table(models.TableNamePluginPackages).
			Where("id = ?", pluginPackageId).
			Get(pluginPackageData)
	*/
	exists, err = db.MysqlEngine.Context(c).SQL(fmt.Sprintf("select * from %s where id=?", models.TableNamePluginPackages), pluginPackageId).Get(pluginPackageData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if !exists {
		err = fmt.Errorf("pluginPackageId: %s is invalid", pluginPackageId)
		return
	}

	pluginConfigId := reqParam.Id
	if pluginConfigId != "" {
		// check whether pluginConfigId is belonged to pluginPackageId
		pluginConfigData := &models.PluginConfigs{}
		// todo!!
		/*
			exists, err = db.MysqlEngine.Context(c).Table(models.TableNamePluginConfigs).
				Where("id = ? AND plugin_package_id = ?", pluginConfigId, pluginPackageId).
				Get(pluginConfigData)
		*/
		exists, err = db.MysqlEngine.Context(c).SQL(fmt.Sprintf("select * from %s where id=? AND plugin_package_id = ?", models.TableNamePluginConfigs), pluginConfigId, pluginPackageId).Get(pluginConfigData)
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}
		if !exists {
			err = fmt.Errorf("pluginConfigId: %s is not belonged to pluginPackageId: %s", pluginConfigId, pluginPackageId)
			return
		}

		delActions, tmpErr := GetDelPluginConfigActions(c, pluginConfigId)
		if tmpErr != nil {
			err = fmt.Errorf("get delete pluginConfig actions failed: %s", tmpErr.Error())
			return
		}
		actions = append(actions, delActions...)
	} else {
		pluginConfigId = models.IdPrefixPluCfg + guid.CreateGuid()
	}

	// 校验 pluginPackage 的 pluginConfig name 是否已存在 registerName
	var isRegisterNameValid bool
	isRegisterNameValid, err = validateRegisterName(c, pluginPackageId, reqParam.Name, reqParam.RegisterName, pluginConfigId)
	if err != nil {
		err = fmt.Errorf("validate registerName: %s failed: %s", reqParam.RegisterName, err.Error())
		return
	}
	if !isRegisterNameValid {
		err = fmt.Errorf(fmt.Sprintf("PluginPackage[:%s] already have this PluginConfig[:%s] with RegisterName[:%s]",
			pluginPackageId, reqParam.Name, reqParam.RegisterName))
		return
	}

	createActions, tmpErr := GetCreatePluginConfigActions(c, pluginConfigId, reqParam, false, pluginPackageData)
	if tmpErr != nil {
		err = fmt.Errorf("get create pluginConfig actions failed: %s", tmpErr.Error())
		return
	}
	actions = append(actions, createActions...)

	err = db.Transaction(actions, c)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}

	// query pluginConfig
	// result, err = GetPluginConfigQueryObjById(c, pluginConfigId)
	var pluginConfigQueryObjList []*models.PluginConfigQueryObj
	pluginConfigQueryObjList, err = GetPluginConfigsWithInterfaces(c, "", middleware.GetRequestRoles(c), pluginConfigId)
	if err != nil {
		return
	}
	if len(pluginConfigQueryObjList) > 0 {
		if len(pluginConfigQueryObjList[0].PluginConfigDtoList) > 0 {
			result = pluginConfigQueryObjList[0].PluginConfigDtoList[0]
		}
	}
	return
}

func validateRegisterName(c *gin.Context, pluginPackageId, pluginConfigName, pluginConfigRegisterName, pluginConfigId string) (isValid bool, err error) {
	var exists bool
	pluginConfigData := &models.PluginConfigs{}
	// todo!!
	/*
		exists, err = db.MysqlEngine.Context(c).Table(models.TableNamePluginConfigs).
			Where("plugin_package_id = ? AND name = ? AND register_name = ?", pluginPackageId, pluginConfigName, pluginConfigRegisterName).
			Get(pluginConfigData)
	*/
	exists, err = db.MysqlEngine.Context(c).SQL(fmt.Sprintf("select * from %s where plugin_package_id = ? AND name = ? AND register_name = ?", models.TableNamePluginConfigs), pluginPackageId, pluginConfigName, pluginConfigRegisterName).Get(pluginConfigData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if !exists {
		isValid = true
		return
	}

	// record existed
	if pluginConfigData.Id == pluginConfigId {
		isValid = true
		return
	}
	isValid = false
	return
}

func GetDelPluginConfigActionsForImportData(c *gin.Context, pluginPackageId string, pluginConfigDtoForImportList []*models.PluginConfigDto) (resultActions []*db.ExecAction, err error) {
	resultActions = []*db.ExecAction{}

	if pluginPackageId == "" {
		return
	}

	if len(pluginConfigDtoForImportList) == 0 {
		return
	}

	var pluginConfigsExistedList []*models.PluginConfigs
	// todo!!
	/*
		err = db.MysqlEngine.Context(c).Table(models.TableNamePluginConfigs).
			Where("plugin_package_id = ?", pluginPackageId).
			Find(&pluginConfigsExistedList)
	*/
	err = db.MysqlEngine.Context(c).SQL(fmt.Sprintf("select * from %s where plugin_package_id = ?", models.TableNamePluginConfigs), pluginPackageId).Find(&pluginConfigsExistedList)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	pluginCfgIdToDelMap := make(map[string]map[string]string)
	for _, pluginCfgData := range pluginConfigsExistedList {
		if _, isExisted := pluginCfgIdToDelMap[pluginCfgData.Name]; !isExisted {
			pluginCfgIdToDelMap[pluginCfgData.Name] = make(map[string]string)
		}
		pluginCfgIdToDelMap[pluginCfgData.Name][pluginCfgData.RegisterName] = pluginCfgData.Id
	}

	pluginCfgIdToDelList := []string{}
	for _, pluginCfgToImport := range pluginConfigDtoForImportList {
		if _, isOk1 := pluginCfgIdToDelMap[pluginCfgToImport.Name]; isOk1 {
			if pluginCfgId, isOk2 := pluginCfgIdToDelMap[pluginCfgToImport.Name][pluginCfgToImport.RegisterName]; isOk2 {
				pluginCfgIdToDelList = append(pluginCfgIdToDelList, pluginCfgId)
			}
		}
	}

	for _, pluginCfgId := range pluginCfgIdToDelList {
		curDelActions, tmpErr := GetDelPluginConfigActions(c, pluginCfgId)
		if tmpErr != nil {
			err = fmt.Errorf("get del pluginConfig actions for pluginCfgId: %s failed: %s", pluginCfgId, tmpErr.Error())
			return
		}
		resultActions = append(resultActions, curDelActions...)
	}
	return
}

func GetDelPluginConfigActions(c *gin.Context, pluginConfigId string) (resultActions []*db.ExecAction, err error) {
	resultActions = []*db.ExecAction{}

	if pluginConfigId == "" {
		return
	}
	var pluginCfgInterfaceIds []string
	// todo!!
	/*
		err = db.MysqlEngine.Context(c).Table(models.TableNamePluginConfigInterfaces).
			Where("plugin_config_id = ?", pluginConfigId).
			Cols("id").
			Find(&pluginCfgInterfaceIds)
	*/
	err = db.MysqlEngine.Context(c).SQL(fmt.Sprintf("select id from %s where plugin_config_id = ?", models.TableNamePluginConfigInterfaces), pluginConfigId).Find(&pluginCfgInterfaceIds)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	// delete pluginConfigInterfaceParameters
	interfaceParamsActions := []*db.ExecAction{}
	for _, interfaceId := range pluginCfgInterfaceIds {
		action := &db.ExecAction{
			Sql:   db.CombineDBSql("DELETE FROM ", models.TableNamePluginConfigInterfaceParameters, " WHERE plugin_config_interface_id=?"),
			Param: []interface{}{interfaceId},
		}
		interfaceParamsActions = append(interfaceParamsActions, action)
	}
	resultActions = append(interfaceParamsActions, resultActions...)

	// delete pluginConfigInterfaces
	action := &db.ExecAction{
		Sql:   db.CombineDBSql("DELETE FROM ", models.TableNamePluginConfigInterfaces, " WHERE plugin_config_id=?"),
		Param: []interface{}{pluginConfigId},
	}
	resultActions = append(resultActions, action)

	// delete pluginConfig
	action = &db.ExecAction{
		Sql:   db.CombineDBSql("DELETE FROM ", models.TableNamePluginConfigs, " WHERE id=?"),
		Param: []interface{}{pluginConfigId},
	}
	resultActions = append(resultActions, action)

	// delete pluginConfigRoles
	action = &db.ExecAction{
		Sql:   db.CombineDBSql("DELETE FROM ", models.TableNamePluginConfigRoles, " WHERE plugin_cfg_id=?"),
		Param: []interface{}{pluginConfigId},
	}
	resultActions = append(resultActions, action)

	var pluginObjectMetaIds []string
	err = db.MysqlEngine.Context(c).SQL(fmt.Sprintf("select id from %s where config_id = ?", models.TableNamePluginObjectMeta), pluginConfigId).Find(&pluginObjectMetaIds)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	// delete plugin_object_property_meta by pluginConfigId
	for _, objectMetaId := range pluginObjectMetaIds {
		action = &db.ExecAction{
			Sql:   db.CombineDBSql("DELETE FROM ", models.TableNamePluginObjectPropertyMeta, " WHERE object_meta_id=?"),
			Param: []interface{}{objectMetaId},
		}
		resultActions = append(resultActions, action)
	}

	// delete plugin_object_meta by pluginConfigId
	for _, objectMetaId := range pluginObjectMetaIds {
		action = &db.ExecAction{
			Sql:   db.CombineDBSql("DELETE FROM ", models.TableNamePluginObjectMeta, " WHERE id=?"),
			Param: []interface{}{objectMetaId},
		}
		resultActions = append(resultActions, action)
	}
	return
}

func GetCreatePluginConfigActions(c *gin.Context, pluginConfigId string, pluginConfigDto *models.PluginConfigDto, isImportRequest bool, pluginPackageData *models.PluginPackages) (resultActions []*db.ExecAction, err error) {
	resultActions = []*db.ExecAction{}

	pluginConfigDto.Id = pluginConfigId
	if !isImportRequest {
		tmpPackageEntityStr := strings.TrimSuffix(pluginConfigDto.TargetEntityWithFilterRule, pluginConfigDto.FilterRule)
		if tmpPackageEntityStr != "" {
			tmpPackageEntitySlice := strings.Split(tmpPackageEntityStr, ":")
			if len(tmpPackageEntitySlice) > 0 {
				pluginConfigDto.TargetPackage = tmpPackageEntitySlice[0]
			}
			if len(tmpPackageEntitySlice) > 1 {
				pluginConfigDto.TargetEntity = tmpPackageEntitySlice[1]
			}
		}
		pluginConfigDto.TargetEntityFilterRule = pluginConfigDto.FilterRule
	}

	// handle pluginConfig
	pluginConfigData := pluginConfigDto.PluginConfigs
	action, tmpErr := db.GetInsertTableExecAction(models.TableNamePluginConfigs, pluginConfigData, nil)
	if tmpErr != nil {
		err = fmt.Errorf("get insert sql for pluginConfig failed: %s", tmpErr.Error())
		log.Logger.Error(err.Error())
		return
	}
	resultActions = append(resultActions, action)

	now := time.Now()
	reqUser := middleware.GetRequestUser(c)

	// handle pluginConfigInterfaces and pluginConfigInterfaceParameters
	for _, interfaceInfo := range pluginConfigDto.Interfaces {
		interfaceInfo.Id = models.IdPrefixPluCfgItf + guid.CreateGuid()
		interfaceInfo.PluginConfigId = pluginConfigId

		if !isImportRequest {
			serviceName := fmt.Sprintf("%s/%s/%s", pluginPackageData.Name, pluginConfigDto.Name, interfaceInfo.Action)
			if pluginConfigDto.RegisterName != "" {
				serviceName = fmt.Sprintf("%s/%s(%s)/%s", pluginPackageData.Name, pluginConfigDto.Name, pluginConfigDto.RegisterName, interfaceInfo.Action)
			}
			interfaceInfo.ServiceName = serviceName
			interfaceInfo.ServiceDisplayName = serviceName
		}

		action, tmpErr = db.GetInsertTableExecAction(models.TableNamePluginConfigInterfaces, *interfaceInfo, nil)
		if tmpErr != nil {
			err = fmt.Errorf("get insert sql for pluginConfigInterfaces failed: %s", tmpErr.Error())
			log.Logger.Error(err.Error())
			return
		}
		resultActions = append(resultActions, action)

		// handle inputParam
		for _, inputParam := range interfaceInfo.InputParameters {
			inputParam.Id = models.IdPrefixPluCfgItfPar + guid.CreateGuid()
			inputParam.PluginConfigInterfaceId = interfaceInfo.Id

			action, tmpErr = db.GetInsertTableExecAction(models.TableNamePluginConfigInterfaceParameters, *inputParam, nil)
			if tmpErr != nil {
				err = fmt.Errorf("get insert sql for pluginConfigInterfaceParameters failed: %s", tmpErr.Error())
				log.Logger.Error(err.Error())
				return
			}
			resultActions = append(resultActions, action)

			// handle object type
			if inputParam.DataType == models.PluginParamDataTypeObject {
				if inputParam.RefObjectMeta != nil {
					objectActions, tmpErr := getHandleObjectTypeActions(inputParam.RefObjectMeta, pluginConfigId, reqUser, now)
					if tmpErr != nil {
						err = fmt.Errorf("getHandleObjectTypeActions for inputParam: %s failed: %s", inputParam.Name, tmpErr.Error())
						return
					}
					resultActions = append(resultActions, objectActions...)
				}
			}
		}

		// handle outputParam
		for _, outputParam := range interfaceInfo.OutputParameters {
			outputParam.Id = models.IdPrefixPluCfgItfPar + guid.CreateGuid()
			outputParam.PluginConfigInterfaceId = interfaceInfo.Id

			action, tmpErr = db.GetInsertTableExecAction(models.TableNamePluginConfigInterfaceParameters, *outputParam, nil)
			if tmpErr != nil {
				err = fmt.Errorf("get insert sql for pluginConfigInterfaceParameters failed: %s", tmpErr.Error())
				log.Logger.Error(err.Error())
				return
			}
			resultActions = append(resultActions, action)

			// handle object type
			if outputParam.DataType == models.PluginParamDataTypeObject {
				if outputParam.RefObjectMeta != nil {
					objectActions, tmpErr := getHandleObjectTypeActions(outputParam.RefObjectMeta, pluginConfigId, reqUser, now)
					if tmpErr != nil {
						err = fmt.Errorf("getHandleObjectTypeActions for outputParam: %s failed: %s", outputParam.Name, tmpErr.Error())
						return
					}
					resultActions = append(resultActions, objectActions...)
				}
			}
		}
	}

	// handle pluginConfigRoles
	pluginConfigRolesActions, tmpErr := getCreatePluginCfgRolesActions(c, pluginConfigId, pluginConfigDto.PermissionToRole)
	if tmpErr != nil {
		err = fmt.Errorf("get insert sql for pluginConfigRoles failed: %s", tmpErr.Error())
		log.Logger.Error(err.Error())
		return
	}
	resultActions = append(resultActions, pluginConfigRolesActions...)
	return
}

func getHandleObjectTypeActions(objectMeta *models.CoreObjectMeta, pluginConfigId string, reqUser string, now time.Time) (resultActions []*db.ExecAction, err error) {
	objectMeta.Id = models.IdPrefixPluObjMeta + guid.CreateGuid()
	objectMeta.ConfigId = pluginConfigId
	objectMeta.CreatedBy = reqUser
	objectMeta.CreatedTime = now
	objectMeta.UpdatedBy = reqUser
	objectMeta.UpdatedTime = now

	action, tmpErr := db.GetInsertTableExecAction(models.TableNamePluginObjectMeta, *objectMeta, nil)
	if tmpErr != nil {
		err = fmt.Errorf("get insert sql for objectMeta failed: %s", tmpErr.Error())
		log.Logger.Error(err.Error())
		return
	}
	resultActions = append(resultActions, action)

	for _, propertyMeta := range objectMeta.PropertyMetas {
		propertyMeta.Id = models.IdPrefixPluObjPropertyMeta + guid.CreateGuid()
		propertyMeta.ObjectMetaId = objectMeta.Id
		propertyMeta.ConfigId = pluginConfigId
		propertyMeta.CreatedBy = reqUser
		propertyMeta.CreatedTime = now
		propertyMeta.UpdatedBy = reqUser
		propertyMeta.UpdatedTime = now

		action, tmpErr = db.GetInsertTableExecAction(models.TableNamePluginObjectPropertyMeta, *propertyMeta, nil)
		if tmpErr != nil {
			err = fmt.Errorf("get insert sql for inputParam.RefObjectMeta.PropertyMeta failed: %s", tmpErr.Error())
			log.Logger.Error(err.Error())
			return
		}
		resultActions = append(resultActions, action)
	}
	return
}

func getCreatePluginCfgRolesActions(c *gin.Context,
	pluginConfigId string,
	permissionToRole *models.PermissionRoleData) (resultActions []*db.ExecAction, err error) {
	var actions []*db.ExecAction
	now := time.Now()
	reqUser := middleware.GetRequestUser(c)
	userToken := c.GetHeader(models.AuthorizationHeader)
	language := c.GetHeader(middleware.AcceptLanguageHeader)
	respData, err := remote.RetrieveAllLocalRoles("Y", userToken, language, false)
	if err != nil {
		err = fmt.Errorf("retrieve all local roles failed: %s", err.Error())
		return
	}

	roleNameMapId := make(map[string]string)
	if len(respData.Data) > 0 {
		for _, roleDto := range respData.Data {
			roleNameMapId[roleDto.Name] = roleDto.ID
		}
	} else {
		log.Logger.Error("retrieve all local roles empty")
	}

	pluginConfigRolesList := []*models.PluginConfigRoles{}
	mgmtRoleNameMap := make(map[string]struct{})
	for _, roleName := range permissionToRole.MGMT {
		if _, isExisted := mgmtRoleNameMap[roleName]; !isExisted {
			mgmtRoleNameMap[roleName] = struct{}{}
			pluginConfigRolesList = append(pluginConfigRolesList, &models.PluginConfigRoles{
				Id:          models.IdPrefixPluCfgRol + guid.CreateGuid(),
				IsActive:    true,
				PermType:    models.PermissionTypeMGMT,
				PluginCfgId: pluginConfigId,
				RoleId:      roleNameMapId[roleName],
				RoleName:    roleName,
				CreatedBy:   reqUser,
				CreatedTime: now,
				UpdatedTime: now,
			})
		}
	}

	useRoleNameMap := make(map[string]struct{})
	for _, roleName := range permissionToRole.USE {
		if _, isExisted := useRoleNameMap[roleName]; !isExisted {
			useRoleNameMap[roleName] = struct{}{}
			pluginConfigRolesList = append(pluginConfigRolesList, &models.PluginConfigRoles{
				Id:          models.IdPrefixPluCfgRol + guid.CreateGuid(),
				IsActive:    true,
				PermType:    models.PermissionTypeUSE,
				PluginCfgId: pluginConfigId,
				RoleId:      roleNameMapId[roleName],
				RoleName:    roleName,
				CreatedBy:   reqUser,
				CreatedTime: now,
				UpdatedTime: now,
			})
		}
	}

	for i := range pluginConfigRolesList {
		action, tmpErr := db.GetInsertTableExecAction(models.TableNamePluginConfigRoles, *pluginConfigRolesList[i], nil)
		if tmpErr != nil {
			err = fmt.Errorf("get insert sql failed: %s", tmpErr.Error())
			return
		}
		actions = append(actions, action)
	}
	resultActions = actions
	return
}

func DeletePluginConfig(c *gin.Context, pluginConfigId string) (err error) {
	// check whether pluginConfigId is valid
	pluginConfigsData := &models.PluginConfigs{}
	var exists bool
	// todo!!
	/*
		exists, err = db.MysqlEngine.Context(c).Table(models.TableNamePluginConfigs).
			Where("id = ?", pluginConfigId).
			Get(pluginConfigsData)
	*/
	exists, err = db.MysqlEngine.Context(c).SQL(fmt.Sprintf("select * from %s where id=?", models.TableNamePluginConfigs), pluginConfigId).Get(pluginConfigsData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if !exists {
		err = fmt.Errorf("pluginConfigId: %s is invalid", pluginConfigId)
		return
	}

	actions, tmpErr := GetDelPluginConfigActions(c, pluginConfigId)
	if tmpErr != nil {
		err = fmt.Errorf("get delete pluginConfig actions failed: %s", tmpErr.Error())
		return
	}
	err = db.Transaction(actions, c)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	return
}

func GetPluginConfigsWithInterfaces(c *gin.Context, pluginPackageId string, roles []string, pluginConfigId string) (result []*models.PluginConfigQueryObj, err error) {
	result, err = GetPluginConfigs(c, pluginPackageId, roles, pluginConfigId)
	if err != nil {
		return
	}

	// handle interfaces
	err = enrichPluginConfigInterfaces(c, result)
	if err != nil {
		return
	}
	return
}

func GetPluginConfigQueryObjById(c *gin.Context, pluginConfigId string) (result *models.PluginConfigDto, err error) {
	pluginConfigData := &models.PluginConfigs{}
	var exists bool
	// todo!!
	/*
		exists, err = db.MysqlEngine.Context(c).Table(models.TableNamePluginConfigs).
			Where("id = ?", pluginConfigId).
			Get(pluginConfigData)
	*/
	exists, err = db.MysqlEngine.Context(c).SQL(fmt.Sprintf("select * from %s where id=?", models.TableNamePluginConfigs), pluginConfigId).Get(pluginConfigData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if !exists {
		err = fmt.Errorf("pluginConfigId: %s is invalid", pluginConfigId)
		return
	}

	result = &models.PluginConfigDto{
		PluginConfigs: *pluginConfigData,
		FilterRule:    pluginConfigData.TargetEntityFilterRule,
	}

	tmpPluginConfigQueryObj := &models.PluginConfigQueryObj{
		PluginConfigDtoList: []*models.PluginConfigDto{result},
	}
	// handle interfaces
	err = enrichPluginConfigInterfaces(c, []*models.PluginConfigQueryObj{tmpPluginConfigQueryObj})
	if err != nil {
		return
	}
	return
}

func enrichPluginConfigInterfaces(c *gin.Context, pluginConfigQueryObjList []*models.PluginConfigQueryObj) (err error) {
	for _, pluginConfigQueryObj := range pluginConfigQueryObjList {
		for _, pluginConfigDto := range pluginConfigQueryObj.PluginConfigDtoList {
			pluginInterfaceQueryObjList, tmpErr := GetConfigInterfaces(c, pluginConfigDto.Id)
			if tmpErr != nil {
				err = tmpErr
				return
			}
			pluginCfgInterfaces := make([]*models.PluginConfigInterfaces, 0, len(pluginInterfaceQueryObjList))
			for _, interfaceQueryObj := range pluginInterfaceQueryObjList {
				curInterfaces := interfaceQueryObj.PluginConfigInterfaces
				curInterfaces.InputParameters = interfaceQueryObj.InputParameters
				curInterfaces.OutputParameters = interfaceQueryObj.OutputParameters
				pluginCfgInterfaces = append(pluginCfgInterfaces, &curInterfaces)
			}
			pluginConfigDto.Interfaces = pluginCfgInterfaces
		}
	}
	return
}

func ImportPluginConfigs(c *gin.Context, pluginPackageId string, packagePluginsXmlData *models.PackagePluginsXML) (result *models.PluginConfigDto, err error) {
	// validate pluginPackageId
	pluginPackageData := &models.PluginPackages{}
	var exists bool
	// todo!!
	/*
		exists, err = db.MysqlEngine.Context(c).Table(models.TableNamePluginPackages).
			Where("id = ?", pluginPackageId).
			Get(pluginPackageData)
	*/
	exists, err = db.MysqlEngine.Context(c).SQL(fmt.Sprintf("select * from %s where id=?", models.TableNamePluginPackages), pluginPackageId).Get(pluginPackageData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if !exists {
		err = fmt.Errorf("pluginPackageId: %s is invalid", pluginPackageId)
		return
	}

	// get save plugin config list
	savePluginConfigList := getImportPluginConfigData(pluginPackageId, packagePluginsXmlData)
	var actions []*db.ExecAction

	// handle del actions for import data
	delActions, tmpErr := GetDelPluginConfigActionsForImportData(c, pluginPackageId, savePluginConfigList)
	if tmpErr != nil {
		err = fmt.Errorf("get del pluginConfig actions for import data failed: %s", tmpErr.Error())
		return
	}
	actions = append(actions, delActions...)

	// handle creation actions for import data
	for i := range savePluginConfigList {
		curPluginConfigId := models.IdPrefixPluCfg + guid.CreateGuid()
		curCreationActions, tmpErr := GetCreatePluginConfigActions(c, curPluginConfigId, savePluginConfigList[i], true, pluginPackageData)
		if tmpErr != nil {
			err = fmt.Errorf("get create pluginConfig actions failed: %s", tmpErr.Error())
			return
		}
		actions = append(actions, curCreationActions...)
	}

	// handle system parameters
	systemVariablesList := getImportSystemVariablesData(packagePluginsXmlData)
	updateSystemVarActions, buildActionErr := getPluginSystemVariableUpdateActions(systemVariablesList, pluginPackageData.Name, pluginPackageData.Version)
	if buildActionErr != nil {
		err = buildActionErr
		return
	}
	actions = append(actions, updateSystemVarActions...)

	err = db.Transaction(actions, c)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	return
}

func getPluginSystemVariableUpdateActions(systemVariablesList []*models.SystemVariables, packageName, packageVersion string) (actions []*db.ExecAction, err error) {
	systemVarDelActions := []*db.ExecAction{}
	systemVarCreationActions := []*db.ExecAction{}
	inactiveOtherVarActions := []*db.ExecAction{}
	for i, sysVar := range systemVariablesList {
		sysVar.Source = fmt.Sprintf("%s__%s", packageName, packageVersion)
		curDelAction := &db.ExecAction{
			Sql:   db.CombineDBSql("DELETE FROM ", models.TableNameSystemVariables, " WHERE name=? AND source=?"),
			Param: []interface{}{sysVar.Name, sysVar.Source},
		}
		systemVarDelActions = append(systemVarDelActions, curDelAction)
		inactiveOtherVarActions = append(inactiveOtherVarActions, &db.ExecAction{Sql: "update system_variables set status='inactive' where name=? and status='active'", Param: []interface{}{sysVar.Name}})
		sysVar.Status = "active"
		sysVar.Id = "sys_var_" + guid.CreateGuid()
		curCreationAction, tmpErr := db.GetInsertTableExecAction(models.TableNameSystemVariables, *systemVariablesList[i], nil)
		if tmpErr != nil {
			err = fmt.Errorf("get create system variables actions failed: %s", tmpErr.Error())
			return
		}
		systemVarCreationActions = append(systemVarCreationActions, curCreationAction)
	}
	actions = append(actions, systemVarDelActions...)
	actions = append(actions, inactiveOtherVarActions...)
	actions = append(actions, systemVarCreationActions...)
	return
}

func getImportSystemVariablesData(packagePluginsXmlData *models.PackagePluginsXML) (result []*models.SystemVariables) {
	result = []*models.SystemVariables{}
	for i := range packagePluginsXmlData.SystemParameters.SystemParameter {
		sysParamInfo := packagePluginsXmlData.SystemParameters.SystemParameter[i]
		systemVar := &models.SystemVariables{
			Id:           models.IdPrefixSysVar + guid.CreateGuid(),
			PackageName:  sysParamInfo.PackageName,
			Name:         sysParamInfo.Name,
			Value:        sysParamInfo.Value,
			DefaultValue: sysParamInfo.DefaultValue,
			Scope:        sysParamInfo.ScopeType,
			Source:       sysParamInfo.Source,
			Status:       sysParamInfo.Status,
		}
		result = append(result, systemVar)
	}
	return
}

func handleImportObjectParameter(paramObjectNameMapInfo map[string]*models.ParamObjectXML, refObjectName string) (resultObjectMeta *models.CoreObjectMeta) {
	if _, isExisted := paramObjectNameMapInfo[refObjectName]; !isExisted {
		return
	}

	paramObjectXML := paramObjectNameMapInfo[refObjectName]
	resultObjectMeta = &models.CoreObjectMeta{
		Name:         paramObjectXML.Name,
		PackageName:  paramObjectXML.PackageName,
		Source:       paramObjectXML.Source,
		LatestSource: paramObjectXML.LatestSource,
	}

	if len(paramObjectXML.Property) > 0 {
		propertyList := make([]*models.CoreObjectPropertyMeta, 0, len(paramObjectXML.Property))
		for _, property := range paramObjectXML.Property {
			curProperty := &models.CoreObjectPropertyMeta{
				Name:          property.Name,
				DataType:      property.DataType,
				Multiple:      property.Multiple,
				MappingType:   property.MapType,
				SensitiveData: property.SensitiveData,
				MapExpr:       property.MapExpr,
				ObjectName:    resultObjectMeta.Name,
				PackageName:   resultObjectMeta.PackageName,
			}
			curProperty.Sensitive = false
			if curProperty.SensitiveData == "Y" {
				curProperty.Sensitive = true
			}
			propertyList = append(propertyList, curProperty)
		}
		resultObjectMeta.PropertyMetas = propertyList
	}
	return
}

func getImportPluginConfigData(pluginPackageId string, packagePluginsXmlData *models.PackagePluginsXML) (result []*models.PluginConfigDto) {
	savePluginConfigList := []*models.PluginConfigDto{}
	for i := range packagePluginsXmlData.Plugins.Plugin {
		pluginInfo := packagePluginsXmlData.Plugins.Plugin[i]
		pluginCfg := &models.PluginConfigs{
			PluginPackageId:        pluginPackageId,
			Name:                   pluginInfo.Name,
			TargetPackage:          pluginInfo.TargetPackage,
			TargetEntity:           pluginInfo.TargetEntity,
			TargetEntityFilterRule: pluginInfo.TargetEntityFilterRule,
			RegisterName:           pluginInfo.RegisterName,
			Status:                 pluginInfo.Status,
		}

		// handle paramObject
		paramObjectNameMapInfo := make(map[string]*models.ParamObjectXML)
		if len(pluginInfo.ParamObject) > 0 {
			for _, paramObj := range pluginInfo.ParamObject {
				paramObjectNameMapInfo[paramObj.Name] = paramObj
			}
		}

		// handle interfaces
		pluginCfgInterfaceList := []*models.PluginConfigInterfaces{}
		for ii := range pluginInfo.Interface {
			interfaceInfo := pluginInfo.Interface[ii]

			serviceName := fmt.Sprintf("%s/%s/%s", packagePluginsXmlData.Name, pluginInfo.Name, interfaceInfo.Action)
			if pluginInfo.RegisterName != "" {
				serviceName = fmt.Sprintf("%s/%s(%s)/%s", packagePluginsXmlData.Name, pluginInfo.Name, pluginInfo.RegisterName, interfaceInfo.Action)
			}

			pluginCfgInterface := &models.PluginConfigInterfaces{
				Action:             interfaceInfo.Action,
				ServiceName:        serviceName,
				ServiceDisplayName: serviceName,
				Path:               interfaceInfo.Path,
				HttpMethod:         interfaceInfo.HttpMethod,
				IsAsyncProcessing:  interfaceInfo.IsAsyncProcessing,
				Type:               interfaceInfo.Type,
				FilterRule:         interfaceInfo.FilterRule,
				Description:        interfaceInfo.Description,
			}

			// handle interfaces parameters
			// handle input params
			inputParamList := []*models.PluginConfigInterfaceParameters{}
			for iii := range interfaceInfo.InputParameters.Parameter {
				inputParamInfo := &interfaceInfo.InputParameters.Parameter[iii]
				inputParam := &models.PluginConfigInterfaceParameters{
					Type:                      models.PluginParamTypeInput,
					Name:                      inputParamInfo.Text,
					DataType:                  inputParamInfo.Datatype,
					MappingType:               inputParamInfo.MappingType,
					MappingEntityExpression:   inputParamInfo.MappingEntityExpression,
					Required:                  inputParamInfo.Required,
					SensitiveData:             inputParamInfo.SensitiveData,
					MappingSystemVariableName: inputParamInfo.MappingSystemVariableName,
					Description:               inputParamInfo.Description,
					MappingVal:                inputParamInfo.MappingValue,
					Multiple:                  inputParamInfo.Multiple,
					RefObjectName:             inputParamInfo.RefObjectName,
				}
				inputParamList = append(inputParamList, inputParam)

				// handle object parameter
				if inputParam.DataType == models.PluginParamDataTypeObject {
					inputParam.RefObjectMeta = handleImportObjectParameter(paramObjectNameMapInfo, inputParamInfo.RefObjectName)
				}
			}
			pluginCfgInterface.InputParameters = inputParamList

			// handle output params
			outputParamList := []*models.PluginConfigInterfaceParameters{}
			for iii := range interfaceInfo.OutputParameters.Parameter {
				outputParamInfo := &interfaceInfo.OutputParameters.Parameter[iii]
				outputParam := &models.PluginConfigInterfaceParameters{
					Type:                      models.PluginParamTypeOutput,
					Name:                      outputParamInfo.Text,
					DataType:                  outputParamInfo.Datatype,
					MappingType:               outputParamInfo.MappingType,
					MappingEntityExpression:   outputParamInfo.MappingEntityExpression,
					Required:                  outputParamInfo.Required,
					SensitiveData:             outputParamInfo.SensitiveData,
					MappingSystemVariableName: outputParamInfo.MappingSystemVariableName,
					Description:               outputParamInfo.Description,
					MappingVal:                outputParamInfo.MappingValue,
					Multiple:                  outputParamInfo.Multiple,
					RefObjectName:             outputParamInfo.RefObjectName,
				}
				outputParamList = append(outputParamList, outputParam)

				// handle object parameter
				if outputParam.DataType == models.PluginParamDataTypeObject {
					outputParam.RefObjectMeta = handleImportObjectParameter(paramObjectNameMapInfo, outputParam.RefObjectName)
				}
			}
			pluginCfgInterface.OutputParameters = outputParamList

			pluginCfgInterfaceList = append(pluginCfgInterfaceList, pluginCfgInterface)
		}
		pluginCfg.Interfaces = pluginCfgInterfaceList

		// handle permission roles
		pluginCfgPermissionRoleData := &models.PermissionRoleData{MGMT: []string{}, USE: []string{}}
		roleBindsList := pluginInfo.RoleBinds.RoleBind
		MGMTRoleNameMap := make(map[string]struct{})
		USERoleNameMap := make(map[string]struct{})
		for j := range roleBindsList {
			roleInfo := &roleBindsList[j]
			if roleInfo.Permission == models.PermissionTypeMGMT {
				if _, isExisted := MGMTRoleNameMap[roleInfo.RoleName]; !isExisted {
					MGMTRoleNameMap[roleInfo.RoleName] = struct{}{}
					pluginCfgPermissionRoleData.MGMT = append(pluginCfgPermissionRoleData.MGMT, roleInfo.RoleName)
				}
			} else if roleInfo.Permission == models.PermissionTypeUSE {
				if _, isExisted := USERoleNameMap[roleInfo.RoleName]; !isExisted {
					USERoleNameMap[roleInfo.RoleName] = struct{}{}
					pluginCfgPermissionRoleData.USE = append(pluginCfgPermissionRoleData.USE, roleInfo.RoleName)
				}
			}
		}

		savePluginCfgData := &models.PluginConfigDto{
			PluginConfigs:    *pluginCfg,
			PermissionToRole: pluginCfgPermissionRoleData,
		}
		savePluginConfigList = append(savePluginConfigList, savePluginCfgData)
	}
	result = savePluginConfigList
	return
}

func handleExportObjectParameter(c *gin.Context, pluginConfigId string) (result []*models.ParamObjectXML, err error) {
	var pluginObjectMetaList []*models.CoreObjectMeta
	err = db.MysqlEngine.Context(c).SQL(fmt.Sprintf("select * from %s where config_id = ?", models.TableNamePluginObjectMeta), pluginConfigId).Find(&pluginObjectMetaList)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	if len(pluginObjectMetaList) > 0 {
		var pluginObjectPropertyMetaList []*models.CoreObjectPropertyMeta
		err = db.MysqlEngine.Context(c).SQL(fmt.Sprintf("select * from %s where config_id = ?", models.TableNamePluginObjectPropertyMeta), pluginConfigId).Find(&pluginObjectPropertyMetaList)
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}

		objectMetaIdMapPropertyData := make(map[string][]*models.CoreObjectPropertyMeta)
		if len(pluginObjectPropertyMetaList) > 0 {
			for _, propertyData := range pluginObjectPropertyMetaList {
				objectMetaIdMapPropertyData[propertyData.ObjectMetaId] = append(objectMetaIdMapPropertyData[propertyData.ObjectMetaId], propertyData)
			}
		}

		for _, objectMetaData := range pluginObjectMetaList {
			curParamObjectXML := &models.ParamObjectXML{
				Name:         objectMetaData.Name,
				PackageName:  objectMetaData.PackageName,
				Source:       objectMetaData.Source,
				LatestSource: objectMetaData.LatestSource,
			}

			if len(objectMetaIdMapPropertyData[objectMetaData.Id]) > 0 {
				for _, propertyData := range objectMetaIdMapPropertyData[objectMetaData.Id] {
					sensitiveData := "N"
					if propertyData.Sensitive {
						sensitiveData = "Y"
					}
					curPropertyXML := &models.PropertyXML{
						Name:          propertyData.Name,
						DataType:      propertyData.DataType,
						Multiple:      propertyData.Multiple,
						MapType:       propertyData.MappingType,
						SensitiveData: sensitiveData,
						MapExpr:       propertyData.MapExpr,
					}

					curParamObjectXML.Property = append(curParamObjectXML.Property, curPropertyXML)
				}
			}

			result = append(result, curParamObjectXML)
		}
	}
	return
}

func ExportPluginConfigs(c *gin.Context, pluginPackageId string, exportConfigList []*models.PluginConfigsBatchEnable) (result *models.PackagePluginsXML, err error) {
	// validate pluginPackageId
	pluginPackageData := &models.PluginPackages{}
	var exists bool
	// todo!!
	/*
		exists, err = db.MysqlEngine.Context(c).Table(models.TableNamePluginPackages).
			Where("id = ?", pluginPackageId).
			Get(pluginPackageData)
	*/
	exists, err = db.MysqlEngine.Context(c).SQL(fmt.Sprintf("select * from %s where id=?", models.TableNamePluginPackages), pluginPackageId).Get(pluginPackageData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if !exists {
		err = fmt.Errorf("pluginPackageId: %s is invalid", pluginPackageId)
		return
	}

	result = &models.PackagePluginsXML{
		Name:    pluginPackageData.Name,
		Version: pluginPackageData.Version,
	}
	ignorePluginConfigMap := make(map[string]int)
	for _, configNameObj := range exportConfigList {
		for _, configObj := range configNameObj.PluginConfigs {
			if !configObj.Checked {
				ignorePluginConfigMap[configObj.Id] = 1
			}
		}
	}
	// query pluginConfigs by pluginPackageId
	pluginConfigQueryObjList, err := GetPluginConfigsWithInterfaces(c, pluginPackageId, middleware.GetRequestRoles(c), "")
	if err != nil {
		return
	}

	pluginXMLList := []models.PluginXML{}
	for _, pluginCfgObj := range pluginConfigQueryObjList {
		for _, pluginCfgDto := range pluginCfgObj.PluginConfigDtoList {
			// 忽略根节点
			if pluginCfgDto.TargetPackage == "" && pluginCfgDto.TargetEntity == "" &&
				pluginCfgDto.TargetEntityFilterRule == "" && pluginCfgDto.RegisterName == "" {
				continue
			}
			// 导出选择过滤
			if _, ignoreOk := ignorePluginConfigMap[pluginCfgDto.Id]; ignoreOk {
				continue
			}
			// handle pluginConfig
			pluginXMLData := models.PluginXML{
				Name:                   pluginCfgDto.Name,
				TargetPackage:          pluginCfgDto.TargetPackage,
				TargetEntity:           pluginCfgDto.TargetEntity,
				TargetEntityFilterRule: pluginCfgDto.TargetEntityFilterRule,
				RegisterName:           pluginCfgDto.RegisterName,
				Status:                 pluginCfgDto.Status,
			}

			// handle paramObject
			paramObjectXML, tmpErr := handleExportObjectParameter(c, pluginCfgDto.Id)
			if tmpErr != nil {
				err = fmt.Errorf("handleExportObjectParameter for pluginConfigId: %s failed: %s", pluginCfgDto.Id, tmpErr.Error())
				return
			}
			pluginXMLData.ParamObject = paramObjectXML

			// handle interfaces
			interfaceXMLList := []models.InterfaceXML{}
			for _, interfaceInfo := range pluginCfgDto.Interfaces {
				interfaceXMLData := models.InterfaceXML{
					Action:            interfaceInfo.Action,
					Path:              interfaceInfo.Path,
					HttpMethod:        interfaceInfo.HttpMethod,
					IsAsyncProcessing: interfaceInfo.IsAsyncProcessing,
					Type:              interfaceInfo.Type,
					FilterRule:        interfaceInfo.FilterRule,
					Description:       interfaceInfo.Description,
				}

				// handle input parameters
				inputParamsXMLList := []models.ParameterXML{}
				for _, parameter := range interfaceInfo.InputParameters {
					parameterXMLData := models.ParameterXML{
						Text:                      parameter.Name,
						Datatype:                  parameter.DataType,
						MappingType:               parameter.MappingType,
						MappingEntityExpression:   parameter.MappingEntityExpression,
						MappingSystemVariableName: parameter.MappingSystemVariableName,
						Required:                  parameter.Required,
						SensitiveData:             parameter.SensitiveData,
						Description:               parameter.Description,
						MappingValue:              parameter.MappingVal,
						Multiple:                  parameter.Multiple,
						RefObjectName:             parameter.RefObjectName,
					}
					inputParamsXMLList = append(inputParamsXMLList, parameterXMLData)
				}
				interfaceXMLData.InputParameters.Parameter = inputParamsXMLList

				// handle output parameters
				outputParamsXMLList := []models.ParameterXML{}
				for _, parameter := range interfaceInfo.OutputParameters {
					parameterXMLData := models.ParameterXML{
						Text:                      parameter.Name,
						Datatype:                  parameter.DataType,
						MappingType:               parameter.MappingType,
						MappingEntityExpression:   parameter.MappingEntityExpression,
						MappingSystemVariableName: parameter.MappingSystemVariableName,
						Required:                  parameter.Required,
						SensitiveData:             parameter.SensitiveData,
						Description:               parameter.Description,
						MappingValue:              parameter.MappingVal,
						Multiple:                  parameter.Multiple,
						RefObjectName:             parameter.RefObjectName,
					}
					outputParamsXMLList = append(outputParamsXMLList, parameterXMLData)
				}
				interfaceXMLData.OutputParameters.Parameter = outputParamsXMLList

				interfaceXMLList = append(interfaceXMLList, interfaceXMLData)
			}
			pluginXMLData.Interface = interfaceXMLList

			// handle permission roles
			roleBindXMLList := []models.RoleBindXML{}
			if pluginCfgDto.PermissionToRole != nil {
				for _, roleName := range pluginCfgDto.PermissionToRole.MGMT {
					roleBindXMLData := models.RoleBindXML{
						Permission: models.PermissionTypeMGMT,
						RoleName:   roleName,
					}
					roleBindXMLList = append(roleBindXMLList, roleBindXMLData)
				}
				for _, roleName := range pluginCfgDto.PermissionToRole.USE {
					roleBindXMLData := models.RoleBindXML{
						Permission: models.PermissionTypeUSE,
						RoleName:   roleName,
					}
					roleBindXMLList = append(roleBindXMLList, roleBindXMLData)
				}
			}
			pluginXMLData.RoleBinds.RoleBind = roleBindXMLList

			pluginXMLList = append(pluginXMLList, pluginXMLData)
		}
	}
	result.Plugins.Plugin = pluginXMLList

	// handle system parameters
	packageSource := fmt.Sprintf("%s__%s", pluginPackageData.Name, pluginPackageData.Version)
	systemVarsList := []*models.SystemVariables{}
	err = db.MysqlEngine.Context(c).SQL("SELECT * FROM system_variables WHERE source=?", packageSource).Find(&systemVarsList)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
	}

	systemParamsXMLList := []models.SystemParameterXML{}
	for _, sysParameter := range systemVarsList {
		systemParXMLData := models.SystemParameterXML{
			Name:         sysParameter.Name,
			ScopeType:    sysParameter.Scope,
			DefaultValue: sysParameter.DefaultValue,
			Value:        sysParameter.Value,
			Status:       sysParameter.Status,
			Source:       sysParameter.Source,
			PackageName:  sysParameter.PackageName,
		}
		systemParamsXMLList = append(systemParamsXMLList, systemParXMLData)
	}
	result.SystemParameters.SystemParameter = systemParamsXMLList
	return
}

func GetPluginObjectMetaById(ctx context.Context, objectMetaId string) (result *models.CoreObjectMeta, err error) {
	var objectMetaList []*models.CoreObjectMeta
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_object_meta where id=?", objectMetaId).Find(&objectMetaList)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(objectMetaList) > 0 {
		result = objectMetaList[0]
	}
	return
}

func GetObjectMetas(ctx context.Context, objectMetaId string) (objectMetaEntity *models.CoreObjectMeta, err error) {
	objectMetaEntity, err = GetPluginObjectMetaById(ctx, objectMetaId)
	if err != nil {
		err = fmt.Errorf("get plugin object meta by id: %s failed: %s", objectMetaId, err.Error())
		return
	}
	if objectMetaEntity == nil {
		err = fmt.Errorf("get plugin object meta by id: %s empty", objectMetaId)
		return
	}
	objectMetaEntity.MappingEntityExpression = objectMetaEntity.MapExpr

	propertyMetaEntities, err := getPluginObjectPropertyMetaListByObjectMeta(ctx, objectMetaEntity.Id)
	if err != nil {
		err = fmt.Errorf("get plugin object property meta list by object meta id: %s failed: %s", objectMetaEntity.Id, err.Error())
		return
	}
	if len(propertyMetaEntities) == 0 {
		return
	}

	for _, propertyMetaEntity := range propertyMetaEntities {
		if propertyMetaEntity == nil {
			continue
		}
		propertyMetaEntity.SensitiveData = "N"
		if propertyMetaEntity.Sensitive {
			propertyMetaEntity.SensitiveData = "Y"
		}
		propertyMetaEntity.MappingEntityExpression = propertyMetaEntity.MapExpr
		if propertyMetaEntity.DataType == models.PluginParamDataTypeObject {
			refObjectMetaEntity := QueryCoreObjectMeta(ctx, propertyMetaEntity.PackageName, propertyMetaEntity.RefObjectName, propertyMetaEntity.ConfigId)
			propertyMetaEntity.RefObjectMeta = refObjectMetaEntity
		}
	}
	objectMetaEntity.PropertyMetas = propertyMetaEntities
	return
}

func UpdateObjectMetas(c *gin.Context, reqParam *models.CoreObjectMeta) (err error) {
	now := time.Now()
	reqUser := middleware.GetRequestUser(c)
	var actions []*db.ExecAction
	action := &db.ExecAction{
		Sql: db.CombineDBSql("update ", models.TableNamePluginObjectMeta,
			" set `name`=?,`package_name`=?,`source`=?,`latest_source`=?,`updated_by`=?,`updated_time`=?,`config_id`=?,`map_expr`=? where `id`=?"),
		Param: []interface{}{reqParam.Name, reqParam.PackageName, reqParam.Source, reqParam.LatestSource, reqUser, now, reqParam.ConfigId, reqParam.MappingEntityExpression, reqParam.Id},
	}
	actions = append(actions, action)

	for _, propertyMetaInfo := range reqParam.PropertyMetas {
		isSensitive := false
		if propertyMetaInfo.SensitiveData == "Y" {
			isSensitive = true
		}
		action := &db.ExecAction{
			Sql: db.CombineDBSql("update ", models.TableNamePluginObjectPropertyMeta,
				" set `name`=?,`data_type`=?,`map_type`=?,`map_expr`=?,`source`=?,`updated_by`=?,`updated_time`=?,`is_sensitive`=?,`multiple`=? where `id`=?"),
			Param: []interface{}{propertyMetaInfo.Name, propertyMetaInfo.DataType, propertyMetaInfo.MappingType, propertyMetaInfo.MappingEntityExpression, propertyMetaInfo.Source, reqUser, now, isSensitive, propertyMetaInfo.Multiple, propertyMetaInfo.Id},
		}
		actions = append(actions, action)
	}

	err = db.Transaction(actions, c)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	return
}

func getPluginConfigDeleteActions(pluginPackageId string) (actions []*db.ExecAction) {
	actions = append(actions, &db.ExecAction{Sql: "delete from plugin_config_roles where plugin_cfg_id in (select id from plugin_configs where plugin_package_id=? and register_name!='')", Param: []interface{}{pluginPackageId}})
	actions = append(actions, &db.ExecAction{Sql: "delete from plugin_config_interface_parameters where plugin_config_interface_id in (select id from plugin_config_interfaces where plugin_config_id in (select id from plugin_configs where plugin_package_id=? and register_name!=''))", Param: []interface{}{pluginPackageId}})
	actions = append(actions, &db.ExecAction{Sql: "delete from plugin_config_interfaces where plugin_config_id in (select id from plugin_configs where plugin_package_id=? and register_name!='')", Param: []interface{}{pluginPackageId}})
	actions = append(actions, &db.ExecAction{Sql: "delete from plugin_configs where plugin_package_id=? and register_name!=''", Param: []interface{}{pluginPackageId}})
	return
}
