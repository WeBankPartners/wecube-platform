package models

import (
	"encoding/json"
	"fmt"
	"os"
	"strings"

	"github.com/WeBankPartners/go-common-lib/cipher"
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
	Enable              bool   `json:"enable"`
	Url                 string `json:"url"`
	JwtSigningKey       string `json:"jwt_signing_key"`
	SubSystemPrivateKey string `json:"sub_system_private_key"`
}

type S3Config struct {
	ServerAddress       string `json:"server_address"`
	AccessKey           string `json:"access_key"`
	SecretKey           string `json:"secret_key"`
	PluginPackageBucket string `json:"plugin_package_bucket"`
}

type StaticResourceConfig struct {
	Server   string `json:"server"`
	User     string `json:"user"`
	Password string `json:"password"`
	Port     string `json:"port"`
	Path     string `json:"path"`
}

type PluginJsonConfig struct {
	BaseMountPath         string `json:"base_mount_path"`
	DeployPath            string `json:"deploy_path"`
	PasswordPubKeyPath    string `json:"password_pub_key_path"`
	PasswordPubKeyContent string `json:"-"`
	ResourcePasswordSeed  string `json:"resource_password_seed"`
	PublicReleaseUrl      string `json:"public_release_url"`
}

type GatewayConfig struct {
	Url       string `json:"url"`
	HostPorts string `json:"host_ports"`
}

type CronConfig struct {
	KeepBatchExecDays int64 `json:"keep_batch_exec_days"`
}

type GlobalConfig struct {
	Version                string                  `json:"version"`
	DefaultLanguage        string                  `json:"default_language"`
	PasswordPrivateKeyPath string                  `json:"password_private_key_path"`
	HostIp                 string                  `json:"host_ip"`
	HttpsEnable            string                  `json:"https_enable"`
	EncryptSeed            string                  `json:"encrypt_seed"`
	HttpServer             *HttpServerConfig       `json:"http_server"`
	Log                    *LogConfig              `json:"log"`
	Database               *DatabaseConfig         `json:"database"`
	Auth                   *AuthConfig             `json:"auth"`
	S3                     *S3Config               `json:"s3"`
	StaticResources        []*StaticResourceConfig `json:"static_resources"`
	Plugin                 *PluginJsonConfig       `json:"plugin"`
	Gateway                *GatewayConfig          `json:"gateway"`
	Cron                   *CronConfig             `json:"cron"`
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
	if len(c.StaticResources) > 0 {
		firstStaticResourceObj := c.StaticResources[0]
		if strings.Contains(firstStaticResourceObj.Server, ",") {
			srServerList := strings.Split(firstStaticResourceObj.Server, ",")
			srUserList := strings.Split(firstStaticResourceObj.User, ",")
			if len(srUserList) != len(srServerList) {
				errMessage = "static resource config validate fail,multiple user required"
				return
			}
			srPwdList := strings.Split(firstStaticResourceObj.Password, ",")
			if len(srPwdList) != len(srServerList) {
				errMessage = "static resource config validate fail,multiple password required"
				return
			}
			srPortList := strings.Split(firstStaticResourceObj.Port, ",")
			if len(srPortList) != len(srServerList) {
				errMessage = "static resource config validate fail,multiple port required"
				return
			}
			srPathList := strings.Split(firstStaticResourceObj.Path, ",")
			if len(srPathList) != len(srServerList) {
				errMessage = "static resource config validate fail,multiple path required"
				return
			}
			newStaticResourceList := []*StaticResourceConfig{}
			for i, tmpServer := range srServerList {
				newStaticResourceList = append(newStaticResourceList, &StaticResourceConfig{
					Server:   tmpServer,
					User:     srUserList[i],
					Password: srPwdList[i],
					Port:     srPortList[i],
					Path:     srPathList[i],
				})
			}
			c.StaticResources = newStaticResourceList
		}
	}
	if c.PasswordPrivateKeyPath != "" {
		privateBytes, readPriErr := os.ReadFile(c.PasswordPrivateKeyPath)
		if readPriErr == nil {
			if c.Database.Password, err = cipher.DecryptRsa(c.Database.Password, string(privateBytes)); err != nil {
				errMessage = "decrypt database password config fail," + err.Error()
				return
			}
			c.Database.Password = strings.ReplaceAll(c.Database.Password, "\n", "")
			if c.S3.SecretKey, err = cipher.DecryptRsa(c.S3.SecretKey, string(privateBytes)); err != nil {
				errMessage = "decrypt s3 secretKey config fail," + err.Error()
				return
			}
			c.S3.SecretKey = strings.ReplaceAll(c.S3.SecretKey, "\n", "")
			for i, staticResourceObj := range c.StaticResources {
				if c.StaticResources[i].Password, err = cipher.DecryptRsa(staticResourceObj.Password, string(privateBytes)); err != nil {
					errMessage = "decrypt static resource password config fail," + err.Error()
					return
				}
				c.StaticResources[i].Password = strings.ReplaceAll(c.StaticResources[i].Password, "\n", "")
			}
			if c.Plugin.ResourcePasswordSeed, err = cipher.DecryptRsa(c.Plugin.ResourcePasswordSeed, string(privateBytes)); err != nil {
				errMessage = "decrypt public resource password seed config fail," + err.Error()
				return
			}
			c.Plugin.ResourcePasswordSeed = strings.ReplaceAll(c.Plugin.ResourcePasswordSeed, "\n", "")
		} else {
			fmt.Printf("raed private key:%s fail:%s ", c.PasswordPrivateKeyPath, readPriErr.Error())
		}
	}
	if c.Plugin.PasswordPubKeyPath != "" {
		publicBytes, readPubErr := os.ReadFile(c.Plugin.PasswordPubKeyPath)
		if readPubErr == nil {
			c.Plugin.PasswordPubKeyContent = string(publicBytes)
		} else {
			fmt.Printf("raed public public key:%s fail:%s ", c.Plugin.PasswordPubKeyPath, readPubErr.Error())
		}
	}
	if c.Auth.SubSystemPrivateKey == "" {
		c.Auth.SubSystemPrivateKey = "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAwnTN7JDXFcSoikXuNOQDtAjic1Wu6oAtCQJquCJmXrBTqB7hwS2mK6TuT8P7Jx60BQcaRL12hPLi6cOiCawuVwIDAQABAkB9NORazDARjhzPW5OzbpWL2KSmiqcjywA0at/4S/4KPPM8vwRjzEMs7pV9nSJ2M+/YOqPMBDl8iBUSLpfKf/uxAiEA52UroIvo2URlmAycaJm7+e4QqqfhEnM9wlGCJwL2jTsCIQDXIh2zwN7KQEIypmOL+uXvlZUjmx0Tj29mWOwP/fBBlQIhAI9+VLSlror1eE73GxNeqoxNznYVz2RCpLzZEO4iT0S7AiARg0Z1tpKsVjTNWLwrzf3f1gZxApSIXhnMdBqrZpmjTQIhAJhgYctlaydmggTPCqWLGub9WqEyH2HrrcabRvpWdEcV"
	}
	Config = &c
	return
}
