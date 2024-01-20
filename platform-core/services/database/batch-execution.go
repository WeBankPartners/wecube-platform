package database

import (
	"fmt"
	"time"

	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
)

func CreateOrUpdateBatchExecTemplate(c *gin.Context, reqParam *models.BatchExecTemplateInfo) (err error) {
	var actions []*db.ExecAction
	now := time.Now()
	if reqParam.Id == "" {
		// create
		reqParam.Id = guid.CreateGuid()
		templateData := &models.BatchExecutionTemplate{
			Id:            reqParam.Id,
			Name:          reqParam.Name,
			Status:        models.BatchExecTemplateStatusAvailable,
			OperateObject: reqParam.OperateObject,
			PluginService: reqParam.PluginService,
			ConfigData:    reqParam.ConfigData,
			CreatedBy:     middleware.GetRequestUser(c),
			UpdatedBy:     "",
			CreatedTime:   &now,
			UpdatedTime:   &now,
		}
		action, tmpErr := db.GetInsertTableExecAction(models.TableNameBatchExecTemplate, *templateData, nil)
		if tmpErr != nil {
			err = fmt.Errorf("get insert sql failed: %s", tmpErr.Error())
			return
		}
		actions = append(actions, action)
	} else {
		// update
		updateColumnStr := "`name`=?,`operate_object`=?,`plugin_service`=?,`config_data`=?,`updated_by`=?,`updated_time`=?"
		action := &db.ExecAction{
			Sql:   db.CombineDBSql("UPDATE ", models.TableNameBatchExecTemplate, " SET ", updateColumnStr, " WHERE id=?"),
			Param: []interface{}{reqParam.Name, reqParam.OperateObject, reqParam.PluginService, reqParam.ConfigData, middleware.GetRequestUser(c), now, reqParam.Id},
		}
		actions = append(actions, action)
	}
	batchExecTemplateId := reqParam.Id
	// update batchExecTemplateRole
	// firstly delete original batchExecTemplateRole and then create new batchExecTemplateRole
	action := &db.ExecAction{
		Sql:   db.CombineDBSql("DELETE FROM ", models.TableNameBatchExecTemplateRole, " WHERE batch_execution_template_id=?"),
		Param: []interface{}{batchExecTemplateId},
	}
	actions = append(actions, action)

	var templateRoleDataList []*models.BatchExecutionTemplateRole
	mgmtRoleNameMap := make(map[string]struct{})
	for _, roleName := range reqParam.PermissionToRole.MGMT {
		if _, isExisted := mgmtRoleNameMap[roleName]; !isExisted {
			mgmtRoleNameMap[roleName] = struct{}{}
			templateRoleDataList = append(templateRoleDataList, &models.BatchExecutionTemplateRole{
				Id:                       guid.CreateGuid(),
				BatchExecutionTemplateId: batchExecTemplateId,
				Permission:               models.PermissionTypeMGMT,
				RoleName:                 roleName,
			})
		}
	}
	useRoleNameMap := make(map[string]struct{})
	for _, roleName := range reqParam.PermissionToRole.USE {
		if _, isExisted := useRoleNameMap[roleName]; !isExisted {
			useRoleNameMap[roleName] = struct{}{}
			templateRoleDataList = append(templateRoleDataList, &models.BatchExecutionTemplateRole{
				Id:                       guid.CreateGuid(),
				BatchExecutionTemplateId: batchExecTemplateId,
				Permission:               models.PermissionTypeUSE,
				RoleName:                 roleName,
			})
		}
	}
	for i := range templateRoleDataList {
		action, tmpErr := db.GetInsertTableExecAction(models.TableNameBatchExecTemplateRole, *templateRoleDataList[i], nil)
		if tmpErr != nil {
			err = fmt.Errorf("get insert sql failed: %s", tmpErr.Error())
			return
		}
		actions = append(actions, action)
	}

	err = db.Transaction(actions, c)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func CollectBatchExecTemplate(c *gin.Context, reqParam *models.BatchExecutionTemplateCollect) (err error) {
	reqParam.UserId = middleware.GetRequestUser(c)
	// validate favoritesId
	templateData := &models.BatchExecutionTemplate{}
	var exists bool
	exists, err = db.MysqlEngine.Context(c).Table(new(models.BatchExecutionTemplate)).
		Where("id = ?", reqParam.BatchExecutionTemplateId).
		Get(templateData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if !exists {
		err = fmt.Errorf("batchExecTemplateId: %s is invalid", reqParam.BatchExecutionTemplateId)
		return
	}

	templateCollectData := &models.BatchExecutionTemplateCollect{}
	exists, err = db.MysqlEngine.Context(c).Table(new(models.BatchExecutionTemplateCollect)).
		Where("batch_execution_template_id = ? AND user_id = ?", reqParam.BatchExecutionTemplateId, reqParam.UserId).
		Get(templateCollectData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if exists {
		return
	}

	now := time.Now()
	reqParam.Id = guid.CreateGuid()
	reqParam.CreatedTime = &now
	var actions []*db.ExecAction
	action, tmpErr := db.GetInsertTableExecAction(models.TableNameBatchExecTemplateCollect, *reqParam, nil)
	if tmpErr != nil {
		err = fmt.Errorf("get insert sql failed: %s", tmpErr.Error())
		return
	}
	actions = append(actions, action)

	err = db.Transaction(actions, c)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func RetrieveTemplate(c *gin.Context, reqParam *models.QueryRequestParam) (result *models.BatchExecTemplatePageData, err error) {
	result = &models.BatchExecTemplatePageData{PageInfo: models.PageInfo{}, Contents: []*models.BatchExecTemplateInfo{}}
	userRoles := middleware.GetRequestRoles(c)
	userId := middleware.GetRequestUser(c)

	var queryFilters []*models.QueryRequestFilterObj
	var permissionTypes []string
	isShowCollectTemplate := false
	for _, filter := range reqParam.Filters {
		if filter.Name == "permissionType" {
			permissionTypes = append(permissionTypes, filter.Value.(string))
			continue
		}
		if filter.Name == "isShowCollectTemplate" {
			isShowCollectTemplate = filter.Value.(bool)
			continue
		}
		queryFilters = append(queryFilters, &models.QueryRequestFilterObj{
			Name:     filter.Name,
			Operator: filter.Operator,
			Value:    filter.Value,
		})
	}

	// query collect templateId
	var collectTemplateIds []string
	if isShowCollectTemplate {
		err = db.MysqlEngine.Context(c).Table(models.TableNameBatchExecTemplateCollect).
			Where("user_id = ?", userId).
			Cols("batch_execution_template_id").
			Find(&collectTemplateIds)
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}
	}

	// filter templateId by roleName (and permissionType)
	var roleFilterTemplateIds []string
	session := db.MysqlEngine.Context(c).Table(models.TableNameBatchExecTemplateRole).
		In("role_name", userRoles).
		Distinct("batch_execution_template_id")
	if len(permissionTypes) > 0 {
		session = session.In("permission", permissionTypes)
	}
	if len(collectTemplateIds) > 0 {
		session = session.In("batch_execution_template_id", collectTemplateIds)
	}
	err = session.Find(&roleFilterTemplateIds)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	reqParam.Filters = append(reqParam.Filters, &models.QueryRequestFilterObj{
		Name:     "id",
		Operator: "in",
		Value:    roleFilterTemplateIds,
	})

	// filter template info
	var templateData []*models.BatchExecTemplateInfo
	filterSql, _, queryParam := transFiltersToSQL(reqParam, &models.TransFiltersParam{IsStruct: true, StructObj: models.BatchExecutionTemplate{}, PrimaryKey: "id"})
	baseSql := db.CombineDBSql("SELECT * FROM ", models.TableNameBatchExecTemplate, " WHERE 1=1 ", filterSql)
	baseCountSql := db.CombineDBSql("SELECT COUNT(*) FROM ", models.TableNameBatchExecTemplate, " WHERE 1=1 ", filterSql)
	if reqParam.Paging {
		var count int64
		count, err = db.MysqlEngine.Context(c).Table(models.TableNameBatchExecTemplate).SQL(baseCountSql, queryParam...).Count()
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}
		result.PageInfo = models.PageInfo{StartIndex: reqParam.Pageable.StartIndex, PageSize: reqParam.Pageable.PageSize, TotalRows: int(count)}
		pageSql, pageParam := transPageInfoToSQL(*reqParam.Pageable)
		baseSql += pageSql
		queryParam = append(queryParam, pageParam...)
	}
	err = db.MysqlEngine.Context(c).Table(models.TableNameBatchExecTemplate).SQL(baseSql, queryParam...).Find(&templateData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	// filter permission roles
	var templateIds []string
	for _, template := range templateData {
		templateIds = append(templateIds, template.Id)
	}

	var templateRoleData []*models.BatchExecutionTemplateRole
	err = db.MysqlEngine.Context(c).Table(models.TableNameBatchExecTemplateRole).
		In("batch_execution_template_id", templateIds).
		Find(&templateRoleData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	templateIdMapRoleInfo := make(map[string]*models.PermissionToRole)
	for _, roleData := range templateRoleData {
		if _, isExisted := templateIdMapRoleInfo[roleData.BatchExecutionTemplateId]; !isExisted {
			templateIdMapRoleInfo[roleData.BatchExecutionTemplateId] = &models.PermissionToRole{}
		}
		if roleData.Permission == models.PermissionTypeMGMT {
			templateIdMapRoleInfo[roleData.BatchExecutionTemplateId].MGMT = append(
				templateIdMapRoleInfo[roleData.BatchExecutionTemplateId].MGMT, roleData.RoleName)
		} else if roleData.Permission == models.PermissionTypeUSE {
			templateIdMapRoleInfo[roleData.BatchExecutionTemplateId].USE = append(
				templateIdMapRoleInfo[roleData.BatchExecutionTemplateId].USE, roleData.RoleName)
		}
	}

	for _, template := range templateData {
		template.PermissionToRole = templateIdMapRoleInfo[template.Id]
	}

	result.Contents = templateData
	return
}

func GetTemplate(c *gin.Context, templateId string) (result *models.BatchExecTemplateInfo, err error) {
	result = &models.BatchExecTemplateInfo{}

	var templateData []*models.BatchExecTemplateInfo
	err = db.MysqlEngine.Context(c).Table(models.TableNameBatchExecTemplate).
		Where("id = ?", templateId).
		Find(&templateData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(templateData) == 0 {
		err = fmt.Errorf("templateId: %s is invalid", templateId)
		return
	}

	result = templateData[0]
	// filter permission roles
	var templateRoleData []*models.BatchExecutionTemplateRole
	err = db.MysqlEngine.Context(c).Table(models.TableNameBatchExecTemplateRole).
		In("batch_execution_template_id", result.Id).
		Find(&templateRoleData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	for _, roleData := range templateRoleData {
		if roleData.Permission == models.PermissionTypeMGMT {
			result.PermissionToRole.MGMT = append(result.PermissionToRole.MGMT, roleData.RoleName)
		} else if roleData.Permission == models.PermissionTypeUSE {
			result.PermissionToRole.USE = append(result.PermissionToRole.USE, roleData.RoleName)
		}
	}
	return
}
