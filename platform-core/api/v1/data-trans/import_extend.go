package data_trans

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/encrypt"
	"go.uber.org/zap"
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
	strategyServiceGroupConst  = "strategy_service_group_"
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
	importFuncList = append(importFuncList, updateWebBaseImportSuccess)
	importFuncList = append(importFuncList, modifyNewEnvData)
	importFuncList = append(importFuncList, execWorkflow)
	importFuncList = append(importFuncList, importMonitorServiceConfig)
}

// StartTransImport 执行导入
func StartTransImport(ctx context.Context, param models.ExecImportParam) (err error) {
	var transImport *models.TransImportTable
	var localPath string
	var transImportAction *models.TransImportActionTable
	if transImport, err = database.GetTransImport(ctx, param.TransImportId); err != nil {
		log.Error(nil, log.LOGGER_APP, "GetTransImport err", zap.Error(err))
		return
	}
	if transImport == nil || transImport.Id == "" {
		// 下载物料包
		_, _, err = database.DownloadImportArtifactPackages(ctx, param.ExportNexusUrl, param.TransImportId)
		if err != nil {
			log.Error(nil, log.LOGGER_APP, "download import artifact packages fail", zap.String("url", param.ExportNexusUrl), zap.Error(err))
			return
		}
		// 文件解压
		if localPath, err = database.DecompressExportZip(ctx, param.ExportNexusUrl, param.TransImportId); err != nil {
			log.Error(nil, log.LOGGER_APP, "DecompressExportZip err", zap.Error(err))
			return
		}
		// 初始化导入
		if err = database.InitTransImport(ctx, param.TransImportId, param.ExportNexusUrl, localPath, param.Operator); err != nil {
			log.Error(nil, log.LOGGER_APP, "initTransImport err", zap.Error(err))
			return
		}
	} else {
		localPath = fmt.Sprintf(database.TempTransImportDir, transImport.Id)
	}
	if transImportAction, err = database.GetLatestTransImportAction(ctx, param.TransImportId); err != nil {
		log.Error(nil, log.LOGGER_APP, "GetLatestTransImportAction err", zap.Error(err))
		return
	}
	actionParam := &models.CallTransImportActionParam{
		TransImportId:        param.TransImportId,
		Action:               string(models.TransImportStatusStart),
		Operator:             param.Operator,
		ActionId:             transImportAction.Id,
		DirPath:              localPath,
		Token:                param.Token,
		Language:             param.Language,
		WebStep:              param.WebStep,
		ImportCustomFormData: param.ImportCustomFormData,
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

// 10. 修改新环境数据

// 开始执行
// 11、开始执行编排(创建资源、初始化资源、应用部署)
// 继续导入
// 12、导入监控业务配置、层级对象指标、层级对象阈值配置、自定义看板、关键字层级对象
func doImportAction(ctx context.Context, callParam *models.CallTransImportActionParam) (err error) {
	transImportJobParam, getConfigErr := database.GetTransImportWithDetail(ctx, callParam.TransImportId, false)
	if getConfigErr != nil {
		err = getConfigErr
		log.Error(nil, log.LOGGER_APP, "GetTransImportWithDetail err", zap.Error(err))
		return
	}
	if err = database.RecordTransImportAction(ctx, callParam); err != nil {
		err = fmt.Errorf("record trans import action table fail,%s ", err.Error())
		log.Error(nil, log.LOGGER_APP, "RecordTransImportAction err", zap.Error(err))
		return
	}
	transImportJobParam.DirPath = callParam.DirPath
	transImportJobParam.Token = callParam.Token
	transImportJobParam.Language = callParam.Language
	transImportJobParam.Operator = callParam.Operator
	transImportJobParam.ImportCustomFormData = callParam.ImportCustomFormData
	if callParam.Action == string(models.TransImportActionStart) {
		if checkImportHasExit(ctx, callParam.TransImportId) {
			return
		}
		var currentStep int
		for _, detailRow := range transImportJobParam.Details {
			if detailRow.Status == string(models.TransImportStatusNotStart) {
				currentStep = detailRow.Step
				break
			}
		}
		if currentStep > 0 {
			transImportJobParam.CurrentDetail = transImportJobParam.Details[currentStep-1]
		}
		if currentStep == int(models.TransImportStepWebBaseImportSuccess) && callParam.WebStep == int(models.ImportWebDisplayStepTwo) {
			// 第二步完成
			if err = callImportFunc(ctx, transImportJobParam, updateWebBaseImportSuccess); err != nil {
				return
			}
		} else if currentStep == int(models.TransImportStepModifyNewEnvData) && callParam.WebStep == int(models.ImportWebDisplayStepThree) {
			// 完成第三步
			if err = callImportFunc(ctx, transImportJobParam, modifyNewEnvData); err != nil {
				return
			}
			// 触发第四步编排执行
			currentStep++
			transImportJobParam.CurrentDetail = transImportJobParam.Details[currentStep-1]
			if err = callImportFunc(ctx, transImportJobParam, execWorkflow); err != nil {
				return
			}
		} else if currentStep == int(models.TransImportStepMonitorBusiness) && callParam.WebStep == int(models.ImportWebDisplayStepFour) {
			if err = callImportFunc(ctx, transImportJobParam, importMonitorServiceConfig); err != nil {
				return
			}
		} else {
			for currentStep <= int(models.TransImportStepRequestTemplate) {
				// 每一步都需要判断导入最新状态是否终止
				if checkImportHasExit(ctx, callParam.TransImportId) {
					return
				}
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
		log.Error(nil, log.LOGGER_APP, "doImportAction fail", log.JsonObj("callParam", callParam), zap.Error(err))
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
		if transImportJobParam.CurrentDetail.Step != int(models.TransImportStepInitWorkflow) {
			database.UpdateTransImportDetailStatus(ctx, transImportJobParam.TransImport.Id, transImportJobParam.CurrentDetail.Id, "success", output, "")
		}
	}
	return
}

// 1、导入角色
func importRole(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	// 解析role.json,导入角色
	log.Info(nil, log.LOGGER_APP, "1. importRole start!!!")
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
			log.Error(nil, log.LOGGER_APP, "RegisterLocalRole err", zap.Error(err))
			return
		}
		if response.Status != "OK" || response.Data.ID == "" {
			err = fmt.Errorf("RegisterLocalRole fail,msg:%s", response.Message)
			log.Error(nil, log.LOGGER_APP, "RegisterLocalRole fail", zap.String("roleName", role.Name), zap.String("msg", response.Message))
			return
		}
	}
	log.Info(nil, log.LOGGER_APP, "1. importRole success end!!!")
	return
}

// 4、导入编排
func importWorkflow(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	// 解析workflow.json,导入编排
	log.Info(nil, log.LOGGER_APP, "4. importWorkflow start!!!")
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
		log.Info(nil, log.LOGGER_APP, "importWorkflow data empty!")
		return
	}
	if err = database.ParseJsonData(workflowPath, &procDefList); err != nil {
		return
	}
	if len(procDefList) > 0 {
		param := models.ProcDefImportDto{
			Ctx:           ctx,
			InputList:     procDefList,
			Operator:      transImportParam.Operator,
			UserToken:     transImportParam.Token,
			Language:      transImportParam.Language,
			IsTransImport: true,
		}
		if importResult, err = process.ProcDefImport(param); err != nil {
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
					log.Error(nil, log.LOGGER_APP, "importWorkflow fail", zap.String("name", data.ProcDefName), zap.String("errMsg", errMsg))
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
					log.Error(nil, log.LOGGER_APP, "importWorkflow Deployed fail", zap.String("name", procDef.Name), zap.String("errMsg", errMsg))
					return
				}
			}
		}
	}
	log.Info(nil, log.LOGGER_APP, "4. importWorkflow success end!!!")
	return
}

// 5、导入批量执行
func importBatchExecution(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	log.Info(nil, log.LOGGER_APP, "5. importBatchExecution start!!!")
	var batchExecutionTemplateList []*models.BatchExecutionTemplate
	var exist bool
	batchExecutionPath := fmt.Sprintf("%s/batch_execution.json", transImportParam.DirPath)
	if exist, err = tools.PathExist(batchExecutionPath); err != nil {
		return
	}
	if !exist {
		log.Info(nil, log.LOGGER_APP, "importBatchExecution data empty!")
		return
	}
	if err = database.ParseJsonData(batchExecutionPath, &batchExecutionTemplateList); err != nil {
		return
	}
	if len(batchExecutionTemplateList) > 0 {
		if err = database.ImportTemplate(ctx, transImportParam.Operator, batchExecutionTemplateList); err != nil {
			log.Error(nil, log.LOGGER_APP, "importBatchExecution ImportTemplate fail", zap.Error(err))
			return
		}
	}
	log.Info(nil, log.LOGGER_APP, "5. importBatchExecution success end!!!")
	return
}

// 7、导入监控基础类型、对象组、基础类型指标、对象组指标、对象组阈值配置、业务配置模版
func importMonitorBaseConfig(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	log.Info(nil, log.LOGGER_APP, "6. importMonitorBaseConfig start!!!")
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
			log.Error(nil, log.LOGGER_APP, "ImportMonitorType fail", zap.Error(err))
			return
		}
		if response.Status != "OK" {
			err = fmt.Errorf("ImportMonitorType %s", response.Message)
			log.Error(nil, log.LOGGER_APP, "ImportMonitorType fail", zap.String("msg", response.Message))
			return
		}
		log.Info(nil, log.LOGGER_APP, "6-1. import monitorType success!")
	} else {
		log.Info(nil, log.LOGGER_APP, "6-1. import monitorType data empty!")
	}
	// 导入对象组
	endpointGroupPath := fmt.Sprintf("%s/monitor/endpoint_group.json", transImportParam.DirPath)
	if endpointGroupExist, err = tools.PathExist(endpointGroupPath); err != nil {
		return
	}
	if endpointGroupExist {
		if err = monitor.ImportEndpointGroup(endpointGroupPath, transImportParam.Token, transImportParam.Language); err != nil {
			log.Error(nil, log.LOGGER_APP, "ImportEndpointGroup fail", zap.Error(err))
			return
		}
		log.Info(nil, log.LOGGER_APP, "6-2. import endpointGroup success!")
	} else {
		log.Info(nil, log.LOGGER_APP, "6-2. import endpointGroup date empty!")
	}

	// 导入基础类型指标、对象组指标
	metricPath := fmt.Sprintf("%s/monitor/metric", transImportParam.DirPath)
	if metricExist, err = tools.PathExist(metricPath); err != nil {
		return
	}
	if metricExist {
		var files, childFiles []fs.DirEntry
		if files, err = os.ReadDir(metricPath); err != nil {
			log.Error(nil, log.LOGGER_APP, "ReadDir fail", zap.String("metricPath", metricPath), zap.Error(err))
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
					log.Error(nil, log.LOGGER_APP, "ReadDir fail", zap.Error(err))
					return
				}
				childFiles = sortDirEntry(childFiles)
				for _, newFile := range childFiles {
					comparison := "N"
					endpointGroup := newFile.Name()[:strings.LastIndex(newFile.Name(), ".")]
					if strings.Contains(newFile.Name(), "_comparison") {
						comparison = "Y"
						endpointGroup = newFile.Name()[:strings.LastIndex(newFile.Name(), "_comparison")]
					}
					param := monitor.ImportMetricParam{
						FilePath:      fmt.Sprintf("%s/endpoint_group/%s", metricPath, newFile.Name()),
						UserToken:     transImportParam.Token,
						Language:      transImportParam.Language,
						EndpointGroup: endpointGroup,
						MonitorType:   "process",
						Comparison:    comparison,
					}
					if err = monitor.ImportMetric(param); err != nil {
						err = fmt.Errorf("%s,fileName:%s", err.Error(), newFile.Name())
						log.Error(nil, log.LOGGER_APP, "ImportMetric err", zap.String("fileName", newFile.Name()), zap.Error(err))
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
				err = fmt.Errorf("%s,fileName:%s", err.Error(), file.Name())
				log.Error(nil, log.LOGGER_APP, "ImportMetric err", zap.String("fileName", file.Name()), zap.Error(err))
				return
			}
		}
		log.Info(nil, log.LOGGER_APP, "6-3. import metric monitorType and endpointGroup success!")
	} else {
		log.Info(nil, log.LOGGER_APP, "6-3. import metric data empty!")
	}

	// 导入对象组阈值配置
	strategyPath := fmt.Sprintf("%s/monitor/strategy", transImportParam.DirPath)
	if strategyExist, err = tools.PathExist(strategyPath); err != nil {
		return
	}
	if strategyExist {
		var strategyFiles []fs.DirEntry
		if strategyFiles, err = os.ReadDir(strategyPath); err != nil {
			log.Error(nil, log.LOGGER_APP, "ReadDir fail", zap.String("strategyPath", strategyPath), zap.Error(err))
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
					log.Error(nil, log.LOGGER_APP, "ImportStrategy err", zap.String("fileName", file.Name()), zap.Error(err))
					return
				}
			}
		}
		log.Info(nil, log.LOGGER_APP, "6-4. import  monitor strategy endpointGroup success!")
	} else {
		log.Info(nil, log.LOGGER_APP, "6-4. import  monitor strategy endpointGroup data empty!")
	}

	// 导入业务模版配置
	logMonitorTemplatePath := fmt.Sprintf("%s/monitor/log_monitor_template.json", transImportParam.DirPath)
	if logMonitorTemplateExist, err = tools.PathExist(logMonitorTemplatePath); err != nil {
		return
	}
	if logMonitorTemplateExist {
		err = monitor.ImportLogMonitorTemplate(logMonitorTemplatePath, transImportParam.Token, transImportParam.Language)
		if err != nil {
			log.Error(nil, log.LOGGER_APP, "ImportLogMetricTemplate fail", zap.Error(err))
			return
		}
		log.Info(nil, log.LOGGER_APP, "6-5. import  log_metric_template success!")
	} else {
		log.Info(nil, log.LOGGER_APP, "6-5. import  log_metric_template data empty!")
	}
	log.Info(nil, log.LOGGER_APP, "6. importMonitorBaseConfig success end!!!")
	return
}

// 8.importTaskManComponentLibrary 导入组件库
func importTaskManComponentLibrary(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	// 判断是否要导入组件库
	var input string
	var pathExist bool
	log.Info(nil, log.LOGGER_APP, "8. importTaskManComponentLibrary start!!!")
	componentLibraryPath := fmt.Sprintf("%s/component_library.json", transImportParam.DirPath)
	if transImportParam.CurrentDetail == nil {
		err = fmt.Errorf("importTaskManTemplate CurrentDetail is empty")
		log.Error(nil, log.LOGGER_APP, "err:", zap.Error(err))
		return
	}
	if input, err = database.GetTransImportDetailInput(ctx, transImportParam.CurrentDetail.Id); err != nil {
		return
	}
	if input == "true" {
		// 导入时候先检查 组件库路径是否存在,不存在直接跳过,不报错
		if pathExist, err = tools.PathExist(componentLibraryPath); err != nil || !pathExist {
			log.Info(nil, log.LOGGER_APP, "pathExist", zap.Error(err), zap.String("path", componentLibraryPath), zap.Bool("pathExist", pathExist))
			err = nil
			return
		}
		// 导入组件库
		err = remote.ImportComponentLibrary(componentLibraryPath, transImportParam.Token, transImportParam.Language)
		if err != nil {
			log.Error(nil, log.LOGGER_APP, "importTaskManComponentLibrary", zap.Error(err))
			return
		}
		log.Info(nil, log.LOGGER_APP, "8. importTaskManComponentLibrary success end!!!")
	} else {
		log.Info(nil, log.LOGGER_APP, "8. importTaskManComponentLibrary data empty!!!")
	}
	return
}

// 9、导入taskman模版
func importTaskManTemplate(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	log.Info(nil, log.LOGGER_APP, "9. importTaskManTemplate start!!!")
	// 导入模版
	err = remote.ImportRequestTemplate(fmt.Sprintf("%s/request_template.json", transImportParam.DirPath), transImportParam.Token, transImportParam.Language)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "ImportRequestTemplate fail", zap.Error(err))
		return
	}
	log.Info(nil, log.LOGGER_APP, "9. importTaskManTemplate success end!!!")
	return
}

// 10、页面第二步导入数据成功,点击下一步触发
func updateWebBaseImportSuccess(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	log.Info(nil, log.LOGGER_APP, "10. updateWebBaseImportSuccess start!!!")
	err = database.UpdateTransImportDetailStatus(ctx, transImportParam.TransImport.Id, transImportParam.CurrentDetail.Id, string(models.TransImportStatusSuccess), "", "")
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "UpdateTransImportDetailStatus fail", zap.Error(err))
		return
	}
	log.Info(nil, log.LOGGER_APP, "10. updateWebBaseImportSuccess end!!!")
	return
}

// 11、修改新环境数据
func modifyNewEnvData(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	log.Info(nil, log.LOGGER_APP, "11. modifyNewEnvData start!!!")
	if transImportParam.ImportCustomFormData == nil {
		err = fmt.Errorf("modify new environment is empty")
		log.Error(nil, log.LOGGER_APP, "ImportCustomFormData err", zap.Error(err))
		return
	}
	transImportParam.ImportCustomFormData.WecubeHost1Pwd = transImportParam.ImportCustomFormData.WecubeHost1Password
	transImportParam.ImportCustomFormData.WecubeHost2Pwd = transImportParam.ImportCustomFormData.WecubeHost2Password
	// 密码加密
	if !strings.HasPrefix(transImportParam.ImportCustomFormData.WecubeHost1Password, models.AESPrefix) {
		transImportParam.ImportCustomFormData.WecubeHost1Password = models.AESPrefix + encrypt.EncryptWithAesECB(transImportParam.ImportCustomFormData.WecubeHost1Password,
			models.Config.Plugin.ResourcePasswordSeed, models.Config.Plugin.ResourcePasswordSeed)
	}
	if transImportParam.ImportCustomFormData.WecubeHost2Password != "" && !strings.HasPrefix(transImportParam.ImportCustomFormData.WecubeHost2Password, models.AESPrefix) {
		transImportParam.ImportCustomFormData.WecubeHost2Password = models.AESPrefix + encrypt.EncryptWithAesECB(transImportParam.ImportCustomFormData.WecubeHost2Password,
			models.Config.Plugin.ResourcePasswordSeed, models.Config.Plugin.ResourcePasswordSeed)
	}
	byteArr, _ := json.Marshal(transImportParam.ImportCustomFormData)
	if err = database.UpdateTransImportDetailInput(ctx, transImportParam.TransImport.Id, models.TransImportStepModifyNewEnvData, string(byteArr)); err != nil {
		log.Error(nil, log.LOGGER_APP, "UpdateTransImportDetailInput err", zap.Error(err))
		return
	}
	// 更新cmdb数据
	if err = database.UpdateTransImportCMDBData(ctx, transImportParam); err != nil {
		log.Error(nil, log.LOGGER_APP, "UpdateTransImportCMDBData err", zap.Error(err))
		return
	}
	log.Info(nil, log.LOGGER_APP, "11. modifyNewEnvData success end!!!")
	return
}

// 13、导入监控业务配置、层级对象指标、层级对象阈值配置、自定义看板、关键字层级对象
func importMonitorServiceConfig(ctx context.Context, transImportParam *models.TransImportJobParam) (output string, err error) {
	log.Info(nil, log.LOGGER_APP, "13. importMonitorServiceConfig start!!!")
	var logMonitorExist, serviceGroupMetricExist, strategyExist, dashboardExist, logKeywordExist bool
	// 导入监控业务配置 (说明: 业务配置导入在层级对象指标之前导入,层级对象指标导入做了防止重复处理)
	logMonitorPath := fmt.Sprintf("%s/monitor/log_monitor", transImportParam.DirPath)
	if logMonitorExist, err = tools.PathExist(logMonitorPath); err != nil {
		return
	}
	if logMonitorExist {
		var files []fs.DirEntry
		if files, err = os.ReadDir(logMonitorPath); err != nil {
			log.Error(nil, log.LOGGER_APP, "ReadDir fail", zap.String("logMonitorPath", logMonitorPath), zap.Error(err))
			return
		}
		// 遍历文件和子目录
		for _, file := range files {
			serviceGroup := file.Name()[:strings.LastIndex(file.Name(), ".")]
			err = monitor.ImportLogMonitor(fmt.Sprintf("%s/%s", logMonitorPath, file.Name()), transImportParam.Token, transImportParam.Language, serviceGroup)
			if err != nil {
				log.Error(nil, log.LOGGER_APP, "ImportLogMonitor err", zap.String("fileName", file.Name()))
				return
			}
		}
		log.Info(nil, log.LOGGER_APP, "13-1. import log_monitor success!")
	} else {
		log.Info(nil, log.LOGGER_APP, "13-1. import log_monitor data empty!")
	}

	// 导入层级对象指标
	serviceGroupMetricPath := fmt.Sprintf("%s/monitor/metric/service_group", transImportParam.DirPath)
	if serviceGroupMetricExist, err = tools.PathExist(serviceGroupMetricPath); err != nil {
		return
	}
	if serviceGroupMetricExist {
		var files []fs.DirEntry
		if files, err = os.ReadDir(serviceGroupMetricPath); err != nil {
			log.Error(nil, log.LOGGER_APP, "ReadDir fail", zap.String("serviceGroupMetricPath", serviceGroupMetricPath), zap.Error(err))
			return
		}
		files = sortDirEntry(files)
		// 遍历文件和子目录
		for _, file := range files {
			comparison := "N"
			serviceGroup := file.Name()[:strings.LastIndex(file.Name(), ".")]
			if strings.Contains(file.Name(), "_comparison") {
				comparison = "Y"
				serviceGroup = file.Name()[:strings.LastIndex(file.Name(), "_comparison")]
			}
			param := monitor.ImportMetricParam{
				FilePath:     fmt.Sprintf("%s/%s", serviceGroupMetricPath, file.Name()),
				UserToken:    transImportParam.Token,
				Language:     transImportParam.Language,
				ServiceGroup: serviceGroup,
				MonitorType:  "process",
				Comparison:   comparison,
			}
			if err = monitor.ImportMetric(param); err != nil {
				log.Error(nil, log.LOGGER_APP, "ImportMetric err", zap.String("fileName", file.Name()), zap.Error(err))
				return
			}
		}
		log.Info(nil, log.LOGGER_APP, "13-2. import service_group metric success!")
	} else {
		log.Info(nil, log.LOGGER_APP, "13-2. import service_group metric data empty!")
	}

	// 导入层级对象阈值配置
	strategyExist = false
	strategyPath := fmt.Sprintf("%s/monitor/strategy", transImportParam.DirPath)
	if strategyExist, err = tools.PathExist(strategyPath); err != nil {
		return
	}
	if strategyExist {
		var strategyFiles []fs.DirEntry
		if strategyFiles, err = os.ReadDir(strategyPath); err != nil {
			log.Error(nil, log.LOGGER_APP, "ReadDir fail", zap.String("strategyPath", strategyPath), zap.Error(err))
			return
		}
		// 遍历文件和子目录
		for _, file := range strategyFiles {
			index := 0
			endIndex := 0
			if strings.HasPrefix(file.Name(), strategyServiceGroupConst) {
				index = len(strategyServiceGroupConst)
				endIndex = strings.LastIndex(file.Name(), ".")
				serviceGroup := file.Name()[index:endIndex]
				param := monitor.ImportStrategyParam{
					StrategyType: "service",
					Value:        serviceGroup,
					FilePath:     fmt.Sprintf("%s/%s", strategyPath, file.Name()),
					UserToken:    transImportParam.Token,
					Language:     transImportParam.Language,
				}
				if err = monitor.ImportStrategy(param); err != nil {
					log.Error(nil, log.LOGGER_APP, "ImportStrategy err", zap.String("fileName", file.Name()), zap.Error(err))
					return
				}
			}
		}
		log.Info(nil, log.LOGGER_APP, "13-3. import strategy service_group success!")
	} else {
		log.Info(nil, log.LOGGER_APP, "13-3. import strategy service_group data empty!")
	}
	// 导入自定义看板
	dashboardPath := fmt.Sprintf("%s/monitor/dashboard", transImportParam.DirPath)
	if dashboardExist, err = tools.PathExist(dashboardPath); err != nil {
		return
	}
	if dashboardExist {
		var files []fs.DirEntry
		if files, err = os.ReadDir(dashboardPath); err != nil {
			log.Error(nil, log.LOGGER_APP, "ReadDir fail", zap.String("dashboardPath", dashboardPath), zap.Error(err))
			return
		}
		// 遍历文件和子目录
		for _, file := range files {
			if err = monitor.ImportDashboard(fmt.Sprintf("%s/%s", dashboardPath, file.Name()), transImportParam.Token, transImportParam.Language); err != nil {
				log.Error(nil, log.LOGGER_APP, "ImportDashboard err", zap.String("fileName", file.Name()), zap.Error(err))
				return
			}
		}
		log.Info(nil, log.LOGGER_APP, "13-4. import dashboard success!")
	} else {
		log.Info(nil, log.LOGGER_APP, "13-4. import dashboard data empty!")
	}

	// 导入关键字(包含层级对象)
	logKeywordPath := fmt.Sprintf("%s/monitor/keyword", transImportParam.DirPath)
	if logKeywordExist, err = tools.PathExist(logKeywordPath); err != nil {
		return
	}
	if logKeywordExist {
		var files []fs.DirEntry
		if files, err = os.ReadDir(logKeywordPath); err != nil {
			log.Error(nil, log.LOGGER_APP, "ReadDir fail", zap.String("logKeywordPath", logKeywordPath), zap.Error(err))
			return
		}
		// 遍历文件和子目录
		for _, file := range files {
			serviceGroup := file.Name()[:strings.LastIndex(file.Name(), ".")]
			if err = monitor.ImportLogKeyword(fmt.Sprintf("%s/%s", logKeywordPath, file.Name()), transImportParam.Token, transImportParam.Language, serviceGroup); err != nil {
				log.Error(nil, log.LOGGER_APP, "ImportLogKeyword err", zap.String("fileName", file.Name()), zap.Error(err))
				return
			}
		}
		log.Info(nil, log.LOGGER_APP, "13-5. import log_keyword success!")
	} else {
		log.Info(nil, log.LOGGER_APP, "13-5. import log_keyword data empty!")
	}
	log.Info(nil, log.LOGGER_APP, "13. importMonitorServiceConfig end!!!")
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

// checkImportHasExit 判断导入是否终止
func checkImportHasExit(ctx context.Context, transImportId string) bool {
	var transImportAction *models.TransImportActionTable
	var err error
	if transImportAction, err = database.GetLatestTransImportAction(ctx, transImportId); err != nil {
		return false
	}
	if transImportAction == nil {
		return false
	}
	if transImportAction.Action == string(models.TransImportActionExit) {
		log.Info(nil, log.LOGGER_APP, "import has exit", zap.String("transImportId", transImportId))
		return true
	}
	return false
}
