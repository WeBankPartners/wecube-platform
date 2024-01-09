package database

import (
	"context"
	"fmt"
	"strings"
	"time"

	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

// AddProcessDefinition 添加编排
func AddProcessDefinition(ctx context.Context, user string, param models.ProcessDefinitionParam) (err error) {
	// 1.权限参数校验
	if len(param.PermissionToRole.USE) == 0 || len(param.PermissionToRole.MGMT) == 0 {
		return exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err,permissionToRole is empty"))
	}
	// 2. 校验编排是否重复
	procDef, err := GetProcessDefinition(ctx, param.Id)
	if err != nil {
		return
	}
	// 3. 添加编排
	if procDef != nil && procDef.Status != string(models.Draft) {
		return exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("request param err"))
	}
	return execAddProcessDefinition(ctx, user, param, procDef)
}

func execAddProcessDefinition(ctx context.Context, user string, param models.ProcessDefinitionParam, procDef *models.ProcDef) (err error) {
	var draftEntity *models.ProcDef
	now := time.Now()
	if procDef != nil {
		draftEntity = procDef
	}
	if procDef == nil {
		draftEntity.Id = guid.CreateGuid()
		draftEntity.Status = string(models.Draft)
		draftEntity.CreatedBy = user
		draftEntity.CreatedTime = now
		err = insertProcDef(ctx, draftEntity)
		if err != nil {
			return
		}
	}
	draftEntity.Name = param.Name
	draftEntity.Version = param.Version
	draftEntity.Tags = param.Tags
	draftEntity.ForPlugin = strings.Join(param.AuthPlugins, ",")
	draftEntity.Scene = param.UseCase
	draftEntity.ConflictCheck = param.ConflictCheck
	draftEntity.UpdatedBy = user
	draftEntity.UpdatedTime = now
	err = UpdateProcDef(ctx, draftEntity)
	if err != nil {
		return err
	}
	// 绑定编排权限
	err = batchAddProcDefPermission(ctx, draftEntity.Id, param.PermissionToRole)
	return
}

// GetProcessDefinition 获取编排定义
func GetProcessDefinition(ctx context.Context, id string) (result *models.ProcDef, err error) {
	if id == "" {
		return
	}
	var list []*models.ProcDef
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def where id = ?", id).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(list) > 0 {
		result = list[0]
	}
	return
}

func UpdateProcDef(ctx context.Context, procDef *models.ProcDef) (err error) {
	var actions []*db.ExecAction
	sql, params := transProcDefUpdateConditionToSQL(procDef)
	actions = append(actions, &db.ExecAction{Sql: sql, Param: params})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func batchAddProcDefPermission(ctx context.Context, procDefId string, permission models.PermissionToRole) (err error) {
	if len(permission.USE) > 0 {
		for _, roleName := range permission.USE {
			err = saveProcDefPermission(ctx, procDefId, roleName, string(models.USE))
			if err != nil {
				return
			}
		}
	}
	if len(permission.MGMT) > 0 {
		for _, roleName := range permission.MGMT {
			err = saveProcDefPermission(ctx, procDefId, roleName, string(models.MGMT))
			if err != nil {
				return
			}
		}
	}
	return
}

func saveProcDefPermission(ctx context.Context, procDefId, roleName, perm string) (err error) {
	var list []*models.ProcDefPermission
	list, err = GetProcDefPermissionByCondition(ctx, models.ProcDefPermission{ProcDefId: procDefId, RoleName: roleName, Permission: perm})
	if err != nil {
		return
	}
	if len(list) > 0 {
		log.Logger.Warn("found stored data in DB", log.String("procId", procDefId), log.String("roleName", roleName), log.String("permission", perm))
		return
	}
	return insertProcDefPermission(ctx, models.ProcDefPermission{Id: guid.CreateGuid(), ProcDefId: procDefId, RoleId: roleName, RoleName: roleName, Permission: perm})
}

func insertProcDef(ctx context.Context, procDef *models.ProcDef) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "insert into  proc_def(id,key,name,version,root_entitiy,status,tags,for_plugin,scene," +
		"conflict_check,created_by,created_time,updated_by,updated_time) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{procDef.Id,
		procDef.Key, procDef.Name, procDef.Version, procDef.RootEntity, procDef.Status, procDef.Status, procDef.Tags, procDef.ForPlugin, procDef.Scene,
		procDef.ConflictCheck, procDef.CreatedBy, procDef.CreatedTime, procDef.UpdatedBy, procDef.UpdatedTime}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func insertProcDefPermission(ctx context.Context, permission models.ProcDefPermission) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "insert into proc_def_permission(id,proc_def_id,role_id,role_name," +
		"permission)values(?,?,?,?,?)", Param: []interface{}{permission.Id, permission.ProcDefId, permission.RoleId, permission.RoleName}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

// GetProcDefPermissionByCondition 根据条件 获取编排权限
func GetProcDefPermissionByCondition(ctx context.Context, permission models.ProcDefPermission) (list []*models.ProcDefPermission, err error) {
	var params []interface{}
	sql := "select * from proc_def_permission where 1=1"
	if permission.Id != "" {
		sql = sql + " and id = ?"
		params = append(params, permission.Id)

	}
	if permission.ProcDefId != "" {
		sql = sql + " and proc_def_id = ?"
		params = append(params, permission.ProcDefId)
	}
	if permission.RoleName != "" {
		sql = sql + " and role_name = ?"
		params = append(params, permission.RoleName)
	}
	if permission.Permission != "" {
		sql = sql + " and permission = ?"
		params = append(params, permission.Permission)
	}
	err = db.MysqlEngine.Context(ctx).SQL(sql, params).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func transProcDefUpdateConditionToSQL(procDef *models.ProcDef) (sql string, params []interface{}) {
	sql = "update proc_def set id = ?"
	params = append(params, procDef.Id)
	if procDef.Key != "" {
		sql = sql + ",key=?"
		params = append(params, procDef.Key)
	}
	if procDef.Name != "" {
		sql = sql + ",name=?"
		params = append(params, procDef.Name)
	}
	if procDef.Version != "" {
		sql = sql + ",version=?"
		params = append(params, procDef.Version)
	}
	if procDef.RootEntity != "" {
		sql = sql + ",root_entity=?"
		params = append(params, procDef.RootEntity)
	}
	if procDef.Status != "" {
		sql = sql + ",status=?"
		params = append(params, procDef.Status)
	}
	if procDef.Tags != "" {
		sql = sql + ",tags=?"
		params = append(params, procDef.Tags)
	}
	if procDef.ForPlugin != "" {
		sql = sql + ",for_plugin=?"
		params = append(params, procDef.ForPlugin)
	}
	if procDef.Scene != "" {
		sql = sql + ",scene=?"
		params = append(params, procDef.Scene)
	}
	sql = sql + ",conflictCheck=?"
	params = append(params, procDef.ConflictCheck)
	if procDef.UpdatedBy != "" {
		sql = sql + ",updated_by=?"
		params = append(params, procDef.UpdatedBy)
	}
	sql = sql + ",updated_time=?"
	params = append(params, procDef.UpdatedTime)
	sql = " where id= ?"
	params = append(params, procDef.Id)
	return
}
