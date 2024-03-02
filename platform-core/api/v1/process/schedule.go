package process

import (
	"context"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/timer"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/workflow"
	"github.com/gin-gonic/gin"
	"sync"
	"time"
)

func QueryProcScheduleList(c *gin.Context) {
	var param models.ProcScheduleQueryParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	result := []*models.ProcScheduleConfigObj{}
	middleware.ReturnData(c, result)
}

func CreateProcSchedule(c *gin.Context) {
	var param models.CreateProcScheduleParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	param.Operator = middleware.GetRequestUser(c)
	cronExpr, transErr := database.TransScheduleToCronExpr(param.ScheduleMode, param.ScheduleExpr)
	if transErr != nil {
		middleware.ReturnError(c, transErr)
		return
	}
	param.CronExpr = cronExpr
	newRow, err := database.CreateProcSchedule(c, &param)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if err = procScheduleTimer.AddCron(newRow.Id, param.CronExpr, handleProcScheduleJob, *newRow); err != nil {
		err = fmt.Errorf("register cron job:%s fail,%s", param.CronExpr, err.Error())
		if rollbackErr := database.DeleteProcSchedule(c, newRow.Id); rollbackErr != nil {
			log.Logger.Error("rollback create proc schedule config fail", log.String("id", newRow.Id), log.Error(rollbackErr))
		}
		middleware.ReturnError(c, err)
		return
	}
	procScheduleConfigMap.Store(newRow.Id, newRow)
	result := &models.ProcScheduleConfigObj{}
	middleware.ReturnData(c, result)
}

func StartProcSchedule(c *gin.Context) {
	var param []*models.ProcScheduleOperationParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	var err error
	for _, v := range param {
		if err = database.UpdateProcScheduleStatus(c, v.Id, models.ScheduleStatusReady); err != nil {
			break
		}
	}
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnSuccess(c)
	}
}

func StopProcSchedule(c *gin.Context) {
	var param []*models.ProcScheduleOperationParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	var err error
	for _, v := range param {
		if err = database.UpdateProcScheduleStatus(c, v.Id, models.ScheduleStatusStop); err != nil {
			break
		}
	}
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnSuccess(c)
	}
}

func DeleteProcSchedule(c *gin.Context) {
	var param []*models.ProcScheduleOperationParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	var err error
	for _, v := range param {
		if err = database.UpdateProcScheduleStatus(c, v.Id, models.ScheduleStatusDelete); err != nil {
			break
		}
		procScheduleTimer.Remove(v.Id)
		procScheduleConfigMap.Delete(v.Id)
	}
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnSuccess(c)
	}
}

func QueryProcScheduleInstance(c *gin.Context) {
	var param models.ProcScheduleInstQueryParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	result := []*models.ProcScheduleInstQueryObj{}
	middleware.ReturnData(c, result)
}

var (
	procScheduleTimer     *timer.CronSecond
	procScheduleConfigMap *sync.Map
)

func InitProcScheduleTimer() {
	procScheduleTimer = timer.New()
	procScheduleTimer.Start()
	// 加载数据库定时配置
	procScheduleConfigMap = new(sync.Map)
	go checkNewProcScheduleJob()
}

// 定时检测在其它实例受理的编排定时配置
func checkNewProcScheduleJob() {
	t := time.NewTicker(300 * time.Second).C
	for {
		<-t
		newConfigList, getErr := database.GetNewProcScheduleList()
		if getErr != nil {
			log.Logger.Error("checkNewProcScheduleJob get newly config list fail", log.Error(getErr))
			continue
		}
		if len(newConfigList) == 0 {
			continue
		}
		for _, newConfigRow := range newConfigList {
			if _, ok := procScheduleConfigMap.Load(newConfigRow.Id); !ok {
				if registerErr := procScheduleTimer.AddCron(newConfigRow.Id, newConfigRow.CronExpr, handleProcScheduleJob, *newConfigRow); registerErr != nil {
					log.Logger.Error("register proc schedule config fail", log.String("id", newConfigRow.Id), log.Error(registerErr))
				} else {
					procScheduleConfigMap.Store(newConfigRow.Id, newConfigRow)
				}
			}
		}
	}
}

func handleProcScheduleJob(unixTimestamp int64, param interface{}) {
	psConfig, ok := param.(models.ProcScheduleConfig)
	if !ok {
		log.Logger.Error("handleProcScheduleJob fail with assert param to ProcScheduleConfig")
		return
	}
	jobId := fmt.Sprintf("%s_%d", psConfig.Id, unixTimestamp)
	ctx := context.WithValue(context.Background(), models.TransactionIdHeader, jobId)
	// 检测状态
	pscStatus := database.GetProcScheduleConfigStatus(ctx, psConfig.Id)
	// 如果是stop态跳过
	if pscStatus == models.ScheduleStatusStop || pscStatus == "" {
		return
	}
	// 如果是delete就删除任务
	if pscStatus == models.ScheduleStatusDelete {
		procScheduleTimer.Remove(psConfig.Id)
		procScheduleConfigMap.Delete(psConfig.Id)
		return
	}
	log.Logger.Info("start handleProcScheduleJob", log.String("psConfigId", psConfig.Id), log.String("jobId", jobId))
	// 抢占任务
	if duplicateRow, err := database.NewProcScheduleJob(ctx, &psConfig, jobId); err != nil {
		log.Logger.Error("NewProcScheduleJob fail", log.String("psConfigId", psConfig.Id), log.Error(err))
		return
	} else if duplicateRow {
		log.Logger.Warn("NewProcScheduleJob insert with duplicate id,break", log.String("psConfigId", psConfig.Id))
		return
	}
	var err error
	defer func() {
		if err != nil {
			if updateJobErr := database.UpdateProcScheduleJob(ctx, jobId, "fail", err.Error(), ""); updateJobErr != nil {
				log.Logger.Error("handleProcScheduleJob create instance fail,but update schedule job message error", log.String("jobId", jobId), log.String("catchErr", err.Error()), log.Error(updateJobErr))
			}
		}
	}()
	operator := "systemCron"
	// preview
	previewData, previewErr := buildProcPreviewData(ctx, psConfig.ProcDefId, psConfig.EntityDataId, operator)
	if previewErr != nil {
		err = previewErr
		log.Logger.Error("handleProcScheduleJob fail with build proc preview data", log.String("psConfigId", psConfig.Id), log.Error(previewErr))
		return
	}
	// proc instance start
	procStartParam := models.ProcInsStartParam{
		EntityDataId:      psConfig.EntityDataId,
		EntityDisplayName: psConfig.EntityDataName,
		ProcDefId:         psConfig.ProcDefId,
		ProcessSessionId:  previewData.ProcessSessionId,
	}
	// 新增 proc_ins,proc_ins_node,proc_data_binding 纪录
	procInsId, workflowRow, workNodes, workLinks, createInsErr := database.CreateProcInstance(ctx, &procStartParam, operator)
	if createInsErr != nil {
		err = createInsErr
		log.Logger.Error("handleProcScheduleJob fail with create proc instance data", log.String("psConfigId", psConfig.Id), log.String("sessionId", previewData.ProcessSessionId), log.Error(createInsErr))
		return
	}
	// 初始化workflow并开始
	workObj := workflow.Workflow{ProcRunWorkflow: *workflowRow}
	workObj.Init(context.Background(), workNodes, workLinks)
	workflow.GlobalWorkflowMap.Store(workObj.Id, &workObj)
	go workObj.Start(&models.ProcOperation{CreatedBy: operator})
	if updateJobInsIdErr := database.UpdateProcScheduleJob(ctx, jobId, "done", "", procInsId); updateJobInsIdErr != nil {
		log.Logger.Error("handleProcScheduleJob done but update job proc instance id fail", log.String("jobId", jobId), log.String("procInsId", procInsId), log.Error(updateJobInsIdErr))
	} else {
		log.Logger.Info("done handleProcScheduleJob", log.String("psConfigId", psConfig.Id), log.String("jobId", jobId), log.String("procInsId", procInsId))
	}
}
