package database

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"io"
	"os"
	"strings"

	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/tools"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/bash"
)

const (
	tempTransImportDir = "/tmp/trans_import/%s"
)

// DecompressExportZip 导出压缩文件解压
func DecompressExportZip(ctx context.Context, nexusUrl, transImportId string) (localPath string, err error) {
	// 获取nexus配置
	nexusConfig, getNexusConfigErr := GetDataTransImportNexusConfig(ctx)
	if getNexusConfigErr != nil {
		err = getNexusConfigErr
		return
	}
	// 建临时目录
	var exportFileName, localExportFilePath string
	if lastPathIndex := strings.LastIndex(nexusUrl, "/"); lastPathIndex > 0 {
		exportFileName = nexusUrl[lastPathIndex+1:]
	}
	tmpImportDir := fmt.Sprintf(tempTransImportDir, transImportId)
	localExportFilePath = fmt.Sprintf("%s/%s", tmpImportDir, exportFileName)
	if err = os.MkdirAll(tmpImportDir, 0755); err != nil {
		err = fmt.Errorf("make tmp import dir fail,%s ", err.Error())
		return
	}
	// 从nexus下载
	downloadParam := tools.NexusReqParam{
		UserName:   nexusConfig.NexusUser,
		Password:   nexusConfig.NexusPwd,
		RepoUrl:    nexusConfig.NexusUrl,
		Repository: nexusConfig.NexusRepo,
		TimeoutSec: 60,
		FileParams: []*tools.NexusFileParam{{SourceFilePath: nexusUrl, DestFilePath: localExportFilePath}},
	}
	if err = tools.DownloadFile(&downloadParam); err != nil {
		err = fmt.Errorf("donwload nexus import file fail,%s ", err.Error())
		return
	}
	// 解压
	localPath, err = bash.DecompressFile(localExportFilePath, tmpImportDir)
	return
}

func RemoveTempExportDir(path string) (err error) {
	// 删除导出目录
	if err = os.RemoveAll(path); err != nil {
		log.Logger.Error("delete fail", log.String("path", path), log.Error(err))
	}
	return
}

func ParseJsonFile(jsonPath string) (byteValue []byte, err error) {
	var file *os.File
	file, err = os.Open(jsonPath)
	if err != nil {
		fmt.Println("Error opening JSON file:", err)
		return
	}
	defer file.Close()
	// 读取文件内容
	byteValue, _ = io.ReadAll(file)
	return
}

func GetBusinessList(localPath string) (result models.GetBusinessListRes, err error) {
	var envByteArr, productByteArr []byte
	envFilePath := fmt.Sprintf("%s/export/env.json", localPath)
	productFilePath := fmt.Sprintf("%s/export/product.json", localPath)
	if envByteArr, err = ParseJsonFile(envFilePath); err != nil {
		return
	}
	if productByteArr, err = ParseJsonFile(productFilePath); err != nil {
		return
	}
	if err = json.Unmarshal(envByteArr, &result.Environment); err != nil {
		log.Logger.Error("Environment json Unmarshal err", log.Error(err))
		return
	}
	if err = json.Unmarshal(productByteArr, &result.BusinessList); err != nil {
		log.Logger.Error("Product json Unmarshal err", log.Error(err))
		return
	}
	return
}

// GetImportUIData 获取导入的页面展示数据
func GetImportUIData(localPath string) (detail *models.TransExportDetail, err error) {
	var uiDataByteArr []byte
	detail = &models.TransExportDetail{}
	uiDataFilePath := fmt.Sprintf("%s/export/ui-data.json", localPath)
	if uiDataByteArr, err = ParseJsonFile(uiDataFilePath); err != nil {
		return
	}
	if err = json.Unmarshal(uiDataByteArr, detail); err != nil {
		log.Logger.Error("TransExportDetail json Unmarshal err", log.Error(err))
		return
	}
	return
}

// ExecImport 执行导入
func ExecImport(ctx context.Context, param models.ExecImportParam) (err error) {
	return
}

func createImport(ctx context.Context) {

}

func GetTransImport(ctx context.Context, transImportId string) (transImport *models.TransImportTable, err error) {
	transImport = &models.TransImportTable{}
	_, err = db.MysqlEngine.Context(ctx).SQL("select * from trans_import where id=?", transImportId).Get(transImport)
	return
}
