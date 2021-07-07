package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.workflow.FlowNodeDefDto;
import com.webank.wecube.platform.core.dto.workflow.GraphNodeDto;
import com.webank.wecube.platform.core.dto.workflow.InterfaceParameterDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefOutlineDto;
import com.webank.wecube.platform.core.dto.workflow.ProcessDataPreviewDto;
import com.webank.wecube.platform.core.dto.workflow.RequestObjectDto;
import com.webank.wecube.platform.core.dto.workflow.RequestObjectDto.RequestParamObjectDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefObjectBindInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeExecContextDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeInstObjectBindInfoDto;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaceParameters;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaces;
import com.webank.wecube.platform.core.entity.workflow.GraphNodeEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingTmpEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecParamEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecRequestEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.repository.workflow.GraphNodeMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcDefInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcExecBindingMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcExecBindingTmpMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcInstInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcRoleBindingMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeDefInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeExecParamMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeExecRequestMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeInstInfoMapper;
import com.webank.wecube.platform.core.service.dme.EntityOperationRootCondition;
import com.webank.wecube.platform.core.service.dme.EntityTreeNodesOverview;
import com.webank.wecube.platform.core.service.dme.StandardEntityDataNode;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationService;
import com.webank.wecube.platform.core.service.plugin.PluginConfigMgmtService;

/**
 * 
 * @author gavin
 *
 */
@Service
public class WorkflowDataService extends AbstractWorkflowService{
    private static final Logger log = LoggerFactory.getLogger(WorkflowDataService.class);

    public static final String MASKED_VALUE = "***MASK***";

    public static final String CALLBACK_PARAMETER_KEY = "callbackParameter";

    @Autowired
    private WorkflowProcDefService workflowProcDefService;

    @Autowired
    private TaskNodeDefInfoMapper taskNodeDefInfoRepository;

    @Autowired
    private TaskNodeInstInfoMapper taskNodeInstInfoRepository;

    @Autowired
    private StandardEntityOperationService standardEntityOperationService;

    @Autowired
    protected PluginConfigMgmtService pluginConfigMgmtService;

    @Autowired
    protected TaskNodeExecParamMapper taskNodeExecParamRepository;

    @Autowired
    protected TaskNodeExecRequestMapper taskNodeExecRequestRepository;

    @Autowired
    protected ProcExecBindingTmpMapper procExecBindingTmpRepository;

    @Autowired
    protected ProcDefInfoMapper procDefInfoRepository;

    @Autowired
    protected GraphNodeMapper graphNodeRepository;

    @Autowired
    protected ProcExecBindingMapper procExecBindingMapper;

    @Autowired
    protected ProcInstInfoMapper procInstInfoMapper;

    @Autowired
    protected ProcRoleBindingMapper procRoleBindingMapper;

    @Autowired
    @Qualifier("userJwtSsoTokenRestTemplate")
    protected RestTemplate userJwtSsoTokenRestTemplate;

    /**
     * 
     * @param procInstId
     * @param nodeInstId
     * @return
     */
    public List<TaskNodeInstObjectBindInfoDto> getTaskNodeInstanceExecBindings(Integer procInstId, Integer nodeInstId) {

        List<TaskNodeInstObjectBindInfoDto> bindInfoDtos = new ArrayList<>();
        List<ProcExecBindingEntity> bindingEntities = procExecBindingMapper.selectAllTaskNodeBindings(procInstId,
                nodeInstId);
        if (bindingEntities == null || bindingEntities.isEmpty()) {
            return bindInfoDtos;
        }

        for (ProcExecBindingEntity e : bindingEntities) {
            TaskNodeInstObjectBindInfoDto d = new TaskNodeInstObjectBindInfoDto();
            d.setId(e.getId());
            d.setEntityDataId(e.getEntityDataId());
            d.setEntityDisplayName(e.getEntityDataName());
            d.setEntityTypeId(e.getEntityTypeId());
            d.setBound(e.getBindFlag());
            d.setConfirmToken(e.getConfirmToken());
            d.setNodeDefId(e.getNodeDefId());
            d.setNodeInstId(e.getTaskNodeInstId());
            d.setProcInstId(e.getProcInstId());
            d.setEntityDisplayName(e.getEntityDataName());
            if (StringUtils.isNoneBlank(d.getEntityTypeId())) {
                String[] parts = d.getEntityTypeId().trim().split(":");
                if (parts.length == 1) {
                    d.setEntityName(parts[0]);
                } else if (parts.length == 2) {
                    d.setPackageName(parts[0]);
                    d.setEntityName(parts[1]);
                }
            }

            bindInfoDtos.add(d);
        }

        return bindInfoDtos;
    }

    /**
     * 
     * @param procInstId
     * @param nodeInstId
     * @param bindings
     */
    @Transactional
    public void updateTaskNodeInstanceExecBindings(Integer procInstId, Integer nodeInstId,
            List<TaskNodeInstObjectBindInfoDto> bindingInfoDtos) {
        ProcInstInfoEntity procInstInfo = procInstInfoMapper.selectByPrimaryKey(procInstId);
        if (procInstInfo == null) {
            String errMsg = String.format("Such process instance with id [:%s] does not exist.", procInstId);
            throw new WecubeCoreException("3197", errMsg, procInstId);
        }

        if (ProcInstInfoEntity.COMPLETED_STATUS.equals(procInstInfo.getStatus())
                || ProcInstInfoEntity.INTERNALLY_TERMINATED_STATUS.equals(procInstInfo.getStatus())) {
            String errMsg = "Cannot update task node bindings due to completed process instance state.";
            throw new WecubeCoreException(errMsg);
        }

        TaskNodeInstInfoEntity nodeInstInfo = taskNodeInstInfoRepository.selectByPrimaryKey(nodeInstId);
        if (nodeInstInfo == null) {
            String errMsg = String.format("Such node instance with id [:%s] does not exist.", nodeInstId);
            throw new WecubeCoreException("3323", errMsg, nodeInstId);
        }

        if (TaskNodeInstInfoEntity.COMPLETED_STATUS.equals(nodeInstInfo.getStatus())) {
            String errMsg = "Cannot update task node bindings due to completed task node instance state.";
            throw new WecubeCoreException(errMsg);
        }

        if (bindingInfoDtos == null || bindingInfoDtos.isEmpty()) {
            log.info("object bind infos to update is empty.");
            return;
        }

        List<ProcRoleBindingEntity> procRoleBinds = procRoleBindingMapper
                .selectAllByProcIdAndPermission(procInstInfo.getProcDefId(), ProcRoleBindingEntity.USE);

        if (procRoleBinds == null || procRoleBinds.isEmpty()) {
            throw new WecubeCoreException("Lack of permission to update task node bindings.");
        }

        Set<String> currUserRoles = AuthenticationContextHolder.getCurrentUserRoles();
        if (currUserRoles == null) {
            currUserRoles = new HashSet<String>();
        }

        boolean lackOfPermission = true;
        for (ProcRoleBindingEntity procRoleBind : procRoleBinds) {
            if (currUserRoles.contains(procRoleBind.getRoleName())) {
                lackOfPermission = false;
                break;
            }
        }

        if (lackOfPermission) {
            throw new WecubeCoreException("Lack of permission to update task node bindings.");
        }

        for (TaskNodeInstObjectBindInfoDto bindInfoDto : bindingInfoDtos) {
            ProcExecBindingEntity bindEntity = procExecBindingMapper.selectByPrimaryKey(bindInfoDto.getId());
            if (bindEntity == null) {
                String errMsg = String.format("Such exec binding does not exist with id:%s", bindInfoDto.getId());
                throw new WecubeCoreException(errMsg);
            }

//            if (bindInfoDto.getBound().equals(bindEntity.getBindFlag())) {
//                continue;
//            }

            bindEntity.setBindFlag(bindInfoDto.getBound());
            bindEntity.setConfirmToken(bindInfoDto.getConfirmToken());

            procExecBindingMapper.updateByPrimaryKey(bindEntity);
        }
    }

    /**
     * 
     * @param procInstId
     * @return
     */
    public ProcessDataPreviewDto generateProcessDataPreviewForProcInstance(Integer procInstId) {
        List<GraphNodeEntity> gNodeEntities = graphNodeRepository.selectAllByProcInstId(procInstId);
        ProcessDataPreviewDto result = new ProcessDataPreviewDto();
        if (gNodeEntities == null || gNodeEntities.isEmpty()) {
            return result;
        }

        result.setProcessSessionId(gNodeEntities.get(0).getProcSessId());

        List<GraphNodeDto> gNodes = new ArrayList<>();
        for (GraphNodeEntity entity : gNodeEntities) {
            GraphNodeDto gNode = new GraphNodeDto();
            gNode.setDataId(entity.getDataId());
            gNode.setDisplayName(entity.getDisplayName());
            gNode.setEntityName(entity.getEntityName());
            gNode.setId(entity.getGraphNodeId());
            gNode.setPackageName(entity.getPkgName());
            gNode.setPreviousIds(GraphNodeEntity.convertIdsStringToList(entity.getPrevIds()));
            gNode.setSucceedingIds(GraphNodeEntity.convertIdsStringToList(entity.getSuccIds()));

            gNodes.add(gNode);
        }

        result.addAllEntityTreeNodes(gNodes);

        return result;
    }

    /**
     * 
     * @param procDefKey
     * @return
     */
    public List<Map<String, Object>> getProcessDefinitionRootEntitiesByProcDefKey(String procDefKey) {
        if (StringUtils.isBlank(procDefKey)) {
            throw new WecubeCoreException("3186", "Process definition ID cannot be blank.");
        }

        List<ProcDefInfoEntity> procDefEntities = procDefInfoRepository
                .selectAllDeployedProcDefsByProcDefKey(procDefKey, ProcDefInfoEntity.DEPLOYED_STATUS);

        List<Map<String, Object>> result = new ArrayList<>();
        if (procDefEntities == null || procDefEntities.isEmpty()) {
            return result;
        }

        Collections.sort(procDefEntities, new Comparator<ProcDefInfoEntity>() {

            @Override
            public int compare(ProcDefInfoEntity o1, ProcDefInfoEntity o2) {
                if (o1.getProcDefVer() == null && o2.getProcDefVer() == null) {
                    return 0;
                }

                if (o1.getProcDefVer() == null && o2.getProcDefVer() != null) {
                    return -1;
                }

                if (o1.getProcDefVer() != null && o2.getProcDefVer() == null) {
                    return 1;
                }

                if (o1.getProcDefVer() == o2.getProcDefVer()) {
                    return 0;
                }

                return o1.getProcDefVer() > o2.getProcDefVer() ? -1 : 1;
            }

        });

        ProcDefInfoEntity procDef = procDefEntities.get(0);

        String rootEntityExpr = procDef.getRootEntity();
        if (StringUtils.isBlank(rootEntityExpr)) {
            return result;
        }

        Map<Object, Object> externalCacheMap = new HashMap<Object, Object>();

        EntityOperationRootCondition condition = new EntityOperationRootCondition(rootEntityExpr, null);

        List<Map<String, Object>> retRecords = standardEntityOperationService.queryAttributeValuesOfLeafNode(condition,
                userJwtSsoTokenRestTemplate, externalCacheMap);

        if (retRecords == null) {
            return result;
        }

        result.addAll(retRecords);

        return result;
    }

    /**
     * 
     * @param procDefId
     * @return
     */
    public List<Map<String, Object>> getProcessDefinitionRootEntities(String procDefId) {
        if (StringUtils.isBlank(procDefId)) {
            throw new WecubeCoreException("3186", "Process definition ID cannot be blank.");
        }
        ProcDefInfoEntity procDef = procDefInfoRepository.selectByPrimaryKey(procDefId);
        if (procDef == null) {
            throw new WecubeCoreException("3187",
                    String.format("Cannot find such process definition with ID [%s]", procDefId), procDefId);
        }

        List<Map<String, Object>> result = new ArrayList<>();

        String rootEntityExpr = procDef.getRootEntity();
        if (StringUtils.isBlank(rootEntityExpr)) {
            return result;
        }

        List<Map<String, Object>> retRecords = standardEntityOperationService.queryAttributeValuesOfLeafNode(
                new EntityOperationRootCondition(rootEntityExpr, null), userJwtSsoTokenRestTemplate, null);

        if (retRecords == null) {
            return result;
        }

        result.addAll(retRecords);

        return result;
    }

    /**
     * 
     * @param nodeDefId
     * @param processSessionId
     * @param bindings
     */
    public void updateProcessInstanceExecBindingsOfSession(String nodeDefId, String processSessionId,
            List<TaskNodeDefObjectBindInfoDto> bindings) {

        List<ProcExecBindingTmpEntity> bindingEntities = procExecBindingTmpRepository
                .selectAllNodeBindingsByNodeAndSession(nodeDefId, processSessionId);

        if (bindingEntities == null || bindingEntities.isEmpty()) {
            return;
        }

        List<ProcExecBindingTmpEntity> bindingsSelected = new ArrayList<ProcExecBindingTmpEntity>();

        if (bindings != null) {
            for (TaskNodeDefObjectBindInfoDto dto : bindings) {
                ProcExecBindingTmpEntity existEntity = findProcExecBindingTmpEntityWithNodeAndEntity(dto.getNodeDefId(),
                        dto.getEntityTypeId(), dto.getEntityDataId(), bindingEntities);
                if (existEntity != null) {
                    bindingsSelected.add(existEntity);

                    existEntity.setIsBound(ProcExecBindingTmpEntity.BOUND);
                    existEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
                    existEntity.setUpdatedTime(new Date());

                    procExecBindingTmpRepository.updateByPrimaryKeySelective(existEntity);
                    continue;
                }

            }
        }

        for (ProcExecBindingTmpEntity entity : bindingEntities) {
            if (bindingsSelected.contains(entity)) {
                continue;
            }

            entity.setIsBound(ProcExecBindingTmpEntity.UNBOUND);
            entity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            entity.setUpdatedTime(new Date());

            procExecBindingTmpRepository.updateByPrimaryKeySelective(entity);
        }
    }

    /**
     * 
     * @param processSessionId
     * @return
     */
    public List<TaskNodeDefObjectBindInfoDto> getProcessInstanceExecBindingsOfSession(String processSessionId) {
        List<ProcExecBindingTmpEntity> bindingEntities = procExecBindingTmpRepository
                .selectAllNodeBindingsBySession(processSessionId);

        List<TaskNodeDefObjectBindInfoDto> result = new ArrayList<>();
        if (bindingEntities == null) {
            return result;
        }

        bindingEntities.forEach(entity -> {
            TaskNodeDefObjectBindInfoDto dto = new TaskNodeDefObjectBindInfoDto();
            dto.setBound(entity.getIsBound());
            dto.setEntityDataId(entity.getEntityDataId());
            dto.setEntityTypeId(entity.getEntityTypeId());
            dto.setNodeDefId(entity.getNodeDefId());
            dto.setOrderedNo(entity.getOrderedNo());

            result.add(dto);
        });

        return result;
    }

    /**
     * 
     * @param nodeDefId
     * @param processSessionId
     * @return
     */
    public List<TaskNodeDefObjectBindInfoDto> getProcessInstanceExecBindingsOfSessionAndNode(String nodeDefId,
            String processSessionId) {
        List<ProcExecBindingTmpEntity> bindingEntities = procExecBindingTmpRepository
                .selectAllNodeBindingsByNodeAndSession(nodeDefId, processSessionId);

        List<TaskNodeDefObjectBindInfoDto> result = new ArrayList<>();
        if (bindingEntities == null) {
            return result;
        }

        bindingEntities.forEach(entity -> {
            TaskNodeDefObjectBindInfoDto dto = new TaskNodeDefObjectBindInfoDto();
            dto.setBound(entity.getIsBound());
            dto.setEntityDataId(entity.getEntityDataId());
            dto.setEntityTypeId(entity.getEntityTypeId());
            dto.setNodeDefId(entity.getNodeDefId());
            dto.setOrderedNo(entity.getOrderedNo());
            dto.setEntityDisplayName(entity.getEntityDataName());

            result.add(dto);
        });

        return result;
    }

    /**
     * 
     * @param procInstId
     * @param nodeInstId
     * @return
     */
    public TaskNodeExecContextDto getTaskNodeContextInfo(Integer procInstId, Integer nodeInstId) {
        TaskNodeInstInfoEntity nodeEntity = taskNodeInstInfoRepository.selectByPrimaryKey(nodeInstId);
        if (nodeEntity == null) {
            throw new WecubeCoreException("3188", String.format("Invalid node instance id: %s", nodeInstId),
                    nodeInstId);
        }
        
        TaskNodeDefInfoEntity nodeDefInfoEntity = taskNodeDefInfoRepository.selectByPrimaryKey(nodeEntity.getNodeDefId());

        TaskNodeExecContextDto result = new TaskNodeExecContextDto();
        result.setNodeDefId(nodeEntity.getNodeDefId());
        result.setNodeId(nodeEntity.getNodeId());
        result.setNodeInstId(nodeEntity.getId());
        result.setNodeName(nodeEntity.getNodeName());
        result.setNodeType(nodeEntity.getNodeType());
        result.setErrorMessage(nodeEntity.getErrMsg());
        
        if(nodeDefInfoEntity != null) {
            result.setNodeExpression(nodeDefInfoEntity.getRoutineExp());
            result.setPluginInfo(nodeDefInfoEntity.getServiceId());
        }

        List<TaskNodeExecRequestEntity> requestEntities = taskNodeExecRequestRepository
                .selectCurrentEntityByNodeInstId(nodeEntity.getId());

        if (requestEntities == null || requestEntities.isEmpty()) {
            return result;
        }

        TaskNodeExecRequestEntity requestEntity = requestEntities.get(0);

        result.setRequestId(requestEntity.getReqId());
        result.setErrorCode(requestEntity.getErrCode());
        result.setErrorMessage(requestEntity.getErrMsg());

        List<TaskNodeExecParamEntity> requestParamEntities = taskNodeExecParamRepository
                .selectAllByRequestIdAndParamType(requestEntity.getReqId(), TaskNodeExecParamEntity.PARAM_TYPE_REQUEST);

        List<TaskNodeExecParamEntity> responseParamEntities = taskNodeExecParamRepository
                .selectAllByRequestIdAndParamType(requestEntity.getReqId(),
                        TaskNodeExecParamEntity.PARAM_TYPE_RESPONSE);

        List<RequestObjectDto> requestObjects = calculateRequestObjectDtos(requestParamEntities, responseParamEntities);

        requestObjects.forEach(result::addRequestObjects);

        return result;
    }

    /**
     * 
     * @param procDefId
     * @param nodeDefId
     * @return
     */
    public List<InterfaceParameterDto> getTaskNodeParameters(String procDefId, String nodeDefId) {
        List<InterfaceParameterDto> result = new ArrayList<>();
        TaskNodeDefInfoEntity e = taskNodeDefInfoRepository.selectByPrimaryKey(nodeDefId);
        if (e == null) {
            return result;
        }

        // #1993
        String nodeType = e.getNodeType();

        if (TaskNodeDefInfoEntity.NODE_TYPE_START_EVENT.equalsIgnoreCase(nodeType)) {
            List<InterfaceParameterDto> startEventParams = prepareNodeParameters();
            result.addAll(startEventParams);
            return result;
        }

        String serviceId = e.getServiceId();

        if (StringUtils.isBlank(serviceId)) {
            log.debug("service id is present for {}", nodeDefId);
            return result;
        }

        PluginConfigInterfaces pci = pluginConfigMgmtService.getPluginConfigInterfaceByServiceName(serviceId);
        List<PluginConfigInterfaceParameters> inputParameters = pci.getInputParameters();
        List<PluginConfigInterfaceParameters> outputParameters = pci.getOutputParameters();

        inputParameters.forEach(p -> {
            result.add(buildInterfaceParameterDto(p));
        });

        outputParameters.forEach(p -> {
            result.add(buildInterfaceParameterDto(p));
        });

        return result;
    }

    /**
     * 
     * @param procDefId
     * @param dataId
     * @return
     */
    @Transactional
    public ProcessDataPreviewDto generateProcessDataPreview(String procDefId, String dataId) {
        if (StringUtils.isBlank(procDefId) || StringUtils.isBlank(dataId)) {
            throw new WecubeCoreException("3189", "Process definition ID or entity ID is not provided.");
        }

        ProcDefOutlineDto procDefOutline = workflowProcDefService.getProcessDefinitionOutline(procDefId);

        if (procDefOutline == null) {
            log.debug("process definition with id {} does not exist.", procDefId);
            throw new WecubeCoreException("3190",
                    String.format("Such process definition {%s} does not exist.", procDefId), procDefId);
        }

        ProcessDataPreviewDto previewDto = doFetchProcessPreviewData(procDefOutline, dataId, true);
        saveProcessDataPreview(previewDto);

        return previewDto;

    }

    private List<InterfaceParameterDto> prepareNodeParameters() {
        List<InterfaceParameterDto> predefinedParams = new ArrayList<>();

        // 1
        InterfaceParameterDto procDefName = new InterfaceParameterDto();
        procDefName.setDataType(LocalWorkflowConstants.PLUGIN_DATA_TYPE_STRING);
        procDefName.setName(LocalWorkflowConstants.CONTEXT_NAME_PROC_DEF_NAME);
        procDefName.setType(LocalWorkflowConstants.PLUGIN_PARAM_TYPE_INPUT);

        predefinedParams.add(procDefName);

        // 2
        InterfaceParameterDto procDefKey = new InterfaceParameterDto();
        procDefKey.setDataType(LocalWorkflowConstants.PLUGIN_DATA_TYPE_STRING);
        procDefKey.setName(LocalWorkflowConstants.CONTEXT_NAME_PROC_DEF_KEY);
        procDefKey.setType(LocalWorkflowConstants.PLUGIN_PARAM_TYPE_INPUT);

        predefinedParams.add(procDefKey);

        // 3
        InterfaceParameterDto procInstId = new InterfaceParameterDto();
        procInstId.setDataType(LocalWorkflowConstants.PLUGIN_DATA_TYPE_STRING);
        procInstId.setName(LocalWorkflowConstants.CONTEXT_NAME_PROC_INST_ID);
        procInstId.setType(LocalWorkflowConstants.PLUGIN_PARAM_TYPE_INPUT);

        predefinedParams.add(procInstId);

        // 4
        InterfaceParameterDto procInstKey = new InterfaceParameterDto();
        procInstKey.setDataType(LocalWorkflowConstants.PLUGIN_DATA_TYPE_STRING);
        procInstKey.setName(LocalWorkflowConstants.CONTEXT_NAME_PROC_INST_KEY);
        procInstKey.setType(LocalWorkflowConstants.PLUGIN_PARAM_TYPE_INPUT);

        predefinedParams.add(procInstKey);

        // 5
        InterfaceParameterDto procInstName = new InterfaceParameterDto();
        procInstName.setDataType(LocalWorkflowConstants.PLUGIN_DATA_TYPE_STRING);
        procInstName.setName(LocalWorkflowConstants.CONTEXT_NAME_PROC_INST_NAME);
        procInstName.setType(LocalWorkflowConstants.PLUGIN_PARAM_TYPE_INPUT);

        predefinedParams.add(procInstName);

        // 6
        InterfaceParameterDto rootEntityName = new InterfaceParameterDto();
        rootEntityName.setDataType(LocalWorkflowConstants.PLUGIN_DATA_TYPE_STRING);
        rootEntityName.setName(LocalWorkflowConstants.CONTEXT_NAME_ROOT_ENTITY_NAME);
        rootEntityName.setType(LocalWorkflowConstants.PLUGIN_PARAM_TYPE_INPUT);

        predefinedParams.add(rootEntityName);

        // 7
        InterfaceParameterDto rootEntityId = new InterfaceParameterDto();
        rootEntityId.setDataType(LocalWorkflowConstants.PLUGIN_DATA_TYPE_STRING);
        rootEntityId.setName(LocalWorkflowConstants.CONTEXT_NAME_ROOT_ENTITY_ID);
        rootEntityId.setType(LocalWorkflowConstants.PLUGIN_PARAM_TYPE_INPUT);

        predefinedParams.add(rootEntityId);

        return predefinedParams;
    }

    private void saveProcessDataPreview(ProcessDataPreviewDto previewDto) {
        for (GraphNodeDto gNode : previewDto.getEntityTreeNodes()) {
            GraphNodeEntity entity = new GraphNodeEntity();
            entity.setDataId(gNode.getDataId());
            entity.setDisplayName(gNode.getDisplayName());
            entity.setEntityName(gNode.getEntityName());
            entity.setGraphNodeId(gNode.getId());
            entity.setPkgName(gNode.getPackageName());
            entity.setPrevIds(GraphNodeEntity.convertIdsListToString(gNode.getPreviousIds()));
            entity.setSuccIds(GraphNodeEntity.convertIdsListToString(gNode.getSucceedingIds()));
            entity.setProcSessId(previewDto.getProcessSessionId());
            entity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
            entity.setCreatedTime(new Date());
            
            //#2169
            entity.setFullDataId(gNode.getFullDataId());

            graphNodeRepository.insert(entity);
        }
    }

    private void saveProcInstExecBindingTmpEntity(ProcDefOutlineDto outline, String dataId, String dataName,
            String processSessionId) {
        ProcExecBindingTmpEntity procInstBindingTmpEntity = new ProcExecBindingTmpEntity();
        procInstBindingTmpEntity.setBindType(ProcExecBindingTmpEntity.BIND_TYPE_PROC_INSTANCE);
        procInstBindingTmpEntity.setIsBound(ProcExecBindingTmpEntity.BOUND);
        procInstBindingTmpEntity.setProcSessionId(processSessionId);
        procInstBindingTmpEntity.setProcDefId(outline.getProcDefId());
        procInstBindingTmpEntity.setEntityDataId(dataId);
        procInstBindingTmpEntity.setEntityTypeId(outline.getRootEntity());
        procInstBindingTmpEntity.setEntityDataName(dataName);
        procInstBindingTmpEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
        procInstBindingTmpEntity.setCreatedTime(new Date());
        procInstBindingTmpEntity.setFullEntityDataId(dataId);

        procExecBindingTmpRepository.insert(procInstBindingTmpEntity);
    }

    protected ProcessDataPreviewDto doFetchProcessPreviewData(ProcDefOutlineDto outline, String dataId,
            boolean needSaveTmp) {
        ProcessDataPreviewDto result = new ProcessDataPreviewDto();

        List<GraphNodeDto> hierarchicalEntityNodes = new ArrayList<>();
        String processSessionId = UUID.randomUUID().toString();

        Map<Object, Object> externalCacheMap = new HashMap<>();

        for (FlowNodeDefDto f : outline.getFlowNodes()) {
            String nodeType = f.getNodeType();

            if (!NODE_SUB_PROCESS.equals(nodeType)) {
                continue;
            }

            if (TaskNodeDefInfoEntity.DYNAMIC_BIND_YES.equalsIgnoreCase(f.getDynamicBind())) {
                log.info("task node {}-{} is dynamic binding node and no need to pre-bind.", f.getNodeDefId(),
                        f.getNodeName());
                continue;
            }

            tryProcessSingleFlowNodeDef(f, hierarchicalEntityNodes, dataId, processSessionId, needSaveTmp,
                    externalCacheMap);
        }

        result.addAllEntityTreeNodes(hierarchicalEntityNodes);
        result.setProcessSessionId(processSessionId);

        if (needSaveTmp) {
            GraphNodeDto rootEntity = tryCalculateRootGraphNode(hierarchicalEntityNodes, dataId);
            String dataName = null;
            if (rootEntity != null) {
                dataName = rootEntity.getDisplayName();
            }
            saveProcInstExecBindingTmpEntity(outline, dataId, dataName, processSessionId);
        }

        return result;

    }

    private GraphNodeDto tryCalculateRootGraphNode(List<GraphNodeDto> hierarchicalEntityNodes, String dataId) {
        if (hierarchicalEntityNodes == null || hierarchicalEntityNodes.isEmpty()) {
            return null;
        }

        for (GraphNodeDto d : hierarchicalEntityNodes) {
            if (d.getPreviousIds().isEmpty() && d.getDataId().equals(dataId)) {
                return d;
            }
        }

        return null;
    }

    private void tryProcessSingleFlowNodeDef(FlowNodeDefDto flowNode, List<GraphNodeDto> hierarchicalEntityNodes,
            String dataId, String processSessionId, boolean needSaveTmp, Map<Object, Object> cacheMap) {
        String routineExpr = calculateDataModelExpression(flowNode);

        if (StringUtils.isBlank(routineExpr)) {
            log.info("the routine expression is blank for {} {}", flowNode.getNodeDefId(), flowNode.getNodeName());
            return;
        }

        log.info("About to fetch data for node {} {} with expression {} and data id {}", flowNode.getNodeDefId(),
                flowNode.getNodeName(), routineExpr, dataId);
        EntityOperationRootCondition condition = new EntityOperationRootCondition(routineExpr, dataId);
        List<StandardEntityDataNode> nodes = null;
        try {
            EntityTreeNodesOverview overview = standardEntityOperationService.generateEntityLinkOverview(condition,
                    this.userJwtSsoTokenRestTemplate, cacheMap);
            nodes = overview.getHierarchicalEntityNodes();

            if (needSaveTmp) {
                saveLeafNodeEntityNodesTemporary(flowNode, overview.getLeafNodeEntityNodes(), processSessionId);
            }
        } catch (Exception e) {
            String errMsg = String.format("Errors while fetching data for node %s %s with expr %s and data id %s",
                    flowNode.getNodeDefId(), flowNode.getNodeName(), routineExpr, dataId);
            log.error(errMsg, e);
            throw new WecubeCoreException("3191", errMsg, flowNode.getNodeDefId(), flowNode.getNodeName(), routineExpr, dataId);
        }

        if (nodes == null || nodes.isEmpty()) {
            log.warn("None data returned for {} and {}", routineExpr, dataId);
            return;
        }

        log.info("total {} records returned for {} and {}", nodes.size(), routineExpr, dataId);

        processTreeNodes(hierarchicalEntityNodes, nodes);
    }

    private void processTreeNodes(List<GraphNodeDto> hierarchicalEntityNodes, List<StandardEntityDataNode> nodes) {
        for (StandardEntityDataNode tn : nodes) {
            String treeNodeId = buildId(tn);
            GraphNodeDto currNode = findGraphNodeDtoById(hierarchicalEntityNodes, treeNodeId);
            if (currNode == null) {
                currNode = new GraphNodeDto();
                currNode.setDataId(tn.getId());
                currNode.setPackageName(tn.getPackageName());
                currNode.setEntityName(tn.getEntityName());
                currNode.setDisplayName(tn.getDisplayName());
                //#2169
                currNode.setFullDataId(tn.getFullId());

                addToResult(hierarchicalEntityNodes, currNode);
            }

            StandardEntityDataNode parentTreeNode = tn.getParent();
            if (parentTreeNode != null) {
                String parentTreeNodeId = buildId(parentTreeNode);
                currNode.addPreviousIds(parentTreeNodeId);
            }

            List<StandardEntityDataNode> childrenTreeNodes = tn.getChildren();
            if (childrenTreeNodes != null) {
                for (StandardEntityDataNode ctn : childrenTreeNodes) {
                    String ctnId = buildId(ctn);
                    currNode.addSucceedingIds(ctnId);
                }
            }
        }
    }

    private void saveLeafNodeEntityNodesTemporary(FlowNodeDefDto f, List<StandardEntityDataNode> leafNodeEntityNodes,
            String processSessionId) {
        if (leafNodeEntityNodes == null) {
            return;
        }

        if (log.isInfoEnabled()) {
            log.info("total {} nodes returned as default bindings for {} {} {}", leafNodeEntityNodes.size(),
                    f.getNodeDefId(), f.getNodeId(), f.getNodeName());
        }

        List<StandardEntityDataNode> savedTreeNodes = new ArrayList<>();

        for (StandardEntityDataNode tn : leafNodeEntityNodes) {
            if (containsTreeNode(savedTreeNodes, tn)) {
                continue;
            }

            ProcExecBindingTmpEntity taskNodeBinding = new ProcExecBindingTmpEntity();
            taskNodeBinding.setBindType(ProcExecBindingTmpEntity.BIND_TYPE_TASK_NODE_INSTANCE);
            taskNodeBinding.setIsBound(ProcExecBindingTmpEntity.BOUND);
            taskNodeBinding.setProcSessionId(processSessionId);
            taskNodeBinding.setProcDefId(f.getProcDefId());
            taskNodeBinding.setEntityDataId(String.valueOf(tn.getId()));
            taskNodeBinding.setEntityTypeId(String.format("%s:%s", tn.getPackageName(), tn.getEntityName()));
            taskNodeBinding.setEntityDataName(tn.getDisplayName());
            taskNodeBinding.setNodeDefId(f.getNodeDefId());
            taskNodeBinding.setOrderedNo(f.getOrderedNo());
            taskNodeBinding.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
            taskNodeBinding.setCreatedTime(new Date());
            
            //#2169 full data id
            taskNodeBinding.setFullEntityDataId(tn.getFullId());

            procExecBindingTmpRepository.insert(taskNodeBinding);
            savedTreeNodes.add(tn);
        }

        return;

    }

    private boolean containsTreeNode(List<StandardEntityDataNode> treeNodes, StandardEntityDataNode treeNode) {
        for (StandardEntityDataNode tn : treeNodes) {
            if (tn.equals(treeNode)) {
                return true;
            }
        }

        return false;
    }

    private String calculateDataModelExpression(FlowNodeDefDto flowNode) {
        if (StringUtils.isBlank(flowNode.getRoutineExpression())) {
            return null;
        }

        String expr = flowNode.getRoutineExpression();

        if (StringUtils.isBlank(flowNode.getServiceId())) {
            return expr;
        }

        PluginConfigInterfaces pluginConfigIntf = pluginConfigMgmtService.getPluginConfigInterfaceByServiceName(flowNode.getServiceId());
        if (pluginConfigIntf == null) {
            return expr;
        }

        if (StringUtils.isBlank(pluginConfigIntf.getFilterRule())) {
            return expr;
        }

        return expr + pluginConfigIntf.getFilterRule();
    }

    private void addToResult(List<GraphNodeDto> result, GraphNodeDto... nodes) {
        for (GraphNodeDto n : nodes) {
            if (result.contains(n)) {
                continue;
            }

            GraphNodeDto exist = findGraphNodeDtoById(result, n.getId());
            if (exist == null) {
                result.add(n);
            }
        }
    }

    private GraphNodeDto findGraphNodeDtoById(List<GraphNodeDto> result, String id) {
        for (GraphNodeDto n : result) {
            if (n.getId().equals(id)) {
                return n;
            }
        }

        return null;
    }

    private String buildId(StandardEntityDataNode n) {
        return String.format("%s:%s:%s", n.getPackageName(), n.getEntityName(), n.getId());
    }

    private boolean isSensitiveData(TaskNodeExecParamEntity respParamEntity) {
        if (respParamEntity == null) {
            return false;
        }

        if (respParamEntity.getIsSensitive() == null) {
            return false;
        }

        return respParamEntity.getIsSensitive();

    }

    //#2169
    private List<RequestObjectDto> calculateRequestObjectDtos(List<TaskNodeExecParamEntity> requestParamEntities,
            List<TaskNodeExecParamEntity> responseParamEntities) {
        List<RequestObjectDto> requestObjects = new ArrayList<>();

        Map<String, RequestParamObjectDto> reqParamObjectsByObjectId = new HashMap<>();

        Map<String, RequestParamObjectDto> respParamObjectsByObjectId = new HashMap<>();

        if (requestParamEntities != null) {
            for (TaskNodeExecParamEntity reqParam : requestParamEntities) {
                RequestParamObjectDto paramObjectDto = reqParamObjectsByObjectId.get(reqParam.getObjId());
                if (paramObjectDto == null) {
                    paramObjectDto = new RequestParamObjectDto();
                    paramObjectDto.setObjectId(reqParam.getObjId());
                    reqParamObjectsByObjectId.put(reqParam.getObjId(), paramObjectDto);
                }

                String attrValue = null;
                if (isSensitiveData(reqParam)) {
                    attrValue = MASKED_VALUE;
                } else {
                    attrValue = reqParam.getParamDataValue();
                }

                if (CALLBACK_PARAMETER_KEY.equalsIgnoreCase(reqParam.getParamName())) {
                    paramObjectDto.setCallbackParameter(reqParam.getParamDataValue());
                } else {
//                    RequestParamAttrDto attrDto = new RequestParamAttrDto(reqParam.getParamName(), attrValue);
                    paramObjectDto.addParamAttr(reqParam.getParamName(), attrValue);
                }
            }
        }
        
        if(responseParamEntities != null){
            for (TaskNodeExecParamEntity param : responseParamEntities) {
                RequestParamObjectDto paramObjectDto = respParamObjectsByObjectId.get(param.getObjId());
                if (paramObjectDto == null) {
                    paramObjectDto = new RequestParamObjectDto();
                    paramObjectDto.setObjectId(param.getObjId());
                    respParamObjectsByObjectId.put(param.getObjId(), paramObjectDto);
                }

                String attrValue = null;
                if (isSensitiveData(param)) {
                    attrValue = MASKED_VALUE;
                } else {
                    attrValue = param.getParamDataValue();
                }

                if (CALLBACK_PARAMETER_KEY.equalsIgnoreCase(param.getParamName())) {
                    paramObjectDto.setCallbackParameter(param.getParamDataValue());
                } else {
//                    RequestParamAttrDto attrDto = new RequestParamAttrDto(param.getParamName(), attrValue);
                    paramObjectDto.addParamAttr(param.getParamName(), attrValue);
                }
            }
        }
        
        Map<String, RequestObjectDto> requestObjectsByCallbackParameter = new HashMap<String,RequestObjectDto>();
        
        for(RequestParamObjectDto reqParamObjectDto : reqParamObjectsByObjectId.values()){
            String callbackParameter = reqParamObjectDto.getCallbackParameter();
            if(StringUtils.isBlank(callbackParameter)){
                continue;
            }
            RequestObjectDto objectDto = requestObjectsByCallbackParameter.get(callbackParameter);
            if(objectDto == null){
                objectDto = new RequestObjectDto();
                objectDto.setCallbackParameter(callbackParameter);
                
                requestObjectsByCallbackParameter.put(callbackParameter, objectDto);
            }
            
            objectDto.addInput(reqParamObjectDto.getParamAttrs());
            
        }
        
        for(RequestParamObjectDto respParamObjectDto : respParamObjectsByObjectId.values()){
            String callbackParameter = respParamObjectDto.getCallbackParameter();
            if(StringUtils.isBlank(callbackParameter)){
                continue;
            }
            RequestObjectDto objectDto = requestObjectsByCallbackParameter.get(callbackParameter);
            if(objectDto == null){
                objectDto = new RequestObjectDto();
                objectDto.setCallbackParameter(callbackParameter);
                
                requestObjectsByCallbackParameter.put(callbackParameter, objectDto);
            }
            
            objectDto.addOutput(respParamObjectDto.getParamAttrs());
        }
        
        requestObjects.addAll(requestObjectsByCallbackParameter.values());

        return requestObjects;
    }

    private InterfaceParameterDto buildInterfaceParameterDto(PluginConfigInterfaceParameters p) {
        InterfaceParameterDto d = new InterfaceParameterDto();
        d.setType(p.getType());
        d.setName(p.getName());
        d.setDataType(p.getDataType());

        return d;
    }

    private ProcExecBindingTmpEntity findProcExecBindingTmpEntityWithNodeAndEntity(String nodeDefId,
            String entityTypeId, String entityDataId, List<ProcExecBindingTmpEntity> bindingEntities) {
        for (ProcExecBindingTmpEntity entity : bindingEntities) {
            if (nodeDefId.equals(entity.getNodeDefId()) && entityTypeId.equals(entity.getEntityTypeId())
                    && entityDataId.equals(entity.getEntityDataId())) {
                return entity;
            }
        }

        return null;
    }

}
