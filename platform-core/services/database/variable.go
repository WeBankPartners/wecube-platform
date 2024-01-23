package database

import (
	"context"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

func QuerySystemVariables(ctx context.Context, param *models.QueryRequestParam) (result *models.SystemVariablesListPageData, err error) {
	result = &models.SystemVariablesListPageData{PageInfo: &models.PageInfo{}, Contents: []*models.SystemVariables{}}
	filterSql, _, queryParam := transFiltersToSQL(param, &models.TransFiltersParam{IsStruct: true, StructObj: models.SystemVariables{}})
	baseSql := db.CombineDBSql("SELECT * FROM system_variables WHERE 1=1 ", filterSql)
	if param.Paging {
		result.PageInfo = &models.PageInfo{StartIndex: param.Pageable.StartIndex, PageSize: param.Pageable.PageSize, TotalRows: queryCount(ctx, baseSql, queryParam...)}
		pageSql, pageParam := transPageInfoToSQL(*param.Pageable)
		baseSql = db.CombineDBSql(baseSql, pageSql)
		queryParam = append(queryParam, pageParam...)
	}
	err = db.MysqlEngine.Context(ctx).SQL(baseSql, queryParam...).Find(&result.Contents)
	if err != nil {
		return result, exterror.Catch(exterror.New().DatabaseQueryError, err)
	}
	return
}

func QuerySystemVariablesByCondition(ctx context.Context, condition models.SystemVariablesQueryCondition) (list []*models.SystemVariables, err error) {
	var param []interface{}
	sql := "select * from system_variables where 1= 1"
	if condition.Name != "" {
		sql = sql + " and name = ?"
		param = append(param, condition.Name)
	}
	if condition.Status != "" {
		sql = sql + " and status = ?"
		param = append(param, condition.Status)
	}
	if condition.Scope != "" {
		sql = sql + " and scope = ?"
	}
	err = db.MysqlEngine.Context(ctx).SQL(sql, param...).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func CreateSystemVariables(ctx context.Context, params []*models.SystemVariables) (err error) {
	var actions []*db.ExecAction
	for _, v := range params {
		v.Id = "sys_var_" + guid.CreateGuid()
		actions = append(actions, &db.ExecAction{Sql: "INSERT INTO system_variables (id,package_name,name,value,default_value,`scope`,source,status) VALUES (?,?,?,?,?,?,?,?)", Param: []interface{}{
			v.Id, v.PackageName, v.Name, v.Value, v.DefaultValue, v.Scope, v.Source, v.Status,
		}})
	}
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func UpdateSystemVariables(ctx context.Context, params []*models.SystemVariables) (err error) {
	var actions []*db.ExecAction
	for _, v := range params {
		actions = append(actions, &db.ExecAction{Sql: "update system_variables set package_name=?,name=?,value=?,default_value=?,`scope`=?,source=?,status=? where id=?", Param: []interface{}{
			v.PackageName, v.Name, v.Value, v.DefaultValue, v.Scope, v.Source, v.Status, v.Id,
		}})
	}
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func DeleteSystemVariables(ctx context.Context, params []*models.SystemVariables) (err error) {
	var actions []*db.ExecAction
	for _, v := range params {
		actions = append(actions, &db.ExecAction{Sql: "delete from system_variables where id=?", Param: []interface{}{v.Id}})
	}
	err = db.Transaction(actions, ctx)
	return
}

func ActivePluginSystemVariable(ctx context.Context, name, version string) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "update system_variables set status='inactive' where package_name=? and status='active'", Param: []interface{}{name}})
	actions = append(actions, &db.ExecAction{Sql: "update system_variables set status='active' where source=?", Param: []interface{}{fmt.Sprintf("%s__%s", name, version)}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}
