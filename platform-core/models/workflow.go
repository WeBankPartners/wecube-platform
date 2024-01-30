package models

import (
	"context"
	"time"
)

type ProcRunWorkflow struct {
	Id            string    `json:"id" xorm:"id"`                         // 唯一标识
	Name          string    `json:"name" xorm:"name"`                     // 名称
	Status        string    `json:"status" xorm:"status"`                 // 状态->ready(初始化) | running(运行中) | fail(失败) | success(成功) | problem(节点失败) | kill(终止)
	ErrorMessage  string    `json:"errorMessage" xorm:"error_message"`    // 错误信息
	Sleep         bool      `json:"sleep" xorm:"sleep"`                   // 休眠->problem超10min或running中当前节点wait超10min,防止不是终态的工作流一直占用资源
	Stop          bool      `json:"stop" xorm:"stop"`                     // 暂停->人为停止
	CreatedTime   time.Time `json:"createdTime" xorm:"created_time"`      // 创建时间
	UpdatedTime   time.Time `json:"updatedTime" xorm:"updated_time"`      // 更新时间
	Host          string    `json:"host" xorm:"host"`                     // 当前运行主机
	LastAliveTime time.Time `json:"lastAliveTime" xorm:"last_alive_time"` // 定期打卡时间->每隔10s更新
}

type ProcRunNode struct {
	Id           string    `json:"id" xorm:"id"`                      // 唯一标识
	WorkflowId   string    `json:"workflowId" xorm:"workflow_id"`     // 工作流id
	Name         string    `json:"name" xorm:"name"`                  // 名称
	JobType      string    `json:"jobType" xorm:"job_type"`           // 任务类型->start(开始) | auto(自动) | data(数据写入) | human(人工) | | fork(分流) | merge(聚合) | time(定时) | date(定期) | decision(判断) | end(结束) | break(异常结束)
	Status       string    `json:"status" xorm:"status"`              // 状态->ready(初始化) | running(运行中) | wait(等待or聚合) | fail(失败) | success(成功) | timeout(超时)
	Input        string    `json:"input" xorm:"input"`                // 输入
	Output       string    `json:"output" xorm:"output"`              // 输出
	TmpData      string    `json:"tmpData" xorm:"tmp_data"`           // 临时数据
	ErrorMessage string    `json:"errorMessage" xorm:"error_message"` // 错误信息
	Timeout      int       `json:"timeout" xorm:"timeout"`            // 超时时间
	CreatedTime  time.Time `json:"createdTime" xorm:"created_time"`   // 创建时间
	UpdatedTime  time.Time `json:"updatedTime" xorm:"updated_time"`   // 更新时间
	StartTime    time.Time `json:"startTime" xorm:"start_time"`       // 开始时间
	EndTime      time.Time `json:"endTime" xorm:"end_time"`           // 结束时间
}

type ProcRunLink struct {
	Id         string `json:"id" xorm:"id"`                  // 唯一标识
	WorkflowId string `json:"workflowId" xorm:"workflow_id"` // 工作流id
	Name       string `json:"name" xorm:"name"`              // 名称
	Source     string `json:"source" xorm:"source"`          // 源
	Target     string `json:"target" xorm:"target"`          // 目标
}

type ProcRunWorkRecord struct {
	Id          int64     `json:"id" xorm:"id"`                    // 自增id
	WorkflowId  string    `json:"workflowId" xorm:"workflow_id"`   // 工作流id
	Host        string    `json:"host" xorm:"host"`                // 主机
	Action      string    `json:"action" xorm:"action"`            // 行为->ready(初始化) | running(运行中) | fail(失败) | success(成功) | problem(节点失败) | kill(终止) | sleep(休眠) | wakeup(唤醒) | takeOver(接管) | stop(暂停) | continue(恢复)
	Message     string    `json:"message" xorm:"message"`          // 详细信息,终止原因等
	CreatedBy   string    `json:"createdBy" xorm:"created_by"`     // 创建人
	CreatedTime time.Time `json:"createdTime" xorm:"created_time"` // 创建时间
}

type ProcRunOperation struct {
	Id          int64     `json:"id" xorm:"id"`                    // 自增id
	WorkflowId  string    `json:"workflowId" xorm:"workflow_id"`   // 工作流id
	NodeId      string    `json:"nodeId" xorm:"node_id"`           // 节点id
	Operation   string    `json:"operation" xorm:"operation"`      // 操作->kill(终止工作流) | retry(重试节点) | ignore(跳过节点) | approve(人工审批) | date(定期触发) | stop(暂停) | continue(恢复)
	Status      string    `json:"status" xorm:"status"`            // 状态->wait(待处理) | doing(正在处理) | done(已处理)
	Message     string    `json:"message" xorm:"message"`          // 详细信息->审批结果,终止原因等
	CreatedBy   string    `json:"createdBy" xorm:"created_by"`     // 创建人
	CreatedTime time.Time `json:"createdTime" xorm:"created_time"` // 创建时间
	HandleBy    string    `json:"handleBy" xorm:"handle_by"`       // 处理的主机
	StartTime   time.Time `json:"startTime" xorm:"start_time"`     // 处理开始时间
	EndTime     time.Time `json:"endTime" xorm:"end_time"`         // 处理结束时间
}

type ProcOperation struct {
	Ctx        context.Context
	WorkflowId string             `json:"workflowId" xorm:"workflow_id"` // 工作流id
	Message    string             `json:"message" xorm:"message"`        // 详细信息,终止原因等
	CreatedBy  string             `json:"createdBy" xorm:"created_by"`   // 创建人
	NodeErr    *WorkProblemErrObj `json:"nodeErr"`
}

type ProcNodeContext struct {
	Id     string
	Input  interface{}
	Output interface{}
}

type TimeNodeParam struct {
	Type     string `json:"type"` // duration/date
	Duration int    `json:"duration"`
	Unit     string `json:"unit"` // sec/min/hour/day
	Date     string `json:"date"` // 2024-01-15 00:00:00
}

type WorkProblemErrObj struct {
	NodeId     string `json:"nodeId"`
	NodeName   string `json:"nodeName"`
	ErrMessage string `json:"errMessage"`
}
