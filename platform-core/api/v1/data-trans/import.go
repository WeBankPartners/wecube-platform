package data_trans

import (
	"context"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/tools"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/bash"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"os"
	"strings"
)

const (
	tempTransImportDir = "/tmp/trans_import/%s"
)

func ExecTransImport(ctx context.Context, nexusUrl string) (err error) {
	// 获取nexus配置
	nexusConfig, getNexusConfigErr := database.GetDataTransImportNexusConfig(ctx)
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
	// 读解压后的文件录进数据库为了给用户展示要导入什么东西

	// 开始导入
	// 1、导入角色
	// 2、导入cmdb插件服务、导入cmdb数据、同步cmdb数据模型、导入其它插件服务
	// 3、导入编排
	// 4、导入批量执行
	// 5、导入物料包
	// 6、导入监控基础类型、对象组、基础类型指标、对象组指标、对象组阈值配置、业务配置模版
	// 7、导入taskman模版和公共组件
	// 开始执行
	// 8、开始执行编排(创建资源、初始化资源、应用部署)
	// 继续导入
	// 9、导入监控业务配置、层级对象指标、层级对象阈值配置、自定义看板
	return
}
