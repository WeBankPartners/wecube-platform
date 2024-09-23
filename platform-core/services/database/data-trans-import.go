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

func GetTransImportWithDetail(ctx context.Context, transImportId string, withDetailData bool) (result *models.TransImportJobParam, err error) {
	var transImportRows []*models.TransImportTable
	err = db.MysqlEngine.Context(ctx).SQL("select * from trans_import where id=?", transImportId).Find(&transImportRows)
	if err != nil {
		err = fmt.Errorf("query trans import table fail,%s ", err.Error())
		return
	}
	if len(transImportRows) == 0 {
		err = fmt.Errorf("can not find trans import with id:%s ", transImportId)
		return
	}
	result = &models.TransImportJobParam{TransImport: transImportRows[0], Details: []*models.TransImportDetail{}}
	if withDetailData {
		err = db.MysqlEngine.Context(ctx).SQL("select * from trans_import_detail where trans_import=?").Find(&result.Details)
	} else {
		err = db.MysqlEngine.Context(ctx).SQL("select id,trans_import,name,step,status,error_msg,start_time,end_time from trans_import_detail where trans_import=?").Find(&result.Details)
	}
	if err != nil {
		err = fmt.Errorf("query trans import detail table fail,%s ", err.Error())
	}
	return
}

func UpdateTransImportDetailStatus(ctx context.Context, transImportId, transImportDetailId, status, output, errorMsg string) (err error) {

	return
}
