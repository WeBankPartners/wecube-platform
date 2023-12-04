package database

import (
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

func GetPackages(allFlag bool) (result []*models.PluginPackages, err error) {
	result = []*models.PluginPackages{}
	if allFlag {
		err = db.MysqlEngine.SQL("select * from plugin_packages").Find(&result)
	} else {
		err = db.MysqlEngine.SQL("select * from plugin_packages where status in (0,1)").Find(&result)
	}
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	for _, v := range result {
		v.StatusString = models.PluginPackagesStatusMap[v.Status]
		v.EditionString = models.PluginPackagesEditionMap[v.Edition]
	}
	return
}
