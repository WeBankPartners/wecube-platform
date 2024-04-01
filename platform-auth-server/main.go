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
)

func main() {
	configFile := flag.String("c", "config/default.json", "config file path")
	flag.Parse()
	if initConfigMessage := model.InitConfig(*configFile); initConfigMessage != "" {
		fmt.Printf("Init config file error,%s \n", initConfigMessage)
		return
	}

	log.InitLogger()
	err := exterror.InitErrorTemplateList(model.Config.ErrorTemplateDir, model.Config.ErrorDetailReturn)
	if err != nil {
		log.Logger.Error("Init error template list fail", log.Error(err))
		return
	}
	log.Logger.Info("Server started")
	if err := service.AuthServiceInstance.InitKey(); err != nil {
		fmt.Printf("failed to init auth service key")
		return
	}
	if initDbError := db.InitDatabase(); initDbError != nil {
		log.Logger.Error("Init db connection error", log.Error(initDbError))
		return
	}

	router := sw.NewRouter()
	router.Run(":" + model.Config.ServerPort)

}
