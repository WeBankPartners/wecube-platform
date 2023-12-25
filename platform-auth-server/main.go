package main

import (
	"flag"
	"fmt"
	sw "github.com/WeBankPartners/wecube-platform/platform-auth-server/api"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/service/jwt"
)

func main() {
	configFile := flag.String("c", "config/default.json", "config file path")
	flag.Parse()
	if initConfigMessage := model.InitConfig(*configFile); initConfigMessage != "" {
		fmt.Printf("Init config file error,%s \n", initConfigMessage)
		return
	}

	log.InitLogger()
	/*	middleware.InitAuth()
		middleware.InitRedirect()
	*/
	err := exterror.InitErrorTemplateList(model.Config.ErrorTemplateDir, model.Config.ErrorDetailReturn)
	if err != nil {
		log.Logger.Error("Init error template list fail", log.Error(err))
		return
	}
	log.Logger.Info("Server started")
	if err := jwt.InitKey(); err != nil {
		fmt.Printf("failed to init jwt key")
		return
	}
	if err := middleware.InitAuth(); err != nil {
		fmt.Printf("failed to init middleware auth")
		return
	}

	router := sw.NewRouter()
	go router.Run(":" + model.Config.Port)

}
