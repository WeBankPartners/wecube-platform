package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.workflow.DynamicWorkflowInstCreationInfoDto;
import com.webank.wecube.platform.core.dto.workflow.DynamicWorkflowInstInfoDto;
import com.webank.wecube.platform.core.dto.workflow.ProcInstTerminationRequestDto;
import com.webank.wecube.platform.core.dto.workflow.RegisteredEntityAttrDefDto;
import com.webank.wecube.platform.core.dto.workflow.RegisteredEntityDefDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowNodeDefInfoDto;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageAttributes;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageEntities;
import com.webank.wecube.platform.core.entity.workflow.ProcDefAuthInfoQueryEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageAttributesMapper;
import com.webank.wecube.platform.core.repository.plugin.PluginPackageEntitiesMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcDefInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcRoleBindingMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeDefInfoMapper;
import com.webank.wecube.platform.core.service.dme.EntityQueryExprNodeInfo;
import com.webank.wecube.platform.core.service.dme.EntityQueryExpressionParser;

@Service
public class WorkflowPublicAccessService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowPublicAccessService.class);

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
    
    
    /**
     * 
     * @param requestDto
     */
    public void createWorkflowInstanceTerminationRequest(ProcInstTerminationRequestDto requestDto){
        if(requestDto == null){
            throw new WecubeCoreException("3320", "Unknown which process instance to terminate.");
        }
        
        if(StringUtils.isBlank(requestDto.getProcInstId())){
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
                    .findByProcIdAndRoleNameAndPermission(procDefInfo.getId(), roleName, ProcRoleBindingEntity.USE);
            if (procRoleBindingEntity != null) {
                procRoleBinding = procRoleBindingEntity;
                break;
            }
        }

        if (procRoleBinding == null) {
            log.info("There is not any authorized process found for {}.", currUserRoleNames);
            return nodeDefInfoDtos;
        }

        List<TaskNodeDefInfoEntity> taskNodeDefInfos = taskNodeDefInfoRepository.findAllByProcDefId(procDefId);

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

            RegisteredEntityDefDto boundEntity = buildTaskNodeBoundEntity(nodeDefInfo);

            // bound entity
            nodeDto.setBoundEntity(boundEntity);

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
        return new DynamicWorkflowInstInfoDto();
    }

    private RegisteredEntityDefDto buildTaskNodeBoundEntity(TaskNodeDefInfoEntity nodeDefInfo) {
        String routineExp = nodeDefInfo.getRoutineExp();
        if (StringUtils.isBlank(routineExp)) {
            return null;
        }

        List<EntityQueryExprNodeInfo> exprNodeInfos = this.entityQueryExpressionParser.parse(routineExp);
        if (exprNodeInfos == null || exprNodeInfos.isEmpty()) {
            return null;
        }

        EntityQueryExprNodeInfo tailExprNodeInfo = exprNodeInfos.get(exprNodeInfos.size() - 1);

        return buildRegisteredEntityDefDto(tailExprNodeInfo.getPackageName(), tailExprNodeInfo.getEntityName());
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
        List<ProcDefAuthInfoQueryEntity> procDefInfos = this.procDefInfoRepository.findAllAuthorizedProcDefs(roleNames);

        return procDefInfos;
    }

    private List<PluginPackageAttributes> findPluginPackageAttributesByEntityId(String entityId) {
        List<PluginPackageAttributes> attributes = this.pluginPackageAttributesMapper.selectAllByEntity(entityId);
        return attributes;
    }

}
