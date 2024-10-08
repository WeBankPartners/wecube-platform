package database

import (
	"context"
	"encoding/base64"
	"encoding/hex"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/cipher"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"strings"
	"time"

	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/encrypt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

func QueryResourceServer(ctx context.Context, param *models.QueryRequestParam) (result *models.ResourceServerListPageData, err error) {
	result = &models.ResourceServerListPageData{PageInfo: &models.PageInfo{}, Contents: []*models.ResourceServer{}}
	for _, v := range param.Filters {
		if v.Name == "isAllocated" {
			if fmt.Sprintf("%s", v.Value) == "true" {
				v.Value = true
			}
		}
	}
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
	nowTime := time.Now()
	for _, v := range params {
		if decodePwd, tmpErr := DecodeUIPassword(ctx, v.LoginPassword); tmpErr != nil {
			log.Logger.Info("try to decode ui password fail", log.Error(tmpErr))
		} else {
			v.LoginPassword = decodePwd
		}
		v.Id = "rs_ser_" + guid.CreateGuid()
		if !strings.HasPrefix(v.LoginPassword, models.AESPrefix) {
			enPwd := encrypt.EncryptWithAesECB(v.LoginPassword, models.Config.Plugin.ResourcePasswordSeed, v.Name)
			v.LoginPassword = models.AESPrefix + enPwd
		}
		actions = append(actions, &db.ExecAction{Sql: "insert into resource_server (id,created_by,created_date,host,is_allocated,login_password,login_username,name,port,purpose,status,`type`,updated_by,updated_date,login_mode) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
			v.Id, v.CreatedBy, nowTime, v.Host, v.IsAllocated, v.LoginPassword, v.LoginUsername, v.Name, v.Port, v.Purpose, v.Status, v.Type, v.UpdatedBy, nowTime, v.LoginMode,
		}})
	}
	err = db.Transaction(actions, ctx)
	return
}

func UpdateResourceServer(ctx context.Context, params []*models.ResourceServer) (err error) {
	var actions []*db.ExecAction
	nowTime := time.Now()
	for _, v := range params {
		if decodePwd, tmpErr := DecodeUIPassword(ctx, v.LoginPassword); tmpErr != nil {
			log.Logger.Info("try to decode ui password fail", log.Error(tmpErr))
		} else {
			v.LoginPassword = decodePwd
		}
		if !strings.HasPrefix(v.LoginPassword, models.AESPrefix) {
			enPwd := encrypt.EncryptWithAesECB(v.LoginPassword, models.Config.Plugin.ResourcePasswordSeed, v.Name)
			v.LoginPassword = models.AESPrefix + enPwd
		}
		actions = append(actions, &db.ExecAction{Sql: "update resource_server set host=?,is_allocated=?,login_password=?,login_username=?,name=?,port=?,purpose=?,status=?,`type`=?,updated_by=?,updated_date=?,login_mode=? where id=?", Param: []interface{}{
			v.Host, v.IsAllocated, v.LoginPassword, v.LoginUsername, v.Name, v.Port, v.Purpose, v.Status, v.Type, v.UpdatedBy, nowTime, v.LoginMode, v.Id,
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

func GetResourceServerByIp(hostIp string) (resourceServer *models.ResourceServer, err error) {
	var resourceServerRows []*models.ResourceServer
	err = db.MysqlEngine.SQL("select id,is_allocated,login_password,login_username,login_mode,name,port,`type`,host from resource_server where host=? and `type`='docker'", hostIp).Find(&resourceServerRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(resourceServerRows) == 0 {
		err = exterror.Catch(exterror.New().DatabaseQueryEmptyError, err)
		return
	}
	resourceServer = resourceServerRows[0]
	if strings.HasPrefix(resourceServer.LoginPassword, models.AESPrefix) {
		resourceServer.LoginPassword = encrypt.DecryptWithAesECB(resourceServer.LoginPassword[5:], models.Config.Plugin.ResourcePasswordSeed, resourceServer.Name)
	}
	return
}

func GetResourceServerById(resId string) (resourceServer *models.ResourceServer, err error) {
	var resourceServerRows []*models.ResourceServer
	err = db.MysqlEngine.SQL("select id,is_allocated,login_password,login_username,login_mode,name,port,`type`,host from resource_server where `id`=?", resId).Find(&resourceServerRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(resourceServerRows) == 0 {
		err = exterror.Catch(exterror.New().DatabaseQueryEmptyError, err)
		return
	}
	resourceServer = resourceServerRows[0]
	if strings.HasPrefix(resourceServer.LoginPassword, models.AESPrefix) {
		resourceServer.LoginPassword = encrypt.DecryptWithAesECB(resourceServer.LoginPassword[5:], models.Config.Plugin.ResourcePasswordSeed, resourceServer.Name)
	}
	return
}

func DecodeUIPassword(ctx context.Context, inputValue string) (output string, err error) {
	if inputValue == "" {
		return
	}
	var seed string
	if seed, err = GetEncryptSeed(ctx); err != nil {
		return
	}
	if pwdBytes, pwdErr := base64.StdEncoding.DecodeString(inputValue); pwdErr == nil {
		inputValue = hex.EncodeToString(pwdBytes)
	} else {
		err = fmt.Errorf("base64 decode input data fail,%s ", pwdErr.Error())
		return
	}
	output, err = decodeUIAesPassword(seed, inputValue)
	return
}

func decodeUIAesPassword(seed, password string) (decodePwd string, err error) {
	unixTime := time.Now().Unix() / 100
	decodePwd, err = cipher.AesDePasswordWithIV(seed, password, fmt.Sprintf("%d", unixTime*100000000))
	if err != nil {
		unixTime = unixTime - 1
		decodePwd, err = cipher.AesDePasswordWithIV(seed, password, fmt.Sprintf("%d", unixTime*100000000))
	}
	if err != nil {
		err = fmt.Errorf("aes decode with iv fail,%s ", err.Error())
	}
	return
}
