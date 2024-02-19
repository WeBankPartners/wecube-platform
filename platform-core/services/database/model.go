package database

import (
	"context"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"strings"
	"time"
)

func GetDataModels(ctx context.Context, pluginPackage string, withAttr bool) (result []*models.DataModel, err error) {
	var dataModelRows []*models.PluginPackageDataModel
	if pluginPackage == "" {
		err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_package_data_model where concat(package_name,'_',`version`) in (select concat(package_name,'_',max(`version`)) from plugin_package_data_model group by package_name) order by package_name").Find(&dataModelRows)
	} else {
		err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_package_data_model where package_name=? and concat(package_name,'_',`version`) in (select concat(package_name,'_',max(`version`)) from plugin_package_data_model group by package_name) order by package_name", pluginPackage).Find(&dataModelRows)
	}
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	var dmIds []string
	for _, row := range dataModelRows {
		dmIds = append(dmIds, row.Id)
	}
	dmFilterSql, dmFilterParam := db.CreateListParams(dmIds, "")
	var entityRows []*models.PluginPackageEntities
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_package_entities where data_model_id in ("+dmFilterSql+") order by name", dmFilterParam...).Find(&entityRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	modelEntityMap := make(map[string][]*models.DataModelEntity)
	entityIdMap := make(map[string]*models.DataModelEntity)
	for _, row := range entityRows {
		entityIdMap[row.Id] = &models.DataModelEntity{PluginPackageEntities: *row, ReferenceByEntityList: []*models.DataModelRefEntity{}, ReferenceToEntityList: []*models.DataModelRefEntity{}}
	}
	if withAttr {
		var entityAttrRows []*models.PluginPackageAttributes
		err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_package_attributes where entity_id in (select id from plugin_package_entities where data_model_id in ("+dmFilterSql+")) order by order_no", dmFilterParam...).Find(&entityAttrRows)
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}
		entityAttrMap := make(map[string][]*models.PluginPackageAttributes)
		entityIdDataMap := make(map[string]string)
		for _, row := range entityAttrRows {
			if mapData, ok := entityAttrMap[row.EntityId]; ok {
				entityAttrMap[row.EntityId] = append(mapData, row)
			} else {
				entityAttrMap[row.EntityId] = []*models.PluginPackageAttributes{row}
			}
			if row.Name == "id" {
				entityIdDataMap[row.Id] = row.EntityId
			}
		}
		for _, entityObj := range entityIdMap {
			if attrList, ok := entityAttrMap[entityObj.Id]; ok {
				entityObj.Attributes = attrList
				tmpRefToList := []*models.DataModelRefEntity{}
				for _, attrObj := range attrList {
					if attrObj.ReferenceId != "" {
						if refEntity, refOk := entityIdMap[entityIdDataMap[attrObj.ReferenceId]]; refOk {
							tmpRefToList = append(tmpRefToList, &models.DataModelRefEntity{PluginPackageEntities: refEntity.PluginPackageEntities, RelatedAttribute: attrObj})
							refEntity.ReferenceByEntityList = append(refEntity.ReferenceByEntityList, &models.DataModelRefEntity{PluginPackageEntities: entityObj.PluginPackageEntities, RelatedAttribute: attrObj})
						}
					}
				}
				entityObj.ReferenceToEntityList = tmpRefToList
			} else {
				entityObj.Attributes = []*models.PluginPackageAttributes{}
			}
		}
	}
	for _, row := range entityRows {
		if mapData, ok := modelEntityMap[row.DataModelId]; ok {
			modelEntityMap[row.DataModelId] = append(mapData, entityIdMap[row.Id])
		} else {
			modelEntityMap[row.DataModelId] = []*models.DataModelEntity{entityIdMap[row.Id]}
		}
	}
	for _, dataModel := range dataModelRows {
		tmpDMObj := models.DataModel{PluginPackageDataModel: *dataModel, Entities: []*models.DataModelEntity{}}
		if entityList, ok := modelEntityMap[dataModel.Id]; ok {
			tmpDMObj.Entities = entityList
		}
		result = append(result, &tmpDMObj)
	}
	return
}

func GetEntityModel(ctx context.Context, packageName, entityName string, onlyAttr bool) (result *models.DataModelEntity, err error) {
	var entityRows []*models.PluginPackageEntities
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_package_entities where package_name=? and name=? and data_model_id in (select id from plugin_package_data_model where concat(package_name,'_',`version`) in (select concat(package_name,'_',max(`version`)) from plugin_package_data_model group by package_name))", packageName, entityName).Find(&entityRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(entityRows) == 0 {
		err = exterror.New().DatabaseQueryEmptyError
		return
	}
	result = &models.DataModelEntity{PluginPackageEntities: *entityRows[0], Attributes: []*models.PluginPackageAttributes{}, ReferenceByEntityList: []*models.DataModelRefEntity{}, ReferenceToEntityList: []*models.DataModelRefEntity{}}
	var entityAttrRows []*models.PluginPackageAttributes
	err = db.MysqlEngine.Context(ctx).SQL("select t1.*,t2.package_name from plugin_package_attributes t1 left join plugin_package_entities t2 on t1.entity_id=t2.id where t1.entity_id=?", result.Id).Find(&entityAttrRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	result.Attributes = entityAttrRows
	if onlyAttr {
		return
	}
	var refToEntityIds []string
	var idAttr string
	for _, attrObj := range entityAttrRows {
		if attrObj.ReferenceId != "" {
			refToEntityIds = append(refToEntityIds, attrObj.ReferenceId)
		}
		if attrObj.Name == "id" {
			idAttr = attrObj.Id
		}
		if attrObj.Mandatory {
			attrObj.MandatoryString = "Y"
		} else {
			attrObj.MandatoryString = "N"
		}
	}
	if len(refToEntityIds) > 0 {
		refFilterSql, refFilterParam := db.CreateListParams(refToEntityIds, "")
		var refToEntityRows []*models.PluginPackageEntities
		err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_package_entities where id in (select entity_id from plugin_package_attributes where id in ("+refFilterSql+"))", refFilterParam...).Find(&refToEntityRows)
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}
		for _, attrObj := range entityAttrRows {
			if attrObj.ReferenceId != "" {
				for _, refEntityObj := range refToEntityRows {
					if attrObj.RefPackage == refEntityObj.PackageName && attrObj.RefEntity == refEntityObj.Name {
						result.ReferenceToEntityList = append(result.ReferenceToEntityList, &models.DataModelRefEntity{PluginPackageEntities: *refEntityObj, RelatedAttribute: attrObj})
					}
				}
			}
		}
	}
	if idAttr != "" {
		var refByEntityAttrRows []*models.PluginPackageAttributes
		err = db.MysqlEngine.Context(ctx).SQL("select t1.*,t2.package_name from plugin_package_attributes t1 left join plugin_package_entities t2 on t1.entity_id=t2.id where t1.reference_id=?", idAttr).Find(&refByEntityAttrRows)
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}
		if len(refByEntityAttrRows) > 0 {
			var refByEntityIds []string
			for _, row := range refByEntityAttrRows {
				refByEntityIds = append(refByEntityIds, row.EntityId)
			}
			refFilterSql, refFilterParam := db.CreateListParams(refByEntityIds, "")
			var refByEntityRows []*models.PluginPackageEntities
			err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_package_entities where id in ("+refFilterSql+")", refFilterParam...).Find(&refByEntityRows)
			if err != nil {
				err = exterror.Catch(exterror.New().DatabaseQueryError, err)
				return
			}
			for _, entityObj := range refByEntityRows {
				tmpByEntity := models.DataModelRefEntity{PluginPackageEntities: *entityObj}
				for _, attrObj := range refByEntityAttrRows {
					if attrObj.EntityId == entityObj.Id {
						if attrObj.Mandatory {
							attrObj.MandatoryString = "Y"
						} else {
							attrObj.MandatoryString = "N"
						}
						tmpByEntity.RelatedAttribute = attrObj
						break
					}
				}
				result.ReferenceByEntityList = append(result.ReferenceByEntityList, &tmpByEntity)
			}
		}
	}
	result.LeafEntityList = &models.DataModelLeafEntityList{PackageName: result.PackageName, Name: result.Name, ReferenceByEntityList: []*models.DataModelLeafEntity{}, ReferenceToEntityList: []*models.DataModelLeafEntity{}}
	if len(result.ReferenceToEntityList) == 0 && len(result.ReferenceByEntityList) == 0 {
		return
	}
	var refToFilters, refByFilters []string
	for _, v := range result.ReferenceToEntityList {
		refToFilters = append(refToFilters, fmt.Sprintf("(target_package='%s' and target_entity='%s')", v.PackageName, v.Name))
	}
	for _, v := range result.ReferenceByEntityList {
		refByFilters = append(refByFilters, fmt.Sprintf("(target_package='%s' and target_entity='%s')", v.PackageName, v.Name))
	}
	if len(refToFilters) > 0 {
		var pluginConfigRows []*models.PluginConfigs
		err = db.MysqlEngine.Context(ctx).SQL("select distinct target_package,target_entity,target_entity_filter_rule from plugin_configs where status='ENABLED' and (" + strings.Join(refToFilters, " or ") + ")").Find(&pluginConfigRows)
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}
		for _, row := range pluginConfigRows {
			result.LeafEntityList.ReferenceToEntityList = append(result.LeafEntityList.ReferenceToEntityList, &models.DataModelLeafEntity{PackageName: row.TargetPackage, EntityName: row.TargetEntity, FilterRule: fmt.Sprintf("%s:%s%s", row.TargetPackage, row.TargetEntity, row.TargetEntityFilterRule)})
		}
	}
	if len(refByFilters) > 0 {
		var pluginConfigRows []*models.PluginConfigs
		err = db.MysqlEngine.Context(ctx).SQL("select distinct target_package,target_entity,target_entity_filter_rule from plugin_configs where status='ENABLED' and (" + strings.Join(refByFilters, " or ") + ")").Find(&pluginConfigRows)
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}
		for _, row := range pluginConfigRows {
			result.LeafEntityList.ReferenceByEntityList = append(result.LeafEntityList.ReferenceByEntityList, &models.DataModelLeafEntity{PackageName: row.TargetPackage, EntityName: row.TargetEntity, FilterRule: fmt.Sprintf("%s:%s%s", row.TargetPackage, row.TargetEntity, row.TargetEntityFilterRule)})
		}
	}
	return
}

func SyncPluginDataModels(ctx context.Context, packageName string, allModels []*models.SyncDataModelCiType) (err error) {
	maxVersion, getVersionErr := getMaxDataModelVersion(packageName)
	if getVersionErr != nil {
		err = getVersionErr
		return
	}
	maxVersion = maxVersion + 1
	var actions []*db.ExecAction
	nowTime := time.Now()
	dmId := "p_model_" + guid.CreateGuid()
	actions = append(actions, &db.ExecAction{Sql: "INSERT INTO plugin_package_data_model (id,`version`,package_name,is_dynamic,update_path,update_method,update_source,updated_time,update_time) VALUES (?,?,?,?,?,?,?,?,?)", Param: []interface{}{
		dmId, maxVersion, packageName, 1, "/data-model", "GET", "PLUGIN_PACKAGE", nowTime, nowTime.UnixMilli(),
	}})
	curEntityAttrMap := make(map[string]string)
	refAttrMap := make(map[string]string)
	for _, entity := range allModels {
		entityId := "p_mod_entity_" + guid.CreateGuid()
		actions = append(actions, &db.ExecAction{Sql: "INSERT INTO plugin_package_entities (id,data_model_id,data_model_version,package_name,name,display_name,description) VALUES (?,?,?,?,?,?,?)", Param: []interface{}{
			entityId, dmId, maxVersion, packageName, entity.Name, entity.DisplayName, entity.Description,
		}})
		for attrIndex, attr := range entity.Attributes {
			attrId := "p_mod_attr_" + guid.CreateGuid()
			curEntityAttrMap[fmt.Sprintf("%s^%s^%s", packageName, entity.Name, attr.Name)] = attrId
			tmpMultiple := false
			if attr.Multiple == "Y" {
				tmpMultiple = true
			} else {
				attr.Multiple = "N"
			}
			if attr.DataType == "ref" {
				actions = append(actions, &db.ExecAction{Sql: "INSERT INTO plugin_package_attributes (id,entity_id,name,description,data_type,ref_package,ref_entity,ref_attr,mandatory,multiple,is_array,created_time,order_no) values  (?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
					attrId, entityId, attr.Name, attr.Description, attr.DataType, attr.RefPackageName, attr.RefEntityName, attr.RefAttributeName, 0, attr.Multiple, tmpMultiple, nowTime, attrIndex,
				}})
				refAttrMap[attrId] = fmt.Sprintf("%s^%s^%s", attr.RefPackageName, attr.RefEntityName, attr.RefAttributeName)
			} else {
				actions = append(actions, &db.ExecAction{Sql: "INSERT INTO plugin_package_attributes (id,entity_id,name,description,data_type,mandatory,multiple,is_array,created_time,order_no) values  (?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
					attrId, entityId, attr.Name, attr.Description, attr.DataType, 0, attr.Multiple, tmpMultiple, nowTime, attrIndex,
				}})
			}
		}
	}
	if len(refAttrMap) > 0 {
		for k, v := range refAttrMap {
			targetRefAttrId := curEntityAttrMap[v]
			if targetRefAttrId == "" {
				tmpVList := strings.Split(v, "^")
				if len(tmpVList) == 3 {
					targetRefAttrId = getLatestDataModelAttrId(ctx, tmpVList[0], tmpVList[1], tmpVList[2])
				}
			}
			if targetRefAttrId != "" {
				actions = append(actions, &db.ExecAction{Sql: "update plugin_package_attributes set reference_id=? where id=?", Param: []interface{}{targetRefAttrId, k}})
			}
		}
	}
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func getLatestDataModelAttrId(ctx context.Context, packageName, entityName, attrName string) (attrId string) {
	queryResult, queryErr := db.MysqlEngine.Context(ctx).QueryString("select t1.id from plugin_package_attributes t1 left join plugin_package_entities t2 on t1.entity_id=t2.id where t2.package_name=? and t2.name=? and t1.name=? order by t2.data_model_version desc limit 1", packageName, entityName, attrName)
	if queryErr != nil {
		log.Logger.Error("getLatestDataModelAttrId fail", log.String("package", packageName), log.String("entity", entityName), log.String("attr", attrName), log.Error(queryErr))
		return
	}
	if len(queryResult) > 0 {
		attrId = queryResult[0]["id"]
	}
	return
}

func getLatestPluginDataModel(ctx context.Context, packageName string) (pluginDataModel *models.PluginPackageDataModel, err error) {
	var modelRows []*models.PluginPackageDataModel
	err = db.MysqlEngine.Context(ctx).SQL("SELECT t1.id, t1.version, t1.package_name, t1.is_dynamic FROM plugin_package_data_model t1 WHERE t1.package_name = ? AND t1.version = ( SELECT max(t2.version) FROM plugin_package_data_model t2 WHERE t2.package_name = ? GROUP BY t2.package_name )", packageName, packageName).Find(&modelRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(modelRows) == 0 {
		err = exterror.New().DatabaseQueryEmptyError
	} else {
		pluginDataModel = modelRows[0]
	}
	return
}

func QueryExpressionEntityAttr(ctx context.Context, exprObj *models.ExpressionObj) (result *models.ExpressionEntitiesRespObj, err error) {
	pluginDataModel, getLatestModelErr := getLatestPluginDataModel(ctx, exprObj.Package)
	if getLatestModelErr != nil {
		err = getLatestModelErr
		return
	}
	var attrRows []*models.PluginPackageAttributes
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_package_attributes where entity_id in (select id from plugin_package_entities where name =? and data_model_id=?)", exprObj.Entity, pluginDataModel.Id).Find(&attrRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	result = &models.ExpressionEntitiesRespObj{PackageName: exprObj.Package, EntityName: exprObj.Entity, Attributes: attrRows}
	return
}
