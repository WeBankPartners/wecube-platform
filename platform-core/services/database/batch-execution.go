package database

import (
	"fmt"
	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
	"time"
)

func CreateOrUpdateFavorites(c *gin.Context, reqParam *models.CreateOrUpdateFavoritesReq) (err error) {
	actions := []*db.ExecAction{}
	now := time.Now()
	if reqParam.Id == "" {
		// create
		reqParam.Id = guid.CreateGuid()
		favoritesData := &models.Favorites{
			Id:                       reqParam.Id,
			CollectionName:           reqParam.CollectionName,
			BatchExecutionTemplateId: reqParam.BatchExecutionTemplateId,
			CreatedBy:                middleware.GetRequestUser(c),
			UpdatedBy:                "",
			CreatedTime:              &now,
			UpdatedTime:              &now,
		}
		action, tmpErr := db.GetInsertTableExecAction(models.TableNameFavorites, *favoritesData, nil)
		if tmpErr != nil {
			err = fmt.Errorf("get insert sql failed: %s", tmpErr.Error())
			return
		}
		actions = append(actions, action)
	} else {
		// update
		updateColumnStr := "`collection_name`=?,`updated_by`=?,`updated_time`=?"
		action := &db.ExecAction{
			Sql:   db.CombineDBQuery("UPDATE ", models.TableNameFavorites, " SET ", updateColumnStr, " WHERE id=?"),
			Param: []interface{}{reqParam.CollectionName, middleware.GetRequestUser(c), now, reqParam.Id},
		}
		actions = append(actions, action)
	}
	favoritsId := reqParam.Id
	// update favoritesRole
	// firstly delete original favoritesRole and then create new favoritesRole
	action := &db.ExecAction{
		Sql:   db.CombineDBQuery("DELETE FROM ", models.TableNameFavoritesRole, " WHERE favorites_id=?"),
		Param: []interface{}{favoritsId},
	}
	actions = append(actions, action)

	var favoritsRoleDataList []*models.FavoritesRole
	mgmtRoleNameMap := make(map[string]struct{})
	for _, roleName := range reqParam.PermissionToRole.MGMT {
		if _, isExisted := mgmtRoleNameMap[roleName]; !isExisted {
			mgmtRoleNameMap[roleName] = struct{}{}
			favoritsRoleDataList = append(favoritsRoleDataList, &models.FavoritesRole{
				Id:          guid.CreateGuid(),
				FavoritesId: favoritsId,
				Permission:  string(models.MGMT),
				RoleName:    roleName,
			})
		}
	}
	useRoleNameMap := make(map[string]struct{})
	for _, roleName := range reqParam.PermissionToRole.USE {
		if _, isExisted := useRoleNameMap[roleName]; !isExisted {
			useRoleNameMap[roleName] = struct{}{}
			favoritsRoleDataList = append(favoritsRoleDataList, &models.FavoritesRole{
				Id:          guid.CreateGuid(),
				FavoritesId: favoritsId,
				Permission:  string(models.USE),
				RoleName:    roleName,
			})
		}
	}
	for i := range favoritsRoleDataList {
		action, tmpErr := db.GetInsertTableExecAction(models.TableNameFavoritesRole, *favoritsRoleDataList[i], nil)
		if tmpErr != nil {
			err = fmt.Errorf("get insert sql failed: %s", tmpErr.Error())
			return
		}
		actions = append(actions, action)
	}

	err = db.Transaction(actions, c)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
	}
	return
}
