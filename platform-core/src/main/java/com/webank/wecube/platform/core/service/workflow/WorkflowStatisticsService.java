package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.dto.workflow.ProcDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefBriefDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefObjectBindInfoDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionReportItemDto;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.repository.workflow.ProcExecBindingMapper;

@Service
public class WorkflowStatisticsService extends AbstractWorkflowProcDefService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowStatisticsService.class);

    @Autowired
    protected ProcExecBindingMapper procExecBindingMapper;

    /**
     * 
     * @return
     */
    public List<ProcDefInfoDto> fetchReleasedWorkflowDefs() {

        List<ProcDefInfoDto> procDefDtos = new ArrayList<>();
        List<ProcDefInfoEntity> procDefs = processDefInfoRepo
                .selectAllProcDefsByStatus(ProcDefInfoEntity.DEPLOYED_STATUS);

        if (procDefs == null) {
            return procDefDtos;
        }

        for (ProcDefInfoEntity procDef : procDefs) {
            ProcDefInfoDto procDefDto = procDefInfoDtoFromEntity(procDef);
            procDefDtos.add(procDefDto);
        }

        return procDefDtos;
    }

    /**
     * 
     * @return
     */
    public List<String> fetchAllPluginConfigInterfaces() {
        List<String> serviceIds = taskNodeDefInfoRepo.selectAllBoundServices();

        return serviceIds;
    }

    /**
     * 
     * @param procDefIds
     * @return
     */
    public List<TaskNodeDefBriefDto> fetchWorkflowTasknodeInfos(List<String> procDefIds) {
        List<TaskNodeDefBriefDto> taskNodeDtos = new ArrayList<>();
        if (procDefIds == null || procDefIds.isEmpty()) {
            return taskNodeDtos;
        }

        for (String procDefId : procDefIds) {
            ProcDefInfoEntity procDefEntity = processDefInfoRepo.selectByPrimaryKey(procDefId);
            if (procDefEntity == null) {
                continue;
            }

            List<TaskNodeDefInfoEntity> nodeEntities = taskNodeDefInfoRepo.selectAllByProcDefId(procDefId);
            if (nodeEntities == null || nodeEntities.isEmpty()) {
                continue;
            }

            for (TaskNodeDefInfoEntity nodeEntity : nodeEntities) {
                TaskNodeDefBriefDto d = new TaskNodeDefBriefDto();
                d.setNodeDefId(nodeEntity.getId());
                d.setNodeId(nodeEntity.getNodeId());
                d.setNodeName(nodeEntity.getNodeName());
                d.setNodeType(nodeEntity.getNodeType());
                d.setProcDefId(nodeEntity.getProcDefId());
                d.setServiceId(nodeEntity.getServiceId());
                d.setServiceName(nodeEntity.getServiceName());

                taskNodeDtos.add(d);
            }
        }
        return taskNodeDtos;
    }

    /**
     * 
     * @param taskNodeIds
     * @return
     */
    public List<TaskNodeDefObjectBindInfoDto> fetchWorkflowTasknodeBindings(List<String> taskNodeIds) {
        List<TaskNodeDefObjectBindInfoDto> bindObjectDtos = new ArrayList<>();
        if (taskNodeIds == null || taskNodeIds.isEmpty()) {
            return bindObjectDtos;
        }

        for (String taskNodeId : taskNodeIds) {
            TaskNodeDefInfoEntity taskNodeEntity = taskNodeDefInfoRepo.selectByPrimaryKey(taskNodeId);
            if (taskNodeEntity == null) {
                continue;
            }

            List<ProcExecBindingEntity> bindingEntities = procExecBindingMapper
                    .selectAllBoundTaskNodeBindingsByNodeDef(taskNodeId);

            if (bindingEntities == null) {
                continue;
            }

            for (ProcExecBindingEntity bindingEntity : bindingEntities) {
                TaskNodeDefObjectBindInfoDto bindInfoDto = new TaskNodeDefObjectBindInfoDto();
                bindInfoDto.setBound(bindingEntity.getBindFlag());
                bindInfoDto.setEntityDataId(bindingEntity.getEntityDataId());
                bindInfoDto.setEntityTypeId(bindingEntity.getEntityTypeId());
                bindInfoDto.setEntityDisplayName(bindingEntity.getEntityDataName());
                bindInfoDto.setNodeDefId(bindingEntity.getNodeDefId());
                bindInfoDto.setOrderedNo("");
                bindInfoDto.setFullEntityDataId(bindingEntity.getFullEntityDataId());

                bindObjectDtos.add(bindInfoDto);
            }
        }

        return bindObjectDtos;
    }

    /**
     * 
     * @param serviceIds
     * @return
     */
    public List<TaskNodeDefObjectBindInfoDto> fetchWorkflowPluginBindings(List<String> serviceIds) {
        List<TaskNodeDefObjectBindInfoDto> bindObjectDtos = new ArrayList<>();
        if (serviceIds == null || serviceIds.isEmpty()) {
            return bindObjectDtos;
        }

        List<String> taskNodeIds = new ArrayList<>();
        for (String serviceId : serviceIds) {
            List<TaskNodeDefInfoEntity> nodeDefs = taskNodeDefInfoRepo.selectAllByServiceAndStatus(serviceId,
                    TaskNodeDefInfoEntity.DEPLOYED_STATUS);
            if (nodeDefs == null) {
                continue;
            }

            for (TaskNodeDefInfoEntity nodeDef : nodeDefs) {
                taskNodeIds.add(nodeDef.getId());
            }
        }

        return fetchWorkflowTasknodeBindings(taskNodeIds);
    }

    public List<WorkflowExecutionReportItemDto> fetchWorkflowExecutionTasknodeReports() {
        // TODO
        return null;
    }

    public List<WorkflowExecutionReportItemDto> fetchWorkflowExecutionPluginReports() {
        // TODO
        return null;
    }
}
