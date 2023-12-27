package models

type PluginConfigQueryObj struct {
	PluginConfigName    string             `json:"pluginConfigName"`
	PluginConfigDtoList []*PluginConfigDto `json:"pluginConfigDtoList"`
}

type PluginConfigDto struct {
	PluginConfigs
	FilterRule       string              `json:"filterRule"`
	PermissionToRole *PermissionRoleData `json:"permissionToRole"`
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
