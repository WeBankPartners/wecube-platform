package main

import (
	"flag"
	"fmt"
	sw "github.com/WeBankPartners/wecube-platform/platform-auth-server/api"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/service"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/service/db"
	"go.uber.org/zap"
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
	err := exterror.InitErrorTemplateList(model.Config.ErrorTemplateDir, model.Config.ErrorDetailReturn)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "Init error template list fail", zap.Error(err))
		return
	}
	log.Info(nil, log.LOGGER_APP, "Server started")
	if err := service.AuthServiceInstance.InitKey(); err != nil {
		fmt.Printf("failed to init auth service key")
		return
	}
	if initDbError := db.InitDatabase(); initDbError != nil {
		log.Error(nil, log.LOGGER_APP, "Init db connection error", zap.Error(initDbError))
		return
	}
	go service.StartCornJob()
	router := sw.NewRouter()
	router.Run(":" + model.Config.ServerPort)

}
