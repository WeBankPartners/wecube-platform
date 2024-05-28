package database

import (
	"fmt"
	"sort"

	"github.com/WeBankPartners/go-common-lib/cipher"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
)

var (
	ProcExecCompleted  = "Completed"
	ProcExecInProgress = "InProgress"
	ProcExecFaulted    = "Faulted"
	ProcExecTotal      = "Total"
)

// 插件注册列表
func StatisticsServiceNames(ctx *gin.Context) (serviceNames []string, err error) {
	serviceNames = []string{}
	baseSql := db.CombineDBSql("SELECT DISTINCT service_name FROM proc_def_node WHERE service_name!='' AND id IN (SELECT proc_def_node_id FROM proc_ins_node)")
	err = db.MysqlEngine.Context(ctx).SQL(baseSql).Find(&serviceNames)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

// 插件服务-数据对象
func StatisticsBindingsEntityByService(ctx *gin.Context, serviceNameList []string) (result []*models.TasknodeBindsEntityData, err error) {
	result = []*models.TasknodeBindsEntityData{}
	filterSql, filterParams := db.CreateListParams(serviceNameList, "")
	/*
		baseSql := db.CombineDBSql(`SELECT pdb.entity_data_id,pdb.entity_data_name FROM proc_ins_node_req_param pinrp LEFT JOIN proc_ins_node_req pinr ON pinrp.req_id=pinr.id LEFT JOIN proc_ins_node pin ON pinr.proc_ins_node_id=pin.id LEFT JOIN proc_data_binding pdb ON pin.id=pdb.proc_ins_node_id WHERE pdb.proc_ins_node_id IN `,
			` (SELECT DISTINCT pin1.id FROM proc_ins_node_req_param pinrp1 LEFT JOIN proc_ins_node_req pinr1 ON pinrp1.req_id=pinr1.id LEFT JOIN proc_ins_node pin1 ON pinr1.proc_ins_node_id=pin1.id LEFT JOIN proc_def_node pdn1 ON pin1.proc_def_node_id=pdn1.id `)
	*/
	baseSql := db.CombineDBSql(`SELECT pdb.entity_data_id,pdb.entity_data_name FROM proc_data_binding pdb WHERE pdb.proc_ins_node_id IN `,
		` (SELECT DISTINCT pin.id FROM proc_ins_node pin LEFT JOIN proc_def_node pdn ON pin.proc_def_node_id=pdn.id `)
	if filterSql != "" {
		baseSql = db.CombineDBSql(baseSql, " WHERE pdn.service_name IN (", filterSql, ")")
	}
	baseSql = db.CombineDBSql(baseSql, ") GROUP BY pdb.entity_data_id,pdb.entity_data_name")
	err = db.MysqlEngine.Context(ctx).SQL(baseSql, filterParams...).Find(&result)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

// 任务节点查询
func StatisticsTasknodes(ctx *gin.Context, procDefIdList []string) (result []*models.Tasknode, err error) {
	result = []*models.Tasknode{}
	filterSql, filterParams := db.CreateListParams(procDefIdList, "")
	baseSql := db.CombineDBSql(`SELECT node_id,id,name,node_type,proc_def_id,service_name,service_name FROM proc_def_node `)
	if filterSql != "" {
		baseSql = db.CombineDBSql(baseSql, " WHERE proc_def_id in (", filterSql, ")")
	}
	err = db.MysqlEngine.Context(ctx).SQL(baseSql, filterParams...).Find(&result)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

// 编排节点-数据对象
func StatisticsBindingsEntityByNode(ctx *gin.Context, nodeList []string) (result []*models.TasknodeBindsEntityData, err error) {
	result = []*models.TasknodeBindsEntityData{}
	filterSql, filterParams := db.CreateListParams(nodeList, "")
	/*
		baseSql := db.CombineDBSql(`SELECT pdb.entity_data_id,pdb.entity_data_name FROM proc_ins_node_req_param pinrp LEFT JOIN proc_ins_node_req pinr ON pinrp.req_id=pinr.id LEFT JOIN proc_ins_node pin ON pinr.proc_ins_node_id=pin.id LEFT JOIN proc_data_binding pdb ON pin.id=pdb.proc_ins_node_id WHERE pdb.proc_ins_node_id IN `,
			` (SELECT DISTINCT pin1.id FROM proc_ins_node_req_param pinrp1 LEFT JOIN proc_ins_node_req pinr1 ON pinrp1.req_id=pinr1.id LEFT JOIN proc_ins_node pin1 ON pinr1.proc_ins_node_id=pin1.id LEFT JOIN proc_def_node pdn1 ON pin1.proc_def_node_id=pdn1.id `)
	*/
	baseSql := db.CombineDBSql(`SELECT pdb.entity_data_id,pdb.entity_data_name FROM proc_data_binding pdb WHERE pdb.proc_ins_node_id IN `,
		` (SELECT DISTINCT pin.id FROM proc_ins_node pin LEFT JOIN proc_def_node pdn ON pin.proc_def_node_id=pdn.id `)
	if filterSql != "" {
		baseSql = db.CombineDBSql(baseSql, " WHERE pdn.id IN (", filterSql, ")")
	}
	baseSql = db.CombineDBSql(baseSql, ") GROUP BY pdb.entity_data_id,pdb.entity_data_name")
	err = db.MysqlEngine.Context(ctx).SQL(baseSql, filterParams...).Find(&result)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

// 编排-查询
func StatisticsProcessExec(ctx *gin.Context, reqParam *models.StatisticsProcessExecReq) (result []*models.StatisticsProcessExecResp, err error) {
	result = []*models.StatisticsProcessExecResp{}
	var statisticsProcExecCnt []*models.StatisticsProcExecCnt
	queryParams := []interface{}{}

	filterSql, reqProcDefIds := db.CreateListParams(reqParam.ProcDefIds, "")
	baseSql := db.CombineDBSql("SELECT proc_def_id,status,count(1) AS cnt FROM proc_ins WHERE 1=1 ")
	if filterSql != "" {
		baseSql = db.CombineDBSql(baseSql, " AND proc_def_id IN (", filterSql, ")")
		queryParams = append(queryParams, reqProcDefIds...)
	}
	if reqParam.StartDate != "" {
		baseSql = db.CombineDBSql(baseSql, " AND created_time >= ?")
		queryParams = append(queryParams, reqParam.StartDate)
	}
	if reqParam.EndDate != "" {
		baseSql = db.CombineDBSql(baseSql, " AND created_time <= ?")
		queryParams = append(queryParams, reqParam.EndDate)
	}
	baseSql = db.CombineDBSql(baseSql, " GROUP BY proc_def_id,status ORDER BY proc_def_id")

	if reqParam.Pageable != nil {
		if reqParam.Pageable.PageSize != 0 {
			baseSql = db.CombineDBSql(baseSql, " LIMIT ?")
			queryParams = append(queryParams, reqParam.Pageable.PageSize)
		}
	}

	err = db.MysqlEngine.Context(ctx).SQL(baseSql, queryParams...).Find(&statisticsProcExecCnt)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	procDefIdMapStatusCnt := make(map[string]map[string]int)
	// 计算每个 procExec 每种状态的总量
	for _, procExecCnt := range statisticsProcExecCnt {
		if _, ok := procDefIdMapStatusCnt[procExecCnt.ProcDefId]; !ok {
			procDefIdMapStatusCnt[procExecCnt.ProcDefId] = make(map[string]int)
		}
		if procExecCnt.Status == ProcExecCompleted {
			procDefIdMapStatusCnt[procExecCnt.ProcDefId][ProcExecCompleted] += procExecCnt.Cnt
		} else if procExecCnt.Status == ProcExecInProgress {
			procDefIdMapStatusCnt[procExecCnt.ProcDefId][ProcExecInProgress] += procExecCnt.Cnt
		} else {
			procDefIdMapStatusCnt[procExecCnt.ProcDefId][ProcExecFaulted] += procExecCnt.Cnt
		}
	}

	// 计算每个 procExec 的总量
	for _, statisticsDataMap := range procDefIdMapStatusCnt {
		totalCnt := 0
		for _, count := range statisticsDataMap {
			totalCnt += count
		}
		statisticsDataMap[ProcExecTotal] = totalCnt
	}

	var queryProcDefIdList []string
	if len(reqParam.ProcDefIds) > 0 {
		queryProcDefIdList = reqParam.ProcDefIds
	} else {
		// 获取查询到的结果中的 procDefId 列表
		tmpProcDefIdMap := make(map[string]struct{})
		for _, procExecCnt := range statisticsProcExecCnt {
			tmpProcDefIdMap[procExecCnt.ProcDefId] = struct{}{}
		}
		for procDefId := range tmpProcDefIdMap {
			queryProcDefIdList = append(queryProcDefIdList, procDefId)
		}
	}

	// 查询 proc_def 以获取 proc name 和 version
	if len(queryProcDefIdList) > 0 {
		var procDefData []*models.ProcDef
		filterSql, filterParams := db.CreateListParams(queryProcDefIdList, "")
		baseSql = db.CombineDBSql("SELECT id,name,version FROM proc_def WHERE id IN (", filterSql, ") ORDER BY name,version")
		err = db.MysqlEngine.Context(ctx).SQL(baseSql, filterParams...).Find(&procDefData)
		if err != nil {
			err = exterror.Catch(exterror.New().DatabaseQueryError, err)
			return
		}

		procDefIdMapInfo := make(map[string]*models.ProcDef)
		for i, data := range procDefData {
			procDefIdMapInfo[data.Id] = procDefData[i]
		}

		for procDefId, info := range procDefIdMapInfo {
			if _, ok := procDefIdMapStatusCnt[procDefId]; !ok {
				procDefIdMapStatusCnt[procDefId] = make(map[string]int)
			}
			resultData := &models.StatisticsProcessExecResp{
				ProcDefId:                procDefId,
				ProcDefName:              fmt.Sprintf("%s%s", info.Name, info.Version),
				TotalCompletedInstances:  procDefIdMapStatusCnt[procDefId][ProcExecCompleted],
				TotalFaultedInstances:    procDefIdMapStatusCnt[procDefId][ProcExecFaulted],
				TotalInProgressInstances: procDefIdMapStatusCnt[procDefId][ProcExecInProgress],
				TotalInstances:           procDefIdMapStatusCnt[procDefId][ProcExecTotal],
			}
			result = append(result, resultData)

			sort.Slice(result, func(i int, j int) bool {
				return result[i].ProcDefName < result[j].ProcDefName
			})
		}
	}
	return
}

// 编排节点-查询
func StatisticsTasknodeExec(ctx *gin.Context, reqParam *models.StatisticsTasknodeExecReq) (result *models.StatisticsTasknodeExecResp, err error) {
	result = &models.StatisticsTasknodeExecResp{
		PageInfo: &models.PageInfo{},
		Contents: []*models.StatisticsTasknodeExecResult{},
	}
	var queryParams []interface{}
	procDefIdsFilterSql, procDefIdsFilterParams := db.CreateListParams(reqParam.ProcDefIds, "")
	taskNodeIdsFilterSql, taskNodeIdsFilterParams := db.CreateListParams(reqParam.TaskNodeIds, "")
	entityDataIdsFilterSql, entityDataIdsFilterParams := db.CreateListParams(reqParam.EntityDataIds, "")

	baseSql := db.CombineDBSql(`SELECT pdb.proc_def_id, pd.name AS proc_def_name, pd.version AS proc_def_version, pin.proc_def_node_id, pdn.name AS proc_def_node_name, pinrp.callback_id AS entity_data_id, pdb.entity_data_name, pinrp.data_value, COUNT(1) AS cnt FROM proc_ins_node_req_param pinrp
    LEFT JOIN proc_ins_node_req pinr ON pinrp.req_id=pinr.id
    LEFT JOIN proc_ins_node pin ON pinr.proc_ins_node_id=pin.id
    LEFT JOIN proc_data_binding pdb ON pdb.proc_ins_node_id=pin.id
    LEFT JOIN proc_def_node pdn ON pdn.id=pin.proc_def_node_id
    LEFT JOIN proc_def pd ON pd.id=pdb.proc_def_id
    WHERE pinrp.from_type='output' AND pinrp.name='errorCode'`)

	if reqParam.StartDate != "" {
		baseSql = db.CombineDBSql(baseSql, " AND pinrp.created_time >= ?")
		queryParams = append(queryParams, reqParam.StartDate)
	}
	if reqParam.EndDate != "" {
		baseSql = db.CombineDBSql(baseSql, " AND pinrp.created_time <= ?")
		queryParams = append(queryParams, reqParam.EndDate)
	}
	if procDefIdsFilterSql != "" {
		baseSql = db.CombineDBSql(baseSql, " AND pdb.proc_def_id IN (", procDefIdsFilterSql, ")")
		queryParams = append(queryParams, procDefIdsFilterParams...)
	}
	if taskNodeIdsFilterSql != "" {
		baseSql = db.CombineDBSql(baseSql, " AND pin.proc_def_node_id IN (", taskNodeIdsFilterSql, ")")
		queryParams = append(queryParams, taskNodeIdsFilterParams...)
	}
	if entityDataIdsFilterSql != "" {
		baseSql = db.CombineDBSql(baseSql, " AND pinrp.callback_id IN (", entityDataIdsFilterSql, ")")
		queryParams = append(queryParams, entityDataIdsFilterParams...)

		baseSql = db.CombineDBSql(baseSql, " AND pdb.entity_data_id IN (", entityDataIdsFilterSql, ")")
		queryParams = append(queryParams, entityDataIdsFilterParams...)
	}

	baseSql = db.CombineDBSql(baseSql, " GROUP BY pdb.proc_def_id, pd.name, pd.version, pin.proc_def_node_id, pdn.name, pinrp.callback_id, pdb.entity_data_name, pinrp.data_value")

	if reqParam.Pageable != nil {
		if reqParam.Pageable.PageSize != 0 {
			result.PageInfo.PageSize = reqParam.Pageable.PageSize
			result.PageInfo.StartIndex = 1

			baseSql = db.CombineDBSql(baseSql, " LIMIT ?")
			queryParams = append(queryParams, reqParam.Pageable.PageSize)
		}
	}

	queryResult := []*models.StatisticsTasknodeExecQueryResult{}
	err = db.MysqlEngine.Context(ctx).SQL(baseSql, queryParams...).Find(&queryResult)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	md5MapResultData := make(map[string]*models.StatisticsTasknodeExecResult)
	for _, data := range queryResult {
		strValForHash := data.StringValForHash()
		strValMd5 := cipher.Md5Encode(strValForHash)
		if _, ok := md5MapResultData[strValMd5]; !ok {
			md5MapResultData[strValMd5] = &models.StatisticsTasknodeExecResult{
				EntityDataId:   data.EntityDataId,
				EntityDataName: data.EntityDataName,
				NodeDefId:      data.NodeDefId,
				NodeDefName:    data.NodeDefName,
				ProcDefId:      data.ProcDefId,
				ProcDefName:    fmt.Sprintf("%s%s", data.ProcDefName, data.ProcDefVersion),
				ProcDefVersion: data.ProcDefVersion,
			}
		}
		if data.DataValue == "0" {
			// 成功
			md5MapResultData[strValMd5].SuccessCount += data.Cnt
		} else {
			// 失败
			md5MapResultData[strValMd5].FailureCount += data.Cnt
		}
	}

	for _, resultData := range md5MapResultData {
		result.Contents = append(result.Contents, resultData)
	}
	result.PageInfo.TotalRows = len(result.Contents)

	sort.Slice(result.Contents, func(i int, j int) bool {
		// return result.Contents[i].ProcDefName < result.Contents[j].ProcDefName
		if result.Contents[i].ProcDefName != result.Contents[j].ProcDefName {
			return result.Contents[i].ProcDefName < result.Contents[j].ProcDefName
		} else if result.Contents[i].NodeDefName != result.Contents[j].NodeDefName {
			return result.Contents[i].NodeDefName < result.Contents[j].NodeDefName
		} else if result.Contents[i].EntityDataId != result.Contents[j].EntityDataId {
			return result.Contents[i].EntityDataId < result.Contents[j].EntityDataId
		}
		return result.Contents[i].EntityDataName < result.Contents[j].EntityDataName
	})
	return
}

// 插件注册-查询
func StatisticsPluginExec(ctx *gin.Context, reqParam *models.StatisticsTasknodeExecReq) (result *models.StatisticsTasknodeExecResp, err error) {
	result = &models.StatisticsTasknodeExecResp{
		PageInfo: &models.PageInfo{},
		Contents: []*models.StatisticsTasknodeExecResult{},
	}
	var queryParams []interface{}
	serviceIdsFilterSql, serviceIdsFilterParams := db.CreateListParams(reqParam.ServiceIds, "")
	entityDataIdsFilterSql, entityDataIdsFilterParams := db.CreateListParams(reqParam.EntityDataIds, "")

	baseSql := db.CombineDBSql(`SELECT pdn.service_name, pinrp.callback_id AS entity_data_id, pdb.entity_data_name, pinrp.data_value, COUNT(1) AS cnt FROM proc_ins_node_req_param pinrp
    LEFT JOIN proc_ins_node_req pinr ON pinrp.req_id=pinr.id
    LEFT JOIN proc_ins_node pin ON pinr.proc_ins_node_id=pin.id
    LEFT JOIN proc_data_binding pdb ON pdb.proc_ins_node_id=pin.id
    LEFT JOIN proc_def_node pdn ON pdn.id=pin.proc_def_node_id
    WHERE pinrp.from_type='output' AND pinrp.name='errorCode'`)

	if reqParam.StartDate != "" {
		baseSql = db.CombineDBSql(baseSql, " AND pinrp.created_time >= ?")
		queryParams = append(queryParams, reqParam.StartDate)
	}
	if reqParam.EndDate != "" {
		baseSql = db.CombineDBSql(baseSql, " AND pinrp.created_time <= ?")
		queryParams = append(queryParams, reqParam.EndDate)
	}
	if serviceIdsFilterSql != "" {
		baseSql = db.CombineDBSql(baseSql, " AND pdn.service_name IN (", serviceIdsFilterSql, ")")
		queryParams = append(queryParams, serviceIdsFilterParams...)
	}
	if entityDataIdsFilterSql != "" {
		baseSql = db.CombineDBSql(baseSql, " AND pinrp.callback_id IN (", entityDataIdsFilterSql, ")")
		queryParams = append(queryParams, entityDataIdsFilterParams...)

		baseSql = db.CombineDBSql(baseSql, " AND pdb.entity_data_id IN (", entityDataIdsFilterSql, ")")
		queryParams = append(queryParams, entityDataIdsFilterParams...)
	}

	baseSql = db.CombineDBSql(baseSql, " GROUP BY pdn.service_name, pinrp.callback_id, pdb.entity_data_name, pinrp.data_value")

	if reqParam.Pageable != nil {
		if reqParam.Pageable.PageSize != 0 {
			result.PageInfo.PageSize = reqParam.Pageable.PageSize
			result.PageInfo.StartIndex = 1

			baseSql = db.CombineDBSql(baseSql, " LIMIT ?")
			queryParams = append(queryParams, reqParam.Pageable.PageSize)
		}
	}

	queryResult := []*models.StatisticsPluginExecQueryResult{}
	err = db.MysqlEngine.Context(ctx).SQL(baseSql, queryParams...).Find(&queryResult)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	md5MapResultData := make(map[string]*models.StatisticsTasknodeExecResult)
	for _, data := range queryResult {
		strValForHash := data.StringValForHash()
		strValMd5 := cipher.Md5Encode(strValForHash)
		if _, ok := md5MapResultData[strValMd5]; !ok {
			md5MapResultData[strValMd5] = &models.StatisticsTasknodeExecResult{
				EntityDataId:   data.EntityDataId,
				EntityDataName: data.EntityDataName,
				ServiceId:      data.ServiceName,
			}
		}
		if data.DataValue == "0" {
			// 成功
			md5MapResultData[strValMd5].SuccessCount += data.Cnt
		} else {
			// 失败
			md5MapResultData[strValMd5].FailureCount += data.Cnt
		}
	}

	for _, resultData := range md5MapResultData {
		result.Contents = append(result.Contents, resultData)
	}
	result.PageInfo.TotalRows = len(result.Contents)

	sort.Slice(result.Contents, func(i int, j int) bool {
		// return result.Contents[i].ServiceId < result.Contents[j].ServiceId
		if result.Contents[i].ServiceId != result.Contents[j].ServiceId {
			return result.Contents[i].ServiceId < result.Contents[j].ServiceId
		} else {
			if result.Contents[i].EntityDataId != result.Contents[j].EntityDataId {
				return result.Contents[i].EntityDataId < result.Contents[j].EntityDataId
			}
		}
		return result.Contents[i].EntityDataName < result.Contents[j].EntityDataName
	})
	return
}

// 编排节点-查询-详情
func StatisticsTasknodeExecDetails(ctx *gin.Context, reqParam *models.StatisticsTasknodeExecDetailsReq) (result []*models.StatisticsTasknodeExecDetailsResp, err error) {
	result = []*models.StatisticsTasknodeExecDetailsResp{}
	var queryParams []interface{}

	baseSql := db.CombineDBSql(`SELECT pdb.proc_def_id, pd.name AS proc_def_name, pd.version AS proc_def_version, pi.created_time AS proc_exec_date, pi.created_by AS proc_exec_oper, pi.status AS proc_exec_status, pin.proc_def_node_id, pdn.name AS proc_def_node_name, pin.created_time AS proc_node_exec_date, pin.status AS proc_node_status, pdb.entity_data_id, pdb.entity_data_name, pinr.created_time AS exec_date, pinr.id AS req_id,
       pinrp.id AS pinrp_id, pinrp.req_id AS pinrp_req_id, pinrp.from_type AS pinrp_param_type, pinrp.name AS pinrp_name, pinrp.data_type AS pinrp_data_type, pinrp.data_value AS pinrp_data_value, pinrp.callback_id AS pinrp_callback_id, pinrp.entity_type_id AS pinrp_entity_type_id FROM proc_ins_node_req_param pinrp
    LEFT JOIN proc_ins_node_req pinr ON pinrp.req_id=pinr.id
    LEFT JOIN proc_ins_node pin ON pinr.proc_ins_node_id=pin.id
    LEFT JOIN proc_data_binding pdb ON pdb.proc_ins_node_id=pin.id
    LEFT JOIN proc_def_node pdn ON pdn.id=pin.proc_def_node_id
    LEFT JOIN proc_def pd ON pd.id=pdb.proc_def_id
    LEFT JOIN proc_ins pi ON pi.id=pin.proc_ins_id WHERE 1=1 `)

	if reqParam.Status == ProcExecCompleted {
		baseSql = db.CombineDBSql(baseSql, " AND pinrp.data_value='0'")
	} else {
		baseSql = db.CombineDBSql(baseSql, " AND pinrp.data_value!='0'")
	}

	if reqParam.StartDate != "" {
		baseSql = db.CombineDBSql(baseSql, " AND pinrp.created_time >= ?")
		queryParams = append(queryParams, reqParam.StartDate)
	}
	if reqParam.EndDate != "" {
		baseSql = db.CombineDBSql(baseSql, " AND pinrp.created_time <= ?")
		queryParams = append(queryParams, reqParam.EndDate)
	}
	if reqParam.ProcDefId != "" {
		baseSql = db.CombineDBSql(baseSql, " AND pdb.proc_def_id = ?")
		queryParams = append(queryParams, reqParam.ProcDefId)
	}
	if reqParam.NodeDefId != "" {
		baseSql = db.CombineDBSql(baseSql, " AND pin.proc_def_node_id = ?")
		queryParams = append(queryParams, reqParam.NodeDefId)
	}
	if reqParam.EntityDataId != "" {
		baseSql = db.CombineDBSql(baseSql, " AND pinrp.callback_id = ?")
		queryParams = append(queryParams, reqParam.EntityDataId)

		baseSql = db.CombineDBSql(baseSql, " AND pdb.entity_data_id = ?")
		queryParams = append(queryParams, reqParam.EntityDataId)
	}
	if reqParam.EntityDataName != "" {
		baseSql = db.CombineDBSql(baseSql, " AND pdb.entity_data_name = ?")
		queryParams = append(queryParams, reqParam.EntityDataName)
	}

	baseSql = db.CombineDBSql(baseSql, " ORDER BY pinr.created_time,pinr.id,pinrp.from_type,pinrp.name")

	queryResult := []*models.StatisticsTasknodeExecDetailsQueryResult{}
	err = db.MysqlEngine.Context(ctx).SQL(baseSql, queryParams...).Find(&queryResult)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}

	// handle queryResult
	reqIdMapResultData := make(map[string]*models.StatisticsTasknodeExecDetailsResp)
	for _, data := range queryResult {
		if _, ok := reqIdMapResultData[data.ReqId]; !ok {
			reqIdMapResultData[data.ReqId] = &models.StatisticsTasknodeExecDetailsResp{
				EntityDataId: data.EntityDataId,
				ExecDate:     data.ExecDate,
				ExecParams:   []*models.TasknodeExecParam{},
				NodeDefId:    data.NodeDefId,
				NodeDefName:  data.NodeDefName,
				NodeExecDate: data.NodeExecDate,
				NodeStatus:   data.NodeStatus,
				ProcDefId:    data.ProcDefId,
				ProcDefName:  fmt.Sprintf("%s%s", data.ProcDefName, data.ProcDefVersion),
				ProcExecDate: data.ProcExecDate,
				ProcExecOper: data.ProcExecOper,
				ProcStatus:   data.ProcStatus,
				ReqId:        data.ReqId,
				ServiceId:    data.ServiceId,
			}
		}
		execParam := &models.TasknodeExecParam{
			EntityDataId:   data.CallbackId,
			EntityTypeId:   data.EntityTypeId,
			Id:             data.Id,
			ObjectId:       data.ObjectId,
			ParamDataType:  data.ParamDataType,
			ParamDataValue: data.ParamDataValue,
			ParamName:      data.ParamName,
			ParamType:      data.ParamType,
			RequestId:      data.RequestId,
		}
		reqIdMapResultData[data.ReqId].ExecParams = append(reqIdMapResultData[data.ReqId].ExecParams, execParam)
	}

	for _, resultData := range reqIdMapResultData {
		result = append(result, resultData)
	}

	sort.Slice(result, func(i int, j int) bool {
		if result[i].ExecDate != result[j].ExecDate {
			return result[i].ExecDate < result[j].ExecDate
		}
		return result[i].ReqId < result[j].ReqId
	})
	return
}

// 插件注册-查询-详情
func StatisticsPluginExecDetails(ctx *gin.Context, reqParam *models.StatisticsTasknodeExecDetailsReq) (result []*models.StatisticsTasknodeExecDetailsResp, err error) {
	result = []*models.StatisticsTasknodeExecDetailsResp{}
	// var queryParams []interface{}

	return
}
