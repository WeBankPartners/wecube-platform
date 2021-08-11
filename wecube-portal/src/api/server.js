import req from './base'

export const getMyMenus = () => req.get('/platform/v1/my-menus')
// init page

// flow
export const saveFlow = data => req.post('/platform/v1/process/definitions/deploy', data)
export const confirmSaveFlow = (continueToken, data) => {
  return req.post(`/platform/v1/process/definitions/deploy?continue_token=${continueToken}`, data)
}
export const saveFlowDraft = data => req.post('/platform/v1/process/definitions/draft', data)
export const confirmSaveFlowDraft = (continueToken, data) =>
  req.post(`/platform/v1/process/definitions/draft?continue_token=${continueToken}`, data)
export const getAllFlow = (isIncludeDraft = true) => {
  return isIncludeDraft
    ? req.get('/platform/v1/process/definitions?permission=MGMT')
    : req.get('/platform/v1/process/definitions?includeDraft=0&permission=USE')
}
export const getFlowDetailByID = id => req.get(`/platform/v1/process/definitions/${id}/detail`)
export const getFlowOutlineByID = id => req.get(`/platform/v1/process/definitions/${id}/outline`)

export const getTargetOptions = (pkgName, entityName) =>
  req.post(`/${pkgName}/entities/${entityName}/query`, {
    additionalFilters: []
  })
export const getTreePreviewData = (flowId, targetId) =>
  req.get(`platform/v1/process/definitions/${flowId}/preview/entities/${targetId}`)
export const createFlowInstance = data => req.post(`platform/v1/process/instances`, data)
export const getProcessInstances = () => req.get(`platform/v1/process/instances`)
export const getProcessInstance = id => req.get(`platform/v1/process/instances/${id}`)

export const retryProcessInstance = data => req.post(`platform/v1/process/instances/proceed`, data)
export const removeProcessDefinition = id => req.delete(`platform/v1/process/definitions/${id}`)

export const getParamsInfosByFlowIdAndNodeId = (flowId, nodeId) =>
  req.get(`platform/v1/process/definitions/${flowId}/tasknodes/${nodeId}`)

export const getFlowNodes = flowId => req.get(`platform/v1/process/definitions/${flowId}/tasknodes/briefs`)
export const getContextParametersNodes = (flowId, taskNodeId, prevCtxNodeIds) =>
  req.get(
    `/platform/v1/process/definitions/${flowId}/root-context-nodes/briefs?taskNodeId=${taskNodeId}&prevCtxNodeIds=${prevCtxNodeIds}`
  )

export const getAllDataModels = () => req.get(`platform/v1/models`)

export const getPluginInterfaceList = () => req.get(`platform/v1/plugins/interfaces/enabled`)

export const exportProcessDefinitionWithId = procDefId => {
  req.get(`platform/v1/process/definitions/${procDefId}/export`)
}

// admin
export const retrieveSystemVariables = data => req.post('/platform/v1/system-variables/retrieve', data)
export const createSystemVariables = data => req.post('/platform/v1/system-variables/create', data)
export const updateSystemVariables = data => req.post('/platform/v1/system-variables/update', data)
export const deleteSystemVariables = data => req.post('/platform/v1/system-variables/delete', data)
export const retrieveServers = data => req.post('/platform/resource/servers/retrieve', data)
export const createServers = data => req.post('/platform/resource/servers/create', data)
export const updateServers = data => req.post('/platform/resource/servers/update', data)
export const deleteServers = data => req.post('/platform/resource/servers/delete', data)
export const retrieveItems = data => req.post('/platform/resource/items/retrieve', data)
export const getResourceServerStatus = () => req.get('/platform/resource/constants/resource-server-status')
export const getResourceServerType = () => req.get('/platform/resource/constants/resource-server-types')
export const getResourceItemStatus = () => req.get('/platform/resource/constants/resource-item-status')
export const getResourceItemType = () => req.get('/platform/resource/constants/resource-item-types')

// enum
export const getEnumCodesByCategoryId = (catTypeId, catId) =>
  req.get(`/platform/v1/cmdb/enum/category-types/${catTypeId}/categories/${catId}/codes`)
export const getAllPluginPkgs = () => req.get('/platform/v1/packages')
export const getRefCiTypeFrom = id => req.get(`/platform/v1/cmdb/ci-types/${id}/references/by`)
export const getRefCiTypeTo = id => req.get(`/platform/v1/cmdb/ci-types/${id}/references/to`)
export const getCiTypeAttr = id => req.get(`/platform/v1/cmdb/ci-types/${id}/attributes`)
export const getAvailableInstancesByPackageId = packageId => req.get(`/platform/v1/packages/${packageId}/instances`)
export const createPluginInstanceByPackageIdAndHostIp = (packageId, ip, port) =>
  req.post(`/platform/v1/packages/${packageId}/hosts/${ip}/ports/${port}/instance/launch`)

export const removePluginInstance = instanceId => req.delete(`/platform/v1/packages/instances/${instanceId}/remove`)
export const queryLog = data => req.post(`/platform/v1/plugin/packages/instances/log`, data)
export const getAvailableContainerHosts = () => req.get(`/platform/v1/available-container-hosts`)
export const getAvailablePortByHostIp = ip => req.get(`/platform/v1/hosts/${ip}/next-available-port`)
export const createEnumCategory = data =>
  req.post(`/platform/v1/cmdb/enum/category-types/${data.catTypeId}/categories/create`, data)
export const updateEnumCategory = data =>
  req.put(`/platform/v1/cmdb/enum/category-types/${data.catTypeId}/categories/${data.catId}`, data)
export const login = data => req.post('/auth/v1/api/login', data)
export const deletePluginPkg = id => req.post(`/platform/v1/packages/decommission/${id}`)
export const getPluginPkgDataModel = id => req.get(`/platform/v1/packages/${id}/models`)
export const getPluginPkgDependcy = id => req.get(`/platform/v1/packages/${id}/dependencies`)
export const getAllPluginByPkgId = id => req.get(`/platform/v1/packages/${id}/plugins`)
export const deleteRegisterSource = id => req.delete(`/platform/v1/plugins/configs/${id}`)
export const getMenuInjection = id => req.get(`/platform/v1/packages/${id}/menus`)
export const getSysParams = id => req.get(`/platform/v1/packages/${id}/system-parameters`)
export const getRuntimeResource = id => req.get(`/platform/v1/packages/${id}/runtime-resources`)
export const getAuthSettings = id => req.get(`/platform/v1/packages/${id}/authorities`)
export const registerPlugin = id => req.post(`/platform/v1/plugins/enable/${id}`)
export const deletePlugin = id => req.post(`/platform/v1/plugins/disable/${id}`)
export const savePluginConfig = data => req.post(`/platform/v1/plugins`, data)
export const registPluginPackage = id => req.post(`/platform/v1/packages/register/${id}`)
export const queryDataBaseByPackageId = (id, payload) =>
  req.post(`/platform/v1/packages/${id}/resources/mysql/query`, payload)
export const queryStorageFilesByPackageId = (id, payload) =>
  req.get(`/platform/v1/packages/${id}/resources/s3/files`, payload)
export const getAllPluginPackageResourceFiles = () => req.get('/platform/v1/resource-files')
export const pullDynamicDataModel = name => req.get(`/platform/v1/models/package/${name}`)
export const getRefByIdInfoByPackageNameAndEntityName = (pkgName, entityName) =>
  req.get(`/platform/v1/models/package/${pkgName}/entity/${entityName}/refById`)
export const getModelNodeDetail = (packageName, entityName, data) =>
  req.post(`/${packageName}/entities/${entityName}/query`, data)
export const getNodeBindings = id => req.get(`/platform/v1/process/instances/${id}/tasknode-bindings`)
export const getNodeContext = (procId, nodeId) =>
  req.get(`/platform/v1/process/instances/${procId}/tasknodes/${nodeId}/context`)
export const userCreate = data => req.post(`/platform/v1/users/create`, data)
export const removeUser = roleId => req.delete(`/platform/v1/users/${roleId}/delete`)
export const changePassword = data => req.post(`/platform//v1/users/change-password`, data)
export const getUserList = () => req.get(`/platform/v1/users/retrieve`)
export const deleteUser = id => req.delete(`/platform/v1/users/${id}/delete`)
export const roleCreate = data => req.post(`/platform/v1/roles/create`, data)
export const getRoleList = () => req.get(`/platform/v1/roles/retrieve`)
export const deleteRole = id => req.delete(`/platform/v1/roles/${id}/delete`)
export const getRolesByUserName = userName => req.get(`/platform/v1/users/${userName}/roles`)
export const getUsersByRoleId = roleId => req.get(`/platform/v1/roles/${roleId}/users`)
export const grantRolesForUser = (data, roleId) => req.post(`/platform/v1/roles/${roleId}/users/grant`, data)
export const revokeRolesForUser = (data, roleId) => req.delete(`/platform/v1/roles/${roleId}/users/revoke`, { data })
export const getAllMenusList = () => req.get('/platform/v1/all-menus')
export const getMenusByUserName = name => req.get(`platform/v1/users/${name}/menus`)
export const getMenusByRoleId = id => req.get(`platform/v1/roles/${id}/menus`)
export const updateRoleToMenusByRoleId = (roleId, data) => req.post(`platform/v1/roles/${roleId}/menus`, data)
export const getFilteredPluginInterfaceList = (packageName, entityName) =>
  req.get(`/platform/v1/plugins/interfaces/package/${packageName}/entity/${entityName}/enabled`)
export const getRolesByCurrentUser = () => req.get(`/platform/v1/users/roles`)
export const getPermissionByProcessId = id => req.get(`/platform/v1/process/${id}/roles`)
export const updateFlowPermission = (id, data) => req.post(`/platform/v1/process/${id}/roles`, data)
export const deleteFlowPermission = (id, data) => req.delete(`/platform/v1/process/${id}/roles`, { data })
export const dmeAllEntities = data => req.post(`/platform/v1/data-model/dme/all-entities`, data)
export const dmeIntegratedQuery = data => req.post(`/platform/v1/data-model/dme/integrated-query`, data)
export const entityView = (packageName, entityName) =>
  req.get(`/platform/v1/models/package/${packageName}/entity/${entityName}/attributes`)
export const batchExecution = (url, data) => req.post(url, data)
export const deleteCollectionsRole = (id, data) => req.delete(`/platform/v1/roles/${id}/favorites`, { data })
export const addCollectionsRole = (id, data) => req.post(`/platform/v1/roles/${id}/favorites`, data)
export const getAllCollections = data => req.get(`/platform/v1/roles/favorites/retrieve`)
export const deleteCollections = id => req.delete(`/platform/v1/roles/favorites/${id}/delete`)
export const saveBatchExecution = data => req.post(`/platform/v1/roles/favorites/create`, data)
export const updateCollections = data => req.post(`/platform/v1/roles/favorites/update`, data)
export const getVariableScope = () => req.get(`/platform/v1/system-variables/constant/system-variable-scope`)
export const getPluginConfigsByPackageId = packageId => req.get(`/platform/v1/packages/${packageId}/plugin-configs`)
export const getInterfacesByPluginConfigId = configId => req.get(`/platform/v1/plugins/interfaces/${configId}`)
export const getEntityRefsByPkgNameAndEntityName = (pkgName, entityName) =>
  req.get(`/platform/v1/models/package/${pkgName}/entity/${entityName}`)
export const getPluginsByTargetEntityFilterRule = data =>
  req.post(`/platform/v1/plugins/interfaces/enabled/query-by-target-entity-filter-rule`, data)
export const getDataByNodeDefIdAndProcessSessionId = (nodeDefId, ProcessSessionId) =>
  req.get(`/platform/v1/process/instances/tasknodes/${nodeDefId}/session/${ProcessSessionId}/tasknode-bindings`)
export const setDataByNodeDefIdAndProcessSessionId = (nodeDefId, ProcessSessionId, data) =>
  req.post(`/platform/v1/process/instances/tasknodes/${nodeDefId}/session/${ProcessSessionId}/tasknode-bindings`, data)
export const getAllBindingsProcessSessionId = ProcessSessionId =>
  req.get(`/platform/v1/process/instances/tasknodes/session/${ProcessSessionId}/tasknode-bindings`)
export const getTargetModelByProcessDefId = id => req.get(`/platform/v1/process/definitions/${id}/root-entities`)
export const getPreviewEntitiesByInstancesId = id => req.get(`/platform/v1/process/instances/${id}/preview/entities`)
export const getPluginArtifacts = () => req.get(`/platform/v1/plugin-artifacts`)
export const pullPluginArtifact = data => req.post(`/platform/v1/plugin-artifacts/pull-requests`, data)
export const getPluginArtifactStatus = id => req.get(`/platform/v1/plugin-artifacts/pull-requests/${id}`)

export const exportPluginXMLWithId = id => {
  req.get(`/platform/v1/plugins/packages/export/${id}`)
}
export const updatePluginConfigRoleBinding = (id, data) => req.post(`/platform/v1/plugins/roles/configs/${id}`, data)
export const deletePluginConfigRoleBinding = (id, data) =>
  req.delete(`/platform/v1/plugins/roles/configs/${id}`, { data })
export const getApplicationVersion = id => req.get(`/platform/v1/appinfo/version`)
export const getConfigByPkgId = id => req.get(`/platform/v1/packages/${id}/plugin-config-outlines`)
export const updateConfigStatus = (id, data) =>
  req.post(`/platform/v1/packages/${id}/plugin-configs/enable-in-batch`, data)
export const resetPassword = data => req.post(`platform/v1/users/reset-password`, data)
export const createWorkflowInstanceTerminationRequest = data =>
  req.post(`platform/v1/public/process/instances/${data.procInstId}/terminations`, data)
export const getTaskNodeInstanceExecBindings = data =>
  req.get(`/platform/v1/process/instances/${data.procInstId}/tasknodes/${data.nodeInstId}/tasknode-bindings`)
export const updateTaskNodeInstanceExecBindings = data =>
  req.post(`platform/v1/process/instances/${data.procInstId}/tasknodes/${data.nodeInstId}/tasknode-bindings`, data.data)
export const getPluginRegisterObjectType = objectMetaId => req.get(`platform/v1/plugins/objectmetas/id/${objectMetaId}`)
export const updatePluginRegisterObjectType = (pluginConfigId, objectMetaId, data) =>
  req.post(`platform/v1/plugins/configs/${pluginConfigId}/interfaces/objectmetas/${objectMetaId}`, data)

export const getCertification = () => req.get(`platform/v1/plugin-certifications`)
export const deleteCertification = id => req.delete(`platform/v1/plugin-certifications/${id}`)
export const exportCertification = id => req.get(`platform/v1/plugin-certifications/${id}/export`)
export const importCertification = () => req.post(`platform/v1/plugin-certifications/import`)

export const productSerial = id => req.get(`platform/resource/servers/${id}/product-serial`)
