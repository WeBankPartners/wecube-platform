package com.webank.wecube.core.service.workflow;

import static com.webank.wecube.core.domain.plugin.PluginRegisteringModel.getCiTypeAttributeIds;
import static com.webank.wecube.core.domain.plugin.PluginRegisteringModel.getCiTypeIds;
import static com.webank.wecube.core.domain.plugin.PluginRegisteringModel.pathToList;
import static com.webank.wecube.core.utils.CollectionUtils.putToSet;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.core.domain.plugin.PluginConfigInterfaceParameter;
import com.webank.wecube.core.domain.workflow.FlowNodeVO;
import com.webank.wecube.core.domain.workflow.ProcessDefinitionDeployRequest;
import com.webank.wecube.core.domain.workflow.ProcessDefinitionDeployResponse;
import com.webank.wecube.core.domain.workflow.ProcessDefinitionEntity;
import com.webank.wecube.core.domain.workflow.ProcessDefinitionOutline;
import com.webank.wecube.core.domain.workflow.ProcessDefinitionPreviewVO;
import com.webank.wecube.core.domain.workflow.ProcessDefinitionTaskServiceEntity;
import com.webank.wecube.core.domain.workflow.ProcessDefinitionVO;
import com.webank.wecube.core.domain.workflow.ServiceTaskBindInfoVO;
import com.webank.wecube.core.domain.workflow.ServiceTaskVO;
import com.webank.wecube.core.domain.workflow.TaskNodeDefinitionPreviewResultVO;
import com.webank.wecube.core.domain.workflow.TaskNodeDefinitionPreviewResultVO.CiDataAttrItem;
import com.webank.wecube.core.domain.workflow.TaskNodeDefinitionPreviewResultVO.CiDataItem;
import com.webank.wecube.core.domain.workflow.TaskNodeDefinitionPreviewResultVO.HeaderItem;
import com.webank.wecube.core.domain.workflow.TaskNodeDefinitionPreviewVO;
import com.webank.wecube.core.interceptor.UsernameStorage;
import com.webank.wecube.core.support.cmdb.dto.v2.CatCodeDto;
import com.webank.wecube.core.support.cmdb.dto.v2.CategoryDto;
import com.webank.wecube.core.support.cmdb.dto.v2.CiTypeAttrDto;
import com.webank.wecube.core.support.cmdb.dto.v2.CiTypeDto;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;
import com.webank.wecube.platform.workflow.parse.BpmnParseAttachment;
import com.webank.wecube.platform.workflow.parse.BpmnProcessModelCustomizer;
import com.webank.wecube.platform.workflow.parse.SubProcessAdditionalInfo;

@Service
public class ProcessDefinitionService extends AbstractProcessService {
    private static final Logger log = LoggerFactory.getLogger(ProcessDefinitionService.class);

    private static final String BPMN_SUFFIX = ".bpmn20.xml";
    
    private String encoding = "UTF-8";

    public TaskNodeDefinitionPreviewResultVO previewTaskNodeDefinition(TaskNodeDefinitionPreviewVO request) {
        TaskNodeDefinitionPreviewResultVO vo = new TaskNodeDefinitionPreviewResultVO();
        if (request == null) {
            return vo;
        }

        if (StringUtils.isBlank(request.getProcDefKey()) || StringUtils.isBlank(request.getTaskNodeId())
                || StringUtils.isBlank(request.getRootCiTypeId()) || StringUtils.isBlank(request.getRootCiDataId())) {
            log.warn("procDefKey,taskNodeId,rootCiTypeId,rootCiDataId must provide");
            return vo;
        }

        try {
            return doPreviewTaskNodeDefinition(request, vo);
        } catch (IOException e) {
            log.error("errors while processing preview result", e);
            throw new WecubeCoreException("errors while processing preview result:" + e.getMessage());
        }
    }

    private TaskNodeDefinitionPreviewResultVO doPreviewTaskNodeDefinition(TaskNodeDefinitionPreviewVO request,
            TaskNodeDefinitionPreviewResultVO result) throws JsonParseException, JsonMappingException, IOException {
        String procDefKey = request.getProcDefKey();
        ProcessDefinition proc = repositoryService.createProcessDefinitionQuery().processDefinitionKey(procDefKey)
                .active().latestVersion().singleResult();

        if (proc == null) {
            log.error("illegal process definition key,procDefKey={}", procDefKey);
            throw new WecubeCoreException("process definition key is invalid");
        }

        int version = proc.getVersion();
        ProcessDefinitionEntity defEntity = processDefinitionEntityRepository.findByProcDefKeyAndVersion(procDefKey,
                version);

        if (defEntity == null) {
            log.error("such process definition did not regitered correctly,procDefKey={}, version={}", procDefKey,
                    version);
            throw new WecubeCoreException("such process definition did not regitered correctly");
        }

        String taskNodeId = request.getTaskNodeId();

        List<ProcessDefinitionTaskServiceEntity> taskEntities = processDefinitionTaskServiceEntityRepository
                .findTaskServicesByProcDefKeyAndVersionAndTaskNodeId(procDefKey, version, taskNodeId);

        if (taskEntities.isEmpty()) {
            log.error(
                    "such process definition does not have task node regitered,procDefKey={},version={},taskNodeId={}",
                    procDefKey, version, taskNodeId);
            throw new WecubeCoreException("such task node did not regitered correctly");
        }

        ProcessDefinitionTaskServiceEntity taskEntity = taskEntities.get(0);

        String rootCiDataId = request.getRootCiDataId();
        int rootCiTypeId = Integer.parseInt(request.getRootCiTypeId());

        result.setProcDefKey(procDefKey);
        result.setProcDefVersion(version);
        result.setRootCiDataId(rootCiDataId);
        result.setRootCiTypeId(request.getRootCiTypeId());
        result.setServiceName(taskEntity.getBindServiceName());
        result.setTaskNodeId(taskNodeId);

        int ciTypeId = calCiTypeIdForTaskServiceNode(rootCiTypeId, rootCiDataId, taskEntity);

        result.setBindCiTypeId(String.valueOf(ciTypeId));

        CiTypeDto bindCiTypeDto = cmdbServiceV2Stub.getCiType(ciTypeId, true);

        if (bindCiTypeDto == null) {
            throw new WecubeCoreException("cannot find such ci type information");
        }

        List<CiTypeAttrDto> attrDtos = bindCiTypeDto.getAttributes();

        int order = 0;
        for (CiTypeAttrDto attrDto : attrDtos) {
            if (attrDto.getIsHidden()) {
                continue;
            }

            if (!attrDto.getIsDisplayed()) {
                continue;
            }

            HeaderItem headerItem = new HeaderItem();
            headerItem.setCiAttrId(attrDto.getCiTypeAttrId());
            headerItem.setCiTypeId(attrDto.getCiTypeId());
            headerItem.setDescription(attrDto.getDescription());
            headerItem.setName(attrDto.getName());
            headerItem.setPropertyName(attrDto.getPropertyName());
            headerItem.setOrder(order++);

            result.addHeaderItem(headerItem);
        }

        List<SimpleCiDataInfo> ciDataInfos = calCiDataInfosForTaskServiceNode(rootCiTypeId, rootCiDataId, taskEntity);

        for (SimpleCiDataInfo ciDataInfo : ciDataInfos) {
            List<Object> records = cmdbServiceV2Stub.getCiDataByGuid(ciDataInfo.getCiDataTypeId(),
                    Arrays.asList(ciDataInfo.getGuid()));
            if (records.isEmpty()) {
                continue;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> recordMap = (Map<String, Object>) ((Map<String, Object>) records.get(0)).get("data");

            CiDataItem dataItem = new CiDataItem();
            dataItem.setCiDataId(ciDataInfo.getGuid());
            dataItem.setCiTypeId(ciDataInfo.getCiDataTypeId());
            dataItem.setName(bindCiTypeDto.getName());

            for (HeaderItem headerItem : result.getHeaderItems()) {
                CiDataAttrItem attrItem = new CiDataAttrItem();
                attrItem.setOrder(headerItem.getOrder());
                attrItem.setCiAttrId(headerItem.getCiAttrId());
                attrItem.setCiTypeId(headerItem.getCiTypeId());
                attrItem.setPropertyName(headerItem.getPropertyName());
                attrItem.setPropertyVal(recordMap.get(headerItem.getPropertyName()));

                dataItem.addAttrItem(attrItem);
            }

            result.addCiDataItem(dataItem);
        }

        return result;
    }

    public List<ServiceTaskVO> calPluginEnabledServiceTasks(String processDefinitionId) {

        ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();

        if (procDef == null) {
            log.error("none process definition found for processDefinitionId={}", processDefinitionId);
            throw new WecubeCoreException("such process definition does not exist");
        }
        BpmnModelInstance bpmnModel = repositoryService.getBpmnModelInstance(processDefinitionId);

        Collection<org.camunda.bpm.model.bpmn.instance.Process> processes = bpmnModel
                .getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Process.class);

        if (processes.size() != 1) {
            log.error("At least one process should be provided, processDefinitionId={}", processDefinitionId);
            throw new WecubeCoreException("process definition is not correct");
        }

        org.camunda.bpm.model.bpmn.instance.Process process = processes.iterator().next();

        Collection<ServiceTask> serviceTasks = process.getChildElementsByType(ServiceTask.class);
        Collection<SubProcess> subProcesses = process.getChildElementsByType(SubProcess.class);

        List<ProcessDefinitionTaskServiceEntity> taskEntities = processDefinitionTaskServiceEntityRepository
                .findTaskServicesByProcDefKeyAndVersion(procDef.getKey(), procDef.getVersion());

        List<ServiceTaskVO> serviceTaskVOs = new ArrayList<>();
        for (ProcessDefinitionTaskServiceEntity taskEntity : taskEntities) {
            if(isServiceTask(taskEntity, serviceTasks) || isSubProcess(taskEntity, subProcesses)){
                ServiceTaskVO stVo = new ServiceTaskVO();
                stVo.setServiceCode(taskEntity.getBindServiceName());
                stVo.setId(taskEntity.getTaskNodeId());
                stVo.setName(taskEntity.getTaksNodeName());
                stVo.setCiLocateExpression(taskEntity.getBindCiRoutineExp());

                serviceTaskVOs.add(stVo);
            }else{
                log.warn("such task node ID does not exist in process definition, taskNodeId={}",
                        taskEntity.getTaskNodeId());
                continue;
            }
        }

        return serviceTaskVOs;
    }

    public Map<String, Object> evaluateRequiredInputParameters(String processDefinitionId) {
        List<ServiceTaskVO> serviceTasks = calPluginEnabledServiceTasks(processDefinitionId);
        if (isEmpty(serviceTasks)){
            throw new WecubeCoreException("No service task found for process - " + processDefinitionId);
        }
        return evaluateRequiredInputParameters(serviceTasks);
    }

    public Map<String, Object> evaluateRequiredInputParameters(List<ServiceTaskVO> serviceTasks) {
        List<PluginConfigInterfaceParameter> inputParameters = new ArrayList<>();
        Set<Integer> outputParameterAttributeIds = new HashSet<>();
        serviceTasks.stream().map(ServiceTaskVO::getServiceCode).forEach(serviceName -> {
            Optional<PluginConfigInterface> pluginConfigInterface = pluginConfigRepository
                    .findLatestOnlinePluginConfigInterfaceByServiceNameAndFetchParameters(serviceName);
            if (!pluginConfigInterface.isPresent()){
                throw new WecubeCoreException(
                        String.format("Plugin interface not found for serviceName [%s].", serviceName));
            }
            PluginConfigInterface inf = pluginConfigInterface.get();
            inputParameters.addAll(inf.getInputParameters());
            putToSet(outputParameterAttributeIds, inf.getOutputParameters(),
                    PluginConfigInterfaceParameter::getCmdbAttributeId);
        });

        Set<Integer> ciTypeIds = new HashSet<>();
        Set<Integer> requiredInputParameters = new HashSet<>();
        for (PluginConfigInterfaceParameter inputParameter : inputParameters) {
            ciTypeIds.add(inputParameter.getCmdbCitypeId());
            if (!outputParameterAttributeIds.contains(inputParameter.getCmdbAttributeId())) {
                requiredInputParameters.add(inputParameter.getCmdbAttributeId());
            }

            List<Integer> pathCiTypeAndAttributeIds = pathToList(inputParameter.getCmdbCitypePath());
            ciTypeIds.addAll(getCiTypeIds(pathCiTypeAndAttributeIds));
            getCiTypeAttributeIds(pathCiTypeAndAttributeIds).stream()
                    .filter(id -> !outputParameterAttributeIds.contains(inputParameter.getCmdbAttributeId()))
                    .forEach(requiredInputParameters::add);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("ci-types", cmdbServiceV2Stub.getCiTypes(new ArrayList<>(ciTypeIds), true));
        resultMap.put("required-input-parameters", requiredInputParameters);
        return resultMap;
    }

    public List<ProcessDefinitionOutline> outlineProcessDefinitions(
            List<ProcessDefinitionPreviewVO> definitionsPreviewVOs) {
        List<ProcessDefinitionOutline> outlines = new ArrayList<ProcessDefinitionOutline>();
        definitionsPreviewVOs.forEach(d -> {
            outlines.add(outlineProcessDefinition(d));
        });
        return outlines;
    }

    public ProcessDefinitionOutline outlineSingleProcessDefinition(ProcessDefinitionPreviewVO definitionsPreviewVO) {
        ProcessDefinitionOutline d = outlineProcessDefinition(definitionsPreviewVO);
        return d;
    }

    private ProcessDefinitionOutline outlineProcessDefinition(ProcessDefinitionPreviewVO definitionsPreviewVO) {
        ProcessDefinitionOutline outline = new ProcessDefinitionOutline();
        int procDefKeyEnumId = definitionsPreviewVO.getDefinitionKey();

        // String definitionKey = definitionsPreviewVO.getDefinitionKey();
        String definitionKey = getCodeByEnumCodeId(procDefKeyEnumId);

        if (StringUtils.isBlank(definitionKey)) {
            log.warn("Illegal definition key provided [{}]", definitionKey);
            return outline;
        }

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(definitionKey).latestVersion().singleResult();

        if (processDefinition == null) {
            log.warn("None process definition found with definition key [{}]", definitionKey);
            return outline;
        }

        String processDefinitionId = processDefinition.getId();

        outline.setDefinitionId(processDefinitionId);
        outline.setDefinitionName(processDefinition.getName());
        outline.setDefintiionKey(processDefinition.getKey());
        outline.setVersion(processDefinition.getVersion());

        BpmnModelInstance bpmnModel = repositoryService.getBpmnModelInstance(processDefinitionId);

        Collection<org.camunda.bpm.model.bpmn.instance.Process> processes = bpmnModel
                .getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Process.class);

        if (processes.size() != 1) {
            log.error("at least one process should be provided, processDefinitionId={}", processDefinitionId);
            return outline;
        }

        org.camunda.bpm.model.bpmn.instance.Process process = processes.iterator().next();

        Collection<StartEvent> startEvents = process.getChildElementsByType(StartEvent.class);

        if (startEvents.size() != 1) {
            log.error("only one start event supported, processDefinitionId={}", processDefinitionId);
            return outline;
        }

        StartEvent startEvent = startEvents.iterator().next();

        try {
            populateFlowNodes(outline, startEvent);
        } catch (Exception e) {
            log.error("errors while populating flow nodes", e);
        }

        return outline;
    }

    public ProcessDefinitionDeployResponse deployProcessDefinition(ProcessDefinitionDeployRequest request) {
        log.info("start to deploy process definition,request={}", request);

        ProcessDefinitionEntity processDefinitionEntity = buildProcessDefinitionEntity(request);

        processDefinitionEntityRepository.save(processDefinitionEntity);

        List<ProcessDefinitionTaskServiceEntity> processDefinitionTaskServiceEntities = buildProcessDefinitionTaskServiceEntities(
                request, processDefinitionEntity);
        processDefinitionTaskServiceEntityRepository.saveAll(processDefinitionTaskServiceEntities);
        
        String fileName = request.getProcessName() + BPMN_SUFFIX;
        
        BpmnParseAttachment bpmnParseAttachment = buildBpmnParseAttachment(processDefinitionEntity, processDefinitionTaskServiceEntities);
        
        BpmnProcessModelCustomizer customizer = new BpmnProcessModelCustomizer(fileName, request.getProcessData(), encoding);
        customizer.setBpmnParseAttachment(bpmnParseAttachment);
        BpmnModelInstance procModelInstance = customizer.build();
        
        DeploymentWithDefinitions deployment = repositoryService.createDeployment()
                .addModelInstance(fileName, procModelInstance).deployWithResult();
        List<ProcessDefinition> processDefs = deployment.getDeployedProcessDefinitions();

        if (processDefs == null || processDefs.isEmpty()) {
            log.error("abnormally to parse process definition,request={}", request);
            handleDeployFailure(processDefinitionEntity);
            throw new WecubeCoreException("process deploying failed");
        }

//        DeploymentBuilder deploymentBuilder = processEngine.getRepositoryService().createDeployment()
//                .name(request.getProcessName())
//                .addString(request.getProcessName() + BPMN_SUFFIX, request.getProcessData());
//
//        DeploymentWithDefinitions deployment = deploymentBuilder.deployWithResult();

        

        ProcessDefinition processDef = processDefs.get(0);

        updateProcessDefinitionEntity(processDefinitionEntity, processDef);

        updateProcessDefinitionTaskServiceEntities(processDefinitionTaskServiceEntities, processDef);

        pushProcDefKeyToCmdb(request.getRootCiTypeId(), procDefKeyCmdbAttrName, processDef.getKey(),
                processDef.getName());

        return buildProcessDefinitionDeployResponse(processDef);
    }
    
    private BpmnParseAttachment buildBpmnParseAttachment(ProcessDefinitionEntity processDefinitionEntity, List<ProcessDefinitionTaskServiceEntity> processDefinitionTaskServiceEntities){
        BpmnParseAttachment bpmnParseAttachment = new BpmnParseAttachment();
        
        for(ProcessDefinitionTaskServiceEntity entity : processDefinitionTaskServiceEntities){
            SubProcessAdditionalInfo info = new SubProcessAdditionalInfo();
            info.setSubProcessNodeId(entity.getTaskNodeId());
            info.setSubProcessNodeName(entity.getTaksNodeName());
            info.setTimeoutExpression(convertIsoTimeFormat(entity.getTimeoutExpression()));
            
            bpmnParseAttachment.addSubProcessAddtionalInfo(info);
        }
        
        return bpmnParseAttachment;
    }
    
    private String convertIsoTimeFormat(String timeoutExpression){
        if(StringUtils.isBlank(timeoutExpression)){
            return timeoutExpression;
        }
        
        return "PT"+timeoutExpression.trim()+"M";
    }

    private void updateProcessDefinitionEntity(ProcessDefinitionEntity entity, ProcessDefinition processDef) {
        entity.setActive(ProcessDefinitionEntity.ACTIVE_VALUE);
        entity.setProcDefKey(processDef.getKey());
        entity.setProcStatus(ProcessDefinitionEntity.PROC_STATUS_DEPLOYED);
        entity.setUpdateBy(UsernameStorage.getIntance().get());
        entity.setUpdateTime(new Date());
        entity.setVersion(processDef.getVersion());

        processDefinitionEntityRepository.save(entity);
    }

    private List<ProcessDefinitionTaskServiceEntity> buildProcessDefinitionTaskServiceEntities(
            ProcessDefinitionDeployRequest request, ProcessDefinitionEntity processDefinitionEntity) {
        List<ProcessDefinitionTaskServiceEntity> taskServiceEntities = new ArrayList<ProcessDefinitionTaskServiceEntity>();

        Date currTime = new Date();
        String currUser = UsernameStorage.getIntance().get();
        for (ServiceTaskBindInfoVO bindInfo : request.getServiceTaskBindInfos()) {
            ProcessDefinitionTaskServiceEntity taskEntity = new ProcessDefinitionTaskServiceEntity();
            taskEntity.setId(LocalIdGenerator.INSTANCE.generateTimestampedId());
            taskEntity.setActive(ProcessDefinitionTaskServiceEntity.ACTIVE_VALUE);
            taskEntity.setBindCiRoutineExp(bindInfo.getCiRoutineExp());
            taskEntity.setBindCiRoutineRaw(bindInfo.getCiRoutineRaw());
            taskEntity.setBindServiceId(bindInfo.getServiceId());
            taskEntity.setBindServiceName(bindInfo.getServiceName());
            taskEntity.setTaskNodeId(bindInfo.getNodeId());
            taskEntity.setTaksNodeName(bindInfo.getNodeName());

            taskEntity.setDescription(bindInfo.getDescription());
            taskEntity.setTimeoutExpression(bindInfo.getTimeoutExpression());

            taskEntity.setCreateTime(currTime);
            taskEntity.setCreateBy(currUser);

            taskEntity.setCoreProcDefId(processDefinitionEntity.getId());

            taskServiceEntities.add(taskEntity);
        }

        return taskServiceEntities;
    }

    private void updateProcessDefinitionTaskServiceEntities(
            List<ProcessDefinitionTaskServiceEntity> processDefinitionTaskServiceEntities,
            ProcessDefinition processDef) {
        BpmnModelInstance bpmnModel = repositoryService.getBpmnModelInstance(processDef.getId());

        Collection<org.camunda.bpm.model.bpmn.instance.Process> processes = bpmnModel
                .getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Process.class);

        if (processes.size() != 1) {
            log.error("at least one process should be provided, processDefinitionId={}", processDef.getId());
            throw new WecubeCoreException("process definition is not correct");
        }

        org.camunda.bpm.model.bpmn.instance.Process process = processes.iterator().next();

        Collection<ServiceTask> serviceTasks = process.getChildElementsByType(ServiceTask.class);
        Collection<SubProcess> subProcesses = process.getChildElementsByType(SubProcess.class);

        Date currTime = new Date();
        String currUser = UsernameStorage.getIntance().get();

        for (ProcessDefinitionTaskServiceEntity entity : processDefinitionTaskServiceEntities) {
            entity.setUpdateBy(currUser);
            entity.setUpdateTime(currTime);
            entity.setProcDefId(processDef.getId());
            entity.setProcDefKey(processDef.getKey());
            entity.setVersion(processDef.getVersion());
            entity.setActive(ProcessDefinitionTaskServiceEntity.ACTIVE_VALUE);

            if (isServiceTask(entity, serviceTasks)) {
                entity.setTaskNodeType(ProcessDefinitionTaskServiceEntity.TASK_NODE_TYPE_SERVICE_TASK);
                processDefinitionTaskServiceEntityRepository.save(entity);
            } else if (isSubProcess(entity, subProcesses)) {
                entity.setTaskNodeType(ProcessDefinitionTaskServiceEntity.TASK_NODE_TYPE_SUBPROCESS);
                processDefinitionTaskServiceEntityRepository.save(entity);
            } else {
                log.warn("such service task bind information is invalid, nodeId={}, serviceTaskId={}",
                        entity.getTaskNodeId(), entity.getBindServiceId());
                processDefinitionTaskServiceEntityRepository.delete(entity);
            }
        }
    }

    

    private void handleDeployFailure(ProcessDefinitionEntity processDefinitionEntity) {
        List<ProcessDefinitionTaskServiceEntity> taskServiceEntities = processDefinitionTaskServiceEntityRepository
                .findAllByProcDefId(processDefinitionEntity.getId());
        
        processDefinitionTaskServiceEntityRepository.deleteAll(taskServiceEntities);
        
        processDefinitionEntityRepository.delete(processDefinitionEntity);
    }

    private void pushProcDefKeyToCmdb(int ciTypeId, String catAttrName, String procDefKey, String procDefName) {
        if (StringUtils.isBlank(procDefName)) {
            procDefName = procDefKey;
        }

        CategoryDto category = queryCategory(catAttrName, ciTypeId);
        if (category == null) {
            log.error("such category did not find,ciTypeId={}, catAttrName={}", ciTypeId, catAttrName);
            throw new WecubeCoreException("such category did not find");
        }

        CatCodeDto catCode = queryEnumCodes(procDefKey, category.getCatId());
        if (catCode != null) {
            log.info("such cat code already exists,catId={}, catCode={}", category.getCatId(), procDefKey);

            if (procDefName.equals(catCode.getValue())) {
                return;
            }

            catCode.setValue(procDefName);
            cmdbServiceV2Stub.updateEnumCodes(catCode);
            return;
        }

        createEnumCodes(category.getCatId(), procDefKey, procDefName);
    }

    public List<ProcessDefinitionVO> listProcessDefinitions() {
        List<ProcessDefinitionVO> pdVos = new ArrayList<ProcessDefinitionVO>();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        ProcessDefinitionQuery pdQuery = repositoryService.createProcessDefinitionQuery().latestVersion();
        List<ProcessDefinition> pds = pdQuery.list();

        pds.forEach(v -> {
            ProcessDefinitionVO vo = new ProcessDefinitionVO();
            vo.setDefinitionBizKey(v.getKey());
            vo.setDefinitionId(v.getId());
            vo.setDefinitionVersion(String.valueOf(v.getVersion()));
            vo.setProcessName(v.getName());

            pdVos.add(vo);
        });

        return pdVos;
    }

    public ProcessDefinitionVO findProcessDefinition(String processDefinitionId) {

        ProcessDefinitionQuery pdQuery = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId);

        ProcessDefinition pd = pdQuery.singleResult();

        if (pd == null) {
            log.warn("cannot find process definition with id, processDefinitionId={}", processDefinitionId);
            throw new WecubeCoreException("such process definition doesnt exist");
        }

        

        ProcessDefinitionVO procVo = new ProcessDefinitionVO();
        procVo.setDefinitionBizKey(pd.getKey());
        procVo.setDefinitionId(pd.getId());
        procVo.setDefinitionVersion(String.valueOf(pd.getVersion()));
        procVo.setProcessName(pd.getName());
        

        String procDefKey = pd.getKey();
        int version = pd.getVersion();

        ProcessDefinitionEntity defEntity = processDefinitionEntityRepository.findByProcDefKeyAndVersion(procDefKey,
                version);
        if (defEntity == null) {
            log.error("none process definition entity found for processDefinitionId={}", processDefinitionId);
            throw new WecubeCoreException("such process definition doesnt exist");
        } else {
            procVo.setRootCiTypeId(defEntity.getBindCiTypeId());
            procVo.setDefinitionText(defEntity.getProcData());
        }

        List<ProcessDefinitionTaskServiceEntity> taskEntities = processDefinitionTaskServiceEntityRepository
                .findTaskServicesByProcDefKeyAndVersion(procDefKey, version);

        for (ProcessDefinitionTaskServiceEntity taskEntity : taskEntities) {
            procVo.addServiceTaskBindInfo(buildServiceTaskBindInfoVO(taskEntity));
        }

        return procVo;
    }

    private ProcessDefinitionDeployResponse buildProcessDefinitionDeployResponse(ProcessDefinition procDef) {
        ProcessDefinitionDeployResponse response = new ProcessDefinitionDeployResponse();
        response.setProcessDefinitionId(procDef.getId());
        response.setProcessName(procDef.getName());
        response.setProcessDefinitionVersion(String.valueOf(procDef.getVersion()));
        response.setProcessKey(procDef.getKey());

        return response;
    }

    private ProcessDefinitionEntity buildProcessDefinitionEntity(ProcessDefinitionDeployRequest request) {
        ProcessDefinitionEntity entity = new ProcessDefinitionEntity();
        entity.setActive(ProcessDefinitionEntity.INACTIVE_VALUE);
        entity.setCreateBy(UsernameStorage.getIntance().get());
        entity.setCreateTime(new Date());
        entity.setId(LocalIdGenerator.INSTANCE.generateTimestampedId());
        entity.setProcData(request.getProcessData());
        entity.setProcName(request.getProcessName());
        entity.setBindCiTypeId(request.getRootCiTypeId());
        entity.setProcStatus(ProcessDefinitionEntity.PROC_STATUS_PREDEPLOY);

        return entity;
    }

    private ServiceTaskBindInfoVO buildServiceTaskBindInfoVO(ProcessDefinitionTaskServiceEntity taskEntity) {
        ServiceTaskBindInfoVO infoVo = new ServiceTaskBindInfoVO();
        infoVo.setCiRoutineExp(taskEntity.getBindCiRoutineExp());
        infoVo.setCiRoutineRaw(taskEntity.getBindCiRoutineRaw());
        infoVo.setId(taskEntity.getId());
        infoVo.setNodeId(taskEntity.getTaskNodeId());
        infoVo.setNodeName(taskEntity.getTaksNodeName());

        infoVo.setProcessDefinitionKey(taskEntity.getProcDefKey());
        infoVo.setVersion(taskEntity.getVersion());

        infoVo.setServiceId(taskEntity.getBindServiceId());
        infoVo.setServiceName(taskEntity.getBindServiceName());
        infoVo.setDescription(taskEntity.getDescription());
        
        infoVo.setTimeoutExpression(taskEntity.getTimeoutExpression());

        return infoVo;
    }

    private void populateFlowNodes(ProcessDefinitionOutline outline, FlowNode rootFlowNode) {
        FlowNodeVO rootFlowNodeVO = buildFlowNodeVO(rootFlowNode);
        rootFlowNodeVO.setProcessDefinitionId(outline.getDefinitionId());

        outline.addFlowNode(rootFlowNodeVO);
        populateSucceedings(outline, rootFlowNode);
    }

    private void populateSucceedings(ProcessDefinitionOutline outline, FlowNode srcFlowNode) {
        Collection<FlowNode> succeedingFlowNodes = srcFlowNode.getSucceedingNodes().list();
        FlowNodeVO srcFlowNodeVO = findSourceFlowNodeVO(outline, srcFlowNode);

        for (FlowNode fn : succeedingFlowNodes) {
            FlowNodeVO vo = outline.findFlowNodeVOById(fn.getId());
            if (vo != null) {
                srcFlowNodeVO.addToNode(vo);
                vo.addFromNode(srcFlowNodeVO);
                populateSucceedings(outline, fn);
                continue;
            }

            // to exclude some nodes needing to hide
            if (fn.getElementType().getTypeName().equals("parallelGateway") && (fn.getPreviousNodes().count() == 1)) {
                vo = null;
                populateSucceedings(outline, fn);
                continue;
            }

            vo = buildFlowNodeVO(fn);
            vo.setProcessDefinitionId(outline.getDefinitionId());
            srcFlowNodeVO.addToNode(vo);
            vo.addFromNode(srcFlowNodeVO);

            outline.addFlowNode(vo);
            populateSucceedings(outline, fn);
        }
    }

    private FlowNodeVO findSourceFlowNodeVO(ProcessDefinitionOutline outline, FlowNode srcFlowNode) {
        FlowNodeVO srcFlowNodeVO = outline.findFlowNodeVOById(srcFlowNode.getId());
        if (srcFlowNodeVO != null) {
            return srcFlowNodeVO;
        }

        Collection<FlowNode> previousFlowNodes = srcFlowNode.getPreviousNodes().list();
        if (previousFlowNodes.isEmpty() || (previousFlowNodes.size() > 1)) {
            throw new RuntimeException("unknown previous flow node for node:" + srcFlowNode.getId());
        }

        FlowNode previousFlowNode = previousFlowNodes.iterator().next();

        return findSourceFlowNodeVO(outline, previousFlowNode);
    }
}
