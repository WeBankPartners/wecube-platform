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
	Role           string    `json:"role" xorm:"role"`                       // 管理角色
	MailMode       string    `json:"mailMode" xorm:"mail_mode"`              // 邮件发送模式->role(角色邮箱) | user(用户邮箱) | none(不发送)
	CreatedBy      string    `json:"createdBy" xorm:"created_by"`            // 创建人
	CreatedTime    time.Time `json:"createdTime" xorm:"created_time"`        // 创建时间
	UpdatedBy      string    `json:"updatedBy" xorm:"updated_by"`            // 更新人
	UpdatedTime    time.Time `json:"updatedTime" xorm:"updated_time"`        // 更新时间
	Name           string    `json:"name" xorm:"name"`                       // 任务名
}

type ProcScheduleJob struct {
	Id               string    `json:"id" xorm:"id"`                               // 定时配置id加时间戳
	ScheduleConfigId string    `json:"scheduleConfigId" xorm:"schedule_config_id"` // 定时配置id
	ProcInsId        string    `json:"procInsId" xorm:"proc_ins_id"`               // 编排实例id
	Status           string    `json:"status" xorm:"status"`                       // 状态->ready(准备启动) | fail(报错) | done(已完成)
	HandleBy         string    `json:"handleBy" xorm:"handle_by"`                  // 处理的主机
	ErrorMsg         string    `json:"errorMsg" xorm:"error_msg"`                  // 错误信息
	MailStatus       string    `json:"mailStatus" xorm:"mail_status"`              // 邮件状态->none(不发邮件) | wait(等待发) | sending(正在发) | fail(发送失败) | done(已发送)
	MailMsg          string    `json:"mailMsg" xorm:"mail_msg"`                    // 邮件通知信息
	CreatedTime      time.Time `json:"createdTime" xorm:"created_time"`            // 创建时间
	UpdatedTime      time.Time `json:"updatedTime" xorm:"updated_time"`            // 更新时间
}

type ProcScheduleQueryParam struct {
	ScheduleMode        string `json:"scheduleMode"`
	Owner               string `json:"owner"`
	StartTime           string `json:"startTime"`
	EndTime             string `json:"endTime"`
	Name                string `json:"name"`
	ProcDefId           string `json:"procDefId"`
	JobCreatedStartTime string `json:"jobCreatedStartTime"`
	JobCreatedEndTime   string `json:"jobCreatedEndTime"`
}

type ProcScheduleQueryRow struct {
	Id     string `xorm:"id"`
	Status string `xorm:"status"`
	Num    int    `xorm:"num"`
}

type CreateProcScheduleParam struct {
	ScheduleMode   string `json:"scheduleMode" binding:"required"`
	ScheduleExpr   string `json:"scheduleExpr" binding:"required"`
	ProcDefId      string `json:"procDefId" binding:"required"`
	ProcDefName    string `json:"procDefName"`
	EntityDataId   string `json:"entityDataId"`
	EntityDataName string `json:"entityDataName"`
	Role           string `json:"role" binding:"required"`
	MailMode       string `json:"mailMode" binding:"required"` // 邮件发送模式->role(角色邮箱) | user(用户邮箱) | none(不发送)
	CronExpr       string `json:"-"`
	Operator       string `json:"-"`
	Name           string `json:"name"`
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
	TotalTimeoutInstances    int    `json:"totalTimeoutInstances"`
	TotalTerminateInstances  int    `json:"totalTerminateInstances"`
	Role                     string `json:"role" xorm:"role"`          // 管理角色
	MailMode                 string `json:"mailMode" xorm:"mail_mode"` // 邮件发送模式->role(角色邮箱) | user(用户邮箱) | none(不发送)
	Version                  string `json:"version"`
	Name                     string `json:"name"`
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

type SendMailTarget struct {
	Accept  []string `json:"accept"`
	Subject string   `json:"subject"`
	Content string   `json:"content"`
}

type SendMailSource struct {
	Sender   string `json:"sender"`
	Server   string `json:"server"`
	Password string `json:"password"`
	SSL      bool   `json:"SSL"`
}

type ScheduleJobMailQueryObj struct {
	Id               string `xorm:"id"`
	ProcInsId        string `xorm:"proc_ins_id"`
	ProcDefName      string `xorm:"proc_def_name"`
	EntityDataName   string `xorm:"entity_data_name"`
	Status           string `xorm:"status"`
	CreatedTime      string `xorm:"created_time"`
	NodeStatus       string `xorm:"node_status"`
	NodeName         string `xorm:"node_name"`
	ScheduleConfigId string `xorm:"schedule_config_id"`
}
