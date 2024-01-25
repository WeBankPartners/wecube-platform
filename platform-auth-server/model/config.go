package model

import (
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/cipher"
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
	PlatformMgmtAddress string `json:"platform_core_address"`
	Timeout             int    `json:"timeout"`
}

type Auth struct {
	//JwtPrivateKeyPath string `json:"jwt_private_key_path"`
	//JwtPublicKeyPath  string `json:"jwt_public_key_path"`
	SigningKey      string `json:"signing_key"`
	SigningKeyBytes []byte `json:"-"`
	//JwtPublicKeyBytes []byte `json:"-"`
	AccessTokenMins   int    `json:"access_token_mins"`
	RefreshTokenMins  int    `json:"refresh_token_mins"`
	WebPrivateKeyPath string `json:"web_private_key_path"`
	WebPublicKeyPath  string `json:"web_public_key_path"`
}

type UmAuth struct {
	Address  string `json:"address"`
	AppId    string `json:"appId"`
	AppToken string `json:"appToken"`
}

type GlobalConfig struct {
	ServerAddress          string             `json:"server_address"`
	ServerPort             string             `json:"server_port"`
	Log                    LogConfig          `json:"log"`
	PasswordPrivateKeyPath string             `json:"password_private_key_path"`
	Database               DatabaseConfig     `json:"database"`
	UmPermissionUpload     UmPermissionUpload `json:"um_permission_upload"`
	Auth                   Auth               `json:"auth"`
	ErrorTemplateDir       string             `json:"error_template_dir"`
	ErrorDetailReturn      bool               `json:"error_detail_return"`
}

type AuthConfig struct {
	PrivateKey string         `json:"private_key"`
	PublicKey  string         `json:"public_key"`
	JwtToken   JwtTokenConfig `json:"jwt_token"`
}

type JwtTokenConfig struct {
	UserRefreshToken      int `json:"user_refresh_token"`
	UserAccessToken       int `json:"user_access_token"`
	SubSystemRefreshToken int `json:"sub_system_refresh_token"`
	SubSystemAccessToken  int `json:"sub_system_access_token"`
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

	if c.PasswordPrivateKeyPath != "" {
		privateBytes, readPriErr := os.ReadFile(c.PasswordPrivateKeyPath)
		if readPriErr == nil {
			if c.Database.Password, err = cipher.DecryptRsa(c.Database.Password, string(privateBytes)); err != nil {
				errMessage = "decrypt database password config fail," + err.Error()
				return
			}
		} else {
			fmt.Printf("raed private key:%s fail:%s ", c.PasswordPrivateKeyPath, readPriErr.Error())
		}
	}

	Config = &c
	return
}
