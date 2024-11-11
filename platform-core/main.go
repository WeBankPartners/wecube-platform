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

func main() {
	configFile := flag.String("c", "config/default.json", "config file path")
	flag.Parse()
	if initConfigMessage := models.InitConfig(*configFile); initConfigMessage != "" {
		fmt.Printf("Init config file error,%s \n", initConfigMessage)
		return
	}
	log.InitLogger()
	if initDbError := db.InitDatabase(); initDbError != nil {
		return
	}
	// 初始化token
	remote.InitToken()
	// start cron job
	cron.StartCronJob()
	go bash.InitPluginDockerHostSSH()
	workflow.StartCronJob()
	process.InitProcScheduleTimer()
	go data_trans.StartExecWorkflowCron()
	//start http
	api.InitHttpServer()
}
