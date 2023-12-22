package database

import (
	"context"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

func QueryResourceServer(ctx context.Context, param *models.QueryRequestParam) (result *models.ResourceServerListPageData, err error) {
	result = &models.ResourceServerListPageData{PageInfo: &models.PageInfo{}, Contents: []*models.ResourceServer{}}
	filterSql, _, queryParam := transFiltersToSQL(param, &models.TransFiltersParam{IsStruct: true, StructObj: models.ResourceServer{}})
	baseSql := db.CombineDBSql("SELECT * FROM resource_server WHERE 1=1 ", filterSql)
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

func QueryResourceItem(ctx context.Context, param *models.QueryRequestParam) (result *models.ResourceItemListPageData, err error) {
	result = &models.ResourceItemListPageData{PageInfo: &models.PageInfo{}, Contents: []*models.ResourceItem{}}
	filterSql, _, queryParam := transFiltersToSQL(param, &models.TransFiltersParam{IsStruct: true, StructObj: models.ResourceItem{}})
	baseSql := db.CombineDBSql("SELECT * FROM resource_item WHERE 1=1 ", filterSql)
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

func CreateResourceServer(ctx context.Context, params []*models.ResourceServer) (err error) {
	var actions []*db.ExecAction
	for _, v := range params {
		v.Id = "rs_ser_" + guid.CreateGuid()
		actions = append(actions, &db.ExecAction{Sql: "insert into resource_server (id,created_by,created_date,host,is_allocated,login_password,login_username,name,port,purpose,status,`type`,updated_by,updated_date,login_mode) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
			v.Id, v.CreatedBy, v.CreatedDate, v.Host, v.IsAllocated, v.LoginPassword, v.LoginUsername, v.Name, v.Port, v.Purpose, v.Status, v.Type, v.UpdatedBy, v.UpdatedDate, v.LoginMode,
		}})
	}
	err = db.Transaction(actions, ctx)
	return
}

func UpdateResourceServer(ctx context.Context, params []*models.ResourceServer) (err error) {
	var actions []*db.ExecAction
	for _, v := range params {
		actions = append(actions, &db.ExecAction{Sql: "update resource_server set created_by=?,created_date=?,host=?,is_allocated=?,login_password=?,login_username=?,name=?,port=?,purpose=?,status=?,`type`=?,updated_by=?,updated_date=?,login_mode=? where id=?", Param: []interface{}{
			v.CreatedBy, v.CreatedDate, v.Host, v.IsAllocated, v.LoginPassword, v.LoginUsername, v.Name, v.Port, v.Purpose, v.Status, v.Type, v.UpdatedBy, v.UpdatedDate, v.LoginMode, v.Id,
		}})
	}
	err = db.Transaction(actions, ctx)
	return
}

func DeleteResourceServer(ctx context.Context, params []*models.ResourceServer) (err error) {
	var actions []*db.ExecAction
	for _, v := range params {
		actions = append(actions, &db.ExecAction{Sql: "delete from resource_server where id=?", Param: []interface{}{v.Id}})
	}
	err = db.Transaction(actions, ctx)
	return
}

func GetAvailableContainerHost() (availableHost []string, err error) {
	var resourceServerRows []*models.ResourceServer
	err = db.MysqlEngine.SQL("select host from resource_server where `type`='docker' and status='active'").Find(&resourceServerRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	for _, v := range resourceServerRows {
		availableHost = append(availableHost, v.Host)
	}
	return
}
