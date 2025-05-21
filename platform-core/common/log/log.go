package log

import (
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/logger"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"go.uber.org/zap"
	"log"
	"path/filepath"
	"strings"
)

var (
	Logger                 *zap.SugaredLogger
	AccessLogger           *zap.SugaredLogger
	MetricLogger           *zap.SugaredLogger
	TraceLogger            *zap.SugaredLogger
	DatabaseLogger         *zap.SugaredLogger
	WorkflowLogger         *zap.SugaredLogger
	WorkflowDatabaseLogger *zap.SugaredLogger
	DebugEnable            bool
)

func InitLogger() (err error) {
	baseLogDir := models.Config.Log.LogDir
	if strings.HasSuffix(models.Config.Log.LogDir, "/") {
		baseLogDir = baseLogDir[:len(baseLogDir)-1]
	}
	models.Config.Log.Level = strings.ToLower(models.Config.Log.Level)
	if models.Config.Log.Level == "debug" {
		DebugEnable = true
	}
	appName := models.GlobalProjectName
	param := &logger.LoggerParam{
		MaxSize:       models.Config.Log.ArchiveMaxSize,
		MaxAge:        models.Config.Log.ArchiveMaxDay,
		MaxBackups:    models.Config.Log.ArchiveMaxBackup,
		Compress:      models.Config.Log.Compress,
		Level:         models.Config.Log.Level,
		AddCallerSkip: 1,
	}
	// 业务日志实例
	param.Filename = filepath.Join(baseLogDir, "/"+appName+".log")
	if Logger, err = newLogger(param); err != nil {
		return
	}
	// 访问日志实例
	if models.Config.Log.AccessLogEnable {
		param.Filename = filepath.Join(baseLogDir, fmt.Sprintf("/%s-access.log", appName))
		if AccessLogger, err = newLogger(param); err != nil {
			return
		}
	}

	// 应用性能实例
	param.Filename = filepath.Join(baseLogDir, fmt.Sprintf("/%s-metric.log", appName))
	if MetricLogger, err = newLogger(param); err != nil {
		return
	}

	// 交易链路日志实例
	param.Filename = filepath.Join(baseLogDir, fmt.Sprintf("/%s-trace.log", appName))
	if TraceLogger, err = newLogger(param); err != nil {
		return
	}

	// Db日志实例
	if models.Config.Log.DbLogEnable {
		param.Filename = filepath.Join(baseLogDir, fmt.Sprintf("/%s-db.log", appName))
		if DatabaseLogger, err = newLogger(param); err != nil {
			return
		}

		param.Filename = filepath.Join(baseLogDir, fmt.Sprintf("/%s-workflow-db.log", appName))
		if WorkflowDatabaseLogger, err = newLogger(param); err != nil {
			return
		}
	}

	// 编排执行日志实例
	param.Filename = filepath.Join(baseLogDir, fmt.Sprintf("/%s-workflow.log", appName))
	if WorkflowLogger, err = newLogger(param); err != nil {
		return
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
	syncLogger(MetricLogger)
	syncLogger(DatabaseLogger)
	syncLogger(WorkflowLogger)
	syncLogger(WorkflowDatabaseLogger)
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
