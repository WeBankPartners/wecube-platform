package com.webank.wecube.platform.core.service.workflow;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.PageableDto;
import com.webank.wecube.platform.core.dto.plugin.QueryResponse;
import com.webank.wecube.platform.core.dto.workflow.ProcDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefBriefDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefObjectBindInfoDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionReportDetailDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionReportDetailQueryDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionReportItemDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionReportQueryDto;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingTasknodeStatistics;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.repository.workflow.ProcExecBindingMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeExecParamMapper;

@Service
public class WorkflowStatisticsService extends AbstractWorkflowProcDefService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowStatisticsService.class);

    @Autowired
    protected ProcExecBindingMapper procExecBindingMapper;
    
    @Autowired
    protected TaskNodeExecParamMapper taskNodeExecParamMapper;

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
                    .selectAllTaskNodeBindingsByNodeDef(taskNodeId);

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

    public QueryResponse<WorkflowExecutionReportItemDto> fetchWorkflowExecutionTasknodeReports(WorkflowExecutionReportQueryDto queryDto) {
        List<String> taskNodeIds = queryDto.getTaskNodeIds();
        if(taskNodeIds == null || taskNodeIds.isEmpty()) {
            throw new WecubeCoreException("Task nodes did not find and must provide.");
        }
        
        com.github.pagehelper.PageInfo<ProcExecBindingTasknodeStatistics> statisticsItemPageInfo = doFetchPageableProcExecBindingTasknodeStatistics(queryDto);
        
        List<ProcExecBindingTasknodeStatistics> statisticsItems = statisticsItemPageInfo.getList();
        
        List<WorkflowExecutionReportItemDto> workflowExecutionReportItemDtos = new ArrayList<>();
        
        for(ProcExecBindingTasknodeStatistics statisticsItem : statisticsItems) {
            WorkflowExecutionReportItemDto workflowExecutionReportItemDto = buildWorkflowExecutionReportItem(queryDto, statisticsItem);
            workflowExecutionReportItemDtos.add(workflowExecutionReportItemDto);
        }
        
        
        com.webank.wecube.platform.core.dto.plugin.PageInfo localPageInfo = new com.webank.wecube.platform.core.dto.plugin.PageInfo();
        localPageInfo.setPageSize(statisticsItemPageInfo.getPageSize());
        localPageInfo.setTotalRows(Long.valueOf(statisticsItemPageInfo.getTotal()).intValue());
        localPageInfo.setStartIndex(statisticsItemPageInfo.getStartRow());
        
        QueryResponse<WorkflowExecutionReportItemDto> queryResponseDto = new QueryResponse<WorkflowExecutionReportItemDto>(localPageInfo, workflowExecutionReportItemDtos);
        return queryResponseDto;
    }

    public QueryResponse<WorkflowExecutionReportItemDto> fetchWorkflowExecutionPluginReports(WorkflowExecutionReportQueryDto queryDto) {
        // TODO
        return null;
    }
    
    public List<WorkflowExecutionReportDetailDto> fetchWorkflowExecutionTasknodeReportDetails(WorkflowExecutionReportDetailQueryDto queryDto){
        //TODO
        return null;
    }
    
    public List<WorkflowExecutionReportDetailDto> fetchWorkflowExecutionPluginReportDetails(WorkflowExecutionReportDetailQueryDto queryDto){
        //TODO
        return null;
    }
    
    private WorkflowExecutionReportItemDto buildWorkflowExecutionReportItem(WorkflowExecutionReportQueryDto queryDto, ProcExecBindingTasknodeStatistics statisticsItem) {
        WorkflowExecutionReportItemDto reportItemDto = new WorkflowExecutionReportItemDto();
        String nodeDefId = statisticsItem.getNodeDefId();
        String entityDataId = statisticsItem.getEntityDataId();
        reportItemDto.setEntityDataId(entityDataId);
        reportItemDto.setEntityDataName(statisticsItem.getEntityDataName());
        reportItemDto.setNodeDefId(nodeDefId);
        
        TaskNodeDefInfoEntity nodeDefInfo = taskNodeDefInfoRepo.selectByPrimaryKey(nodeDefId);
        String procDefId = nodeDefInfo.getProcDefId();
        
        
        reportItemDto.setNodeDefName(nodeDefInfo.getNodeName());
        
        ProcDefInfoEntity procDefInfo = processDefInfoRepo.selectByPrimaryKey(procDefId);
        reportItemDto.setProcDefId(procDefId);
        reportItemDto.setProcDefName(procDefInfo.getProcDefName());
        
        String startDateStr = queryDto.getStartDate();
        String endDateStr = queryDto.getEndDate();
        
        Date startDate = parseDate(startDateStr);
        Date endDate = parseDate(endDateStr);
        
        //TODO
        
        int succTimes = taskNodeExecParamMapper.countSuccessTasknodeStatistics(nodeDefId, entityDataId, startDate, endDate);
        int failedTimes = taskNodeExecParamMapper.countFailedTasknodeStatistics(nodeDefId, entityDataId, startDate, endDate);
        
        reportItemDto.setSuccessCount(succTimes);
        reportItemDto.setFailureCount(failedTimes);
        //TODO
        return reportItemDto;
    }
    
    private Date parseDate(String dateStr) {
        if(StringUtils.isBlank(dateStr)) {
            return null;
        }
        String pattern = "yyyyMMdd";
        DateFormat df = new SimpleDateFormat(pattern);
        
        try {
            Date date = df.parse(dateStr);
            return date;
        } catch (ParseException e) {
            return null;
        }
    }
    
    private com.github.pagehelper.PageInfo<ProcExecBindingTasknodeStatistics> doFetchPageableProcExecBindingTasknodeStatistics(WorkflowExecutionReportQueryDto queryDto){
        PageableDto pageable = queryDto.getPageable();
        int pageNum = pageable.getStartIndex() / pageable.getPageSize() + 1;
        int pageSize = pageable.getPageSize();
        PageHelper.startPage(pageNum, pageSize);
        List<ProcExecBindingTasknodeStatistics> items = procExecBindingMapper.selectAllProcExecBindingTasknodeStatistics(queryDto.getTaskNodeIds(), queryDto.getEntityDataIds());
        
        com.github.pagehelper.PageInfo<ProcExecBindingTasknodeStatistics> pageInfo = new com.github.pagehelper.PageInfo<ProcExecBindingTasknodeStatistics>(items);
        return pageInfo;
    }
}
