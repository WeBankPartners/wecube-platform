package db

import (
	"context"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/constant"
	"reflect"
	"strconv"
	"strings"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"

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
		log.Error(nil, log.LOGGER_APP, "Init database connect fail", zap.Error(err))
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
	log.Info(nil, log.LOGGER_APP, "Success init database connect !!")
	return nil
}

type dbContextLogger struct {
	LogLevel xorm_log.LogLevel
	ShowSql  bool
	Logger   *zap.SugaredLogger
}

func (d *dbContextLogger) BeforeSQL(ctx xorm_log.LogContext) {
}

func (d *dbContextLogger) AfterSQL(ctx xorm_log.LogContext) {
	var costMs float64 = 0
	costTime := ctx.ExecuteTime.String()
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
	d.Logger.Infow("sql:"+ctx.SQL, zap.String("param", fmt.Sprintf("%v", ctx.Args)), zap.Float64("cost_ms", costMs))
}

func (d *dbContextLogger) Debugf(format string, v ...interface{}) {
	d.Logger.Debugw(fmt.Sprintf(format, v...))
}

func (d *dbContextLogger) Errorf(format string, v ...interface{}) {
	d.Logger.Errorw(fmt.Sprintf(format, v...))
}

func (d *dbContextLogger) Infof(format string, v ...interface{}) {
	d.Logger.Infow(fmt.Sprintf(format, v...))
}

func (d *dbContextLogger) Warnf(format string, v ...interface{}) {
	d.Logger.Warnw(fmt.Sprintf(format, v...))
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

func createListParams(inputList []string, prefix string) (filterSql string, filterParam []interface{}) {
	if len(inputList) > 0 {
		var specList []string
		for _, v := range inputList {
			specList = append(specList, "?")
			filterParam = append(filterParam, prefix+v)
		}
		filterSql = strings.Join(specList, ",")
	}
	return
}

func combineDBSql(input ...interface{}) string {
	var buf strings.Builder
	fmt.Fprint(&buf, input...)
	return buf.String()
}

func transFiltersToSQL(queryParam *model.QueryRequestParam, transParam *model.TransFiltersParam) (filterSql, queryColumn string, param []interface{}) {
	if transParam.Prefix != "" && !strings.HasSuffix(transParam.Prefix, ".") {
		transParam.Prefix = transParam.Prefix + "."
	}
	if transParam.IsStruct {
		transParam.KeyMap, transParam.PrimaryKey = getJsonToXormMap(transParam.StructObj)
	}
	for _, filter := range queryParam.Filters {
		if transParam.KeyMap[filter.Name] == "" || transParam.KeyMap[filter.Name] == "-" {
			continue
		}
		if filter.Operator == "eq" {
			filterSql += fmt.Sprintf(" AND %s%s=? ", transParam.Prefix, transParam.KeyMap[filter.Name])
			param = append(param, filter.Value)
		} else if filter.Operator == "contains" {
			filterSql += fmt.Sprintf(" AND %s%s LIKE ? ", transParam.Prefix, transParam.KeyMap[filter.Name])
			param = append(param, fmt.Sprintf("%%%s%%", filter.Value))
		} else if filter.Operator == "in" || filter.Operator == "notIn" {
			inValueStringList := ParseFilterInValue(filter)
			tmpSpecSql, tmpListParams := createListParams(inValueStringList, "")
			if tmpSpecSql == "" {
				tmpSpecSql = "''"
			}
			if filter.Operator == "in" {
				if len(inValueStringList) == 1 && filter.Name == "status" {
					if inValueStringList[0] == string(constant.UserRolePermissionStatusInEffect) {
						filterSql += " AND status='approve' AND (expire_time > ? or expire_time is null) AND EXISTS ( SELECT * from auth_sys_user_role WHERE role_apply = ap.id AND is_deleted = 0) "
						param = append(param, time.Now().Format(constant.DateTimeFormat))
					} else if inValueStringList[0] == string(constant.UserRolePermissionStatusExpire) {
						filterSql += " AND status='approve' AND expire_time <= ?"
						param = append(param, time.Now().Format(constant.DateTimeFormat))
					} else if inValueStringList[0] == string(constant.UserRolePermissionStatusDeleted) {
						filterSql += " AND status='approve' AND EXISTS ( SELECT * from auth_sys_user_role WHERE role_apply = ap.id AND is_deleted = 1) "
					} else {
						filterSql += fmt.Sprintf(" AND %s%s in (%s) ", transParam.Prefix, transParam.KeyMap[filter.Name], tmpSpecSql)
						param = append(param, tmpListParams...)
					}
				} else {
					filterSql += fmt.Sprintf(" AND %s%s in (%s) ", transParam.Prefix, transParam.KeyMap[filter.Name], tmpSpecSql)
					param = append(param, tmpListParams...)
				}
			} else {
				filterSql += fmt.Sprintf(" AND %s%s not in (%s) ", transParam.Prefix, transParam.KeyMap[filter.Name], tmpSpecSql)
				param = append(param, tmpListParams...)
			}
		} else if filter.Operator == "lte" {
			filterSql += fmt.Sprintf(" AND %s%s<=? ", transParam.Prefix, transParam.KeyMap[filter.Name])
			param = append(param, filter.Value)
		} else if filter.Operator == "lt" {
			filterSql += fmt.Sprintf(" AND %s%s<? ", transParam.Prefix, transParam.KeyMap[filter.Name])
			param = append(param, filter.Value)
		} else if filter.Operator == "gte" {
			filterSql += fmt.Sprintf(" AND %s%s>=? ", transParam.Prefix, transParam.KeyMap[filter.Name])
			param = append(param, filter.Value)
		} else if filter.Operator == "gt" {
			filterSql += fmt.Sprintf(" AND %s%s>? ", transParam.Prefix, transParam.KeyMap[filter.Name])
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
		filterSql += " ORDER BY "
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
			filterSql += fmt.Sprintf(" %s ", strings.Join(sortFilterList, ","))
		}
	}
	if len(queryParam.ResultColumns) > 0 {
		for _, resultColumn := range queryParam.ResultColumns {
			if transParam.KeyMap[resultColumn] == "" || transParam.KeyMap[resultColumn] == "-" {
				continue
			}
			queryColumn += fmt.Sprintf("%s%s,", transParam.Prefix, transParam.KeyMap[resultColumn])
		}
	}
	if queryColumn == "" {
		queryColumn = " * "
	} else {
		queryColumn = queryColumn[:len(queryColumn)-1]
	}
	return
}

func getJsonToXormMap(input interface{}) (resultMap map[string]string, idKeyName string) {
	resultMap = make(map[string]string)
	t := reflect.TypeOf(input)
	for i := 0; i < t.NumField(); i++ {
		resultMap[t.Field(i).Tag.Get("json")] = t.Field(i).Tag.Get("xorm")
		if i == 0 {
			idKeyName = t.Field(i).Tag.Get("xorm")
		}
	}
	return resultMap, idKeyName
}

func queryCount(ctx context.Context, sql string, params ...interface{}) int {
	resultMap := make(map[string]interface{})
	_, err := Engine.Context(ctx).SQL(combineDBSql("SELECT COUNT(1) FROM ( ", sql, " ) sub_query"), params...).Get(&resultMap)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "Query sql count message fail", zap.Error(err))
		return 0
	}
	if countV, b := resultMap["COUNT(1)"]; b {
		countIntV, _ := strconv.Atoi(fmt.Sprintf("%d", countV))
		return countIntV
	}
	return 0
}

func transPageInfoToSQL(pageInfo model.PageInfo) (pageSql string, param []interface{}) {
	pageSql = " LIMIT ?,? "
	param = append(param, pageInfo.StartIndex)
	param = append(param, pageInfo.PageSize)
	return
}

func ParseFilterInValue(filter *model.QueryRequestFilterObj) []string {
	inValueStringList := []string{}
	if filter.Operator == "in" || filter.Operator == "notIn" {
		fValueType := reflect.TypeOf(filter.Value).String()
		if fValueType == "[]string" {
			inValueStringList = filter.Value.([]string)
		} else if strings.HasPrefix(fValueType, "[]interface") {
			inValueList := filter.Value.([]interface{})
			for _, inValueInterfaceObj := range inValueList {
				if inValueInterfaceObj == nil {
					inValueStringList = append(inValueStringList, "")
				} else {
					inValueStringList = append(inValueStringList, inValueInterfaceObj.(string))
				}
			}
		}
	}
	return inValueStringList
}
