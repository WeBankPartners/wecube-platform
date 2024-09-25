import req from './base'

export const getMyMenus = () => req.get('/platform/v1/my-menus')
// init page

// flow
export const saveFlow = data => req.post('/platform/v1/process/definitions/deploy', data)
export const confirmSaveFlow = (continueToken, data) => {
  const params = {
    continue_token: continueToken
  }
  return req.post('/platform/v1/process/definitions/deploy', data, { params })
}
export const saveFlowDraft = data => req.post('/platform/v1/process/definitions/draft', data)
export const confirmSaveFlowDraft = (continueToken, data) => {
  const params = {
    continue_token: continueToken
  }
  return req.post('/platform/v1/process/definitions/draft', data, { params })
}
export const getAllFlow = (isIncludeDraft = true) => {
  let params = {}
  if (isIncludeDraft) {
    params = {
      permission: 'MGMT'
    }
  } else {
    params = {
      includeDraft: 0,
      permission: 'USE'
    }
  }
  return req.get('/platform/v1/process/definitions', { params })
}
export const getFlowDetailByID = id => req.get(`/platform/v1/process/definitions/${id}/detail`)
export const getFlowOutlineByID = id => req.get(`/platform/v1/process/definitions/${id}/outline`)

export const getTargetOptions = (pkgName, entityName) =>
  req.post(`/${pkgName}/entities/${entityName}/query`, {
    additionalFilters: []
  })
export const getTreePreviewData = (flowId, targetId, sessionId) =>
  req.get(`platform/v1/process/definitions/${flowId}/preview/entities/${targetId}?sessionId=${sessionId}`)
export const createFlowInstance = data => req.post('platform/v1/process/instances', data)
export const instancesWithPaging = data => req.post('platform/v1/process/instancesWithPaging', data)
export const getProcessInstances = params => req.get('platform/v1/process/instances', params)
export const getProcessInstance = id => req.get(`platform/v1/process/instances/${id}`)

export const retryProcessInstance = data => req.post('platform/v1/process/instances/proceed', data)
export const removeProcessDefinition = id => req.delete(`platform/v1/process/definitions/${id}`)

export const getParamsInfosByFlowIdAndNodeId = (flowId, nodeId) =>
  req.get(`platform/v1/process/definitions/${flowId}/tasknodes/${nodeId}`)

export const getFlowNodes = flowId => req.get(`platform/v1/process/definitions/${flowId}/tasknodes/briefs`)
export const getContextParametersNodes = (flowId, taskNodeId, contextParamNodes) => {
  const params = {
    taskNodeId,
    contextParamNodes: contextParamNodes.join(',')
  }
  return req.get(`/platform/v1/process/definitions/${flowId}/root-context-nodes/briefs`, { params })
}

export const getAllDataModels = () => req.get('platform/v1/models')

export const getPluginInterfaceList = () => req.get('platform/v1/plugins/interfaces/enabled')

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
export const getAllPluginPkgs = isRetrieveAllPluginPackages =>
  req.get(`/platform/v1/packages?all=${isRetrieveAllPluginPackages}`)
export const getRefCiTypeFrom = id => req.get(`/platform/v1/cmdb/ci-types/${id}/references/by`)
export const getRefCiTypeTo = id => req.get(`/platform/v1/cmdb/ci-types/${id}/references/to`)
export const getCiTypeAttr = id => req.get(`/platform/v1/cmdb/ci-types/${id}/attributes`)
export const getAvailableInstancesByPackageId = packageId => req.get(`/platform/v1/packages/${packageId}/instances`)
export const createPluginInstanceByPackageIdAndHostIp = (packageId, ip, port) =>
  req.post(`/platform/v1/packages/${packageId}/hosts/${ip}/ports/${port}/instance/launch`)

export const removePluginInstance = instanceId => req.delete(`/platform/v1/packages/instances/${instanceId}/remove`)
export const queryLog = data => req.post('/platform/v1/plugin/packages/instances/log', data)
export const getAvailableContainerHosts = () => req.get('/platform/v1/available-container-hosts')
export const getAvailablePortByHostIp = ip => req.get(`/platform/v1/hosts/${ip}/next-available-port`)
export const createEnumCategory = data =>
  req.post(`/platform/v1/cmdb/enum/category-types/${data.catTypeId}/categories/create`, data)
export const updateEnumCategory = data =>
  req.put(`/platform/v1/cmdb/enum/category-types/${data.catTypeId}/categories/${data.catId}`, data)
export const login = data => req.post('/auth/v1/api/login', data)
export const getEncryptKey = () => req.get('/auth/v1/api/seed')
// 获取可申请角色列表
export const getApplyRoles = data => req.get(`/auth/v1/roles?all=${data.all}&roleAdmin=${data.roleAdmin}`)
export const startApply = data => req.post('/auth/v1/roles/apply', data)
export const deleteApplyData = params => req.delete('/auth/v1/roles/apply', params)
export const registerUser = data => req.post('/auth/v1/users/register', data)

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
export const savePluginConfig = data => req.post('/platform/v1/plugins', data)
export const registPluginPackage = id => req.post(`/platform/v1/packages/register/${id}`)
export const queryDataBaseByPackageId = (id, payload) =>
  req.post(`/platform/v1/packages/${id}/resources/mysql/query`, payload)
export const queryStorageFilesByPackageId = (id, payload) =>
  req.get(`/platform/v1/packages/${id}/resources/s3/files`, payload)
export const getAllPluginPackageResourceFiles = () => req.get('/platform/v1/resource-files')
export const pullDynamicDataModel = name => req.get(`/platform/v1/models/package/${name}`)
export const getRefByIdInfoByPackageNameAndEntityName = (pkgName, entityName) =>
  req.get(`/platform/v1/models/package/${pkgName}/entity/${entityName}/refById`)
// export const getModelNodeDetail = (packageName, entityName, data) =>
//   req.post(`/${packageName}/entities/${entityName}/query`, data)
export const getModelNodeDetail = (packageName, entityName, data) =>
  req.post(`/platform/v1/packages/${packageName}/entities/${entityName}/query`, data)
export const getNodeBindings = id => req.get(`/platform/v1/process/instances/${id}/tasknode-bindings`)
export const getNodeContext = (procId, nodeId) =>
  req.get(`/platform/v1/process/instances/${procId}/tasknodes/${nodeId}/context`)
export const userCreate = data => req.post('/platform/v1/users/create', data)
export const removeUser = roleId => req.delete(`/platform/v1/users/${roleId}/delete`)
export const editUser = data => req.post(`/platform/v1/user/${data.username}/update`, data)
export const changePassword = data => req.post('/platform/v1/users/change-password', data)
export const getUserList = () => req.get('/platform/v1/users/retrieve')
export const deleteUser = id => req.delete(`/platform/v1/users/${id}/delete`)
export const roleCreate = data => req.post('/platform/v1/roles/create', data)
export const getRoleList = params => req.get('/platform/v1/roles/retrieve', { params })
export const getCurrentUserRoles = () => req.get('/platform/v1/users/roles')
export const deleteRole = id => req.delete(`/platform/v1/roles/${id}/delete`)
export const addRoleToUser = (id, data) => req.post(`/platform/v1/users/${id}/roles/grant`, data)
export const updateRole = (id, data) => req.post(`/platform/v1/roles/${id}/update`, data)
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
export const getRolesByCurrentUser = () => req.get('/platform/v1/users/roles')
export const getPermissionByProcessId = id => req.get(`/platform/v1/process/${id}/roles`)
export const updateFlowPermission = (id, data) => req.post(`/platform/v1/process/${id}/roles`, data)
export const deleteFlowPermission = (id, data) => req.delete(`/platform/v1/process/${id}/roles`, { data })
export const dmeAllEntities = data => req.post('/platform/v1/data-model/dme/all-entities', data)
export const dmeIntegratedQuery = data => req.post('/platform/v1/data-model/dme/integrated-query', data)
export const entityView = (packageName, entityName) =>
  req.get(`/platform/v1/models/package/${packageName}/entity/${entityName}/attributes`)
export const batchExecution = (url, data) => req.post(url, data)
export const deleteCollectionsRole = (id, data) => req.delete(`/platform/v1/roles/${id}/favorites`, { data })
export const addCollectionsRole = (id, data) => req.post(`/platform/v1/roles/${id}/favorites`, data)
export const getAllCollections = () => req.get('/platform/v1/roles/favorites/retrieve')
export const deleteCollections = id => req.delete(`/platform/v1/roles/favorites/${id}/delete`)
export const saveBatchExecution = data => req.post('/platform/v1/roles/favorites/create', data)
export const updateCollections = data => req.post('/platform/v1/roles/favorites/update', data)
export const getVariableScope = () => req.get('/platform/v1/system-variables/constant/system-variable-scope')
export const getPluginConfigsByPackageId = packageId => req.get(`/platform/v1/packages/${packageId}/plugin-configs`)
export const getInterfacesByPluginConfigId = configId => req.get(`/platform/v1/plugins/interfaces/${configId}`)
export const getEntityRefsByPkgNameAndEntityName = (pkgName, entityName) =>
  req.get(`/platform/v1/models/package/${pkgName}/entity/${entityName}`)
export const getPluginsByTargetEntityFilterRule = data => req.post('/platform/v1/plugins/query-by-target-entity', data)
export const getDataByNodeDefIdAndProcessSessionId = (nodeDefId, ProcessSessionId) =>
  req.get(`/platform/v1/process/instances/tasknodes/${nodeDefId}/session/${ProcessSessionId}/tasknode-bindings`)
export const setDataByNodeDefIdAndProcessSessionId = (nodeDefId, ProcessSessionId, data) =>
  req.post(`/platform/v1/process/instances/tasknodes/${nodeDefId}/session/${ProcessSessionId}/tasknode-bindings`, data)
export const getAllBindingsProcessSessionId = ProcessSessionId =>
  req.get(`/platform/v1/process/instances/tasknodes/session/${ProcessSessionId}/tasknode-bindings`)
export const getTargetModelByProcessDefId = id => req.get(`/platform/v1/process/definitions/${id}/root-entities`)
export const getPreviewEntitiesByInstancesId = id => req.get(`/platform/v1/process/instances/${id}/preview/entities`)
export const getPluginArtifacts = () => req.get('/platform/v1/plugin-artifacts')
export const pullPluginArtifact = data => req.post('/platform/v1/plugin-artifacts/pull-requests', data)
export const getPluginArtifactStatus = id => req.get(`/platform/v1/plugin-artifacts/pull-requests/${id}`)

export const exportPluginXMLWithId = id => {
  req.get(`/platform/v1/plugins/packages/export/${id}`)
}
export const updatePluginConfigRoleBinding = (id, data) => req.post(`/platform/v1/plugins/roles/configs/${id}`, data)
export const deletePluginConfigRoleBinding = (id, data) =>
  req.delete(`/platform/v1/plugins/roles/configs/${id}`, { data })
export const getApplicationVersion = () => req.get('/platform/v1/appinfo/version')
export const getConfigByPkgId = id => req.get(`/platform/v1/packages/${id}/plugin-config-outlines`)
export const updateConfigStatus = (id, data) =>
  req.post(`/platform/v1/packages/${id}/plugin-configs/enable-in-batch`, data)

export const batchExportConfig = (id, data) => req.post(`/platform/v1/plugins/packages/export-choose/${id}`, data)

export const resetPassword = data => req.post('platform/v1/users/reset-password', data)
// 终止执行
export const createWorkflowInstanceTerminationRequest = data =>
  req.post(`platform/v1/public/process/instances/${data.procInstId}/terminations`, data)
// 批量终止执行
export const batchWorkflowInstanceTermination = data =>
  req.post('/platform/v1/public/process/instances/batch-terminations', data)
export const getTaskNodeInstanceExecBindings = data =>
  req.get(`/platform/v1/process/instances/${data.procInstId}/tasknodes/${data.nodeInstId}/tasknode-bindings`)
export const updateTaskNodeInstanceExecBindings = data =>
  req.post(`platform/v1/process/instances/${data.procInstId}/tasknodes/${data.nodeInstId}/tasknode-bindings`, data.data)
export const getPluginRegisterObjectType = objectMetaId => req.get(`platform/v1/plugins/objectmetas/id/${objectMetaId}`)
export const updatePluginRegisterObjectType = (pluginConfigId, objectMetaId, data) =>
  req.post(`platform/v1/plugins/configs/${pluginConfigId}/interfaces/objectmetas/${objectMetaId}`, data)

export const getCertification = () => req.get('platform/v1/plugin-certifications')
export const deleteCertification = id => req.delete(`platform/v1/plugin-certifications/${id}`)
export const exportCertification = id => req.get(`platform/v1/plugin-certifications/${id}/export`)
export const importCertification = () => req.post('platform/v1/plugin-certifications/import')

export const productSerial = id => req.get(`platform/resource/servers/${id}/product-serial`)

export const getProcessList = () => req.get('platform/v1/statistics/process/definitions')
export const getTasknodesList = data => req.post('platform/v1/statistics/process/definitions/tasknodes/query', data)
export const getPluginTasknodesBindings = data =>
  req.post('platform/v1/statistics/process/definitions/service-ids/tasknode-bindings/query', data)
export const getTasknodesBindings = data =>
  req.post('platform/v1/statistics/process/definitions/tasknodes/tasknode-bindings/query', data)
export const getTasknodesReport = data =>
  req.post('platform/v1/statistics/process/definitions/executions/tasknodes/reports/query', data)
export const getReportDetails = data =>
  req.post('platform/v1/statistics/process/definitions/executions/tasknodes/report-details/query', data)
export const getPluginReportDetails = data =>
  req.post('platform/v1/statistics/process/definitions/executions/plugin/report-details/query', data)
export const getPluginReport = data =>
  req.post('platform/v1/statistics/process/definitions/executions/plugin/reports/query', data)
export const getFlowExecutePluginList = () =>
  req.get('platform/v1/statistics/process/definitions/tasknodes/service-ids')
export const getFlowExecuteOverviews = data =>
  req.post('platform/v1/statistics/process/definitions/executions/overviews/query', data)

export const getUserScheduledTasks = data => req.post('platform/v1/user-scheduled-tasks/query', data)
export const setUserScheduledTasks = data => req.post('platform/v1/user-scheduled-tasks/create', data)
export const deleteUserScheduledTasks = data => req.post('platform/v1/user-scheduled-tasks/delete', data)
export const resumeUserScheduledTasks = data => req.post('platform/v1/user-scheduled-tasks/resume', data)
export const stopUserScheduledTasks = data => req.post('platform/v1/user-scheduled-tasks/stop', data)
export const getScheduledTasksByStatus = data =>
  req.post('platform/v1/user-scheduled-tasks/process-instances/query', data)

export const getMetaData = params => req.post('platform/v1/plugins/configs/interfaces/param/metadata/query', params)

// 编排列表
export const flowList = data => req.post('platform/v1/process/definitions/list', data)
// 收藏编排
export const collectFlow = data => req.post('platform/v1/process/definitions/collect/add', data)
// 取消收藏编排
export const unCollectFlow = data => req.post('platform/v1/process/definitions/collect/del', data)
export const flowMgmt = data => req.post('platform/v1/process/definitions', data)
export const getFlowById = id => req.get(`platform/v1/process/definitions/${id}`)
export const getChildFlowList = params => req.get('/platform/v1/process/definitions', params)
// 获取子编排列表
export const getChildFlowListNew = data => req.post('/platform/v1/process/definitions/sub/list', data)
export const getParentFlowList = (id, data) => req.post(`/platform/v1/process/definitions/${id}/parent/get`, data)
export const flowNodeMgmt = data => req.post('platform/v1/process/definitions/tasknodes', data)
export const flowNodeDelete = (flowId, nodeId) =>
  req.delete(`platform/v1/process/definitions/${flowId}/tasknodes/${nodeId}`)
export const flowEdgeMgmt = data => req.post('platform/v1/process/definitions/link', data)
export const flowEdgeDelete = (flowId, edgeId) => req.delete(`platform/v1/process/definitions/${flowId}/link/${edgeId}`)
export const getPluginList = () => req.get('platform/v1/packages/name/list')
export const getPluginFunByRule = data => req.post('platform/v1/plugins/query-by-target-entity', data)
export const flowStatusChange = data => req.post('platform/v1/process/definitions/status', data)
export const flowRelease = flowId => req.post(`platform/v1/process/definitions/deploy/${flowId}`, {})
export const getAssociatedNodes = (flowId, nodeId) =>
  req.get(`platform/v1/process/definitions/${flowId}/tasknodes/${nodeId}/preorder`)
export const getNodeParams = (flowId, nodeId) =>
  req.get(`platform/v1/process/definitions/${flowId}/tasknodes/${nodeId}/parameters`)

export const flowBatchAuth = data => req.post('platform/v1/process/definitions/permission', data)
export const flowBatchChangeStatus = data => req.post('platform/v1/process/definitions/status', data)
export const flowCopy = (flowId, association) =>
  req.post(`platform/v1/process/definitions/${flowId}/copy/${association}`, {})
export const getSourceNode = flowId => req.get(`platform/v1/process/definitions/${flowId}/tasknodes/briefs`)
export const flowExport = data => req.post('platform/v1/process/definitions/export', data)
export const flowImport = data => req.post('platform/v1/process/definitions/import', data)
export const getNodeDetailById = (flowId, nodeId) =>
  req.get(`platform/v1/process/definitions/${flowId}/tasknodes/${nodeId}`)
// 编排列表转给我
export const transferToMe = data => req.post('platform/v1/process/definitions/handler/update', data)
// 批量执行重构
// 保存模板
export const saveBatchExecuteTemplate = data => req.post('platform/v1/batch-execution/templates', data)
// 模板列表
export const getBatchExecuteTemplateList = data => req.post('/platform/v1/batch-execution/templates/list', data)
// 更新批量执行模板权限
export const updateExecuteTemplateRole = data =>
  req.post('/platform/v1/batch-execution/templates/permission/update', data)
// 删除模板
export const deleteExecuteTemplate = id => req.delete(`/platform/v1/batch-execution/templates/${id}`)
// 模板详情
export const getBatchExecuteTemplateDetail = templateId =>
  req.get(`/platform/v1/batch-execution/templates/${templateId}`)
// 收藏模板
export const collectBatchTemplate = data => req.post('/platform/v1/batch-execution/templates/collect', data)
// 取消收藏模板
export const uncollectBatchTemplate = data => req.post('/platform/v1/batch-execution/templates/uncollect', data)
// 批量执行列表
export const getBatchExecuteList = data => req.post('/platform/v1/batch-execution/list', data)
// 批量执行历史
export const batchExecuteHistory = id => req.get(`/platform/v1/batch-execution/${id}`)
// 保存批量执行
export const saveBatchExecute = (url, data) => req.post(url, data)
// 批量执行获取密钥
export const getInputParamsEncryptKey = () => req.get('/platform/v1/batch-execution/seed')

// 申请列表-管理员视角
export const getProcessableList = data => req.post('/auth/v1/roles/apply/byhandler', data)
// 获取所有用户
export const getAllUser = () => req.get('/auth/v1/users')
// 获取角色下用户
export const getUserByRole = roleId => req.get(`/auth/v1/roles/${roleId}/users`)
// 从角色中删除用户
export const removeUserFromRole = (roleId, data) => req.post(`/auth/v1/roles/${roleId}/users/revoke`, data)
// 为角色添加用户
export const addUserForRole = (roleId, data) => req.post(`/auth/v1/roles/${roleId}/users`, data)
export const handleApplication = data => req.put('/auth/v1/roles/apply', data)
// 申请列表-用户视角
export const getApplyList = data => req.post('/auth/v1/roles/apply/byapplier', data)

// 编排执行-获取时间节点预计执行时间
export const getExecutionTimeByNodeId = nodeId => req.get(`/platform/v1/process/instances/node-message/${nodeId}/time`)
// 编排执行-获取判断节点可执行分支
export const getBranchByNodeId = nodeId => req.get(`/platform/v1/process/instances/node-message/${nodeId}/choose`)
// 编排执行-跳过时间节点
export const skipNode = data => req.post('/platform/v1/process/instances/proceed', data)
// 编排执行-执行判断分支
export const executeBranch = data => req.post('/platform/v1/process/instances/proceed', data)

// 编排执行-暂停、继续
export const pauseAndContinueFlow = data => req.post('/platform/v1/process/instances/proceed', data)

// 底座迁移
// 导出历史列表
export const getBaseMigrationExportList = data => req.post('/platform/v1/data/transfer/export/list', data)
// 导出列表查询条件
export const getBaseMigrationExportQuery = () => req.get('/platform/v1/data/transfer/export/list/options')
// 获取环境和产品
export const getExportBusinessList = data => req.post('/platform/v1/data/transfer/business/list', data)
// 保存环境和产品
export const saveEnvBusiness = data => req.post('/platform/v1/data/transfer/export/create', data)
// 更新环境和产品
export const updateEnvBusiness = data => req.post('/platform/v1/data/transfer/export/update', data)
// 查询所有编排设计
export const getAllExportFlows = () => req.get('/platform/v1/process/definitions/all')
// 查询所有批量执行
export const getAllExportBatch = () => req.get('/platform/v1/batch-execution/templates/all')
// 查询所有ITSM
export const getAllExportItsm = () => req.get('/taskman/api/v1/request-template/all')
// 执行导出
export const exportBaseMigration = data => req.post('/platform/v1/data/transfer/export', data)
// 导出详情
export const getExportDetail = params => req.get('/platform/v1/data/transfer/export/detail', params)
// 导入产品环境
export const getImportBusinessList = params => req.get('/platform/v1/data/transfer/import/business', params)
// 执行导入
export const saveImportData = data => req.post('/platform/v1/data/transfer/import', data)
// 导入详情
export const getImportDetail = params => req.get('/platform/v1/data/transfer/import/detail', params)
// 导入列表
export const getBaseMigrationImportList = data => req.post('/platform/v1/data/transfer/import/list', data)
// 导入列表查询条件
export const getBaseMigrationImportQuery = () => req.get('/platform/v1/data/transfer/import/list/options')
