package database

import (
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
)

func StatisticsServiceNames(ctx *gin.Context) (serviceNames []string, err error) {
	serviceNames = []string{}
	baseSql := db.CombineDBSql("select distinct service_name from proc_def_node where service_name!='' and id in (select proc_def_node_id from proc_ins_node)")
	err = db.MysqlEngine.Context(ctx).SQL(baseSql).Find(&serviceNames)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func StatisticsTasknodeBindingsEntity(ctx *gin.Context, serviceNameList []string) (result []*models.TasknodeBindsEntityData, err error) {
	result = []*models.TasknodeBindsEntityData{}
	filterSql, filterParams := db.CreateListParams(serviceNameList, "")
	baseSql := db.CombineDBSql(`SELECT pdb.entity_data_id,pdb.entity_data_name from proc_ins_node_req_param pinrp LEFT JOIN proc_ins_node_req pinr on pinrp.req_id=pinr.id LEFT JOIN proc_ins_node pin on pinr.proc_ins_node_id=pin.id LEFT JOIN proc_data_binding pdb on pin.id=pdb.proc_ins_node_id WHERE pdb.proc_ins_node_id in `,
		` (SELECT DISTINCT pin1.id from proc_ins_node_req_param pinrp1 LEFT JOIN proc_ins_node_req pinr1 on pinrp1.req_id=pinr1.id LEFT JOIN proc_ins_node pin1 on pinr1.proc_ins_node_id=pin1.id LEFT JOIN proc_def_node pdn1 on pin1.proc_def_node_id=pdn1.id `)
	if filterSql != "" {
		baseSql = db.CombineDBSql(baseSql, " WHERE pdn1.service_name in (", filterSql, ")")
	}
	baseSql = db.CombineDBSql(baseSql, ") GROUP BY pdb.entity_data_id,pdb.entity_data_name")
	err = db.MysqlEngine.Context(ctx).SQL(baseSql, filterParams...).Find(&result)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}
