package db

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"reflect"
	"strconv"
	"strings"
	"time"

	_ "github.com/go-sql-driver/mysql"
	"go.uber.org/zap"
	"xorm.io/core"
	"xorm.io/xorm"
	xorm_log "xorm.io/xorm/log"
)

var (
	Engine *xorm.Engine
	_      xorm_log.ContextLogger = &dbContextLogger{}
)

func InitDatabase() error {
	connStr := fmt.Sprintf("%s:%s@%s(%s)/%s?collation=utf8mb4_unicode_ci&allowNativePasswords=true",
		model.Config.Database.User, model.Config.Database.Password, "tcp", fmt.Sprintf("%s:%s", model.Config.Database.Server, model.Config.Database.Port), model.Config.Database.DataBase)
	engine, err := xorm.NewEngine("mysql", connStr)
	if err != nil {
		log.Logger.Error("Init database connect fail", log.Error(err))
		return err
	}
	engine.SetMaxIdleConns(model.Config.Database.MaxIdle)
	engine.SetMaxOpenConns(model.Config.Database.MaxOpen)
	engine.SetConnMaxLifetime(time.Duration(model.Config.Database.Timeout) * time.Second)
	if model.Config.Log.DbLogEnable {
		engine.SetLogger(&dbContextLogger{LogLevel: 1, ShowSql: true, Logger: log.DatabaseLogger})
	}
	// 使用驼峰式映射
	engine.SetMapper(core.SnakeMapper{})
	Engine = engine
	log.Logger.Info("Success init database connect !!")
	return nil
}

/*type dbLogger struct {
	LogLevel xorm_log.LogLevel
	ShowSql  bool
	Logger   *zap.Logger
}

func (d *dbLogger) Debug(v ...interface{}) {
	d.Logger.Debug(fmt.Sprint(v...))
}

func (d *dbLogger) Debugf(format string, v ...interface{}) {
	d.Logger.Debug(fmt.Sprintf(format, v...))
}

func (d *dbLogger) Error(v ...interface{}) {
	d.Logger.Error(fmt.Sprint(v...))
}

func (d *dbLogger) Errorf(format string, v ...interface{}) {
	d.Logger.Error(fmt.Sprintf(format, v...))
}

func (d *dbLogger) Info(v ...interface{}) {
	d.Logger.Info(fmt.Sprint(v...))
}

func (d *dbLogger) Infof(format string, v ...interface{}) {
	if len(v) < 4 {
		d.Logger.Info(fmt.Sprintf(format, v...))
		return
	}
	var costMs float64 = 0
	costTime := fmt.Sprintf("%s", v[3])
	if strings.Contains(costTime, "µs") {
		costMs, _ = strconv.ParseFloat(strings.ReplaceAll(costTime, "µs", ""), 64)
		costMs = costMs / 1000
	} else if strings.Contains(costTime, "ms") {
		costMs, _ = strconv.ParseFloat(costTime[:len(costTime)-2], 64)
	} else if strings.Contains(costTime, "s") && !strings.Contains(costTime, "m") {
		costMs, _ = strconv.ParseFloat(costTime[:len(costTime)-1], 64)
		costMs = costMs * 1000
	} else {
		costTime = costTime[:len(costTime)-1]
		mIndex := strings.Index(costTime, "m")
		minTime, _ := strconv.ParseFloat(costTime[:mIndex], 64)
		secTime, _ := strconv.ParseFloat(costTime[mIndex+1:], 64)
		costMs = (minTime*60 + secTime) * 1000
	}
	d.Logger.Info("db_log", log.String("sql", fmt.Sprintf("%s", v[1])), log.String("param", fmt.Sprintf("%v", v[2])), log.Float64("cost_ms", costMs))
}

func (d *dbLogger) Warn(v ...interface{}) {
	d.Logger.Warn(fmt.Sprint(v...))
}

func (d *dbLogger) Warnf(format string, v ...interface{}) {
	d.Logger.Warn(fmt.Sprintf(format, v...))
}

func (d *dbLogger) Level() xorm_log.LogLevel {
	return d.LogLevel
}

func (d *dbLogger) SetLevel(l xorm_log.LogLevel) {
	d.LogLevel = l
}

func (d *dbLogger) ShowSQL(b ...bool) {
	d.ShowSql = b[0]
}

func (d *dbLogger) IsShowSQL() bool {
	return d.ShowSql
}*/

type dbContextLogger struct {
	LogLevel xorm_log.LogLevel
	ShowSql  bool
	Logger   *zap.Logger
}

func (d *dbContextLogger) BeforeSQL(ctx xorm_log.LogContext) {
}

func (d *dbContextLogger) AfterSQL(ctx xorm_log.LogContext) {
	/*	t := ctx.Ctx.Value(constant.TransactionId)
		transactionId := "NoTransactionId"
		if tmpTransactionId, ok := t.(string); ok {
			transactionId = tmpTransactionId
		}
	*/var costMs float64 = 0
	costTime := fmt.Sprintf("%s", ctx.ExecuteTime)
	if strings.Contains(costTime, "µs") {
		costMs, _ = strconv.ParseFloat(strings.ReplaceAll(costTime, "µs", ""), 64)
		costMs = costMs / 1000
	} else if strings.Contains(costTime, "ms") {
		costMs, _ = strconv.ParseFloat(costTime[:len(costTime)-2], 64)
	} else if strings.Contains(costTime, "s") && !strings.Contains(costTime, "m") {
		costMs, _ = strconv.ParseFloat(costTime[:len(costTime)-1], 64)
		costMs = costMs * 1000
	} else {
		costTime = costTime[:len(costTime)-1]
		mIndex := strings.Index(costTime, "m")
		minTime, _ := strconv.ParseFloat(costTime[:mIndex], 64)
		secTime, _ := strconv.ParseFloat(costTime[mIndex+1:], 64)
		costMs = (minTime*60 + secTime) * 1000
	}
	d.Logger.Info("sql:"+ctx.SQL, log.String("param", fmt.Sprintf("%v", ctx.Args)), log.Float64("cost_ms", costMs))
}

func (d *dbContextLogger) Debugf(format string, v ...interface{}) {
	d.Logger.Debug(fmt.Sprintf(format, v...))
}

func (d *dbContextLogger) Errorf(format string, v ...interface{}) {
	d.Logger.Debug(fmt.Sprintf(format, v...))
}

func (d *dbContextLogger) Infof(format string, v ...interface{}) {
	d.Logger.Debug(fmt.Sprintf(format, v...))
}

func (d *dbContextLogger) Warnf(format string, v ...interface{}) {
	d.Logger.Debug(fmt.Sprintf(format, v...))
}

func (d *dbContextLogger) Level() xorm_log.LogLevel {
	return d.LogLevel
}

func (d *dbContextLogger) SetLevel(l xorm_log.LogLevel) {
	d.LogLevel = l
}

func (d *dbContextLogger) ShowSQL(b ...bool) {
	d.ShowSql = b[0]
}

func (d *dbContextLogger) IsShowSQL() bool {
	return d.ShowSql
}

/*
	func DBCtx(transactionId string) context.Context {
		return context.WithValue(context.Background(), constant.TransactionId, transactionId)
	}
*/
func CheckDbConnection() (err error) {
	_, err = Engine.QueryString("select 1=1")
	return err
}

/*func TransFiltersToSQL(queryParam *model.QueryRequestParam, transParam *model.TransFiltersParam) (filterSql string, orderSql string, param []interface{}) {
	filterSql = "1=1"
	for _, filter := range queryParam.Filters {
		if transParam.KeyMap[filter.Name] == "" || transParam.KeyMap[filter.Name] == "-" {
			continue
		}
		if filter.Operator == "eq" {
			filterSql += fmt.Sprintf(" AND %s%s=? ", transParam.Prefix, transParam.KeyMap[filter.Name])
			param = append(param, filter.Value)
		} else if filter.Operator == "like" {
			filterSql += fmt.Sprintf(" AND %s%s LIKE ? ", transParam.Prefix, transParam.KeyMap[filter.Name])
			param = append(param, fmt.Sprintf("%%%s%%", filter.Value))
		} else if filter.Operator == "in" || filter.Operator == "notIn" {
			inValueList := filter.Value.([]interface{})
			inValueStringList := []string{}
			for _, inValueInterfaceObj := range inValueList {
				if inValueInterfaceObj == nil {
					inValueStringList = append(inValueStringList, "")
				} else {
					inValueStringList = append(inValueStringList, inValueInterfaceObj.(string))
				}
			}
			tmpSpecSql, tmpListParams := createListParams(inValueStringList, "")
			if tmpSpecSql == "" {
				tmpSpecSql = "''"
			}
			if filter.Operator == "in" {
				filterSql += fmt.Sprintf(" AND %s%s in (%s) ", transParam.Prefix, transParam.KeyMap[filter.Name], tmpSpecSql)
			} else {
				filterSql += fmt.Sprintf(" AND %s%s not in (%s) ", transParam.Prefix, transParam.KeyMap[filter.Name], tmpSpecSql)
			}
			param = append(param, tmpListParams...)
		} else if filter.Operator == "lt" {
			filterSql += fmt.Sprintf(" AND %s%s<=? ", transParam.Prefix, transParam.KeyMap[filter.Name])
			param = append(param, filter.Value)
		} else if filter.Operator == "gt" {
			filterSql += fmt.Sprintf(" AND %s%s>=? ", transParam.Prefix, transParam.KeyMap[filter.Name])
			param = append(param, filter.Value)
		} else if filter.Operator == "neq" {
			filterSql += fmt.Sprintf(" AND %s%s!=? ", transParam.Prefix, transParam.KeyMap[filter.Name])
			param = append(param, filter.Value)
		} else if filter.Operator == "notNull" {
			filterSql += fmt.Sprintf(" AND %s%s is not null ", transParam.Prefix, transParam.KeyMap[filter.Name])
		} else if filter.Operator == "null" {
			filterSql += fmt.Sprintf(" AND %s%s is null ", transParam.Prefix, transParam.KeyMap[filter.Name])
		}
	}
	if len(queryParam.Sorting) > 0 {
		var sortFilterList []string
		for _, sortObj := range queryParam.Sorting {
			if transParam.KeyMap[sortObj.Field] == "" || transParam.KeyMap[sortObj.Field] == "-" {
				sortObj.Field = transParam.PrimaryKey
			} else {
				sortObj.Field = transParam.KeyMap[sortObj.Field]
			}
			if sortObj.Asc {
				sortFilterList = append(sortFilterList, fmt.Sprintf("%s%s ASC", transParam.Prefix, sortObj.Field))
			} else {
				sortFilterList = append(sortFilterList, fmt.Sprintf("%s%s DESC", transParam.Prefix, sortObj.Field))
			}
		}
		if len(sortFilterList) > 0 {
			orderSql += fmt.Sprintf(" %s ", strings.Join(sortFilterList, ","))
		}
	}
	return
}

func InitFilterParam(filterParam *model.TransFiltersParam) *model.TransFiltersParam {
	if filterParam.Prefix != "" && !strings.HasSuffix(filterParam.Prefix, ".") {
		filterParam.Prefix = filterParam.Prefix + "."
	}
	if filterParam.IsStruct {
		filterParam.KeyMap, filterParam.PrimaryKey = getJsonToDbMap(filterParam.StructObj)
	}
	return filterParam
}*/

func createListParams(inputList []string, prefix string) (specSql string, paramList []interface{}) {
	if len(inputList) > 0 {
		var specList []string
		for _, v := range inputList {
			specList = append(specList, "?")
			paramList = append(paramList, prefix+v)
		}
		specSql = strings.Join(specList, ",")
	}
	return
}

func getJsonToDbMap(input interface{}) (resultMap map[string]string, idKeyName string) {
	resultMap = make(map[string]string)
	t := reflect.TypeOf(input)
	for i := 0; i < t.NumField(); i++ {
		resultMap[t.Field(i).Tag.Get("json")] = t.Field(i).Tag.Get("db")
		if i == 0 {
			idKeyName = t.Field(i).Tag.Get("db")
		}
	}
	return resultMap, idKeyName
}
