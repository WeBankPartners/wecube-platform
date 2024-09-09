package database

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"os"
	"strings"
	"time"
	"xorm.io/xorm"
)

func AnalyzeCMDBDataExport(ctx context.Context, param *models.AnalyzeDataTransParam) (err error) {
	cmdbEngine, getDBErr := getCMDBPluginDBResource(ctx)
	if getDBErr != nil {
		err = getDBErr
		return
	}
	var ciTypeRows []*models.SysCiTypeTable
	err = cmdbEngine.SQL("select * from sys_ci_type").Find(&ciTypeRows)
	if err != nil {
		err = fmt.Errorf("query ci type table fail,%s ", err.Error())
		return
	}
	var ciTypeAttrRows []*models.SysCiTypeAttrTable
	err = cmdbEngine.SQL("select * from sys_ci_type_attr").Find(&ciTypeAttrRows)
	if err != nil {
		err = fmt.Errorf("query ci type attribute table fail,%s ", err.Error())
		return
	}
	ciTypeAttrMap := make(map[string][]*models.SysCiTypeAttrTable)
	for _, row := range ciTypeAttrRows {
		if v, ok := ciTypeAttrMap[row.CiType]; ok {
			ciTypeAttrMap[row.CiType] = append(v, row)
		} else {
			ciTypeAttrMap[row.CiType] = []*models.SysCiTypeAttrTable{row}
		}
	}
	transConfig, getConfigErr := getDataTransVariableMap(ctx)
	if getConfigErr != nil {
		err = getConfigErr
		return
	}
	ciTypeDataMap := make(map[string]*models.CiTypeData)
	filters := []*models.CiTypeDataFilter{{CiType: transConfig.EnvCiType, Condition: "in", GuidList: []string{param.Env}}}
	log.Logger.Info("<--- start analyzeCMDBData --->")
	err = analyzeCMDBData(transConfig.BusinessCiType, param.Business, filters, ciTypeAttrMap, ciTypeDataMap, cmdbEngine)
	if err != nil {
		log.Logger.Error("<--- fail analyzeCMDBData --->", log.Error(err))
		return err
	}
	log.Logger.Info("<--- done analyzeCMDBData --->")
	dmBytes, _ := json.Marshal(ciTypeDataMap)
	if wfErr := os.WriteFile(fmt.Sprintf("/mnt/d/tmp/wecmdb/cmdb_%d.json", time.Now().Unix()), dmBytes, 0666); wfErr != nil {
		log.Logger.Error("try to write ciTypeDataMap to json file fail", log.Error(wfErr))
	}
	return
}

func analyzeCMDBData(ciType string, ciDataGuidList []string, filters []*models.CiTypeDataFilter, ciTypeAttrMap map[string][]*models.SysCiTypeAttrTable, ciTypeDataMap map[string]*models.CiTypeData, cmdbEngine *xorm.Engine) (err error) {
	log.Logger.Info("analyzeCMDBData", log.String("ciType", ciType), log.StringList("guidList", ciDataGuidList))
	if len(ciDataGuidList) == 0 {
		return
	}
	ciTypeAttributes := ciTypeAttrMap[ciType]
	var queryFilterList []string
	queryFilterList = append(queryFilterList, fmt.Sprintf("guid in ('%s')", strings.Join(ciDataGuidList, "','")))
	for _, filterObj := range filters {
		filterSql, buildFilterSqlErr := getCMDBFilterSql(ciTypeAttributes, filterObj, cmdbEngine)
		if buildFilterSqlErr != nil {
			err = fmt.Errorf("try to build filter sql with ciType:%s fail,%s ", ciType, buildFilterSqlErr.Error())
			break
		}
		if filterSql != "" {
			queryFilterList = append(queryFilterList, filterSql)
		}
	}
	if err != nil {
		return
	}
	queryCiDataResult, queryErr := cmdbEngine.QueryString(fmt.Sprintf("select * from %s where "+strings.Join(queryFilterList, " and "), ciType))
	if queryErr != nil {
		err = fmt.Errorf("query ciType:%s data fail,%s ", ciType, queryErr.Error())
		return
	}
	if len(queryCiDataResult) == 0 {
		return
	}
	var newRows []map[string]string
	var newRowsGuidList []string
	if existData, ok := ciTypeDataMap[ciType]; ok {
		for _, row := range queryCiDataResult {
			if _, existFlag := existData.DataMap[row["guid"]]; !existFlag {
				newRows = append(newRows, row)
			}
		}
		if len(newRows) == 0 {
			// 此次数据已经全在分析过的数据中，不用再递归了
			return
		}
		for _, row := range newRows {
			tmpRowGuid := row["guid"]
			existData.DataMap[tmpRowGuid] = row
			newRowsGuidList = append(newRowsGuidList, tmpRowGuid)
		}
	} else {
		dataMap := make(map[string]map[string]string)
		for _, row := range queryCiDataResult {
			tmpRowGuid := row["guid"]
			dataMap[tmpRowGuid] = row
			newRowsGuidList = append(newRowsGuidList, tmpRowGuid)
			newRows = append(newRows, row)
		}
		ciTypeDataMap[ciType] = &models.CiTypeData{DataMap: dataMap}
	}
	// 往下分析数据行的依赖
	for _, attr := range ciTypeAttributes {
		if attr.InputType == "ref" {
			refCiTypeGuidList := []string{}
			for _, row := range newRows {
				tmpRefCiDataGuid := row[attr.RefCiType]
				if tmpRefCiDataGuid != "" {
					refCiTypeGuidList = append(refCiTypeGuidList, tmpRefCiDataGuid)
				}
			}
			if len(refCiTypeGuidList) > 0 {
				if err = analyzeCMDBData(attr.RefCiType, refCiTypeGuidList, filters, ciTypeAttrMap, ciTypeDataMap, cmdbEngine); err != nil {
					break
				}
			}
		} else if attr.InputType == "multiRef" {
			toGuidList, getMultiToGuidErr := getCMDBMultiRefGuidList(ciType, attr.Name, "in", newRowsGuidList, []string{}, cmdbEngine)
			if getMultiToGuidErr != nil {
				err = fmt.Errorf("try to analyze ciType:%s dependent multiRef attr:%s error:%s ", ciType, attr.Name, getMultiToGuidErr.Error())
				break
			}
			if len(toGuidList) > 0 {
				if err = analyzeCMDBData(attr.RefCiType, toGuidList, filters, ciTypeAttrMap, ciTypeDataMap, cmdbEngine); err != nil {
					break
				}
			}
		}
	}
	if err != nil {
		return
	}
	// 往下分析数据行的被依赖
	for depCiType, depCiTypeAttrList := range ciTypeAttrMap {
		for _, depCiAttr := range depCiTypeAttrList {
			if depCiAttr.RefCiType == ciType {
				if depCiAttr.InputType == "ref" {
					queryDepCiGuidRows, tmpQueryDepCiGuidErr := cmdbEngine.QueryString(fmt.Sprintf("select guid from %s where %s in ('%s')", depCiType, depCiAttr.Name, strings.Join(newRowsGuidList, "','")))
					if tmpQueryDepCiGuidErr != nil {
						err = fmt.Errorf("try to get ciType:%s with dependent ciType:%s ref attr:%s guidList fail,%s ", ciType, depCiType, depCiAttr.Name, tmpQueryDepCiGuidErr.Error())
						break
					}
					if len(queryDepCiGuidRows) > 0 {
						depCiGuidList := []string{}
						for _, row := range queryDepCiGuidRows {
							depCiGuidList = append(depCiGuidList, row["guid"])
						}
						if err = analyzeCMDBData(depCiType, depCiGuidList, filters, ciTypeAttrMap, ciTypeDataMap, cmdbEngine); err != nil {
							break
						}
					}
				} else if depCiAttr.InputType == "multiRef" {
					depFromGuidList, tmpQueryDepCiGuidErr := getCMDBMultiRefGuidList(depCiType, depCiAttr.Name, "in", []string{}, newRowsGuidList, cmdbEngine)
					if tmpQueryDepCiGuidErr != nil {
						err = fmt.Errorf("try to get ciType:%s with dependent ciType:%s multiRef attr:%s guidList fail,%s ", ciType, depCiType, depCiAttr.Name, tmpQueryDepCiGuidErr.Error())
						break
					}
					if len(depFromGuidList) > 0 {
						if err = analyzeCMDBData(depCiType, depFromGuidList, filters, ciTypeAttrMap, ciTypeDataMap, cmdbEngine); err != nil {
							break
						}
					}
				}
			}
		}
		if err != nil {
			break
		}
	}
	if err != nil {
		return
	}
	return
}

func getCMDBPluginDBResource(ctx context.Context) (dbEngine *xorm.Engine, err error) {
	pluginMysqlInstance, getInstanceErr := GetPluginMysqlInstance(ctx, "wecmdb")
	if getInstanceErr != nil {
		err = fmt.Errorf("get cmdb mysql instance fail,%s ", getInstanceErr.Error())
		return
	}
	var resourceServerRows []*models.ResourceServer
	err = db.MysqlEngine.Context(ctx).SQL("select * from resource_server where id in (select resource_server_id from resource_item where id=?)", pluginMysqlInstance.ResourceItemId).Find(&resourceServerRows)
	if err != nil {
		return
	}
	if len(resourceServerRows) == 0 {
		err = fmt.Errorf("can not find resource server with item:%s ", pluginMysqlInstance.ResourceItemId)
		return
	}
	dbConfig := models.DatabaseConfig{
		User:     pluginMysqlInstance.Username,
		Password: pluginMysqlInstance.Password,
		Server:   resourceServerRows[0].Host,
		Port:     resourceServerRows[0].Port,
		DataBase: pluginMysqlInstance.SchemaName,
		MaxOpen:  5,
		MaxIdle:  5,
		Timeout:  60,
	}
	dbEngine, err = db.GetDatabaseEngine(&dbConfig)
	return
}

func getDataTransVariableMap(ctx context.Context) (result *models.TransDataVariableConfig, err error) {
	result = &models.TransDataVariableConfig{}
	var sysVarRows []*models.SystemVariables
	err = db.MysqlEngine.Context(ctx).SQL("select name,value,default_value from system_variables where status='active' and name like 'PLATFORM_EXPORT_%'").Find(&sysVarRows)
	if err != nil {
		err = fmt.Errorf("query system variable table fail,%s ", err.Error())
		return
	}
	for _, row := range sysVarRows {
		tmpValue := row.DefaultValue
		if row.Value != "" {
			tmpValue = row.Value
		}
		switch row.Name {
		case "PLATFORM_EXPORT_CI_BUSINESS":
			result.BusinessCiType = tmpValue
			if tmpValue == "" {
				err = fmt.Errorf("PLATFORM_EXPORT_CI_BUSINESS is empty")
			}
		case "PLATFORM_EXPORT_CI_ENV":
			result.EnvCiType = tmpValue
			if tmpValue == "" {
				err = fmt.Errorf("PLATFORM_EXPORT_CI_ENV is empty")
			}
		case "PLATFORM_EXPORT_NEXUS_URL":
			result.NexusUrl = tmpValue
		case "PLATFORM_EXPORT_NEXUS_USER":
			result.NexusUser = tmpValue
		case "PLATFORM_EXPORT_NEXUS_PWD":
			result.NexusPwd = tmpValue
		}
	}
	return
}

func getCMDBFilterSql(ciTypeAttributes []*models.SysCiTypeAttrTable, filter *models.CiTypeDataFilter, cmdbEngine *xorm.Engine) (filterSql string, err error) {
	matchAttr := &models.SysCiTypeAttrTable{}
	for _, attr := range ciTypeAttributes {
		if attr.RefCiType == filter.CiType {
			matchAttr = attr
			break
		}
	}
	if matchAttr.Id == "" {
		return
	}
	condition := "in"
	if filter.Condition == "notIn" {
		condition = "not in"
	}
	if matchAttr.InputType == "multiRef" {
		var fromGuidList []string
		if fromGuidList, err = getCMDBMultiRefGuidList(matchAttr.CiType, matchAttr.Name, condition, []string{}, filter.GuidList, cmdbEngine); err != nil {
			return
		}
		filterSql = fmt.Sprintf("guid in ('%s')", strings.Join(fromGuidList, "','"))
	} else if matchAttr.InputType == "ref" {
		filterSql = fmt.Sprintf("%s %s ('%s')", matchAttr.Name, condition, strings.Join(filter.GuidList, "','"))
	} else {
		err = fmt.Errorf("ciTypeAttr:%s refCiType:%s illegal with inputType:%s ", matchAttr.Id, filter.CiType, matchAttr.InputType)
	}
	return
}

func getCMDBMultiRefGuidList(ciType, attrName, condition string, fromGuidList, toGuidList []string, cmdbEngine *xorm.Engine) (resultGuidList []string, err error) {
	var filterColumn, filterSql, resultColumn string
	if len(fromGuidList) > 0 {
		filterColumn = "from_guid"
		resultColumn = "to_guid"
		filterSql = strings.Join(fromGuidList, "','")
	} else if len(toGuidList) > 0 {
		filterColumn = "to_guid"
		resultColumn = "from_guid"
		filterSql = strings.Join(toGuidList, "','")
	} else {
		return
	}
	queryResult, queryErr := cmdbEngine.QueryString(fmt.Sprintf("select from_guid,to_guid from `%s$%s` where %s %s ('%s')", ciType, attrName, filterColumn, condition, filterSql))
	if queryErr != nil {
		err = fmt.Errorf("query multiRef list fail,ciType:%s attrName:%s,error:%s ", ciType, attrName, queryErr.Error())
		return
	}
	for _, row := range queryResult {
		resultGuidList = append(resultGuidList, row[resultColumn])
	}
	return
}
