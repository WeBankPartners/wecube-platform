package database

import (
	"context"
	"encoding/base64"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"strings"
	"time"

	"github.com/WeBankPartners/go-common-lib/cipher"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"go.uber.org/zap"

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
	result = &models.ResourceItemListPageData{PageInfo: &models.PageInfo{}, Contents: []*models.ResourceItemQueryRow{}}
	filterSql, _, queryParam := transFiltersToSQL(param, &models.TransFiltersParam{IsStruct: true, StructObj: models.ResourceItem{}})
	baseSql := db.CombineDBSql("SELECT * FROM resource_item WHERE 1=1 ", filterSql)
	if param.Paging {
		result.PageInfo = &models.PageInfo{StartIndex: param.Pageable.StartIndex, PageSize: param.Pageable.PageSize, TotalRows: queryCount(ctx, baseSql, queryParam...)}
		pageSql, pageParam := transPageInfoToSQL(*param.Pageable)
		baseSql = db.CombineDBSql(baseSql, pageSql)
		queryParam = append(queryParam, pageParam...)
	}
	var queryRows []*models.ResourceItem
	err = db.MysqlEngine.Context(ctx).SQL(baseSql, queryParam...).Find(&queryRows)
	if err != nil {
		return result, exterror.Catch(exterror.New().DatabaseQueryError, err)
	}
	resourceList, resourceErr := QueryResourceServer(ctx, &models.QueryRequestParam{Filters: []*models.QueryRequestFilterObj{}, Paging: false, Pageable: &models.PageInfo{}})
	if resourceErr != nil {
		err = resourceErr
		return
	}
	var instanceRows []*models.PluginMysqlInstances
	err = db.MysqlEngine.Context(ctx).SQL("select docker_instance_resource_id as 'resource_item_id' from plugin_instances union select resource_item_id from plugin_mysql_instances").Find(&instanceRows)
	if err != nil {
		return result, exterror.Catch(exterror.New().DatabaseQueryError, err)
	}
	for _, row := range queryRows {
		tmpRow := models.ResourceItemQueryRow{ResourceItem: *row}
		tmpRow.CreatedDateString = tmpRow.CreatedDate.Format(models.DateTimeFormat)
		tmpRow.UpdatedDateString = tmpRow.UpdatedDate.Format(models.DateTimeFormat)
		for _, resourceRow := range resourceList.Contents {
			if resourceRow.Id == row.ResourceServerId {
				tmpRow.ResourceServer = resourceRow.Host
				tmpRow.Port = resourceRow.Port
			}
		}
		for _, usedByInstance := range instanceRows {
			if usedByInstance.ResourceItemId == row.Id {
				tmpRow.Used = true
				break
			}
		}
		result.Contents = append(result.Contents, &tmpRow)
	}
	return
}

func CreateResourceServer(ctx context.Context, params []*models.ResourceServer) (err error) {
	var actions []*db.ExecAction
	nowTime := time.Now()
	for _, v := range params {
		if decodePwd, tmpErr := DecodeUIPassword(ctx, v.LoginPassword); tmpErr != nil {
			log.Info(nil, log.LOGGER_APP, "try to decode ui password fail", zap.Error(tmpErr))
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
			log.Info(nil, log.LOGGER_APP, "try to decode ui password fail", zap.Error(tmpErr))
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
	var specialAndChar = "&" + string([]byte{0x01})
	var tmpPwdIV string
	if splitCharList := strings.Split(inputValue, specialAndChar); len(splitCharList) > 1 {
		inputValue = splitCharList[0]
		tmpPwdIV = splitCharList[1]
	}
	if pwdBytes, pwdErr := base64.StdEncoding.DecodeString(inputValue); pwdErr == nil {
		inputValue = hex.EncodeToString(pwdBytes)
	} else {
		err = fmt.Errorf("base64 decode input data fail,%s ", pwdErr.Error())
		return
	}
	output, err = decodeAesPassword(seed, inputValue, tmpPwdIV)
	return
}

func decodeAesPassword(seed, password, ivValue string) (decodePwd string, err error) {
	if ivValue != "" {
		decodePwd, err = cipher.AesDePasswordWithIV(seed, password, ivValue)
		return
	}
	unixTime := time.Now().Unix() / 100
	ivValue = fmt.Sprintf("%d", unixTime*100000000)
	decodePwd, err = cipher.AesDePasswordWithIV(seed, password, ivValue)
	if err != nil {
		unixTime = unixTime - 1
		ivValue = fmt.Sprintf("%d", unixTime*100000000)
		decodePwd, err = cipher.AesDePasswordWithIV(seed, password, ivValue)
	}
	return
}

func CreateResourceItem(ctx context.Context, params []*models.ResourceItem, operator string) (err error) {
	var actions []*db.ExecAction
	nowTime := time.Now()
	var existResourceItemRows []*models.ResourceItem
	err = db.MysqlEngine.Context(ctx).SQL("select id,name,`type` from resource_item").Find(&existResourceItemRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	for _, v := range params {
		if v.Type != "mysql_database" {
			err = fmt.Errorf("item type %s illegal", v.Type)
			return
		}
		if v.ResourceServerId == "" {
			err = fmt.Errorf("resource server can not empty")
			return
		}
		for _, existRow := range existResourceItemRows {
			if existRow.Type == v.Type && existRow.Name == v.Name {
				err = fmt.Errorf("resourceItem type:%s name:%s already exists", v.Type, v.Name)
				return
			}
		}
		v.Id = "rs_item_" + guid.CreateGuid()
		if decodePwd, tmpErr := DecodeUIPassword(ctx, v.Password); tmpErr != nil {
			err = fmt.Errorf("try to decode ui password fail,%s ", tmpErr.Error())
			return
		} else {
			v.Password = decodePwd
		}
		if !strings.HasPrefix(v.Password, models.AESPrefix) {
			enPwd := encrypt.EncryptWithAesECB(v.Password, models.Config.Plugin.ResourcePasswordSeed, v.Name)
			v.Password = models.AESPrefix + enPwd
		}
		properties := models.MysqlResourceItemProperties{Username: v.Username, Password: v.Password}
		propertiesBytes, _ := json.Marshal(&properties)
		actions = append(actions, &db.ExecAction{Sql: "INSERT INTO resource_item (id,additional_properties,created_by,created_date,is_allocated,name,purpose,resource_server_id,status,`type`,`username`,`password`,updated_by,updated_date) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
			v.Id, string(propertiesBytes), operator, nowTime, 1, v.Name, v.Purpose, v.ResourceServerId, "created", "mysql_database", v.Username, v.Password, operator, nowTime,
		}})
	}
	err = db.Transaction(actions, ctx)
	return
}

func UpdateResourceItem(ctx context.Context, params []*models.ResourceItem, operator string) (err error) {
	var actions []*db.ExecAction
	nowTime := time.Now()
	for _, v := range params {
		if v.Type != "mysql_database" {
			err = fmt.Errorf("item type %s illegal", v.Type)
			return
		}
		if decodePwd, tmpErr := DecodeUIPassword(ctx, v.Password); tmpErr != nil {
			err = fmt.Errorf("try to decode ui password fail,%s ", tmpErr.Error())
			return
		} else {
			v.Password = decodePwd
		}
		if !strings.HasPrefix(v.Password, models.AESPrefix) {
			enPwd := encrypt.EncryptWithAesECB(v.Password, models.Config.Plugin.ResourcePasswordSeed, v.Name)
			v.Password = models.AESPrefix + enPwd
		}
		properties := models.MysqlResourceItemProperties{Username: v.Username, Password: v.Password}
		propertiesBytes, _ := json.Marshal(&properties)
		actions = append(actions, &db.ExecAction{Sql: "update resource_item set resource_server_id=?,name=?,additional_properties=?,`username`=?,`password`=?,is_allocated=?,purpose=?,updated_by=?,updated_date=? where id=?", Param: []interface{}{
			v.ResourceServerId, v.Name, string(propertiesBytes), v.Username, v.Password, v.IsAllocated, v.Purpose, operator, nowTime, v.Id,
		}})
		pluginMysqlInstanceRow, getMysqlInstanceErr := getPluginMysqlInstanceByItem(ctx, v.Id)
		if getMysqlInstanceErr != nil {
			err = getMysqlInstanceErr
			return
		}
		if pluginMysqlInstanceRow != nil {
			actions = append(actions, &db.ExecAction{Sql: "update plugin_mysql_instances set password=?,updated_time=? where id=?", Param: []interface{}{v.Password, nowTime, pluginMysqlInstanceRow.Id}})
		}
	}
	err = db.Transaction(actions, ctx)
	return
}

func DeleteResourceItem(ctx context.Context, params []*models.ResourceItem) (err error) {
	var actions []*db.ExecAction
	for _, v := range params {
		tmpQueryRows, tmpQueryErr := db.MysqlEngine.Context(ctx).QueryString("select id from resource_item where id=? and `type`='mysql_database'", v.Id)
		if tmpQueryErr != nil {
			err = fmt.Errorf("query resourct item table fail,%s ", tmpQueryErr.Error())
			return
		}
		if len(tmpQueryRows) == 0 {
			err = fmt.Errorf("id %s is not mysql type item", v.Id)
			return
		}
		pluginMysqlInstanceRow, getMysqlInstanceErr := getPluginMysqlInstanceByItem(ctx, v.Id)
		if getMysqlInstanceErr != nil {
			err = getMysqlInstanceErr
			return
		}
		if pluginMysqlInstanceRow != nil {
			err = fmt.Errorf("item:%s already used by pluginMysqlInstance:%s ", v.Id, pluginMysqlInstanceRow.Id)
			return
		}
		actions = append(actions, &db.ExecAction{Sql: "delete from resource_item where id=?", Param: []interface{}{v.Id}})
	}
	err = db.Transaction(actions, ctx)
	return
}

func getPluginMysqlInstanceByItem(ctx context.Context, resourceItemId string) (pluginMysqlInstanceRow *models.PluginMysqlInstances, err error) {
	var pluginMysqlInstanceRows []*models.PluginMysqlInstances
	err = db.MysqlEngine.Context(ctx).SQL("select id from plugin_mysql_instances where resource_item_id=?", resourceItemId).Find(&pluginMysqlInstanceRows)
	if err != nil {
		err = fmt.Errorf("query plugin mysql instance by resource item fail,%s ", err.Error())
		return
	}
	if len(pluginMysqlInstanceRows) > 0 {
		pluginMysqlInstanceRow = pluginMysqlInstanceRows[0]
	}
	return
}

func GetResourceItem(ctx context.Context, resourceType, name string, isAllocated bool) (resourceItemRows []*models.ResourceItem, err error) {
	baseSql := "select * from resource_item where "
	var filterSql []string
	var filterParams []interface{}
	if resourceType != "" {
		filterSql = append(filterSql, "`type`=?")
		filterParams = append(filterParams, resourceType)
	}
	if name != "" {
		filterSql = append(filterSql, "`name`=?")
		filterParams = append(filterParams, name)
	}
	if isAllocated {
		filterSql = append(filterSql, "`is_allocated`=1")
	} else {
		filterSql = append(filterSql, "`is_allocated`=0")
	}
	err = db.MysqlEngine.Context(ctx).SQL(baseSql+strings.Join(filterSql, " and "), filterParams...).Find(&resourceItemRows)
	if err != nil {
		err = fmt.Errorf("query resource item fail,%s ", err.Error())
	} else {
		for _, row := range resourceItemRows {
			if strings.HasPrefix(row.Password, models.AESPrefix) {
				row.Password = encrypt.DecryptWithAesECB(row.Password[5:], models.Config.Plugin.ResourcePasswordSeed, row.Name)
			}
		}
	}
	return
}

func ValidateResourceServer(ctx context.Context, resourceServer *models.ResourceServer) (err error) {
	if resourceServer.Type == "docker" {
		return
	}
	if resourceServer.Status == "active" && resourceServer.IsAllocated {
		queryResult, queryErr := db.MysqlEngine.Context(ctx).QueryString("select id from resource_server where `type`=? and status='active' and is_allocated=1", resourceServer.Type)
		if queryErr != nil {
			err = fmt.Errorf("query resource server fail,%s ", queryErr.Error())
			return
		}
		if len(queryResult) > 0 {
			legalFlag := false
			if resourceServer.Id != "" {
				if len(queryResult) == 1 {
					if queryResult[0]["id"] == resourceServer.Id {
						legalFlag = true
					}
				}
			}
			if !legalFlag {
				err = fmt.Errorf("resource server validate fail,already have an active&&allocated %s resource", resourceServer.Type)
			}
		}
	}
	return
}
