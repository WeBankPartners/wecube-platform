package main

import (
	"flag"
	"fmt"
	sw "github.com/WeBankPartners/wecube-platform/platform-gateway/api"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/model"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/service"
)

func main() {
	configFile := flag.String("c", "config/default.json", "config file path")
	flag.Parse()
	if initConfigMessage := model.InitConfig(*configFile); initConfigMessage != "" {
		fmt.Printf("Init config file error,%s \n", initConfigMessage)
		return
	}

	if err := log.InitLogger(); err != nil {
		fmt.Printf("Server  init loggers failed, err: %v\n", err)
		return
	}
	defer log.SyncLoggers()
	middleware.Init()

	if err := service.Init(); err != nil {
		fmt.Printf("failed to init service:%s", err.Error())
		return
	}
	log.Info(nil, log.LOGGER_APP, "Server started")

	router := sw.NewRouter()
	router.Run(model.Config.ServerAddress + ":" + model.Config.ServerPort)

}
