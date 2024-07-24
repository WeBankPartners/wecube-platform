package database

import (
	"context"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/tools"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"sort"
	"strconv"
	"strings"
	"time"

	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

// QueryProcessDefinitionList 查询编排列表
func QueryProcessDefinitionList(ctx context.Context, param models.QueryProcessDefinitionParam, userToken, language, operator string) (list []*models.ProcDefQueryDto, err error) {
	var procDefList, pList, filterProcDefList []*models.ProcDef
	var permissionList []*models.ProcDefPermission
	var roleProcDefMap = make(map[string][]*models.ProcDefDto)
	var userRolesMap = convertArray2Map(param.UserRoles)
	var manageRoles, userRoles, allManageRoles, manageRolesDisplay, userRolesDisplay, userPermissionRoles, managePermissionRoles []string
	var response models.QueryRolesResponse
	var roleDisplayNameMap = make(map[string]string)
	var enabledCreated bool
	var queryParam []interface{}
	var where string
	list = make([]*models.ProcDefQueryDto, 0)
	filterProcDefList = make([]*models.ProcDef, 0)
	where, queryParam = transProcDefConditionToSQL(param)
	procDefList, err = getProcessDefinitionByWhere(ctx, where, queryParam)
	if err != nil {
		return
	}
	if len(procDefList) == 0 {
		return
	}
	// 通过授权插件过滤数据
	if len(param.Plugins) > 0 {
		for _, procDef := range procDefList {
			for _, plugin := range param.Plugins {
				if strings.Contains(procDef.ForPlugin, plugin) {
					filterProcDefList = append(filterProcDefList, procDef)
				}
			}
		}
	} else {
		filterProcDefList = procDefList
	}
	response, err = remote.RetrieveAllLocalRoles("Y", userToken, language, false)
	if err != nil {
		return
	}
	collectProcDefMap := make(map[string]string)
	if collectProcDefMap, err = getUserProcDefCollectMap(ctx, operator); err != nil {
		return
	}
	if len(response.Data) > 0 {
		for _, roleDto := range response.Data {
			roleDisplayNameMap[roleDto.Name] = roleDto.DisplayName
		}
	}
	lastVersionMap := make(map[string]string) // map k -> procDefKey v -> version
	if param.LastVersion {
		for _, procDef := range filterProcDefList {
			if ver, ok := lastVersionMap[procDef.Key]; ok {
				if tools.CompareVersion(procDef.Version, ver) {
					lastVersionMap[procDef.Key] = procDef.Version
				}
			} else {
				lastVersionMap[procDef.Key] = procDef.Version
			}
		}
	}
	for _, procDef := range filterProcDefList {
		if param.LastVersion {
			if ver, ok := lastVersionMap[procDef.Key]; ok {
				if procDef.Version != ver {
					continue
				}
			}
		}
		enabledCreated = false
		manageRoles = []string{}
		managePermissionRoles = []string{}
		userRoles = []string{}
		userPermissionRoles = []string{}
		manageRolesDisplay = []string{}
		userRolesDisplay = []string{}
		permissionList, err = GetProcDefPermissionByCondition(ctx, models.ProcDefPermission{ProcDefId: procDef.Id})
		if err != nil {
			return
		}
		if procDef.Status == string(models.Deployed) {
			pList, err = GetProcessDefinitionByCondition(ctx, models.ProcDefCondition{Key: procDef.Key, Status: string(models.Draft)})
			if err != nil {
				return
			}
			// 没有同 key并且 草稿态的编排,则可以新建编排
			if len(pList) == 0 {
				enabledCreated = true
			}
		}
		for _, permission := range permissionList {
			if permission.Permission == "MGMT" {
				manageRoles = append(manageRoles, permission.RoleName)
				manageRolesDisplay = append(manageRolesDisplay, roleDisplayNameMap[permission.RoleName])
			} else if permission.Permission == "USE" {
				userRoles = append(userRoles, permission.RoleName)
				userRolesDisplay = append(userRolesDisplay, roleDisplayNameMap[permission.RoleName])
			}
			if permission.Permission == "MGMT" && userRolesMap[permission.RoleName] {
				managePermissionRoles = append(managePermissionRoles, permission.RoleName)
			}
			if permission.Permission == "USE" && userRolesMap[permission.RoleName] {
				userPermissionRoles = append(userPermissionRoles, permission.RoleName)
			}
		}
		if param.PermissionType == "MGMT" {
			for _, manageRole := range managePermissionRoles {
				if _, ok := roleProcDefMap[manageRole]; !ok {
					roleProcDefMap[manageRole] = make([]*models.ProcDefDto, 0)
					allManageRoles = append(allManageRoles, manageRole)
				}
				roleProcDefMap[manageRole] = append(roleProcDefMap[manageRole], models.BuildProcDefDto(procDef, userRoles, manageRoles, userRolesDisplay, manageRolesDisplay, enabledCreated, collectProcDefMap))
			}
		} else {
			for _, userRole := range userPermissionRoles {
				if _, ok := roleProcDefMap[userRole]; !ok {
					roleProcDefMap[userRole] = make([]*models.ProcDefDto, 0)
					allManageRoles = append(allManageRoles, userRole)
				}
				roleProcDefMap[userRole] = append(roleProcDefMap[userRole], models.BuildProcDefDto(procDef, userRoles, manageRoles, userRolesDisplay, manageRolesDisplay, enabledCreated, collectProcDefMap))
			}
		}
	}
	// 角色排序
	sort.Strings(allManageRoles)
	for _, manageRole := range allManageRoles {
		dataList := roleProcDefMap[manageRole]
		// 排序
		sort.Sort(models.ProcDefDtoSort(dataList))
		list = append(list, &models.ProcDefQueryDto{
			ManageRole:        manageRole,
			ManageRoleDisplay: roleDisplayNameMap[manageRole],
			ProcDefList:       dataList,
		})
	}
	return
}

func QueryPluginProcessDefinitionList(ctx context.Context, plugin string) (list []*models.ProcDef, err error) {
	var allProcDefList []*models.ProcDef
	list = make([]*models.ProcDef, 0)
	allProcDefList, err = GetDeployedProcessDefinitionList(ctx)
	if err != nil {
		return
	}
	if len(allProcDefList) == 0 {
		return
	}
	for _, procDef := range allProcDefList {
		if strings.Contains(procDef.ForPlugin, plugin) {
			permissionList, _ := GetProcDefPermissionByCondition(ctx, models.ProcDefPermission{ProcDefId: procDef.Id, Permission: "MGMT"})
			if len(permissionList) > 0 {
				procDef.ManageRole = permissionList[0].RoleName
			}
			list = append(list, procDef)
		}
	}
	return
}

// AddProcessDefinition 添加编排
func AddProcessDefinition(ctx context.Context, user string, param models.ProcessDefinitionParam) (draftEntity *models.ProcDef, err error) {
	draftEntity = &models.ProcDef{}
	now := time.Now()
	draftEntity.Id = "pdef_" + guid.CreateGuid()
	draftEntity.Status = string(models.Draft)
	draftEntity.Key = "pdef_key_" + guid.CreateGuid()
	draftEntity.Name = param.Name
	draftEntity.Tags = param.Tags
	draftEntity.ForPlugin = strings.Join(param.AuthPlugins, ",")
	draftEntity.Scene = param.Scene
	draftEntity.ConflictCheck = param.ConflictCheck
	draftEntity.CreatedBy = user
	draftEntity.CreatedTime = now
	draftEntity.UpdatedBy = user
	draftEntity.UpdatedTime = now
	draftEntity.RootEntity = param.RootEntity
	// 计算编排的版本
	draftEntity.Version = "v1"
	err = insertProcDef(ctx, draftEntity)
	if err != nil {
		return
	}
	return
}

func CopyProcessDefinitionByDto(ctx context.Context, procDef *models.ProcessDefinitionDto, userToken, language, operator string) (newProcDefId string, err error) {
	var permissionList []*models.ProcDefPermission
	var nodeList []*models.ProcDefNode
	var linkList []*models.ProcDefNodeLink
	var nodeParamList []*models.ProcDefNodeParam
	var response models.QueryRolesResponse
	var now = time.Now()
	procDefModel := models.ConvertProcDefDto2Model(procDef.ProcDef)
	if procDefModel == nil {
		err = fmt.Errorf("procDef is emtpy")
		return
	}
	// 编排的创建更新人设置为当前人
	procDefModel.CreatedBy = operator
	procDefModel.UpdatedBy = operator
	procDefModel.CreatedTime = now
	procDefModel.UpdatedTime = now
	// 获取操作用户的角色
	response, err = remote.GetRolesByUsername(operator, userToken, language)
	if err != nil {
		return
	}
	if len(response.Data) > 0 {
		// 设置当前用户的默认角色为属主和使用角色
		roleName := response.Data[0].Name
		permissionList = append(permissionList, &models.ProcDefPermission{
			ProcDefId:  procDef.ProcDef.Id,
			RoleId:     roleName,
			RoleName:   roleName,
			Permission: "USE",
		})
		permissionList = append(permissionList, &models.ProcDefPermission{
			ProcDefId:  procDef.ProcDef.Id,
			RoleId:     roleName,
			RoleName:   roleName,
			Permission: "MGMT",
		})
	}

	if len(procDef.ProcDefNodeExtend.Nodes) > 0 {
		for _, node := range procDef.ProcDefNodeExtend.Nodes {
			if node != nil {
				if node.ProcDefNodeCustomAttrs.SubProcDefName != "" && node.ProcDefNodeCustomAttrs.SubProcDefVersion != "" {
					subProcDefList, tmpErr := GetProcessDefinitionByCondition(ctx, models.ProcDefCondition{Name: node.ProcDefNodeCustomAttrs.SubProcDefName, Version: node.ProcDefNodeCustomAttrs.SubProcDefVersion})
					if tmpErr != nil {
						err = tmpErr
						return
					}
					if len(subProcDefList) == 0 {
						err = fmt.Errorf("Can not find sub process with name:version -> %s:%s ", node.ProcDefNodeCustomAttrs.SubProcDefName, node.ProcDefNodeCustomAttrs.SubProcDefVersion)
						return
					}
					node.ProcDefNodeCustomAttrs.SubProcDefId = subProcDefList[0].Id
				}
				nodeModel, nodeParams := models.ConvertProcDefNodeResultDto2Model(node)
				if nodeModel != nil {
					nodeModel.CreatedBy = operator
					nodeModel.UpdatedBy = operator
					nodeModel.CreatedTime = now
					nodeModel.UpdatedTime = now
					nodeList = append(nodeList, nodeModel)
				}
				nodeParamList = append(nodeParamList, nodeParams...)
			}
		}
	}
	if len(procDef.ProcDefNodeExtend.Edges) > 0 {
		for _, link := range procDef.ProcDefNodeExtend.Edges {
			if link != nil {
				linkList = append(linkList, models.CovertNodeLinkDto2Model(link))
			}
		}
	}

	return execCopyProcessDefinition(ctx, procDefModel, nodeList, linkList, nodeParamList, permissionList, operator)
}

// CopyProcessDefinition 复制编排
func CopyProcessDefinition(ctx context.Context, procDef *models.ProcDef, operator string) (newProcDefId string, err error) {
	var permissionList []*models.ProcDefPermission
	var nodeList []*models.ProcDefNode
	var linkList []*models.ProcDefNodeLink
	var allNodeParamList []*models.ProcDefNodeParam
	// 查询权限
	permissionList, err = GetProcDefPermissionByCondition(ctx, models.ProcDefPermission{ProcDefId: procDef.Id})
	if err != nil {
		return
	}
	// 查询编排节点列表
	nodeList, err = GetProcDefNodeModelByProcDefId(ctx, procDef.Id)
	if err != nil {
		return
	}
	// 获取编排线
	linkList, err = GetProcDefNodeLinkListByProcDefId(ctx, procDef.Id)
	if err != nil {
		return
	}
	if len(nodeList) > 0 {
		for _, node := range nodeList {
			nodeParamList, _ := GetProcDefNodeParamByNodeId(ctx, node.Id)
			// nodeParamList 中 存储nodeId,为node.NodeId,而非 node.id
			if len(nodeParamList) > 0 {
				for _, param := range nodeParamList {
					param.ProcDefNodeId = node.NodeId
				}
				allNodeParamList = append(allNodeParamList, nodeParamList...)
			}
		}
	}

	return execCopyProcessDefinition(ctx, procDef, nodeList, linkList, allNodeParamList, permissionList, operator)
}

func execCopyProcessDefinition(ctx context.Context, procDef *models.ProcDef, nodeList []*models.ProcDefNode,
	linkList []*models.ProcDefNodeLink, nodeParamList []*models.ProcDefNodeParam, permissionList []*models.ProcDefPermission, operator string) (newProcDefId string, err error) {
	var curNodeParamList []*models.ProcDefNodeParam
	newProcDefId = "pdef_" + guid.CreateGuid()
	currTime := time.Now().Format(models.DateTimeFormat)
	var actions []*db.ExecAction
	// 插入编排
	actions = append(actions, &db.ExecAction{Sql: "insert into proc_def (id,`key`,name,root_entity,status,tags,for_plugin,scene," +
		"conflict_check,created_by,version,sub_proc,created_time,updated_by,updated_time) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{newProcDefId,
		procDef.Key, procDef.Name, procDef.RootEntity, models.Draft, procDef.Tags, procDef.ForPlugin, procDef.Scene,
		procDef.ConflictCheck, operator, procDef.Version, procDef.SubProc, currTime, operator, currTime}})

	// 插入权限
	if len(permissionList) > 0 {
		for _, permission := range permissionList {
			actions = append(actions, &db.ExecAction{Sql: "insert into proc_def_permission(id,proc_def_id,role_id,role_name,permission)values(" +
				"?,?,?,?,?)", Param: []interface{}{guid.CreateGuid(), newProcDefId, permission.RoleId, permission.RoleName, permission.Permission}})
		}
	}

	// 插入节点 & 节点参数 & 更新线集合中节点ID数据
	if len(nodeList) > 0 {
		for _, node := range nodeList {
			curNodeParamList = []*models.ProcDefNodeParam{}
			newNodeId := models.GenNodeId(node.NodeType)
			actions = append(actions, &db.ExecAction{Sql: "insert into  proc_def_node(id,node_id,proc_def_id,name,description,status,node_type,service_name," +
				"dynamic_bind,bind_node_id,risk_check,routine_expression,context_param_nodes,timeout,time_config,ordered_no,ui_style,created_by,created_time," +
				"updated_by,updated_time,allow_continue,sub_proc_def_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{newNodeId, node.NodeId, newProcDefId, node.Name, node.Description,
				models.Draft, node.NodeType, node.ServiceName, node.DynamicBind, node.BindNodeId, node.RiskCheck, node.RoutineExpression, node.ContextParamNodes,
				node.Timeout, node.TimeConfig, node.OrderedNo, node.UiStyle, operator, currTime, node.UpdatedBy, currTime, node.AllowContinue, node.SubProcDefId}})
			for _, nodeParam := range nodeParamList {
				if nodeParam.ProcDefNodeId == node.NodeId {
					curNodeParamList = append(curNodeParamList, nodeParam)
				}
			}
			for _, nodeParam := range curNodeParamList {
				actions = append(actions, &db.ExecAction{Sql: "insert into  proc_def_node_param(id,proc_def_node_id,param_id,name,bind_type," +
					"value,ctx_bind_node,ctx_bind_type,ctx_bind_name,required) values (?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{guid.CreateGuid(), newNodeId, nodeParam.ParamId,
					nodeParam.Name, nodeParam.BindType, nodeParam.Value, nodeParam.CtxBindNode, nodeParam.CtxBindType, nodeParam.CtxBindName, nodeParam.Required}})
			}
			// 遍历 线集合,找到以前老节点(包含source,target的老节点),更新成新的节点id
			if len(linkList) > 0 {
				for _, link := range linkList {
					if link.Source == node.Id {
						link.Source = newNodeId
					}
					if link.Target == node.Id {
						link.Target = newNodeId
					}
				}
			}
		}
	}

	// 插入线
	if len(linkList) > 0 {
		for _, nodeLink := range linkList {
			actions = append(actions, &db.ExecAction{Sql: "insert into  proc_def_node_link(id,source,target,name,ui_style,link_id,proc_def_id) values(?,?,?,?,?,?,?)",
				Param: []interface{}{"pdl_" + guid.CreateGuid(), nodeLink.Source, nodeLink.Target, nodeLink.Name, nodeLink.UiStyle, nodeLink.LinkId, newProcDefId}})
		}
	}

	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
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

func getProcessDefinitionByWhere(ctx context.Context, where string, queryParam []interface{}) (list []*models.ProcDef, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def "+where, queryParam...).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func GetDeployedProcessDefinitionList(ctx context.Context) (list []*models.ProcDef, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def where status = ?", string(models.Deployed)).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func GetDeployedProcessDefinitionByIds(ctx context.Context, ids []string) (list []*models.ProcDef, err error) {
	idsFilterSql, idsFilterParam := createListParams(ids, "")
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def where id in ("+idsFilterSql+") and status='deployed'", idsFilterParam...).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func GetProcessDefinitionByCondition(ctx context.Context, condition models.ProcDefCondition) (list []*models.ProcDef, err error) {
	var param []interface{}
	sql := "select * from proc_def where 1= 1"
	if condition.Name != "" {
		sql = sql + " and name = ?"
		param = append(param, condition.Name)
	}
	if condition.Key != "" {
		sql = sql + " and `key` = ?"
		param = append(param, condition.Key)
	}
	if condition.Status != "" {
		sql = sql + " and status = ?"
		param = append(param, condition.Status)
	}
	if condition.Version != "" {
		sql = sql + " and `version` = ?"
		param = append(param, condition.Version)
	}
	err = db.MysqlEngine.Context(ctx).SQL(sql, param...).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func UpdateProcDef(ctx context.Context, procDef *models.ProcDef) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "update proc_def set name=?,root_entity=?,tags=?,for_plugin=?,scene=?," +
		"conflict_check=?,updated_by=?,updated_time=?,sub_proc=? where id=?", Param: []interface{}{procDef.Name, procDef.RootEntity,
		procDef.Tags, procDef.ForPlugin, procDef.Scene, procDef.ConflictCheck, procDef.UpdatedBy, procDef.UpdatedTime, procDef.SubProc, procDef.Id}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func UpdateProcDefHandler(ctx context.Context, procDefId, handler string) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "update proc_def set updated_by=?,updated_time=? where id=?", Param: []interface{}{handler, time.Now(), procDefId}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

// UpdateProcDefAndNode 编排根节点改变,需要同时清除编排所有节点的 RoutineExpression、ServiceName、ParamInfos
func UpdateProcDefAndNode(ctx context.Context, procDef *models.ProcDef, nodeList []*models.ProcDefNode) (err error) {
	var actions []*db.ExecAction
	// 更新编排表
	actions = append(actions, &db.ExecAction{Sql: "update proc_def set name=?,root_entity=?,tags=?,for_plugin=?,scene=?," +
		"conflict_check=?,updated_by=?,updated_time=?,sub_proc=? where id=?", Param: []interface{}{procDef.Name, procDef.RootEntity,
		procDef.Tags, procDef.ForPlugin, procDef.Scene, procDef.ConflictCheck, procDef.UpdatedBy, procDef.UpdatedTime, procDef.SubProc, procDef.Id}})
	// 更新节点表
	actions = append(actions, &db.ExecAction{Sql: "update proc_def_node  set service_name = null,routine_expression = null where" +
		" proc_def_id =?", Param: []interface{}{procDef.Id}})
	for _, node := range nodeList {
		// 删除节点参数表
		actions = append(actions, &db.ExecAction{Sql: "delete from proc_def_node_param where proc_def_node_id = ?", Param: []interface{}{node.Id}})
	}
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func UpdateProcDefStatus(ctx context.Context, procDef *models.ProcDef) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "update proc_def set status=?,updated_by=?,updated_time=? where id = ?", Param: []interface{}{
		procDef.Status, procDef.UpdatedBy, procDef.UpdatedTime, procDef.Id}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}
func UpdateProcDefStatusAndVersion(ctx context.Context, procDef *models.ProcDef) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "update proc_def set status=?,version=?,updated_by=?,updated_time=? where id = ?", Param: []interface{}{
		procDef.Status, procDef.Version, procDef.UpdatedBy, procDef.UpdatedTime, procDef.Id}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

// GetProcDefNode 获取编排节点
func GetProcDefNode(ctx context.Context, procDefId, nodeId string) (result *models.ProcDefNode, err error) {
	var list []*models.ProcDefNode
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def_node where proc_def_id = ? and node_id = ?", procDefId, nodeId).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(list) > 0 {
		result = list[0]
	}
	return
}

// GetProcDefNodeByIdAndProcDefId 获取编排节点
func GetProcDefNodeByIdAndProcDefId(ctx context.Context, procDefId, id string) (result *models.ProcDefNode, err error) {
	var list []*models.ProcDefNode
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def_node where proc_def_id = ? and id = ?", procDefId, id).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(list) > 0 {
		result = list[0]
	}
	return
}

// GetProcDefNodeLink  获取编排线
func GetProcDefNodeLink(ctx context.Context, procDefId string, linkId string) (result *models.ProcDefNodeLink, err error) {
	var list []*models.ProcDefNodeLink
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def_node_link where proc_def_id= ? and link_id = ?  ", procDefId, linkId).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(list) > 0 {
		result = list[0]
	}
	return
}

// GetProcDefNodeLinkListByProcDefId  根据编排id获取编排线列表
func GetProcDefNodeLinkListByProcDefId(ctx context.Context, procDefId string) (list []*models.ProcDefNodeLink, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def_node_link where proc_def_id= ? ", procDefId).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func GetProcDefNodeLinkByProcDefIdAndTarget(ctx context.Context, procDefId string, target string) (list []*models.ProcDefNodeLink, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def_node_link where proc_def_id= ? and target = ?", procDefId, target).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func GetProcDefNodeLinkByProcDefIdAndSource(ctx context.Context, procDefId string, source string) (list []*models.ProcDefNodeLink, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def_node_link where proc_def_id= ? and source = ?", procDefId, source).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func GetProcDefNodeByProcDefId(ctx context.Context, procDefId string) (list []*models.ProcDefNode, result []*models.ProcDefNodeResultDto, err error) {
	list = make([]*models.ProcDefNode, 0)
	if procDefId == "" {
		return
	}
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def_node where proc_def_id = ?", procDefId).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	var subProcDefIdList []string
	for _, procDefNode := range list {
		var nodeParamList []*models.ProcDefNodeParam
		err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def_node_param where proc_def_node_id = ?", procDefNode.Id).Find(&nodeParamList)
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}
		result = append(result, models.ConvertProcDefNode2Dto(procDefNode, nodeParamList))
		if procDefNode.SubProcDefId != "" {
			subProcDefIdList = append(subProcDefIdList, procDefNode.SubProcDefId)
		}
	}
	if len(subProcDefIdList) > 0 {
		var subProcDefRows []*models.ProcDef
		filterSql, filterParam := db.CreateListParams(subProcDefIdList, "")
		err = db.MysqlEngine.Context(ctx).SQL("select id,`key`,name,`version` from proc_def where id in ("+filterSql+")", filterParam...).Find(&subProcDefRows)
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}
		sucProcMap := make(map[string]*models.ProcDef)
		for _, row := range subProcDefRows {
			sucProcMap[row.Id] = row
		}
		for _, resultObj := range result {
			if subProcObj, ok := sucProcMap[resultObj.ProcDefNodeCustomAttrs.SubProcDefId]; ok {
				resultObj.ProcDefNodeCustomAttrs.SubProcDefName = subProcObj.Name
				resultObj.ProcDefNodeCustomAttrs.SubProcDefVersion = subProcObj.Version
			}
		}
	}
	return
}

func GetProcDefNodeModelByProcDefId(ctx context.Context, procDefId string) (list []*models.ProcDefNode, err error) {
	if procDefId == "" {
		return
	}
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def_node where proc_def_id = ?", procDefId).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

// GetProcDefNodeById 根据编排Id编排节点
func GetProcDefNodeById(ctx context.Context, procDefId string) (list []*models.ProcDefNode, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def_node where proc_def_id = ? ", procDefId).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

// InsertProcDefNodeLink 添加编排节点线
func InsertProcDefNodeLink(ctx context.Context, nodeLink *models.ProcDefNodeLink) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "insert into  proc_def_node_link(id,source,target,name,ui_style,link_id,proc_def_id) values(?,?,?,?,?,?,?)",
		Param: []interface{}{nodeLink.Id, nodeLink.Source, nodeLink.Target, nodeLink.Name, nodeLink.UiStyle, nodeLink.LinkId, nodeLink.ProcDefId}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

// UpdateProcDefNodeLink 更新编排节点线
func UpdateProcDefNodeLink(ctx context.Context, procDefNodeLink *models.ProcDefNodeLink) (err error) {
	var actions []*db.ExecAction
	sql, params := transProcDefNodeLinkUpdateConditionToSQL(procDefNodeLink)
	actions = append(actions, &db.ExecAction{Sql: sql, Param: params})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

// InsertProcDefNode 添加编排节点
func InsertProcDefNode(ctx context.Context, node *models.ProcDefNode) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "insert into  proc_def_node(id,node_id,proc_def_id,name,description,status,node_type,service_name," +
		"dynamic_bind,bind_node_id,risk_check,routine_expression,context_param_nodes,timeout,time_config,ordered_no,ui_style,created_by,created_time," +
		"updated_by,updated_time,allow_continue,sub_proc_def_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{node.Id, node.NodeId, node.ProcDefId, node.Name, node.Description,
		node.Status, node.NodeType, node.ServiceName, node.DynamicBind, node.BindNodeId, node.RiskCheck, node.RoutineExpression, node.ContextParamNodes,
		node.Timeout, node.TimeConfig, node.OrderedNo, node.UiStyle, node.CreatedBy, node.CreatedTime.Format(models.DateTimeFormat), node.UpdatedBy, node.UpdatedTime.Format(models.DateTimeFormat), node.AllowContinue, node.SubProcDefId}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func DeleteProcDef(ctx context.Context, procDefId string) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "delete  from proc_def where id=? and status = ?", Param: []interface{}{procDefId, string(models.Draft)}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

// DeleteProcDefChain 删除编排链,包含节点，线，节点参数
func DeleteProcDefChain(ctx context.Context, procDefId string) (err error) {
	var nodeIds []string
	nodeIds, err = getProcDefNodeIdsByProcDefId(ctx, procDefId)
	if err != nil {
		return
	}
	var actions []*db.ExecAction
	// 删除编排线
	actions = append(actions, &db.ExecAction{Sql: "delete  from proc_def_node_link where proc_def_id=?", Param: []interface{}{procDefId}})
	// 删除编排节点参数定义
	if len(nodeIds) > 0 {
		nodeIdsFilterSql, nodeIdsFilterParam := createListParams(nodeIds, "")
		actions = append(actions, &db.ExecAction{Sql: "delete  from proc_def_node_param where proc_def_node_id in (" + nodeIdsFilterSql + ")", Param: nodeIdsFilterParam})
	}
	// 删除编排节点
	actions = append(actions, &db.ExecAction{Sql: "delete  from proc_def_node where proc_def_id=?", Param: []interface{}{procDefId}})
	// 删除权限表
	actions = append(actions, &db.ExecAction{Sql: "delete  from proc_def_permission where proc_def_id=?", Param: []interface{}{procDefId}})
	// 删除编排表
	actions = append(actions, &db.ExecAction{Sql: "delete  from proc_def  where id=?", Param: []interface{}{procDefId}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func getProcDefNodeIdsByProcDefId(ctx context.Context, procDefId string) (ids []string, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select id from proc_def_node where proc_def_id = ?", procDefId).Find(&ids)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func DeleteProcDefNode(ctx context.Context, procDefId, nodeId string) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "delete  from proc_def_node where proc_def_id=? and node_id=?", Param: []interface{}{procDefId, nodeId}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func DeleteProcDefNodeParamByNodeId(ctx context.Context, nodeId string) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "delete  from proc_def_node_param where proc_def_node_id=?", Param: []interface{}{nodeId}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func DeleteProcDefNodeLinkByNode(ctx context.Context, nodeId string) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "delete  from proc_def_node_link where source=? or target=?", Param: []interface{}{nodeId, nodeId}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func DeleteProcDefNodeLink(ctx context.Context, procDefId string, linkId, operator string) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "delete  from proc_def_node_link where proc_def_id=? and link_id= ?", Param: []interface{}{procDefId, linkId}})
	actions = append(actions, &db.ExecAction{Sql: "update proc_def set updated_by=?,updated_time=? where id=?", Param: []interface{}{operator, time.Now(), procDefId}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

// UpdateProcDefNode 更新编排节点
func UpdateProcDefNode(ctx context.Context, procDefNode *models.ProcDefNode) (err error) {
	var actions []*db.ExecAction
	sql, params := transProcDefNodeUpdateConditionToSQL(procDefNode)
	actions = append(actions, &db.ExecAction{Sql: sql, Param: params})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

// UpdateProcDefNodeStatusByProcDefId 根据编排id更新编排节点状态
func UpdateProcDefNodeStatusByProcDefId(ctx context.Context, procDefId, status, updatedBy string) (err error) {
	var actions []*db.ExecAction
	now := time.Now()
	actions = append(actions, &db.ExecAction{Sql: "update proc_def_node set status = ?,updated_by=?,updated_time = ?" +
		" where proc_def_id= ?", Param: []interface{}{status, updatedBy, now.Format(models.DateTimeFormat), procDefId}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func UpdateProcDefNodeOrder(ctx context.Context, nodeIndexMap map[string]int) (err error) {
	var actions []*db.ExecAction
	for k, v := range nodeIndexMap {
		actions = append(actions, &db.ExecAction{Sql: "update proc_def_node set ordered_no=? where id=?", Param: []interface{}{v, k}})
	}
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
	err = db.MysqlEngine.Context(ctx).SQL(sql, params...).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

// InsertProcDefNodeParam 添加编排节点参数
func InsertProcDefNodeParam(ctx context.Context, nodeParam *models.ProcDefNodeParam) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "insert into  proc_def_node_param(id,proc_def_node_id,param_id,name,bind_type," +
		"value,ctx_bind_node,ctx_bind_type,ctx_bind_name,required) values (?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{nodeParam.Id, nodeParam.ProcDefNodeId, nodeParam.ParamId,
		nodeParam.Name, nodeParam.BindType, nodeParam.Value, nodeParam.CtxBindNode, nodeParam.CtxBindType, nodeParam.CtxBindName, nodeParam.Required}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func DeleteProcDefNodeParam(ctx context.Context, procDefNodeId string) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "delete from proc_def_node_param where proc_def_node_id=?", Param: []interface{}{procDefNodeId}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

// GetProcDefNodeParamByNodeId 根据节点获取编排节点参数
func GetProcDefNodeParamByNodeId(ctx context.Context, nodeId string) (list []*models.ProcDefNodeParam, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def_node_param where proc_def_node_id = ?", nodeId).Find(&list)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func AddUseRoles(ctx context.Context, procDefId string, useRoles []string) (err error) {
	var list []*models.ProcDefPermission
	var permissionMap = make(map[string]bool)
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def_permission where proc_def_id = ? and permission = ?", procDefId, string(models.USE)).Find(&list)
	if err != nil {
		return
	}
	if len(list) > 0 {
		for _, permission := range list {
			permissionMap[permission.RoleName] = true
		}
	}

	for _, role := range useRoles {
		if !permissionMap[role] {
			_, err = db.MysqlEngine.Context(ctx).Exec("insert into proc_def_permission(id,proc_def_id,role_id,role_name,permission)values(?,?,?,?,?)", guid.CreateGuid(),
				procDefId, role, role, string(models.USE))
			if err != nil {
				return
			}
		}
	}
	return
}

func BatchAddProcDefPermission(ctx context.Context, procDefId string, permission models.PermissionToRole) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "delete from proc_def_permission where proc_def_id= ? ",
		Param: []interface{}{procDefId}})
	if len(permission.USE) > 0 {
		for _, roleName := range permission.USE {
			actions = append(actions, &db.ExecAction{Sql: "insert into proc_def_permission(id,proc_def_id,role_id,role_name,permission)values(" +
				"?,?,?,?,?)", Param: []interface{}{guid.CreateGuid(), procDefId, roleName, roleName, string(models.USE)}})
		}
	}
	if len(permission.MGMT) > 0 {
		for _, roleName := range permission.MGMT {
			actions = append(actions, &db.ExecAction{Sql: "insert into proc_def_permission(id,proc_def_id,role_id,role_name,permission)values(" +
				"?,?,?,?,?)", Param: []interface{}{guid.CreateGuid(), procDefId, roleName, roleName, string(models.MGMT)}})
		}
	}
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func insertProcDef(ctx context.Context, procDef *models.ProcDef) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "insert into  proc_def (id,`key`,name,root_entity,status,tags,for_plugin,scene," +
		"conflict_check,version,created_by,created_time,updated_by,updated_time) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{procDef.Id,
		procDef.Key, procDef.Name, procDef.RootEntity, procDef.Status, procDef.Tags, procDef.ForPlugin, procDef.Scene,
		procDef.ConflictCheck, procDef.Version, procDef.CreatedBy, procDef.CreatedTime.Format(models.DateTimeFormat), procDef.UpdatedBy, procDef.UpdatedTime.Format(models.DateTimeFormat)}})
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func transProcDefNodeUpdateConditionToSQL(procDefNode *models.ProcDefNode) (sql string, params []interface{}) {
	sql = "update proc_def_node set id = ?"
	params = append(params, procDefNode.Id)
	if procDefNode.ProcDefId != "" {
		sql = sql + ",proc_def_id=?"
		params = append(params, procDefNode.ProcDefId)
	}
	if procDefNode.NodeId != "" {
		sql = sql + ",node_id=?"
		params = append(params, procDefNode.NodeId)
	}
	if procDefNode.Name != "" {
		sql = sql + ",name=?"
		params = append(params, procDefNode.Name)
	}
	if procDefNode.Description != "" {
		sql = sql + ",description=?"
		params = append(params, procDefNode.Description)
	}
	if procDefNode.Status != "" {
		sql = sql + ",status=?"
		params = append(params, procDefNode.Status)
	}
	if procDefNode.NodeType != "" {
		sql = sql + ",node_type=?"
		params = append(params, procDefNode.NodeType)
	}
	if procDefNode.ServiceName != "" {
		sql = sql + ",service_name=?"
		params = append(params, procDefNode.ServiceName)
	}
	sql = sql + ",dynamic_bind=?"
	params = append(params, procDefNode.DynamicBind)
	if procDefNode.BindNodeId != "" {
		sql = sql + ",bind_node_id=?"
		params = append(params, procDefNode.BindNodeId)
	}
	sql = sql + ",allow_continue=?"
	params = append(params, procDefNode.AllowContinue)
	sql = sql + ",risk_check=?"
	params = append(params, procDefNode.RiskCheck)
	if procDefNode.RoutineExpression != "" {
		sql = sql + ",routine_expression=?"
		params = append(params, procDefNode.RoutineExpression)
	}
	if procDefNode.ContextParamNodes != "" {
		sql = sql + ",context_param_nodes=?"
		params = append(params, procDefNode.ContextParamNodes)
	}
	if procDefNode.Timeout != 0 {
		sql = sql + ",timeout=?"
		params = append(params, procDefNode.Timeout)
	}
	if procDefNode.TimeConfig != "" {
		sql = sql + ",time_config=?"
		params = append(params, procDefNode.TimeConfig)
	}
	if procDefNode.OrderedNo != 0 {
		sql = sql + ",ordered_no=?"
		params = append(params, procDefNode.OrderedNo)
	}
	if procDefNode.UiStyle != "" {
		sql = sql + ",ui_style=?"
		params = append(params, procDefNode.UiStyle)
	}
	if procDefNode.SubProcDefId != "" {
		sql = sql + ",sub_proc_def_id=?"
		params = append(params, procDefNode.SubProcDefId)
	}
	if procDefNode.UpdatedBy != "" {
		sql = sql + ",updated_by=?"
		params = append(params, procDefNode.UpdatedBy)
	}
	sql = sql + ",updated_time=?"
	params = append(params, procDefNode.UpdatedTime.Format(models.DateTimeFormat))
	sql = sql + " where id= ?"
	params = append(params, procDefNode.Id)
	return
}

func transProcDefNodeLinkUpdateConditionToSQL(procDefNodeLink *models.ProcDefNodeLink) (sql string, params []interface{}) {
	sql = "update proc_def_node_link set id=?"
	params = append(params, procDefNodeLink.Id)
	if procDefNodeLink.LinkId != "" {
		sql = sql + ",link_id=?"
		params = append(params, procDefNodeLink.LinkId)
	}
	if procDefNodeLink.Source != "" {
		sql = sql + ",source=?"
		params = append(params, procDefNodeLink.Source)
	}
	if procDefNodeLink.Target != "" {
		sql = sql + ",target=?"
		params = append(params, procDefNodeLink.Target)
	}
	if procDefNodeLink.Name != "" {
		sql = sql + ",name=?"
		params = append(params, procDefNodeLink.Name)
	}
	if procDefNodeLink.UiStyle != "" {
		sql = sql + ",ui_style=?"
		params = append(params, procDefNodeLink.UiStyle)
	}
	if procDefNodeLink.ProcDefId != "" {
		sql = sql + ",proc_def_id=?"
		params = append(params, procDefNodeLink.ProcDefId)
	}
	sql = sql + " where id= ?"
	params = append(params, procDefNodeLink.Id)
	return
}

func transProcDefConditionToSQL(param models.QueryProcessDefinitionParam) (where string, queryParam []interface{}) {
	where = "where 1 = 1 "
	if param.ProcDefId != "" {
		where = where + " and  id like '%" + param.ProcDefId + "%' "
	}
	if param.ProcDefName != "" {
		where = where + " and  name like '%" + param.ProcDefName + "%' "
	}
	if param.Status == string(models.Draft) || param.Status == string(models.Disabled) || param.Status == string(models.Deployed) {
		where = where + " and status = ?"
		queryParam = append(queryParam, param.Status)
	}
	if param.UpdatedTimeStart != "" && param.UpdatedTimeEnd != "" {
		where = where + " and updated_time >= ? and updated_time <= ?"
		queryParam = append(queryParam, []interface{}{param.UpdatedTimeStart, param.UpdatedTimeEnd}...)
	}
	if param.CreatedTimeStart != "" && param.CreatedTimeEnd != "" {
		where = where + " and created_time >= ? and created_time <= ?"
		queryParam = append(queryParam, []interface{}{param.CreatedTimeStart, param.CreatedTimeEnd}...)
	}
	if param.CreatedBy != "" {
		where = where + " and  created_by like '%" + param.CreatedBy + "%'"
	}
	if param.UpdatedBy != "" {
		where = where + " and updated_by like '%" + param.UpdatedBy + "%'"
	}
	if param.Scene != "" {
		where = where + " and scene like '%" + param.Scene + "%'"
	}
	if param.SubProc == "main" {
		where = where + " and sub_proc=0 "
	} else if param.SubProc == "sub" {
		where = where + " and sub_proc=1 "
	}
	if param.OnlyCollect {
		where = where + " and id in (select proc_def_id from proc_def_collect where user_id=?) "
		queryParam = append(queryParam, param.Operator)
	}
	if len(param.UserRoles) > 0 {
		if param.PermissionType == "" {
			param.PermissionType = "MGMT"
		}
		userRolesFilterSql, userRolesFilterParam := createListParams(param.UserRoles, "")
		where = where + " and  id in (select proc_def_id from proc_def_permission where role_name in (" + userRolesFilterSql + ") and permission = ?)"
		queryParam = append(queryParam, userRolesFilterParam...)
		queryParam = append(queryParam, param.PermissionType)
	}
	return
}

func convertArray2Map(roles []string) map[string]bool {
	hashMap := make(map[string]bool)
	for _, role := range roles {
		hashMap[role] = true
	}
	return hashMap
}

func GetProcDefParentList(ctx context.Context, procDefId string, pageInfo *models.PageInfo) (parentProcDefList []*models.ProcDefParentListItem, err error) {
	queryParam := []interface{}{procDefId}
	countResultMap, countErr := db.MysqlEngine.Context(ctx).QueryString("select count(1) as num from proc_def where id in (select proc_def_id from proc_def_node where sub_proc_def_id=?)", procDefId)
	if countErr != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, countErr)
		return
	}
	if pageInfo.TotalRows, err = strconv.Atoi(countResultMap[0]["num"]); err != nil {
		err = fmt.Errorf("count result:%v illegal", countResultMap)
		return
	}
	pageSql, pageParam := transPageInfoToSQL(*pageInfo)
	queryParam = append(queryParam, pageParam...)
	var procDefRows []*models.ProcDef
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def where id in (select proc_def_id from proc_def_node where sub_proc_def_id=?) order by created_time desc "+pageSql, queryParam...).Find(&procDefRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	parentProcDefList = []*models.ProcDefParentListItem{}
	for _, row := range procDefRows {
		parentProcDefList = append(parentProcDefList, &models.ProcDefParentListItem{
			Id:      row.Id,
			Key:     row.Key,
			Name:    row.Name,
			Version: row.Version,
			Status:  row.Status,
		})
	}
	return
}

func AddProcDefCollect(ctx context.Context, procDefId, operator string) (err error) {
	_, err = db.MysqlEngine.Context(ctx).Exec("insert into proc_def_collect(id,proc_def_id,user_id,created_time) values (?,?,?,?)", "pdc_"+guid.CreateGuid(), procDefId, operator, time.Now())
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func DelProcDefCollect(ctx context.Context, procDefId, operator string) (err error) {
	_, err = db.MysqlEngine.Context(ctx).Exec("delete from proc_def_collect where proc_def_id=? and user_id=?", procDefId, operator)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func getUserProcDefCollectMap(ctx context.Context, userId string) (procDefMap map[string]string, err error) {
	var collectRows []*models.ProcDefCollect
	err = db.MysqlEngine.Context(ctx).SQL("select proc_def_id from proc_def_collect where user_id=?", userId).Find(&collectRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	procDefMap = make(map[string]string)
	for _, row := range collectRows {
		procDefMap[row.ProcDefId] = row.ProcDefId
	}
	return
}
