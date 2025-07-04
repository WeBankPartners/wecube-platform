package api

import (
	"bytes"
	"fmt"
	data_trans "github.com/WeBankPartners/wecube-platform/platform-core/api/v1/data-trans"
	"go.uber.org/zap"
	"io"
	"net/http"
	"strings"
	"time"

	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	batch_execution "github.com/WeBankPartners/wecube-platform/platform-core/api/v1/batch-execution"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/v1/certification"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/v1/plugin"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/v1/process"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/v1/system"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/gin-gonic/gin"
)

type handlerFuncObj struct {
	HandlerFunc  func(c *gin.Context)
	Method       string
	Url          string
	LogOperation bool
	PreHandle    func(c *gin.Context)
	ApiCode      string
}

var (
	httpHandlerFuncList []*handlerFuncObj
	apiCodeMap          = make(map[string]string)
)

func init() {
	httpHandlerFuncList = append(httpHandlerFuncList,
		// health check
		&handlerFuncObj{Url: "/health-check", Method: "GET", HandlerFunc: healthCheck, ApiCode: "health"},
		// base
		&handlerFuncObj{Url: "/appinfo/version", Method: "GET", HandlerFunc: system.AppVersion, ApiCode: "get-version"},
		&handlerFuncObj{Url: "/resource-files", Method: "GET", HandlerFunc: plugin.GetPluginResourceFiles, ApiCode: "get-resource-files"},
		// system-variable
		&handlerFuncObj{Url: "/system-variables/retrieve", Method: "POST", HandlerFunc: system.QuerySystemVariables, ApiCode: "query-system-variables"},
		&handlerFuncObj{Url: "/system-variables/create", Method: "POST", HandlerFunc: system.CreateSystemVariable, ApiCode: "create-system-variables"},
		&handlerFuncObj{Url: "/system-variables/update", Method: "POST", HandlerFunc: system.UpdateSystemVariable, ApiCode: "update-system-variables"},
		&handlerFuncObj{Url: "/system-variables/delete", Method: "POST", HandlerFunc: system.DeleteSystemVariable, ApiCode: "delete-system-variables"},
		&handlerFuncObj{Url: "/system-variables/constant/system-variable-scope", Method: "GET", HandlerFunc: system.GetSystemVariableScope, ApiCode: "get-system-variable-scope"},
		// resource
		&handlerFuncObj{Url: "/resource/constants/resource-server-status", Method: "GET", HandlerFunc: system.GetResourceServerStatus, ApiCode: "get-resource-server-status"},
		&handlerFuncObj{Url: "/resource/constants/resource-server-types", Method: "GET", HandlerFunc: system.GetResourceServerTypes, ApiCode: "get-resource-server-types"},
		&handlerFuncObj{Url: "/resource/constants/resource-item-status", Method: "GET", HandlerFunc: system.GetResourceItemStatus, ApiCode: "get-resource-item-status"},
		&handlerFuncObj{Url: "/resource/constants/resource-item-types", Method: "GET", HandlerFunc: system.GetResourceItemTypes, ApiCode: "get-resource-item-types"},
		&handlerFuncObj{Url: "/resource/servers/retrieve", Method: "POST", HandlerFunc: system.QueryResourceServer, ApiCode: "query-resource-server"},
		&handlerFuncObj{Url: "/resource/items/retrieve", Method: "POST", HandlerFunc: system.QueryResourceItem, ApiCode: "query-resource-item"},
		&handlerFuncObj{Url: "/resource/servers/create", Method: "POST", HandlerFunc: system.CreateResourceServer, ApiCode: "create-resource-server"},
		&handlerFuncObj{Url: "/resource/servers/update", Method: "POST", HandlerFunc: system.UpdateResourceServer, ApiCode: "update-resource-server"},
		&handlerFuncObj{Url: "/resource/servers/delete", Method: "POST", HandlerFunc: system.DeleteResourceServer, ApiCode: "delete-resource-server"},
		&handlerFuncObj{Url: "/resource/servers/:resourceServerId/product-serial", Method: "GET", HandlerFunc: system.GetResourceServerSerialNum, ApiCode: "get-serial-num"},
		&handlerFuncObj{Url: "/resource/items/create", Method: "POST", HandlerFunc: system.CreateResourceItem, ApiCode: "create-resource-item"},
		&handlerFuncObj{Url: "/resource/items/update", Method: "POST", HandlerFunc: system.UpdateResourceItem, ApiCode: "update-resource-item"},
		&handlerFuncObj{Url: "/resource/items/delete", Method: "POST", HandlerFunc: system.DeleteResourceItem, ApiCode: "delete-resource-item"},
		// plugin
		&handlerFuncObj{Url: "/packages", Method: "GET", HandlerFunc: plugin.GetPackages, ApiCode: "get-packages"},
		&handlerFuncObj{Url: "/packages/web-running", Method: "GET", HandlerFunc: plugin.GetWebRunningPackages, ApiCode: "get-web-running-packages"},
		&handlerFuncObj{Url: "/packages", Method: "POST", HandlerFunc: plugin.UploadPackage, ApiCode: "upload-packages"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/dependencies", Method: "GET", HandlerFunc: plugin.GetPluginDependencies, ApiCode: "get-plugin-dependencies"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/menus", Method: "GET", HandlerFunc: plugin.GetPluginMenus, ApiCode: "get-plugin-menus"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/models", Method: "GET", HandlerFunc: plugin.GetPluginModels, ApiCode: "get-plugin-models"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/system-parameters", Method: "GET", HandlerFunc: plugin.GetPluginSystemParameters, ApiCode: "get-plugin-system-parameters"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/authorities", Method: "GET", HandlerFunc: plugin.GetPluginAuthorities, ApiCode: "get-plugin-authorities"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/runtime-resources", Method: "GET", HandlerFunc: plugin.GetPluginRuntimeResources, ApiCode: "get-runtime-resource"},
		&handlerFuncObj{Url: "/packages/register/:pluginPackageId", Method: "POST", HandlerFunc: plugin.RegisterPackage, ApiCode: "register-package"},
		&handlerFuncObj{Url: "/available-container-hosts", Method: "GET", HandlerFunc: plugin.GetAvailableContainerHost, ApiCode: "get-available-host"},
		&handlerFuncObj{Url: "/hosts/:hostIp/next-available-port", Method: "GET", HandlerFunc: plugin.GetHostAvailablePort, ApiCode: "get-available-port"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/hosts/:hostIp/ports/:port/instance/launch", Method: "POST", HandlerFunc: plugin.LaunchPlugin, ApiCode: "launch-plugin"},
		&handlerFuncObj{Url: "/packages/instances/:pluginInstanceId/remove", Method: "DELETE", HandlerFunc: plugin.RemovePlugin, ApiCode: "remove-plugin"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/instances", Method: "GET", HandlerFunc: plugin.GetPluginRunningInstances, ApiCode: "get-plugin-running-instance"},
		&handlerFuncObj{Url: "/packages/name/list", Method: "GET", HandlerFunc: plugin.GetPackageNames, ApiCode: "get-package-names"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/resources/s3/files", Method: "GET", HandlerFunc: plugin.GetPluginS3Files, ApiCode: "get-plugin-s3-files"},
		&handlerFuncObj{Url: "/packages/ui/register", Method: "POST", HandlerFunc: plugin.UIRegisterPackage, ApiCode: "ui-register-package"},
		&handlerFuncObj{Url: "/packages/register-done", Method: "POST", HandlerFunc: plugin.RegisterPackageDone, ApiCode: "register-package-done"},
		&handlerFuncObj{Url: "/plugins/packages/version/get", Method: "GET", HandlerFunc: plugin.GetPluginConfigVersionList, ApiCode: "get-package-config-version"},
		&handlerFuncObj{Url: "/plugins/packages/version/inherit", Method: "POST", HandlerFunc: plugin.InheritPluginConfig, ApiCode: "inherit-package-config"},

		// plugin-config
		&handlerFuncObj{Url: "/packages/:pluginPackageId/plugin-configs", Method: "GET", HandlerFunc: plugin.GetPluginConfigs, ApiCode: "get-plugin-configs"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/plugins", Method: "GET", HandlerFunc: plugin.GetPluginConfigsWithInterfaces, ApiCode: "get-plugin-configs-with-interfaces"},
		&handlerFuncObj{Url: "/plugins/interfaces/:pluginConfigId", Method: "GET", HandlerFunc: plugin.GetConfigInterfaces, ApiCode: "get-config-interface"},
		&handlerFuncObj{Url: "/plugins/roles/configs/:pluginConfigId", Method: "POST", HandlerFunc: plugin.UpdatePluginConfigRoles, ApiCode: "update-config-roles"},
		&handlerFuncObj{Url: "/plugins/disable/:pluginConfigId", Method: "POST", HandlerFunc: plugin.DisablePluginConfig, ApiCode: "disable-plugin-config"},
		&handlerFuncObj{Url: "/plugins/enable/:pluginConfigId", Method: "POST", HandlerFunc: plugin.EnablePluginConfig, ApiCode: "enable-plugin-configs"},
		&handlerFuncObj{Url: "/plugins", Method: "POST", HandlerFunc: plugin.SavePluginConfig, ApiCode: "save-plugin-configs"},
		&handlerFuncObj{Url: "/plugins/configs/:pluginConfigId", Method: "DELETE", HandlerFunc: plugin.DeletePluginConfig, ApiCode: "delete-plugin-configs"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/plugin-config-outlines", Method: "GET", HandlerFunc: plugin.GetBatchPluginConfigs, ApiCode: "get-batch-plugin-configs"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/plugin-configs/enable-in-batch", Method: "POST", HandlerFunc: plugin.BatchEnablePluginConfig, ApiCode: "batch-enable-plugin-configs"},
		&handlerFuncObj{Url: "/plugins/packages/export/:pluginPackageId", Method: "GET", HandlerFunc: plugin.ExportPluginConfigs, ApiCode: "export-plugin-configs"},
		&handlerFuncObj{Url: "/plugins/packages/export-choose/:pluginPackageId", Method: "POST", HandlerFunc: plugin.ExportPluginConfigs, ApiCode: "export-choose-plugin-configs"},
		&handlerFuncObj{Url: "/plugins/packages/import/:pluginPackageId", Method: "POST", HandlerFunc: plugin.ImportPluginConfigs, ApiCode: "import-plugin-configs"},
		&handlerFuncObj{Url: "/packages/decommission/:pluginPackageId", Method: "POST", HandlerFunc: plugin.DeletePlugin, ApiCode: "delete-plugin"},
		&handlerFuncObj{Url: "/plugins/query-by-target-entity", Method: "POST", HandlerFunc: plugin.QueryPluginByTargetEntity, ApiCode: "query-plugin-by-target-entity"},
		&handlerFuncObj{Url: "/plugin-artifacts", Method: "GET", HandlerFunc: plugin.ListOnliePackage, ApiCode: "list-online-packages"},
		&handlerFuncObj{Url: "/plugin-artifacts/pull-requests", Method: "POST", HandlerFunc: plugin.PullOnliePackage, ApiCode: "pull-online-package"},
		&handlerFuncObj{Url: "/plugin-artifacts/pull-requests/:pullId", Method: "GET", HandlerFunc: plugin.PullOnliePackageStatus, ApiCode: "pull-online-package-status"},
		&handlerFuncObj{Url: "/plugins/configs/interfaces/param/metadata/query", Method: "POST", HandlerFunc: plugin.QueryPluginInterfaceParam, ApiCode: "query-plugin-interface-param"},
		&handlerFuncObj{Url: "/plugins/objectmetas/id/:objectMetaId", Method: "GET", HandlerFunc: plugin.GetObjectMetas, ApiCode: "get-object-metas"},
		&handlerFuncObj{Url: "/plugins/configs/:pluginConfigId/interfaces/objectmetas/:objectMetaId", Method: "POST", HandlerFunc: plugin.UpdateObjectMetas, ApiCode: "update-object-metas"},

		// model
		&handlerFuncObj{Url: "/models", Method: "GET", HandlerFunc: plugin.GetAllModels, ApiCode: "get-all-models"},
		&handlerFuncObj{Url: "/models/package/:packageName/entity/:entity", Method: "GET", HandlerFunc: plugin.GetEntityModel, ApiCode: "get-entity-model"},
		&handlerFuncObj{Url: "/models/package/:packageName/entity/:entity/attributes", Method: "GET", HandlerFunc: plugin.GetEntityAttributes, ApiCode: "get-entity-attr"},
		&handlerFuncObj{Url: "/models/package/:packageName", Method: "GET", HandlerFunc: plugin.SyncDynamicModels, ApiCode: "sync-entity-model"},
		&handlerFuncObj{Url: "/data-model/dme/all-entities", Method: "POST", HandlerFunc: plugin.QueryExpressionEntities, ApiCode: "query-expr-entities"},
		&handlerFuncObj{Url: "/data-model/dme/integrated-query", Method: "POST", HandlerFunc: plugin.QueryExpressionData, ApiCode: "query-expr-data"},
		&handlerFuncObj{Url: "/public/data-model/dme/integrated-query", Method: "POST", HandlerFunc: plugin.QueryExpressionDataForPlugin, ApiCode: "public-query-expr-data"},

		// permission
		&handlerFuncObj{Url: "/my-menus", Method: "GET", HandlerFunc: system.GetMyMenuItems, ApiCode: "get-my-menu"},
		&handlerFuncObj{Url: "/users/create", Method: "POST", HandlerFunc: system.CreateUser, ApiCode: "create-user"},
		&handlerFuncObj{Url: "/user/:username/get", Method: "GET", HandlerFunc: system.GetUserByUsername, ApiCode: "get-user"},
		&handlerFuncObj{Url: "/user/:username/update", Method: "POST", HandlerFunc: system.UpdateUser, ApiCode: "update-user"},
		&handlerFuncObj{Url: "/users/retrieve", Method: "GET", HandlerFunc: system.GetAllUser, ApiCode: "get-all-user"},
		&handlerFuncObj{Url: "/users/query", Method: "POST", HandlerFunc: system.QueryUser, ApiCode: "query-user"},
		&handlerFuncObj{Url: "/roles/create", Method: "POST", HandlerFunc: system.CreateRole, ApiCode: "create-roles"},
		&handlerFuncObj{Url: "/roles/retrieve", Method: "GET", HandlerFunc: system.QueryRoles, ApiCode: "query-roles"},
		&handlerFuncObj{Url: "/roles/:role-id/menus", Method: "GET", HandlerFunc: system.GetMenusByRoleId, ApiCode: "get-role-menu"},
		&handlerFuncObj{Url: "/roles/:role-id/users", Method: "GET", HandlerFunc: system.GetUsersByRoleId, ApiCode: "get-role-user"},
		&handlerFuncObj{Url: "/roles/:role-id/update", Method: "POST", HandlerFunc: system.UpdateRole, ApiCode: "update-role"},
		&handlerFuncObj{Url: "/roles/:role-id/users/grant", Method: "POST", HandlerFunc: system.GrantUserAddRoles, ApiCode: "grant-role-users"},
		&handlerFuncObj{Url: "/roles/:role-id/users/revoke", Method: "DELETE", HandlerFunc: system.RevokeRoleFromUsers, ApiCode: "revoke-role-users"},
		&handlerFuncObj{Url: "/roles/:role-id/menus", Method: "POST", HandlerFunc: system.UpdateRoleToMenusByRoleId, ApiCode: "update-role-menus"},
		&handlerFuncObj{Url: "/all-menus", Method: "GET", HandlerFunc: system.AllMenus, ApiCode: "all-menus"},
		&handlerFuncObj{Url: "/users/:username/menus", Method: "GET", HandlerFunc: system.GetMenusByUsername, ApiCode: "get-user-menus"},
		&handlerFuncObj{Url: "/users/:username/roles", Method: "GET", HandlerFunc: system.GetRolesByUsername, ApiCode: "get-user-roles"},
		&handlerFuncObj{Url: "/users/:user-id/roles/grant", Method: "POST", HandlerFunc: system.GrantRoleToUsers, ApiCode: "grant-user-roles"},
		&handlerFuncObj{Url: "/users/reset-password", Method: "POST", HandlerFunc: system.ResetUserPassword, ApiCode: "reset-user-password"},
		&handlerFuncObj{Url: "/users/change-password", Method: "POST", HandlerFunc: system.ChangeUserPassword, ApiCode: "change-user-password"},
		&handlerFuncObj{Url: "/users/:user-id/delete", Method: "DELETE", HandlerFunc: system.DeleteUserByUserId, ApiCode: "delete-user"},
		&handlerFuncObj{Url: "/users/roles", Method: "GET", HandlerFunc: system.GetRolesOfCurrentUser, ApiCode: "get-user-roles"},

		// process manage
		&handlerFuncObj{Url: "/process/definitions", Method: "POST", HandlerFunc: process.AddOrUpdateProcessDefinition, ApiCode: "add-update-process-definition"},
		&handlerFuncObj{Url: "/process/definitions/:proc-def-id", Method: "GET", HandlerFunc: process.GetProcessDefinition, ApiCode: "get-process-definition"},
		&handlerFuncObj{Url: "/process/definitions/:proc-def-id/copy/:association", Method: "POST", HandlerFunc: process.CopyProcessDefinition, ApiCode: "copy-process-definition"},
		&handlerFuncObj{Url: "/process/definitions/list", Method: "POST", HandlerFunc: process.QueryProcessDefinitionList, ApiCode: "process-definition-list"},
		&handlerFuncObj{Url: "/process/definitions/all", Method: "GET", HandlerFunc: process.QueryAllProcessDefinitionList, ApiCode: "process-definition-all"},
		&handlerFuncObj{Url: "/process/definitions/list/:plugin", Method: "GET", HandlerFunc: process.QueryPluginProcessDefinitionList, ApiCode: "plugin-process-definition-list"},
		&handlerFuncObj{Url: "/process/definitions/status", Method: "POST", HandlerFunc: process.BatchUpdateProcessDefinitionStatus, ApiCode: "update-process-definition-status"},
		&handlerFuncObj{Url: "/process/definitions/permission", Method: "POST", HandlerFunc: process.BatchUpdateProcessDefinitionPermission, ApiCode: "update-process-definition-permission"},
		&handlerFuncObj{Url: "/process/definitions/export", Method: "POST", HandlerFunc: process.ExportProcessDefinition, ApiCode: "process-definition-export"},
		&handlerFuncObj{Url: "/process/definitions/import", Method: "POST", HandlerFunc: process.ImportProcessDefinition, ApiCode: "import-process-definition"},
		&handlerFuncObj{Url: "/process/definitions/deploy/:proc-def-id", Method: "POST", HandlerFunc: process.DeployProcessDefinition, ApiCode: "deploy-process-definition"},
		&handlerFuncObj{Url: "/process/definitions/:proc-def-id/tasknodes/briefs", Method: "GET", HandlerFunc: process.GetProcDefRootTaskNode, ApiCode: "get-process-definition-root-nodes"},
		&handlerFuncObj{Url: "/process/definitions/tasknodes", Method: "POST", HandlerFunc: process.AddOrUpdateProcDefTaskNodes, ApiCode: "add-update-process-definition-nodes"},
		&handlerFuncObj{Url: "/process/definitions/:proc-def-id/tasknodes/:node-id", Method: "DELETE", HandlerFunc: process.DeleteProcDefNode, ApiCode: "delete-process-definition-nodes"},
		&handlerFuncObj{Url: "/process/definitions/:proc-def-id/tasknodes/:node-id", Method: "GET", HandlerFunc: process.GetProcDefNode, ApiCode: "get-process-definition-node"},
		&handlerFuncObj{Url: "/process/definitions/:proc-def-id/tasknodes/:node-id/preorder", Method: "GET", HandlerFunc: process.GetProcDefNodePreorder, ApiCode: "get-process-definition-node-preorder"},
		&handlerFuncObj{Url: "/process/definitions/:proc-def-id/tasknodes/:node-id/parameters", Method: "GET", HandlerFunc: process.GetProcDefNodeParameters, ApiCode: "get-process-definition-node-parameters"},
		&handlerFuncObj{Url: "/process/definitions/link", Method: "POST", HandlerFunc: process.AddOrUpdateProcDefNodeLink, ApiCode: "add-update-process-definition-node-link"},
		&handlerFuncObj{Url: "/process/definitions/:proc-def-id/link/:node-link-id", Method: "DELETE", HandlerFunc: process.DeleteProcDefNodeLink, ApiCode: "delete-process-definition-node-link"},
		&handlerFuncObj{Url: "/process/definitions/:proc-def-id/link/:node-link-id", Method: "GET", HandlerFunc: process.GetProcDefNodeLink, ApiCode: "get-process-definition-node-link"},
		&handlerFuncObj{Url: "/public/process/definitions/syncUseRole", Method: "POST", HandlerFunc: process.SyncUseRole, ApiCode: "public-sync-user-role"},
		&handlerFuncObj{Url: "/process/definitions/handler/update", Method: "POST", HandlerFunc: process.UpdateProcDefHandler, ApiCode: "update-process-definition-handler"},
		&handlerFuncObj{Url: "/process/definitions/:proc-def-id/parent/get", Method: "POST", HandlerFunc: process.GetProcDefParentList, ApiCode: "get-process-definition-parent"},
		&handlerFuncObj{Url: "/process/definitions/collect/add", Method: "POST", HandlerFunc: process.AddProcDefCollect, ApiCode: "add-proc-collect"},
		&handlerFuncObj{Url: "/process/definitions/collect/del", Method: "POST", HandlerFunc: process.DelProcDefCollect, ApiCode: "del-proc-collect"},
		&handlerFuncObj{Url: "/process/definitions/sub/list", Method: "POST", HandlerFunc: process.SubProcDefList, ApiCode: "sub-proc-list"},
		&handlerFuncObj{Url: "/process/definitions/nodes/empty-query", Method: "GET", HandlerFunc: process.QueryEmptyNodes, ApiCode: "query-empty-node"},
		// process runtime
		&handlerFuncObj{Url: "/process/definitions", Method: "GET", HandlerFunc: process.ProcDefList, ApiCode: "list-process-def"},

		&handlerFuncObj{Url: "/process/definitions/:proc-def-id/outline", Method: "GET", HandlerFunc: process.ProcDefOutline, ApiCode: "process-def-outline"},
		&handlerFuncObj{Url: "/process/definitions/:proc-def-id/root-entities", Method: "GET", HandlerFunc: process.ProcDefRootEntities, ApiCode: "process-def-root-entity"},
		&handlerFuncObj{Url: "/process/definitions/:proc-def-id/preview/entities/:entityDataId", Method: "GET", HandlerFunc: process.ProcDefPreview, ApiCode: "process-def-preview"},
		&handlerFuncObj{Url: "/public/process/definitions", Method: "GET", HandlerFunc: process.PublicProcDefList, ApiCode: "public-list-process-def"},
		&handlerFuncObj{Url: "/public/process/definitions/detail", Method: "GET", HandlerFunc: process.GetProcessDefinitionByNameAndVersion, ApiCode: "process-definition-by-name"},
		&handlerFuncObj{Url: "/public/process/definitions/:proc-def-id/preview/entities/:entityDataId", Method: "GET", HandlerFunc: process.PublicProcDefPreview, ApiCode: "process-def-preview"},
		&handlerFuncObj{Url: "/public/process/definitions/:proc-def-id/tasknodes", Method: "GET", HandlerFunc: process.PublicProcDefTaskNodes, ApiCode: "get-process-definition-tasknodes"},
		&handlerFuncObj{Url: "/process/instances/tasknodes/session/:sessionId/tasknode-bindings", Method: "GET", HandlerFunc: process.ProcInsTaskNodeBindings, ApiCode: "process-ins-binding"},
		&handlerFuncObj{Url: "/process/instances/tasknodes/:taskNodeId/session/:sessionId/tasknode-bindings", Method: "GET", HandlerFunc: process.ProcInsTaskNodeBindings, ApiCode: "process-ins-node-binding"},
		&handlerFuncObj{Url: "/process/instances/tasknodes/:taskNodeId/session/:sessionId/tasknode-bindings", Method: "POST", HandlerFunc: process.UpdateProcNodeBindingData, ApiCode: "update-process-ins-node-binding"},
		&handlerFuncObj{Url: "/process/instances", Method: "POST", HandlerFunc: process.ProcInsStart, ApiCode: "process-ins-start"},
		&handlerFuncObj{Url: "/public/process/instances", Method: "POST", HandlerFunc: process.PublicProcInsStart, ApiCode: "public-process-ins-start"},
		&handlerFuncObj{Url: "/process/instances", Method: "GET", HandlerFunc: process.ProcInsList, ApiCode: "process-ins-list"},
		&handlerFuncObj{Url: "/process/instances/:procInsId", Method: "GET", HandlerFunc: process.ProcInsDetail, ApiCode: "process-ins-detail"},
		&handlerFuncObj{Url: "/process/instances/:procInsId/tasknodes/:procInsNodeId/context", Method: "GET", HandlerFunc: process.GetProcInsNodeContext, ApiCode: "process-ins-node-context"},
		&handlerFuncObj{Url: "/process/instances/:procInsId/tasknodes/:procInsNodeId/tasknode-bindings", Method: "POST", HandlerFunc: process.UpdateProcInsTaskNodeBindings, ApiCode: "process-ins-node-update-binding"},
		&handlerFuncObj{Url: "/process/instances/:procInsId/tasknodes/:procInsNodeId/tasknode-bindings", Method: "GET", HandlerFunc: process.GetProcInsTaskNodeBindings, ApiCode: "get-process-ins-node-binding"},
		&handlerFuncObj{Url: "/process/instances/:procInsId/tasknode-bindings", Method: "GET", HandlerFunc: process.GetInstanceTaskNodeBindings, ApiCode: "get-process-ins-binding"},
		&handlerFuncObj{Url: "/process/instances/:procInsId/preview/entities", Method: "GET", HandlerFunc: process.GetProcInsPreview, ApiCode: "get-ins-preview"},
		&handlerFuncObj{Url: "/public/process/instances/:procInsId/terminations", Method: "POST", HandlerFunc: process.ProcTermination, ApiCode: "process-ins-terminations"},
		&handlerFuncObj{Url: "/public/process/instances/batch-terminations", Method: "POST", HandlerFunc: process.BatchProcTermination, ApiCode: "batch-ins-terminations"},
		&handlerFuncObj{Url: "/process/instances/proceed", Method: "POST", HandlerFunc: process.ProcInsOperation, ApiCode: "proc-ins-operation"},
		&handlerFuncObj{Url: "/packages/:pluginPackageId/entities/:entityName/query", Method: "POST", HandlerFunc: process.ProcEntityDataQuery, ApiCode: "proc-ins-operation"},
		&handlerFuncObj{Url: "/process/instances/callback", Method: "POST", HandlerFunc: process.ProcInstanceCallback, ApiCode: "proc-ins-callback"},
		&handlerFuncObj{Url: "/process/instancesWithPaging", Method: "POST", HandlerFunc: process.QueryProcInsPageData, ApiCode: "proc-ins-page-data"},
		&handlerFuncObj{Url: "/operation-events", Method: "POST", HandlerFunc: process.ProcStartEvents, ApiCode: "proc-start-events"},
		&handlerFuncObj{Url: "/public/process/definitions/:proc-def-id/options/:proc-node-def-id", Method: "GET", HandlerFunc: process.GetProcNodeAllowOptions, ApiCode: "get-proc-node-options"},
		&handlerFuncObj{Url: "/process/instances/node-message/:procInsNodeId/time", Method: "GET", HandlerFunc: process.GetProcNodeEndTime, ApiCode: "get-process-ins-node-time"},
		&handlerFuncObj{Url: "/process/instances/node-message/:procInsNodeId/choose", Method: "GET", HandlerFunc: process.GetProcNodeNextChoose, ApiCode: "get-process-ins-node-choose"},
		&handlerFuncObj{Url: "/process/instances/by-session-id", Method: "GET", HandlerFunc: process.GetProcInstanceBySessionId, ApiCode: "get-process-ins-by-session-id"},

		// certification manager
		&handlerFuncObj{Url: "/plugin-certifications", Method: "GET", HandlerFunc: certification.GetCertifications, ApiCode: "get-certifications"},
		&handlerFuncObj{Url: "/plugin-certifications/:certId/export", Method: "GET", HandlerFunc: certification.ExportCertification, ApiCode: "export-certification"},
		&handlerFuncObj{Url: "/plugin-certifications/import", Method: "POST", HandlerFunc: certification.ImportCertification, ApiCode: "import-certification"},
		&handlerFuncObj{Url: "/plugin-certifications/:certId", Method: "DELETE", HandlerFunc: certification.DeleteCertification, ApiCode: "delete-certification"},

		// batch-execution
		&handlerFuncObj{Url: "/batch-execution/templates", Method: "POST", HandlerFunc: batch_execution.CreateOrUpdateTemplate, ApiCode: "create-update-batch-execution-template"},
		&handlerFuncObj{Url: "/batch-execution/templates/list", Method: "POST", HandlerFunc: batch_execution.RetrieveTemplate, ApiCode: "retrieve-batch-execution-template"},
		&handlerFuncObj{Url: "/batch-execution/templates/all", Method: "GET", HandlerFunc: batch_execution.GetAllTemplate, ApiCode: "retrieve-batch-execution-template"},
		&handlerFuncObj{Url: "/batch-execution/templates/:templateId", Method: "GET", HandlerFunc: batch_execution.GetTemplate, ApiCode: "get-batch-execution-template"},
		&handlerFuncObj{Url: "/batch-execution/templates/:templateId", Method: "DELETE", HandlerFunc: batch_execution.DeleteTemplate, ApiCode: "delete-batch-execution-template"},
		&handlerFuncObj{Url: "/batch-execution/templates/collect", Method: "POST", HandlerFunc: batch_execution.CollectTemplate, ApiCode: "collect-batch-execution-template"},
		&handlerFuncObj{Url: "/batch-execution/templates/uncollect", Method: "POST", HandlerFunc: batch_execution.UncollectTemplate, ApiCode: "uncollect-batch-execution-template"},
		&handlerFuncObj{Url: "/batch-execution/templates/collect/check", Method: "POST", HandlerFunc: batch_execution.CheckCollectTemplate, ApiCode: "check-collect-batch-execution-template"},
		&handlerFuncObj{Url: "/batch-execution/templates/permission/update", Method: "POST", HandlerFunc: batch_execution.UpdateTemplatePermission, ApiCode: "update-batch-execution-template-permission"},
		&handlerFuncObj{Url: "/batch-execution/list", Method: "POST", HandlerFunc: batch_execution.RetrieveBatchExec, ApiCode: "retrieve-batch-execution"},
		&handlerFuncObj{Url: "/batch-execution/:batchExecId", Method: "GET", HandlerFunc: batch_execution.GetBatchExec, ApiCode: "get-batch-execution"},
		&handlerFuncObj{Url: "/batch-execution/job/run", Method: "POST", HandlerFunc: batch_execution.RunJob, ApiCode: "run-batch-execution-job"},
		&handlerFuncObj{Url: "/batch-execution/seed", Method: "GET", HandlerFunc: batch_execution.GetSeed, ApiCode: "get-batch-execution-seed"},
		&handlerFuncObj{Url: "/batch-execution/templates/export", Method: "POST", HandlerFunc: batch_execution.ExportTemplate, ApiCode: "export-batch-execution-template"},
		&handlerFuncObj{Url: "/batch-execution/templates/import", Method: "POST", HandlerFunc: batch_execution.ImportTemplate, ApiCode: "import-batch-execution-template"},

		// process schedule
		&handlerFuncObj{Url: "/user-scheduled-tasks/query", Method: "POST", HandlerFunc: process.QueryProcScheduleList, ApiCode: "query_proc_schedule"},
		&handlerFuncObj{Url: "/user-scheduled-tasks/create", Method: "POST", HandlerFunc: process.CreateProcSchedule, ApiCode: "create_proc_schedule"},
		&handlerFuncObj{Url: "/user-scheduled-tasks/stop", Method: "POST", HandlerFunc: process.StopProcSchedule, ApiCode: "stop_proc_schedule"},
		&handlerFuncObj{Url: "/user-scheduled-tasks/resume", Method: "POST", HandlerFunc: process.StartProcSchedule, ApiCode: "resume_proc_schedule"},
		&handlerFuncObj{Url: "/user-scheduled-tasks/delete", Method: "POST", HandlerFunc: process.DeleteProcSchedule, ApiCode: "delete_proc_schedule"},
		&handlerFuncObj{Url: "/user-scheduled-tasks/process-instances/query", Method: "POST", HandlerFunc: process.QueryProcScheduleInstance, ApiCode: "query_proc_schedule_inst"},

		// process report
		// 编排 tab
		&handlerFuncObj{Url: "/statistics/process/definitions", Method: "GET", HandlerFunc: process.StatisticsProDefList, ApiCode: "statistics-prodef-list"},
		&handlerFuncObj{Url: "/statistics/process/definitions/executions/overviews/query", Method: "POST", HandlerFunc: process.StatisticsProcessExec, ApiCode: "statistics-process-exec"},
		// 编排节点 tab
		&handlerFuncObj{Url: "/statistics/process/definitions/tasknodes/query", Method: "POST", HandlerFunc: process.StatisticsTasknodes, ApiCode: "statistics-tasknodes"},
		&handlerFuncObj{Url: "/statistics/process/definitions/tasknodes/tasknode-bindings/query", Method: "POST", HandlerFunc: process.StatisticsBindingsEntityByNode, ApiCode: "statistics-bindings-entity-by-node"},
		&handlerFuncObj{Url: "/statistics/process/definitions/executions/tasknodes/reports/query", Method: "POST", HandlerFunc: process.StatisticsTasknodeExec, ApiCode: "statistics-tasknode-exec"},
		&handlerFuncObj{Url: "/statistics/process/definitions/executions/tasknodes/report-details/query", Method: "POST", HandlerFunc: process.StatisticsTasknodeExecDetails, ApiCode: "statistics-tasknode-exec-details"},
		// 插件服务 tab
		&handlerFuncObj{Url: "/statistics/process/definitions/tasknodes/service-ids", Method: "GET", HandlerFunc: process.StatisticsServiceNames, ApiCode: "statistics-service-ids"},
		&handlerFuncObj{Url: "/statistics/process/definitions/service-ids/tasknode-bindings/query", Method: "POST", HandlerFunc: process.StatisticsBindingsEntityByService, ApiCode: "statistics-bindings-entity-by-service"},
		&handlerFuncObj{Url: "/statistics/process/definitions/executions/plugin/reports/query", Method: "POST", HandlerFunc: process.StatisticsPluginExec, ApiCode: "statistics-plugin-exec"},
		&handlerFuncObj{Url: "/statistics/process/definitions/executions/plugin/report-details/query", Method: "POST", HandlerFunc: process.StatisticsPluginExecDetails, ApiCode: "statistics-plugin-exec-details"},

		// 底座导入导出
		&handlerFuncObj{Url: "/data/transfer/business/list", Method: "POST", HandlerFunc: data_trans.QueryBusinessList, ApiCode: "data-transfer-business-list"},
		&handlerFuncObj{Url: "/data/transfer/export/customer", Method: "POST", HandlerFunc: data_trans.CreateOrUpdateExportCustomer, ApiCode: "data-transfer-export-customer-add"},
		&handlerFuncObj{Url: "/data/transfer/export/nexus", Method: "GET", HandlerFunc: data_trans.GetExportNexusInfo, ApiCode: "data-transfer-export-nexus"},
		&handlerFuncObj{Url: "/data/transfer/export/customer", Method: "GET", HandlerFunc: data_trans.QueryExportCustomerList, ApiCode: "data-transfer-export-customer-get"},
		&handlerFuncObj{Url: "/data/transfer/export/customer", Method: "DELETE", HandlerFunc: data_trans.DeleteExportCustomer, ApiCode: "data-transfer-export-customer-delete"},
		&handlerFuncObj{Url: "/data/transfer/export/create", Method: "POST", HandlerFunc: data_trans.CreateExport, ApiCode: "data-transfer-export-create"},
		&handlerFuncObj{Url: "/data/transfer/export/update", Method: "POST", HandlerFunc: data_trans.UpdateExport, ApiCode: "data-transfer-export-update"},
		&handlerFuncObj{Url: "/data/transfer/export", Method: "POST", HandlerFunc: data_trans.ExecExport, ApiCode: "data-transfer-export"},
		&handlerFuncObj{Url: "/data/transfer/export/detail", Method: "GET", HandlerFunc: data_trans.ExportDetail, ApiCode: "data-transfer-export-detail"},
		&handlerFuncObj{Url: "/data/transfer/export/list/options", Method: "GET", HandlerFunc: data_trans.GetExportListOptions, ApiCode: "data-transfer-export-options"},
		&handlerFuncObj{Url: "/data/transfer/export/list", Method: "POST", HandlerFunc: data_trans.ExportList, ApiCode: "data-transfer-export-list"},

		&handlerFuncObj{Url: "/data/transfer/import/business", Method: "GET", HandlerFunc: data_trans.GetBusinessList, ApiCode: "data-transfer-get-business-list"},
		&handlerFuncObj{Url: "/data/transfer/import", Method: "POST", HandlerFunc: data_trans.ExecImport, ApiCode: "data-transfer-exec-import"},
		&handlerFuncObj{Url: "/data/transfer/import/detail", Method: "GET", HandlerFunc: data_trans.ImportDetail, ApiCode: "data-transfer-import-detail"},
		&handlerFuncObj{Url: "/data/transfer/import/list/options", Method: "GET", HandlerFunc: data_trans.GetImportListOptions, ApiCode: "data-transfer-import-options"},
		&handlerFuncObj{Url: "/data/transfer/import/list", Method: "POST", HandlerFunc: data_trans.ImportList, ApiCode: "data-transfer-import-list"},
		&handlerFuncObj{Url: "/data/transfer/import/status", Method: "POST", HandlerFunc: data_trans.UpdateImportStatus, ApiCode: "data-transfer-update-import-status"},

		// 查询机器人配置
		&handlerFuncObj{Url: "/robot/config", Method: "GET", HandlerFunc: system.GetRotConfig, ApiCode: "get-robot-config"},
	)
}

func InitHttpServer() {
	middleware.InitHttpError()
	r := gin.New()
	// access log
	r.Use(httpLogHandle())
	// recover
	r.Use(gin.CustomRecovery(recoverHandle))
	// register handler func with auth
	authRouter := r.Group(models.UrlPrefix, middleware.AuthToken)
	for _, funcObj := range httpHandlerFuncList {
		if !strings.HasPrefix(funcObj.Url, "/resource/") {
			funcObj.Url = "/v1" + funcObj.Url
		}
		tmpApiCode := fmt.Sprintf("%s_%s%s", funcObj.Method, models.UrlPrefix, funcObj.Url)
		apiCodeMap[tmpApiCode] = funcObj.ApiCode
		handleFuncList := []gin.HandlerFunc{funcObj.HandlerFunc}
		if funcObj.PreHandle != nil {
			handleFuncList = append([]gin.HandlerFunc{funcObj.PreHandle}, funcObj.HandlerFunc)
		}
		switch funcObj.Method {
		case "GET":
			authRouter.GET(funcObj.Url, handleFuncList...)
		case "POST":
			authRouter.POST(funcObj.Url, handleFuncList...)
		case "PUT":
			authRouter.PUT(funcObj.Url, handleFuncList...)
		case "DELETE":
			authRouter.DELETE(funcObj.Url, handleFuncList...)
		}
	}
	r.GET(models.UrlPrefix+"/v1/route-items", system.GetRouteItems)
	r.GET(models.UrlPrefix+"/v1/route-items/:name", system.GetRouteItems)
	r.POST(models.UrlPrefix+"/entities/role/query", plugin.QueryRoleEntity)
	middleware.InitApiMenuMap(apiCodeMap)
	r.Run(":" + models.Config.HttpServer.Port)
}

func httpLogHandle() gin.HandlerFunc {
	return func(c *gin.Context) {
		start := time.Now()
		requestId := c.GetHeader(models.RequestIdHeader)
		transactionId := c.GetHeader(models.TransactionIdHeader)
		if requestId == "" {
			requestId = "req_" + guid.CreateGuid()
		}
		if transactionId == "" {
			transactionId = "trans_" + guid.CreateGuid()
		}
		c.Set(models.RequestIdHeader, requestId)
		c.Set(models.TransactionIdHeader, transactionId)
		if !strings.HasSuffix(c.Request.RequestURI, "/v1/packages") {
			bodyBytes, _ := io.ReadAll(c.Request.Body)
			c.Request.Body.Close()
			c.Request.Body = io.NopCloser(bytes.NewReader(bodyBytes))
			c.Set(models.ContextRequestBody, string(bodyBytes))
		}
		apiCode := apiCodeMap[c.Request.Method+"_"+c.FullPath()]
		c.Writer.Header().Add("Api-Code", apiCode)
		c.Set(models.ContextApiCode, apiCode)
		log.Info(nil, log.LOGGER_ACCESS, zap.String("uri", c.Request.RequestURI), zap.String("serviceCode", apiCode), zap.String("method", c.Request.Method), zap.String("sourceIp", getRemoteIp(c)), zap.String(models.ContextOperator, c.GetString(models.ContextOperator)), zap.String(models.ContextRequestBody, c.GetString(models.ContextRequestBody)))
		c.Next()
		costTime := time.Since(start).Seconds() * 1000
		userId := c.GetString(models.ContextUserId)
		if log.DebugEnable {
			log.Info(nil, log.LOGGER_ACCESS, zap.String("userId", userId), zap.String("uri", c.Request.RequestURI), zap.String("serviceCode", apiCode), zap.String("method", c.Request.Method), zap.Int("httpCode", c.Writer.Status()), zap.Int(models.ContextErrorCode, c.GetInt(models.ContextErrorCode)), zap.String(models.ContextErrorMessage, c.GetString(models.ContextErrorMessage)), zap.Float64("costTime", costTime), zap.String(models.ContextResponseBody, c.GetString(models.ContextResponseBody)))
		} else {
			log.Info(nil, log.LOGGER_ACCESS, zap.String("userId", userId), zap.String("uri", c.Request.RequestURI), zap.String("serviceCode", apiCode), zap.String("method", c.Request.Method), zap.Int("httpCode", c.Writer.Status()), zap.Int(models.ContextErrorCode, c.GetInt(models.ContextErrorCode)), zap.String(models.ContextErrorMessage, c.GetString(models.ContextErrorMessage)), zap.Float64("costTime", costTime))
		}
	}
}

func getRemoteIp(c *gin.Context) string {
	return c.ClientIP()
}

func recoverHandle(c *gin.Context, err interface{}) {
	var errorMessage string
	if err != nil {
		errorMessage = err.(error).Error()
	}
	log.Error(c, log.LOGGER_APP, "Handle recover error", zap.Int("code", -2), zap.String("message", errorMessage))
	c.JSON(http.StatusInternalServerError, models.HttpResponseMeta{Code: -2, Status: models.DefaultHttpErrorCode})
}

// @Summary 健康检查
// @description 健康检查
// @Tags 健康检查接口
// @Produce  application/json
// @Success 200 {object} models.ResponseJson
// @Router /health-check [get]
func healthCheck(c *gin.Context) {
	if err := db.CheckDbConnection(); err != nil {
		c.JSON(http.StatusInternalServerError, models.HttpResponseMeta{Status: models.DefaultHttpErrorCode, Message: err.Error()})
	} else {
		middleware.ReturnSuccess(c)
	}
}
