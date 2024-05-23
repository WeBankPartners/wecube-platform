package database

import (
	"fmt"
	"sort"

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

func StatisticsBindingsEntityByService(ctx *gin.Context, serviceNameList []string) (result []*models.TasknodeBindsEntityData, err error) {
	result = []*models.TasknodeBindsEntityData{}
	filterSql, filterParams := db.CreateListParams(serviceNameList, "")
	baseSql := db.CombineDBSql(`SELECT pdb.entity_data_id,pdb.entity_data_name FROM proc_ins_node_req_param pinrp LEFT JOIN proc_ins_node_req pinr ON pinrp.req_id=pinr.id LEFT JOIN proc_ins_node pin ON pinr.proc_ins_node_id=pin.id LEFT JOIN proc_data_binding pdb ON pin.id=pdb.proc_ins_node_id WHERE pdb.proc_ins_node_id IN `,
		` (SELECT DISTINCT pin1.id FROM proc_ins_node_req_param pinrp1 LEFT JOIN proc_ins_node_req pinr1 ON pinrp1.req_id=pinr1.id LEFT JOIN proc_ins_node pin1 ON pinr1.proc_ins_node_id=pin1.id LEFT JOIN proc_def_node pdn1 ON pin1.proc_def_node_id=pdn1.id `)
	if filterSql != "" {
		baseSql = db.CombineDBSql(baseSql, " WHERE pdn1.service_name IN (", filterSql, ")")
	}
	baseSql = db.CombineDBSql(baseSql, ") GROUP BY pdb.entity_data_id,pdb.entity_data_name")
	err = db.MysqlEngine.Context(ctx).SQL(baseSql, filterParams...).Find(&result)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

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

func StatisticsBindingsEntityByNode(ctx *gin.Context, nodeList []string) (result []*models.TasknodeBindsEntityData, err error) {
	result = []*models.TasknodeBindsEntityData{}
	filterSql, filterParams := db.CreateListParams(nodeList, "")
	baseSql := db.CombineDBSql(`SELECT pdb.entity_data_id,pdb.entity_data_name FROM proc_ins_node_req_param pinrp LEFT JOIN proc_ins_node_req pinr ON pinrp.req_id=pinr.id LEFT JOIN proc_ins_node pin ON pinr.proc_ins_node_id=pin.id LEFT JOIN proc_data_binding pdb ON pin.id=pdb.proc_ins_node_id WHERE pdb.proc_ins_node_id IN `,
		` (SELECT DISTINCT pin1.id FROM proc_ins_node_req_param pinrp1 LEFT JOIN proc_ins_node_req pinr1 ON pinrp1.req_id=pinr1.id LEFT JOIN proc_ins_node pin1 ON pinr1.proc_ins_node_id=pin1.id LEFT JOIN proc_def_node pdn1 ON pin1.proc_def_node_id=pdn1.id `)
	if filterSql != "" {
		baseSql = db.CombineDBSql(baseSql, " WHERE pdn1.node_id IN (", filterSql, ")")
	}
	baseSql = db.CombineDBSql(baseSql, ") GROUP BY pdb.entity_data_id,pdb.entity_data_name")
	err = db.MysqlEngine.Context(ctx).SQL(baseSql, filterParams...).Find(&result)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

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
				ProcDefName:              fmt.Sprintf("%s_%s", info.Name, info.Version),
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
