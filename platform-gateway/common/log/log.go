package log

import (
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/model"
	"log"
	"path/filepath"
	"strings"

	"github.com/WeBankPartners/go-common-lib/logger"
	"go.uber.org/zap"
)

var (
	Logger         *zap.SugaredLogger
	TxnLogger      *zap.SugaredLogger
	AccessLogger   *zap.SugaredLogger
	DatabaseLogger *zap.SugaredLogger
	MetricLogger   *zap.SugaredLogger
)

func InitLogger() (err error) {
	baseLogDir := model.Config.Log.LogDir
	if strings.HasSuffix(model.Config.Log.LogDir, "/") {
		baseLogDir = baseLogDir[:len(baseLogDir)-1]
	}
	appName := "platform-gateway"
	param := &logger.LoggerParam{
		MaxSize:       model.Config.Log.ArchiveMaxSize,
		MaxAge:        model.Config.Log.ArchiveMaxDay,
		MaxBackups:    model.Config.Log.ArchiveMaxBackup,
		Compress:      model.Config.Log.Compress,
		Level:         model.Config.Log.Level,
		AddCallerSkip: 1,
	}
	// 业务日志实例
	param.Filename = filepath.Join(baseLogDir, "/"+appName+".log")
	if Logger, err = newLogger(param); err != nil {
		return
	}
	// 访问日志实例
	if model.Config.Log.AccessLogEnable {
		param.Filename = filepath.Join(baseLogDir, fmt.Sprintf("/%s-access.log", appName))
		if AccessLogger, err = newLogger(param); err != nil {
			return
		}
		param.Filename = filepath.Join(baseLogDir, "/txn.log")
		if TxnLogger, err = newLogger(param); err != nil {
			return
		}
	}

	// 应用性能实例
	param.Filename = filepath.Join(baseLogDir, fmt.Sprintf("/%s-metric.log", appName))
	if MetricLogger, err = newLogger(param); err != nil {
		return
	}

	// Db日志实例
	if model.Config.Log.DbLogEnable {
		param.Filename = filepath.Join(baseLogDir, fmt.Sprintf("/%s-db.log", appName))
		if DatabaseLogger, err = newLogger(param); err != nil {
			return
		}
	}
	return
}

// 创建日志实例
func newLogger(param *logger.LoggerParam) (sugaredLogger *zap.SugaredLogger, err error) {
	l, err := logger.NewLogger(param)
	if err != nil {
		return
	}
	sugaredLogger = l.Sugar()
	if Logger != nil {
		Logger.Debugf("Logger %s initialized", param.Filename)
	} else {
		log.Printf("Logger %s initialized\n", param.Filename)
	}
	return
}

// SyncLoggers 同步日志实例
func SyncLoggers() {
	syncLogger(Logger)
	syncLogger(AccessLogger)
	syncLogger(TxnLogger)
	syncLogger(MetricLogger)
	syncLogger(DatabaseLogger)
}

// 调用Sync方法将缓冲区中的日志条目刷新到磁盘
func syncLogger(logger *zap.SugaredLogger) {
	if logger != nil {
		if err := logger.Sync(); err != nil {
			log.Println(err)
		}
	}
}

func JsonObj(k string, v interface{}) zap.Field {
	b, err := json.Marshal(v)
	if err == nil {
		return zap.String(k, string(b))
	} else {
		return zap.Error(err)
	}
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
