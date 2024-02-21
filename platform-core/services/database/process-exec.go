package database

import (
	"context"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/tools"
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

func ProcDefNodeList(ctx context.Context, procDefId string) (nodes []*models.ProcNodeObj, err error) {
	var nodeDefRows []*models.ProcDefNode
	err = db.MysqlEngine.Context(ctx).SQL("select id,node_id,proc_def_id,name,description,status,node_type,service_name,dynamic_bind,bind_node_id,risk_check,routine_expression,context_param_nodes,timeout,ordered_no from proc_def_node where proc_def_id=? order by ordered_no", procDefId).Find(&nodeDefRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	for _, row := range nodeDefRows {
		nodeObj := models.ProcNodeObj{
			NodeId:      row.Id,
			NodeName:    row.Name,
			NodeType:    row.NodeType,
			NodeDefId:   row.Id,
			RoutineExp:  row.RoutineExpression,
			ServiceId:   row.ServiceName,
			ServiceName: row.ServiceName,
			OrderedNo:   fmt.Sprintf("%d", row.OrderedNo),
			DynamicBind: "N",
		}
		if row.DynamicBind {
			nodeObj.DynamicBind = "Y"
		}
		if row.NodeType == "automatic" {
			nodeObj.TaskCategory = "SSTN"
		} else if row.NodeType == "human" {
			nodeObj.TaskCategory = "SUTN"
		} else if row.NodeType == "data" {
			nodeObj.TaskCategory = "SDTN"
		}
		nodes = append(nodes, &nodeObj)
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

func CreateProcInstance(ctx context.Context, procStartParam *models.ProcInsStartParam, operator string) (procInsId string, workflowRow *models.ProcRunWorkflow, workNodes []*models.ProcRunNode, workLinks []*models.ProcRunLink, err error) {
	procInsId = "pins_" + guid.CreateGuid()
	procDefObj, getProcDefErr := GetSimpleProcDefRow(ctx, procStartParam.ProcDefId)
	if getProcDefErr != nil {
		err = getProcDefErr
		return
	}
	var actions []*db.ExecAction
	nowTime := time.Now()
	actions = append(actions, &db.ExecAction{Sql: "insert into proc_ins(id,proc_def_id,proc_def_key,proc_def_name,status,created_by,created_time,updated_by,updated_time) values (?,?,?,?,?,?,?,?,?)", Param: []interface{}{
		procInsId, procDefObj.Id, procDefObj.Key, procDefObj.Name, "ready", operator, nowTime, operator, nowTime,
	}})
	workflowRow = &models.ProcRunWorkflow{Id: "wf_" + guid.CreateGuid(), ProcInsId: procInsId, Name: procDefObj.Name, Status: "ready", CreatedTime: nowTime}
	actions = append(actions, &db.ExecAction{Sql: "insert into proc_run_workflow(id,proc_ins_id,name,status,created_time) values (?,?,?,?,?)", Param: []interface{}{
		workflowRow.Id, workflowRow.ProcInsId, workflowRow.Name, workflowRow.Status, workflowRow.CreatedTime,
	}})
	var procDefNodes []*models.ProcDefNode
	err = db.MysqlEngine.Context(ctx).SQL("select id,node_id,proc_def_id,name,description,status,node_type,service_name,dynamic_bind,bind_node_id,risk_check,routine_expression,context_param_nodes,timeout,ordered_no,time_config from proc_def_node where proc_def_id=? order by ordered_no", procStartParam.ProcDefId).Find(&procDefNodes)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	var procDefLinks []*models.ProcDefNodeLink
	err = db.MysqlEngine.Context(ctx).SQL("select id,link_id,proc_def_id,source,target,name from proc_def_node_link where proc_def_id=?", procStartParam.ProcDefId).Find(&procDefLinks)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	previewRows := []*models.ProcDataPreview{}
	if procStartParam.ProcessSessionId != "" {
		err = db.MysqlEngine.Context(ctx).SQL("select * from proc_data_preview where proc_session_id=?", procStartParam.ProcessSessionId).Find(&previewRows)
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}
	}
	workNodeIdMap := make(map[string]string)
	for _, node := range procDefNodes {
		tmpProcInsNodeId := "pins_node_" + guid.CreateGuid()
		actions = append(actions, &db.ExecAction{Sql: "insert into proc_ins_node(id,proc_ins_id,proc_def_node_id,name,node_type,status,ordered_no,created_by,created_time) values (?,?,?,?,?,?,?,?,?)", Param: []interface{}{
			tmpProcInsNodeId, procInsId, node.Id, node.Name, node.NodeType, "ready", node.OrderedNo, operator, nowTime,
		}})
		workNodeObj := models.ProcRunNode{Id: "wn_" + guid.CreateGuid(), WorkflowId: workflowRow.Id, ProcInsNodeId: tmpProcInsNodeId, Name: node.Name, JobType: node.NodeType, Status: "ready", Timeout: node.Timeout, CreatedTime: nowTime}
		actions = append(actions, &db.ExecAction{Sql: "insert into proc_run_node(id,workflow_id,proc_ins_node_id,name,job_type,status,timeout,created_time) values (?,?,?,?,?,?,?,?)", Param: []interface{}{
			workNodeObj.Id, workNodeObj.WorkflowId, workNodeObj.ProcInsNodeId, workNodeObj.Name, workNodeObj.JobType, workNodeObj.Status, workNodeObj.Timeout, workNodeObj.CreatedTime,
		}})
		workNodeIdMap[node.Id] = workNodeObj.Id
		workNodes = append(workNodes, &workNodeObj)
		// data bind
		for _, row := range previewRows {
			if row.ProcDefNodeId == node.Id {
				actions = append(actions, &db.ExecAction{Sql: "insert into proc_data_binding(id,proc_def_id,proc_ins_id,proc_def_node_id,proc_ins_node_id,entity_id,entity_data_id,entity_data_name,entity_type_id,bind_flag,bind_type,full_data_id,created_by,created_time) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
					fmt.Sprintf("p_bind_%d", row.Id), procDefObj.Id, procInsId, node.Id, tmpProcInsNodeId, row.EntityDataId, row.EntityDataId, row.EntityDataName, row.EntityTypeId, row.IsBound, row.BindType, row.FullDataId, operator, nowTime,
				}})
			}
		}
	}
	for _, link := range procDefLinks {
		workLinkObj := models.ProcRunLink{Id: "wl_" + guid.CreateGuid(), WorkflowId: workflowRow.Id, ProcDefLinkId: link.Id, Name: link.Name, Source: workNodeIdMap[link.Source], Target: workNodeIdMap[link.Target]}
		actions = append(actions, &db.ExecAction{Sql: "insert into proc_run_link(id,workflow_id,proc_def_link_id,name,source,target) values (?,?,?,?,?,?)", Param: []interface{}{
			workLinkObj.Id, workLinkObj.WorkflowId, workLinkObj.ProcDefLinkId, workLinkObj.Name, workLinkObj.Source, workLinkObj.Target,
		}})
		workLinks = append(workLinks, &workLinkObj)
	}
	if err = db.Transaction(actions, ctx); err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func GetProcInstance(ctx context.Context, procInsId string) (result *models.ProcInsDetail, err error) {

	return
}

func GetProcExecNodeData(ctx context.Context, procRunNodeId string) (procInsNode *models.ProcInsNode, procDefNode *models.ProcDefNode, procDefNodeParams []*models.ProcDefNodeParam, dataBinding []*models.ProcDataBinding, err error) {
	var procInsNodeRows []*models.ProcInsNode
	err = db.MysqlEngine.Context(ctx).SQL("select id,proc_ins_id,proc_def_node_id,name,node_type,status from proc_ins_node where id in (select proc_ins_node_id from proc_run_node where id=?)", procRunNodeId).Find(&procInsNodeRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(procInsNodeRows) == 0 {
		err = exterror.Catch(exterror.New().DatabaseQueryEmptyError, fmt.Errorf("proc_ins_node"))
		return
	}
	procInsNode = procInsNodeRows[0]
	var procDefNodeRows []*models.ProcDefNode
	err = db.MysqlEngine.Context(ctx).SQL("select id,node_id,proc_def_id,name,node_type,service_name,dynamic_bind,bind_node_id,risk_check,routine_expression,context_param_nodes,timeout from proc_def_node where id=?", procInsNode.ProcDefNodeId).Find(&procDefNodeRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(procDefNodeRows) == 0 {
		err = exterror.Catch(exterror.New().DatabaseQueryEmptyError, fmt.Errorf("proc_def_node"))
		return
	}
	procDefNode = procDefNodeRows[0]
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def_node_param where proc_def_node_id=?", procDefNode.Id).Find(&procDefNodeParams)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_data_binding where proc_ins_node_id=?", procInsNode.Id).Find(&dataBinding)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func GetDynamicBindNodeData(ctx context.Context, procInsNodeId, procDefId, bindNodeId string) (dataBinding []*models.ProcDataBinding, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_data_binding where proc_ins_node_id=? and proc_def_node_id in (select id from proc_def_node where proc_def_id=? and node_id=?)", procInsNodeId, procDefId, bindNodeId).Find(&dataBinding)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
	}
	return
}

func UpdateProcInsNodeData(ctx context.Context, procInsId, status, errorMsg, riskCheckResult string) (err error) {
	if riskCheckResult != "" {
		_, err = db.MysqlEngine.Context(ctx).Exec("update proc_ins_node set risk_check_result=? where id=?", riskCheckResult, procInsId)
	}
	if status != "" {
		_, err = db.MysqlEngine.Context(ctx).Exec("update proc_ins_node set status=? where id=?", status, procInsId)
	}
	if errorMsg != "" {
		_, err = db.MysqlEngine.Context(ctx).Exec("update proc_ins_node set error_msg=? where id=?", errorMsg, procInsId)
	}
	return
}

func GetLastEnablePluginInterface(ctx context.Context, serviceName string) (pluginInterface *models.PluginConfigInterfaces, err error) {
	var interfaceRows []*models.PluginInterfaceWithVer
	err = db.MysqlEngine.Context(ctx).SQL("select t1.*,t3.`version` from plugin_config_interfaces t1 left join plugin_configs t2 on t1.plugin_config_id=t2.id left join plugin_packages t3 on t2.plugin_package_id=t3.id where t1.service_name=? and t2.status='ENABLED'", serviceName).Find(&interfaceRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(interfaceRows) == 0 {
		err = fmt.Errorf("can not find enable plugin config interface with name:%s ", serviceName)
		return
	}
	interfaceObj := &models.PluginInterfaceWithVer{}
	if len(interfaceRows) == 1 {
		interfaceObj = interfaceRows[0]
	} else {
		maxVersion := interfaceRows[0].Version
		for _, row := range interfaceRows {
			if tools.CompareVersion(row.Version, maxVersion) {
				maxVersion = row.Version
			}
		}
		for _, row := range interfaceRows {
			if row.Version == maxVersion {
				interfaceObj = row
			}
		}
	}
	pluginInterface = &models.PluginConfigInterfaces{
		Id:                 interfaceObj.Id,
		PluginConfigId:     interfaceObj.PluginConfigId,
		Action:             interfaceObj.Action,
		ServiceName:        interfaceObj.ServiceName,
		ServiceDisplayName: interfaceObj.ServiceDisplayName,
		Path:               interfaceObj.Path,
		HttpMethod:         interfaceObj.HttpMethod,
		IsAsyncProcessing:  interfaceObj.IsAsyncProcessing,
		Type:               interfaceObj.Type,
		FilterRule:         interfaceObj.FilterRule,
		Description:        interfaceObj.Description,
	}
	var interfaceParamRows []*models.PluginConfigInterfaceParameters
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_config_interface_parameters where plugin_config_interface_id=?", interfaceObj.Id).Find(&interfaceParamRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	for _, row := range interfaceParamRows {
		if row.Type == "INPUT" {
			pluginInterface.InputParameters = append(pluginInterface.InputParameters, row)
		} else {
			pluginInterface.OutputParameters = append(pluginInterface.OutputParameters, row)
		}
	}
	var pluginConfigRows []*models.PluginConfigs
	err = db.MysqlEngine.Context(ctx).SQL("select * from plugin_configs where id=?", pluginInterface.PluginConfigId).Find(&pluginConfigRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(pluginConfigRows) > 0 {
		pluginInterface.PluginConfig = pluginConfigRows[0]
	}
	return
}

func RecordProcCallReq(ctx context.Context, param *models.ProcInsNodeReq, inputFlag bool) (err error) {
	nowTime := time.Now()
	var actions []*db.ExecAction
	if inputFlag {
		actions = append(actions, &db.ExecAction{Sql: "insert into proc_ins_node_req(id,proc_ins_node_id,req_url,req_data_amount,created_time) values (?,?,?,?,?)", Param: []interface{}{
			param.Id, param.ProcInsNodeId, param.ReqUrl, param.ReqDataAmount, nowTime,
		}})
		for _, v := range param.Params {
			if v.FromType == "input" {
				actions = append(actions, &db.ExecAction{Sql: "insert into proc_ins_node_req_param(req_id,data_index,from_type,name,data_type,data_value,entity_data_id,entity_type_id,is_sensitive,full_data_id,multiple,param_def_id,mapping_type,callback_id,created_time) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
					v.ReqId, v.DataIndex, v.FromType, v.Name, v.DataType, v.DataValue, v.EntityDataId, v.EntityTypeId, v.IsSensitive, v.FullDataId, v.Multiple, v.ParamDefId, v.MappingType, v.CallbackId, nowTime,
				}})
			}
		}
	} else {
		actions = append(actions, &db.ExecAction{Sql: "update proc_ins_node_req set is_completed=1,error_msg=?,updated_time=? where id=?", Param: []interface{}{param.ErrorMsg, nowTime, param.Id}})
		for _, v := range param.Params {
			if v.FromType == "output" {
				actions = append(actions, &db.ExecAction{Sql: "insert into proc_ins_node_req_param(req_id,data_index,from_type,name,data_type,data_value,entity_data_id,entity_type_id,is_sensitive,full_data_id,multiple,param_def_id,mapping_type,callback_id,created_time) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
					v.ReqId, v.DataIndex, v.FromType, v.Name, v.DataType, v.DataValue, v.EntityDataId, v.EntityTypeId, v.IsSensitive, v.FullDataId, v.Multiple, v.ParamDefId, v.MappingType, v.CallbackId, nowTime,
				}})
			}
		}
	}
	err = db.Transaction(actions, ctx)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}
