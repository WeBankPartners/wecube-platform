package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter;
import com.webank.wecube.platform.core.dto.workflow.FlowNodeDefDto;
import com.webank.wecube.platform.core.dto.workflow.GraphNodeDto;
import com.webank.wecube.platform.core.dto.workflow.InterfaceParameterDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefOutlineDto;
import com.webank.wecube.platform.core.dto.workflow.ProcessDataPreviewDto;
import com.webank.wecube.platform.core.dto.workflow.RequestObjectDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefObjectBindInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeExecContextDto;
import com.webank.wecube.platform.core.entity.workflow.GraphNodeEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingTmpEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecParamEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecRequestEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.repository.workflow.GraphNodeRepository;
import com.webank.wecube.platform.core.repository.workflow.ProcDefInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcExecBindingTmpRepository;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeDefInfoRepository;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeExecParamRepository;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeExecRequestRepository;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeInstInfoRepository;
import com.webank.wecube.platform.core.service.dme.EntityOperationRootCondition;
import com.webank.wecube.platform.core.service.dme.EntityTreeNodesOverview;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationService;
import com.webank.wecube.platform.core.service.dme.TreeNode;
import com.webank.wecube.platform.core.service.plugin.PluginConfigService;

@Service
public class WorkflowDataService{
    private static final Logger log = LoggerFactory.getLogger(WorkflowDataService.class);

    @Autowired
    private WorkflowProcDefService workflowProcDefService;

    @Autowired
    private TaskNodeDefInfoRepository taskNodeDefInfoRepository;

    @Autowired
    private TaskNodeInstInfoRepository taskNodeInstInfoRepository;

    @Autowired
    private StandardEntityOperationService standardEntityOperationService;

    @Autowired
    private PluginConfigService pluginConfigService;

    @Autowired
    protected TaskNodeExecParamRepository taskNodeExecParamRepository;

    @Autowired
    protected TaskNodeExecRequestRepository taskNodeExecRequestRepository;

    @Autowired
    protected ProcExecBindingTmpRepository procExecBindingTmpRepository;

    @Autowired
    protected ProcDefInfoMapper procDefInfoRepository;

    @Autowired
    protected GraphNodeRepository graphNodeRepository;

    @Autowired
    @Qualifier("userJwtSsoTokenRestTemplate")
    protected RestTemplate userJwtSsoTokenRestTemplate;

    public ProcessDataPreviewDto generateProcessDataPreviewForProcInstance(Integer procInstId) {
        List<GraphNodeEntity> gNodeEntities = graphNodeRepository.findAllByProcInstId(procInstId);
        ProcessDataPreviewDto result = new ProcessDataPreviewDto();
        if (gNodeEntities == null || gNodeEntities.isEmpty()) {
            return result;
        }

        result.setProcessSessionId(gNodeEntities.get(0).getProcessSessionId());

        List<GraphNodeDto> gNodes = new ArrayList<>();
        for (GraphNodeEntity entity : gNodeEntities) {
            GraphNodeDto gNode = new GraphNodeDto();
            gNode.setDataId(entity.getDataId());
            gNode.setDisplayName(entity.getDisplayName());
            gNode.setEntityName(entity.getEntityName());
            gNode.setId(entity.getGraphNodeId());
            gNode.setPackageName(entity.getPackageName());
            gNode.setPreviousIds(GraphNodeEntity.convertIdsStringToList(entity.getPreviousIds()));
            gNode.setSucceedingIds(GraphNodeEntity.convertIdsStringToList(entity.getSucceedingIds()));

            gNodes.add(gNode);
        }

        result.addAllEntityTreeNodes(gNodes);

        return result;
    }
    
    public List<Map<String, Object>> getProcessDefinitionRootEntitiesByProcDefKey(String procDefKey) {
        if (StringUtils.isBlank(procDefKey)) {
            throw new WecubeCoreException("3186","Process definition ID cannot be blank.");
        }
        
        List<ProcDefInfoEntity> procDefEntities = procDefInfoRepository
                .findAllDeployedProcDefsByProcDefKey(procDefKey, ProcDefInfoEntity.DEPLOYED_STATUS);

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


        List<Map<String, Object>> retRecords = standardEntityOperationService.queryAttributeValuesOfLeafNode(
                new EntityOperationRootCondition(rootEntityExpr, null), userJwtSsoTokenRestTemplate);

        if (retRecords == null) {
            return result;
        }

        result.addAll(retRecords);

        return result;
    }

    public List<Map<String, Object>> getProcessDefinitionRootEntities(String procDefId) {
        if (StringUtils.isBlank(procDefId)) {
            throw new WecubeCoreException("3186","Process definition ID cannot be blank.");
        }
        ProcDefInfoEntity procDef = procDefInfoRepository.selectByPrimaryKey(procDefId);
        if (procDef == null) {
            throw new WecubeCoreException("3187",String.format("Cannot find such process definition with ID [%s]" , procDefId), procDefId);
        }

        List<Map<String, Object>> result = new ArrayList<>();

        String rootEntityExpr = procDef.getRootEntity();
        if (StringUtils.isBlank(rootEntityExpr)) {
            return result;
        }


        List<Map<String, Object>> retRecords = standardEntityOperationService.queryAttributeValuesOfLeafNode(
                new EntityOperationRootCondition(rootEntityExpr, null), userJwtSsoTokenRestTemplate);

        if (retRecords == null) {
            return result;
        }

        result.addAll(retRecords);

        return result;
    }

    public void updateProcessInstanceExecBindingsOfSession(String nodeDefId, String processSessionId,
            List<TaskNodeDefObjectBindInfoDto> bindings) {

        List<ProcExecBindingTmpEntity> bindingEntities = procExecBindingTmpRepository
                .findAllNodeBindingsByNodeAndSession(nodeDefId, processSessionId);

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

                    existEntity.setBound(ProcExecBindingTmpEntity.BOUND);
                    existEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
                    existEntity.setUpdatedTime(new Date());

                    procExecBindingTmpRepository.saveAndFlush(existEntity);
                    continue;
                }

            }
        }

        for (ProcExecBindingTmpEntity entity : bindingEntities) {
            if (bindingsSelected.contains(entity)) {
                continue;
            }

            entity.setBound(ProcExecBindingTmpEntity.UNBOUND);
            entity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            entity.setUpdatedTime(new Date());

            procExecBindingTmpRepository.saveAndFlush(entity);
        }
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

    public List<TaskNodeDefObjectBindInfoDto> getProcessInstanceExecBindingsOfSession(String processSessionId) {
        List<ProcExecBindingTmpEntity> bindingEntities = procExecBindingTmpRepository
                .findAllNodeBindingsBySession(processSessionId);

        List<TaskNodeDefObjectBindInfoDto> result = new ArrayList<>();
        if (bindingEntities == null) {
            return result;
        }

        bindingEntities.forEach(entity -> {
            TaskNodeDefObjectBindInfoDto dto = new TaskNodeDefObjectBindInfoDto();
            dto.setBound(entity.getBound());
            dto.setEntityDataId(entity.getEntityDataId());
            dto.setEntityTypeId(entity.getEntityTypeId());
            dto.setNodeDefId(entity.getNodeDefId());
            dto.setOrderedNo(entity.getOrderedNo());

            result.add(dto);
        });

        return result;
    }

    public List<TaskNodeDefObjectBindInfoDto> getProcessInstanceExecBindingsOfSessionAndNode(String nodeDefId,
            String processSessionId) {
        List<ProcExecBindingTmpEntity> bindingEntities = procExecBindingTmpRepository
                .findAllNodeBindingsByNodeAndSession(nodeDefId, processSessionId);

        List<TaskNodeDefObjectBindInfoDto> result = new ArrayList<>();
        if (bindingEntities == null) {
            return result;
        }

        bindingEntities.forEach(entity -> {
            TaskNodeDefObjectBindInfoDto dto = new TaskNodeDefObjectBindInfoDto();
            dto.setBound(entity.getBound());
            dto.setEntityDataId(entity.getEntityDataId());
            dto.setEntityTypeId(entity.getEntityTypeId());
            dto.setNodeDefId(entity.getNodeDefId());
            dto.setOrderedNo(entity.getOrderedNo());
            dto.setEntityDisplayName(entity.getEntityDataName());

            result.add(dto);
        });

        return result;
    }

    public TaskNodeExecContextDto getTaskNodeContextInfo(Integer procInstId, Integer nodeInstId) {
        Optional<TaskNodeInstInfoEntity> nodeEntityOpt = taskNodeInstInfoRepository.findById(nodeInstId);
        if (!nodeEntityOpt.isPresent()) {
            throw new WecubeCoreException("3188",String.format("Invalid node instance id: %s" , nodeInstId), nodeInstId);
        }

        TaskNodeInstInfoEntity nodeEntity = nodeEntityOpt.get();

        TaskNodeExecContextDto result = new TaskNodeExecContextDto();
        result.setNodeDefId(nodeEntity.getNodeDefId());
        result.setNodeId(nodeEntity.getNodeId());
        result.setNodeInstId(nodeEntity.getId());
        result.setNodeName(nodeEntity.getNodeName());
        result.setNodeType(nodeEntity.getNodeType());
        result.setErrorMessage(nodeEntity.getErrorMessage());

        List<TaskNodeExecRequestEntity> requestEntities = taskNodeExecRequestRepository
                .findCurrentEntityByNodeInstId(nodeEntity.getId());

        if (requestEntities == null || requestEntities.isEmpty()) {
            return result;
        }

        TaskNodeExecRequestEntity requestEntity = requestEntities.get(0);

        result.setRequestId(requestEntity.getRequestId());
        result.setErrorCode(requestEntity.getErrorCode());

        List<TaskNodeExecParamEntity> requestParamEntities = taskNodeExecParamRepository.findAllByRequestIdAndParamType(
                requestEntity.getRequestId(), TaskNodeExecParamEntity.PARAM_TYPE_REQUEST);

        List<TaskNodeExecParamEntity> responseParamEntities = taskNodeExecParamRepository
                .findAllByRequestIdAndParamType(requestEntity.getRequestId(),
                        TaskNodeExecParamEntity.PARAM_TYPE_RESPONSE);

        List<RequestObjectDto> requestObjects = calculateRequestObjectDtos(requestParamEntities, responseParamEntities);

        requestObjects.forEach(result::addRequestObjects);

        return result;
    }

    public List<InterfaceParameterDto> getTaskNodeParameters(String procDefId, String nodeDefId) {
        List<InterfaceParameterDto> result = new ArrayList<>();
        Optional<TaskNodeDefInfoEntity> entityOptional = taskNodeDefInfoRepository.findById(nodeDefId);
        if (!entityOptional.isPresent()) {
            return result;
        }

        //#1993
        TaskNodeDefInfoEntity e = entityOptional.get();
        String nodeType = e.getNodeType();
        
        if(TaskNodeDefInfoEntity.NODE_TYPE_START_EVENT.equalsIgnoreCase(nodeType)){
            List<InterfaceParameterDto> startEventParams = prepareNodeParameters();
            result.addAll(startEventParams);
            return result;
        }
        
        String serviceId = e.getServiceId();

        if (StringUtils.isBlank(serviceId)) {
            log.debug("service id is present for {}", nodeDefId);
            return result;
        }

        PluginConfigInterface pci = pluginConfigService.getPluginConfigInterfaceByServiceName(serviceId);
        Set<PluginConfigInterfaceParameter> inputParameters = pci.getInputParameters();
        Set<PluginConfigInterfaceParameter> outputParameters = pci.getOutputParameters();

        inputParameters.forEach(p -> {
            result.add(buildInterfaceParameterDto(p));
        });

        outputParameters.forEach(p -> {
            result.add(buildInterfaceParameterDto(p));
        });

        return result;
    }

    @Transactional
    public ProcessDataPreviewDto generateProcessDataPreview(String procDefId, String dataId) {
        if (StringUtils.isBlank(procDefId) || StringUtils.isBlank(dataId)) {
            throw new WecubeCoreException("3189","Process definition ID or entity ID is not provided.");
        }

        ProcDefOutlineDto procDefOutline = workflowProcDefService.getProcessDefinitionOutline(procDefId);

        if (procDefOutline == null) {
            log.debug("process definition with id {} does not exist.", procDefId);
            throw new WecubeCoreException("3190",String.format("Such process definition {%s} does not exist.", procDefId), procDefId);
        }

        ProcessDataPreviewDto previewDto = doFetchProcessPreviewData(procDefOutline, dataId, true);
        saveProcessDataPreview(previewDto);

        return previewDto;

    }
    
    private List<InterfaceParameterDto> prepareNodeParameters(){
        List<InterfaceParameterDto> predefinedParams = new ArrayList<>();
        
        //1
        InterfaceParameterDto procDefName = new InterfaceParameterDto();
        procDefName.setDataType(LocalWorkflowConstants.PLUGIN_DATA_TYPE_STRING);
        procDefName.setName(LocalWorkflowConstants.CONTEXT_NAME_PROC_DEF_NAME);
        procDefName.setType(LocalWorkflowConstants.PLUGIN_PARAM_TYPE_INPUT);
        
        predefinedParams.add(procDefName);
        
        //2
        InterfaceParameterDto procDefKey = new InterfaceParameterDto();
        procDefKey.setDataType(LocalWorkflowConstants.PLUGIN_DATA_TYPE_STRING);
        procDefKey.setName(LocalWorkflowConstants.CONTEXT_NAME_PROC_DEF_KEY);
        procDefKey.setType(LocalWorkflowConstants.PLUGIN_PARAM_TYPE_INPUT);
        
        predefinedParams.add(procDefKey);
        
        //3
        InterfaceParameterDto procInstId = new InterfaceParameterDto();
        procInstId.setDataType(LocalWorkflowConstants.PLUGIN_DATA_TYPE_STRING);
        procInstId.setName(LocalWorkflowConstants.CONTEXT_NAME_PROC_INST_ID);
        procInstId.setType(LocalWorkflowConstants.PLUGIN_PARAM_TYPE_INPUT);
        
        predefinedParams.add(procInstId);
        
        //4
        InterfaceParameterDto procInstKey = new InterfaceParameterDto();
        procInstKey.setDataType(LocalWorkflowConstants.PLUGIN_DATA_TYPE_STRING);
        procInstKey.setName(LocalWorkflowConstants.CONTEXT_NAME_PROC_INST_KEY);
        procInstKey.setType(LocalWorkflowConstants.PLUGIN_PARAM_TYPE_INPUT);
        
        predefinedParams.add(procInstKey);
        
        //5
        InterfaceParameterDto procInstName = new InterfaceParameterDto();
        procInstName.setDataType(LocalWorkflowConstants.PLUGIN_DATA_TYPE_STRING);
        procInstName.setName(LocalWorkflowConstants.CONTEXT_NAME_PROC_INST_NAME);
        procInstName.setType(LocalWorkflowConstants.PLUGIN_PARAM_TYPE_INPUT);
        
        predefinedParams.add(procInstName);
        
        //6
        InterfaceParameterDto rootEntityName = new InterfaceParameterDto();
        rootEntityName.setDataType(LocalWorkflowConstants.PLUGIN_DATA_TYPE_STRING);
        rootEntityName.setName(LocalWorkflowConstants.CONTEXT_NAME_ROOT_ENTITY_NAME);
        rootEntityName.setType(LocalWorkflowConstants.PLUGIN_PARAM_TYPE_INPUT);
        
        predefinedParams.add(rootEntityName);
       
        return predefinedParams;
    }

    private void saveProcessDataPreview(ProcessDataPreviewDto previewDto) {
        for (GraphNodeDto gNode : previewDto.getEntityTreeNodes()) {
            GraphNodeEntity entity = new GraphNodeEntity();
            entity.setDataId(gNode.getDataId());
            entity.setDisplayName(gNode.getDisplayName());
            entity.setEntityName(gNode.getEntityName());
            entity.setGraphNodeId(gNode.getId());
            entity.setPackageName(gNode.getPackageName());
            entity.setPreviousIds(GraphNodeEntity.convertIdsListToString(gNode.getPreviousIds()));
            entity.setSucceedingIds(GraphNodeEntity.convertIdsListToString(gNode.getSucceedingIds()));
            entity.setProcessSessionId(previewDto.getProcessSessionId());

            graphNodeRepository.saveAndFlush(entity);
        }
    }

    private void saveProcExecBindingTmpEntity(ProcDefOutlineDto outline, String dataId, String dataName,
            String processSessionId) {
        ProcExecBindingTmpEntity procInstBindingTmpEntity = new ProcExecBindingTmpEntity();
        procInstBindingTmpEntity.setBindType(ProcExecBindingTmpEntity.BIND_TYPE_PROC_INSTANCE);
        procInstBindingTmpEntity.setBound(ProcExecBindingTmpEntity.BOUND);
        procInstBindingTmpEntity.setProcSessionId(processSessionId);
        procInstBindingTmpEntity.setProcDefId(outline.getProcDefId());
        procInstBindingTmpEntity.setEntityDataId(dataId);
        procInstBindingTmpEntity.setEntityTypeId(outline.getRootEntity());
        procInstBindingTmpEntity.setEntityDataName(dataName);
        procInstBindingTmpEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());

        procExecBindingTmpRepository.saveAndFlush(procInstBindingTmpEntity);
    }

    protected ProcessDataPreviewDto doFetchProcessPreviewData(ProcDefOutlineDto outline, String dataId,
            boolean needSaveTmp) {
        ProcessDataPreviewDto result = new ProcessDataPreviewDto();

        List<GraphNodeDto> hierarchicalEntityNodes = new ArrayList<>();
        String processSessionId = UUID.randomUUID().toString();

        for (FlowNodeDefDto f : outline.getFlowNodes()) {
            String nodeType = f.getNodeType();

            if (!"subProcess".equals(nodeType)) {
                continue;
            }

            processSingleFlowNodeDefDto(f, hierarchicalEntityNodes, dataId, processSessionId, needSaveTmp);
        }

        result.addAllEntityTreeNodes(hierarchicalEntityNodes);
        result.setProcessSessionId(processSessionId);

        if (needSaveTmp) {
            GraphNodeDto rootEntity = tryCalRootGraphNode(hierarchicalEntityNodes, dataId);
            String dataName = null;
            if (rootEntity != null) {
                dataName = rootEntity.getDisplayName();
            }
            saveProcExecBindingTmpEntity(outline, dataId, dataName, processSessionId);
        }

        return result;

    }

    private GraphNodeDto tryCalRootGraphNode(List<GraphNodeDto> hierarchicalEntityNodes, String dataId) {
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

    private void processSingleFlowNodeDefDto(FlowNodeDefDto f, List<GraphNodeDto> hierarchicalEntityNodes,
            String dataId, String processSessionId, boolean needSaveTmp) {
        String routineExpr = calculateDataModelExpression(f);

        if (StringUtils.isBlank(routineExpr)) {
            log.info("the routine expression is blank for {} {}", f.getNodeDefId(), f.getNodeName());
            return;
        }

        log.info("About to fetch data for node {} {} with expression {} and data id {}", f.getNodeDefId(),
                f.getNodeName(), routineExpr, dataId);
        EntityOperationRootCondition condition = new EntityOperationRootCondition(routineExpr, dataId);
        List<TreeNode> nodes = null;
        try {
            EntityTreeNodesOverview overview = standardEntityOperationService.generateEntityLinkOverview(condition, this.userJwtSsoTokenRestTemplate);
            nodes = overview.getHierarchicalEntityNodes();

            if (needSaveTmp) {
                saveLeafNodeEntityNodesTemporary(f, overview.getLeafNodeEntityNodes(), processSessionId);
            }
        } catch (Exception e) {
            String errMsg = String.format("Errors while fetching data for node %s %s with expr %s and data id %s",
                    f.getNodeDefId(), f.getNodeName(), routineExpr, dataId);
            log.error(errMsg, e);
            throw new WecubeCoreException("3191",errMsg, f.getNodeDefId(), f.getNodeName(), routineExpr, dataId);
        }

        if (nodes == null || nodes.isEmpty()) {
            log.warn("None data returned for {} and {}", routineExpr, dataId);
            return;
        }

        log.info("total {} records returned for {} and {}", nodes.size(), routineExpr, dataId);

        processTreeNodes(hierarchicalEntityNodes, nodes);
    }

    private void processTreeNodes(List<GraphNodeDto> hierarchicalEntityNodes, List<TreeNode> nodes) {
        for (TreeNode tn : nodes) {
            String treeNodeId = buildId(tn);
            GraphNodeDto currNode = findGraphNodeDtoById(hierarchicalEntityNodes, treeNodeId);
            if (currNode == null) {
                currNode = new GraphNodeDto();
                currNode.setDataId(tn.getRootId().toString());
                currNode.setPackageName(tn.getPackageName());
                currNode.setEntityName(tn.getEntityName());
                currNode.setDisplayName(tn.getDisplayName() == null ? null : tn.getDisplayName().toString());

                addToResult(hierarchicalEntityNodes, currNode);
            }

            TreeNode parentTreeNode = tn.getParent();
            if (parentTreeNode != null) {
                String parentTreeNodeId = buildId(parentTreeNode);
                currNode.addPreviousIds(parentTreeNodeId);
            }

            List<TreeNode> childrenTreeNodes = tn.getChildren();
            if (childrenTreeNodes != null) {
                for (TreeNode ctn : childrenTreeNodes) {
                    String ctnId = buildId(ctn);
                    currNode.addSucceedingIds(ctnId);
                }
            }
        }
    }

    private void saveLeafNodeEntityNodesTemporary(FlowNodeDefDto f, List<TreeNode> leafNodeEntityNodes,
            String processSessionId) {
        if (leafNodeEntityNodes == null) {
            return;
        }

        if (log.isInfoEnabled()) {
            log.info("total {} nodes returned as default bindings for {} {} {}", leafNodeEntityNodes.size(),
                    f.getNodeDefId(), f.getNodeId(), f.getNodeName());
        }

        List<TreeNode> savedTreeNodes = new ArrayList<>();

        for (TreeNode tn : leafNodeEntityNodes) {
            if (containsTreeNode(savedTreeNodes, tn)) {
                continue;
            }

            ProcExecBindingTmpEntity taskNodeBinding = new ProcExecBindingTmpEntity();
            taskNodeBinding.setBindType(ProcExecBindingTmpEntity.BIND_TYPE_TASK_NODE_INSTANCE);
            taskNodeBinding.setBound(ProcExecBindingTmpEntity.BOUND);
            taskNodeBinding.setProcSessionId(processSessionId);
            taskNodeBinding.setProcDefId(f.getProcDefId());
            taskNodeBinding.setEntityDataId(String.valueOf(tn.getRootId()));
            taskNodeBinding.setEntityTypeId(String.format("%s:%s", tn.getPackageName(), tn.getEntityName()));
            taskNodeBinding.setNodeDefId(f.getNodeDefId());
            taskNodeBinding.setOrderedNo(f.getOrderedNo());
            taskNodeBinding.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());

            procExecBindingTmpRepository.saveAndFlush(taskNodeBinding);
            savedTreeNodes.add(tn);
        }

        return;

    }

    private boolean containsTreeNode(List<TreeNode> treeNodes, TreeNode treeNode) {
        for (TreeNode tn : treeNodes) {
            if (tn.equals(treeNode)) {
                return true;
            }
        }

        return false;
    }

    private String calculateDataModelExpression(FlowNodeDefDto f) {
        if (StringUtils.isBlank(f.getRoutineExpression())) {
            return null;
        }

        String expr = f.getRoutineExpression();

        if (StringUtils.isBlank(f.getServiceId())) {
            return expr;
        }

        PluginConfigInterface inter = pluginConfigService.getPluginConfigInterfaceByServiceName(f.getServiceId());
        if (inter == null) {
            return expr;
        }

        if (StringUtils.isBlank(inter.getFilterRule())) {
            return expr;
        }

        return expr + inter.getFilterRule();
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

    private String buildId(TreeNode n) {
        return String.format("%s:%s:%s", n.getPackageName(), n.getEntityName(), n.getRootId());
    }

    private boolean isSensitiveData(TaskNodeExecParamEntity respParamEntity) {
        if (respParamEntity == null) {
            return false;
        }

        if (respParamEntity.getSensitive() == null) {
            return false;
        }

        if (Boolean.TRUE.equals(respParamEntity.getSensitive())) {
            return true;

        }

        return false;

    }

    private Map<String, Map<String, String>> calculateRespParamsByObjectId(
            List<TaskNodeExecParamEntity> requestParamEntities, List<TaskNodeExecParamEntity> responseParamEntities) {
        Map<String, Map<String, String>> respParamsByObjectId = new HashMap<String, Map<String, String>>();
        if (responseParamEntities != null) {
            for (TaskNodeExecParamEntity respParamEntity : responseParamEntities) {
                Map<String, String> respParamsMap = respParamsByObjectId.get(respParamEntity.getObjectId());
                if (respParamsMap == null) {
                    respParamsMap = new HashMap<String, String>();
                    respParamsByObjectId.put(respParamEntity.getObjectId(), respParamsMap);
                }
                if (isSensitiveData(respParamEntity)) {
                    respParamsMap.put(respParamEntity.getParamName(), "***MASK***");
                } else {
                    respParamsMap.put(respParamEntity.getParamName(), respParamEntity.getParamDataValue());
                }

            }
        }

        return respParamsByObjectId;
    }

    private Map<String, RequestObjectDto> calculateRequestObjects(List<TaskNodeExecParamEntity> requestParamEntities,
            List<TaskNodeExecParamEntity> responseParamEntities) {
        Map<String, RequestObjectDto> objs = new HashMap<>();
        for (TaskNodeExecParamEntity rp : requestParamEntities) {
            RequestObjectDto ro = objs.get(rp.getObjectId());
            if (ro == null) {
                ro = new RequestObjectDto();
                objs.put(rp.getObjectId(), ro);
            }

            if (isSensitiveData(rp)) {
                ro.addInput(rp.getParamName(), "***MASK***");
            } else {
                ro.addInput(rp.getParamName(), rp.getParamDataValue());
            }
        }

        return objs;
    }

    private List<RequestObjectDto> calculateRequestObjectDtos(List<TaskNodeExecParamEntity> requestParamEntities,
            List<TaskNodeExecParamEntity> responseParamEntities) {
        List<RequestObjectDto> requestObjects = new ArrayList<>();

        if (requestParamEntities == null) {
            return requestObjects;
        }

        Map<String, Map<String, String>> respParamsByObjectId = calculateRespParamsByObjectId(requestParamEntities,
                responseParamEntities);

        Map<String, RequestObjectDto> objs = calculateRequestObjects(requestParamEntities, responseParamEntities);

        for (String objectId : objs.keySet()) {
            RequestObjectDto obj = objs.get(objectId);
            Map<String, String> respParamsMap = respParamsByObjectId.get(objectId);
            if (respParamsMap != null) {
                respParamsMap.forEach((k, v) -> {
                    obj.addOutput(k, v);
                });
            }

            requestObjects.add(obj);
        }

        return requestObjects;
    }

    private InterfaceParameterDto buildInterfaceParameterDto(PluginConfigInterfaceParameter p) {
        InterfaceParameterDto d = new InterfaceParameterDto();
        d.setType(p.getType());
        d.setName(p.getName());
        d.setDataType(p.getDataType());

        return d;
    }

}
