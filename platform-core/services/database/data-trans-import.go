package database

import (
	"context"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/tools"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/bash"
	"os"
	"strings"
)

const (
	tempTransImportDir = "/tmp/trans_import/%s"
)

func ExecTransImport(ctx context.Context, nexusUrl string) (err error) {
	// 获取nexus配置
	nexusConfig, getNexusConfigErr := getDataTransImportNexusConfig(ctx)
	if getNexusConfigErr != nil {
		err = getNexusConfigErr
		return
	}
	// 建临时目录
	var exportFileName, localExportFilePath, transImportId string
	if lastPathIndex := strings.LastIndex(nexusUrl, "/"); lastPathIndex > 0 {
		exportFileName = nexusUrl[lastPathIndex+1:]
	}
	transImportId = "t_import_" + guid.CreateGuid()
	tmpImportDir := fmt.Sprintf(tempTransImportDir, transImportId)
	localExportFilePath = fmt.Sprintf("%s/%s", tmpImportDir, exportFileName)
	if err = os.MkdirAll(tmpImportDir, 666); err != nil {
		err = fmt.Errorf("make tmp import dir fail,%s ", err.Error())
		return
	}
	// 从nexus下载
	downloadParam := tools.NexusReqParam{UserName: nexusConfig.NexusUser, Password: nexusConfig.NexusPwd, FileParams: []*tools.NexusFileParam{{SourceFilePath: nexusUrl, DestFilePath: localExportFilePath}}}
	if err = tools.DownloadFile(&downloadParam); err != nil {
		err = fmt.Errorf("donwload nexus import file fail,%s ", err.Error())
		return
	}
	// 解压
	if _, err = bash.DecompressFile(localExportFilePath, tmpImportDir); err != nil {
		return
	}

	return
}

func getDataTransImportNexusConfig(ctx context.Context) (result *models.TransDataImportNexusConfig, err error) {
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
