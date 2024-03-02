package database

import (
	"context"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"strconv"
	"strings"
	"time"
)

func CreateProcSchedule(ctx context.Context, param *models.CreateProcScheduleParam) (result *models.ProcScheduleConfig, err error) {
	result = &models.ProcScheduleConfig{
		Id:             "psc_" + guid.CreateGuid(),
		ProcDefId:      param.ProcDefId,
		ProcDefName:    param.ProcDefName,
		Status:         models.ScheduleStatusReady,
		EntityDataId:   param.EntityDataId,
		EntityDataName: param.EntityDataName,
		ScheduleMode:   param.ScheduleMode,
		ScheduleExpr:   param.ScheduleExpr,
		CreatedBy:      param.Operator,
		CreatedTime:    time.Now(),
	}
	_, err = db.MysqlEngine.Context(ctx).Exec("insert into proc_schedule_config(id,proc_def_id,proc_def_key,proc_def_name,status,entity_data_id,entity_type_id,entity_data_name,schedule_mode,schedule_expr,cron_expr,created_by,created_time) values (?,?,?,?,?,?,?,?,?,?,?,?,?)",
		result.Id, result.ProcDefId, result.ProcDefKey, result.ProcDefName, result.Status, result.EntityDataId, result.EntityTypeId, result.EntityDataName, result.ScheduleMode, result.ScheduleExpr, result.CronExpr, result.CreatedBy, result.CreatedTime)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func DeleteProcSchedule(ctx context.Context, id string) (err error) {
	_, err = db.MysqlEngine.Context(ctx).Exec("delete from proc_schedule_config where id=?", id)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

// GetNewProcScheduleList 获取1小时内新增加的定时配置
func GetNewProcScheduleList() (result []*models.ProcScheduleConfig, err error) {
	lastHourTime := time.Unix(time.Now().Unix()-3600, 0)
	err = db.MysqlEngine.SQL("select * from proc_schedule_config where status<>'Deleted' and created_time>?", lastHourTime).Find(&result)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
	}
	return
}

func UpdateProcScheduleStatus(ctx context.Context, id, status string) (err error) {
	_, err = db.MysqlEngine.Context(ctx).Exec("update proc_schedule_config set status=? where id=?", status, id)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func GetProcScheduleConfigStatus(ctx context.Context, id string) (status string) {
	queryRows, err := db.MysqlEngine.Context(ctx).QueryString("select status from proc_schedule_config where id=?", id)
	if err != nil {
		log.Logger.Error("GetProcScheduleConfigStatus query status fail", log.String("id", id), log.Error(err))
		return
	}
	if len(queryRows) > 0 {
		status = queryRows[0]["status"]
	}
	return
}

func NewProcScheduleJob(ctx context.Context, psConfig *models.ProcScheduleConfig, newId string) (duplicateRow bool, err error) {
	_, insertErr := db.MysqlEngine.Context(ctx).Exec("insert into proc_schedule_job(id,schedule_config_id,status,handle_by,created_time) values (?,?,?,?,?)",
		newId, psConfig.Id, "ready", models.Config.HostIp, time.Now())
	if insertErr != nil {
		if strings.Contains(strings.ToLower(insertErr.Error()), "duplicate") {
			duplicateRow = true
		} else {
			err = insertErr
		}
	}
	return
}

func UpdateProcScheduleJob(ctx context.Context, jobId, status, errorMsg, procInsId string) (err error) {
	nowTime := time.Now()
	if status == "fail" {
		_, err = db.MysqlEngine.Context(ctx).Exec("update proc_schedule_job set status=?,error_msg=?,updated_time=? where id=?", status, errorMsg, nowTime, jobId)
	} else {
		_, err = db.MysqlEngine.Context(ctx).Exec("update proc_schedule_job set status=?,proc_ins_id=?,updated_time=? where id=?", status, procInsId, nowTime, jobId)
	}
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func TransScheduleToCronExpr(scheduleMode, scheduleExpr string) (cronExpr string, err error) {
	var monthDay, weekDay, hour, min, sec int
	if scheduleMode == "Monthly" {
		exprSplit := strings.Split(scheduleExpr, " ")
		if len(exprSplit) != 2 {
			err = fmt.Errorf("scheduleExpr:'%s' illegal with %s ", scheduleExpr, scheduleMode)
			return
		}
		if monthDay, err = strconv.Atoi(exprSplit[0]); err != nil {
			err = fmt.Errorf("month day:%s illegal", exprSplit[0])
			return
		}
		if hour, min, sec, err = getHourMinSec(exprSplit[1]); err != nil {
			return
		}
		cronExpr = fmt.Sprintf("%d %d %d %d * ? *", sec, min, hour, monthDay)
	} else if scheduleMode == "Weekly" {
		exprSplit := strings.Split(scheduleExpr, " ")
		if len(exprSplit) != 2 {
			err = fmt.Errorf("scheduleExpr:'%s' illegal with %s ", scheduleExpr, scheduleMode)
			return
		}
		if weekDay, err = strconv.Atoi(exprSplit[0]); err != nil {
			err = fmt.Errorf("week day:%s illegal", exprSplit[0])
			return
		}
		if weekDay < 1 || weekDay > 7 {
			err = fmt.Errorf("week day:%d illegal", weekDay)
			return
		}
		if hour, min, sec, err = getHourMinSec(exprSplit[1]); err != nil {
			return
		}
		cronExpr = fmt.Sprintf("%d %d %d ? * %d *", sec, min, hour, weekDay)
	} else if scheduleMode == "Daily" {
		if hour, min, sec, err = getHourMinSec(scheduleExpr); err != nil {
			return
		}
		cronExpr = fmt.Sprintf("%d %d %d * * ? *", sec, min, hour)
	} else if scheduleMode == "Hourly" {
		if hour, min, sec, err = getHourMinSec(scheduleExpr); err != nil {
			return
		}
		cronExpr = fmt.Sprintf("%d %d * * * ? *", sec, min)
	} else {
		err = fmt.Errorf("schedule mode:%s illegal", scheduleMode)
	}
	return
}

func getHourMinSec(input string) (hour, min, sec int, err error) {
	inputSplit := strings.Split(input, ":")
	if len(inputSplit) == 2 {
		if min, err = strconv.Atoi(inputSplit[0]); err != nil {
			err = fmt.Errorf("minute %s illegal", inputSplit[0])
			return
		}
		if sec, err = strconv.Atoi(inputSplit[1]); err != nil {
			err = fmt.Errorf("second %s illegal", inputSplit[1])
			return
		}
	} else if len(inputSplit) == 3 {
		if hour, err = strconv.Atoi(inputSplit[0]); err != nil {
			err = fmt.Errorf("hour %s illegal", inputSplit[0])
			return
		}
		if min, err = strconv.Atoi(inputSplit[1]); err != nil {
			err = fmt.Errorf("minute %s illegal", inputSplit[1])
			return
		}
		if sec, err = strconv.Atoi(inputSplit[2]); err != nil {
			err = fmt.Errorf("second %s illegal", inputSplit[2])
			return
		}
	} else {
		err = fmt.Errorf("hour:min:sec expr:%s illegal", input)
		return
	}
	if hour < 0 || hour > 23 {
		err = fmt.Errorf("hour:%d illegal", hour)
		return
	}
	if min < 0 || min > 59 {
		err = fmt.Errorf("minute:%d illegal", min)
		return
	}
	if sec < 0 || sec > 59 {
		err = fmt.Errorf("second:%d illegal", sec)
		return
	}
	return
}
