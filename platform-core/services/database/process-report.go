package database

import (
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
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
