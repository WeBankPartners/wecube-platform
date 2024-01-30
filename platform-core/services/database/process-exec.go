package database

import (
	"context"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"strings"
)

func ProcDefList(ctx context.Context, includeDraft, permission, tag string, userRoles []string) (result []*models.ProcDefListObj, err error) {
	var procDefRows []*models.ProcDef
	baseSql := "select * from proc_def where 1=1"
	var filterSqlList []string
	var filterParams []interface{}
	if includeDraft == "0" {
		filterSqlList = append(filterSqlList, "status=?")
		filterParams = append(filterParams, models.Deployed)
	} else {
		filterSqlList = append(filterSqlList, "(status=? or status=?)")
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
	baseSql += strings.Join(filterSqlList, " and ")
	err = db.MysqlEngine.Context(ctx).SQL(baseSql, filterParams...).Find(&procDefRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	for _, row := range procDefRows {
		resultObj := models.ProcDefListObj{
			ProcDefId:      row.Id,
			ProcDefKey:     row.Key,
			ProcDefName:    row.Name,
			ProcDefVersion: row.Version,
			Status:         row.Status,
			Tags:           row.Tags,
			CreatedTime:    row.CreatedTime.Format(models.DateTimeFormat),
			ExcludeMode:    "N",
			Scene:          row.Scene,
		}
		if row.ConflictCheck {
			resultObj.ExcludeMode = "Y"
		}
		result = append(result, &resultObj)
	}
	return
}
