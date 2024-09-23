package database

import (
	"context"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

func GetDataTransImportNexusConfig(ctx context.Context) (result *models.TransDataImportNexusConfig, err error) {
	result = &models.TransDataImportNexusConfig{}
	var sysVarRows []*models.SystemVariables
	err = db.MysqlEngine.Context(ctx).SQL("select name,value,default_value from system_variables where status='active' and name like 'PLATFORM_IMPORT_%'").Find(&sysVarRows)
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
		case "PLATFORM_IMPORT_NEXUS_URL":
			result.NexusUrl = tmpValue
		case "PLATFORM_IMPORT_NEXUS_USER":
			result.NexusUser = tmpValue
		case "PLATFORM_IMPORT_NEXUS_PWD":
			result.NexusPwd = tmpValue
		case "PLATFORM_IMPORT_NEXUS_REPO":
			result.NexusRepo = tmpValue
		}
	}
	return
}
