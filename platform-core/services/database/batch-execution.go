package database

import (
	"encoding/json"
	"fmt"
	"time"

	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
)

func CreateOrUpdateBatchExecTemplate(c *gin.Context, reqParam *models.BatchExecutionTemplate) (result *models.BatchExecutionTemplate, err error) {
	var actions []*db.ExecAction
	now := time.Now()
	configDataStr := ""
	if reqParam.ConfigData != nil {
		configDataByte, tmpErr := json.Marshal(*reqParam.ConfigData)
		if tmpErr != nil {
			err = fmt.Errorf("marshal reqParam.ConfigData error: %s", tmpErr.Error())
			return
		}
		configDataStr = string(configDataByte)
	}
	if reqParam.Id == "" {
		// create
		reqParam.Id = guid.CreateGuid()
		templateData := &models.BatchExecutionTemplate{
			Id:            reqParam.Id,
			Name:          reqParam.Name,
			Status:        models.BatchExecTemplateStatusAvailable,
			OperateObject: reqParam.OperateObject,
			PluginService: reqParam.PluginService,
			ConfigDataStr: configDataStr,
			SourceData:    reqParam.SourceData,
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
		// check whether templateId is valid
		templateData := &models.BatchExecutionTemplate{}
		var exists bool
		exists, err = db.MysqlEngine.Context(c).Table(new(models.BatchExecutionTemplate)).
			Where("id = ?", reqParam.Id).
			Get(templateData)
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}
		if !exists {
			err = fmt.Errorf("batchExecTemplateId: %s is invalid", reqParam.Id)
			return
		}

		// update
		updateColumnStr := "`name`=?,`operate_object`=?,`plugin_service`=?,`config_data`=?,`source_data`=?,`updated_by`=?,`updated_time`=?"
		action := &db.ExecAction{
			Sql:   db.CombineDBSql("UPDATE ", models.TableNameBatchExecTemplate, " SET ", updateColumnStr, " WHERE id=?"),
			Param: []interface{}{reqParam.Name, reqParam.OperateObject, reqParam.PluginService, configDataStr, reqParam.SourceData, middleware.GetRequestUser(c), now, reqParam.Id},
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
		return
	}

	// query batchExecTemplate info
	result, err = GetTemplate(c, reqParam.Id)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func CollectBatchExecTemplate(c *gin.Context, reqParam *models.BatchExecutionTemplateCollect) (err error) {
	reqParam.UserId = middleware.GetRequestUser(c)
	// validate batchExecTemplateId
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
		return
	}
	return
}

func UncollectBatchExecTemplate(c *gin.Context, reqParam *models.BatchExecutionTemplateCollect) (err error) {
	reqParam.UserId = middleware.GetRequestUser(c)
	// validate batchExecTemplateId
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

	var actions []*db.ExecAction
	action := &db.ExecAction{
		Sql:   db.CombineDBSql("DELETE FROM ", models.TableNameBatchExecTemplateCollect, " WHERE batch_execution_template_id = ? AND user_id = ?"),
		Param: []interface{}{reqParam.BatchExecutionTemplateId, reqParam.UserId},
	}
	actions = append(actions, action)

	err = db.Transaction(actions, c)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	return
}

func CheckCollectBatchExecTemplate(c *gin.Context, reqParam *models.BatchExecutionTemplateCollect) (result *models.CheckBatchExecTemplateResp, err error) {
	result = &models.CheckBatchExecTemplateResp{}
	reqParam.UserId = middleware.GetRequestUser(c)
	// validate batchExecTemplateId
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
		result.IsCollectTemplate = true
	} else {
		result.IsCollectTemplate = false
	}
	return
}

func RetrieveTemplate(c *gin.Context, reqParam *models.QueryRequestParam) (result *models.BatchExecTemplatePageData, err error) {
	result = &models.BatchExecTemplatePageData{PageInfo: models.PageInfo{}, Contents: []*models.BatchExecutionTemplate{}}
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
		if len(collectTemplateIds) == 0 {
			return
		}
	}

	// query templateId by roleName (and permissionType)
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
	if len(roleFilterTemplateIds) == 0 {
		return
	}

	reqParam.Filters = append(reqParam.Filters, &models.QueryRequestFilterObj{
		Name:     "id",
		Operator: "in",
		Value:    roleFilterTemplateIds,
	})

	// query template info
	var templateData []*models.BatchExecutionTemplate
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
	if len(templateData) == 0 {
		return
	}

	// query permission roles
	var templateIds []string
	for _, template := range templateData {
		templateIds = append(templateIds, template.Id)

		if template.ConfigDataStr != "" {
			configData := models.BatchExecRun{}
			err = json.Unmarshal([]byte(template.ConfigDataStr), &configData)
			if err != nil {
				err = fmt.Errorf("unmarshal templateId: %s configData error: %s", template.Id, err.Error())
				return
			}
			template.ConfigData = &configData
		}
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

	// check is collect by userId
	err = CheckIsCollected(c, templateData, userId)
	if err != nil {
		return
	}

	result.Contents = templateData
	return
}

func CheckIsCollected(c *gin.Context, templateData []*models.BatchExecutionTemplate, userId string) (err error) {
	if len(templateData) == 0 {
		return
	}

	templateIdMap := make(map[string]bool)
	for _, template := range templateData {
		templateIdMap[template.Id] = false
		template.IsCollected = false
	}

	templateIdList := make([]string, 0, len(templateIdMap))
	for k := range templateIdMap {
		templateIdList = append(templateIdList, k)
	}

	var collectData []*models.BatchExecutionTemplateCollect
	err = db.MysqlEngine.Context(c).Table(models.TableNameBatchExecTemplateCollect).
		Where("user_id = ?", userId).
		In("batch_execution_template_id", templateIdList).
		Find(&collectData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	if len(collectData) == 0 {
		return
	}

	for _, collect := range collectData {
		if _, isExisted := templateIdMap[collect.BatchExecutionTemplateId]; isExisted {
			templateIdMap[collect.BatchExecutionTemplateId] = true
		}
	}

	for _, template := range templateData {
		if isCollected, isExisted := templateIdMap[template.Id]; isExisted {
			template.IsCollected = isCollected
		}
	}
	return
}

func GetTemplate(c *gin.Context, templateId string) (result *models.BatchExecutionTemplate, err error) {
	result = &models.BatchExecutionTemplate{}

	var templateData []*models.BatchExecutionTemplate
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
	if result.ConfigDataStr != "" {
		configData := models.BatchExecRun{}
		err = json.Unmarshal([]byte(result.ConfigDataStr), &configData)
		if err != nil {
			err = fmt.Errorf("unmarshal templateId: %s configData error: %s", result.Id, err.Error())
			return
		}
		result.ConfigData = &configData
	}
	// filter permission roles
	var templateRoleData []*models.BatchExecutionTemplateRole
	err = db.MysqlEngine.Context(c).Table(models.TableNameBatchExecTemplateRole).
		In("batch_execution_template_id", result.Id).
		Find(&templateRoleData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	result.PermissionToRole = &models.PermissionToRole{}
	for _, roleData := range templateRoleData {
		if roleData.Permission == models.PermissionTypeMGMT {
			result.PermissionToRole.MGMT = append(result.PermissionToRole.MGMT, roleData.RoleName)
		} else if roleData.Permission == models.PermissionTypeUSE {
			result.PermissionToRole.USE = append(result.PermissionToRole.USE, roleData.RoleName)
		}
	}

	// check is collect by userId
	err = CheckIsCollected(c, []*models.BatchExecutionTemplate{result}, middleware.GetRequestUser(c))
	if err != nil {
		return
	}
	return
}

func DeleteTemplate(c *gin.Context, templateId string) (err error) {
	var actions []*db.ExecAction
	var templateData []*models.BatchExecutionTemplate
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
	templateInfo := templateData[0]

	// delete batchExecTemplate
	action := &db.ExecAction{
		Sql:   db.CombineDBSql("DELETE FROM ", models.TableNameBatchExecTemplate, " WHERE id=?"),
		Param: []interface{}{templateInfo.Id},
	}
	actions = append(actions, action)

	// delete batchExecTemplateRole
	action = &db.ExecAction{
		Sql:   db.CombineDBSql("DELETE FROM ", models.TableNameBatchExecTemplateRole, " WHERE batch_execution_template_id=?"),
		Param: []interface{}{templateInfo.Id},
	}
	actions = append(actions, action)

	// delete batchExecTemplateCollect
	action = &db.ExecAction{
		Sql:   db.CombineDBSql("DELETE FROM ", models.TableNameBatchExecTemplateCollect, " WHERE batch_execution_template_id = ?"),
		Param: []interface{}{templateInfo.Id},
	}
	actions = append(actions, action)

	err = db.Transaction(actions, c)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	return
}

func UpdateTemplatePermission(c *gin.Context, reqParam *models.BatchExecutionTemplate) (result *models.BatchExecutionTemplate, err error) {
	var actions []*db.ExecAction
	now := time.Now()
	// check whether templateId is valid
	templateData := &models.BatchExecutionTemplate{}
	var exists bool
	exists, err = db.MysqlEngine.Context(c).Table(new(models.BatchExecutionTemplate)).
		Where("id = ?", reqParam.Id).
		Get(templateData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if !exists {
		err = fmt.Errorf("batchExecTemplateId: %s is invalid", reqParam.Id)
		return
	}

	// update template updatedTime
	updateColumnStr := "`updated_by`=?,`updated_time`=?"
	action := &db.ExecAction{
		Sql:   db.CombineDBSql("UPDATE ", models.TableNameBatchExecTemplate, " SET ", updateColumnStr, " WHERE id=?"),
		Param: []interface{}{middleware.GetRequestUser(c), now, reqParam.Id},
	}
	actions = append(actions, action)

	batchExecTemplateId := reqParam.Id
	// update batchExecTemplateRole
	// firstly delete original batchExecTemplateRole and then create new batchExecTemplateRole
	action = &db.ExecAction{
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
		return
	}

	// query batchExecTemplate info
	result, err = GetTemplate(c, reqParam.Id)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func RetrieveBatchExec(c *gin.Context, reqParam *models.QueryRequestParam) (result *models.BatchExecListPageData, err error) {
	result = &models.BatchExecListPageData{PageInfo: models.PageInfo{}, Contents: []*models.BatchExecution{}}

	var batchExecData []*models.BatchExecution
	filterSql, _, queryParam := transFiltersToSQL(reqParam, &models.TransFiltersParam{IsStruct: true, StructObj: models.BatchExecution{}, PrimaryKey: "id"})
	baseSql := db.CombineDBSql("SELECT * FROM ", models.TableNameBatchExec, " WHERE 1=1 ", filterSql)
	baseCountSql := db.CombineDBSql("SELECT COUNT(*) FROM ", models.TableNameBatchExec, " WHERE 1=1 ", filterSql)
	if reqParam.Paging {
		var count int64
		count, err = db.MysqlEngine.Context(c).Table(models.TableNameBatchExec).SQL(baseCountSql, queryParam...).Count()
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}
		result.PageInfo = models.PageInfo{StartIndex: reqParam.Pageable.StartIndex, PageSize: reqParam.Pageable.PageSize, TotalRows: int(count)}
		pageSql, pageParam := transPageInfoToSQL(*reqParam.Pageable)
		baseSql += pageSql
		queryParam = append(queryParam, pageParam...)
	}
	err = db.MysqlEngine.Context(c).Table(models.TableNameBatchExec).SQL(baseSql, queryParam...).Find(&batchExecData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	for _, execData := range batchExecData {
		if execData.ConfigDataStr != "" {
			configData := models.BatchExecRun{}
			err = json.Unmarshal([]byte(execData.ConfigDataStr), &configData)
			if err != nil {
				err = fmt.Errorf("unmarshal batchExec: %s configData error: %s", execData.Id, err.Error())
				return
			}
			execData.ConfigData = &configData
		}
	}

	result.Contents = batchExecData
	return
}

func GetBatchExec(c *gin.Context, batchExecId string) (result *models.BatchExecution, err error) {
	result = &models.BatchExecution{}

	// get batchExecution
	var batchExecData []*models.BatchExecution
	err = db.MysqlEngine.Context(c).Table(models.TableNameBatchExec).
		Where("id = ?", batchExecId).
		Find(&batchExecData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	if len(batchExecData) == 0 {
		err = fmt.Errorf("batchExecId: %s is invalid", batchExecId)
		return
	}
	result = batchExecData[0]
	if result.ConfigDataStr != "" {
		configData := models.BatchExecRun{}
		err = json.Unmarshal([]byte(result.ConfigDataStr), &configData)
		if err != nil {
			err = fmt.Errorf("unmarshal batchExec: %s configData error: %s", result.Id, err.Error())
			return
		}
		result.ConfigData = &configData
	}

	// get batchExecutionJobs
	batchExecJobsQueryParam := &models.QueryRequestParam{
		Filters: []*models.QueryRequestFilterObj{
			&models.QueryRequestFilterObj{
				Name:     "batch_execution_id",
				Operator: "eq",
				Value:    result.Id,
			},
		},
		Sorting: []*models.QueryRequestSorting{
			&models.QueryRequestSorting{
				Field: "execute_time",
				Asc:   false,
			},
			&models.QueryRequestSorting{
				Field: "id",
				Asc:   true,
			},
		},
	}
	batchExecutionJobsPageData, err := RetrieveBatchExecJobs(c, batchExecJobsQueryParam)
	if err != nil {
		return
	}
	result.BatchExecutionJobs = batchExecutionJobsPageData.Contents
	return
}

func RetrieveBatchExecJobs(c *gin.Context, reqParam *models.QueryRequestParam) (result *models.BatchExecJobsPageData, err error) {
	result = &models.BatchExecJobsPageData{PageInfo: models.PageInfo{}, Contents: []*models.BatchExecutionJobs{}}

	var batchExecJobsData []*models.BatchExecutionJobs
	filterSql, _, queryParam := transFiltersToSQL(reqParam, &models.TransFiltersParam{IsStruct: true, StructObj: models.BatchExecutionJobs{}, PrimaryKey: "id"})
	baseSql := db.CombineDBSql("SELECT * FROM ", models.TableNameBatchExecJobs, " WHERE 1=1 ", filterSql)
	baseCountSql := db.CombineDBSql("SELECT COUNT(*) FROM ", models.TableNameBatchExecJobs, " WHERE 1=1 ", filterSql)
	if reqParam.Paging {
		var count int64
		count, err = db.MysqlEngine.Context(c).Table(models.TableNameBatchExecJobs).SQL(baseCountSql, queryParam...).Count()
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}
		result.PageInfo = models.PageInfo{StartIndex: reqParam.Pageable.StartIndex, PageSize: reqParam.Pageable.PageSize, TotalRows: int(count)}
		pageSql, pageParam := transPageInfoToSQL(*reqParam.Pageable)
		baseSql += pageSql
		queryParam = append(queryParam, pageParam...)
	}
	err = db.MysqlEngine.Context(c).Table(models.TableNameBatchExecJobs).SQL(baseSql, queryParam...).Find(&batchExecJobsData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	result.Contents = batchExecJobsData
	return
}
