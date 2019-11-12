import req from "./base";

export const getMyMenus = () => req.get("/platform/v1/my-menus");
// init page

// flow
export const saveFlow = data =>
  req.post("/platform/v1/process/definitions", data);
export const getAllFlow = () => req.get("/platform/v1/process/definitions");
export const getFlowDetailByID = id =>
  req.get(`process/definitions/definition/${id}`);
export const getFlowPreview = data =>
  req.post(
    `/platform/v1/process/definitions/definition/input-parameters/preview`,
    data
  );

// admin
export const deleteCiTypeLayer = layerId =>
  req.delete(`/platform/v1/cmdb/ci-type-layers/${layerId}`);
export const getAllUsers = () => req.get("/platform/v1/admin/users");
export const getAllRoles = () => req.get("/platform/v1/admin/roles");
export const getAllMenus = () => req.get("/platform/v1/admin/menus");
export const getAllPermissionEntryPoints = () =>
  req.get("/platform/v1/admin/permission-entry-points");
export const getRolesByUser = username =>
  req.get(`/platform/v1/admin/users/${username}/roles`);
export const getUsersByRole = roleId =>
  req.get(`/platform/v1/admin/roles/${roleId}/users`);
export const getPermissionsByRole = roleId =>
  req.get(`/platform/v1/admin/roles/${roleId}/permissions`);
export const getPermissionsByUser = username =>
  req.get(`/platform/v1/admin/users/${username}/permissions`);
export const addRole = data =>
  req.post(`/platform/v1/admin/roles/create`, data);
export const addUser = data =>
  req.post(`/platform/v1/admin/users/create`, data);
export const addUsersToRole = (users, roleId) =>
  req.post(`/platform/v1/admin/roles/${roleId}/users`, users);
export const romoveUsersFromRole = (users, roleId) =>
  req.delete(`/platform/v1/admin/roles/${roleId}/users`, { data: users });
export const addMenusToRole = (menuCodes, roleId) =>
  req.post(`/platform/v1/admin/roles/${roleId}/menu-permissions`, menuCodes);
export const removeMenusFromRole = (menuCodes, roleId) =>
  req.delete(`/platform/v1/admin/roles/${roleId}/menu-permissions`, {
    data: menuCodes
  });
export const addDataPermissionAction = (roleId, ciTypeId, actionCode) =>
  req.post(
    `/platform/v1/admin/roles/${roleId}/citypes/${ciTypeId}/actions/${actionCode}`
  );
export const removeDataPermissionAction = (roleId, ciTypeId, actionCode) =>
  req.delete(
    `/platform/v1/admin/roles/${roleId}/citypes/${ciTypeId}/actions/${actionCode}`
  );
export const addCITypeAttrsAttr = (roleId, ciTypeAttributeId, optionId) =>
  req.post(
    `/platform/v1/admin/roles/${roleId}/citype-attributes/${ciTypeAttributeId}/options/${optionId}`
  );
export const removeCITypeAttrsAttr = (roleId, ciTypeAttributeId, optionId) =>
  req.delete(
    `/platform/v1/admin/roles/${roleId}/citype-attributes/${ciTypeAttributeId}/options/${optionId}`
  );
export const getRoleCiTypeCtrlAttributesByRoleCiTypeId = roleCitypeId =>
  req.get(`/platform/v1/admin/role-citypes/${roleCitypeId}/ctrl-attributes`);
export const createRoleCiTypeCtrlAttributes = (roleCitypeId, data) =>
  req.post(
    `/platform/v1/admin/role-citypes/${roleCitypeId}/ctrl-attributes/create`,
    data
  );
export const updateRoleCiTypeCtrlAttributes = (roleCitypeId, data) =>
  req.post(
    `/platform/v1/admin/role-citypes/${roleCitypeId}/ctrl-attributes/update`,
    data
  );
export const deleteRoleCiTypeCtrlAttributes = (roleCitypeId, data) =>
  req.post(
    `/platform/v1/admin/role-citypes/${roleCitypeId}/ctrl-attributes/delete`,
    data
  );

//enum
export const getEnumList = data =>
  req.post(`/platform/v1/cmdb/enum/codes/query`, data);

export const deleteEnumRecord = (catTypeId, catId, codeId) =>
  req.delete(
    `/platform/v1/cmdb/enum/category-types/${catTypeId}/categories/${catId}/codes/${codeId}`
  );
export const addEnumRecord = (catTypeId, data) =>
  req.post(
    `/platform/v1/cmdb/enum/category-types/${catTypeId}/categories/${data.catId}/codes/create`,
    data
  );
export const getEnumCatList = () =>
  req.get(`/platform/v1/cmdb/enum/all-categories`);
export const getEnumCodesByCategoryId = (catTypeId, catId) =>
  req.get(
    `/platform/v1/cmdb/enum/category-types/${catTypeId}/categories/${catId}/codes`
  );

export const getEnumCategoriesByTypeId = catTypeId =>
  req.get(`/platform/v1/cmdb/enum/category-types/${catTypeId}/categories`);

export const queryEnumCategories = data =>
  req.post(`/platform/v1/cmdb/enum/categories/query`, data);

export const queryEnumCodes = (catTypeId, catId, data) =>
  req.post(
    `/platform/v1/cmdb/enum/category-types/${catTypeId}/categories/${catId}/codes/query`,
    data
  );

//CI
export const getCITableHeader = ciTypeId =>
  req.get(`/platform/v1/cmdb/ci-types/${ciTypeId}/header`);
export const updateCIRecord = (ciTypeId, data) =>
  req.put(`/platform/v1/cmdb/ci-types/${ciTypeId}/ci-data/${data.guid}`, data);
export const addCIRecord = (ciTypeId, data) =>
  req.post(`/platform/v1/cmdb/ci-types/${ciTypeId}/ci-data/create`, data);
export const deleteCIRecord = (ciTypeId, ciId) =>
  req.delete(`/platform/v1/cmdb/ci-types/${ciTypeId}/ci-data/${ciId}`);
// plugin manager
export const getAllPluginPkgs = () => req.get("/platform/v1/packages");
export const getPluginInterfaces = id =>
  req.get(`/platform/v1/plugin/configs/${id}/interfaces`);
export const getRefCiTypeFrom = id =>
  req.get(`/platform/v1/cmdb/ci-types/${id}/references/by`);
export const getRefCiTypeTo = id =>
  req.get(`/platform/v1/cmdb/ci-types/${id}/references/to`);
export const getCiTypeAttr = id =>
  req.get(`/platform/v1/cmdb/ci-types/${id}/attributes`);
export const preconfigurePluginPackage = id =>
  req.post(`/platform/v1/plugin/packages/${id}/preconfigure`);
export const getAllInstancesByPackageId = packageId =>
  req.get(`/platform/v1/instances/packages/${packageId}`);
export const getAvailableInstancesByPackageId = packageId =>
  req.get(`/platform/v1/packages/${packageId}/instances`);
export const createPluginInstanceByPackageIdAndHostIp = (packageId, ip, port) =>
  req.post(
    `/platform/v1/packages/${packageId}/hosts/${ip}/ports/${port}/instance/launch`
  );
export const savePluginInstance = data =>
  req.post(
    `/platform/v1/plugin/configs/${data.configId}/save?cmdbCiTypeId=${data.cmdbCiTypeId}&cmdbCiTypeName=${data.cmdbCiTypeName}`,
    data.pluginRegisteringModels
  );
export const decommissionPluginConfig = configId =>
  req.post(`/platform/v1/plugin/configs/${configId}/decommission`);
export const releasePluginConfig = configId =>
  req.post(`/platform/v1/plugin/configs/${configId}/release`);
export const removePluginInstance = instanceId =>
  req.delete(`/platform/v1/packages/instances/${instanceId}`);
export const queryLog = data =>
  req.post(`/platform/v1/plugin/packages/instances/log`, data);
export const getPluginInstanceLogDetail = (id, data) =>
  req.post(`/platform/v1/plugin/packages/instances/${id}/log-detail`, data);
export const getCiTypeAttrRefAndSelect = id =>
  req.get(
    `/platform/v1/cmdb/ci-types/${id}/attributes?accept-input-types=select,ref`
  );
export const getAvailableContainerHosts = () =>
  req.get(`/platform/v1/available-container-hosts`);
export const getAvailablePortByHostIp = ip =>
  req.get(`/platform/v1/hosts/${ip}/next-available-port`);
export const getLatestOnlinePluginInterfaces = ciTypeId =>
  req.get(
    `/platform/v1/plugin/latest-online-interfaces?ci-type-id=${ciTypeId || ""}`
  );

// CI design

export const implementCiType = (id, op) =>
  req.post(`/platform/v1/cmdb/ci-types/${id}/implement?operation=${op}`);
export const implementCiAttr = (ciTypeId, ciAttrId, op) =>
  req.post(
    `/platform/v1/cmdb/ci-types/${ciTypeId}/attributes/${ciAttrId}/implement?operation=${op}`
  );
export const getAllCITypes = () => req.get("/platform/v1/cmdb/ci-types");
export const getAllEnumCategoryTypes = () =>
  req.get("/platform/v1/cmdb/enum/category-types");
export const getAllEnumCategories = () =>
  req.get("/platform/v1/cmdb/enum/category-types/categories");
export const createEnumCategory = data =>
  req.post(
    `/platform/v1/cmdb/enum/category-types/${data.catTypeId}/categories/create`,
    data
  );
export const updateEnumCategory = data =>
  req.put(
    `/platform/v1/cmdb/enum/category-types/${data.catTypeId}/categories/${data.catId}`,
    data
  );
export const getAllCITypesByLayerWithAttr = data => {
  const status = data.toString();
  return req.get(
    `/platform/v1/cmdb/ci-types?group-by=layer&with-attributes=yes&status=${status}`
  );
};
export const getAllLayers = () => req.get("/platform/v1/cmdb/ci-type-layers");
export const createLayer = data =>
  req.post("/platform/v1/cmdb/ci-type-layers/create", data);
export const deleteLayer = id =>
  req.delete(`/platform/v1/cmdb/ci-type-layers/${id}`);
export const updateLayer = data =>
  req.post(`/platform/v1/cmdb/enum/codes/update`, data);
export const swapLayerPosition = (layerId, targetLayerId) =>
  req.post(
    `/platform/v1/cmdb/ci-type-layers/${layerId}/swap-position?target-layer-id=${targetLayerId}`
  );
export const deleteCITypeByID = id =>
  req.delete(`/platform/v1/cmdb/ci-types/${id}`);
export const deleteAttr = (ciTypeId, attrId) =>
  req.delete(`/platform/v1/cmdb/ci-types/${ciTypeId}/attributes/${attrId}`);
export const applyCiTypes = id =>
  req.post(`/platform/v1/cmdb/ci-types/${id}/apply`);
export const updateCIType = (id, data) =>
  req.put(`/platform/v1/cmdb/ci-types/${id}`, data);
export const createNewCIType = data =>
  req.post(`/platform/v1/cmdb/ci-types/create`, data);
export const createNewCIAttr = (id, data) =>
  req.post(`/platform/v1/cmdb/ci-types/${id}/attributes/create`, data);
export const updateCIAttr = (attrId, ciTypeId, data) =>
  req.put(`/platform/v1/cmdb/ci-types/${ciTypeId}/attributes/${attrId}`, data);
export const applyCIAttr = (ciTypeId, attrIds) =>
  req.post(
    `/platform/v1/cmdb/ci-types/${ciTypeId}/ci-type-attributes/apply`,
    attrIds
  );

export const swapCiTypeAttributePosition = (ciTypeId, attrId, targetAttrId) => {
  return req.post(
    `/platform/v1/cmdb/ci-types/${ciTypeId}/attributes/${attrId}/swap-position?target-attribute-id=${targetAttrId}`
  );
};
export const getAllInputTypes = () =>
  req.get(
    "/platform/v1/cmdb/static-data/available-ci-type-attribute-input-types"
  );
export const getEnumByCIType = id =>
  req.get(
    `/platform/v1/cmdb/enum/category-types/categories/query-by-multiple-types?ci-type-id=${id}&types=common-private`
  );

export const getTableStatus = () =>
  req.get("/platform/v1/cmdb/static-data/available-ci-type-table-status");

export const getCiTypes = data =>
  req.get(`/platform/v1/cmdb/ci-types?${data.key}=${data.value}`);

export const getAllIdcDesignTrees = () =>
  req.get(`/platform/v1/cmdb/idc-designs/all-design-trees`);

export const getAllIdcImplementTrees = () =>
  req.get(`/platform/v1/cmdb/idc-implements/all-implement-trees`);

export const getAllZoneLinkGroupByIdc = () =>
  req.get(`/platform/v1/cmdb/all-zone-link`);

export const getAllZoneLinkDesignGroupByIdcDesign = () =>
  req.get(`/platform/v1/cmdb/all-zone-link-design`);

export const getAllDesignTreeFromSystemDesign = id =>
  req.get(
    `/platform/v1/cmdb/trees/all-design-trees/from-system-design?system-design-guid=${id}`
  );

export const saveAllDesignTreeFromSystemDesign = id =>
  req.post(
    `/platform/v1/cmdb/trees/all-design-trees/from-system-design/save?system-design-guid=${id}`
  );

export const getAllIdcDesignData = () =>
  req.get(`/platform/v1/cmdb/ci-data/all-idc-design`);

export const getAllIdcData = () => req.get(`/platform/v1/cmdb/ci-data/all-idc`);

export const getIdcDesignTreeByGuid = data =>
  req.post(`/platform/v1/cmdb/data-tree/query-idc-design-tree`, data);

export const getIdcImplementTreeByGuid = data =>
  req.post(`/platform/v1/cmdb/data-tree/query-idc-tree`, data);

export const getApplicationFrameworkDesignDataTree = guid =>
  req.get(
    `/platform/v1/cmdb/data-tree/application-framework-design?system-design-guid=${guid}`
  );

export const getApplicationDeploymentDesignDataTree = (guid, codeId) =>
  req.get(
    `/platform/v1/cmdb/data-tree/application-deployment-design?env-code=${codeId}&system-design-guid=${guid}`
  );

// basic data page
export const getAllSystemEnumCodes = data => {
  return req.post(`/platform/v1/cmdb/enum/system/codes`, data);
};
export const getSystemCategories = () =>
  req.get(`/platform/v1/cmdb/enum/system-categories`);

export const getAllNonSystemEnumCodes = data => {
  return req.post(`/platform/v1/cmdb/enum/non-system/codes`, data);
};
export const getNonSystemCategories = () =>
  req.get(`/platform/v1/cmdb/enum/non-system-categories`);

export const getEffectiveStatus = () =>
  req.get(`/platform/v1/cmdb/static-data/effective-status`);
export const createEnumCode = data => {
  return req.post(
    `/platform/v1/cmdb/enum/category-types/0/categories/${data.catId}/codes/create`,
    data
  );
};
export const updateEnumCode = data => {
  return req.post(`/platform/v1/cmdb/enum/codes/update`, data);
};
export const getGroupListByCodeId = id =>
  req.get(`/platform/v1/cmdb/enum/categories/${id}/group-list`);
export const deleteEnumCodes = data => {
  return req.post(`/platform/v1/cmdb/enum/codes/delete`, data);
};
export const queryCiData = data => {
  return req.post(
    `/platform/v1/cmdb/ci-types/${data.id}/ci-data/query`,
    data.queryObject
  );
};
export const getCiTypeAttributes = id => {
  return req.get(`/platform/v1/cmdb/ci-types/${id}/attributes`);
};
export const deleteCiDatas = data => {
  return req.post(
    `/platform/v1/cmdb/ci-types/${data.id}/ci-data/batch-delete`,
    data.deleteData
  );
};
export const createCiDatas = data => {
  return req.post(
    `/platform/v1/cmdb/ci-types/${data.id}/ci-data/batch-create`,
    data.createData
  );
};
export const updateCiDatas = data => {
  return req.post(
    `/platform/v1/cmdb/ci-types/${data.id}/ci-data/batch-update`,
    data.updateData
  );
};

export const operateCiState = (ciTypeId, guid, op) => {
  const payload = [{ ciTypeId, guid }];
  return req.post(
    `/platform/v1/cmdb/ci/state/operate?operation=${op}`,
    payload
  );
};

// ci integrate query
export const getQueryNames = id => {
  return req.get(`/platform/v1/cmdb/intQuery/ciType/${id}/search`);
};
export const queryIntHeader = id => {
  return req.get(`/platform/v1/cmdb/intQuery/${id}/header`);
};
export const excuteIntQuery = (id, data) => {
  return req.post(`/platform/v1/cmdb/intQuery/${id}/execute`, data);
};

export const fetchIntQueryById = (ciTypeId, queryId) => {
  return req.get(`/platform/v1/cmdb/intQuery/ciType/${ciTypeId}/${queryId}`);
};
export const saveIntQuery = (ciTypeId, queryName, data) => {
  return req.post(
    `/platform/v1/cmdb/intQuery/ciType/${ciTypeId}/${queryName}/save`,
    data
  );
};
export const updateIntQuery = (queryId, data) => {
  return req.post(`/platform/v1/cmdb/intQuery/${queryId}/update`, data);
};
export const deleteIntQuery = (ciTypeId, queryId) => {
  return req.post(
    `/platform/v1/cmdb/intQuery/ciType/${ciTypeId}/${queryId}/delete`
  );
};
//batch-job management
export const getQueryNamesByAttrId = (id, attrId) => {
  return req.get(
    `/platform/v1/cmdb/intQuery/ciType/${id}/search?tailAttrId=${attrId}`
  );
};
export const createBatchJob = data => {
  return req.post(`/platform/v1/batch-job/create`, data);
};
export const execBatchJob = batchJobId => {
  return req.post(`/platform/v1/batch-job/${batchJobId}/execute`);
};
export const getBatchJobExecLog = data => {
  return req.post(`/platform/v1/batch-job/search-text`, data);
};
export const getBatchJobExecLogDetail = data => {
  return req.post(`/platform/v1/batch-job/get-context`, data);
};
// artifact manage
export const getPackageCiTypeId = () =>
  req.get("/platform/v1/artifact/getPackageCiTypeId");
export const getSystemDesignVersions = () => {
  return req.get(`/platform/v1/artifact/system-design-versions`);
};
export const getSystemDesignVersion = version => {
  return req.get(`/platform/v1/artifact/system-design-versions/${version}`);
};
export const queryPackages = (guid, data) => {
  return req.post(
    `/platform/v1/artifact/unit-designs/${guid}/packages/query`,
    data
  );
};
export const deactivePackage = (guid, packageId) => {
  return req.post(
    `/platform/v1/artifact/unit-designs/${guid}/packages/${packageId}/deactive`,
    {}
  );
};
export const activePackage = (guid, packageId) => {
  return req.post(
    `/platform/v1/artifact/unit-designs/${guid}/packages/${packageId}/active`,
    {}
  );
};
export const getFiles = (guid, packageId, data) => {
  return req.post(
    `/platform/v1/artifact/unit-designs/${guid}/packages/${packageId}/files/query`,
    data
  );
};
export const getKeys = (guid, packageId, data) => {
  return req.post(
    `/platform/v1/artifact/unit-designs/${guid}/packages/${packageId}/property-keys/query`,
    data
  );
};
export const saveConfigFiles = (guid, packageId, data) => {
  return req.post(
    `/platform/v1/artifact/unit-designs/${guid}/packages/${packageId}/save`,
    data
  );
};
export const artifactManagementCreateEnumCode = data => {
  return req.post(
    `/platform/v1/cmdb/enum/category-types/2/categories/18/codes/create`,
    data
  );
};
export const saveDiffConfigEnumCodes = data =>
  req.post("/platform/v1/artifact/enum/codes/diff-config/save", data);

export const getDiffConfigEnumCodes = () =>
  req.get("/platform/v1/artifact/enum/codes/diff-config/query");

// deployment design
export const getSystemDesigns = () => {
  return req.get(`/platform/v1/cmdb/system-designs`);
};
export const previewDeployGraph = data => {
  return req.post(`/platform/v1/process/definitions/preview`, data);
};
export const getAllDeployTreesFromDesignCi = (id, env) => {
  return req.get(
    `/platform/v1/cmdb/trees/all-deploy-trees/from-subsys?env-code=${env}&system-design-guid=${id}`
  );
};
export const startProcessInstancesWithCiDataInbatch = data => {
  return req.post(`/platform/v1/process/inbatch/instances`, data);
};
export const getDeployDesignTabs = () =>
  req.get(`/platform/v1/cmdb/deploy-designs/tabs`);

export const getDeployCiData = (data, payload) =>
  req.post(
    `/platform/v1/cmdb/deploy-designs/tabs/ci-data?code-id=${data.codeId}&env-code=${data.envCode}&system-design-guid=${data.systemDesignGuid}`,
    payload
  );

export const getArchitectureDesignTabs = () =>
  req.get("/platform/v1/cmdb/architecture-designs/tabs");
export const getArchitectureCiDatas = (tabId, sysId, payload) =>
  req.post(
    `/platform/v1/cmdb/architecture-designs/tabs/ci-data?code-id=${tabId}&system-design-guid=${sysId}`,
    payload
  );
//deployment
export const listProcessTransactions = () =>
  req.get("/platform/v1/process/process-transactions");
export const refreshStatusesProcessTransactions = id =>
  req.get(`/platform/v1/process/process-transactions/${id}/outlines`);
//filterRules
export const queryReferenceEnumCodes = data =>
  req.post(
    `/platform/v1/cmdb/referenceEnumCodes/${data.attrId}/query`,
    data.params
  );
export const queryReferenceCiData = data =>
  req.post(
    `/platform/v1/cmdb/referenceCiData/${data.attrId}/query`,
    data.queryObject
  );
//worker flow execution
export const previewProcessDefinition = data =>
  req.post("/platform/v1/process/definitions/definition/preview", data);
export const startProcessInstanceWithCiData = data =>
  req.post("/platform/v1/process/instances", data);
export const refreshProcessInstanceStatus = id =>
  req.get(`/platform/v1/process/instances/${id}/outline`);
export const restartProcessInstance = data =>
  req.post("/platform/v1/process/instances/restart", data);
export const login = data =>
  req.post("/platform/v1/auth/platform/v1/login", data);
export const deletePluginPkg = id =>
  req.post(`/platform/v1/packages/decommission/${id}`);
export const getPluginPkgDataModel = id =>
  req.get(`/platform/v1/packages/${id}/models`);
export const getPluginPkgDependcy = id =>
  req.get(`/platform/v1/packages/${id}/dependencies`);
export const getAllPluginByPkgId = id =>
  req.get(`/platform/v1/packages/${id}/plugins`);
export const getMenuInjection = id =>
  req.get(`/platform/v1/packages/${id}/menus`);
export const getSysParams = id =>
  req.get(`/platform/v1/packages/${id}/system-parameters`);
export const getRuntimeResource = id =>
  req.get(`/platform/v1/packages/${id}/runtime-resources`);
export const getAuthSettings = id =>
  req.get(`/platform/v1/packages/${id}/authorities`);
export const getAllDataModels = id => req.get(`/platform/v1/models`);
export const registerPlugin = id =>
  req.post(`/platform/v1/plugins/enable/${id}`);
export const deletePlugin = id =>
  req.post(`/platform/v1/plugins/disable/${id}`);
export const savePluginConfig = data => req.post(`/platform/v1/plugins`, data);
export const registPluginPackage = id =>
  req.post(`/platform/v1/packages/register/${id}`);
export const queryDataBaseByPackageId = (id, payload) =>
  req.post(`/platform/v1/packages/${id}/resources/mysql/query`, payload);
export const queryStorageFilesByPackageId = (id, payload) =>
  req.get(`/platform/v1/packages/${id}/resources/s3/files`, payload);
export const getAllPluginPackageResourceFiles = () =>
  req.get("/platform/v1/resource-files");
