package log

import (
	"fmt"
	"github.com/WeBankPartners/go-common-lib/logger"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/constant"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

// LoggerType 日志器类型
type LoggerType int

const (
	LOGGER_APP    LoggerType = iota // APP日志器
	LOGGER_ACCESS                   // 访问日志器
	LOGGER_DB                       // 数据库日志器
	LOGGER_METRIC                   // 指标日志器
)

// 获取日志器
func getLogger(loggerType LoggerType) *zap.SugaredLogger {
	switch loggerType {
	case LOGGER_APP:
		return Logger
	case LOGGER_ACCESS:
		return AccessLogger
	case LOGGER_DB:
		return DatabaseLogger
	case LOGGER_METRIC:
		return MetricLogger
	default:
		return nil
	}
}

// 添加上下文字段（业务流水号和交易流水号)
func addKeyValues(c *gin.Context, fields []interface{}) (result []interface{}) {
	if fields == nil {
		fields = make([]interface{}, 0)
	}
	if c != nil {
		fields = append(fields, zap.String(logger.HEADER_TRANSACTION_ID,
			c.GetHeader(constant.HEADER_BUSINESS_ID)))
		fields = append(fields, zap.String(logger.HEADER_REQUEST_ID,
			c.GetHeader(constant.HEADER_REQUEST_ID)))
	}
	return fields
}

func Debug(c *gin.Context, loggerType LoggerType, args ...interface{}) {
	logger := getLogger(loggerType)
	if logger == nil {
		return
	}
	logger.Debugw(args[0].(string), addKeyValues(c, args[1:])...)
}

func Debugf(c *gin.Context, loggerType LoggerType, template string,
	args ...interface{}) {

	logger := getLogger(loggerType)
	if logger == nil {
		return
	}
	logger.Debugw(fmt.Sprintf(template, args...), addKeyValues(c, nil)...)
}

func Info(c *gin.Context, loggerType LoggerType, args ...interface{}) {
	logger := getLogger(loggerType)
	if logger == nil {
		return
	}
	logger.Infow(args[0].(string), addKeyValues(c, args[1:])...)
}

func Infof(c *gin.Context, loggerType LoggerType, template string,
	args ...interface{}) {

	logger := getLogger(loggerType)
	if logger == nil {
		return
	}
	logger.Infow(fmt.Sprintf(template, args...), addKeyValues(c, nil)...)
}

func Warn(c *gin.Context, loggerType LoggerType, args ...interface{}) {
	logger := getLogger(loggerType)
	if logger == nil {
		return
	}
	logger.Warnw(args[0].(string), addKeyValues(c, args[1:])...)
}

func Warnf(c *gin.Context, loggerType LoggerType, template string,
	args ...interface{}) {

	logger := getLogger(loggerType)
	if logger == nil {
		return
	}
	logger.Warnw(fmt.Sprintf(template, args...), addKeyValues(c, nil)...)
}

func Error(c *gin.Context, loggerType LoggerType, args ...interface{}) {
	logger := getLogger(loggerType)
	if logger == nil {
		return
	}
	logger.Errorw(args[0].(string), addKeyValues(c, args[1:])...)
}

func Errorf(c *gin.Context, loggerType LoggerType, template string,
	args ...interface{}) {

	logger := getLogger(loggerType)
	if logger == nil {
		return
	}
	logger.Errorw(fmt.Sprintf(template, args...), addKeyValues(c, nil)...)
}

func DPanic(c *gin.Context, loggerType LoggerType, args ...interface{}) {
	logger := getLogger(loggerType)
	if logger == nil {
		return
	}
	logger.DPanicw(args[0].(string), addKeyValues(c, args[1:])...)
}

func DPanicf(c *gin.Context, loggerType LoggerType, template string,
	args ...interface{}) {

	logger := getLogger(loggerType)
	if logger == nil {
		return
	}
	logger.DPanicw(fmt.Sprintf(template, args...), addKeyValues(c, nil)...)
}

func Panic(c *gin.Context, loggerType LoggerType, args ...interface{}) {
	logger := getLogger(loggerType)
	if logger == nil {
		return
	}
	logger.Panicw(args[0].(string), addKeyValues(c, args[1:])...)
}

func Panicf(c *gin.Context, loggerType LoggerType, template string,
	args ...interface{}) {

	logger := getLogger(loggerType)
	if logger == nil {
		return
	}
	logger.Panicw(fmt.Sprintf(template, args...), addKeyValues(c, nil)...)
}

func Fatal(c *gin.Context, loggerType LoggerType, args ...interface{}) {
	logger := getLogger(loggerType)
	if logger == nil {
		return
	}
	logger.Fatalw(args[0].(string), addKeyValues(c, args[1:])...)
}

func Fatalf(c *gin.Context, loggerType LoggerType, template string,
	args ...interface{}) {

	logger := getLogger(loggerType)
	if logger == nil {
		return
	}
	logger.Fatalw(fmt.Sprintf(template, args...), addKeyValues(c, nil)...)
}
