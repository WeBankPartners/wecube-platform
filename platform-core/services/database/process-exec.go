package database

import (
	"context"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"strings"
	"time"
)

func ProcDefList(ctx context.Context, includeDraft, permission, tag string, userRoles []string) (result []*models.ProcDefListObj, err error) {
	var procDefRows []*models.ProcDef
	baseSql := "select * from proc_def"
	var filterSqlList []string
	var filterParams []interface{}
	if includeDraft == "0" {
		filterSqlList = append(filterSqlList, "status=?")
		filterParams = append(filterParams, models.Deployed)
	} else {
		filterSqlList = append(filterSqlList, "status in (?,?)")
		filterParams = append(filterParams, models.Deployed, models.Draft)
	}
	if permission == models.PermissionTypeMGMT || permission == models.PermissionTypeUSE {
		filterSqlList = append(filterSqlList, "id in (select proc_def_id from proc_def_permission where permission=? and role_id in ('"+strings.Join(userRoles, "','")+"'))")
		filterParams = append(filterParams, permission)
	}
	if tag != "" {
		filterSqlList = append(filterSqlList, "tags=?")
		filterParams = append(filterParams, tag)
	}
	if len(filterSqlList) > 0 {
		baseSql += " where " + strings.Join(filterSqlList, " and ")
	}
	baseSql += " order by created_time desc"
	err = db.MysqlEngine.Context(ctx).SQL(baseSql, filterParams...).Find(&procDefRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	for _, row := range procDefRows {
		resultObj := models.ProcDefListObj{}
		resultObj.Parse(row)
		result = append(result, &resultObj)
	}
	return
}

func ProcDefOutline(ctx context.Context, procDefId string) (result *models.ProcDefListObj, err error) {
	var procDefRows []*models.ProcDef
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def where id=?", procDefId).Find(&procDefRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(procDefRows) == 0 {
		err = exterror.New().DatabaseQueryEmptyError
		return
	}
	result = &models.ProcDefListObj{}
	result.Parse(procDefRows[0])
	var procDefNodes []*models.ProcDefNode
	err = db.MysqlEngine.Context(ctx).SQL("select id,node_id,proc_def_id,name,description,status,node_type,service_name,dynamic_bind,bind_node_id,risk_check,routine_expression,context_param_nodes,timeout,ordered_no,time_config from proc_def_node where proc_def_id=? order by ordered_no", procDefId).Find(&procDefNodes)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	var procDefLinks []*models.ProcDefNodeLink
	err = db.MysqlEngine.Context(ctx).SQL("select id,link_id,proc_def_id,source,target,name from proc_def_node_link where proc_def_id=?", procDefId).Find(&procDefLinks)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	parentMap, childrenMap := make(map[string][]string), make(map[string][]string)
	for _, link := range procDefLinks {
		if v, b := childrenMap[link.Source]; b {
			childrenMap[link.Source] = append(v, link.Target)
		} else {
			childrenMap[link.Source] = []string{link.Target}
		}
		if v, b := parentMap[link.Target]; b {
			parentMap[link.Target] = append(v, link.Source)
		} else {
			parentMap[link.Target] = []string{link.Source}
		}
	}
	orderIndex := 1
	for _, node := range procDefNodes {
		nodeObj := models.ProcDefFlowNode{
			NodeId:            node.Id,
			NodeDefId:         node.Id,
			NodeName:          node.Name,
			NodeType:          node.NodeType,
			ProcDefId:         result.ProcDefId,
			ProcDefKey:        result.ProcDefKey,
			RoutineExpression: node.RoutineExpression,
			ServiceId:         node.ServiceName,
			Status:            node.Status,
			Description:       node.Description,
			DynamicBind:       "N",
			PreviousNodeIds:   []string{},
			SucceedingNodeIds: []string{},
		}
		if node.NodeType == string(models.ProcDefNodeTypeHuman) || node.NodeType == string(models.ProcDefNodeTypeAutomatic) || node.NodeType == string(models.ProcDefNodeTypeData) {
			nodeObj.OrderedNo = fmt.Sprintf("%d", orderIndex)
			orderIndex += 1
		}
		if node.DynamicBind {
			nodeObj.DynamicBind = "Y"
		}
		if parentList, ok := parentMap[node.Id]; ok {
			nodeObj.PreviousNodeIds = parentList
		}
		if childrenList, ok := childrenMap[node.Id]; ok {
			nodeObj.SucceedingNodeIds = childrenList
		}
		result.FlowNodes = append(result.FlowNodes, &nodeObj)
	}
	return
}

func GetSimpleProcDefRow(ctx context.Context, procDefId string) (result *models.ProcDef, err error) {
	var procDefRows []*models.ProcDef
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def where id=?", procDefId).Find(&procDefRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(procDefRows) == 0 {
		err = exterror.New().DatabaseQueryEmptyError
		return
	}
	result = procDefRows[0]
	return
}

func CreateProcPreview(ctx context.Context, previewRows []*models.ProcDataPreview, graphRows []*models.ProcInsGraphNode) (err error) {
	var actions []*db.ExecAction
	for _, v := range previewRows {
		actions = append(actions, &db.ExecAction{Sql: "insert into proc_data_preview(proc_def_id,proc_session_id,proc_def_node_id,entity_data_id,entity_data_name,entity_type_id,ordered_no,bind_type,full_data_id,is_bound,created_by,created_time) values (?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
			v.ProcDefId, v.ProcSessionId, v.ProcDefNodeId, v.EntityDataId, v.EntityDataName, v.EntityTypeId, v.OrderedNo, v.BindType, v.FullDataId, v.IsBound, v.CreatedBy, v.CreatedTime,
		}})
	}
	for _, v := range graphRows {
		actions = append(actions, &db.ExecAction{Sql: "insert into proc_ins_graph_node(proc_session_id,proc_ins_id,data_id,display_name,entity_name,graph_node_id,pkg_name,prev_ids,succ_ids,full_data_id,created_by,created_time) values (?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
			v.ProcSessionId, v.ProcInsId, v.DataId, v.DisplayName, v.EntityName, v.GraphNodeId, v.PkgName, v.PrevIds, v.SuccIds, v.FullDataId, v.CreatedBy, v.CreatedTime,
		}})
	}
	if err = db.Transaction(actions, ctx); err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func ProcInsTaskNodeBindings(ctx context.Context, sessionId, taskNodeId string) (result []*models.TaskNodeBindingObj, err error) {
	var previewRows []*models.ProcDataPreview
	if taskNodeId == "" {
		err = db.MysqlEngine.Context(ctx).SQL("select proc_def_node_id,entity_data_id,entity_data_name,entity_type_id,ordered_no,bind_type,is_bound from proc_data_preview where proc_session_id=?", sessionId).Find(&previewRows)
	} else {
		err = db.MysqlEngine.Context(ctx).SQL("select proc_def_node_id,entity_data_id,entity_data_name,entity_type_id,ordered_no,bind_type,is_bound from proc_data_preview where proc_session_id=? and proc_def_node_id=?", sessionId, taskNodeId).Find(&previewRows)
	}
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	result = []*models.TaskNodeBindingObj{}
	for _, row := range previewRows {
		if row.ProcDefNodeId == "" {
			continue
		}
		tmpBindingObj := models.TaskNodeBindingObj{
			Bound:        "Y",
			EntityDataId: row.EntityDataId,
			EntityTypeId: row.EntityTypeId,
			NodeDefId:    row.ProcDefNodeId,
			OrderedNo:    row.OrderedNo,
		}
		if !row.IsBound {
			tmpBindingObj.Bound = "N"
		}
		result = append(result, &tmpBindingObj)
	}
	return
}

func UpdateProcNodeBindingData(ctx context.Context, param []*models.TaskNodeBindingObj, sessionId, taskNodeId, operator string) (err error) {
	var previewRows []*models.ProcDataPreview
	err = db.MysqlEngine.Context(ctx).SQL("select id,proc_def_node_id,entity_data_id,entity_data_name,entity_type_id,ordered_no,bind_type,is_bound from proc_data_preview where proc_session_id=? and proc_def_node_id=?", sessionId, taskNodeId).Find(&previewRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	var actions []*db.ExecAction
	nowTime := time.Now()
	for _, v := range previewRows {
		boundFlag := false
		for _, input := range param {
			if input.EntityTypeId == v.EntityTypeId && input.EntityDataId == v.EntityDataId {
				if input.Bound == "Y" {
					boundFlag = true
					break
				}
			}
		}
		actions = append(actions, &db.ExecAction{Sql: "update proc_data_preview set is_bound=?,updated_by=?,updated_time=? where id=?", Param: []interface{}{boundFlag, operator, nowTime, v.Id}})
	}
	if err = db.Transaction(actions, ctx); err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}
