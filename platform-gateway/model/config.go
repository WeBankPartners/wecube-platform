package model

import (
	"encoding/json"
	"io/ioutil"
	"os"
)

type DatabaseConfig struct {
	Server   string `json:"server"`
	Port     string `json:"port"`
	User     string `json:"user"`
	Password string `json:"password"`
	DataBase string `json:"database"`
	MaxOpen  int    `json:"maxOpen"`
	MaxIdle  int    `json:"maxIdle"`
	Timeout  int    `json:"timeout"`
}

type LogConfig struct {
	Level            string `json:"level"`
	LogDir           string `json:"log_dir"`
	AccessLogEnable  bool   `json:"access_log_enable"`
	DbLogEnable      bool   `json:"db_log_enable"`
	ArchiveMaxSize   int    `json:"archive_max_size"`
	ArchiveMaxBackup int    `json:"archive_max_backup"`
	ArchiveMaxDay    int    `json:"archive_max_day"`
	Compress         bool   `json:"compress"`
	FormatJson       bool   `json:"format_json"`
}

type RemoteServiceConfig struct {
	RouteConfigAddress string `json:"route_config_address"`
	Timeout            int    `json:"timeout"`
}

type RedirectRoute struct {
	Context    string `json:"context"`
	TargetPath string `json:"target_path"`
}

type GlobalConfig struct {
	ServerAddress  string              `json:"server_address"`
	ServerPort     string              `json:"server_port"`
	Log            LogConfig           `json:"log"`
	Database       DatabaseConfig      `json:"database"`
	Remote         RemoteServiceConfig `json:"remote_service"`
	RedirectRoutes []RedirectRoute     `json:"redirect_routes"`
}

var (
	Config *GlobalConfig
)

func InitConfig(configFile string) (errMessage string) {
	if configFile == "" {
		errMessage = "config file empty,use -c to specify configuration file"
		return
	}
	_, err := os.Stat(configFile)
	if os.IsExist(err) {
		errMessage = "config file not found," + err.Error()
		return
	}
	b, err := ioutil.ReadFile(configFile)
	if err != nil {
		errMessage = "read config file fail," + err.Error()
		return
	}
	var c GlobalConfig
	err = json.Unmarshal(b, &c)
	if err != nil {
		errMessage = "parse file to json fail," + err.Error()
		return
	}
	Config = &c
	return
}
