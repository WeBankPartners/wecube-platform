package models

import (
	"encoding/json"
	"os"
	"strings"
)

type HttpServerConfig struct {
	Port              string `json:"port"`
	Cross             bool   `json:"cross"`
	ErrorTemplateDir  string `json:"error_template_dir"`
	ErrorDetailReturn bool   `json:"error_detail_return"`
}

type LogConfig struct {
	Level            string `json:"level"`
	LogDir           string `json:"log_dir"`
	AccessLogEnable  bool   `json:"access_log_enable"`
	DbLogEnable      bool   `json:"db_log_enable"`
	MetricLogEnable  bool   `json:"metric_log_enable"`
	ArchiveMaxSize   int    `json:"archive_max_size"`
	ArchiveMaxBackup int    `json:"archive_max_backup"`
	ArchiveMaxDay    int    `json:"archive_max_day"`
	Compress         bool   `json:"compress"`
	FormatJson       bool   `json:"format_json"`
}

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

type AuthConfig struct {
	Enable           bool   `json:"enable"`
	JwtSigningKey    string `json:"jwt_signing_key"`
	PasswordSeed     string `json:"password_seed"`
	ExpireSec        int64  `json:"expire_sec"`
	FreshTokenExpire int64  `json:"fresh_token_expire"`
}

type GlobalConfig struct {
	DefaultLanguage string            `json:"default_language"`
	HttpServer      *HttpServerConfig `json:"http_server"`
	Log             *LogConfig        `json:"log"`
	Database        *DatabaseConfig   `json:"database"`
	Auth            *AuthConfig       `json:"auth"`
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
	b, readErr := os.ReadFile(strings.Replace(configFile, "../", "", -1))
	if readErr != nil {
		errMessage = "read config file fail," + readErr.Error()
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
