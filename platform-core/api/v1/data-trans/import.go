package data_trans

import (
	"context"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"time"
)

var importFuncList []func(context.Context, *models.TransImportJobParam) (string, error)

func init() {
	importFuncList = append(importFuncList, importRole)
	importFuncList = append(importFuncList, importPluginConfig)
	importFuncList = append(importFuncList, importWorkflow)
	importFuncList = append(importFuncList, importBatchExecution)
	importFuncList = append(importFuncList, importArtifactPackage)
	importFuncList = append(importFuncList, importMonitorBaseConfig)
	importFuncList = append(importFuncList, importTaskmanTemplate)
	importFuncList = append(importFuncList, execWorkflow)
	importFuncList = append(importFuncList, importMonitorServiceConfig)
}

// StartTransImport
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
func doImportAction(ctx context.Context, callParam *models.CallTransImportActionParam) (err error) {
	transImportJobParam, getConfigErr := database.GetTransImportWithDetail(ctx, callParam.TransImportId, false)
	if getConfigErr != nil {
		err = getConfigErr
		return
	}
	if err = database.RecordTransImportAction(ctx, callParam); err != nil {
		err = fmt.Errorf("record trans import action table fail,%s ", err.Error())
		return
	}
	if callParam.Action == string(models.TransImportActionStart) {
		var currentStep int
		for _, detailRow := range transImportJobParam.Details {
			if detailRow.Status == string(models.TransImportStatusNotStart) {
				currentStep = detailRow.Step
				break
			}
		}
		if currentStep == int(models.TransImportStepArtifacts) {
			transImportJobParam.CurrentDetail = transImportJobParam.Details[currentStep-1]
			if err = callImportFunc(ctx, transImportJobParam, execWorkflow); err != nil {
				return
			}
		} else if currentStep == int(models.TransImportStepMonitor) {
			transImportJobParam.CurrentDetail = transImportJobParam.Details[currentStep-1]
			if err = callImportFunc(ctx, transImportJobParam, importMonitorServiceConfig); err != nil {
				return
			}
		} else {
			for currentStep <= int(models.TransImportStepCmdb) {
				transImportJobParam.CurrentDetail = transImportJobParam.Details[currentStep-1]
				funcObj := importFuncList[currentStep-1]
				if err = callImportFunc(ctx, transImportJobParam, funcObj); err != nil {
					break
				}
				currentStep = currentStep + 1
			}
			if err != nil {
				return
			}
		}
	}
	if err != nil {
		callParam.ErrorMsg = err.Error()
		log.Logger.Error("doImportAction fail", log.JsonObj("callParam", callParam), log.Error(err))
		database.RecordTransImportAction(ctx, callParam)
	}
	return
}

func callImportFunc(ctx context.Context, transImportJobParam *models.TransImportJobParam, funcObj func(context.Context, *models.TransImportJobParam) (string, error)) (err error) {
	if err = database.UpdateTransImportDetailStatus(ctx, transImportJobParam.TransImport.Id, transImportJobParam.CurrentDetail.Id, "doing", "", ""); err != nil {
		return
	}
	var output string
	output, err = funcObj(ctx, transImportJobParam)
	if err != nil {
		database.UpdateTransImportDetailStatus(ctx, transImportJobParam.TransImport.Id, transImportJobParam.CurrentDetail.Id, "fail", output, err.Error())
	} else {
		database.UpdateTransImportDetailStatus(ctx, transImportJobParam.TransImport.Id, transImportJobParam.CurrentDetail.Id, "success", output, "")
	}
	return
}

// 1、导入角色
func importRole(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {

	return
}

// 2、导入cmdb插件服务、导入cmdb数据、同步cmdb数据模型、导入其它插件服务
func importPluginConfig(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {

	return
}

// 3、导入编排
func importWorkflow(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {

	return
}

// 4、导入批量执行
func importBatchExecution(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {

	return
}

// 5、导入物料包
func importArtifactPackage(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {

	return
}

// 6、导入监控基础类型、对象组、基础类型指标、对象组指标、对象组阈值配置、业务配置模版
func importMonitorBaseConfig(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {

	return
}

// 7、导入taskman模版和公共组件
func importTaskmanTemplate(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {

	return
}

// 8、开始执行编排(创建资源、初始化资源、应用部署)
func execWorkflow(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {

	return
}

// 9、导入监控业务配置、层级对象指标、层级对象阈值配置、自定义看板
func importMonitorServiceConfig(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {

	return
}

func StartExecWorkflowCron() {
	t := time.NewTicker(5 * time.Second).C
	for {
		<-t
		doExecWorkflowDaemonJob()
	}
}

func doExecWorkflowDaemonJob() {
	procExecList, err := database.GetTransImportProcExecList()
	if err != nil {
		log.Logger.Error("doExecWorkflowDaemonJob fail with get proc exec list", log.Error(err))
		return
	}
	log.Logger.Debug("doExecWorkflowDaemonJob", log.JsonObj("procExecList", procExecList))
}
