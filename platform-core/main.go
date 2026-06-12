// Package main 是 platform-core 服务的入口包，负责完成服务启动所需的全部初始化工作并启动 HTTP 服务器。
package main

import (
	"flag"
	"fmt"

	"github.com/WeBankPartners/wecube-platform/platform-core/api"
	data_trans "github.com/WeBankPartners/wecube-platform/platform-core/api/v1/data-trans"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/v1/process"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/bash"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/cron"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/workflow"
)

// main 是服务程序的入口函数，按序完成以下初始化步骤后启动 HTTP 服务器：
//  1. 解析命令行参数，获取配置文件路径
//  2. 加载并解析 JSON 配置文件（含 RSA 解密敏感密码、加载菜单 API 权限映射等）
//  3. 初始化日志系统
//  4. 初始化数据库连接池
//  5. 初始化子系统间鉴权 token
//  6. 启动各类定时任务（批量执行清理、流程调度邮件、流程事件处理等）
//  7. 异步预热插件 Docker 宿主机 SSH 免密连接
//  8. 启动工作流相关定时任务（操作扫描、工作流恢复接管、睡眠检测等）
//  9. 初始化流程定时调度器并从数据库加载调度配置
//  10. 异步启动数据迁移工作流执行守护任务
//  11. 启动 HTTP 服务器，注册所有路由并开始监听端口
func main() {
	// 解析命令行参数，-c 指定配置文件路径，默认为 config/default.json
	configFile := flag.String("c", "config/default.json", "config file path")
	flag.Parse()

	// 加载并解析配置文件：读取 JSON 配置、RSA 解密数据库/S3/静态资源等敏感密码，加载菜单 API 权限映射
	if initConfigMessage := models.InitConfig(*configFile); initConfigMessage != "" {
		fmt.Printf("Init config file error,%s \n", initConfigMessage)
		return
	}

	// 初始化日志系统，根据配置设置日志级别、输出目录、滚动策略等
	if err := log.InitLogger(); err != nil {
		fmt.Printf("Server  init loggers failed, err: %v\n", err)
		return
	}
	// 程序退出前同步刷新所有日志缓冲区，确保日志不丢失
	defer log.SyncLoggers()

	// 初始化数据库连接池，建立与 MySQL 的连接
	if initDbError := db.InitDatabase(); initDbError != nil {
		return
	}

	// 初始化子系统间调用所需的鉴权 token（CoreToken），用于平台内部服务间的认证
	remote.InitToken()

	// 启动通用定时任务：
	//   - 每24小时清理过期批量执行记录
	//   - 每分钟发送流程调度结果通知邮件
	//   - 每10秒处理 proc_ins_event 表中待处理的流程事件
	//   - 每30秒将旧版 core_operation_event 表数据迁移到新 proc_ins_event 表
	cron.StartCronJob()

	// 异步初始化插件 Docker 宿主机以及静态资源服务器的 SSH 免密连接，避免首次 SSH 操作阻塞主流程
	go bash.InitPluginDockerHostSSH()

	// 启动工作流相关定时任务：
	//   - 加载当前节点负责的所有运行中工作流到内存
	//   - 每2秒扫描工作流操作表，处理 kill/retry/approve 等指令
	//   - 每10秒尝试接管超过30秒未活跃的孤儿工作流
	//   - 每1小时检测并将满足条件的工作流置为睡眠状态，释放内存
	workflow.StartCronJob()

	// 初始化流程定时调度器：创建 CronSecond 定时器，从数据库加载所有有效的调度配置并注册 cron 任务，
	// 同时启动后台协程定时（每300秒）拉取其它实例新增的调度配置
	process.InitProcScheduleTimer()

	// 异步启动数据迁移工作流执行守护任务：每5秒轮询待执行的编排任务，
	// 按序触发创建资源、初始化资源、应用部署等编排流程
	go data_trans.StartExecWorkflowCron()

	// 初始化并启动 HTTP 服务器：注册全量路由（含认证中间件、访问日志、Panic 恢复），
	// 并在配置端口上开始监听 HTTP 请求
	api.InitHttpServer()
}
