package database

import (
	"context"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"time"
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
		param = append(param, condition.Scope)
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

func RegisterPlugin(ctx context.Context, name, version, operator string) (err error) {
	var actions []*db.ExecAction
	//actions = append(actions, &db.ExecAction{Sql: "update plugin_packages set status=? where name=? and status=?", Param: []interface{}{models.PluginStatusUnRegistered, name, models.PluginStatusRegistered}})
	actions = append(actions, &db.ExecAction{Sql: "update plugin_packages set status=?,updated_by=?,updated_time=? where name=? and `version`=?", Param: []interface{}{models.PluginStatusRegistered, operator, time.Now(), name, version}})
	actions = append(actions, &db.ExecAction{Sql: "update system_variables set status='inactive' where package_name=? and status='active'", Param: []interface{}{name}})
	actions = append(actions, &db.ExecAction{Sql: "update system_variables set status='active' where source=?", Param: []interface{}{fmt.Sprintf("%s__%s", name, version)}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func GetSystemVariable(ctx context.Context, name string) (result string, err error) {
	variable := &models.SystemVariables{}
	var found bool
	found, err = db.MysqlEngine.Context(ctx).Table(variable).Where("name=?", name).And("status=?", "active").Get(variable)
	if err != nil {
		return result, exterror.Catch(exterror.New().DatabaseQueryError, err)
	}
	if !found {
		return
	}
	result = variable.Value
	if result == "" {
		result = variable.DefaultValue
	}
	return
}

func GetSystemVariableScope() (result []string, err error) {
	queryResult, queryErr := db.MysqlEngine.QueryString("select distinct `scope` from system_variables")
	if queryErr != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
	} else {
		for _, v := range queryResult {
			result = append(result, v["scope"])
		}
	}
	return
}

func DeactivateSystemVariablesByPackage(ctx context.Context, name, version string) (err error) {
	session := db.MysqlEngine.NewSession().Context(ctx)
	defer session.Close()
	err = session.Begin()
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	updateData := make(map[string]interface{})
	updateData["status"] = models.SystemVariableInactive
	sourceValue := fmt.Sprintf("%s__%s", name, version)
	_, err = session.Table(new(models.SystemVariables)).Where("package_name = ?", name).And("source = ?", sourceValue).Update(updateData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	err = session.Commit()
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	return
}

func GetEncryptSeed(ctx context.Context) (encryptSeed string, err error) {
	encryptSeed, err = GetSystemVariable(ctx, models.SysVarEncryptSeed)
	if err != nil {
		return
	}
	if encryptSeed == "" {
		encryptSeed = models.Config.EncryptSeed
	}
	return
}
