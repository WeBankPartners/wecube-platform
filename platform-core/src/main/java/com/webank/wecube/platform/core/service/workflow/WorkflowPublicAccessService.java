package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.workflow.FlowNodeDefDto;
import com.webank.wecube.platform.core.dto.workflow.GraphNodeDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefOutlineDto;
import com.webank.wecube.platform.core.dto.workflow.ProcInstInfoDto;
import com.webank.wecube.platform.core.dto.workflow.ProcInstTerminationRequestDto;
import com.webank.wecube.platform.core.dto.workflow.ProcessDataPreviewDto;
import com.webank.wecube.platform.core.dto.workflow.StartProcInstRequestDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefObjectBindInfoDto;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaces;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageAttributes;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageEntities;
import com.webank.wecube.platform.core.entity.workflow.ProcDefAuthInfoQueryEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcExecContextEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.model.workflow.TaskNodeBindInfoContext;
import com.webank.wecube.platform.core.model.workflow.WorkflowInstCreationContext;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageAttributesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageEntitiesMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcDefInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcExecContextMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcRoleBindingMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeDefInfoMapper;
import com.webank.wecube.platform.core.service.dme.EntityDataRouteFactory;
import com.webank.wecube.platform.core.service.dme.EntityOperationRootCondition;
import com.webank.wecube.platform.core.service.dme.EntityQueryCriteria;
import com.webank.wecube.platform.core.service.dme.EntityQueryExprNodeInfo;
import com.webank.wecube.platform.core.service.dme.EntityQueryExpressionParser;
import com.webank.wecube.platform.core.service.dme.EntityQuerySpecification;
import com.webank.wecube.platform.core.service.dme.EntityRouteDescription;
import com.webank.wecube.platform.core.service.dme.EntityTreeNodesOverview;
import com.webank.wecube.platform.core.service.dme.StandardEntityDataNode;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationResponseDto;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationRestClient;
import com.webank.wecube.platform.core.service.dme.StandardEntityOperationService;
import com.webank.wecube.platform.core.service.plugin.PluginConfigMgmtService;
import com.webank.wecube.platform.core.support.plugin.dto.DynamicEntityValueDto;
import com.webank.wecube.platform.core.support.plugin.dto.DynamicTaskNodeBindInfoDto;
import com.webank.wecube.platform.core.support.plugin.dto.DynamicWorkflowInstCreationInfoDto;
import com.webank.wecube.platform.core.support.plugin.dto.DynamicWorkflowInstInfoDto;
import com.webank.wecube.platform.core.support.plugin.dto.RegisteredEntityAttrDefDto;
import com.webank.wecube.platform.core.support.plugin.dto.RegisteredEntityDefDto;
import com.webank.wecube.platform.core.support.plugin.dto.WorkflowDefInfoDto;
import com.webank.wecube.platform.core.support.plugin.dto.WorkflowNodeDefInfoDto;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class WorkflowPublicAccessService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowPublicAccessService.class);

    private static final String DME_DELIMETER = "#DME#";

    @Autowired
    private ProcDefInfoMapper procDefInfoRepository;

    @Autowired
    private ProcRoleBindingMapper procRoleBindingRepository;

    @Autowired
    private TaskNodeDefInfoMapper taskNodeDefInfoRepository;

    @Autowired
    private PluginPackageEntitiesMapper pluginPackageEntitiesMapper;

    @Autowired
    private PluginPackageAttributesMapper pluginPackageAttributesMapper;

    @Autowired
    private EntityQueryExpressionParser entityQueryExpressionParser;

    @Autowired
    private WorkflowProcInstService workflowProcInstService;

    @Autowired
    private WorkflowProcDefService workflowProcDefService;

    @Autowired
    protected PluginConfigMgmtService pluginConfigMgmtService;

    @Autowired
    private StandardEntityOperationService standardEntityOperationService;

    @Autowired
    @Qualifier("userJwtSsoTokenRestTemplate")
    protected RestTemplate userJwtSsoTokenRestTemplate;

    @Autowired
    private EntityDataRouteFactory entityDataRouteFactory;

    @Autowired
    private ProcExecContextMapper procExecContextMapper;

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 
     * @param procDefId
     * @param rootEntityDataId
     * @return
     */
    public ProcessDataPreviewDto calculateProcessDataPreview(String procDefId, String rootEntityDataId) {
        if (StringUtils.isBlank(procDefId) || StringUtils.isBlank(rootEntityDataId)) {
            throw new WecubeCoreException("3189", "Process definition ID or entity ID is not provided.");
        }

        ProcDefOutlineDto procDefOutline = workflowProcDefService.getProcessDefinitionOutline(procDefId);

        if (procDefOutline == null) {
            log.debug("process definition with id {} does not exist.", procDefId);
            throw new WecubeCoreException("3190",
                    String.format("Such process definition {%s} does not exist.", procDefId), procDefId);
        }

        ProcessDataPreviewDto previewDto = doCalculateProcessPreviewData(procDefOutline, rootEntityDataId, true);

        return previewDto;
    }

    /**
     * 
     * @param requestDto
     */
    public void createWorkflowInstanceTerminationRequest(ProcInstTerminationRequestDto requestDto) {
        if (requestDto == null) {
            throw new WecubeCoreException("3320", "Unknown which process instance to terminate.");
        }

        if (StringUtils.isBlank(requestDto.getProcInstId())) {
            throw new WecubeCoreException("3320", "Unknown which process instance to terminate.");
        }

        int procInstId = Integer.parseInt(requestDto.getProcInstId());
        workflowProcInstService.createProcessInstanceTermination(procInstId);
    }

    /**
     * 
     * @return
     */
    public List<WorkflowDefInfoDto> fetchLatestReleasedWorkflowDefs() {
        List<WorkflowDefInfoDto> procDefInfoDtos = new ArrayList<>();
        Set<String> currUserRoleNames = AuthenticationContextHolder.getCurrentUserRoles();
        if (currUserRoleNames == null || currUserRoleNames.isEmpty()) {
            return procDefInfoDtos;
        }

        List<ProcDefAuthInfoQueryEntity> procDefInfos = retrieveAllAuthorizedProcDefs(currUserRoleNames);
        if (procDefInfos == null || procDefInfos.isEmpty()) {
            log.debug("There is no authorized process found for {}", currUserRoleNames);
            return procDefInfoDtos;
        }

        Map<String, ProcDefAuthInfoQueryEntity> latestProcDefInfos = new HashMap<>();
        for (ProcDefAuthInfoQueryEntity e : procDefInfos) {
            ProcDefAuthInfoQueryEntity last = latestProcDefInfos.get(e.getProcDefKey());
            if (last == null) {
                latestProcDefInfos.put(e.getProcDefKey(), e);
                continue;
            }

            if (e.getProcDefVersion() > last.getProcDefVersion()) {
                latestProcDefInfos.put(e.getProcDefKey(), e);
            }
        }

        for (ProcDefAuthInfoQueryEntity e : latestProcDefInfos.values()) {
            WorkflowDefInfoDto dto = new WorkflowDefInfoDto();
            dto.setCreatedTime("");
            dto.setProcDefId(e.getId());
            dto.setProcDefKey(e.getProcDefKey());
            dto.setProcDefName(e.getProcDefName());
            dto.setRootEntity(buildRegisteredEntityDefDto(e.getRootEntity()));
            dto.setStatus(e.getStatus());

            procDefInfoDtos.add(dto);
        }

        return procDefInfoDtos;
    }

    /**
     * 
     * @param procDefId
     * @return
     */
    public List<WorkflowNodeDefInfoDto> fetchWorkflowTasknodeInfos(String procDefId) {
        List<WorkflowNodeDefInfoDto> nodeDefInfoDtos = new ArrayList<>();

        if (StringUtils.isBlank(procDefId)) {
            return nodeDefInfoDtos;
        }

        ProcDefInfoEntity procDefInfo = procDefInfoRepository.selectByPrimaryKey(procDefId);
        if (procDefInfo == null) {
            log.info("Invalid process id {}", procDefId);
            return nodeDefInfoDtos;
        }

        Set<String> currUserRoleNames = AuthenticationContextHolder.getCurrentUserRoles();
        if (currUserRoleNames == null || currUserRoleNames.isEmpty()) {
            log.info("There is not any user role names found to fetch workflow task node infos.");
            return nodeDefInfoDtos;
        }

        ProcRoleBindingEntity procRoleBinding = null;
        for (String roleName : currUserRoleNames) {
            ProcRoleBindingEntity procRoleBindingEntity = procRoleBindingRepository
                    .selectByProcIdAndRoleNameAndPermission(procDefInfo.getId(), roleName, ProcRoleBindingEntity.USE);
            if (procRoleBindingEntity != null) {
                procRoleBinding = procRoleBindingEntity;
                break;
            }
        }

        if (procRoleBinding == null) {
            log.info("There is not any authorized process found for {}.", currUserRoleNames);
            return nodeDefInfoDtos;
        }

        List<TaskNodeDefInfoEntity> taskNodeDefInfos = taskNodeDefInfoRepository.selectAllByProcDefId(procDefId);

        if (taskNodeDefInfos == null || taskNodeDefInfos.isEmpty()) {
            return nodeDefInfoDtos;
        }

        for (TaskNodeDefInfoEntity nodeDefInfo : taskNodeDefInfos) {
            WorkflowNodeDefInfoDto nodeDto = new WorkflowNodeDefInfoDto();
            nodeDto.setNodeDefId(nodeDefInfo.getId());
            nodeDto.setNodeId(nodeDefInfo.getNodeId());
            nodeDto.setNodeName(nodeDefInfo.getNodeName());
            nodeDto.setNodeType(nodeDefInfo.getNodeType());
            nodeDto.setServiceId(nodeDefInfo.getServiceId());
            nodeDto.setServiceName(nodeDefInfo.getServiceName());

            nodeDto.setRoutineExp(nodeDefInfo.getRoutineExp());
            nodeDto.setTaskCategory(nodeDefInfo.getTaskCategory());

            List<RegisteredEntityDefDto> boundEntities = buildTaskNodeBoundEntities(nodeDefInfo);

            // bound entity
            nodeDto.setBoundEntities(boundEntities);

            nodeDefInfoDtos.add(nodeDto);
        }

        return nodeDefInfoDtos;
    }

    /**
     * 
     * @param creationInfoDto
     * @return
     */
    public DynamicWorkflowInstInfoDto createNewWorkflowInstance(DynamicWorkflowInstCreationInfoDto creationInfoDto) {
        log.info("try to create new workflow instance with data: {}", creationInfoDto);
        if (creationInfoDto == null) {
            throw new WecubeCoreException("Workflow instance creation infomation must provide.");
        }

        StartProcInstRequestDto requestDto = calculateStartProcInstContext(creationInfoDto);
        ProcInstInfoDto createdProcInstInfoDto = workflowProcInstService.createProcessInstance(requestDto);
        DynamicWorkflowInstInfoDto resultDto = new DynamicWorkflowInstInfoDto();
        resultDto.setId(createdProcInstInfoDto.getId());
        resultDto.setProcDefId(createdProcInstInfoDto.getProcDefId());
        resultDto.setProcDefKey(createdProcInstInfoDto.getProcDefKey());
        resultDto.setProcInstKey(createdProcInstInfoDto.getProcInstKey());
        resultDto.setStatus(createdProcInstInfoDto.getStatus());
        
        WorkflowInstCreationContext ctx = buildWorkflowInstCreationContext(creationInfoDto);

        String jsonData = convertWorkflowInstCreationContextToJson(ctx);

        ProcExecContextEntity contextEntity = new ProcExecContextEntity();
        contextEntity.setId(LocalIdGenerator.generateId());
        contextEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
        contextEntity.setCreatedTime(new Date());
        contextEntity.setCtxData(jsonData);
        contextEntity.setCtxDataFormat(ProcExecContextEntity.CTX_DATA_FORMAT_JSON);
        contextEntity.setCtxType(ProcExecContextEntity.CTX_TYPE_PROCESS);
        contextEntity.setProcDefId(createdProcInstInfoDto.getProcDefId());
        contextEntity.setProcInstId(createdProcInstInfoDto.getId());
        contextEntity.setRev(0);

        procExecContextMapper.insert(contextEntity);

        return resultDto;
    }

    private WorkflowInstCreationContext buildWorkflowInstCreationContext(DynamicWorkflowInstCreationInfoDto dto) {
        WorkflowInstCreationContext ctx = new WorkflowInstCreationContext();
        ctx.setProcDefId(dto.getProcDefId());
        ctx.setProcDefKey(dto.getProcDefKey());
        ctx.setRootEntityOid(dto.getRootEntityValue().getOid());

        ctx.addEntity(dto.getRootEntityValue());

        List<DynamicTaskNodeBindInfoDto> taskNodeBindInfos = dto.getTaskNodeBindInfos();
        if (taskNodeBindInfos == null) {
            taskNodeBindInfos = new ArrayList<>();
        }
        for (DynamicTaskNodeBindInfoDto dynamicBindInfoDto : taskNodeBindInfos) {
            List<DynamicEntityValueDto> boundEntityValues = dynamicBindInfoDto.getBoundEntityValues();
            if (boundEntityValues == null || boundEntityValues.isEmpty()) {
                continue;
            }

            for (DynamicEntityValueDto entityValueDto : boundEntityValues) {
                TaskNodeBindInfoContext bindCtx = new TaskNodeBindInfoContext();
                bindCtx.setBindFlag(ProcExecBindingEntity.BIND_FLAG_YES);
                bindCtx.setOid(entityValueDto.getOid());
                bindCtx.setEntityDataId(entityValueDto.getEntityDataId());
                bindCtx.setNodeId(dynamicBindInfoDto.getNodeId());
                bindCtx.setNodeDefId(dynamicBindInfoDto.getNodeDefId());

                ctx.addBinding(bindCtx);
            }
        }
        
        return ctx;
    }

    private String convertWorkflowInstCreationContextToJson(WorkflowInstCreationContext ctx) {
        try {
            String json = objectMapper.writeValueAsString(ctx);
            return json;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse json object to string.", e);
            throw new WecubeCoreException("Failed to parse json object to string.");
        }
    }

    protected ProcessDataPreviewDto doCalculateProcessPreviewData(ProcDefOutlineDto outline, String dataId,
            boolean needSaveTmp) {
        ProcessDataPreviewDto result = new ProcessDataPreviewDto();

        List<GraphNodeDto> hierarchicalEntityNodes = new ArrayList<>();
        String processSessionId = UUID.randomUUID().toString();

        Map<Object, Object> externalCacheMap = new HashMap<>();

        for (FlowNodeDefDto f : outline.getFlowNodes()) {
            String nodeType = f.getNodeType();

            if (!"subProcess".equals(nodeType)) {
                continue;
            }

            if (TaskNodeDefInfoEntity.DYNAMIC_BIND_YES.equalsIgnoreCase(f.getDynamicBind())) {
                log.info("task node {}-{} is dynamic binding node and no need to pre-bind.", f.getNodeDefId(),
                        f.getNodeName());
                continue;
            }

            tryProcessSingleFlowNodeDefDto(f, hierarchicalEntityNodes, dataId, processSessionId, needSaveTmp,
                    externalCacheMap);
        }

        StandardEntityOperationRestClient client = new StandardEntityOperationRestClient(userJwtSsoTokenRestTemplate);
        for (GraphNodeDto entityNode : hierarchicalEntityNodes) {
            tryEnrichEntityData(entityNode, client);
        }

        result.addAllEntityTreeNodes(hierarchicalEntityNodes);
        result.setProcessSessionId(processSessionId);

        return result;

    }

    private void tryEnrichEntityData(GraphNodeDto entityNode, StandardEntityOperationRestClient client) {
        EntityRouteDescription entityRoute = entityDataRouteFactory.deduceEntityDescription(entityNode.getPackageName(),
                entityNode.getEntityName());

        EntityQuerySpecification querySpec = new EntityQuerySpecification();
        EntityQueryCriteria c = new EntityQueryCriteria();
        c.setAttrName("id");
        c.setCondition(entityNode.getDataId());

        StandardEntityOperationResponseDto respDto = client.query(entityRoute, querySpec);
        List<Map<String, Object>> results = extractEntityDataFromResponse(respDto.getData());

        if (results == null || results.isEmpty()) {
            return;
        }
        entityNode.setEntityData(results.get(0));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractEntityDataFromResponse(Object responseData) {
        List<Map<String, Object>> recordMapList = new ArrayList<Map<String, Object>>();
        if (responseData == null) {
            log.info("response data is empty");
            return recordMapList;
        }

        if (responseData instanceof List) {
            List<?> dataList = ((List<Map<String, Object>>) responseData);
            for (Object m : dataList) {
                if (m == null) {
                    continue;
                }
                if (m instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) m;
                    recordMapList.add(dataMap);
                }
            }
        } else if (responseData instanceof Map) {
            Map<String, Object> dataMap = ((Map<String, Object>) responseData);
            recordMapList.add(dataMap);
        }

        return recordMapList;
    }

    private List<String> calculateDataModelExpressions(FlowNodeDefDto f) {
        if (StringUtils.isBlank(f.getRoutineExpression())) {
            return null;
        }

        String expr = f.getRoutineExpression();
        List<String> exprs = new ArrayList<>();

        if (StringUtils.isBlank(expr)) {
            return exprs;
        }

        String[] exprParts = expr.split(DME_DELIMETER);

        if (exprParts == null || exprParts.length <= 0) {
            return exprs;
        }

        String additionalFilterRule = tryFindOutAdditionalFilterRule(f);
        for (String exprPart : exprParts) {
            if (StringUtils.isBlank(exprPart)) {
                continue;
            }

            String trimmedExprPart = exprPart.trim();

            if (StringUtils.isNoneBlank(additionalFilterRule)) {
                trimmedExprPart = trimmedExprPart + additionalFilterRule.trim();
            }

            exprs.add(trimmedExprPart);
        }

        return exprs;
    }

    private String tryFindOutAdditionalFilterRule(FlowNodeDefDto f) {
        if (StringUtils.isBlank(f.getServiceId())) {
            return null;
        }

        PluginConfigInterfaces inter = pluginConfigMgmtService.getPluginConfigInterfaceByServiceName(f.getServiceId());
        if (inter == null) {
            return null;
        }

        if (StringUtils.isBlank(inter.getFilterRule())) {
            return null;
        }

        return inter.getFilterRule();
    }

    private void tryProcessSingleFlowNodeDefDto(FlowNodeDefDto f, List<GraphNodeDto> hierarchicalEntityNodes,
            String dataId, String processSessionId, boolean needSaveTmp, Map<Object, Object> cacheMap) {
        List<String> routineExprs = calculateDataModelExpressions(f);

        if (routineExprs == null || routineExprs.isEmpty()) {
            log.info("the routine expression is blank for {} {}", f.getNodeDefId(), f.getNodeName());
            return;
        }

        for (String routineExpr : routineExprs) {
            tryProcessSingleFlowNodeDefDtoAndExpression(routineExpr, f, hierarchicalEntityNodes, dataId,
                    processSessionId, needSaveTmp, cacheMap);
        }
    }

    private void tryProcessSingleFlowNodeDefDtoAndExpression(String routineExpr, FlowNodeDefDto f,
            List<GraphNodeDto> hierarchicalEntityNodes, String dataId, String processSessionId, boolean needSaveTmp,
            Map<Object, Object> cacheMap) {
        if (StringUtils.isBlank(routineExpr)) {
            log.info("the routine expression is blank for {} {}", f.getNodeDefId(), f.getNodeName());
            return;
        }

        log.info("About to fetch data for node {} {} with expression {} and data id {}", f.getNodeDefId(),
                f.getNodeName(), routineExpr, dataId);
        EntityOperationRootCondition condition = new EntityOperationRootCondition(routineExpr, dataId);
        List<StandardEntityDataNode> nodes = null;
        try {
            EntityTreeNodesOverview overview = standardEntityOperationService.generateEntityLinkOverview(condition,
                    this.userJwtSsoTokenRestTemplate, cacheMap);
            nodes = overview.getHierarchicalEntityNodes();

        } catch (Exception e) {
            String errMsg = String.format("Errors while fetching data for node %s %s with expr %s and data id %s",
                    f.getNodeDefId(), f.getNodeName(), routineExpr, dataId);
            log.error(errMsg, e);
            throw new WecubeCoreException("3191", errMsg, f.getNodeDefId(), f.getNodeName(), routineExpr, dataId);
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
                currNode.setDisplayName(tn.getDisplayName() == null ? null : tn.getDisplayName().toString());

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

    private StartProcInstRequestDto calculateStartProcInstContext(DynamicWorkflowInstCreationInfoDto creationInfoDto) {
        StartProcInstRequestDto requestDto = new StartProcInstRequestDto();
        requestDto.setEntityDataId(creationInfoDto.getRootEntityValue().getEntityDataId());
        requestDto.setEntityDisplayName(null);// TODO
        requestDto.setEntityTypeId(creationInfoDto.getRootEntityValue().getPackageName() + ":"
                + creationInfoDto.getRootEntityValue().getEntityName());
        requestDto.setProcDefId(creationInfoDto.getProcDefId());

        List<DynamicTaskNodeBindInfoDto> taskNodeBindInfos = creationInfoDto.getTaskNodeBindInfos();
        if (taskNodeBindInfos == null) {
            taskNodeBindInfos = new ArrayList<>();
        }
        List<TaskNodeDefObjectBindInfoDto> taskNodeBinds = new ArrayList<>();
        for (DynamicTaskNodeBindInfoDto dynamicBindInfoDto : taskNodeBindInfos) {
            List<DynamicEntityValueDto> boundEntityValues = dynamicBindInfoDto.getBoundEntityValues();
            if (boundEntityValues == null || boundEntityValues.isEmpty()) {
                continue;
            }

            for (DynamicEntityValueDto entityValueDto : boundEntityValues) {
                TaskNodeDefObjectBindInfoDto bindDto = new TaskNodeDefObjectBindInfoDto();
                bindDto.setBound(ProcExecBindingEntity.BIND_FLAG_YES);
                if (StringUtils.isBlank(entityValueDto.getEntityDataId())) {
                    bindDto.setEntityDataId("OID-" + entityValueDto.getOid());
                } else {
                    bindDto.setEntityDataId(entityValueDto.getEntityDataId());
                }
                bindDto.setEntityDisplayName(null);
                bindDto.setEntityTypeId(entityValueDto.getPackageName() + ":" + entityValueDto.getEntityName());
                bindDto.setNodeDefId(dynamicBindInfoDto.getNodeDefId());
                bindDto.setOrderedNo("");// TODO
                bindDto.setFullEntityDataId(null);// TODO

                taskNodeBinds.add(bindDto);
            }
        }

        requestDto.setTaskNodeBinds(taskNodeBinds);

        return requestDto;
    }

    private List<RegisteredEntityDefDto> buildTaskNodeBoundEntities(TaskNodeDefInfoEntity nodeDefInfo) {
        List<RegisteredEntityDefDto> registerEntities = new ArrayList<>();
        String routineExp = nodeDefInfo.getRoutineExp();
        if (StringUtils.isBlank(routineExp)) {
            return registerEntities;
        }

        String[] exprParts = routineExp.split(DME_DELIMETER);
        for (String exprPart : exprParts) {
            if (StringUtils.isBlank(exprPart)) {
                continue;
            }

            String nodeExpr = exprPart.trim();
            List<EntityQueryExprNodeInfo> exprNodeInfos = this.entityQueryExpressionParser.parse(nodeExpr);
            if (exprNodeInfos == null || exprNodeInfos.isEmpty()) {
                continue;
            }
            EntityQueryExprNodeInfo tailExprNodeInfo = exprNodeInfos.get(exprNodeInfos.size() - 1);

            RegisteredEntityDefDto regEntityDto = buildRegisteredEntityDefDto(tailExprNodeInfo.getPackageName(),
                    tailExprNodeInfo.getEntityName());
            registerEntities.add(regEntityDto);
        }

        return registerEntities;
    }

    private RegisteredEntityDefDto buildRegisteredEntityDefDto(String rootEntity) {
        if (StringUtils.isBlank(rootEntity)) {
            return null;
        }

        String[] rootEntityParts = rootEntity.split(":");
        if (rootEntityParts.length != 2) {
            log.info("Abnormal root entity string : {}", rootEntity);
            return null;
        }

        String packageName = rootEntityParts[0];
        String entityName = rootEntityParts[1];

        return buildRegisteredEntityDefDto(packageName, entityName);
    }

    private RegisteredEntityDefDto buildRegisteredEntityDefDto(String packageName, String entityName) {
        //
        PluginPackageEntities entity = findLatestPluginPackageEntity(packageName, entityName);
        if (entity == null) {
            log.info("Cannot find entity with package name: {} and entity name: {}", packageName, entityName);
            return null;
        }
        RegisteredEntityDefDto entityDefDto = new RegisteredEntityDefDto();
        entityDefDto.setDescription(entity.getDescription());
        entityDefDto.setDisplayName(entity.getDisplayName());
        entityDefDto.setId(entity.getId());
        entityDefDto.setName(entity.getName());
        entityDefDto.setPackageName(entity.getPackageName());

        List<PluginPackageAttributes> attrs = findPluginPackageAttributesByEntityId(entity.getId());
        if (attrs == null || attrs.isEmpty()) {
            return entityDefDto;
        }

        for (PluginPackageAttributes attr : attrs) {
            RegisteredEntityAttrDefDto dto = buildRegisteredEntityAttrDefDto(attr);
            entityDefDto.getAttributes().add(dto);
        }

        return entityDefDto;
    }

    private RegisteredEntityAttrDefDto buildRegisteredEntityAttrDefDto(PluginPackageAttributes attr) {
        RegisteredEntityAttrDefDto attrDto = new RegisteredEntityAttrDefDto();
        attrDto.setDataType(attr.getDataType());
        attrDto.setDescription(attr.getDescription());
        attrDto.setId(attr.getId());
        attrDto.setMandatory(attr.getMandatory() == null ? false : attr.getMandatory());
        attrDto.setName(attr.getName());

        attrDto.setRefAttrName(attr.getRefAttr());
        attrDto.setRefEntityName(attr.getRefEntity());
        attrDto.setRefPackageName(attr.getRefPackage());

        attrDto.setReferenceId(attr.getReferenceId());

        return attrDto;
    }

    private PluginPackageEntities findLatestPluginPackageEntity(String packageName, String entityName) {
        PluginPackageEntities entity = this.pluginPackageEntitiesMapper
                .selectLatestByPackageNameAndEntityName(packageName, entityName);

        return entity;
    }

    private List<ProcDefAuthInfoQueryEntity> retrieveAllAuthorizedProcDefs(Set<String> roleNames) {
        List<ProcDefAuthInfoQueryEntity> procDefInfos = this.procDefInfoRepository
                .selectAllAuthorizedProcDefs(roleNames);

        return procDefInfos;
    }

    private List<PluginPackageAttributes> findPluginPackageAttributesByEntityId(String entityId) {
        List<PluginPackageAttributes> attributes = this.pluginPackageAttributesMapper.selectAllByEntity(entityId);
        return attributes;
    }

}
