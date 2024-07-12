package database

import (
	"context"
	"encoding/json"
	"fmt"
	"strconv"
	"strings"
	"time"

	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/encrypt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/tools"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

func GetPackages(ctx context.Context, allFlag bool) (result []*models.PluginPackages, err error) {
	result = []*models.PluginPackages{}
	if allFlag {
		err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_packages order by name,`version`").Find(&result)
	} else {
		err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_packages where status in ('UNREGISTERED','REGISTERED') order by name,`version`").Find(&result)
	}
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func QueryPluginPackages(ctx context.Context, param *models.PluginPackageQueryParam) (result []*models.PluginPackageQueryObj, err error) {
	var packageRows []*models.PluginPackages
	var filterSql []string
	var filterParam []interface{}
	if param.Id != "" {
		filterSql = append(filterSql, "id=?")
		filterParam = append(filterParam, param.Id)
	}
	if param.UpdatedBy != "" {
		filterSql = append(filterSql, "updated_by like ?")
		filterParam = append(filterParam, fmt.Sprintf("%%%s%%", param.UpdatedBy))
	}
	if param.Name != "" {
		filterSql = append(filterSql, "name like ?")
		filterParam = append(filterParam, fmt.Sprintf("%%%s%%", param.Name))
	}
	if !param.WithDelete {
		filterSql = append(filterSql, "status in ('UNREGISTERED','REGISTERED')")
	}
	if param.WithRunningInstance == "yes" {
		filterSql = append(filterSql, "id in (select package_id from plugin_instances)")
	} else if param.WithRunningInstance == "no" {
		filterSql = append(filterSql, "id not in (select package_id from plugin_instances)")
	}
	var queryFilterSql string
	if len(filterSql) > 0 {
		queryFilterSql = "where " + strings.Join(filterSql, " and ")
	}
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_packages "+queryFilterSql+" order by name,`version`", filterParam...).Find(&packageRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(packageRows) == 0 {
		return
	}
	var packageIdList []string
	for _, row := range packageRows {
		packageIdList = append(packageIdList, row.Id)
	}
	idListFilter, idListParam := db.CreateListParams(packageIdList, "")
	var packageMenuRows []*models.PluginPackageMenus
	err = db.MysqlEngine.Context(ctx).SQL("select plugin_package_id,code,display_name,local_display_name from plugin_package_menus where plugin_package_id in ("+idListFilter+") order by code", idListParam...).Find(&packageMenuRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	var instanceRows []*models.PluginInstances
	err = db.MysqlEngine.Context(ctx).SQL("select id,host,port,package_id from plugin_instances").Find(&instanceRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	menuMap := make(map[string][]string)
	instanceMap := make(map[string][]*models.PluginPackageInstanceObj)
	for _, row := range packageMenuRows {
		if existMenuList, ok := menuMap[row.PluginPackageId]; ok {
			menuMap[row.PluginPackageId] = append(existMenuList, row.LocalDisplayName)
		} else {
			menuMap[row.PluginPackageId] = []string{row.LocalDisplayName}
		}
	}
	for _, row := range instanceRows {
		address := fmt.Sprintf("%s:%d", row.Host, row.Port)
		if existInstanceList, ok := instanceMap[row.PackageId]; ok {
			instanceMap[row.PackageId] = append(existInstanceList, &models.PluginPackageInstanceObj{Id: row.Id, Address: address})
		} else {
			instanceMap[row.PackageId] = []*models.PluginPackageInstanceObj{{Id: row.Id, Address: address}}
		}
	}
	for _, row := range packageRows {
		row.UpdatedTimeString = row.UpdatedTime.Format(models.DateTimeFormat)
		resultObj := models.PluginPackageQueryObj{PluginPackages: *row, Menus: []string{}, Instances: []*models.PluginPackageInstanceObj{}}
		if menuList, ok := menuMap[row.Id]; ok {
			resultObj.Menus = menuList
		}
		if instanceList, ok := instanceMap[row.Id]; ok {
			resultObj.Instances = instanceList
		}
		result = append(result, &resultObj)
	}
	return
}

func GetPluginDependencies(ctx context.Context, pluginPackageId string) (result *models.PluginPackageDepObj, err error) {
	var pluginPackageRows []*models.PluginPackages
	err = db.MysqlEngine.Context(ctx).SQL("select name,`version` from plugin_packages where id=?", pluginPackageId).Find(&pluginPackageRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(pluginPackageRows) == 0 {
		err = exterror.Catch(exterror.New().DatabaseQueryEmptyError, err)
		return
	}
	result = &models.PluginPackageDepObj{PackageName: pluginPackageRows[0].Name, Version: pluginPackageRows[0].Version, Dependencies: []*models.PluginPackageDepObj{}}
	var dependRows []*models.PluginPackageDependencies
	err = db.MysqlEngine.Context(ctx).SQL("select dependency_package_name,dependency_package_version from plugin_package_dependencies where plugin_package_id=?", pluginPackageId).Find(&dependRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	for _, v := range dependRows {
		result.Dependencies = append(result.Dependencies, &models.PluginPackageDepObj{PackageName: v.DependencyPackageName, Version: v.DependencyPackageVersion, Dependencies: []*models.PluginPackageDepObj{}})
	}
	return
}

func GetPluginMenus(ctx context.Context, pluginPackageId string) (result []*models.PluginPackageMenus, err error) {
	var menuItemRows []*models.MenuItems
	err = db.MysqlEngine.SQL("select id, parent_code, code, `source`, description, local_display_name, menu_order from menu_items order by menu_order").Find(&menuItemRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	for _, row := range menuItemRows {
		result = append(result, &models.PluginPackageMenus{
			Active:           true,
			Category:         row.ParentCode,
			Code:             row.Code,
			DisplayName:      row.LocalDisplayName,
			Id:               row.Id,
			LocalDisplayName: row.LocalDisplayName,
			MenuOrder:        row.MenuOrder,
			Source:           row.Source,
		})
	}
	var pluginMenus []*models.PluginPackageMenus
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_package_menus where plugin_package_id=?", pluginPackageId).Find(&pluginMenus)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	for _, v := range pluginMenus {
		v.Source = v.PluginPackageId
		result = append(result, v)
	}
	return
}

func GetPluginSystemParameters(ctx context.Context, pluginPackageId string) (result []*models.SystemVariables, err error) {
	pluginPackageObj, tmpErr := getPluginPackageObj(ctx, pluginPackageId)
	if tmpErr != nil {
		err = tmpErr
		return
	}
	packageName := fmt.Sprintf("%s__%s", pluginPackageObj.Name, pluginPackageObj.Version)
	result = []*models.SystemVariables{}
	err = db.MysqlEngine.Context(ctx).SQL("select * from system_variables where source=?", packageName).Find(&result)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
	}
	return
}

func getPluginPackageObj(ctx context.Context, pluginPackageId string) (pluginPackageObj *models.PluginPackages, err error) {
	var pluginPackageRows []*models.PluginPackages
	err = db.MysqlEngine.Context(ctx).SQL("select id,name,`version`,status,ui_package_included from plugin_packages where id=?", pluginPackageId).Find(&pluginPackageRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
	} else {
		if len(pluginPackageRows) == 0 {
			err = fmt.Errorf("can not find pluginPackages with id:%s ", pluginPackageId)
		} else {
			pluginPackageObj = pluginPackageRows[0]
		}
	}
	return
}

func GetPluginAuthorities(ctx context.Context, pluginPackageId string) (result []*models.PluginPackageAuthorities, err error) {
	result = []*models.PluginPackageAuthorities{}
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_package_authorities where plugin_package_id=?", pluginPackageId).Find(&result)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
	}
	return
}

func GetPluginRuntimeResources(ctx context.Context, pluginPackageId string) (result *models.PluginRuntimeResourceData, err error) {
	result = &models.PluginRuntimeResourceData{Docker: []*models.PluginPackageRuntimeResourcesDocker{}, Mysql: []*models.PluginPackageRuntimeResourcesMysql{}, S3: []*models.PluginPackageRuntimeResourcesS3{}}
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_package_runtime_resources_docker where plugin_package_id=?", pluginPackageId).Find(&result.Docker)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_package_runtime_resources_mysql where plugin_package_id=?", pluginPackageId).Find(&result.Mysql)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_package_runtime_resources_s3 where plugin_package_id=?", pluginPackageId).Find(&result.S3)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func UploadPackage(ctx context.Context, registerConfig *models.RegisterXML, withUi, enterprise bool, pluginPackageId, operator string) (resultPackageId string, err error) {
	var actions []*db.ExecAction
	if pluginPackageId == "" {
		pluginPackageId = "plugin_" + guid.CreateGuid()
	}
	resultPackageId = pluginPackageId
	nowTime := time.Now()
	edition := models.PluginEditionCommunity
	if enterprise {
		edition = models.PluginEditionEnterprise
	}
	actions = append(actions, &db.ExecAction{Sql: "insert into plugin_packages ( id,name,`version`,status,upload_timestamp,ui_package_included,`edition`,register_done,updated_by,updated_time) values (?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
		pluginPackageId, registerConfig.Name, registerConfig.Version, models.PluginStatusUnRegistered, nowTime, withUi, edition, 0, operator, nowTime}})
	for _, pluginConfig := range registerConfig.Plugins.Plugin {
		pluginConfigId := "p_config_" + guid.CreateGuid()
		actions = append(actions, &db.ExecAction{Sql: "insert into plugin_configs (id,plugin_package_id,name,target_package,target_entity,target_entity_filter_rule,register_name) values (?,?,?,?,?,?,?)", Param: []interface{}{
			pluginConfigId, pluginPackageId, pluginConfig.Name, pluginConfig.TargetPackage, pluginConfig.TargetEntity, pluginConfig.TargetEntityFilterRule, pluginConfig.RegisterName,
		}})
		for _, pluginConfigInterface := range pluginConfig.Interface {
			pluginConfigInterfaceId := "p_conf_inf_" + guid.CreateGuid()
			serviceName := fmt.Sprintf("%s/%s/%s", registerConfig.Name, pluginConfig.Name, pluginConfigInterface.Action)
			httpMethod := pluginConfigInterface.HttpMethod
			if httpMethod == "" {
				httpMethod = "POST"
			}
			//isAsyncProcessing := false
			if pluginConfigInterface.IsAsyncProcessing != "Y" {
				pluginConfigInterface.IsAsyncProcessing = "N"
			}
			if pluginConfigInterface.Type == "" {
				pluginConfigInterface.Type = "EXECUTION"
			}
			actions = append(actions, &db.ExecAction{Sql: "insert into plugin_config_interfaces (id,plugin_config_id,action,service_name,service_display_name,path,http_method,is_async_processing,type,filter_rule,description) values (?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
				pluginConfigInterfaceId, pluginConfigId, pluginConfigInterface.Action, serviceName, serviceName, pluginConfigInterface.Path, httpMethod, pluginConfigInterface.IsAsyncProcessing, pluginConfigInterface.Type, pluginConfigInterface.FilterRule, pluginConfigInterface.Description,
			}})
			for _, interfaceParam := range pluginConfigInterface.InputParameters.Parameter {
				interfaceParamId := "p_conf_inf_param_" + guid.CreateGuid()
				required, sensitive, multiple := false, false, false
				if interfaceParam.Required == "Y" {
					required = true
				}
				if interfaceParam.SensitiveData == "Y" {
					sensitive = true
				}
				if interfaceParam.Multiple == "Y" {
					multiple = true
				}
				actions = append(actions, &db.ExecAction{Sql: "insert into plugin_config_interface_parameters (id,plugin_config_interface_id,type,name,data_type,mapping_type,mapping_entity_expression,mapping_system_variable_name,required,sensitive_data,description,mapping_val,ref_object_name,multiple ) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
					interfaceParamId, pluginConfigInterfaceId, models.PluginParamTypeInput, interfaceParam.Text, interfaceParam.Datatype, interfaceParam.MappingType, interfaceParam.MappingEntityExpression, interfaceParam.MappingSystemVariableName, required, sensitive, interfaceParam.Description, interfaceParam.MappingVal, interfaceParam.RefObjectName, multiple,
				}})
			}
			for _, interfaceParam := range pluginConfigInterface.OutputParameters.Parameter {
				interfaceParamId := "p_conf_inf_param_" + guid.CreateGuid()
				required, sensitive, multiple := false, false, false
				if interfaceParam.Required == "Y" {
					required = true
				}
				if interfaceParam.SensitiveData == "Y" {
					sensitive = true
				}
				if interfaceParam.Multiple == "Y" {
					multiple = true
				}
				actions = append(actions, &db.ExecAction{Sql: "insert into plugin_config_interface_parameters (id,plugin_config_interface_id,type,name,data_type,mapping_type,mapping_entity_expression,mapping_system_variable_name,required,sensitive_data,description,mapping_val,ref_object_name,multiple ) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
					interfaceParamId, pluginConfigInterfaceId, models.PluginParamTypeOutput, interfaceParam.Text, interfaceParam.Datatype, interfaceParam.MappingType, interfaceParam.MappingEntityExpression, interfaceParam.MappingSystemVariableName, required, sensitive, interfaceParam.Description, interfaceParam.MappingVal, interfaceParam.RefObjectName, multiple,
				}})
			}
		}
	}
	variableSource := fmt.Sprintf("%s__%s", registerConfig.Name, registerConfig.Version)
	for _, systemVariable := range registerConfig.SystemParameters.SystemParameter {
		svId := "sys_var_" + guid.CreateGuid()
		actions = append(actions, &db.ExecAction{Sql: "INSERT INTO system_variables (id,package_name,name,value,default_value,`scope`,source,status) VALUES (?,?,?,?,?,?,?,?)", Param: []interface{}{
			svId, registerConfig.Name, systemVariable.Name, "", systemVariable.DefaultValue, systemVariable.ScopeType, variableSource, models.SystemVariableInactive,
		}})
	}
	for _, pluginMenu := range registerConfig.Menus.Menu {
		menuId := "p_menu_" + guid.CreateGuid()
		actions = append(actions, &db.ExecAction{Sql: "insert into plugin_package_menus(id,plugin_package_id,code,category,display_name,local_display_name,`path`) values (?,?,?,?,?,?,?)", Param: []interface{}{
			menuId, pluginPackageId, pluginMenu.Code, pluginMenu.Cat, pluginMenu.DisplayName, pluginMenu.LocalDisplayName, pluginMenu.Text,
		}})
	}
	for _, dependence := range registerConfig.PackageDependencies.PackageDependency {
		depId := "p_pkg_dep_" + guid.CreateGuid()
		actions = append(actions, &db.ExecAction{Sql: "insert into plugin_package_dependencies (id,plugin_package_id,dependency_package_name,dependency_package_version) values (?,?,?,?)", Param: []interface{}{
			depId, pluginPackageId, dependence.Name, dependence.Version,
		}})
	}
	actions = append(actions, &db.ExecAction{Sql: "insert into plugin_package_runtime_resources_docker (id,plugin_package_id,image_name,container_name,port_bindings,volume_bindings,env_variables) values (?,?,?,?,?,?,?)", Param: []interface{}{
		"p_res_docker_" + guid.CreateGuid(), pluginPackageId, registerConfig.ResourceDependencies.Docker.ImageName, registerConfig.ResourceDependencies.Docker.ContainerName, registerConfig.ResourceDependencies.Docker.PortBindings, registerConfig.ResourceDependencies.Docker.VolumeBindings, registerConfig.ResourceDependencies.Docker.EnvVariables,
	}})
	if registerConfig.ResourceDependencies.Mysql.Schema != "" {
		actions = append(actions, &db.ExecAction{Sql: "INSERT INTO plugin_package_runtime_resources_mysql (id,plugin_package_id,schema_name,init_file_name,upgrade_file_name) values (?,?,?,?,?)", Param: []interface{}{
			"p_res_mysql_" + guid.CreateGuid(), pluginPackageId, registerConfig.ResourceDependencies.Mysql.Schema, registerConfig.ResourceDependencies.Mysql.InitFileName, registerConfig.ResourceDependencies.Mysql.UpgradeFileName,
		}})
	}
	if registerConfig.ResourceDependencies.S3.BucketName != "" {
		actions = append(actions, &db.ExecAction{Sql: "INSERT INTO plugin_package_runtime_resources_s3 (id,plugin_package_id,bucket_name,additional_properties) values  (?,?,?,NULL)", Param: []interface{}{
			"p_res_s3_" + guid.CreateGuid(), pluginPackageId, registerConfig.ResourceDependencies.S3.BucketName,
		}})
	}
	if len(registerConfig.DataModel.Entity) > 0 {
		maxVersion, getVersionErr := getMaxDataModelVersion(registerConfig.Name)
		if getVersionErr != nil {
			err = getVersionErr
			return
		}
		maxVersion = maxVersion + 1
		dmId := "p_model_" + guid.CreateGuid()
		actions = append(actions, &db.ExecAction{Sql: "INSERT INTO plugin_package_data_model (id,`version`,package_name,is_dynamic,update_path,update_method,update_source,updated_time,update_time) VALUES (?,?,?,?,?,?,?,?,?)", Param: []interface{}{
			dmId, maxVersion, registerConfig.Name, 0, "/data-model", "GET", "PLUGIN_PACKAGE", nowTime, nowTime.UnixMilli(),
		}})
		for _, dataModel := range registerConfig.DataModel.Entity {
			entityId := "p_mod_entity_" + guid.CreateGuid()
			actions = append(actions, &db.ExecAction{Sql: "INSERT INTO plugin_package_entities (id,data_model_id,data_model_version,package_name,name,display_name,description) VALUES (?,?,?,?,?,?,?)", Param: []interface{}{
				entityId, dmId, maxVersion, registerConfig.Name, dataModel.Name, dataModel.DisplayName, dataModel.Description,
			}})
			for attrIndex, attr := range dataModel.Attribute {
				attrId := "p_mod_attr_" + guid.CreateGuid()
				tmpMultiple := false
				if attr.Multiple == "Y" {
					tmpMultiple = true
				} else {
					attr.Multiple = "N"
				}
				actions = append(actions, &db.ExecAction{Sql: "INSERT INTO plugin_package_attributes (id,entity_id,name,description,data_type,mandatory,multiple,is_array,created_time,order_no) values  (?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
					attrId, entityId, attr.Name, attr.Description, attr.Datatype, 0, attr.Multiple, tmpMultiple, nowTime, attrIndex,
				}})
			}
		}
	} else if registerConfig.DataModel.IsDynamic == "true" {
		maxVersion, getVersionErr := getMaxDataModelVersion(registerConfig.Name)
		if getVersionErr != nil {
			err = getVersionErr
			return
		}
		maxVersion = maxVersion + 1
		dmId := "p_model_" + guid.CreateGuid()
		actions = append(actions, &db.ExecAction{Sql: "INSERT INTO plugin_package_data_model (id,`version`,package_name,is_dynamic,update_path,update_method,update_source,updated_time,update_time) VALUES (?,?,?,?,?,?,?,?,?)", Param: []interface{}{
			dmId, maxVersion, registerConfig.Name, 1, "/data-model", "GET", "PLUGIN_PACKAGE", nowTime, nowTime.UnixMilli(),
		}})
	}
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func getMaxDataModelVersion(packageName string) (maxV int, err error) {
	queryResult, queryErr := db.MysqlEngine.QueryString("SELECT max(`version`) as ver FROM plugin_package_data_model WHERE package_name =? GROUP BY package_name", packageName)
	if queryErr != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, fmt.Errorf("query data model max version fail,%s ", queryErr.Error()))
		return
	}
	if len(queryResult) == 0 {
		return
	}
	maxV, _ = strconv.Atoi(queryResult[0]["ver"])
	return
}

func GetSimplePluginPackage(ctx context.Context, param *models.PluginPackages, emptyCheck bool) (err error) {
	var pluginPackagesRows []*models.PluginPackages
	if param.Id != "" {
		err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_packages where id=?", param.Id).Find(&pluginPackagesRows)
	} else {
		if param.Edition == "" {
			err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_packages where name=? and `version`=?", param.Name, param.Version).Find(&pluginPackagesRows)
		} else {
			err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_packages where name=? and `version`=? and `edition`=?", param.Name, param.Version, param.Edition).Find(&pluginPackagesRows)
		}
	}
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
	} else {
		if len(pluginPackagesRows) == 0 {
			if emptyCheck {
				err = fmt.Errorf("can not find plugin packages with id:%s name:%s version:%s", param.Id, param.Name, param.Version)
			}
		} else {
			param.Id = pluginPackagesRows[0].Id
			param.Name = pluginPackagesRows[0].Name
			param.Version = pluginPackagesRows[0].Version
			param.Edition = pluginPackagesRows[0].Edition
			param.Status = pluginPackagesRows[0].Status
			param.UiPackageIncluded = pluginPackagesRows[0].UiPackageIncluded
		}
	}
	return
}

func CheckPluginPackageDependence(ctx context.Context, pluginPackageId string) (ok bool, err error) {
	var pluginPackageDepRows []*models.PluginPackageDependencies
	err = db.MysqlEngine.Context(ctx).SQL("select dependency_package_name,dependency_package_version from plugin_package_dependencies where plugin_package_id=?", pluginPackageId).Find(&pluginPackageDepRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(pluginPackageDepRows) == 0 {
		return true, nil
	}
	var depPlatformVersion string
	var depPluginVersionName []string
	depPluginVersionMap := make(map[string]string)
	for _, row := range pluginPackageDepRows {
		if row.DependencyPackageName == "platform" {
			depPlatformVersion = row.DependencyPackageVersion
			continue
		}
		depPluginVersionMap[row.DependencyPackageName] = row.DependencyPackageVersion
		depPluginVersionName = append(depPluginVersionName, row.DependencyPackageName)
	}
	if depPlatformVersion != "" {
		if tools.CompareVersion(depPlatformVersion, models.Config.Version) {
			err = fmt.Errorf("platform version required %s > %s", depPlatformVersion, models.Config.Version)
			return
		}
	}
	if len(depPluginVersionName) == 0 {
		return true, nil
	}
	var pluginPackageRows []*models.PluginPackages
	filterSql, filterParams := db.CreateListParams(depPluginVersionName, "")
	filterParams = append([]interface{}{models.PluginStatusRegistered}, filterParams...)
	err = db.MysqlEngine.Context(ctx).SQL("select name,`version` from plugin_packages where status=? and name in ("+filterSql+")", filterParams...).Find(&pluginPackageRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	for _, row := range pluginPackageRows {
		if v, b := depPluginVersionMap[row.Name]; b {
			if tools.CompareVersion(row.Version, v) {
				delete(depPluginVersionMap, row.Name)
			}
		}
	}
	if len(depPluginVersionMap) == 0 {
		return true, nil
	}
	var errorMessages []string
	for k, v := range depPluginVersionMap {
		errorMessages = append(errorMessages, fmt.Sprintf("depence %s:%s illegal", k, v))
	}
	err = fmt.Errorf(strings.Join(errorMessages, ","))
	return
}

func GetResourceServer(ctx context.Context, serverType, serverIp, name string) (resourceServerObj *models.ResourceServer, err error) {
	var resourceServerRows []*models.ResourceServer
	if name != "" {
		err = db.MysqlEngine.Context(ctx).SQL("select id,host,is_allocated,login_password,login_username,name,port,login_mode from resource_server where `name`=? and `type`=? and status='active'", name, serverType).Find(&resourceServerRows)
	} else if serverIp == "" {
		err = db.MysqlEngine.Context(ctx).SQL("select id,host,is_allocated,login_password,login_username,name,port,login_mode from resource_server where `type`=? and status='active' order by created_date asc", serverType).Find(&resourceServerRows)
	} else {
		err = db.MysqlEngine.Context(ctx).SQL("select id,host,is_allocated,login_password,login_username,name,port,login_mode from resource_server where `type`=? and host=? and status='active'", serverType, serverIp).Find(&resourceServerRows)
	}
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
	} else {
		if len(resourceServerRows) == 0 {
			err = fmt.Errorf("can not find resource server with type:%s ip:%s ", serverType, serverIp)
		} else {
			//if serverType == "mysql" && len(resourceServerRows) > 1 && name == "" {
			//	err = fmt.Errorf("get more then one mysql resouerce server,please make sure one")
			//	return
			//}
			if serverType == "mysql" && name == "" {
				for _, row := range resourceServerRows {
					if row.Name == "plugin" {
						resourceServerObj = row
						break
					}
				}
				if resourceServerObj == nil {
					resourceServerObj = resourceServerRows[0]
				}
			} else {
				resourceServerObj = resourceServerRows[0]
			}
			if strings.HasPrefix(resourceServerObj.LoginPassword, models.AESPrefix) {
				resourceServerObj.LoginPassword = encrypt.DecryptWithAesECB(resourceServerObj.LoginPassword[5:], models.Config.Plugin.ResourcePasswordSeed, resourceServerObj.Name)
			}
		}
	}
	return
}

func CheckServerPortRunning(ctx context.Context, serverIp string, port int) (running bool, err error) {
	queryResult, queryErr := db.MysqlEngine.Context(ctx).QueryString("select id from plugin_instances where host=? and port=? and container_status='RUNNING'", serverIp, port)
	if queryErr != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, queryErr)
		return
	}
	if len(queryResult) > 0 {
		running = true
	}
	return
}

func GetPluginMysqlInstance(ctx context.Context, name string) (result *models.PluginMysqlInstances, err error) {
	var mysqlInstanceRows []*models.PluginMysqlInstances
	err = db.MysqlEngine.Context(ctx).SQL("select id,`password`,plugun_package_id,resource_item_id,schema_name,username,pre_version from plugin_mysql_instances where status='active' and plugun_package_id in (select id from plugin_packages where name=?)", name).Find(&mysqlInstanceRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
	} else {
		if len(mysqlInstanceRows) > 0 {
			result = mysqlInstanceRows[0]
			if strings.HasPrefix(result.Password, models.AESPrefix) {
				result.Password = encrypt.DecryptWithAesECB(result.Password[5:], models.Config.Plugin.ResourcePasswordSeed, result.SchemaName)
			}
		}
	}
	return
}

func NewPluginMysqlInstance(ctx context.Context, mysqlServer *models.ResourceServer, mysqlInstance *models.PluginMysqlInstances, operator string) (err error) {
	instancePassword := mysqlInstance.Password
	if !strings.HasPrefix(mysqlInstance.Password, models.AESPrefix) {
		instancePassword = models.AESPrefix + encrypt.EncryptWithAesECB(mysqlInstance.Password, models.Config.Plugin.ResourcePasswordSeed, mysqlInstance.SchemaName)
	}
	var actions []*db.ExecAction
	nowTime := time.Now()
	properties := models.MysqlResourceItemProperties{Username: mysqlInstance.Username, Password: instancePassword}
	propertiesBytes, _ := json.Marshal(&properties)
	resourceItemId := "rs_item_" + guid.CreateGuid()
	actions = append(actions, &db.ExecAction{Sql: "INSERT INTO resource_item (id,additional_properties,created_by,created_date,is_allocated,name,purpose,resource_server_id,status,`type`,updated_by,updated_date) values (?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
		resourceItemId, string(propertiesBytes), operator, nowTime, 1, mysqlInstance.SchemaName, fmt.Sprintf("Create MySQL database for plugin[%s]", mysqlInstance.SchemaName), mysqlServer.Id, "created", "mysql_database", operator, nowTime,
	}})
	actions = append(actions, &db.ExecAction{Sql: "INSERT INTO plugin_mysql_instances (id,password,plugun_package_id,plugin_package_id,resource_item_id,schema_name,status,username,pre_version,created_time) values (?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
		mysqlInstance.Id, instancePassword, mysqlInstance.PluginPackageId, mysqlInstance.PluginPackageId, resourceItemId, mysqlInstance.SchemaName, "active", mysqlInstance.Username, "", time.Now(),
	}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func UpdatePluginMysqlInstancePreVersion(ctx context.Context, mysqlInstanceId, preVersion string) (err error) {
	_, err = db.MysqlEngine.Context(ctx).Exec("update plugin_mysql_instances set pre_version=? where id=?", preVersion, mysqlInstanceId)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func BuildDockerEnvMap(ctx context.Context, envMap map[string]string) (replaceMap map[string]string, err error) {
	if envMap == nil {
		return nil, fmt.Errorf("illegal docker env map")
	}
	replaceMap = make(map[string]string)
	var sysVarList []string
	for k, v := range envMap {
		if v == "" {
			if k == "GATEWAY_URL" {
				sysVarList = append(sysVarList, "GATEWAY_URL_NEW")
			}
			sysVarList = append(sysVarList, k)
		}
	}
	defer func() {
		for k, v := range envMap {
			replaceMap[fmt.Sprintf("{{%s}}", k)] = v
		}
	}()
	if len(sysVarList) > 0 {
		var systemVariableRows []*models.SystemVariables
		filterSql, filterParam := db.CreateListParams(sysVarList, "")
		err = db.MysqlEngine.Context(ctx).SQL("select name,`value`,default_value from system_variables where status='active' and name in ("+filterSql+")", filterParam...).Find(&systemVariableRows)
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}
		for _, row := range systemVariableRows {
			tmpV := row.Value
			if tmpV == "" {
				tmpV = row.DefaultValue
			}
			if tmpV != "" {
				envMap[row.Name] = tmpV
			}
		}
		if v, ok := envMap["GATEWAY_URL_NEW"]; ok {
			envMap["GATEWAY_URL"] = v
			delete(envMap, "GATEWAY_URL_NEW")
		}
	}
	for k, v := range envMap {
		replaceMap[fmt.Sprintf("{{%s}}", k)] = v
	}
	return
}

func LaunchPlugin(ctx context.Context, pluginInstance *models.PluginInstances, resourceItem *models.ResourceItem, operator string) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "INSERT INTO resource_item (id,additional_properties,created_by,created_date,is_allocated,name,purpose,resource_server_id,status,`type`,updated_by,updated_date) values (?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
		resourceItem.Id, resourceItem.AdditionalProperties, resourceItem.CreatedBy, resourceItem.CreatedDate, 1, resourceItem.Name, resourceItem.Purpose, resourceItem.ResourceServerId, "created", "docker_container", resourceItem.CreatedBy, resourceItem.CreatedDate,
	}})
	insertInsAction := &db.ExecAction{Sql: "INSERT INTO plugin_instances (id,host,container_name,port,container_status,package_id,docker_instance_resource_id,instance_name,plugin_mysql_instance_resource_id,s3bucket_resource_id) values (?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
		pluginInstance.Id, pluginInstance.Host, pluginInstance.ContainerName, pluginInstance.Port, pluginInstance.ContainerStatus, pluginInstance.PackageId, pluginInstance.DockerInstanceResourceId, pluginInstance.InstanceName,
	}}
	if pluginInstance.PluginMysqlInstanceResourceId != "" {
		insertInsAction.Param = append(insertInsAction.Param, pluginInstance.PluginMysqlInstanceResourceId)
	} else {
		insertInsAction.Param = append(insertInsAction.Param, nil)
	}
	if pluginInstance.S3bucketResourceId != "" {
		insertInsAction.Param = append(insertInsAction.Param, pluginInstance.S3bucketResourceId)
	} else {
		insertInsAction.Param = append(insertInsAction.Param, nil)
	}
	actions = append(actions, insertInsAction)
	//actions = append(actions, &db.ExecAction{Sql: "INSERT INTO plugin_instances (id,host,container_name,port,container_status,package_id,docker_instance_resource_id,instance_name,plugin_mysql_instance_resource_id,s3bucket_resource_id) values (?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
	//	pluginInstance.Id, pluginInstance.Host, pluginInstance.ContainerName, pluginInstance.Port, pluginInstance.ContainerStatus, pluginInstance.PackageId, pluginInstance.DockerInstanceResourceId, pluginInstance.InstanceName, pluginInstance.PluginMysqlInstanceResourceId, pluginInstance.S3bucketResourceId,
	//}})
	actions = append(actions, &db.ExecAction{Sql: "update plugin_package_menus set active=1 where plugin_package_id=?", Param: []interface{}{pluginInstance.PackageId}})
	actions = append(actions, &db.ExecAction{Sql: "update plugin_packages set updated_by=?,updated_time=? where id=?", Param: []interface{}{operator, time.Now(), pluginInstance.PackageId}})
	if err = db.Transaction(actions, ctx); err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func GetPluginInstance(pluginInstanceId string) (pluginInstance *models.PluginInstances, err error) {
	var instanceRows []*models.PluginInstances
	err = db.MysqlEngine.SQL("select * from plugin_instances where id=?", pluginInstanceId).Find(&instanceRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
	} else {
		if len(instanceRows) == 0 {
			err = fmt.Errorf("can not find plugin instance with id:%s ", pluginInstanceId)
		} else {
			pluginInstance = instanceRows[0]
		}
	}
	return
}

func GetPluginDockerRunningResource(dockerInstanceResourceId string) (pluginResourceServer *models.ResourceServer, err error) {
	var resourceServerRows []*models.ResourceServer
	err = db.MysqlEngine.SQL("select id,name,host,login_username,login_password,port,login_mode,is_allocated from resource_server where id in (select resource_server_id from resource_item where id=?)", dockerInstanceResourceId).Find(&resourceServerRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(resourceServerRows) == 0 {
		err = exterror.Catch(exterror.New().DatabaseQueryEmptyError, fmt.Errorf("resource_server"))
		return
	}
	pluginResourceServer = resourceServerRows[0]
	if strings.HasPrefix(pluginResourceServer.LoginPassword, models.AESPrefix) {
		pluginResourceServer.LoginPassword = encrypt.DecryptWithAesECB(pluginResourceServer.LoginPassword[5:], models.Config.Plugin.ResourcePasswordSeed, pluginResourceServer.Name)
	}
	return
}

func GetPluginDockerRuntimeMessage(pluginPackageId string) (imageName, containerName string, err error) {
	var dockerRows []*models.PluginPackageRuntimeResourcesDocker
	err = db.MysqlEngine.SQL("select id,plugin_package_id,image_name,container_name from plugin_package_runtime_resources_docker where plugin_package_id=?", pluginPackageId).Find(&dockerRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(dockerRows) == 0 {
		err = exterror.Catch(exterror.New().DatabaseQueryEmptyError, fmt.Errorf("plugin_package_runtime_resources_docker"))
		return
	}
	imageName = dockerRows[0].ImageName
	containerName = dockerRows[0].ContainerName
	return
}

func RemovePlugin(ctx context.Context, pluginPackageId, pluginInstanceId, resourceItemId string) (err error) {
	queryResult, queryErr := db.MysqlEngine.QueryString("select id from plugin_instances where package_id=?", pluginPackageId)
	if queryErr != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, queryErr)
		return
	}
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "delete from plugin_instances where id=?", Param: []interface{}{pluginInstanceId}})
	actions = append(actions, &db.ExecAction{Sql: "delete from resource_item where id=?", Param: []interface{}{resourceItemId}})
	if len(queryResult) == 1 {
		actions = append(actions, &db.ExecAction{Sql: "update plugin_package_menus set active=0 where plugin_package_id=?", Param: []interface{}{pluginPackageId}})
	}
	if err = db.Transaction(actions, ctx); err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func GetPluginRunningInstances(ctx context.Context, pluginPackageId string) (result []*models.PluginInstances, err error) {
	result = []*models.PluginInstances{}
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_instances where package_id=?", pluginPackageId).Find(&result)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
	}
	return
}

// GetAllMenusByPackageStatus 根据包状态返回对应菜单列表
func GetAllMenusByPackageStatus(ctx context.Context, statusArr []string) (result []*models.PluginPackageMenus, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("SELECT t1.id, t1.plugin_package_id,t1.code, t1.category,t1.source,t1.display_name,t1.local_display_name,t1.menu_order,t1.path," +
		" t1.active FROM plugin_package_menus t1,plugin_packages t2 WHERE t1.plugin_package_id =t2.id and t2.status in (" + getInSQL(statusArr) + ")").Find(&result)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

// GetAllMenusByCodeAndPackageStatus 根据code和包状态返回对应菜单列表
func GetAllMenusByCodeAndPackageStatus(ctx context.Context, code string, statusArr []string) (result []*models.PluginPackageMenus, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("SELECT t1.id, t1.plugin_package_id,t1.code, t1.category,t1.source,t1.display_name,t1.local_display_name,t1.menu_order,t1.path,"+
		" t1.active FROM plugin_package_menus t1,plugin_packages t2 WHERE t1.plugin_package_id =t2.id and t1.code = ? and t2.status in ("+getInSQL(statusArr)+")", code).Find(&result)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func getInSQL(status []string) string {
	var sql string
	for i := 0; i < len(status); i++ {
		if i == len(status)-1 {
			sql = sql + "'" + status[i] + "'"
		} else {
			sql = sql + "'" + status[i] + "',"
		}
	}
	return sql
}

func GetPackageNames(ctx context.Context) (result []string, err error) {
	queryRows, queryErr := db.MysqlEngine.Context(ctx).QueryString("select distinct name from plugin_packages")
	if queryErr != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, queryErr)
	} else {
		for _, row := range queryRows {
			result = append(result, row["name"])
		}
	}
	return
}

func UpdatePluginStaticResourceFiles(ctx context.Context, pluginPackageId, pluginPackageName string, inputs []*models.PluginPackageResourceFiles, operator string) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "delete from plugin_package_resource_files where plugin_package_id=?", Param: []interface{}{pluginPackageId}})
	for _, v := range inputs {
		actions = append(actions, &db.ExecAction{Sql: "INSERT INTO plugin_package_resource_files (id,plugin_package_id,package_name,package_version,`source`,related_path) values (?,?,?,?,?,?)", Param: []interface{}{
			"p_ui_" + guid.CreateGuid(), v.PluginPackageId, v.PackageName, v.PackageVersion, v.Source, v.RelatedPath,
		}})
	}
	actions = append(actions, &db.ExecAction{Sql: "update plugin_packages set ui_active=0 where name=? and ui_active=1", Param: []interface{}{pluginPackageName}})
	actions = append(actions, &db.ExecAction{Sql: "update plugin_packages set ui_active=1,updated_by=?,updated_time=? where id=?", Param: []interface{}{pluginPackageId, operator, time.Now()}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func GetPluginResourceFiles(ctx context.Context) (result []*models.PluginPackageResourceFiles, err error) {
	result = []*models.PluginPackageResourceFiles{}
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_package_resource_files where plugin_package_id in (SELECT t1.id FROM plugin_packages t1 where t1.ui_package_included=1 and t1.status IN ('REGISTERED' ,'RUNNING', 'STOPPED') AND t1.upload_timestamp = (SELECT MAX(t2.upload_timestamp) FROM plugin_packages t2 WHERE t2.status IN ('REGISTERED' ,'RUNNING', 'STOPPED') AND t2.name = t1.name GROUP BY t2.name))").Find(&result)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
	}
	return
}

func GetPluginRunningInstancesByName(ctx context.Context, pluginPackageName string) (result []*models.PluginInstances, err error) {
	result = []*models.PluginInstances{}
	sql := `select
	pi2.*
from
	plugin_instances pi2
left join plugin_packages pp on
	pi2.package_id = pp.id
where
	pi2.container_status = 'RUNNING'
	and pp.name = ?
order by
	pi2.id desc`
	err = db.MysqlEngine.Context(ctx).SQL(sql, pluginPackageName).Find(&result)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
	}
	return
}

func CreatePluginPackagePullReq(ctx context.Context, data *models.PluginArtifactPullReq, userId string) (result *models.PluginArtifactPullReq, err error) {
	session := db.MysqlEngine.NewSession().Context(ctx)
	defer session.Close()
	err = session.Begin()
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	newId := data.Id
	if newId == "" {
		newId = "pluginPull_" + guid.CreateGuid()
	}
	newData := &models.PluginArtifactPullReq{
		Id:          newId,
		BucketName:  data.BucketName,
		ErrMsg:      data.ErrMsg,
		KeyName:     data.KeyName,
		PkgId:       data.PkgId,
		State:       data.State,
		Rev:         data.Rev,
		TotalSize:   data.TotalSize,
		CreatedTime: time.Now().Format("2006-01-02 15:04:05"),
		CreatedBy:   userId,
		UpdatedTime: time.Now().Format("2006-01-02 15:04:05"),
		UpdatedBy:   userId,
	}

	_, err = session.Insert(newData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	err = session.Commit()
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	return
}

func UpdatePluginPackagePullReq(ctx context.Context, pullId, pkgId, state, stateMsg, userId string, fileSize int) (err error) {
	session := db.MysqlEngine.NewSession().Context(ctx)
	defer session.Close()
	err = session.Begin()
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	updateData := make(map[string]interface{})
	updatedTime := time.Now().Format("2006-01-02 15:04:05")
	if pkgId != "" {
		updateData["pkg_id"] = pkgId
	}
	if state != "" {
		updateData["state"] = state
	}
	if stateMsg != "" {
		updateData["err_msg"] = stateMsg
	}
	updateData["updated_time"] = updatedTime
	if userId != "" {
		updateData["updated_by"] = userId
	}
	if fileSize > 0 {
		updateData["total_size"] = fileSize
	}
	_, err = session.Table(new(models.PluginArtifactPullReq)).Where("id = ?", pullId).Update(updateData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	err = session.Commit()
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	return
}

func GetPluginPackagePullReq(ctx context.Context, pullId string) (result *models.PluginArtifactPullReq, err error) {
	result = &models.PluginArtifactPullReq{}
	var exists bool
	exists, err = db.MysqlEngine.Context(ctx).Table(new(models.PluginArtifactPullReq)).Where("id = ?", pullId).Get(result)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if !exists {
		result = nil
		return
	}
	return
}

func IsPluginInstanceRunning(ctx context.Context, pluginPackageId string) (running bool, err error) {
	var count int64
	count, err = db.MysqlEngine.Context(ctx).Table(new(models.PluginInstances)).Where("container_status = ?", "RUNNING").And("package_id = ?", pluginPackageId).Count()
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
	}
	if count > 0 {
		running = true
		return
	}
	return
}

func DisableAllPluginConfigsByPackageId(ctx context.Context, pluginPackageId string) (err error) {
	session := db.MysqlEngine.NewSession().Context(ctx)
	defer session.Close()
	err = session.Begin()
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	updateData := make(map[string]interface{})
	updateData["status"] = "DISABLED"
	_, err = session.Table(new(models.PluginConfigs)).Where("plugin_package_id = ?", pluginPackageId).Update(updateData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	err = session.Commit()
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	return
}

func DecommissionPluginPackage(ctx context.Context, pluginPackageId string) (err error) {
	session := db.MysqlEngine.NewSession().Context(ctx)
	defer session.Close()
	err = session.Begin()
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	updateData := make(map[string]interface{})
	updateData["status"] = models.PluginStatusDecommissioned
	_, err = session.Table(new(models.PluginPackages)).Where("id = ?", pluginPackageId).Update(updateData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	err = session.Commit()
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	return
}

func SetPluginPackageRegisterDone(ctx context.Context, pluginPackageId, operator string) (err error) {
	execResult, execErr := db.MysqlEngine.Context(ctx).Exec("update plugin_packages set register_done=1,updated_by=?,updated_time=? where id=?", operator, time.Now(), pluginPackageId)
	if execErr != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	if affectNum, _ := execResult.RowsAffected(); affectNum <= 0 {
		err = fmt.Errorf("can not find plugin packages with id=%s ", pluginPackageId)
	}
	return
}

func GetPluginConfigVersionList(ctx context.Context, pluginPackageId, pluginPackageName string) (result []*models.PluginVersionListObj, err error) {
	var packageRows []*models.PluginPackages
	err = db.MysqlEngine.Context(ctx).SQL("select id,name,`version` from plugin_packages where status!='DECOMMISSIONED' and name=? and id!=?", pluginPackageName, pluginPackageId).Find(&packageRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	result = []*models.PluginVersionListObj{}
	for _, row := range packageRows {
		result = append(result, &models.PluginVersionListObj{
			PluginPackageId: row.Id,
			Name:            row.Name,
			Version:         row.Version,
		})
	}
	return
}

func InheritPluginConfig(ctx context.Context, param *models.InheritPluginConfigParam, operator string) (err error) {
	parentPluginPackage := &models.PluginPackages{Id: param.InheritPackageId}
	if err = GetSimplePluginPackage(ctx, parentPluginPackage, true); err != nil {
		return
	}
	currentPluginPackage := &models.PluginPackages{Id: param.PluginPackageId}
	if err = GetSimplePluginPackage(ctx, currentPluginPackage, true); err != nil {
		return
	}
	var sourceConfigRows []*models.PluginConfigs
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_configs where plugin_package_id=? and register_name<>''", param.InheritPackageId).Find(&sourceConfigRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(sourceConfigRows) == 0 {
		return
	}
	var sourceInterfaceRows []*models.PluginConfigInterfaces
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_config_interfaces where plugin_config_id in (select id from plugin_configs where plugin_package_id=? and register_name<>'')", param.InheritPackageId).Find(&sourceInterfaceRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	var sourceParamRows []*models.PluginConfigInterfaceParameters
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_config_interface_parameters where plugin_config_interface_id in (select id from plugin_config_interfaces where plugin_config_id in (select id from plugin_configs where plugin_package_id=? and register_name<>''))", param.InheritPackageId).Find(&sourceParamRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	var sourcePermissionRows []*models.PluginConfigRoles
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_config_roles where plugin_cfg_id in (select id from plugin_configs where plugin_package_id=? and register_name<>'')", param.InheritPackageId).Find(&sourcePermissionRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	var actions []*db.ExecAction
	configIdMap := make(map[string]string)
	interfaceIdMap := make(map[string]string)
	newConfigGuidList := guid.CreateGuidList(len(sourceConfigRows))
	newInterfaceGuidList := guid.CreateGuidList(len(sourceInterfaceRows))
	nowTime := time.Now()
	actions = append(actions, getPluginConfigDeleteActions(param.PluginPackageId)...)
	for i, row := range sourceConfigRows {
		newConfigGuid := "p_config_" + newConfigGuidList[i]
		configIdMap[row.Id] = newConfigGuid
		actions = append(actions, &db.ExecAction{Sql: "insert into plugin_configs (id,plugin_package_id,name,target_package,target_entity,target_entity_filter_rule,register_name,status) values (?,?,?,?,?,?,?,?)", Param: []interface{}{
			newConfigGuid, param.PluginPackageId, row.Name, row.TargetPackage, row.TargetEntity, row.TargetEntityFilterRule, row.RegisterName, row.Status,
		}})
	}
	for i, row := range sourceInterfaceRows {
		newInterfaceGuid := "p_conf_inf_" + newInterfaceGuidList[i]
		interfaceIdMap[row.Id] = newInterfaceGuid
		actions = append(actions, &db.ExecAction{Sql: "insert into plugin_config_interfaces (id,plugin_config_id,action,service_name,service_display_name,path,http_method,is_async_processing,type,filter_rule,description) values (?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
			newInterfaceGuid, configIdMap[row.PluginConfigId], row.Action, row.ServiceName, row.ServiceDisplayName, row.Path, row.HttpMethod, row.IsAsyncProcessing, row.Type, row.FilterRule, row.Description,
		}})
	}
	for _, row := range sourceParamRows {
		newParamId := "p_conf_inf_param_" + guid.CreateGuid()
		actions = append(actions, &db.ExecAction{Sql: "insert into plugin_config_interface_parameters (id,plugin_config_interface_id,type,name,data_type,mapping_type,mapping_entity_expression,mapping_system_variable_name,required,sensitive_data,description,mapping_val,ref_object_name,multiple ) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
			newParamId, interfaceIdMap[row.PluginConfigInterfaceId], row.Type, row.Name, row.DataType, row.MappingType, row.MappingEntityExpression, row.MappingSystemVariableName, row.Required, row.SensitiveData, row.Description, row.MappingVal, row.RefObjectName, row.Multiple,
		}})
	}
	for _, row := range sourcePermissionRows {
		actions = append(actions, &db.ExecAction{Sql: "INSERT INTO plugin_config_roles (id,is_active,perm_type,plugin_cfg_id,role_id,role_name,created_by,created_time,updated_by,updated_time) values (?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
			"p_conf_rol_" + guid.CreateGuid(), row.IsActive, row.PermType, configIdMap[row.PluginCfgId], row.RoleId, row.RoleName, operator, nowTime, operator, nowTime,
		}})
	}
	// 继承系统参数
	sysVarSource := fmt.Sprintf("%s__%s", parentPluginPackage.Name, parentPluginPackage.Version)
	var sysVarRows []*models.SystemVariables
	err = db.MysqlEngine.Context(ctx).SQL("select * from system_variables where package_name=? and source=?", parentPluginPackage.Name, sysVarSource).Find(&sysVarRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(sysVarRows) > 0 {
		updateSysVarActions, buildSysVarActionErr := getPluginSystemVariableUpdateActions(sysVarRows, currentPluginPackage.Name, currentPluginPackage.Version)
		if buildSysVarActionErr != nil {
			err = buildSysVarActionErr
			return
		}
		actions = append(actions, updateSysVarActions...)
	}
	err = db.Transaction(actions, ctx)
	return
}
