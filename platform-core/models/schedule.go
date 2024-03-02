package models

import "time"

const (
	ScheduleStatusReady  = "Ready"
	ScheduleStatusStop   = "Stopped"
	ScheduleStatusDelete = "Deleted"
)

type ProcScheduleConfig struct {
	Id             string    `json:"id" xorm:"id"`                           // 唯一标识
	ProcDefId      string    `json:"procDefId" xorm:"proc_def_id"`           // 编排定义id
	ProcDefKey     string    `json:"procDefKey" xorm:"proc_def_key"`         // 编排定义key
	ProcDefName    string    `json:"procDefName" xorm:"proc_def_name"`       // 编排定义名称
	Status         string    `json:"status" xorm:"status"`                   // 状态->Ready(正在运行) | Stopped(暂停) | Deleted(删除)
	EntityDataId   string    `json:"entityDataId" xorm:"entity_data_id"`     // 根数据id
	EntityTypeId   string    `json:"entityTypeId" xorm:"entity_type_id"`     // 根数据类型
	EntityDataName string    `json:"entityDataName" xorm:"entity_data_name"` // 根数据名称
	ScheduleMode   string    `json:"scheduleMode" xorm:"schedule_mode"`      // 定时模式->Monthly(每月) | Weekly(每周) | Daily(每天) | Hourly(每小时)
	ScheduleExpr   string    `json:"scheduleExpr" xorm:"schedule_expr"`      // 时间表达式
	CronExpr       string    `json:"cronExpr" xorm:"cron_expr"`              // cron表达式
	ExecTimes      int       `json:"execTimes" xorm:"exec_times"`            // 执行次数
	CreatedBy      string    `json:"createdBy" xorm:"created_by"`            // 创建人
	CreatedTime    time.Time `json:"createdTime" xorm:"created_time"`        // 创建时间
	UpdatedBy      string    `json:"updatedBy" xorm:"updated_by"`            // 更新人
	UpdatedTime    time.Time `json:"updatedTime" xorm:"updated_time"`        // 更新时间
}

type ProcScheduleJob struct {
	Id               string    `json:"id" xorm:"id"`                               // 定时配置id加时间戳
	ScheduleConfigId string    `json:"scheduleConfigId" xorm:"schedule_config_id"` // 定时配置id
	ProcInsId        string    `json:"procInsId" xorm:"proc_ins_id"`               // 编排实例id
	Status           string    `json:"status" xorm:"status"`                       // 状态->ready(准备启动) | fail(报错) | done(已完成)
	HandleBy         string    `json:"handleBy" xorm:"handle_by"`                  // 处理的主机
	ErrorMsg         string    `json:"errorMsg" xorm:"error_msg"`                  // 错误信息
	CreatedTime      time.Time `json:"createdTime" xorm:"created_time"`            // 创建时间
	UpdatedTime      time.Time `json:"updatedTime" xorm:"updated_time"`            // 更新时间
}

type ProcScheduleQueryParam struct {
	ScheduleMode string `json:"scheduleMode"`
	Owner        string `json:"owner"`
	StartTime    string `json:"startTime"`
	EndTime      string `json:"endTime"`
}

type CreateProcScheduleParam struct {
	ScheduleMode   string `json:"scheduleMode"`
	ScheduleExpr   string `json:"scheduleExpr"`
	ProcDefId      string `json:"procDefId"`
	ProcDefName    string `json:"procDefName"`
	EntityDataId   string `json:"entityDataId"`
	EntityDataName string `json:"entityDataName"`
	CronExpr       string `json:"-"`
	Operator       string `json:"-"`
}

type ProcScheduleConfigObj struct {
	Id                       string `json:"id"`
	ScheduleMode             string `json:"scheduleMode"`
	ScheduleExpr             string `json:"scheduleExpr"`
	ProcDefId                string `json:"procDefId"`
	ProcDefName              string `json:"procDefName"`
	EntityDataId             string `json:"entityDataId"`
	EntityDataName           string `json:"entityDataName"`
	Owner                    string `json:"owner"`
	Status                   string `json:"status"`
	CreatedTime              string `json:"createdTime"`
	TotalCompletedInstances  int    `json:"totalCompletedInstances"`
	TotalFaultedInstances    int    `json:"totalFaultedInstances"`
	TotalInProgressInstances int    `json:"totalInProgressInstances"`
}

type ProcScheduleOperationParam struct {
	Id string `json:"id" binding:"required"`
}

type ProcScheduleInstQueryParam struct {
	UserTaskId         string `json:"userTaskId"`
	ProcInstanceStatus string `json:"procInstanceStatus"` // 编排实例状态 -> S(成功) | F(失败) | R(运行中)
}

type ProcScheduleInstQueryObj struct {
	ProcInstId  string `json:"procInstId"`
	Status      string `json:"status"`
	ExecTime    string `json:"execTime"`
	ProcDefId   string `json:"procDefId"`
	ProcDefName string `json:"procDefName"`
}
