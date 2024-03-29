package database

import (
	"encoding/json"
	"fmt"
	"strings"
	"time"

	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"github.com/gin-gonic/gin"
)

func CreateOrUpdateBatchExecTemplate(c *gin.Context, reqParam *models.BatchExecutionTemplate) (result *models.BatchExecutionTemplate, err error) {
	var actions []*db.ExecAction
	now := time.Now()
	configDataStr := ""
	if reqParam.ConfigData != nil {
		reqParam.ConfigData.IsDangerousBlock = reqParam.IsDangerousBlock
		configDataByte, tmpErr := json.Marshal(*reqParam.ConfigData)
		if tmpErr != nil {
			err = fmt.Errorf("marshal reqParam.ConfigData error: %s", tmpErr.Error())
			return
		}
		configDataStr = string(configDataByte)
	}

	if reqParam.PublishStatus != models.BatchExecTmplPublishStatusPublished &&
		reqParam.PublishStatus != models.BatchExecTmplPublishStatusDraft {
		err = exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err, publishStatus should be [%s, %s]",
			models.BatchExecTmplPublishStatusDraft, models.BatchExecTmplPublishStatusPublished))
		return
	}

	if reqParam.Id == "" {
		// create
		reqParam.Id = guid.CreateGuid()
		templateData := &models.BatchExecutionTemplate{
			Id:               reqParam.Id,
			Name:             reqParam.Name,
			Status:           models.BatchExecTemplateStatusAvailable,
			PublishStatus:    reqParam.PublishStatus,
			OperateObject:    reqParam.OperateObject,
			PluginService:    reqParam.PluginService,
			IsDangerousBlock: reqParam.IsDangerousBlock,
			ConfigDataStr:    configDataStr,
			SourceData:       reqParam.SourceData,
			CreatedBy:        middleware.GetRequestUser(c),
			UpdatedBy:        "",
			CreatedTime:      &now,
			UpdatedTime:      &now,
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
		updateColumnStr := "`name`=?,`publish_status`=?,`operate_object`=?,`plugin_service`=?,`is_dangerous_block`=?,`config_data`=?,`source_data`=?,`updated_by`=?,`updated_time`=?"
		action := &db.ExecAction{
			Sql: db.CombineDBSql("UPDATE ", models.TableNameBatchExecTemplate, " SET ", updateColumnStr, " WHERE id=?"),
			Param: []interface{}{reqParam.Name, reqParam.PublishStatus, reqParam.OperateObject, reqParam.PluginService, reqParam.IsDangerousBlock,
				configDataStr, reqParam.SourceData, middleware.GetRequestUser(c), now, reqParam.Id},
		}

		originalUpdatedTime := reqParam.UpdatedTime
		if originalUpdatedTime != nil {
			// 需要校验修改前的 updatedTime 是否与数据库中的一致
			action.Sql = db.CombineDBSql(action.Sql, " AND updated_time=?")
			action.Param = append(action.Param, originalUpdatedTime)
			action.CheckAffectRow = true
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
		if strings.Contains(err.Error(), "Duplicate entry") {
			if strings.Contains(err.Error(), "for key 'name_unique'") {
				// err = fmt.Errorf("template name: %s has been existed", reqParam.Name)
				err = exterror.New().BatchExecTmplDuplicateNameError
				return
			}
		}
		if strings.Contains(err.Error(), "row affect 0 with exec sql") {
			err = exterror.New().BatchExecTmplHasBeenModifiedError
			return
		}
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

	// var queryFilters []*models.QueryRequestFilterObj
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
		// queryFilters = append(queryFilters, &models.QueryRequestFilterObj{
		// 	Name:     filter.Name,
		// 	Operator: filter.Operator,
		// 	Value:    filter.Value,
		// })
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
	// if len(permissionTypes) > 0 || len(collectTemplateIds) > 0 {
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
	// }

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
		template.CreatedTimeStr = template.CreatedTime.Format(models.DateTimeFormat)
		template.UpdatedTimeStr = template.UpdatedTime.Format(models.DateTimeFormat)
	}

	var templateRoleData []*models.BatchExecutionTemplateRole
	err = db.MysqlEngine.Context(c).Table(models.TableNameBatchExecTemplateRole).
		In("batch_execution_template_id", templateIds).
		Find(&templateRoleData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	userRolesMap := make(map[string]struct{})
	for _, role := range userRoles {
		userRolesMap[role] = struct{}{}
	}
	templateIdMapRoleInfo := make(map[string]*models.BatchExecPermissionToRole)
	for _, roleData := range templateRoleData {
		if _, isExisted := templateIdMapRoleInfo[roleData.BatchExecutionTemplateId]; !isExisted {
			templateIdMapRoleInfo[roleData.BatchExecutionTemplateId] = &models.BatchExecPermissionToRole{}
		}
		if roleData.Permission == models.PermissionTypeMGMT {
			if (len(permissionTypes) == 0) || (len(permissionTypes) > 0 && permissionTypes[0] != models.PermissionTypeMGMT) {
				templateIdMapRoleInfo[roleData.BatchExecutionTemplateId].MGMT = append(
					templateIdMapRoleInfo[roleData.BatchExecutionTemplateId].MGMT, roleData.RoleName)
			} else {
				if _, isExisted := userRolesMap[roleData.RoleName]; isExisted {
					templateIdMapRoleInfo[roleData.BatchExecutionTemplateId].MGMT = append(
						templateIdMapRoleInfo[roleData.BatchExecutionTemplateId].MGMT, roleData.RoleName)
				}
			}
		} else if roleData.Permission == models.PermissionTypeUSE {
			if (len(permissionTypes) == 0) || (len(permissionTypes) > 0 && permissionTypes[0] != models.PermissionTypeUSE) {
				templateIdMapRoleInfo[roleData.BatchExecutionTemplateId].USE = append(
					templateIdMapRoleInfo[roleData.BatchExecutionTemplateId].USE, roleData.RoleName)
			} else {
				if _, isExisted := userRolesMap[roleData.RoleName]; isExisted {
					templateIdMapRoleInfo[roleData.BatchExecutionTemplateId].USE = append(
						templateIdMapRoleInfo[roleData.BatchExecutionTemplateId].USE, roleData.RoleName)
				}
			}
		}
	}

	for _, template := range templateData {
		template.PermissionToRole = templateIdMapRoleInfo[template.Id]
		MGMTDisplayName := append([]string{}, template.PermissionToRole.MGMT...)
		template.PermissionToRole.MGMTDisplayName = MGMTDisplayName

		USEDisplayName := append([]string{}, template.PermissionToRole.USE...)
		template.PermissionToRole.USEDisplayName = USEDisplayName
	}

	// check is collect by userId
	err = CheckIsCollected(c, templateData, userId)
	if err != nil {
		return
	}
	/*
		permissionTypesToCheck := permissionTypes
		if len(permissionTypesToCheck) == 0 {
			permissionTypesToCheck = []string{models.PermissionTypeMGMT, models.PermissionTypeUSE}
		}
	*/
	permissionTypesToCheck := []string{models.PermissionTypeUSE}
	UpdateTemplateStatus(templateData, userRoles, permissionTypesToCheck)
	result.Contents = templateData

	err = UpdateTemplateRolesDisplayName(c, templateData)
	if err != nil {
		err = exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("UpdateTemplateRolesDisplayName failed: %s", err.Error()))
		return
	}
	return
}

func UpdateTemplateRolesDisplayName(c *gin.Context, templateDataList []*models.BatchExecutionTemplate) (err error) {
	userToken := c.GetHeader(models.AuthorizationHeader)
	language := c.GetHeader(middleware.AcceptLanguageHeader)
	respData, err := remote.RetrieveAllLocalRoles("Y", userToken, language, false)
	if err != nil {
		err = fmt.Errorf("retrieve all local roles failed: %s", err.Error())
		return
	}

	if len(respData.Data) > 0 {
		roleNameMapDisplayName := make(map[string]string)
		for _, roleDto := range respData.Data {
			roleNameMapDisplayName[roleDto.Name] = roleDto.DisplayName
		}
		for _, templateInfo := range templateDataList {
			// update MGMTDisplayName
			for i, val := range templateInfo.PermissionToRole.MGMTDisplayName {
				if displayName, isExisted := roleNameMapDisplayName[val]; isExisted {
					templateInfo.PermissionToRole.MGMTDisplayName[i] = displayName
				}
			}

			// update USEDisplayName
			for i, val := range templateInfo.PermissionToRole.USEDisplayName {
				if displayName, isExisted := roleNameMapDisplayName[val]; isExisted {
					templateInfo.PermissionToRole.USEDisplayName[i] = displayName
				}
			}
		}
	} else {
		log.Logger.Error("retrieve all local roles empty")
	}
	return
}

func UpdateTemplateStatus(templateData []*models.BatchExecutionTemplate, userRoles []string, permissionTypesToCheck []string) {
	if len(templateData) == 0 {
		return
	}
	if len(userRoles) == 0 {
		return
	}

	userRolesMap := make(map[string]struct{})
	for _, role := range userRoles {
		userRolesMap[role] = struct{}{}
	}

	permissionTypeToCheckMap := make(map[string]struct{})
	for _, permissionType := range permissionTypesToCheck {
		permissionTypeToCheckMap[permissionType] = struct{}{}
	}

	for _, template := range templateData {
		isMGMTAuthorized := false
		isUSEAuthorized := false
		for _, role := range template.PermissionToRole.MGMT {
			if _, isExisted := userRolesMap[role]; isExisted {
				isMGMTAuthorized = true
				break
			}
		}

		for _, role := range template.PermissionToRole.USE {
			if _, isExisted := userRolesMap[role]; isExisted {
				isUSEAuthorized = true
				break
			}
		}

		status := models.BatchExecTemplateStatusUnauthorized
		for permiType := range permissionTypeToCheckMap {
			if permiType == models.PermissionTypeMGMT && isMGMTAuthorized {
				status = models.BatchExecTemplateStatusAvailable
			} else if permiType == models.PermissionTypeUSE && isUSEAuthorized {
				status = models.BatchExecTemplateStatusAvailable
			}
		}
		template.Status = status
	}
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
	result.CreatedTimeStr = result.CreatedTime.Format(models.DateTimeFormat)
	result.UpdatedTimeStr = result.UpdatedTime.Format(models.DateTimeFormat)
	// filter permission roles
	var templateRoleData []*models.BatchExecutionTemplateRole
	err = db.MysqlEngine.Context(c).Table(models.TableNameBatchExecTemplateRole).
		In("batch_execution_template_id", result.Id).
		Find(&templateRoleData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	result.PermissionToRole = &models.BatchExecPermissionToRole{}
	for _, roleData := range templateRoleData {
		if roleData.Permission == models.PermissionTypeMGMT {
			result.PermissionToRole.MGMT = append(result.PermissionToRole.MGMT, roleData.RoleName)
		} else if roleData.Permission == models.PermissionTypeUSE {
			result.PermissionToRole.USE = append(result.PermissionToRole.USE, roleData.RoleName)
		}
	}
	MGMTDisplayName := append([]string{}, result.PermissionToRole.MGMT...)
	result.PermissionToRole.MGMTDisplayName = MGMTDisplayName

	USEDisplayName := append([]string{}, result.PermissionToRole.USE...)
	result.PermissionToRole.USEDisplayName = USEDisplayName

	// check is collect by userId
	err = CheckIsCollected(c, []*models.BatchExecutionTemplate{result}, middleware.GetRequestUser(c))
	if err != nil {
		return
	}

	// permissionTypesToCheck := []string{models.PermissionTypeMGMT, models.PermissionTypeUSE}
	// UpdateTemplateStatus([]*models.BatchExecutionTemplate{result}, middleware.GetRequestRoles(c), permissionTypesToCheck)
	err = UpdateTemplateRolesDisplayName(c, []*models.BatchExecutionTemplate{result})
	if err != nil {
		err = exterror.Catch(exterror.New().ServerHandleError, fmt.Errorf("UpdateTemplateRolesDisplayName failed: %s", err.Error()))
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
		execData.CreatedTimeStr = execData.CreatedTime.Format(models.DateTimeFormat)
		execData.UpdatedTimeStr = execData.UpdatedTime.Format(models.DateTimeFormat)
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
	result.CreatedTimeStr = result.CreatedTime.Format(models.DateTimeFormat)
	result.UpdatedTimeStr = result.UpdatedTime.Format(models.DateTimeFormat)

	// get batchExecutionJobs
	batchExecJobsQueryParam := &models.QueryRequestParam{
		Filters: []*models.QueryRequestFilterObj{
			{
				Name:     "batchExecutionId",
				Operator: "eq",
				Value:    result.Id,
			},
		},
		Sorting: []*models.QueryRequestSorting{
			{
				Field: "executeTimeT",
				Asc:   false,
			},
			{
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

	for _, jobData := range batchExecJobsData {
		jobData.ExecuteTimeStr = jobData.ExecuteTime.Format(models.DateTimeFormat)
		jobData.CompleteTimeStr = jobData.CompleteTime.Format(models.DateTimeFormat)
	}

	result.Contents = batchExecJobsData
	return
}

func InsertBatchExec(c *gin.Context, reqParam *models.BatchExecRun) (batchExecId string, err error) {
	var actions []*db.ExecAction
	now := time.Now()

	configDataStr := ""
	configDataByte, tmpErr := json.Marshal(*reqParam)
	if tmpErr != nil {
		err = fmt.Errorf("marshal reqParam error: %s", tmpErr.Error())
		return
	}
	configDataStr = string(configDataByte)
	// create
	batchExecId = guid.CreateGuid()
	batchExecData := &models.BatchExecution{
		Id:                         batchExecId,
		Name:                       reqParam.Name,
		BatchExecutionTemplateId:   reqParam.BatchExecutionTemplateId,
		BatchExecutionTemplateName: reqParam.BatchExecutionTemplateName,
		ErrorCode:                  models.BatchExecErrorCodePending,
		ConfigDataStr:              configDataStr,
		SourceData:                 reqParam.SourceData,
		CreatedBy:                  middleware.GetRequestUser(c),
		UpdatedBy:                  "",
		CreatedTime:                &now,
		UpdatedTime:                &now,
	}
	action, tmpErr := db.GetInsertTableExecAction(models.TableNameBatchExec, *batchExecData, nil)
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

func UpdateBatchExec(c *gin.Context, batchExecId string, updateData map[string]interface{}) (err error) {
	if len(updateData) == 0 {
		return
	}
	var actions []*db.ExecAction
	updateColumnStr := ""
	params := []interface{}{}
	isFirst := true
	for col, val := range updateData {
		if !isFirst {
			updateColumnStr += ", "
		}
		updateColumnStr += fmt.Sprintf("`%s` = ?", col)
		params = append(params, val)
		isFirst = false
	}

	params = append(params, batchExecId)
	action := &db.ExecAction{
		Sql:   db.CombineDBSql("UPDATE ", models.TableNameBatchExec, " SET ", updateColumnStr, " WHERE id=?"),
		Param: params,
	}
	actions = append(actions, action)

	err = db.Transaction(actions, c)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	return
}

func InsertBatchExecJobs(c *gin.Context, batchExecId string, execTime *time.Time,
	batchExecRunParam *models.BatchExecRun,
	pluginCallParam *models.BatchExecutionPluginExecParam,
	batchExecRunResult *models.PluginInterfaceApiResultData) (err error) {
	var actions []*db.ExecAction
	now := time.Now()

	retJsonCallbackParameterMap := make(map[string]map[string]interface{})
	for i, output := range batchExecRunResult.Outputs {
		if v, isExisted := output[models.PluginCallResultPresetCallback]; isExisted {
			if callbackParam, ok := v.(string); ok {
				retJsonCallbackParameterMap[callbackParam] = batchExecRunResult.Outputs[i]
			}
		}
	}

	for i := range pluginCallParam.Inputs {
		inputJson := ""
		inputJsonByte, tmpErr := json.Marshal(pluginCallParam.Inputs[i])
		if tmpErr == nil {
			inputJson = string(inputJsonByte)
		}
		returnJson := ""
		errCode := ""
		errMsg := ""
		/*
			if len(batchExecRunResult.Outputs) > i {
				returnJsonByte, tmpErr := json.Marshal(batchExecRunResult.Outputs[i])
				if tmpErr == nil {
					returnJson = string(returnJsonByte)
				}
				if batchExecRunResult.Outputs[i] != nil {
					if tmpVal, isOk := batchExecRunResult.Outputs[i]["errorCode"].(string); isOk {
						errCode = tmpVal
					}
					if tmpVal, isOk := batchExecRunResult.Outputs[i]["errorMessage"].(string); isOk {
						errMsg = tmpVal
					}
				}
			}
		*/
		if outputData, isExisted := retJsonCallbackParameterMap[pluginCallParam.EntityInstances[i].Id]; isExisted {
			if outputData != nil {
				returnJsonByte, tmpErr := json.Marshal(outputData)
				if tmpErr == nil {
					returnJson = string(returnJsonByte)
				}
				if tmpVal, isOk := outputData[models.PluginCallResultPresetErrorCode].(string); isOk {
					errCode = tmpVal
				}
				if tmpVal, isOk := outputData[models.PluginCallResultPresetErrorMsg].(string); isOk {
					errMsg = tmpVal
				}
			}
		}

		batchExecJobData := &models.BatchExecutionJobs{
			Id:                      guid.CreateGuid(),
			BatchExecutionId:        batchExecId,
			PackageName:             batchExecRunParam.PackageName,
			EntityName:              batchExecRunParam.EntityName,
			BusinessKey:             pluginCallParam.EntityInstances[i].BusinessKeyValue,
			RootEntityId:            pluginCallParam.EntityInstances[i].Id,
			ExecuteTime:             execTime,
			CompleteTime:            &now,
			ErrorCode:               errCode,
			ErrorMessage:            errMsg,
			InputJson:               inputJson,
			ReturnJson:              returnJson,
			PluginConfigInterfaceId: batchExecRunParam.PluginConfigInterface.Id,
		}
		action, tmpErr := db.GetInsertTableExecAction(models.TableNameBatchExecJobs, *batchExecJobData, nil)
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
	return
}

func GetPluginConfigRoles(c *gin.Context, pluginConfigId string) (result []*models.PluginConfigRoles, err error) {
	var pluginConfigRolesData []*models.PluginConfigRoles
	err = db.MysqlEngine.Context(c).Table(models.TableNamePluginConfigRoles).
		Where("plugin_cfg_id = ?", pluginConfigId).
		Find(&pluginConfigRolesData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	result = pluginConfigRolesData
	return
}

func GetPluginConfigsById(c *gin.Context, pluginConfigId string) (result *models.PluginConfigs, err error) {
	var pluginConfigsData []*models.PluginConfigs
	err = db.MysqlEngine.Context(c).Table(models.TableNamePluginConfigs).
		Where("id = ?", pluginConfigId).
		Find(&pluginConfigsData)

	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	if len(pluginConfigsData) == 0 {
		err = fmt.Errorf("pluginConfigId: %s is invalid", pluginConfigId)
		return
	}

	result = pluginConfigsData[0]
	return
}

func ValidateBatchExecName(c *gin.Context, batchExecReqParam *models.BatchExecRun, continueToken string) (isValid bool, err error) {
	var exists bool
	batchExecData := models.BatchExecution{}
	exists, err = db.MysqlEngine.Context(c).Table(models.TableNameBatchExec).
		Where("name = ?", batchExecReqParam.Name).
		Get(&batchExecData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if !exists {
		isValid = true
		return
	}

	// continueToken 不为空，批量执行记录详情已存在
	if continueToken != "" {
		if batchExecReqParam.BatchExecId == batchExecData.Id {
			isValid = true
			return
		}
	}
	isValid = false
	return
}
