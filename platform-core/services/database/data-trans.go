package database

import (
	"context"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
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
	//cmdbTableList, getCmdbTableMetaErr := cmdbEngine.DBMetas()
	//if getCmdbTableMetaErr != nil {
	//	err = fmt.Errorf("get orm cmdb table list fail,%s ", getCmdbTableMetaErr.Error())
	//	return
	//}
	log.Logger.Debug("AnalyzeCMDBDataExport", log.JsonObj("transConfig", transConfig))
	//var cmdbSysTableList []*schemas.Table
	//for _, v := range cmdbTableList {
	//	if strings.HasPrefix(v.Name, "sys_") && v.Name != ""
	//}
	return
}

func AnalyzeCMDBDataImport(ctx context.Context) {

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
