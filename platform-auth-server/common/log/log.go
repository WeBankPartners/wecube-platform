package log

import (
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"strings"

	"github.com/WeBankPartners/go-common-lib/logger"
	"go.uber.org/zap"
)

var (
	Logger         *zap.Logger
	AccessLogger   *zap.Logger
	DatabaseLogger *zap.Logger
	MetricLogger   *zap.Logger
)

func InitLogger() {
	baseLogDir := model.Config.Log.LogDir
	if strings.HasSuffix(model.Config.Log.LogDir, "/") {
		baseLogDir = baseLogDir[:len(baseLogDir)-1]
	}
	Logger = logger.InitArchiveZapLogger(logger.LogConfig{
		Name:             "server",
		FilePath:         fmt.Sprintf("%s/auth-server.log", baseLogDir),
		LogLevel:         model.Config.Log.Level,
		ArchiveMaxSize:   model.Config.Log.ArchiveMaxSize,
		ArchiveMaxBackup: model.Config.Log.ArchiveMaxBackup,
		ArchiveMaxDay:    model.Config.Log.ArchiveMaxDay,
		Compress:         model.Config.Log.Compress,
	})
	if model.Config.Log.AccessLogEnable {
		AccessLogger = logger.InitArchiveZapLogger(logger.LogConfig{
			Name:             "access",
			FilePath:         fmt.Sprintf("%s/auth-server-access.log", baseLogDir),
			LogLevel:         model.Config.Log.Level,
			ArchiveMaxSize:   model.Config.Log.ArchiveMaxSize,
			ArchiveMaxBackup: model.Config.Log.ArchiveMaxBackup,
			ArchiveMaxDay:    model.Config.Log.ArchiveMaxDay,
			Compress:         model.Config.Log.Compress,
		})
	}
	if model.Config.Log.DbLogEnable {
		DatabaseLogger = logger.InitArchiveZapLogger(logger.LogConfig{
			Name:             "database",
			FilePath:         fmt.Sprintf("%s/auth-server-db.log", baseLogDir),
			LogLevel:         model.Config.Log.Level,
			ArchiveMaxSize:   model.Config.Log.ArchiveMaxSize,
			ArchiveMaxBackup: model.Config.Log.ArchiveMaxBackup,
			ArchiveMaxDay:    model.Config.Log.ArchiveMaxDay,
			Compress:         model.Config.Log.Compress,
		})
	}
	MetricLogger = logger.InitMetricZapLogger(logger.LogConfig{
		Name:             "metric",
		FilePath:         fmt.Sprintf("%s/auth-server-metric.log", baseLogDir),
		LogLevel:         model.Config.Log.Level,
		ArchiveMaxSize:   model.Config.Log.ArchiveMaxSize,
		ArchiveMaxBackup: model.Config.Log.ArchiveMaxBackup,
		ArchiveMaxDay:    model.Config.Log.ArchiveMaxDay,
		Compress:         model.Config.Log.Compress,
		FormatJson:       model.Config.Log.FormatJson,
	})
}

func Error(err error) zap.Field {
	return zap.Error(err)
}

func String(k, v string) zap.Field {
	return zap.String(k, v)
}

func Int(k string, v int) zap.Field {
	return zap.Int(k, v)
}

func Int64(k string, v int64) zap.Field {
	return zap.Int64(k, v)
}

func Float64(k string, v float64) zap.Field {
	return zap.Float64(k, v)
}

func JsonObj(k string, v interface{}) zap.Field {
	b, err := json.Marshal(v)
	if err == nil {
		return zap.String(k, string(b))
	} else {
		return zap.Error(err)
	}
}

func StringList(k string, v []string) zap.Field {
	return zap.Strings(k, v)
}

type MetricLogObject struct {
	ThreadNum     string
	Module        string
	TransactionId string
	RequestId     string
	LogPoint      string
	Content       map[string]string
	Message       string
}

func LogServerMetric(inputParam *MetricLogObject) {
	contentBytes, _ := json.Marshal(inputParam.Content)
	MetricLogger.Info(inputParam.Message,
		zap.String("threadNum", inputParam.ThreadNum),
		zap.String("module", inputParam.Module),
		zap.String("transactionId", inputParam.TransactionId),
		zap.String("requestId", inputParam.RequestId),
		zap.String("logPoint", inputParam.LogPoint),
		zap.String("content", string(contentBytes)),
		zap.String("message", inputParam.Message),
	)
}
