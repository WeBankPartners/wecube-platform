package database

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

func GetRouteItems(pluginName string) (result []*models.RouteItem, err error) {
	var instanceRows []*models.RouteInstanceQueryObj
	if pluginName != "" {
		err = db.MysqlEngine.SQL("select t1.id,t1.host,t1.port,t2.name from plugin_instances t1 join plugin_packages t2 on t1.package_id=t2.id where t1.container_status= 'RUNNING' and t2.name=?", pluginName).Find(&instanceRows)
	} else {
		err = db.MysqlEngine.SQL("select t1.id,t1.host,t1.port,t2.name from plugin_instances t1 join plugin_packages t2 on t1.package_id=t2.id where t1.container_status= 'RUNNING'").Find(&instanceRows)
	}
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	instanceMap := make(map[string][]*models.RouteInstanceQueryObj)
	for _, row := range instanceRows {
		result = append(result, &models.RouteItem{Context: row.Name, HttpScheme: "http", Host: row.Host, Port: fmt.Sprintf("%d", row.Port)})
		if v, ok := instanceMap[row.Name]; ok {
			instanceMap[row.Name] = append(v, row)
		} else {
			instanceMap[row.Name] = []*models.RouteInstanceQueryObj{row}
		}
	}
	if pluginName != "" {
		return
	}
	var interfaceRows []*models.RouteInterfaceQueryObj
	err = db.MysqlEngine.SQL("select distinct t1.`path`,t1.http_method,t3.name from plugin_config_interfaces t1 join plugin_configs t2 on t1.plugin_config_id=t2.id join plugin_packages t3 on t2.plugin_package_id=t3.id where t2.status='ENABLED' and t3.status IN ('REGISTERED','RUNNING','STOPPED')").Find(&interfaceRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	for _, row := range interfaceRows {
		if itemList, ok := instanceMap[row.Name]; ok {
			for _, item := range itemList {
				if row.HttpMethod == "" {
					row.HttpMethod = "POST"
				}
				result = append(result, &models.RouteItem{Context: item.Name, HttpScheme: "http", Host: item.Host, Port: fmt.Sprintf("%d", item.Port), Path: row.Path, HttpMethod: row.HttpMethod, Weight: "0"})
			}
		}
	}
	return
}
