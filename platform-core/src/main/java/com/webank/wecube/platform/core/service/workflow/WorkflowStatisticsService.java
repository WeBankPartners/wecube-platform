package com.webank.wecube.platform.core.service.workflow;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.PageableDto;
import com.webank.wecube.platform.core.dto.plugin.QueryResponse;
import com.webank.wecube.platform.core.dto.plugin.SortingDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefBriefDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefObjectBindInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeExecParamDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionOverviewDetailDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionOverviewDetailQueryDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionOverviewDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionOverviewsQueryDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionReportDetailDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionReportDetailQueryDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionReportItemDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowExecutionReportQueryDto;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoOverviewEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingPluginStatistics;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingTasknodeStatistics;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecParamEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecRequestEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.repository.workflow.ProcExecBindingMapper;
import com.webank.wecube.platform.core.repository.workflow.ProcInstInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeExecParamMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeExecRequestMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeInstInfoMapper;

@Service
public class WorkflowStatisticsService extends AbstractWorkflowProcDefService {
    protected static final Logger log = LoggerFactory.getLogger(WorkflowStatisticsService.class);

    @Autowired
    protected ProcExecBindingMapper procExecBindingMapper;

    @Autowired
    protected TaskNodeExecParamMapper taskNodeExecParamMapper;

    @Autowired
    protected TaskNodeExecRequestMapper taskNodeExecRequestMapper;

    @Autowired
    protected TaskNodeInstInfoMapper taskNodeInstInfoMapper;

    @Autowired
    protected ProcInstInfoMapper procInstInfoMapper;

    /**
     * 
     * @param queryDto
     * @return
     */
    public List<WorkflowExecutionOverviewDetailDto> fetchWorkflowExecutionOverviewDetails(
            WorkflowExecutionOverviewDetailQueryDto queryDto) {
        Date startDate = parseDate(queryDto.getStartDate());
        Date endDate = parseDate(queryDto.getEndDate());
        List<ProcInstInfoEntity> procInstEntities = procInstInfoMapper
                .selectProcDefInfoOverviewEntities(queryDto.getProcDefId(), queryDto.getStatus(), startDate, endDate);

        List<WorkflowExecutionOverviewDetailDto> dtos = new ArrayList<>();
        if (procInstEntities == null) {
            return dtos;
        }

        for (ProcInstInfoEntity procInst : procInstEntities) {
            WorkflowExecutionOverviewDetailDto dto = new WorkflowExecutionOverviewDetailDto();
            String execEndDate = formatStatiticsDate(procInst.getUpdatedTime());
            dto.setExecEndDate(execEndDate);
            dto.setExecOper(procInst.getOper());
            String execStartDate = formatStatiticsDate(procInst.getCreatedTime());
            dto.setExecStartDate(execStartDate);
            dto.setProcDefId(procInst.getProcDefId());
            dto.setProcDefName(procInst.getProcDefName());
            dto.setProcInstId(String.valueOf(procInst.getId()));
            dto.setStatus(procInst.getStatus());

            String rootEntityDataId = "";
            String rootEntityDataName = "";

            ProcExecBindingEntity procBinding = procExecBindingMapper.selectProcInstBindings(procInst.getId());
            if (procBinding != null) {
                rootEntityDataId = procBinding.getEntityDataId();
                rootEntityDataName = procBinding.getEntityDataName();
            }

            if (StringUtils.isBlank(rootEntityDataName)) {
                rootEntityDataName = rootEntityDataId;
            }

            dto.setRootEntityDataId(rootEntityDataId);
            dto.setRootEntityDataName(rootEntityDataName);

            dtos.add(dto);
        }

        return dtos;
    }

    /**
     * 
     * @return
     */
    public List<WorkflowExecutionOverviewDto> fetchWorkflowExecutionOverviews(
            WorkflowExecutionOverviewsQueryDto queryDto) {
        List<WorkflowExecutionOverviewDto> overviewDtos = new ArrayList<>();
        List<ProcDefInfoOverviewEntity> overviewEntities = new ArrayList<>();

        Date startDate = null;
        Date endDate = null;
        List<String> procDefNames = null;
        startDate = parseDate(queryDto.getStartDate());
        endDate = parseDate(queryDto.getEndDate());
        procDefNames = queryDto.getProcDefNames();
        
        String sortField = null;
        String sortType = null;
        SortingDto sortDto = queryDto.getSorting();
        if (sortDto != null) {
            sortField = sortDto.getField();
            sortType = (sortDto.getAsc() ? "ASC" : "DESC");
        }
        
        overviewEntities = procInstInfoMapper.selectAllProcDefInfoOverviewEntitiesByCriteria(procDefNames, startDate,
                endDate, sortField, sortType);

        if (overviewEntities == null) {
            return overviewDtos;
        }

        for (ProcDefInfoOverviewEntity entity : overviewEntities) {
            WorkflowExecutionOverviewDto dto = new WorkflowExecutionOverviewDto();
            dto.setProcDefId(entity.getProcDefId());
            dto.setProcDefName(entity.getProcDefName());
            dto.setTotalInstances(entity.getTotalInstances());

            dto.setTotalInProgressInstances(entity.getTotalInProgressInstances());
            dto.setTotalCompletedInstances(entity.getTotalCompletedInstances());
            dto.setTotalFaultedInstances(entity.getTotalFaultedInstances());

            overviewDtos.add(dto);
        }
        return overviewDtos;
    }

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
                if (!TaskNodeDefInfoEntity.NODE_TYPE_SUBPROCESS.equalsIgnoreCase(nodeEntity.getNodeType())) {
                    continue;
                }
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

        Map<String, TaskNodeDefObjectBindInfoDto> bindObjectMap = new HashMap<>();

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
                String displayName = bindingEntity.getEntityDataName();
                if (StringUtils.isBlank(displayName)) {
                    displayName = bindingEntity.getEntityDataId();
                }
                bindInfoDto.setEntityDisplayName(displayName);
                bindInfoDto.setNodeDefId(bindingEntity.getNodeDefId());
                bindInfoDto.setOrderedNo("");
                bindInfoDto.setFullEntityDataId(bindingEntity.getFullEntityDataId());

                bindObjectMap.put(bindingEntity.getEntityDataId(), bindInfoDto);
            }
        }

        bindObjectDtos.addAll(bindObjectMap.values());

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

    /**
     * 
     * @param queryDto
     * @return
     */
    public QueryResponse<WorkflowExecutionReportItemDto> fetchWorkflowExecutionTasknodeReports(
            WorkflowExecutionReportQueryDto queryDto) {
        List<String> taskNodeIds = queryDto.getTaskNodeIds();
        if (taskNodeIds == null || taskNodeIds.isEmpty()) {
            throw new WecubeCoreException("Task nodes did not find and must provide.");
        }

        com.github.pagehelper.PageInfo<ProcExecBindingTasknodeStatistics> statisticsItemPageInfo = doFetchPageableProcExecBindingTasknodeStatistics(
                queryDto);

        List<ProcExecBindingTasknodeStatistics> statisticsItems = statisticsItemPageInfo.getList();

        List<WorkflowExecutionReportItemDto> workflowExecutionReportItemDtos = new ArrayList<>();

        for (ProcExecBindingTasknodeStatistics statisticsItem : statisticsItems) {
            WorkflowExecutionReportItemDto workflowExecutionReportItemDto = buildTasknodeWorkflowExecutionReportItem(
                    queryDto, statisticsItem);
            workflowExecutionReportItemDtos.add(workflowExecutionReportItemDto);
        }

        com.webank.wecube.platform.core.dto.plugin.PageInfo localPageInfo = new com.webank.wecube.platform.core.dto.plugin.PageInfo();
        localPageInfo.setPageSize(statisticsItemPageInfo.getPageSize());
        localPageInfo.setTotalRows(Long.valueOf(statisticsItemPageInfo.getTotal()).intValue());
        localPageInfo.setStartIndex(statisticsItemPageInfo.getStartRow());

        QueryResponse<WorkflowExecutionReportItemDto> queryResponseDto = new QueryResponse<WorkflowExecutionReportItemDto>(
                localPageInfo, workflowExecutionReportItemDtos);
        return queryResponseDto;
    }

    /**
     * 
     * @param queryDto
     * @return
     */
    public QueryResponse<WorkflowExecutionReportItemDto> fetchWorkflowExecutionPluginReports(
            WorkflowExecutionReportQueryDto queryDto) {
        List<String> serviceIds = queryDto.getServiceIds();
        if (serviceIds == null || serviceIds.isEmpty()) {
            throw new WecubeCoreException("Services did not find and must provide.");
        }

        com.github.pagehelper.PageInfo<ProcExecBindingPluginStatistics> statisticsItemPageInfo = doFetchPageableProcExecBindingPluginStatistics(
                queryDto);

        List<ProcExecBindingPluginStatistics> statisticsItems = statisticsItemPageInfo.getList();

        List<WorkflowExecutionReportItemDto> workflowExecutionReportItemDtos = new ArrayList<>();

        for (ProcExecBindingPluginStatistics statisticsItem : statisticsItems) {
            WorkflowExecutionReportItemDto workflowExecutionReportItemDto = buildPluginWorkflowExecutionReportItem(
                    queryDto, statisticsItem);
            workflowExecutionReportItemDtos.add(workflowExecutionReportItemDto);
        }

        com.webank.wecube.platform.core.dto.plugin.PageInfo localPageInfo = new com.webank.wecube.platform.core.dto.plugin.PageInfo();
        localPageInfo.setPageSize(statisticsItemPageInfo.getPageSize());
        localPageInfo.setTotalRows(Long.valueOf(statisticsItemPageInfo.getTotal()).intValue());
        localPageInfo.setStartIndex(statisticsItemPageInfo.getStartRow());

        QueryResponse<WorkflowExecutionReportItemDto> queryResponseDto = new QueryResponse<WorkflowExecutionReportItemDto>(
                localPageInfo, workflowExecutionReportItemDtos);
        return queryResponseDto;
    }

    /**
     * 
     * @param queryDto
     * @return
     */
    public List<WorkflowExecutionReportDetailDto> fetchWorkflowExecutionTasknodeReportDetails(
            WorkflowExecutionReportDetailQueryDto queryDto) {
        List<WorkflowExecutionReportDetailDto> detailDtos = new ArrayList<>();
        String nodeDefId = queryDto.getNodeDefId();

        String entityDataId = queryDto.getEntityDataId();

        String queryStatus = queryDto.getStatus();// Completed,Faulted

        Date startDate = parseDate(queryDto.getStartDate());
        Date endDate = parseDate(queryDto.getEndDate());

        // TaskNodeDefInfoEntity nodeDefInfo =
        // taskNodeDefInfoRepo.selectByPrimaryKey(nodeDefId);
        // ProcDefInfoEntity procDefInfo =
        // processDefInfoRepo.selectByPrimaryKey(nodeDefInfo.getProcDefId());

        List<TaskNodeExecParamEntity> errorCodeParams = new ArrayList<>();
        if (TaskNodeInstInfoEntity.COMPLETED_STATUS.equalsIgnoreCase(queryStatus)) {
            errorCodeParams = taskNodeExecParamMapper.selectSuccessTasknodeStatistics(nodeDefId, entityDataId,
                    startDate, endDate);
        } else if (TaskNodeInstInfoEntity.FAULTED_STATUS.equalsIgnoreCase(queryStatus)) {
            errorCodeParams = taskNodeExecParamMapper.selectFailedTasknodeStatistics(nodeDefId, entityDataId, startDate,
                    endDate);
        } else {
            //
        }

        for (TaskNodeExecParamEntity errorCodeParam : errorCodeParams) {
            WorkflowExecutionReportDetailDto detailDto = buildWorkflowExecutionReportDetail(queryDto, errorCodeParam);
            detailDtos.add(detailDto);
        }
        return detailDtos;
    }

    /**
     * 
     * @param queryDto
     * @return
     */
    public List<WorkflowExecutionReportDetailDto> fetchWorkflowExecutionPluginReportDetails(
            WorkflowExecutionReportDetailQueryDto queryDto) {

        List<WorkflowExecutionReportDetailDto> detailDtos = new ArrayList<>();

        String entityDataId = queryDto.getEntityDataId();
        String serviceId = queryDto.getServiceId();

        String queryStatus = queryDto.getStatus();// Completed,Faulted

        Date startDate = parseDate(queryDto.getStartDate());
        Date endDate = parseDate(queryDto.getEndDate());

        List<TaskNodeExecParamEntity> errorCodeParams = new ArrayList<>();
        if (TaskNodeInstInfoEntity.COMPLETED_STATUS.equalsIgnoreCase(queryStatus)) {
            errorCodeParams = taskNodeExecParamMapper.selectSuccessPluginStatistics(serviceId, entityDataId, startDate,
                    endDate);
        } else if (TaskNodeInstInfoEntity.FAULTED_STATUS.equalsIgnoreCase(queryStatus)) {
            errorCodeParams = taskNodeExecParamMapper.selectFailedPluginStatistics(serviceId, entityDataId, startDate,
                    endDate);
        } else {
            //
        }

        for (TaskNodeExecParamEntity errorCodeParam : errorCodeParams) {
            WorkflowExecutionReportDetailDto detailDto = buildWorkflowExecutionReportDetail(queryDto, errorCodeParam);
            detailDtos.add(detailDto);
        }
        return detailDtos;
    }

    private WorkflowExecutionReportDetailDto buildWorkflowExecutionReportDetail(
            WorkflowExecutionReportDetailQueryDto queryDto, TaskNodeExecParamEntity errorCodeParam) {
        WorkflowExecutionReportDetailDto detailDto = new WorkflowExecutionReportDetailDto();

        TaskNodeExecRequestEntity execReq = taskNodeExecRequestMapper.selectByPrimaryKey(errorCodeParam.getReqId());
        TaskNodeInstInfoEntity nodeInstInfo = taskNodeInstInfoMapper.selectByPrimaryKey(execReq.getNodeInstId());

        ProcInstInfoEntity procInstInfo = procInstInfoMapper.selectByPrimaryKey(nodeInstInfo.getProcInstId());

        detailDto.setEntityDataId(queryDto.getEntityDataId());
        detailDto.setNodeDefId(queryDto.getNodeDefId());
        detailDto.setNodeDefName(nodeInstInfo.getNodeName());

        String nodeExecDate = formatStatiticsDate(nodeInstInfo.getCreatedTime());
        detailDto.setNodeExecDate(nodeExecDate);
        detailDto.setNodeStatus(nodeInstInfo.getStatus());
        detailDto.setProcDefId(procInstInfo.getProcDefId());
        detailDto.setProcDefName(procInstInfo.getProcDefName());

        String procExecDate = formatStatiticsDate(procInstInfo.getCreatedTime());
        detailDto.setProcExecDate(procExecDate);
        detailDto.setProcExecOper(procInstInfo.getOper());
        detailDto.setProcStatus(procInstInfo.getStatus());
        detailDto.setReqId(execReq.getReqId());

        String execDate = formatStatiticsDate(errorCodeParam.getCreatedTime());
        detailDto.setExecDate(execDate);

        List<TaskNodeExecParamDto> execParamDtos = new ArrayList<>();
        List<TaskNodeExecParamEntity> reqExecParamEntities = taskNodeExecParamMapper
                .selectAllByRequestIdAndParamType(execReq.getReqId(), TaskNodeExecParamEntity.PARAM_TYPE_REQUEST);
        for (TaskNodeExecParamEntity reqExecParam : reqExecParamEntities) {
            TaskNodeExecParamDto dto = buildTaskNodeExecParamDto(reqExecParam);
            execParamDtos.add(dto);
        }

        List<TaskNodeExecParamEntity> respExecParamEntities = taskNodeExecParamMapper
                .selectAllByRequestIdAndParamType(execReq.getReqId(), TaskNodeExecParamEntity.PARAM_TYPE_RESPONSE);
        for (TaskNodeExecParamEntity respExecParam : respExecParamEntities) {
            TaskNodeExecParamDto dto = buildTaskNodeExecParamDto(respExecParam);
            execParamDtos.add(dto);
        }

        detailDto.setExecParams(execParamDtos);
        return detailDto;
    }

    private String formatStatiticsDate(Date date) {
        if (date == null) {
            return null;
        }

        String pattern = "yyyy-MM-dd HH:mm:ss SSS";
        DateFormat df = new SimpleDateFormat(pattern);
        String sDate = df.format(date);
        return sDate;
    }

    private TaskNodeExecParamDto buildTaskNodeExecParamDto(TaskNodeExecParamEntity e) {
        TaskNodeExecParamDto dto = new TaskNodeExecParamDto();
        dto.setRequestId(e.getReqId());
        dto.setEntityDataId(e.getEntityDataId());
        dto.setId(e.getId());
        dto.setObjectId(e.getObjId());
        dto.setParamDataType(e.getParamDataType());
        dto.setParamDataValue(e.getParamDataValue());
        dto.setParamName(e.getParamName());
        dto.setParamType(e.getParamType());
        return dto;
    }

    private WorkflowExecutionReportItemDto buildTasknodeWorkflowExecutionReportItem(
            WorkflowExecutionReportQueryDto queryDto, ProcExecBindingTasknodeStatistics statisticsItem) {
        WorkflowExecutionReportItemDto reportItemDto = new WorkflowExecutionReportItemDto();
        String nodeDefId = statisticsItem.getNodeDefId();
        String entityDataId = statisticsItem.getEntityDataId();
        reportItemDto.setEntityDataId(entityDataId);
        String entityDataName = statisticsItem.getEntityDataName();
        if (StringUtils.isBlank(entityDataName)) {
            entityDataName = entityDataId;
        }
        reportItemDto.setEntityDataName(entityDataName);
        reportItemDto.setNodeDefId(nodeDefId);

        reportItemDto.setNodeDefName(statisticsItem.getNodeDefName());

        reportItemDto.setProcDefName(statisticsItem.getProcDefName());

        reportItemDto.setSuccessCount(statisticsItem.getSuccessCount());
        reportItemDto.setFailureCount(statisticsItem.getFailureCount());
        return reportItemDto;
    }

    private WorkflowExecutionReportItemDto buildPluginWorkflowExecutionReportItem(
            WorkflowExecutionReportQueryDto queryDto, ProcExecBindingPluginStatistics statisticsItem) {
        WorkflowExecutionReportItemDto reportItemDto = new WorkflowExecutionReportItemDto();

        String serviceId = statisticsItem.getServiceId();
        String entityDataId = statisticsItem.getEntityDataId();
        reportItemDto.setEntityDataId(entityDataId);

        String entityDataName = statisticsItem.getEntityDataName();
        if (StringUtils.isBlank(entityDataName)) {
            entityDataName = entityDataId;
        }
        reportItemDto.setEntityDataName(entityDataName);
        reportItemDto.setServiceId(serviceId);

        reportItemDto.setSuccessCount(statisticsItem.getSuccessCount());
        reportItemDto.setFailureCount(statisticsItem.getFailureCount());
        return reportItemDto;
    }

    private Date parseDate(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        String pattern = "yyyy-MM-dd HH:mm:ss";
        DateFormat df = new SimpleDateFormat(pattern);

        try {
            Date date = df.parse(dateStr);
            return date;
        } catch (ParseException e) {
            return null;
        }
    }

    private com.github.pagehelper.PageInfo<ProcExecBindingTasknodeStatistics> doFetchPageableProcExecBindingTasknodeStatistics(
            WorkflowExecutionReportQueryDto queryDto) {
        String startDateStr = queryDto.getStartDate();
        String endDateStr = queryDto.getEndDate();

        Date startDate = parseDate(startDateStr);
        Date endDate = parseDate(endDateStr);

        String sortField = null;
        String sortType = null;
        SortingDto sortDto = queryDto.getSorting();
        if (sortDto != null) {
            sortField = sortDto.getField();
            sortType = (sortDto.getAsc() ? "ASC" : "DESC");
        }

        PageableDto pageable = queryDto.getPageable();
        int pageNum = pageable.getStartIndex() / pageable.getPageSize() + 1;
        int pageSize = pageable.getPageSize();
        PageHelper.startPage(pageNum, pageSize);
        List<ProcExecBindingTasknodeStatistics> items = procExecBindingMapper
                .selectAllProcExecBindingTasknodeStatistics(queryDto.getTaskNodeIds(), queryDto.getEntityDataIds(),
                        startDate, endDate, sortField, sortType);

        com.github.pagehelper.PageInfo<ProcExecBindingTasknodeStatistics> pageInfo = new com.github.pagehelper.PageInfo<ProcExecBindingTasknodeStatistics>(
                items);
        return pageInfo;
    }

    private com.github.pagehelper.PageInfo<ProcExecBindingPluginStatistics> doFetchPageableProcExecBindingPluginStatistics(
            WorkflowExecutionReportQueryDto queryDto) {

        String startDateStr = queryDto.getStartDate();
        String endDateStr = queryDto.getEndDate();

        Date startDate = parseDate(startDateStr);
        Date endDate = parseDate(endDateStr);

        String sortField = null;
        String sortType = null;
        SortingDto sortDto = queryDto.getSorting();
        if (sortDto != null) {
            sortField = sortDto.getField();
            sortType = (sortDto.getAsc() ? "ASC" : "DESC");
        }

        PageableDto pageable = queryDto.getPageable();
        int pageNum = pageable.getStartIndex() / pageable.getPageSize() + 1;
        int pageSize = pageable.getPageSize();
        PageHelper.startPage(pageNum, pageSize);
        List<ProcExecBindingPluginStatistics> items = procExecBindingMapper.selectAllProcExecBindingPluginStatistics(
                queryDto.getServiceIds(), queryDto.getEntityDataIds(), startDate, endDate, sortField, sortType);

        com.github.pagehelper.PageInfo<ProcExecBindingPluginStatistics> pageInfo = new com.github.pagehelper.PageInfo<ProcExecBindingPluginStatistics>(
                items);
        return pageInfo;
    }
}
