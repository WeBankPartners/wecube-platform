package main

import (
	"flag"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/api"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
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
	//start http
	api.InitHttpServer()
}
