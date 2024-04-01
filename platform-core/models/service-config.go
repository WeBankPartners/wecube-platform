package models

type PluginConfigQueryObj struct {
	PluginConfigName    string             `json:"pluginConfigName"`
	PluginConfigDtoList []*PluginConfigDto `json:"pluginConfigDtoList"`
}

type PluginConfigDto struct {
	PluginConfigs
	FilterRule       string              `json:"filterRule" xorm:"-"`
	PermissionToRole *PermissionRoleData `json:"permissionToRole" xorm:"-"`
}

type PermissionRoleData struct {
	MGMT []string `json:"MGMT"`
	USE  []string `json:"USE"`
}

type PluginInterfaceQueryObj struct {
	PluginConfigInterfaces
	InputParameters  []*PluginConfigInterfaceParameters `json:"inputParameters"`
	OutputParameters []*PluginConfigInterfaceParameters `json:"outputParameters"`
}
