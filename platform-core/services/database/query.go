package database

import (
	"context"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"reflect"
	"strconv"
	"strings"
)

func transFiltersToSQL(queryParam *models.QueryRequestParam, transParam *models.TransFiltersParam) (filterSql, queryColumn string, param []interface{}) {
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
			fValueType := reflect.TypeOf(filter.Value).String()
			inValueStringList := []string{}
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
			tmpSpecSql, tmpListParams := db.CreateListParams(inValueStringList, "")
			if tmpSpecSql == "" {
				tmpSpecSql = "''"
			}
			if filter.Operator == "in" {
				filterSql += fmt.Sprintf(" AND %s%s in (%s) ", transParam.Prefix, transParam.KeyMap[filter.Name], tmpSpecSql)
			} else {
				filterSql += fmt.Sprintf(" AND %s%s not in (%s) ", transParam.Prefix, transParam.KeyMap[filter.Name], tmpSpecSql)
			}
			param = append(param, tmpListParams...)
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
	_, err := db.MysqlEngine.Context(ctx).SQL(db.CombineDBSql("SELECT COUNT(1) FROM ( ", sql, " ) sub_query"), params...).Get(&resultMap)
	if err != nil {
		log.Logger.Error("Query sql count message fail", log.Error(err))
		return 0
	}
	if countV, b := resultMap["COUNT(1)"]; b {
		countIntV, _ := strconv.Atoi(fmt.Sprintf("%d", countV))
		return countIntV
	}
	return 0
}

func transPageInfoToSQL(pageInfo models.PageInfo) (pageSql string, param []interface{}) {
	pageSql = " LIMIT ?,? "
	param = append(param, pageInfo.StartIndex)
	param = append(param, pageInfo.PageSize)
	return
}
