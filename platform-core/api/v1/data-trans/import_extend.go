package data_trans

import (
	"context"
	"fmt"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-core/api/v1/process"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
)

var importFuncList []func(context.Context, *models.TransImportJobParam) (string, error)

func init() {
	importFuncList = append(importFuncList, importRole)
	importFuncList = append(importFuncList, importPluginConfig)
	importFuncList = append(importFuncList, importWorkflow)
	importFuncList = append(importFuncList, importBatchExecution)
	importFuncList = append(importFuncList, importArtifactPackage)
	importFuncList = append(importFuncList, importMonitorBaseConfig)
	importFuncList = append(importFuncList, importTaskManTemplate)
	importFuncList = append(importFuncList, execWorkflow)
	importFuncList = append(importFuncList, importMonitorServiceConfig)
}

// StartTransImport 执行导入
func StartTransImport(ctx context.Context, param models.ExecImportParam) (err error) {
	var transImport *models.TransImportTable
	var localPath string
	var transImportAction *models.TransImportActionTable
	if transImport, err = database.GetTransImport(ctx, param.TransImportId); err != nil {
		log.Logger.Error("GetTransImport err", log.Error(err))
		return
	}
	// 文件解压
	if localPath, err = database.DecompressExportZip(ctx, param.ExportNexusUrl, param.TransImportId); err != nil {
		log.Logger.Error("DecompressExportZip err", log.Error(err))
		return
	}
	if transImport == nil || transImport.Id == "" {
		// 初始化导入
		if err = database.InitTransImport(ctx, param.TransImportId, param.ExportNexusUrl, localPath, param.Operator); err != nil {
			log.Logger.Error("initTransImport err", log.Error(err))
			return
		}
	}
	if transImportAction, err = database.GetLatestTransImportAction(ctx, param.TransImportId); err != nil {
		log.Logger.Error("GetLatestTransImportAction err", log.Error(err))
		return
	}
	actionParam := &models.CallTransImportActionParam{
		TransImportId: param.TransImportId,
		Action:        string(models.TransImportStatusStart),
		Operator:      param.Operator,
		ActionId:      transImportAction.Id,
		DirPath:       localPath,
		Token:         param.Token,
		Language:      param.Language,
	}
	go doImportAction(ctx, actionParam)
	return
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
	transImportJobParam.DirPath = callParam.DirPath
	transImportJobParam.Token = callParam.Token
	transImportJobParam.Language = callParam.Language
	transImportJobParam.Operator = callParam.Operator
	if callParam.Action == string(models.TransImportActionStart) {
		var currentStep int
		for _, detailRow := range transImportJobParam.Details {
			if detailRow.Status == string(models.TransImportStatusNotStart) {
				currentStep = detailRow.Step
				break
			}
		}
		if currentStep == int(models.TransImportStepInitWorkflow) {
			transImportJobParam.CurrentDetail = transImportJobParam.Details[currentStep-1]
			if err = callImportFunc(ctx, transImportJobParam, execWorkflow); err != nil {
				return
			}
		} else if currentStep == int(models.TransImportStepMonitorBusiness) {
			transImportJobParam.CurrentDetail = transImportJobParam.Details[currentStep-1]
			if err = callImportFunc(ctx, transImportJobParam, importMonitorServiceConfig); err != nil {
				return
			}
		} else {
			for currentStep <= int(models.TransImportStepComponentLibrary) {
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
	// 解析role.json,导入角色
	var roleList []*models.SimpleLocalRoleDto
	var response models.QuerySingleRolesResponse
	if err = database.ParseJsonData(fmt.Sprintf("%s/role.json", transImportParam.DirPath), &roleList); err != nil {
		return
	}
	for _, role := range roleList {
		if response, err = remote.RegisterLocalRole(role, transImportParam.Token, transImportParam.Language); err != nil {
			log.Logger.Error("RegisterLocalRole err", log.Error(err))
			return
		}
		if response.Status != "OK" || response.Data.ID == "" {
			err = fmt.Errorf("RegisterLocalRole %s fail", role.Name)
			log.Logger.Error("RegisterLocalRole fail", log.String("roleName", role.Name))
			return
		}
	}
	return
}

// 2、导入cmdb插件服务、导入cmdb数据、同步cmdb数据模型、导入其它插件服务
func importPluginConfig(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {

	return
}

// 3、导入编排
func importWorkflow(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	// 解析workflow.json,导入编排
	var procDefList []*models.ProcessDefinitionDto
	if err = database.ParseJsonData(fmt.Sprintf("%s/workflow.json", transImportParam.DirPath), &procDefList); err != nil {
		return
	}
	if _, err = process.ProcDefImport(ctx, procDefList, transImportParam.Operator, transImportParam.Token, transImportParam.Language); err != nil {
		return
	}
	return
}

// 4、导入批量执行
func importBatchExecution(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	var batchExecutionTemplateList []*models.BatchExecutionTemplate
	if err = database.ParseJsonData(fmt.Sprintf("%s/batchExecution.json.json", transImportParam.DirPath), &batchExecutionTemplateList); err != nil {
		return
	}
	err = database.ImportTemplate(ctx, transImportParam.Operator, batchExecutionTemplateList)
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
func importTaskManTemplate(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	// 判断是否要导入组件库
	if transImportParam.CurrentDetail == nil {
		err = fmt.Errorf("importTaskManTemplate CurrentDetail is empty")
		log.Logger.Error("err:", log.Error(err))
		return
	}
	if transImportParam.CurrentDetail.Input == "true" {
		// 导入组件库
		err = remote.ImportComponentLibrary(fmt.Sprintf("%s/componentLibrary.json", transImportParam.DirPath), transImportParam.Token, transImportParam.Language)
		if err != nil {
			log.Logger.Error("ImportComponentLibrary err", log.Error(err))
			return
		}
	}
	// 导入模版
	err = remote.ImportRequestTemplate(fmt.Sprintf("%s/requestTemplate.json", transImportParam.DirPath), transImportParam.Token, transImportParam.Language)
	if err != nil {
		log.Logger.Error("ImportRequestTemplate err", log.Error(err))
		return
	}
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
