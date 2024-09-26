package data_trans

import (
	"context"
	"fmt"
	"io/fs"
	"os"
	"strings"

	"github.com/WeBankPartners/wecube-platform/platform-core/api/v1/process"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/tools"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote/monitor"
)

var importFuncList []func(context.Context, *models.TransImportJobParam) (string, error)

// defaultRoles 系统默认角色列表
var defaultRoles = []string{"SUB_SYSTEM", "IFA_OPS", "APP_DEV", "IFA_ARC", "APP_ARC", "STG_OPS", "PRD_OPS", "MONITOR_ADMIN", "CMDB_ADMIN", "SUPER_ADMIN"}

const (
	strategyEndpointGroupConst = "strategy_endpoint_group_"
)

func init() {
	importFuncList = append(importFuncList, importRole)
	importFuncList = append(importFuncList, importCmdbConfig)
	importFuncList = append(importFuncList, importPluginConfig)
	importFuncList = append(importFuncList, importWorkflow)
	importFuncList = append(importFuncList, importBatchExecution)
	importFuncList = append(importFuncList, importArtifactPackage)
	importFuncList = append(importFuncList, importMonitorBaseConfig)
	importFuncList = append(importFuncList, importTaskManComponentLibrary)
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
		// 下载物料包
		_, _, err = database.DownloadImportArtifactPackages(ctx, param.ExportNexusUrl, param.TransImportId)
		if err != nil {
			log.Logger.Error("download import artifact packages fail", log.String("url", param.ExportNexusUrl), log.Error(err))
			return
		}
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
// 2、导入cmdb插件服务、导入cmdb数据、同步cmdb数据模型、
// 3、导入其它插件服务
// 4、导入编排
// 5、导入批量执行
// 6、导入物料包
// 7、导入监控基础类型、对象组、基础类型指标、对象组指标、对象组阈值配置、业务配置模版
// 8、导入taskman组件库
// 9. 导入taskman请求模版

// 开始执行
// 10、开始执行编排(创建资源、初始化资源、应用部署)
// 继续导入
// 11、导入监控业务配置、层级对象指标、层级对象阈值配置、自定义看板、关键字层级对象、关键字对象
func doImportAction(ctx context.Context, callParam *models.CallTransImportActionParam) (err error) {
	transImportJobParam, getConfigErr := database.GetTransImportWithDetail(ctx, callParam.TransImportId, false)
	if getConfigErr != nil {
		err = getConfigErr
		log.Logger.Error("GetTransImportWithDetail err", log.Error(err))
		return
	}
	if err = database.RecordTransImportAction(ctx, callParam); err != nil {
		err = fmt.Errorf("record trans import action table fail,%s ", err.Error())
		log.Logger.Error("RecordTransImportAction err", log.Error(err))
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
		if currentStep == int(models.TransImportStepInitWorkflow) && callParam.WebStep == int(models.ImportWebDisplayStepThree) {
			transImportJobParam.CurrentDetail = transImportJobParam.Details[currentStep-1]
			if err = callImportFunc(ctx, transImportJobParam, execWorkflow); err != nil {
				return
			}
		} else if currentStep == int(models.TransImportStepMonitorBusiness) && callParam.WebStep == int(models.ImportWebDisplayStepFour) {
			transImportJobParam.CurrentDetail = transImportJobParam.Details[currentStep-1]
			if err = callImportFunc(ctx, transImportJobParam, importMonitorServiceConfig); err != nil {
				return
			}
		} else {
			for currentStep <= int(models.TransImportStepRequestTemplate) {
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
	log.Logger.Info("1. importRole start!!!")
	var roleList []*models.SimpleLocalRoleDto
	var response models.QuerySingleRolesResponse
	var defaultRoleMap = make(map[string]bool)
	if err = database.ParseJsonData(fmt.Sprintf("%s/role.json", transImportParam.DirPath), &roleList); err != nil {
		return
	}
	for _, role := range defaultRoles {
		defaultRoleMap[role] = true
	}
	for _, role := range roleList {
		if defaultRoleMap[role.Name] {
			// 系统默认角色不需要导入
			continue
		}
		if response, err = remote.RegisterLocalRole(role, transImportParam.Token, transImportParam.Language); err != nil {
			log.Logger.Error("RegisterLocalRole err", log.Error(err))
			return
		}
		if response.Status != "OK" || response.Data.ID == "" {
			err = fmt.Errorf("RegisterLocalRole fail,msg:%s", response.Message)
			log.Logger.Error("RegisterLocalRole fail", log.String("roleName", role.Name), log.String("msg", response.Message))
			return
		}
	}
	log.Logger.Info("1. importRole success end!!!")
	return
}

// 4、导入编排
func importWorkflow(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	// 解析workflow.json,导入编排
	log.Logger.Info("4. importWorkflow start!!!")
	var procDefList []*models.ProcessDefinitionDto
	var importResult *models.ImportResultDto
	var procDef *models.ProcDef
	var exist bool
	var errMsg string
	workflowPath := fmt.Sprintf("%s/workflow.json", transImportParam.DirPath)
	if exist, err = tools.PathExist(workflowPath); err != nil {
		return
	}
	if !exist {
		log.Logger.Info("importWorkflow data empty!")
		return
	}
	if err = database.ParseJsonData(workflowPath, &procDefList); err != nil {
		return
	}
	if len(procDefList) > 0 {
		if importResult, err = process.ProcDefImport(ctx, procDefList, transImportParam.Operator, transImportParam.Token, transImportParam.Language); err != nil {
			return
		}
		if importResult != nil && len(importResult.ResultList) > 0 {
			for _, data := range importResult.ResultList {
				errMsg = data.ErrMsg
				if errMsg == "" {
					errMsg = data.Message
				}
				if data.Code > 0 {
					err = fmt.Errorf("importWorkflow【%s】fail,%s", data.ProcDefName, errMsg)
					log.Logger.Error("importWorkflow fail", log.String("name", data.ProcDefName), log.String("errMsg", errMsg))
					return
				}
			}
			// 发布导入的编排
			for _, dto := range importResult.ResultList {
				if procDef, err = database.GetProcessDefinition(ctx, dto.ProcDefId); err != nil {
					return
				}
				if err = process.ExecDeployedProcDef(ctx, procDef, transImportParam.Operator); err != nil {
					err = fmt.Errorf("deployed【%s】fail,%s", procDef.Name, err.Error())
					log.Logger.Error("importWorkflow Deployed fail", log.String("name", procDef.Name), log.String("errMsg", errMsg))
					return
				}
			}
		}
	}
	log.Logger.Info("4. importWorkflow success end!!!")
	return
}

// 5、导入批量执行
func importBatchExecution(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	log.Logger.Info("5. importBatchExecution start!!!")
	var batchExecutionTemplateList []*models.BatchExecutionTemplate
	var exist bool
	batchExecutionPath := fmt.Sprintf("%s/batch_execution.json", transImportParam.DirPath)
	if exist, err = tools.PathExist(batchExecutionPath); err != nil {
		return
	}
	if !exist {
		log.Logger.Info("importBatchExecution data empty!")
		return
	}
	if err = database.ParseJsonData(batchExecutionPath, &batchExecutionTemplateList); err != nil {
		return
	}
	if len(batchExecutionTemplateList) > 0 {
		if err = database.ImportTemplate(ctx, transImportParam.Operator, batchExecutionTemplateList); err != nil {
			log.Logger.Error("importBatchExecution ImportTemplate fail", log.Error(err))
			return
		}
	}
	log.Logger.Info("5. importBatchExecution success end!!!")
	return
}

// 7、导入监控基础类型、对象组、基础类型指标、对象组指标、对象组阈值配置、业务配置模版
func importMonitorBaseConfig(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	log.Logger.Info("6. importMonitorBaseConfig start!!!")
	var monitorTypeList []string
	var response monitor.BatchAddTypeConfigResp
	var monitorTypeExist, endpointGroupExist, metricExist, strategyExist, logMonitorTemplateExist bool
	// 导入监控基础类型
	monitorTypePath := fmt.Sprintf("%s/monitor/monitor_type.json", transImportParam.DirPath)
	if monitorTypeExist, err = tools.PathExist(monitorTypePath); err != nil {
		return
	}
	if monitorTypeExist {
		if err = database.ParseJsonData(monitorTypePath, &monitorTypeList); err != nil {
			return
		}
		if response, err = monitor.ImportMonitorType(monitorTypeList, transImportParam.Token); err != nil {
			log.Logger.Error("ImportMonitorType fail", log.Error(err))
			return
		}
		if response.Status != "OK" {
			err = fmt.Errorf("ImportMonitorType %s", response.Message)
			log.Logger.Error("ImportMonitorType fail", log.String("msg", response.Message))
			return
		}
		log.Logger.Info("6-1. import monitorType success!")
	} else {
		log.Logger.Info("6-1. import monitorType data empty!")
	}
	// 导入对象组
	endpointGroupPath := fmt.Sprintf("%s/monitor/endpoint_group.json", transImportParam.DirPath)
	if endpointGroupExist, err = tools.PathExist(endpointGroupPath); err != nil {
		return
	}
	if endpointGroupExist {
		if err = monitor.ImportEndpointGroup(endpointGroupPath, transImportParam.Token, transImportParam.Language); err != nil {
			log.Logger.Error("ImportEndpointGroup fail", log.Error(err))
			return
		}
		log.Logger.Info("6-2. import endpointGroup success!")
	} else {
		log.Logger.Info("6-2. import endpointGroup date empty!")
	}

	// 导入基础类型指标、对象组指标
	metricPath := fmt.Sprintf("%s/monitor/metric", transImportParam.DirPath)
	if metricExist, err = tools.PathExist(metricPath); err != nil {
		return
	}
	if metricExist {
		var files, childFiles []fs.DirEntry
		if files, err = os.ReadDir(metricPath); err != nil {
			log.Logger.Error("ReadDir fail", log.String("metricPath", metricPath), log.Error(err))
			return
		}
		files = sortDirEntry(files)
		// 遍历文件和子目录
		for _, file := range files {
			if file.IsDir() {
				if file.Name() != "endpoint_group" {
					continue
				}
				// endpoint_group 对象组目录遍历
				if childFiles, err = os.ReadDir(fmt.Sprintf("%s/%s", metricPath, "endpoint_group")); err != nil {
					log.Logger.Error("ReadDir fail", log.Error(err))
					return
				}
				childFiles = sortDirEntry(childFiles)
				for _, newFile := range childFiles {
					comparison := "N"
					endpointGroup := newFile.Name()[:strings.LastIndex(newFile.Name(), ".")]
					if strings.Contains(file.Name(), "_comparison") {
						comparison = "Y"
						endpointGroup = newFile.Name()[:strings.LastIndex(newFile.Name(), "_comparison")]
					}
					param := monitor.ImportMetricParam{
						FilePath:      fmt.Sprintf("%s/endpoint_group/%s", metricPath, newFile.Name()),
						UserToken:     transImportParam.Token,
						Language:      transImportParam.Language,
						EndpointGroup: endpointGroup,
						Comparison:    comparison,
					}
					if err = monitor.ImportMetric(param); err != nil {
						log.Logger.Error("ImportMetric err", log.String("fileName", newFile.Name()), log.Error(err))
						return
					}
				}
				continue
			}
			comparison := "N"
			monitorType := file.Name()[:strings.LastIndex(file.Name(), ".")]
			if strings.Contains(file.Name(), "_comparison") {
				comparison = "Y"
				monitorType = file.Name()[:strings.LastIndex(file.Name(), "_comparison")]
			}
			param := monitor.ImportMetricParam{
				FilePath:    fmt.Sprintf("%s/%s", metricPath, file.Name()),
				UserToken:   transImportParam.Token,
				Language:    transImportParam.Language,
				MonitorType: monitorType,
				Comparison:  comparison,
			}
			if err = monitor.ImportMetric(param); err != nil {
				log.Logger.Error("ImportMetric err", log.String("fileName", file.Name()), log.Error(err))
				return
			}
		}
		log.Logger.Info("6-3. import metric monitorType and endpointGroup success!")
	} else {
		log.Logger.Info("6-3. import metric data empty!")
	}

	// 导入对象组阈值配置
	strategyPath := fmt.Sprintf("%s/monitor/strategy", transImportParam.DirPath)
	if strategyExist, err = tools.PathExist(strategyPath); err != nil {
		return
	}
	if strategyExist {
		var strategyFiles []fs.DirEntry
		if strategyFiles, err = os.ReadDir(strategyPath); err != nil {
			log.Logger.Error("ReadDir fail", log.String("strategyPath", strategyPath), log.Error(err))
			return
		}
		// 遍历文件和子目录
		for _, file := range strategyFiles {
			index := 0
			endIndex := 0
			if strings.HasPrefix(file.Name(), strategyEndpointGroupConst) {
				index = len(strategyEndpointGroupConst)
				endIndex = strings.LastIndex(file.Name(), ".")
				endpointGroup := file.Name()[index:endIndex]
				param := monitor.ImportStrategyParam{
					StrategyType: "group",
					Value:        endpointGroup,
					FilePath:     fmt.Sprintf("%s/%s", strategyPath, file.Name()),
					UserToken:    transImportParam.Token,
					Language:     transImportParam.Language,
				}
				if err = monitor.ImportStrategy(param); err != nil {
					log.Logger.Error("ImportStrategy err", log.String("fileName", file.Name()), log.Error(err))
					return
				}
			}
		}
		log.Logger.Info("6-4. import  monitor strategy endpointGroup success!")
	} else {
		log.Logger.Info("6-4. import  monitor strategy endpointGroup data empty!")
	}

	// 导入业务模版配置
	logMonitorTemplatePath := fmt.Sprintf("%s/monitor/log_monitor_template.json", transImportParam.DirPath)
	if logMonitorTemplateExist, err = tools.PathExist(logMonitorTemplatePath); err != nil {
		return
	}
	if logMonitorTemplateExist {
		err = monitor.ImportLogMonitorTemplate(logMonitorTemplatePath, transImportParam.Token, transImportParam.Language)
		if err != nil {
			log.Logger.Error("ImportLogMetricTemplate fail", log.Error(err))
			return
		}
		log.Logger.Info("6-5. import  log_metric_template success!")
	} else {
		log.Logger.Info("6-5. import  log_metric_template data empty!")
	}
	log.Logger.Info("6. importMonitorBaseConfig success end!!!")
	return
}

// 8.importTaskManComponentLibrary 导入组件库
func importTaskManComponentLibrary(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	// 判断是否要导入组件库
	log.Logger.Info("8. importTaskManComponentLibrary start!!!")
	if transImportParam.CurrentDetail == nil {
		err = fmt.Errorf("importTaskManTemplate CurrentDetail is empty")
		log.Logger.Error("err:", log.Error(err))
		return
	}
	if transImportParam.CurrentDetail.Input == "true" {
		// 导入组件库
		err = remote.ImportComponentLibrary(fmt.Sprintf("%s/component_library.json", transImportParam.DirPath), transImportParam.Token, transImportParam.Language)
		if err != nil {
			log.Logger.Error("ImportComponentLibrary err", log.Error(err))
			return
		}
	}
	log.Logger.Info("8. importTaskManComponentLibrary success end!!!")
	return
}

// 9、导入taskman模版
func importTaskManTemplate(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	log.Logger.Info("9. importTaskManTemplate start!!!")
	// 导入模版
	err = remote.ImportRequestTemplate(fmt.Sprintf("%s/request_template.json", transImportParam.DirPath), transImportParam.Token, transImportParam.Language)
	if err != nil {
		log.Logger.Error("ImportRequestTemplate fail", log.Error(err))
		return
	}
	log.Logger.Info("8. importTaskManTemplate success end!!!")
	return
}

// 11、导入监控业务配置、层级对象指标、层级对象阈值配置、自定义看板
func importMonitorServiceConfig(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {

	return
}

// sortDirEntry 文件排序,带有 _comparison 同环比指标文件放在后面执行
func sortDirEntry(files []fs.DirEntry) []fs.DirEntry {
	var metricList, comparisonMetricList []fs.DirEntry
	for _, file := range files {
		if strings.HasSuffix(file.Name(), "_comparison.json") {
			comparisonMetricList = append(comparisonMetricList, file)
		} else {
			metricList = append(metricList, file)
		}
	}
	metricList = append(metricList, comparisonMetricList...)
	return metricList
}
