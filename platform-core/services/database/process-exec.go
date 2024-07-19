package database

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/tools"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"strings"
	"time"
)

// ProcDefList
// subProc -> all(全部) | main(主编排) | sub(子编排)
func ProcDefList(ctx context.Context, includeDraft, permission, tag, plugin, subProc, operator string, userRoles []string) (result []*models.ProcDefListObj, err error) {
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
	if plugin != "" {
		filterSqlList = append(filterSqlList, "for_plugin like ?")
		filterParams = append(filterParams, fmt.Sprintf("%%%s%%", plugin))
	}
	if subProc == "main" {
		filterSqlList = append(filterSqlList, "sub_proc=0")
	} else if subProc == "sub" {
		filterSqlList = append(filterSqlList, "sub_proc=1")
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
	collectProcDefMap := make(map[string]string)
	if collectProcDefMap, err = getUserProcDefCollectMap(ctx, operator); err != nil {
		return
	}
	for _, row := range procDefRows {
		resultObj := models.ProcDefListObj{}
		resultObj.Parse(row)
		if _, ok := collectProcDefMap[row.Id]; ok {
			resultObj.Collected = true
		}
		result = append(result, &resultObj)
	}
	return
}

func PublicProcDefNodeList(ctx context.Context, procDefId string) (nodes []*models.ProcNodeObj, err error) {
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
			NodeDefType: row.NodeType,
			NodeDefId:   row.NodeId,
			RoutineExp:  row.RoutineExpression,
			ServiceId:   row.ServiceName,
			ServiceName: row.ServiceName,
			OrderedNo:   fmt.Sprintf("%d", row.OrderedNo),
			DynamicBind: "N",
		}
		if row.DynamicBind > 0 {
			nodeObj.DynamicBind = "Y"
		}
		if row.NodeType == "automatic" {
			nodeObj.TaskCategory = "SSTN"
			nodeObj.NodeType = "subProcess"
		} else if row.NodeType == "human" {
			nodeObj.TaskCategory = "SUTN"
			nodeObj.NodeType = "subProcess"
		} else if row.NodeType == "data" {
			nodeObj.TaskCategory = "SDTN"
			nodeObj.NodeType = "subProcess"
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
	err = db.MysqlEngine.Context(ctx).SQL("select id,node_id,proc_def_id,name,description,status,node_type,service_name,dynamic_bind,bind_node_id,risk_check,routine_expression,context_param_nodes,timeout,ordered_no,time_config,sub_proc_def_id from proc_def_node where proc_def_id=? order by ordered_no", procDefId).Find(&procDefNodes)
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
	result.NodeLinks = procDefLinks
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
			SubProcDefId:      node.SubProcDefId,
		}
		if node.NodeType == string(models.ProcDefNodeTypeHuman) || node.NodeType == string(models.ProcDefNodeTypeAutomatic) || node.NodeType == string(models.ProcDefNodeTypeData) || node.NodeType == models.JobSubProcType {
			nodeObj.OrderedNo = fmt.Sprintf("%d", orderIndex)
			orderIndex += 1
		}
		if node.DynamicBind > 0 {
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

func GetSimpleProcInsRow(ctx context.Context, procInsId string) (result *models.ProcIns, err error) {
	var procInsRows []*models.ProcIns
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_ins where id=?", procInsId).Find(&procInsRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(procInsRows) == 0 {
		err = exterror.New().DatabaseQueryEmptyError
		return
	}
	result = procInsRows[0]
	return
}

func CreateProcPreview(ctx context.Context, previewRows []*models.ProcDataPreview, graphRows []*models.ProcInsGraphNode) (err error) {
	var actions []*db.ExecAction
	for _, v := range previewRows {
		actions = append(actions, &db.ExecAction{Sql: "insert into proc_data_preview(proc_def_id,proc_session_id,proc_def_node_id,entity_data_id,entity_data_name,entity_type_id,ordered_no,bind_type,full_data_id,is_bound,created_by,created_time,sub_session_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
			v.ProcDefId, v.ProcSessionId, v.ProcDefNodeId, v.EntityDataId, v.EntityDataName, v.EntityTypeId, v.OrderedNo, v.BindType, v.FullDataId, v.IsBound, v.CreatedBy, v.CreatedTime, v.SubSessionId,
		}})
	}
	for _, v := range graphRows {
		actions = append(actions, &db.ExecAction{Sql: "insert into proc_ins_graph_node(proc_session_id,proc_ins_id,data_id,display_name,entity_name,graph_node_id,pkg_name,prev_ids,succ_ids,full_data_id,created_by,created_time) values (?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
			v.ProcSessionId, v.ProcInsId, v.DataId, v.DisplayName, v.EntityName, v.GraphNodeId, v.PkgName, v.PrevIds, v.SuccIds, v.FullDataId, v.CreatedBy, v.CreatedTime,
		}})
	}
	if err = db.Transaction(actions, ctx); err != nil {
		log.Logger.Error("CreateProcPreview fail", log.Error(err))
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func ProcInsTaskNodeBindings(ctx context.Context, sessionId, taskNodeId string) (result []*models.TaskNodeBindingObj, err error) {
	var previewRows []*models.ProcDataPreview
	nodeBindDataMap := make(map[string][]*models.ProcDataBinding)
	ignoreNodeBindMap := make(map[string]*models.ProcInsNode)
	if taskNodeId == "" {
		var procInsRows []*models.ProcIns
		err = db.MysqlEngine.Context(ctx).SQL("select id,status from proc_ins where proc_session_id=?", sessionId).Find(&procInsRows)
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}
		if len(procInsRows) > 0 {
			var procInsNodeRows []*models.ProcInsNode
			var procDataBindRows []*models.ProcDataBinding
			procInsStatus := procInsRows[0].Status
			err = db.MysqlEngine.Context(ctx).SQL("select id,proc_def_node_id,proc_ins_node_id,entity_data_id,entity_data_name,bind_flag from proc_data_binding where proc_ins_id=?", procInsRows[0].Id).Find(&procDataBindRows)
			if err != nil {
				err = exterror.Catch(exterror.New().DatabaseQueryError, err)
				return
			}
			notStartNodeMap := make(map[string]*models.ProcInsNode)
			err = db.MysqlEngine.Context(ctx).SQL("select id,proc_def_node_id,status from proc_ins_node where proc_ins_id=?", procInsRows[0].Id).Find(&procInsNodeRows)
			if err != nil {
				err = exterror.Catch(exterror.New().DatabaseQueryError, err)
				return
			}
			if procInsStatus == models.JobStatusRunning || procInsStatus == models.WorkflowStatusStop {
				for _, node := range procInsNodeRows {
					if node.Status == models.JobStatusReady {
						notStartNodeMap[node.ProcDefNodeId] = node
					}
				}
			} else if procInsStatus == models.JobStatusSuccess || procInsStatus == models.JobStatusFail || procInsStatus == models.JobStatusKill {
				for _, node := range procInsNodeRows {
					if node.Status == models.JobStatusReady {
						ignoreNodeBindMap[node.ProcDefNodeId] = node
					}
				}
			}
			for _, bindRow := range procDataBindRows {
				if !bindRow.BindFlag {
					continue
				}
				if _, ok := notStartNodeMap[bindRow.ProcDefNodeId]; ok {
					continue
				}
				if existList, ok := nodeBindDataMap[bindRow.ProcDefNodeId]; ok {
					nodeBindDataMap[bindRow.ProcDefNodeId] = append(existList, bindRow)
				} else {
					nodeBindDataMap[bindRow.ProcDefNodeId] = []*models.ProcDataBinding{bindRow}
				}
			}
			//log.Logger.Debug("nodeBinding", log.JsonObj("notStartNodeMap", notStartNodeMap), log.JsonObj("nodeBindDataMap", nodeBindDataMap))
		}
		err = db.MysqlEngine.Context(ctx).SQL("select * from proc_data_preview where proc_session_id=?", sessionId).Find(&previewRows)
	} else {
		err = db.MysqlEngine.Context(ctx).SQL("select * from proc_data_preview where proc_session_id=? and proc_def_node_id=?", sessionId, taskNodeId).Find(&previewRows)
	}
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	var subProcDefNodeList []string
	for _, row := range previewRows {
		if row.SubSessionId != "" {
			subProcDefNodeList = append(subProcDefNodeList, row.ProcDefNodeId)
		}
	}
	subProcDefMap := make(map[string]string)
	if len(subProcDefNodeList) > 0 {
		var procDefRows []*models.ProcDefNode
		filterSql, filterParam := db.CreateListParams(subProcDefNodeList, "")
		err = db.MysqlEngine.Context(ctx).SQL("select id,sub_proc_def_id from proc_def_node where id in ("+filterSql+")", filterParam...).Find(&procDefRows)
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}
		for _, row := range procDefRows {
			subProcDefMap[row.Id] = row.SubProcDefId
		}
	}
	result = []*models.TaskNodeBindingObj{}
	for _, row := range previewRows {
		if row.ProcDefNodeId == "" {
			continue
		}
		if _, ignoreFlag := ignoreNodeBindMap[row.ProcDefNodeId]; ignoreFlag {
			continue
		}
		if nodeDataRows, ok := nodeBindDataMap[row.ProcDefNodeId]; ok {
			passFlag := true
			for _, v := range nodeDataRows {
				if v.EntityDataId == row.EntityDataId {
					passFlag = false
					break
				}
			}
			if passFlag {
				continue
			}
		}
		tmpBindingObj := models.TaskNodeBindingObj{
			Bound:               "Y",
			EntityDataId:        row.EntityDataId,
			EntityTypeId:        row.EntityTypeId,
			NodeDefId:           row.ProcDefNodeId,
			OrderedNo:           row.OrderedNo,
			SubPreviewSessionId: row.SubSessionId,
			SubProcDefId:        subProcDefMap[row.ProcDefNodeId],
		}
		if !row.IsBound {
			tmpBindingObj.Bound = "N"
		}
		result = append(result, &tmpBindingObj)
	}
	return
}

func getRecurDynamicBindData(ctx context.Context, procInsId, procDefId, procDefNodeId string) (dataBinds []*models.ProcDataBinding, err error) {
	var insNodeRows []*models.ProcInsNode
	err = db.MysqlEngine.Context(ctx).SQL("select id,proc_def_node_id,name,node_type,status from proc_ins_node where proc_ins_id=? and proc_def_node_id in (select id from proc_def_node where proc_def_id=? and node_id=?)", procInsId, procDefId, procDefNodeId).Find(&insNodeRows)
	if err != nil {
		err = fmt.Errorf("get recursive dynamic bind data fail,procDefNodeId:%s ,detail:%s ", procDefNodeId, err.Error())
		return
	}
	if len(insNodeRows) == 0 {
		err = fmt.Errorf("can not find recursive dynamic bind node:%s ", procDefNodeId)
		return
	}
	insNodeObj := insNodeRows[0]
	if insNodeObj.Status == models.JobStatusRunning || insNodeObj.Status == models.JobStatusSuccess {
		dataBinds, err = GetDynamicBindNodeData(ctx, procInsId, procDefId, procDefNodeId)
		return
	}
	procDefNodeObj, getDefNodeErr := GetSimpleProcDefNode(ctx, insNodeObj.ProcDefNodeId)
	if getDefNodeErr != nil {
		err = getDefNodeErr
		return
	}
	if procDefNodeObj.DynamicBind == 1 {
		dataBinds, err = getRecurDynamicBindData(ctx, procInsId, procDefId, procDefNodeObj.BindNodeId)
	} else {
		dataBinds, err = GetDynamicBindNodeData(ctx, procInsId, procDefId, procDefNodeId)
	}
	return
}

func GetInstanceTaskNodeBindings(ctx context.Context, procInsId, procInsNodeId string) (result []*models.TaskNodeBindingObj, err error) {
	var dataBindingRows []*models.ProcDataBinding
	if procInsNodeId != "" {
		procInsNodeObj, getInsNodeErr := GetSimpleProcInsNode(ctx, procInsNodeId, "")
		if getInsNodeErr != nil {
			err = getInsNodeErr
			return
		}
		procDefNodeObj, getDefNodeErr := GetSimpleProcDefNode(ctx, procInsNodeObj.ProcDefNodeId)
		if getDefNodeErr != nil {
			err = getDefNodeErr
			return
		}
		if procDefNodeObj.DynamicBind == 1 && procInsNodeObj.Status != models.JobStatusRunning {
			dataBindingRows, err = getRecurDynamicBindData(ctx, procInsId, procDefNodeObj.ProcDefId, procDefNodeObj.BindNodeId)
			if err != nil {
				return
			}
		} else {
			err = db.MysqlEngine.Context(ctx).SQL("select * from proc_data_binding where proc_ins_id=? and proc_ins_node_id=?", procInsId, procInsNodeId).Find(&dataBindingRows)
		}
	} else {
		err = db.MysqlEngine.Context(ctx).SQL("select * from proc_data_binding where proc_ins_id=?", procInsId).Find(&dataBindingRows)
	}
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	//if procDefNodeObj.DynamicBind && procInsNodeObj.Status != models.JobStatusRunning {
	//	dataBindingRows, err = getRecurDynamicBindData(ctx, procInsId, procDefNodeObj.ProcDefId, procDefNodeObj.BindNodeId)
	//} else {
	//	if procInsNodeId != "" {
	//		err = db.MysqlEngine.Context(ctx).SQL("select * from proc_data_binding where proc_ins_id=? and proc_ins_node_id=?", procInsId, procInsNodeId).Find(&dataBindingRows)
	//	} else {
	//		err = db.MysqlEngine.Context(ctx).SQL("select * from proc_data_binding where proc_ins_id=?", procInsId).Find(&dataBindingRows)
	//	}
	//	if err != nil {
	//		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
	//		return
	//	}
	//}
	result = []*models.TaskNodeBindingObj{}
	for _, row := range dataBindingRows {
		if row.ProcDefNodeId == "" {
			continue
		}
		tmpBindingObj := models.TaskNodeBindingObj{
			Bound:             "Y",
			EntityDataId:      row.EntityDataId,
			EntityTypeId:      row.EntityTypeId,
			NodeDefId:         row.ProcDefNodeId,
			Id:                row.Id,
			EntityDisplayName: row.EntityDataName,
			NodeInstId:        procInsNodeId,
			ProcInstId:        procInsId,
		}
		entityMsg := strings.Split(row.EntityTypeId, ":")
		if len(entityMsg) == 2 {
			tmpBindingObj.PackageName = entityMsg[0]
			tmpBindingObj.EntityName = entityMsg[1]
		}
		if !row.BindFlag {
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
		log.Logger.Error("UpdateProcNodeBindingData fail", log.Error(err))
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func UpdateProcInsNodeBindingData(ctx context.Context, param []*models.TaskNodeBindingObj, procInsId, procInsNodeId, operator string) (err error) {
	var dataBindingRows []*models.ProcDataBinding
	err = db.MysqlEngine.Context(ctx).SQL("select id,proc_def_node_id,entity_data_id,entity_data_name,entity_type_id,bind_type,bind_flag from proc_data_binding where proc_ins_id=? and proc_ins_node_id=?", procInsId, procInsNodeId).Find(&dataBindingRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	var actions []*db.ExecAction
	nowTime := time.Now()
	for _, v := range dataBindingRows {
		boundFlag := false
		for _, input := range param {
			if input.EntityTypeId == v.EntityTypeId && input.EntityDataId == v.EntityDataId {
				if input.Bound == "Y" {
					boundFlag = true
					break
				}
			}
		}
		actions = append(actions, &db.ExecAction{Sql: "update proc_data_binding set bind_flag=?,updated_by=?,updated_time=? where id=?", Param: []interface{}{boundFlag, operator, nowTime, v.Id}})
	}
	if err = db.Transaction(actions, ctx); err != nil {
		log.Logger.Error("UpdateProcInsNodeBindingData fail", log.Error(err))
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func GetProcPreviewEntityNode(ctx context.Context, procInsId string) (result *models.ProcPreviewData, err error) {
	var insRows []*models.ProcIns
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_ins where id=?", procInsId).Find(&insRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(insRows) == 0 {
		err = exterror.Catch(exterror.New().DatabaseQueryEmptyError, fmt.Errorf("proc_ins id=%s", procInsId))
		return
	}
	sessionId := insRows[0].ProcSessionId
	if sessionId == "" || strings.HasPrefix(sessionId, "public_session_") {
		result, err = getProcCacheEntityNode(ctx, procInsId)
		result.ProcessSessionId = sessionId
		return
	}
	result, err = GetProcPreviewBySession(ctx, sessionId)
	return
}

func GetProcPreviewBySession(ctx context.Context, sessionId string) (result *models.ProcPreviewData, err error) {
	result = &models.ProcPreviewData{ProcessSessionId: sessionId, EntityTreeNodes: []*models.ProcPreviewEntityNode{}}
	var graphNodeRows []*models.ProcInsGraphNode
	err = db.MysqlEngine.Context(ctx).SQL("select data_id,display_name,entity_name,graph_node_id,pkg_name,entity_name,prev_ids,succ_ids,full_data_id from proc_ins_graph_node where proc_session_id=?", sessionId).Find(&graphNodeRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	for _, row := range graphNodeRows {
		tmpNodeObj := models.ProcPreviewEntityNode{
			Id:            row.GraphNodeId,
			DataId:        row.DataId,
			DisplayName:   row.DisplayName,
			PackageName:   row.PkgName,
			EntityName:    row.EntityName,
			FullDataId:    row.FullDataId,
			PreviousIds:   []string{},
			SucceedingIds: []string{},
		}
		if row.PrevIds != "" {
			tmpNodeObj.PreviousIds = strings.Split(row.PrevIds, ",")
		}
		if row.SuccIds != "" {
			tmpNodeObj.SucceedingIds = strings.Split(row.SuccIds, ",")
		}
		result.EntityTreeNodes = append(result.EntityTreeNodes, &tmpNodeObj)
	}
	return
}

func getProcCacheEntityNode(ctx context.Context, procInsId string) (result *models.ProcPreviewData, err error) {
	result = &models.ProcPreviewData{EntityTreeNodes: []*models.ProcPreviewEntityNode{}}
	var cacheRows []*models.ProcDataCache
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_data_cache where proc_ins_id=?", procInsId).Find(&cacheRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	succMap := make(map[string][]string)
	prevMap := make(map[string][]string)
	for _, row := range cacheRows {
		entityMsg := strings.Split(row.EntityTypeId, ":")
		if len(entityMsg) != 2 {
			continue
		}
		tmpNodeObj := models.ProcPreviewEntityNode{
			Id:            row.EntityId,
			DataId:        row.EntityDataId,
			DisplayName:   row.EntityDataName,
			PackageName:   entityMsg[0],
			EntityName:    entityMsg[1],
			FullDataId:    row.FullDataId,
			PreviousIds:   []string{},
			SucceedingIds: []string{},
		}
		if row.PrevIds != "" {
			tmpNodeObj.PreviousIds = strings.Split(row.PrevIds, ",")
			prevMap[tmpNodeObj.Id] = tmpNodeObj.PreviousIds
			for _, v := range tmpNodeObj.PreviousIds {
				if sucExist, ok := succMap[v]; ok {
					succMap[v] = append(sucExist, tmpNodeObj.Id)
				} else {
					succMap[v] = []string{tmpNodeObj.Id}
				}
			}
		}
		if row.SuccIds != "" {
			tmpNodeObj.SucceedingIds = strings.Split(row.SuccIds, ",")
			succMap[tmpNodeObj.Id] = tmpNodeObj.SucceedingIds
			for _, v := range tmpNodeObj.SucceedingIds {
				if preExist, ok := prevMap[v]; ok {
					prevMap[v] = append(preExist, tmpNodeObj.Id)
				} else {
					prevMap[v] = []string{tmpNodeObj.Id}
				}
			}
		}
		result.EntityTreeNodes = append(result.EntityTreeNodes, &tmpNodeObj)
	}
	for _, v := range result.EntityTreeNodes {
		if sucExist, ok := succMap[v.Id]; ok {
			v.SucceedingIds = joinStringList(v.SucceedingIds, sucExist)
		}
		if preExist, ok := prevMap[v.Id]; ok {
			v.PreviousIds = joinStringList(v.PreviousIds, preExist)
		}
	}
	return
}

func joinStringList(aList, bList []string) (output []string) {
	existMap := make(map[string]int)
	aList = append(aList, bList...)
	for _, v := range aList {
		if _, ok := existMap[v]; ok {
			continue
		}
		output = append(output, v)
		existMap[v] = 1
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
	previewRows := []*models.ProcDataPreview{}
	if procStartParam.ProcessSessionId != "" {
		err = db.MysqlEngine.Context(ctx).SQL("select * from proc_data_preview where proc_session_id=?", procStartParam.ProcessSessionId).Find(&previewRows)
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}
	}
	var entityDataId, entityTypeId, entityDataName string
	for _, row := range previewRows {
		if row.BindType == "process" {
			entityDataId = row.EntityDataId
			entityTypeId = row.EntityTypeId
			entityDataName = row.EntityDataName
		}
	}
	actions = append(actions, &db.ExecAction{Sql: "insert into proc_ins(id,proc_def_id,proc_def_key,proc_def_name,status,entity_data_id,entity_type_id,entity_data_name,proc_session_id,created_by,created_time,updated_by,updated_time) values (?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
		procInsId, procDefObj.Id, procDefObj.Key, procDefObj.Name, models.JobStatusReady, entityDataId, entityTypeId, entityDataName, procStartParam.ProcessSessionId, operator, nowTime, operator, nowTime,
	}})
	if procStartParam.ParentInsNodeId != "" {
		actions = append(actions, &db.ExecAction{Sql: "update proc_ins set parent_ins_node_id=? where id=?", Param: []interface{}{procStartParam.ParentInsNodeId, procInsId}})
	}
	workflowRow = &models.ProcRunWorkflow{Id: "wf_" + guid.CreateGuid(), ProcInsId: procInsId, Name: procDefObj.Name, Status: models.JobStatusReady, CreatedTime: nowTime}
	actions = append(actions, &db.ExecAction{Sql: "insert into proc_run_workflow(id,proc_ins_id,name,status,created_time) values (?,?,?,?,?)", Param: []interface{}{
		workflowRow.Id, workflowRow.ProcInsId, workflowRow.Name, workflowRow.Status, workflowRow.CreatedTime,
	}})
	if procStartParam.ParentRunNodeId != "" {
		workflowRow.ParentRunNodeId = procStartParam.ParentRunNodeId
		actions = append(actions, &db.ExecAction{Sql: "update proc_run_workflow set parent_run_node_id=? where id=?", Param: []interface{}{procStartParam.ParentRunNodeId, workflowRow.Id}})
	}
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
	for _, row := range previewRows {
		if row.BindType == "process" {
			actions = append(actions, &db.ExecAction{Sql: "insert into proc_data_binding(id,proc_def_id,proc_ins_id,entity_id,entity_data_id,entity_data_name,entity_type_id,bind_flag,bind_type,full_data_id,sub_session_id,created_by,created_time) values (?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
				fmt.Sprintf("p_bind_%d_%d", row.Id, nowTime.Unix()), procDefObj.Id, procInsId, row.EntityDataId, row.EntityDataId, row.EntityDataName, row.EntityTypeId, row.IsBound, row.BindType, row.FullDataId, row.SubSessionId, operator, nowTime,
			}})
		}
	}
	workNodeIdMap := make(map[string]string)
	for _, node := range procDefNodes {
		if node.NodeType != "automatic" && node.NodeType != "data" {
			node.Timeout = 0
		}
		tmpProcInsNodeId := "pins_node_" + guid.CreateGuid()
		actions = append(actions, &db.ExecAction{Sql: "insert into proc_ins_node(id,proc_ins_id,proc_def_node_id,name,node_type,status,ordered_no,created_by,created_time) values (?,?,?,?,?,?,?,?,?)", Param: []interface{}{
			tmpProcInsNodeId, procInsId, node.Id, node.Name, node.NodeType, models.JobStatusReady, node.OrderedNo, operator, nowTime,
		}})
		workNodeObj := models.ProcRunNode{Id: "wn_" + guid.CreateGuid(), WorkflowId: workflowRow.Id, ProcInsNodeId: tmpProcInsNodeId, Name: node.Name, JobType: node.NodeType, Status: models.JobStatusReady, Timeout: node.Timeout, CreatedTime: nowTime}
		if node.NodeType == models.JobTimeType || node.NodeType == models.JobDateType {
			workNodeObj.Input = node.TimeConfig
			actions = append(actions, &db.ExecAction{Sql: "insert into proc_run_node(id,workflow_id,proc_ins_node_id,name,job_type,status,timeout,input,created_time) values (?,?,?,?,?,?,?,?,?)", Param: []interface{}{
				workNodeObj.Id, workNodeObj.WorkflowId, workNodeObj.ProcInsNodeId, workNodeObj.Name, workNodeObj.JobType, workNodeObj.Status, workNodeObj.Timeout, workNodeObj.Input, workNodeObj.CreatedTime,
			}})
		} else {
			actions = append(actions, &db.ExecAction{Sql: "insert into proc_run_node(id,workflow_id,proc_ins_node_id,name,job_type,status,timeout,created_time) values (?,?,?,?,?,?,?,?)", Param: []interface{}{
				workNodeObj.Id, workNodeObj.WorkflowId, workNodeObj.ProcInsNodeId, workNodeObj.Name, workNodeObj.JobType, workNodeObj.Status, workNodeObj.Timeout, workNodeObj.CreatedTime,
			}})
		}
		workNodeIdMap[node.Id] = workNodeObj.Id
		workNodes = append(workNodes, &workNodeObj)
		// data bind
		for _, row := range previewRows {
			if row.ProcDefNodeId == node.Id {
				actions = append(actions, &db.ExecAction{Sql: "insert into proc_data_binding(id,proc_def_id,proc_ins_id,proc_def_node_id,proc_ins_node_id,entity_id,entity_data_id,entity_data_name,entity_type_id,bind_flag,bind_type,full_data_id,sub_session_id,created_by,created_time) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
					fmt.Sprintf("p_bind_%d_%d", row.Id, nowTime.Unix()), procDefObj.Id, procInsId, node.Id, tmpProcInsNodeId, row.EntityDataId, row.EntityDataId, row.EntityDataName, row.EntityTypeId, row.IsBound, row.BindType, row.FullDataId, row.SubSessionId, operator, nowTime,
				}})
			}
		}
	}
	// 数据冲突检测
	if procDefObj.ConflictCheck {
		var existBindingRows []*models.ProcDataBinding
		err = db.MysqlEngine.Context(ctx).SQL("select t2.proc_ins_id,t2.entity_data_id,t2.entity_type_id from proc_ins t1 left join proc_data_binding t2 on t1.id=t2.proc_ins_id where t1.status='InProgress' and t2.bind_flag=1").Find(&existBindingRows)
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}
		// 如果是子编排，要不与父编排冲突
		var parentProcInsId string
		if procStartParam.ParentInsNodeId != "" {
			parentInsNodeObj, getParentInsErr := GetSimpleProcInsNode(ctx, procStartParam.ParentInsNodeId, "")
			if getParentInsErr != nil {
				err = getParentInsErr
				return
			}
			parentProcInsId = parentInsNodeObj.ProcInsId
		}
		for _, previewRow := range previewRows {
			if !previewRow.IsBound {
				continue
			}
			for _, v := range existBindingRows {
				if v.ProcInsId == parentProcInsId {
					continue
				}
				if previewRow.EntityTypeId == v.EntityTypeId && previewRow.EntityDataId == v.EntityDataId {
					err = fmt.Errorf("rowData conflict check fail,data->%s:%s is using by procInsId:%s", v.EntityTypeId, v.EntityDataId, v.ProcInsId)
					break
				}
			}
			if err != nil {
				break
			}
		}
		if err != nil {
			return
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
		log.Logger.Error("CreateProcInstance fail", log.Error(err))
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func CreatePublicProcInstance(ctx context.Context, startParam *models.RequestProcessData, operator string) (procInsId string, workflowRow *models.ProcRunWorkflow, workNodes []*models.ProcRunNode, workLinks []*models.ProcRunLink, err error) {
	procInsId = "pins_" + guid.CreateGuid()
	procDefObj, getProcDefErr := GetSimpleProcDefRow(ctx, startParam.ProcDefId)
	if getProcDefErr != nil {
		err = getProcDefErr
		return
	}
	var actions []*db.ExecAction
	nowTime := time.Now()
	newOidMap := make(map[string]string)
	var entityDataId, entityTypeId, entityDataName string
	for _, row := range startParam.Entities {
		if row.EntityDataOp == "create" {
			newOidMap[row.Oid] = models.NewOidDataPrefix + row.Oid
			row.Oid = models.NewOidDataPrefix + row.Oid
		}
		if row.EntityDataId == "" {
			row.EntityDataId = row.Oid
		}
		if row.Oid == startParam.RootEntityOid {
			entityDataId = row.EntityDataId
			entityTypeId = fmt.Sprintf("%s:%s", row.PackageName, row.EntityName)
			entityDataName = row.EntityDisplayName
			continue
		}
	}
	procSessionId := "public_session_" + guid.CreateGuid()
	actions = append(actions, &db.ExecAction{Sql: "insert into proc_ins(id,proc_def_id,proc_def_key,proc_def_name,status,entity_data_id,entity_type_id,entity_data_name,created_by,created_time,updated_by,updated_time,proc_session_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
		procInsId, procDefObj.Id, procDefObj.Key, procDefObj.Name, models.JobStatusReady, entityDataId, entityTypeId, entityDataName, operator, nowTime, operator, nowTime, procSessionId,
	}})
	workflowRow = &models.ProcRunWorkflow{Id: "wf_" + guid.CreateGuid(), ProcInsId: procInsId, Name: procDefObj.Name, Status: models.JobStatusReady, CreatedTime: nowTime}
	actions = append(actions, &db.ExecAction{Sql: "insert into proc_run_workflow(id,proc_ins_id,name,status,created_time) values (?,?,?,?,?)", Param: []interface{}{
		workflowRow.Id, workflowRow.ProcInsId, workflowRow.Name, workflowRow.Status, workflowRow.CreatedTime,
	}})
	var procDefNodes []*models.ProcDefNode
	err = db.MysqlEngine.Context(ctx).SQL("select id,node_id,proc_def_id,name,description,status,node_type,service_name,dynamic_bind,bind_node_id,risk_check,routine_expression,context_param_nodes,timeout,ordered_no,time_config from proc_def_node where proc_def_id=? order by ordered_no", startParam.ProcDefId).Find(&procDefNodes)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	var procDefLinks []*models.ProcDefNodeLink
	err = db.MysqlEngine.Context(ctx).SQL("select id,link_id,proc_def_id,source,target,name from proc_def_node_link where proc_def_id=?", startParam.ProcDefId).Find(&procDefLinks)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	inputEntityMap := make(map[string]*models.RequestCacheEntityValue)
	for _, row := range startParam.Entities {
		tmpEntityTypeId := fmt.Sprintf("%s:%s", row.PackageName, row.EntityName)
		if row.Oid == startParam.RootEntityOid {
			actions = append(actions, &db.ExecAction{Sql: "insert into proc_data_binding(id,proc_def_id,proc_ins_id,entity_id,entity_data_id,entity_data_name,entity_type_id,bind_flag,bind_type,full_data_id,created_by,created_time) values (?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
				fmt.Sprintf("p_bind_%s", guid.CreateGuid()), procDefObj.Id, procInsId, row.EntityDataId, row.EntityDataId, row.EntityDisplayName, tmpEntityTypeId, 0, "process", row.FullEntityDataId, operator, nowTime,
			}})
			actions = append(actions, &db.ExecAction{Sql: "insert into proc_data_preview(proc_def_id,proc_session_id,proc_def_node_id,entity_data_id,entity_data_name,entity_type_id,ordered_no,bind_type,full_data_id,is_bound,created_by,created_time) values (?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
				procDefObj.Id, procSessionId, "", row.EntityDataId, row.EntityDisplayName, tmpEntityTypeId, "", "process", row.FullEntityDataId, 0, operator, nowTime,
			}})
		}
		actions = append(actions, &db.ExecAction{Sql: "insert into proc_data_cache(id,proc_ins_id,entity_id,entity_data_id,entity_data_name,entity_type_id,full_data_id,data_value,prev_ids,succ_ids,created_time) values (?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
			"p_cache_" + guid.CreateGuid(), procInsId, row.Oid, row.EntityDataId, row.EntityDisplayName, tmpEntityTypeId, row.FullEntityDataId, row.GetAttrDataValueString(), strings.Join(getReplaceOidMap(row.PreviousOids, newOidMap), ","), strings.Join(getReplaceOidMap(row.SucceedingOids, newOidMap), ","), nowTime,
		}})
		inputEntityMap[row.Oid] = row
	}
	workNodeIdMap := make(map[string]string)
	orderIndex := 1
	for _, node := range procDefNodes {
		nodeOrderNo := ""
		if node.NodeType == string(models.ProcDefNodeTypeHuman) || node.NodeType == string(models.ProcDefNodeTypeAutomatic) || node.NodeType == string(models.ProcDefNodeTypeData) {
			nodeOrderNo = fmt.Sprintf("%d", orderIndex)
			orderIndex += 1
		}
		if node.NodeType != "automatic" && node.NodeType != "data" {
			node.Timeout = 0
		}
		tmpProcInsNodeId := "pins_node_" + guid.CreateGuid()
		actions = append(actions, &db.ExecAction{Sql: "insert into proc_ins_node(id,proc_ins_id,proc_def_node_id,name,node_type,status,ordered_no,created_by,created_time) values (?,?,?,?,?,?,?,?,?)", Param: []interface{}{
			tmpProcInsNodeId, procInsId, node.Id, node.Name, node.NodeType, models.JobStatusReady, node.OrderedNo, operator, nowTime,
		}})
		workNodeObj := models.ProcRunNode{Id: "wn_" + guid.CreateGuid(), WorkflowId: workflowRow.Id, ProcInsNodeId: tmpProcInsNodeId, Name: node.Name, JobType: node.NodeType, Status: models.JobStatusReady, Timeout: node.Timeout, CreatedTime: nowTime}
		if node.NodeType == models.JobTimeType || node.NodeType == models.JobDateType {
			workNodeObj.Input = node.TimeConfig
			actions = append(actions, &db.ExecAction{Sql: "insert into proc_run_node(id,workflow_id,proc_ins_node_id,name,job_type,status,timeout,input,created_time) values (?,?,?,?,?,?,?,?,?)", Param: []interface{}{
				workNodeObj.Id, workNodeObj.WorkflowId, workNodeObj.ProcInsNodeId, workNodeObj.Name, workNodeObj.JobType, workNodeObj.Status, workNodeObj.Timeout, workNodeObj.Input, workNodeObj.CreatedTime,
			}})
		} else {
			actions = append(actions, &db.ExecAction{Sql: "insert into proc_run_node(id,workflow_id,proc_ins_node_id,name,job_type,status,timeout,created_time) values (?,?,?,?,?,?,?,?)", Param: []interface{}{
				workNodeObj.Id, workNodeObj.WorkflowId, workNodeObj.ProcInsNodeId, workNodeObj.Name, workNodeObj.JobType, workNodeObj.Status, workNodeObj.Timeout, workNodeObj.CreatedTime,
			}})
		}
		workNodeIdMap[node.Id] = workNodeObj.Id
		workNodes = append(workNodes, &workNodeObj)
		// data bind
		for _, row := range startParam.Bindings {
			if row.NodeId == node.Id {
				tmpOid := row.Oid
				if newOid, matchNew := newOidMap[tmpOid]; matchNew {
					tmpOid = newOid
					if row.EntityDataId == "" {
						row.EntityDataId = tmpOid
					}
				}
				if inputEntityObj, ok := inputEntityMap[tmpOid]; ok {
					tmpBoundFlag := false
					if row.BindFlag == "Y" {
						tmpBoundFlag = true
					}
					tmpEntityTypeId := fmt.Sprintf("%s:%s", inputEntityObj.PackageName, inputEntityObj.EntityName)
					actions = append(actions, &db.ExecAction{Sql: "insert into proc_data_binding(id,proc_def_id,proc_ins_id,proc_def_node_id,proc_ins_node_id,entity_id,entity_data_id,entity_data_name,entity_type_id,bind_flag,bind_type,full_data_id,created_by,created_time) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
						fmt.Sprintf("p_bind_%s", guid.CreateGuid()), procDefObj.Id, procInsId, node.Id, tmpProcInsNodeId, row.EntityDataId, row.EntityDataId, inputEntityObj.EntityDisplayName, tmpEntityTypeId, tmpBoundFlag, "taskNode", inputEntityObj.FullEntityDataId, operator, nowTime,
					}})
					actions = append(actions, &db.ExecAction{Sql: "insert into proc_data_preview(proc_def_id,proc_session_id,proc_def_node_id,entity_data_id,entity_data_name,entity_type_id,ordered_no,bind_type,full_data_id,is_bound,created_by,created_time) values (?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
						procDefObj.Id, procSessionId, node.Id, row.EntityDataId, inputEntityObj.EntityDisplayName, tmpEntityTypeId, nodeOrderNo, "taskNode", inputEntityObj.FullEntityDataId, tmpBoundFlag, operator, nowTime,
					}})
				}
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
		log.Logger.Error("CreatePublicProcInstance fail", log.Error(err))
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func getReplaceOidMap(inputList []string, oidMap map[string]string) (outputList []string) {
	for _, v := range inputList {
		if newOid, ok := oidMap[v]; ok {
			outputList = append(outputList, newOid)
		} else {
			outputList = append(outputList, v)
		}
	}
	return
}

func ListProcInstance(ctx context.Context, userRoles []string, withCronIns, withSubProc, mgmtRole, search, status string) (result []*models.ProcInsDetail, err error) {
	var procInsRows []*models.ProcInsWithVersion
	var filterSql string
	filterParam := []interface{}{}
	if withCronIns == "yes" {
		filterSql += " and t1.created_by='systemCron' "
	} else if withCronIns == "no" {
		filterSql += " and t1.created_by!='systemCron' "
	}
	if withSubProc == "no" {
		filterSql += " and t2.sub_proc=0 "
	}
	if status != "" {
		filterSql += " and t1.status=? "
		filterParam = append(filterParam, status)
	}
	if search != "" {
		filterSql += " and (t1.id like ? or t1.proc_def_name like ? or t1.entity_data_name like ?) "
		filterParam = append(filterParam, fmt.Sprintf("%%%s%%", search), fmt.Sprintf("%%%s%%", search), fmt.Sprintf("%%%s%%", search))
	}
	if mgmtRole != "" {
		userRoles = []string{mgmtRole}
	}
	err = db.MysqlEngine.Context(ctx).SQL("select t1.*,t2.`version`,t2.sub_proc from proc_ins t1 join proc_def t2 on t1.proc_def_id=t2.id and t1.proc_def_id"+
		" in (select proc_def_id from proc_def_permission where permission='"+string(models.USE)+"' and role_id in ('"+strings.Join(userRoles, "','")+"')) "+filterSql+" order by t1.created_time desc limit 20", filterParam...).Find(&procInsRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	result = []*models.ProcInsDetail{}
	var procInProgressIdList []string
	for _, procInsObj := range procInsRows {
		tmpInsObj := &models.ProcInsDetail{
			Id:                procInsObj.Id,
			ProcDefId:         procInsObj.ProcDefId,
			ProcDefKey:        procInsObj.ProcDefKey,
			ProcInstKey:       procInsObj.Id,
			ProcInstName:      procInsObj.ProcDefName,
			Operator:          procInsObj.CreatedBy,
			Status:            procInsObj.Status,
			EntityDataId:      procInsObj.EntityDataId,
			EntityTypeId:      procInsObj.EntityTypeId,
			EntityDisplayName: procInsObj.EntityDataName,
			CreatedTime:       procInsObj.CreatedTime.Format(models.DateTimeFormat),
			Version:           procInsObj.Version,
			SubProc:           procInsObj.SubProc,
			DisplayStatus:     procInsObj.Status,
		}
		if procInsObj.Status == models.JobStatusRunning {
			procInProgressIdList = append(procInProgressIdList, procInsObj.Id)
		}
		//if transStatus, ok := models.ProcStatusTransMap[tmpInsObj.Status]; ok {
		//	tmpInsObj.Status = transStatus
		//}
		result = append(result, tmpInsObj)
	}
	procInsNodeStatusMap := make(map[string]string)
	procInsNodeStatusMap, err = getProcInsNodeStatus(ctx, procInProgressIdList)
	for _, row := range result {
		if nodeStatus, ok := procInsNodeStatusMap[row.Id]; ok {
			row.DisplayStatus = fmt.Sprintf("%s(%s)", row.Status, nodeStatus)
		}
	}
	return
}

func GetProcInstance(ctx context.Context, procInsId string) (result *models.ProcInsDetail, err error) {
	var procInsRows []*models.ProcIns
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_ins where id=?", procInsId).Find(&procInsRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(procInsRows) == 0 {
		err = exterror.Catch(exterror.New().DatabaseQueryEmptyError, fmt.Errorf("cat not find proc_ins with id:%s", procInsId))
		return
	}
	procInsObj := procInsRows[0]
	procDefObj, getProcDefErr := GetSimpleProcDefRow(ctx, procInsObj.ProcDefId)
	if getProcDefErr != nil {
		err = getProcDefErr
		return
	}
	result = &models.ProcInsDetail{
		Id:                procInsObj.Id,
		ProcDefId:         procInsObj.ProcDefId,
		ProcDefKey:        procInsObj.ProcDefKey,
		ProcInstKey:       procInsObj.Id,
		ProcInstName:      procInsObj.ProcDefName,
		Operator:          procInsObj.CreatedBy,
		Status:            procInsObj.Status,
		EntityDataId:      procInsObj.EntityDataId,
		EntityTypeId:      procInsObj.EntityTypeId,
		EntityDisplayName: procInsObj.EntityDataName,
		CreatedTime:       procInsObj.CreatedTime.Format(models.DateTimeFormat),
		SubProc:           procDefObj.SubProc,
		DisplayStatus:     procInsObj.Status,
	}
	//if transStatus, ok := models.ProcStatusTransMap[result.Status]; ok {
	//	result.Status = transStatus
	//}
	if procInsObj.ParentInsNodeId != "" {
		procInsParentMap := make(map[string]*models.ParentProcInsObj)
		procInsParentMap, err = getProcInsParentMap(ctx, []string{procInsObj.Id})
		if err != nil {
			return
		}
		if v, ok := procInsParentMap[procInsObj.Id]; ok {
			result.ParentProcIns = v
		}
	}
	var procInsNodeRows []*models.ProcInsNode
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_ins_node where proc_ins_id=? order by ordered_no", procInsId).Find(&procInsNodeRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	var nodeStatus string
	for _, row := range procInsNodeRows {
		if row.Status == models.JobStatusFail {
			nodeStatus = row.Status
			continue
		}
		if row.Status == models.JobStatusTimeout {
			if nodeStatus == models.JobStatusFail {
				continue
			}
			nodeStatus = row.Status
		}
	}
	if nodeStatus != "" && procInsObj.Status == models.JobStatusRunning {
		result.DisplayStatus = fmt.Sprintf("%s(%s)", models.JobStatusRunning, nodeStatus)
	}
	var procDefLinks []*models.ProcDefNodeLink
	err = db.MysqlEngine.Context(ctx).SQL("select id,link_id,proc_def_id,source,target,name from proc_def_node_link where proc_def_id=?", result.ProcDefId).Find(&procDefLinks)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	result.NodeLinks = procDefLinks
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
	var procNodeDefRows []*models.ProcDefNode
	err = db.MysqlEngine.Context(ctx).SQL("select id,node_id,name,dynamic_bind,bind_node_id,allow_continue from proc_def_node where proc_def_id=?", result.ProcDefId).Find(&procNodeDefRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	defNodeIdNameMap := make(map[string]string)
	defNodeAllowContinueMap := make(map[string]bool)
	for _, v := range procNodeDefRows {
		defNodeIdNameMap[v.NodeId] = v.Name
		defNodeAllowContinueMap[v.Id] = v.AllowContinue
	}
	log.Logger.Info("defNodeAllowContinueMap", log.JsonObj("data", defNodeAllowContinueMap))
	orderIndex := 1
	for _, row := range procInsNodeRows {
		nodeObj := models.ProcInsNodeDetail{
			Id:                row.Id,
			NodeDefId:         row.ProcDefNodeId,
			NodeId:            row.ProcDefNodeId,
			NodeName:          row.Name,
			NodeType:          row.NodeType,
			ProcDefId:         result.ProcDefId,
			ProcDefKey:        result.ProcDefKey,
			ProcInstKey:       result.ProcInstKey,
			ProcInstId:        result.Id,
			Status:            row.Status,
			PreviousNodeIds:   []string{},
			SucceedingNodeIds: []string{},
			AllowContinue:     defNodeAllowContinueMap[row.ProcDefNodeId],
		}
		for _, defRow := range procNodeDefRows {
			if defRow.Id == row.ProcDefNodeId {
				nodeObj.NodeDefId = defRow.NodeId
				nodeObj.DynamicBind = defRow.DynamicBind
				if defRow.DynamicBind == 1 {
					nodeObj.DynamicBindNodeName = defNodeIdNameMap[defRow.BindNodeId]
				}
				break
			}
		}
		//if transStatus, ok := models.ProcStatusTransMap[nodeObj.Status]; ok {
		//	nodeObj.Status = transStatus
		//}
		if row.NodeType == string(models.ProcDefNodeTypeHuman) || row.NodeType == string(models.ProcDefNodeTypeAutomatic) || row.NodeType == string(models.ProcDefNodeTypeData) || row.NodeType == models.JobSubProcType {
			nodeObj.OrderedNo = fmt.Sprintf("%d", orderIndex)
			orderIndex += 1
		}
		if parentList, ok := parentMap[row.ProcDefNodeId]; ok {
			nodeObj.PreviousNodeIds = parentList
		}
		if childrenList, ok := childrenMap[row.ProcDefNodeId]; ok {
			nodeObj.SucceedingNodeIds = childrenList
		}
		result.TaskNodeInstances = append(result.TaskNodeInstances, &nodeObj)
	}
	return
}

func GetProcExecNodeData(ctx context.Context, procRunNodeId string) (procInsNode *models.ProcInsNode, procDefNode *models.ProcDefNode, procDefNodeParams []*models.ProcDefNodeParam, dataBinding []*models.ProcDataBinding, err error) {
	if procInsNode, err = GetSimpleProcInsNode(ctx, "", procRunNodeId); err != nil {
		return
	}
	if procDefNode, err = GetSimpleProcDefNode(ctx, procInsNode.ProcDefNodeId); err != nil {
		return
	}
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def_node_param where proc_def_node_id=?", procDefNode.Id).Find(&procDefNodeParams)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_data_binding where proc_ins_node_id=? and bind_flag=1", procInsNode.Id).Find(&dataBinding)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func GetSimpleProcInsNode(ctx context.Context, procInsNodeId, procRunNodeId string) (procInsNode *models.ProcInsNode, err error) {
	var procInsNodeRows []*models.ProcInsNode
	if procInsNodeId != "" {
		err = db.MysqlEngine.Context(ctx).SQL("select id,proc_ins_id,proc_def_node_id,name,node_type,status,created_by,updated_by from proc_ins_node where id=?", procInsNodeId).Find(&procInsNodeRows)
	} else if procRunNodeId != "" {
		err = db.MysqlEngine.Context(ctx).SQL("select id,proc_ins_id,proc_def_node_id,name,node_type,status,created_by,updated_by from proc_ins_node where id in (select proc_ins_node_id from proc_run_node where id=?)", procRunNodeId).Find(&procInsNodeRows)
	}
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(procInsNodeRows) == 0 {
		err = exterror.Catch(exterror.New().DatabaseQueryEmptyError, fmt.Errorf("proc_ins_node"))
		return
	}
	procInsNode = procInsNodeRows[0]
	return
}

func GetSimpleProcDefNode(ctx context.Context, procDefNodeId string) (procDefNode *models.ProcDefNode, err error) {
	var procDefNodeRows []*models.ProcDefNode
	err = db.MysqlEngine.Context(ctx).SQL("select id,node_id,proc_def_id,name,node_type,service_name,dynamic_bind,bind_node_id,risk_check,routine_expression,context_param_nodes,timeout,sub_proc_def_id from proc_def_node where id=?", procDefNodeId).Find(&procDefNodeRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(procDefNodeRows) == 0 {
		err = exterror.Catch(exterror.New().DatabaseQueryEmptyError, fmt.Errorf("proc_def_node"))
		return
	}
	procDefNode = procDefNodeRows[0]
	return
}

func GetDynamicBindNodeData(ctx context.Context, procInsId, procDefId, bindNodeId string) (dataBinding []*models.ProcDataBinding, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_data_binding where proc_ins_id=? and bind_flag=1 and proc_def_node_id in (select id from proc_def_node where proc_def_id=? and node_id=?)", procInsId, procDefId, bindNodeId).Find(&dataBinding)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
	}
	return
}

func UpdateDynamicNodeBindData(ctx context.Context, procInsId, procInsNodeId, procDefId, procDefNodeId string, dataBinding []*models.ProcDataBinding) (err error) {
	var actions []*db.ExecAction
	nowTime := time.Now()
	actions = append(actions, &db.ExecAction{Sql: "delete from proc_data_binding where proc_ins_id=? and proc_ins_node_id=?", Param: []interface{}{procInsId, procInsNodeId}})
	for _, row := range dataBinding {
		actions = append(actions, &db.ExecAction{Sql: "insert into proc_data_binding(id,proc_def_id,proc_ins_id,proc_def_node_id,proc_ins_node_id,entity_id,entity_data_id,entity_data_name,entity_type_id,bind_flag,bind_type,full_data_id,created_by,created_time) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
			fmt.Sprintf("p_bind_%s", guid.CreateGuid()), procDefId, procInsId, procDefNodeId, procInsNodeId, row.EntityId, row.EntityDataId, row.EntityDataName, row.EntityTypeId, 1, "taskNode", row.FullDataId, row.CreatedBy, nowTime,
		}})
	}
	err = db.Transaction(actions, ctx)
	if err != nil {
		log.Logger.Error("UpdateDynamicNodeBindData fail", log.Error(err))
	}
	return
}

func AddProcCacheData(ctx context.Context, procInsId string, dataBinding []*models.ProcDataBinding) (err error) {
	if len(dataBinding) == 0 {
		return
	}
	var cacheDataRows []*models.ProcDataCache
	err = db.MysqlEngine.Context(ctx).SQL("select id,entity_id,entity_data_id,entity_type_id from proc_data_cache where proc_ins_id=?", procInsId).Find(&cacheDataRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	var actions []*db.ExecAction
	nowTime := time.Now()
	for _, v := range dataBinding {
		existFlag := false
		for _, row := range cacheDataRows {
			if row.EntityTypeId == v.EntityTypeId && row.EntityDataId == v.EntityDataId {
				existFlag = true
				break
			}
		}
		if !existFlag {
			actions = append(actions, &db.ExecAction{Sql: "insert into proc_data_cache(id,proc_ins_id,entity_id,entity_data_id,entity_data_name,entity_type_id,full_data_id,created_time) values (?,?,?,?,?,?,?,?)", Param: []interface{}{
				"p_cache_" + guid.CreateGuid(), procInsId, v.EntityId, v.EntityDataId, v.EntityDataName, v.EntityTypeId, v.FullDataId, nowTime,
			}})
		}
	}
	if len(actions) > 0 {
		err = db.Transaction(actions, ctx)
		if err != nil {
			log.Logger.Error("AddProcCacheData fail", log.Error(err))
			err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		}
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

func GetSimpleLastPluginInterface(ctx context.Context, serviceName string) (interfaceObj *models.PluginInterfaceWithVer, err error) {
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
	interfaceObj = &models.PluginInterfaceWithVer{}
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
	return
}

func GetLastEnablePluginInterface(ctx context.Context, serviceName string) (pluginInterface *models.PluginConfigInterfaces, err error) {
	interfaceObj := &models.PluginInterfaceWithVer{}
	if interfaceObj, err = GetSimpleLastPluginInterface(ctx, serviceName); err != nil {
		return
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
		InputParameters:    []*models.PluginConfigInterfaceParameters{},
		OutputParameters:   []*models.PluginConfigInterfaceParameters{},
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
				tmpDataValue := strings.TrimSpace(fmt.Sprintf("%s", v.DataValue))
				if len(tmpDataValue) > 1000 {
					tmpDataValue = fmt.Sprintf("%s", tmpDataValue[:1000])
				}
				actions = append(actions, &db.ExecAction{Sql: "insert into proc_ins_node_req_param(req_id,data_index,from_type,name,data_type,data_value,entity_data_id,entity_type_id,is_sensitive,full_data_id,multiple,param_def_id,mapping_type,callback_id,created_time) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Param: []interface{}{
					v.ReqId, v.DataIndex, v.FromType, v.Name, v.DataType, tmpDataValue, v.EntityDataId, v.EntityTypeId, v.IsSensitive, v.FullDataId, v.Multiple, v.ParamDefId, v.MappingType, v.CallbackId, nowTime,
				}})
			}
		}
	}
	err = db.Transaction(actions, ctx)
	if err != nil {
		log.Logger.Error("RecordProcCallReq fail", log.Error(err))
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}

func GetProcInsNodeContext(ctx context.Context, procInsId, procInsNodeId, procDefNodeId string) (result *models.ProcNodeContextReq, err error) {
	var queryRows []*models.ProcNodeContextQueryObj
	if procInsNodeId != "" {
		err = db.MysqlEngine.Context(ctx).SQL("select t1.id,t1.name,t1.proc_def_node_id,t1.error_msg,t2.routine_expression,t2.service_name,t2.node_type,t3.start_time,t3.end_time from proc_ins_node t1 left join proc_def_node t2 on t1.proc_def_node_id=t2.id left join proc_run_node t3 on t3.proc_ins_node_id=t1.id where t1.proc_ins_id=? and t1.id=?", procInsId, procInsNodeId).Find(&queryRows)
	} else if procDefNodeId != "" {
		err = db.MysqlEngine.Context(ctx).SQL("select t1.id,t1.name,t1.proc_def_node_id,t1.error_msg,t2.routine_expression,t2.service_name,t2.node_type,t3.start_time,t3.end_time from proc_ins_node t1 left join proc_def_node t2 on t1.proc_def_node_id=t2.id left join proc_run_node t3 on t3.proc_ins_node_id=t1.id where t1.proc_ins_id=? and t1.proc_def_node_id=?", procInsId, procDefNodeId).Find(&queryRows)
	}
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	result = &models.ProcNodeContextReq{}
	if len(queryRows) == 0 {
		return
	}
	queryObj := queryRows[0]
	result.NodeId = queryObj.ProcDefNodeId
	result.NodeInstId = queryObj.Id
	result.NodeName = queryObj.Name
	result.NodeType = queryObj.NodeType
	result.NodeDefId = queryObj.ProcDefNodeId
	result.NodeExpression = queryObj.RoutineExpression
	result.PluginInfo = queryObj.ServiceName
	result.ErrorMessage = queryObj.ErrorMsg
	result.BeginTime = queryObj.StartTime.Format(models.DateTimeFormat)
	result.EndTime = queryObj.EndTime.Format(models.DateTimeFormat)
	result.Operator = getProcNodeOperator(ctx, procInsNodeId)
	result.RequestObjects = []models.ProcNodeContextReqObject{}
	if queryObj.NodeType == models.JobSubProcType {
		// 子编排的节点处理信息
		var sucProcRows []*models.ProcContextSubProcRow
		err = db.MysqlEngine.Context(ctx).SQL("select t1.entity_type_id,t1.entity_data_id,t3.proc_ins_id,t3.created_time,t4.proc_def_id,t4.proc_def_name,t4.status,t3.error_message,t5.`version` from proc_run_node_sub_proc t1 left join proc_run_node t2 on t1.proc_run_node_id=t2.id left join proc_run_workflow t3 on t1.workflow_id=t3.id left join proc_ins t4 on t3.proc_ins_id=t4.id left join proc_def t5 on t4.proc_def_id=t5.id where t2.proc_ins_node_id=?", queryObj.Id).Find(&sucProcRows)
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}
		if len(sucProcRows) == 0 {
			return
		}
		var subProcInsIdList []string
		for _, row := range sucProcRows {
			subProcInsIdList = append(subProcInsIdList, row.ProcInsId)
		}
		procInsNodeStatusMap := make(map[string]string)
		if procInsNodeStatusMap, err = getProcInsNodeStatus(ctx, subProcInsIdList); err != nil {
			return
		}
		for _, row := range sucProcRows {
			tmpReqObject := models.ProcNodeContextReqObject{CallbackParameter: fmt.Sprintf("%s:%s", row.EntityTypeId, row.EntityDataId)}
			inputMap := make(map[string]interface{})
			outputMap := make(map[string]interface{})
			if subNodeStatus, ok := procInsNodeStatusMap[row.ProcInsId]; ok {
				row.Status = fmt.Sprintf("%s(%s)", row.Status, subNodeStatus)
			}
			inputMap["entityTypeId"] = row.EntityTypeId
			inputMap["entityDataId"] = row.EntityDataId
			outputMap["procDefId"] = row.ProcDefId
			outputMap["procDefName"] = row.ProcDefName
			outputMap["procInsId"] = row.ProcInsId
			outputMap["version"] = row.Version
			outputMap["status"] = row.Status
			outputMap["errorMessage"] = row.ErrorMessage
			outputMap["createdTime"] = row.CreatedTime.Format(models.DateTimeFormat)
			tmpReqObject.Inputs = append(tmpReqObject.Inputs, inputMap)
			tmpReqObject.Outputs = append(tmpReqObject.Outputs, outputMap)
			result.RequestObjects = append(result.RequestObjects, tmpReqObject)
		}
	}
	var reqRows []*models.ProcInsNodeReq
	err = db.MysqlEngine.Context(ctx).SQL("select id from proc_ins_node_req where proc_ins_node_id=? order by created_time", queryObj.Id).Find(&reqRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	for _, v := range reqRows {
		queryObj.ReqId = v.Id
	}
	if queryObj.ReqId == "" {
		return
	}
	result.RequestId = queryObj.ReqId
	var procReqParams []*models.ProcInsNodeReqParam
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_ins_node_req_param where req_id=? order by data_index,id", queryObj.ReqId).Find(&procReqParams)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(procReqParams) == 0 {
		return
	}
	curDataIndex := 0
	curReqObj := models.ProcNodeContextReqObject{CallbackParameter: procReqParams[0].CallbackId}
	tmpInputMap := make(map[string]interface{})
	tmpOutputMap := make(map[string]interface{})
	for _, row := range procReqParams {
		if row.DataIndex != curDataIndex {
			curDataIndex = row.DataIndex
			curReqObj.Inputs = []map[string]interface{}{tmpInputMap}
			curReqObj.Outputs = []map[string]interface{}{tmpOutputMap}
			result.RequestObjects = append(result.RequestObjects, curReqObj)
			curReqObj = models.ProcNodeContextReqObject{CallbackParameter: row.CallbackId}
			tmpInputMap = make(map[string]interface{})
			tmpOutputMap = make(map[string]interface{})
		}
		if row.FromType == "input" {
			tmpInputMap[row.Name] = getInterfaceDataByDataType(row.DataValue, row.DataType, row.Name, row.Multiple, row.IsSensitive)
		} else {
			tmpOutputMap[row.Name] = getInterfaceDataByDataType(row.DataValue, row.DataType, row.Name, row.Multiple, row.IsSensitive)
		}
	}
	curReqObj.Inputs = []map[string]interface{}{tmpInputMap}
	curReqObj.Outputs = []map[string]interface{}{tmpOutputMap}
	result.RequestObjects = append(result.RequestObjects, curReqObj)
	return
}

func getProcNodeOperator(ctx context.Context, procInsNodeId string) (operator string) {
	var operationRows []*models.ProcRunOperation
	err := db.MysqlEngine.Context(ctx).SQL("select * from proc_run_operation where node_id in (select id from proc_run_node where proc_ins_node_id=?)", procInsNodeId).Find(&operationRows)
	if err != nil {
		log.Logger.Error("getProcNodeOperator query row fail", log.String("procInsNodeId", procInsNodeId), log.Error(err))
		return
	}
	for _, row := range operationRows {
		operator = row.CreatedBy
	}
	return
}

func getInterfaceDataByDataType(valueString, dataType, name string, multiple, isSensitive bool) (output interface{}) {
	if isSensitive {
		output = models.SensitiveDisplay
		return
	}
	var err error
	if dataType == "string" {
		if multiple {
			stringList := []string{}
			if err = json.Unmarshal([]byte(valueString), &stringList); err == nil {
				output = stringList
			} else {
				output = valueString
			}
		} else {
			output = valueString
		}
	} else if dataType == "list" {
		listMap := []map[string]interface{}{}
		if err = json.Unmarshal([]byte(valueString), &listMap); err == nil {
			output = listMap
		} else {
			output = valueString
		}
	}
	if err != nil {
		log.Logger.Error("getInterfaceDataByDataType error", log.String("value", valueString), log.String("dataType", dataType), log.String("name", name), log.Error(err))
	}
	return
}

func GetProcWorkByInsId(ctx context.Context, procInsId, procInsNodeId string) (workflowId, nodeId string, err error) {
	queryWfResult, queryWfErr := db.MysqlEngine.Context(ctx).QueryString("select id from proc_run_workflow where proc_ins_id=?", procInsId)
	if queryWfErr != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, queryWfErr)
		return
	}
	if len(queryWfResult) == 0 {
		err = exterror.Catch(exterror.New().DatabaseQueryEmptyError, fmt.Errorf("proc_run_workflow proc_ins_id=%s", procInsId))
		return
	}
	workflowId = queryWfResult[0]["id"]
	if procInsNodeId == "" {
		return
	}
	queryWnResult, queryWnErr := db.MysqlEngine.Context(ctx).QueryString("select id from proc_run_node where workflow_id=? and proc_ins_node_id=?", workflowId, procInsNodeId)
	if queryWnErr != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, queryWnErr)
		return
	}
	if len(queryWfResult) == 0 {
		err = exterror.Catch(exterror.New().DatabaseQueryEmptyError, fmt.Errorf("proc_run_node workflow_id=%s proc_ins_node_id=%s", workflowId, procInsNodeId))
		return
	}
	nodeId = queryWnResult[0]["id"]
	return
}

func AddWorkflowOperation(ctx context.Context, operation *models.ProcRunOperation) (lastInsertId int64, err error) {
	execResult, execErr := db.MysqlEngine.Context(ctx).Exec("insert into proc_run_operation(workflow_id,node_id,operation,status,message,created_by,created_time) values (?,?,?,?,?,?,?)",
		operation.WorkflowId, operation.NodeId, operation.Operation, "wait", operation.Message, operation.CreatedBy, time.Now())
	if execErr != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, execErr)
		return
	}
	lastInsertId, _ = execResult.LastInsertId()
	return
}

func GetProcCacheData(ctx context.Context, procInsId string) (procCacheDataRows []*models.ProcDataCache, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_data_cache where proc_ins_id=?", procInsId).Find(&procCacheDataRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, fmt.Errorf("query proc_data_cache with proc_ins_id=%s,%s", procInsId, err.Error()))
		return
	}
	return
}

func GetProcContextBindNodeType(ctx context.Context, procDefId, bindNodeId string) (sourceNodeDef *models.ProcDefNode, err error) {
	var nodeDefRows []*models.ProcDefNode
	err = db.MysqlEngine.Context(ctx).SQL("select id,node_id,proc_def_id,name,status,node_type,service_name,dynamic_bind,bind_node_id,risk_check,routine_expression,ordered_no from proc_def_node where proc_def_id=? and node_id=?", procDefId, bindNodeId).Find(&nodeDefRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(nodeDefRows) == 0 {
		err = fmt.Errorf("can not find context bind node:%s in procDef:%s ", bindNodeId, procDefId)
		return
	}
	sourceNodeDef = nodeDefRows[0]
	return
}

func GetWorkflowNodeByReq(ctx context.Context, reqId string) (runNode *models.TaskCallbackReqQuery, err error) {
	var nodeRows []*models.TaskCallbackReqQuery
	err = db.MysqlEngine.Context(ctx).SQL("select t1.is_completed,t3.id as work_node_id,t3.workflow_id from proc_ins_node_req t1 left join proc_ins_node t2 on t1.proc_ins_node_id=t2.id left join proc_run_node t3 on t3.proc_ins_node_id=t2.id where t1.id=?", reqId).Find(&nodeRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(nodeRows) == 0 {
		err = exterror.Catch(exterror.New().DatabaseQueryEmptyError, fmt.Errorf("proc_run_node req_id=%s", reqId))
		return
	}
	runNode = nodeRows[0]
	return
}

func GetSimpleProcNodeReq(ctx context.Context, procInsNodeId, reqId, procRunNodeId string) (procReq *models.ProcInsNodeReq, err error) {
	var reqRows []*models.ProcInsNodeReq
	if procRunNodeId != "" {
		err = db.MysqlEngine.Context(ctx).SQL("select * from proc_ins_node_req where proc_ins_node_id in (select proc_ins_node_id from proc_run_node where id=?) order by created_time desc", procRunNodeId).Find(&reqRows)
	} else if procInsNodeId != "" {
		err = db.MysqlEngine.Context(ctx).SQL("select * from proc_ins_node_req where proc_ins_node_id=? order by created_time desc", procInsNodeId).Find(&reqRows)
	} else if reqId != "" {
		err = db.MysqlEngine.Context(ctx).SQL("select * from proc_ins_node_req where id=?", reqId).Find(&reqRows)
	}
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(reqRows) > 0 {
		procReq = reqRows[0]
	}
	return
}

func UpdateProcCacheData(ctx context.Context, procInsId string, taskFormList []*models.PluginTaskFormDto) (err error) {
	var cacheDataRows []*models.ProcDataCache
	err = db.MysqlEngine.Context(ctx).SQL("select id,entity_id,entity_data_id,entity_type_id,data_value from proc_data_cache where proc_ins_id=?", procInsId).Find(&cacheDataRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	var actions []*db.ExecAction
	nowTime := time.Now()
	for _, taskForm := range taskFormList {
		for _, entityObj := range taskForm.FormDataEntities {
			existId := ""
			existDataValue := ""
			tmpEntityTypeId := fmt.Sprintf("%s:%s", entityObj.PackageName, entityObj.EntityName)
			for _, row := range cacheDataRows {
				if row.EntityTypeId == tmpEntityTypeId && row.EntityDataId == entityObj.EntityDataId {
					existId = row.Id
					existDataValue = row.DataValue
					break
				}
			}
			if existId != "" {
				actions = append(actions, &db.ExecAction{Sql: "update proc_data_cache set data_value=?,updated_time=? where id=?", Param: []interface{}{entityObj.GetAttrDataValueString(existDataValue), nowTime, existId}})
			} else {
				actions = append(actions, &db.ExecAction{Sql: "insert into proc_data_cache(id,proc_ins_id,entity_id,entity_data_id,entity_data_name,entity_type_id,full_data_id,data_value,created_time) values (?,?,?,?,?,?,?,?,?)", Param: []interface{}{
					"p_cache_" + guid.CreateGuid(), procInsId, entityObj.Oid, entityObj.EntityDataId, entityObj.EntityDataId, tmpEntityTypeId, entityObj.FullEntityDataId, entityObj.GetAttrDataValueString(""), nowTime,
				}})
			}
		}
	}
	if len(actions) > 0 {
		err = db.Transaction(actions, ctx)
		if err != nil {
			log.Logger.Error("UpdateProcCacheData fail", log.Error(err))
			err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		}
	}
	return
}

func GetProcNodeAllowOptions(ctx context.Context, procDefId, ProcNodeDefId string) (options []string, err error) {
	var nextNodes []*models.ProcDefNode
	err = db.MysqlEngine.Context(ctx).SQL("select id,node_id,name,node_type from proc_def_node where id in (select target from proc_def_node_link where source=?)", ProcNodeDefId).Find(&nextNodes)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	options = []string{}
	if len(nextNodes) == 0 {
		return
	}
	if len(nextNodes) > 1 {
		err = fmt.Errorf("node:%s have more than one next nodes", ProcNodeDefId)
		return
	}
	if nextNodes[0].NodeType != models.JobDecisionType {
		return
	}
	var linkRows []*models.ProcDefNodeLink
	err = db.MysqlEngine.Context(ctx).SQL("select id,name from proc_def_node_link where proc_def_id=? and source=?", procDefId, nextNodes[0].Id).Find(&linkRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	for _, row := range linkRows {
		options = append(options, row.Name)
	}
	return
}

func GetProcDataNodeExpression(expression string) (result []*models.ProcDataNodeExprObj, err error) {
	for _, subExpr := range strings.Split(expression, "#DME#") {
		if subExpr == "" {
			continue
		}
		subSplit := strings.Split(subExpr, "#DMEOP#")
		if len(subSplit) != 2 {
			err = fmt.Errorf("data nodeType expression:%s illegal", subExpr)
			break
		}
		result = append(result, &models.ProcDataNodeExprObj{Expression: subSplit[0], Operation: subSplit[1]})
	}
	return
}

func RewriteProcInsEntityData(ctx context.Context, procInsId string, rewriteList []*models.RewriteEntityDataObj) (err error) {
	var actions []*db.ExecAction
	for _, v := range rewriteList {
		actions = append(actions, &db.ExecAction{Sql: "update proc_data_binding set entity_data_id=?,entity_data_name=? where proc_ins_id=? and entity_id=?", Param: []interface{}{v.Nid, v.DisplayName, procInsId, v.Oid}})
		actions = append(actions, &db.ExecAction{Sql: "update proc_data_cache set entity_data_id=?,entity_data_name=? where proc_ins_id=? and entity_id=?", Param: []interface{}{v.Nid, v.DisplayName, procInsId, v.Oid}})
	}
	if len(actions) > 0 {
		err = db.Transaction(actions, ctx)
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		}
	}
	return
}

func RewriteProcInsEntityDataNew(ctx context.Context, procInsId string, rewriteData *models.RewriteEntityDataObj) (err error) {
	var actions []*db.ExecAction
	actions = append(actions, &db.ExecAction{Sql: "update proc_data_binding set entity_data_id=?,entity_data_name=? where proc_ins_id=? and entity_id=?", Param: []interface{}{rewriteData.Nid, rewriteData.DisplayName, procInsId, rewriteData.Oid}})
	actions = append(actions, &db.ExecAction{Sql: "update proc_data_cache set entity_data_id=?,entity_data_name=? where proc_ins_id=? and entity_id=?", Param: []interface{}{rewriteData.Nid, rewriteData.DisplayName, procInsId, rewriteData.Oid}})
	for _, row := range rewriteData.ProcDataCacheList {
		actions = append(actions, &db.ExecAction{Sql: "update proc_data_cache set data_value=? where id=?", Param: []interface{}{row.DataValue, row.Id}})
	}
	if len(actions) > 0 {
		err = db.Transaction(actions, ctx)
		if err != nil {
			log.Logger.Error("RewriteProcInsEntityDataNew fail", log.Error(err))
			err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		}
	}
	return
}

func QueryProcInsPage(ctx context.Context, param *models.QueryProcPageParam, userRoles []string) (result *models.QueryProcPageResponse, err error) {
	result = &models.QueryProcPageResponse{PageInfo: &models.PageInfo{}, Contents: []*models.ProcInsDetail{}}
	var procInsRows []*models.ProcIns
	baseSql := "select * from proc_ins"
	var filterSqlList []string
	var filterParams []interface{}
	var scheduleInsFlag bool
	if param.Id != "" {
		filterSqlList = append(filterSqlList, "id=?")
		filterParams = append(filterParams, param.Id)
	} else {
		if param.EntityDisplayName != "" {
			filterSqlList = append(filterSqlList, "entity_data_name like ?")
			filterParams = append(filterParams, "%"+param.EntityDisplayName+"%")
		}
		if param.ProcInstName != "" {
			filterSqlList = append(filterSqlList, "proc_def_name like ?")
			filterParams = append(filterParams, "%"+param.ProcInstName+"%")
		}
		if param.ProcDefId != "" {
			filterSqlList = append(filterSqlList, "proc_def_id=?")
			filterParams = append(filterParams, param.ProcDefId)
		}
		if param.Status != "" {
			if strings.HasPrefix(param.Status, "InProgress(") {
				nodeStatus := param.Status[11 : len(param.Status)-1]
				param.Status = models.JobStatusRunning
				filterSqlList = append(filterSqlList, "id in (select proc_ins_id from proc_ins_node where status in (?))")
				filterParams = append(filterParams, nodeStatus)
			} else if param.Status == models.JobStatusRunning {
				filterSqlList = append(filterSqlList, "id not in (select proc_ins_id from proc_ins_node where status in ('Faulted','Timeouted'))")
			}
			filterSqlList = append(filterSqlList, "status=?")
			filterParams = append(filterParams, param.Status)
		}
		if param.Operator != "" {
			filterSqlList = append(filterSqlList, "created_by=?")
			filterParams = append(filterParams, param.Operator)
			if param.Operator == "systemCron" {
				scheduleInsFlag = true
			}
		} else {
			filterSqlList = append(filterSqlList, "created_by!='systemCron'")
		}
		if param.StartTime != "" {
			filterSqlList = append(filterSqlList, "created_time>=?")
			filterParams = append(filterParams, param.StartTime)
		}
		if param.EndTime != "" {
			filterSqlList = append(filterSqlList, "created_time<=?")
			filterParams = append(filterParams, param.EndTime)
		}
	}
	if param.SubProc == "main" {
		filterSqlList = append(filterSqlList, "proc_def_id in (select id from proc_def where sub_proc=0)")
	} else if param.SubProc == "sub" {
		filterSqlList = append(filterSqlList, "proc_def_id in (select id from proc_def where sub_proc=1)")
	}
	filterSqlList = append(filterSqlList, "proc_def_id in (select proc_def_id from proc_def_permission where permission=? and role_id in ('"+strings.Join(userRoles, "','")+"'))")
	filterParams = append(filterParams, models.PermissionTypeUSE)

	if len(filterSqlList) > 0 {
		baseSql += " where " + strings.Join(filterSqlList, " and ")
	}
	baseSql += " order by created_time desc"
	if param.Pageable != nil && param.Pageable.PageSize > 0 {
		result.PageInfo.StartIndex = param.Pageable.StartIndex
		result.PageInfo.PageSize = param.Pageable.PageSize
		result.PageInfo.TotalRows = db.QueryCount(baseSql, filterParams...)
		baseSql += fmt.Sprintf(" limit %d,%d", param.Pageable.StartIndex, param.Pageable.PageSize)
	}
	err = db.MysqlEngine.Context(ctx).SQL(baseSql, filterParams...).Find(&procInsRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	var procDefList, procInsIdList, procInProgressIdList []string
	for _, row := range procInsRows {
		procDefList = append(procDefList, row.ProcDefId)
		procInsIdList = append(procInsIdList, row.Id)
		if row.Status == models.JobStatusRunning {
			procInProgressIdList = append(procInProgressIdList, row.Id)
		}
	}
	procDefVersionMap, getVersionErr := getProcInsVersionMap(ctx, procDefList)
	if getVersionErr != nil {
		err = getVersionErr
		return
	}
	procInsParentMap := make(map[string]*models.ParentProcInsObj)
	if param.SubProc == "sub" {
		procInsParentMap, err = getProcInsParentMap(ctx, procInsIdList)
		if err != nil {
			return
		}
	}
	procInsNodeStatusMap := make(map[string]string)
	procInsNodeStatusMap, err = getProcInsNodeStatus(ctx, procInProgressIdList)
	scheduleConfigNameMap := make(map[string]*models.ScheduleInsQueryRow)
	if scheduleInsFlag {
		scheduleConfigNameMap, err = getScheduleProcInsConfigMap(ctx, procInsIdList)
	}
	for _, row := range procInsRows {
		if nodeStatus, ok := procInsNodeStatusMap[row.Id]; ok {
			row.Status = fmt.Sprintf("%s(%s)", row.Status, nodeStatus)
		}
		resultObj := models.ProcInsDetail{
			Id:                row.Id,
			EntityDataId:      row.EntityDataId,
			EntityTypeId:      row.EntityTypeId,
			EntityDisplayName: row.EntityDataName,
			Operator:          row.CreatedBy,
			ProcDefId:         row.ProcDefId,
			ProcInstKey:       row.Id,
			ProcInstName:      row.ProcDefName,
			Status:            row.Status,
			CreatedTime:       row.CreatedTime.Format(models.DateTimeFormat),
			Version:           procDefVersionMap[row.ProcDefId],
			ParentProcIns:     procInsParentMap[row.Id],
			UpdatedBy:         row.UpdatedBy,
			UpdatedTime:       row.UpdatedTime.Format(models.DateTimeFormat),
		}
		if scheduleInsFlag {
			if scheduleMsg, ok := scheduleConfigNameMap[row.Id]; ok {
				resultObj.Operator = scheduleMsg.CreatedBy
				resultObj.ScheduleJobName = scheduleMsg.Name
			}
		}
		result.Contents = append(result.Contents, &resultObj)
	}
	return
}

func GetLatestProcDefByKey(ctx context.Context, procDefKey string) (procDefObj *models.ProcDef, err error) {
	var procDefRows []*models.ProcDef
	err = db.MysqlEngine.Context(ctx).SQL("select * from proc_def where `key`=? and status='deployed'", procDefKey).Find(&procDefRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(procDefRows) == 0 {
		err = fmt.Errorf("can not find proc def with key:%s ", procDefKey)
		return
	}
	latestVer := procDefRows[0].Version
	for _, v := range procDefRows {
		if tools.CompareVersion(v.Version, latestVer) {
			latestVer = v.Version
		}
	}
	for _, v := range procDefRows {
		if v.Version == latestVer {
			procDefObj = v
			break
		}
	}
	return
}

func CreateProcInsEvent(ctx context.Context, param *models.ProcStartEventParam, procDefObj *models.ProcDef) (eventId int64, err error) {
	execResult, execErr := db.MysqlEngine.Context(ctx).Exec("insert into proc_ins_event(event_seq_no,event_type,operation_data,operation_key,operation_user,proc_def_id,source_plugin,status,created_time) values (?,?,?,?,?,?,?,?,?)",
		param.EventSeqNo, param.EventType, param.OperationData, param.OperationKey, param.OperationUser, procDefObj.Id, param.SourceSubSystem, models.ProcEventStatusCreated, time.Now())
	if execErr != nil {
		err = fmt.Errorf("insert proc ins event data fail,%s ", execErr.Error())
	} else {
		eventId, _ = execResult.LastInsertId()
	}
	return
}

func GetProcNodeEndTime(ctx context.Context, procInsNodeId string) (endTime string, err error) {
	var procRunNodeRows []*models.ProcRunNode
	err = db.MysqlEngine.Context(ctx).SQL("select `output` from proc_run_node where proc_ins_node_id=?", procInsNodeId).Find(&procRunNodeRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if len(procRunNodeRows) == 0 {
		err = fmt.Errorf("can not find proc run node with procInsNode=%s ", procInsNodeId)
		return
	}
	endTime = procRunNodeRows[0].Output
	return
}

func GetProcNodeNextChoose(ctx context.Context, procInsNodeId string) (nextChooseList []string, err error) {
	var linkRows []*models.ProcDefNodeLink
	err = db.MysqlEngine.Context(ctx).SQL("select id,name from proc_def_node_link where source in (select proc_def_node_id from proc_ins_node where id=?)", procInsNodeId).Find(&linkRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	for _, row := range linkRows {
		nextChooseList = append(nextChooseList, row.Name)
	}
	return
}

func getProcInsVersionMap(ctx context.Context, procDefList []string) (procDefMap map[string]string, err error) {
	procDefMap = make(map[string]string)
	if len(procDefList) == 0 {
		return
	}
	filterSql, filterParam := db.CreateListParams(procDefList, "")
	var procDefRows []*models.ProcDef
	err = db.MysqlEngine.Context(ctx).SQL("select id,`version` from proc_def where id in ("+filterSql+")", filterParam...).Find(&procDefRows)
	if err != nil {
		err = fmt.Errorf("query proc def version fail,%s ", err.Error())
		return
	}
	for _, row := range procDefRows {
		procDefMap[row.Id] = row.Version
	}
	return
}

func GetProcInsRootEntityData(ctx context.Context, procInsId string) (entityDataId, entityTypeId, rootExpr string, err error) {
	queryResult, queryErr := db.MysqlEngine.Context(ctx).QueryString("select t1.entity_data_id,t1.entity_type_id,t2.root_entity from proc_ins t1 left join proc_def t2 on t1.proc_def_id=t2.id where t1.id=?", procInsId)
	if queryErr != nil {
		err = fmt.Errorf("query proc ins %s root entity data fail,%s ", procInsId, err.Error())
		return
	}
	if len(queryResult) == 0 {
		err = fmt.Errorf("query proc ins %s root entity fail with empty data", procInsId)
		return
	}
	entityDataId = queryResult[0]["entity_data_id"]
	entityTypeId = queryResult[0]["entity_type_id"]
	rootExpr = queryResult[0]["root_entity"]
	return
}

func CheckProcInsUserPermission(ctx context.Context, userRoleList []string, procInsId string) (legal bool, err error) {
	var permissionRows []*models.ProcDefPermission
	err = db.MysqlEngine.Context(ctx).SQL("select role_id from proc_def_permission where proc_def_id in (select proc_def_id from proc_ins where id=?) and permission='USE'", procInsId).Find(&permissionRows)
	if err != nil {
		err = fmt.Errorf("query proc permission data fail,%s ", err.Error())
		return
	}
	for _, row := range permissionRows {
		for _, userRole := range userRoleList {
			if row.RoleId == userRole {
				legal = true
				break
			}
		}
		if legal {
			break
		}
	}
	return
}

func UpdateProcRunNodeSubProc(ctx context.Context, procRunNodeId string, subProcWorkflowList []*models.ProcRunNodeSubProc, dataBinding []*models.ProcDataBinding) (err error) {
	var actions []*db.ExecAction
	nowTime := time.Now()
	actions = append(actions, &db.ExecAction{Sql: "delete from proc_run_node_sub_proc where proc_run_node_id=?", Param: []interface{}{procRunNodeId}})
	for _, row := range subProcWorkflowList {
		actions = append(actions, &db.ExecAction{Sql: "insert into proc_run_node_sub_proc(proc_run_node_id,workflow_id,entity_type_id,entity_data_id,created_time) values (?,?,?,?,?)", Param: []interface{}{
			procRunNodeId, row.WorkflowId, row.EntityTypeId, row.EntityDataId, nowTime,
		}})
	}
	for _, dataBind := range dataBinding {
		if dataBind.SubProcInsId != "" {
			actions = append(actions, &db.ExecAction{Sql: "update proc_data_binding set sub_proc_ins_id=? where id=?", Param: []interface{}{dataBind.SubProcInsId, dataBind.Id}})
		}
	}
	err = db.Transaction(actions, ctx)
	if err != nil {
		log.Logger.Error("UpdateProcRunNodeSubProc fail", log.Error(err))
	}
	return
}

func GetSubProcResult(ctx context.Context, procRunNodeId string) (resultRows []*models.ProcSubProcQueryRow, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select t1.proc_run_node_id,t1.workflow_id,t1.entity_type_id,t1.entity_data_id,t2.status,t2.error_message,t2.proc_ins_id from proc_run_node_sub_proc t1 left join proc_run_workflow t2 on t1.workflow_id=t2.id where t1.proc_run_node_id=?", procRunNodeId).Find(&resultRows)
	return
}

func CheckSubProcStart(ctx context.Context, sessionId string) (isSubProcSession bool, err error) {
	if sessionId == "" {
		return
	}
	queryResult, queryErr := db.MysqlEngine.Context(ctx).QueryString("select proc_def_id from proc_data_preview where sub_session_id=?", sessionId)
	if queryErr != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, queryErr)
		return
	}
	if len(queryResult) > 0 {
		isSubProcSession = true
	}
	return
}

func getProcInsParentMap(ctx context.Context, procInsIdList []string) (procInsParentMap map[string]*models.ParentProcInsObj, err error) {
	procInsParentMap = make(map[string]*models.ParentProcInsObj)
	if len(procInsIdList) == 0 {
		return
	}
	filterSql, filterParam := db.CreateListParams(procInsIdList, "")
	var procInsNodeRows []*models.ParentProcInsObj
	err = db.MysqlEngine.Context(ctx).SQL("select t1.id,t2.proc_ins_id,t3.proc_def_name,t4.`version` from proc_ins t1 left join proc_ins_node t2 on t1.parent_ins_node_id=t2.id left join proc_ins t3 on t2.proc_ins_id=t3.id left join proc_def t4 on t3.proc_def_id=t4.id where t1.id in ("+filterSql+")", filterParam...).Find(&procInsNodeRows)
	if err != nil {
		err = fmt.Errorf("query proc def version fail,%s ", err.Error())
		return
	}
	for _, row := range procInsNodeRows {
		procInsParentMap[row.Id] = row
	}
	return
}

func getProcInsNodeStatus(ctx context.Context, procInsIdList []string) (procInsNodeStatusMap map[string]string, err error) {
	procInsNodeStatusMap = make(map[string]string)
	if len(procInsIdList) == 0 {
		return
	}
	filterSql, filterParam := db.CreateListParams(procInsIdList, "")
	var procInsNodeRows []*models.ProcInsNode
	err = db.MysqlEngine.Context(ctx).SQL("select proc_ins_id,status from proc_ins_node where proc_ins_id in ("+filterSql+") and status in ('Faulted','Timeouted')", filterParam...).Find(&procInsNodeRows)
	if err != nil {
		err = fmt.Errorf("query proc def version fail,%s ", err.Error())
		return
	}
	for _, row := range procInsNodeRows {
		if row.Status == models.JobStatusFail {
			procInsNodeStatusMap[row.ProcInsId] = row.Status
			continue
		}
		if row.Status == models.JobStatusTimeout {
			if procInsNodeStatusMap[row.ProcInsId] == models.JobStatusFail {
				continue
			}
			procInsNodeStatusMap[row.ProcInsId] = row.Status
		}
	}
	return
}

func GetRunningProcInsSubWorkflow(ctx context.Context, procInsId, procNodeId string) (workflowIdList []string, err error) {
	var workflowRows []*models.ProcRunWorkflow
	if procNodeId != "" {
		err = db.MysqlEngine.Context(ctx).SQL("select id from proc_run_workflow where status='InProgress' and proc_ins_id in (select sub_proc_ins_id from proc_data_binding where proc_ins_id=? and proc_ins_node_id=? and sub_proc_ins_id<>'')", procInsId, procNodeId).Find(&workflowRows)
	} else {
		err = db.MysqlEngine.Context(ctx).SQL("select id from proc_run_workflow where status='InProgress' and proc_ins_id in (select sub_proc_ins_id from proc_data_binding where proc_ins_id=? and sub_proc_ins_id<>'')", procInsId).Find(&workflowRows)
	}
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	for _, row := range workflowRows {
		workflowIdList = append(workflowIdList, row.Id)
	}
	return
}

func getScheduleProcInsConfigMap(ctx context.Context, procInsIdList []string) (scheduleConfigNameMap map[string]*models.ScheduleInsQueryRow, err error) {
	scheduleConfigNameMap = make(map[string]*models.ScheduleInsQueryRow)
	if len(procInsIdList) == 0 {
		return
	}
	filterSql, filterParam := db.CreateListParams(procInsIdList, "")
	var procInsNodeRows []*models.ScheduleInsQueryRow
	err = db.MysqlEngine.Context(ctx).SQL("select t1.proc_ins_id,t1.created_by,t2.name from proc_schedule_job t1 left join proc_schedule_config t2 on t1.schedule_config_id=t2.id where t1.proc_ins_id in ("+filterSql+")", filterParam...).Find(&procInsNodeRows)
	if err != nil {
		err = fmt.Errorf("query proc def version fail,%s ", err.Error())
		return
	}
	for _, row := range procInsNodeRows {
		scheduleConfigNameMap[row.ProcInsId] = row
	}
	return
}
