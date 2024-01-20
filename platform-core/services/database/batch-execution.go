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

func CreateOrUpdateBatchExecTemplate(c *gin.Context, reqParam *models.CreateOrUpdateBatchExecTemplateReq) (err error) {
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
			Sql:   db.CombineDBQuery("UPDATE ", models.TableNameBatchExecTemplate, " SET ", updateColumnStr, " WHERE id=?"),
			Param: []interface{}{reqParam.Name, reqParam.OperateObject, reqParam.PluginService, reqParam.ConfigData, middleware.GetRequestUser(c), now, reqParam.Id},
		}
		actions = append(actions, action)
	}
	batchExecTemplateId := reqParam.Id
	// update batchExecTemplateRole
	// firstly delete original batchExecTemplateRole and then create new batchExecTemplateRole
	action := &db.ExecAction{
		Sql:   db.CombineDBQuery("DELETE FROM ", models.TableNameBatchExecTemplateRole, " WHERE batch_execution_template_id=?"),
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
				Permission:               string(models.MGMT),
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
				Permission:               string(models.USE),
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
