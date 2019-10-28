package com.webank.wecube.platform.core.service.workflow;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.workflow.ProcDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefInfoDto;
import com.webank.wecube.platform.workflow.entity.ProcessInstanceStatusEntity;
import com.webank.wecube.platform.workflow.entity.ServiceNodeStatusEntity;
import com.webank.wecube.platform.workflow.model.ProcDefOutline;
import com.webank.wecube.platform.workflow.model.ProcFlowNode;
import com.webank.wecube.platform.workflow.model.ProcFlowNodeInst;
import com.webank.wecube.platform.workflow.model.ProcInstOutline;
import com.webank.wecube.platform.workflow.model.TraceStatus;
import com.webank.wecube.platform.workflow.parse.BpmnCustomizationException;
import com.webank.wecube.platform.workflow.parse.BpmnParseAttachment;
import com.webank.wecube.platform.workflow.parse.BpmnProcessModelCustomizer;
import com.webank.wecube.platform.workflow.parse.SubProcessAdditionalInfo;
import com.webank.wecube.platform.workflow.repository.ProcessInstanceStatusRepository;
import com.webank.wecube.platform.workflow.repository.ServiceNodeStatusRepository;

@Service
public class WorkflowEngineService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowEngineService.class);

    private static final String BPMN_SUFFIX = ".bpmn20.xml";

    private String encoding = "UTF-8";

    @Autowired
    protected RepositoryService repositoryService;

    @Autowired
    protected RuntimeService runtimeService;
    
    @Autowired
    protected HistoryService historyService;

    @Autowired
    protected ProcessInstanceStatusRepository processInstanceStatusRepository;

    @Autowired
    protected ServiceNodeStatusRepository serviceNodeStatusRepository;

    public ProcInstOutline getProcInstOutline(String procInstId) {
        if (procInstId == null) {
            throw new IllegalArgumentException("Process instance is null.");
        }

        ProcessInstanceStatusEntity procInstStatusEntity = processInstanceStatusRepository
                .findOneByprocInstanceId(procInstId);

        String processInstanceId = null;
        String processDefinitionId = null;
        if (TraceStatus.Completed.equals(procInstStatusEntity.getStatus())) {
            processInstanceId = procInstStatusEntity.getProcInstanceId();
            processDefinitionId = procInstStatusEntity.getProcDefinitionId();
        } else {
            ProcessInstance existProcInst = getProcessInstanceByProcInstId(procInstId);

            if (existProcInst == null) {
                log.error("such process instance does not exist,procInstId={}", procInstId);
                throw new WecubeCoreException("Such process instance does not exist.");
            }

            processInstanceId = existProcInst.getId();
            processDefinitionId = existProcInst.getProcessDefinitionId();
        }

        ProcessDefinition procDef = getProcessDefinitionByProcId(processDefinitionId);

        if (procDef == null) {
            log.error("such process definition does not exist,procDefId={}", processDefinitionId);
            throw new WecubeCoreException("Such process definition does not exist.");
        }

        BpmnModelInstance bpmnModel = repositoryService.getBpmnModelInstance(procDef.getId());

        Collection<org.camunda.bpm.model.bpmn.instance.Process> processes = bpmnModel
                .getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Process.class);

        org.camunda.bpm.model.bpmn.instance.Process process = processes.iterator().next();

        Collection<StartEvent> startEvents = process.getChildElementsByType(StartEvent.class);

        StartEvent startEvent = startEvents.iterator().next();

        ProcInstOutline result = new ProcInstOutline();
        result.setId(processInstanceId);
        result.setProcInstKey(procInstStatusEntity.getProcInstanceBizKey());
        result.setProcDefKernelId(procDef.getId());
        result.setProcDefKey(procDef.getKey());
        result.setProcDefName(procDef.getName());

        populateFlowNodeInsts(result, startEvent);
        refreshFlowNodeStatus(result);

        return result;
    }

    protected void refreshFlowNodeStatus(ProcInstOutline outline) {
        for (ProcFlowNodeInst n : outline.getNodeInsts()) {
            if (n.getStatus() != null) {
                continue;
            }

            String nodeStatus = null;

            for (ProcFlowNode child : n.getPreviousFlowNodes()) {
                ProcFlowNodeInst fi = (ProcFlowNodeInst) child;
                if (!TraceStatus.Completed.name().equals(fi.getStatus())) {
                    nodeStatus = TraceStatus.NotStarted.name();
                    break;
                }
            }

            for (ProcFlowNode child : n.getSucceedingFlowNodes()) {
                ProcFlowNodeInst fi = (ProcFlowNodeInst) child;
                if (!TraceStatus.NotStarted.name().equals(fi.getStatus())) {
                    nodeStatus = TraceStatus.Completed.name();
                    break;
                }
            }
            
            if(nodeStatus == null){
                Collection<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
                        .processDefinitionId(outline.getProcDefKernelId()).processInstanceId(outline.getId())
                        .activityId(n.getId()).finished().list();
                HistoricActivityInstance activity = null;

                if (activities.size() > 0) {
                    activity = activities.iterator().next();
                }

                if (activity != null) {
                    nodeStatus = TraceStatus.Completed.name();
                }
            }
            
            if(nodeStatus != null){
                n.setStatus(nodeStatus);
            }

        }
    }

    protected void populateFlowNodeInsts(ProcInstOutline outline, FlowNode flowNode) {
        ProcFlowNodeInst pfn = outline.findProcFlowNodeInstByNodeId(flowNode.getId());
        if (pfn == null) {
            pfn = new ProcFlowNodeInst();
            pfn.setId(flowNode.getId());
            pfn.setNodeType(flowNode.getElementType().getTypeName());
            pfn.setNodeName(flowNode.getName() == null ? "" : flowNode.getName());
            outline.addNodeInsts(pfn);
        }

        ServiceNodeStatusEntity nodeStatus = serviceNodeStatusRepository
                .findOneByProcInstanceIdAndNodeId(outline.getId(), pfn.getId());

        if (nodeStatus != null) {
            pfn.setStartTime(nodeStatus.getStartTime());
            pfn.setEndTime(nodeStatus.getEndTime());
            pfn.setStatus(nodeStatus.getStatus().name());
        }

        for (FlowNode fn : flowNode.getSucceedingNodes().list()) {
            ProcFlowNodeInst childPfn = outline.findProcFlowNodeInstByNodeId(fn.getId());
            if (childPfn == null) {
                childPfn = new ProcFlowNodeInst();
                childPfn.setId(fn.getId());
                childPfn.setNodeType(fn.getElementType().getTypeName());
                childPfn.setNodeName(fn.getName() == null ? "" : fn.getName());
                outline.addNodeInsts(childPfn);
            }

            pfn.addSucceedingFlowNodes(childPfn);

            populateFlowNodeInsts(outline, fn);
        }

    }

    protected ProcessInstance getProcessInstanceByProcInstId(String processInstanceId) {
        ProcessInstance procInst = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId)
                .singleResult();
        return procInst;
    }

    public ProcessDefinition deployProcessDefinition(ProcDefInfoDto procDefDto) {
        try {
            return doDeployProcessDefinition(procDefDto);
        } catch (Exception e) {
            log.error("errors while deploy process definition", e);
            throw new BpmnCustomizationException(e.getMessage());
        }
    }

    public ProcessInstance retrieveProcessInstance(String processInstanceId) {
        if (StringUtils.isBlank(processInstanceId)) {
            throw new IllegalArgumentException("Process instance ID is blank.");
        }

        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId);

        ProcessInstance procInst = query.singleResult();

        if (procInst == null) {
            throw new WecubeCoreException(
                    String.format("Such process instance with id [%s] does not exist.", processInstanceId));
        }

        return procInst;
    }

    public ProcessDefinition retrieveProcessDefinition(String processDefinitionId) {
        if (StringUtils.isBlank(processDefinitionId)) {
            throw new IllegalArgumentException("Process definition ID is blank.");
        }
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId);
        ProcessDefinition procDef = query.singleResult();
        if (procDef == null) {
            log.warn("such process definition did not exist,processDefinitionId={}", processDefinitionId);
        }
        return procDef;
    }

    public ProcessInstance startProcessInstance(String processDefinitionId, String processInstanceKey) {
        if (StringUtils.isBlank(processDefinitionId)) {
            throw new IllegalArgumentException("Process definition ID is blank.");
        }

        if (log.isDebugEnabled()) {
            log.debug("try to start process instance with id {} and key {}", processDefinitionId, processInstanceKey);
        }

        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId);
        ProcessDefinition procDef = query.singleResult();
        if (procDef == null) {
            log.error("such process definition did not exist,processDefinitionId={}", processDefinitionId);
            throw new WecubeCoreException(
                    String.format("Such proccess definition [%s] does not exist.", processDefinitionId));
        }

        ProcessInstance instance = runtimeService.startProcessInstanceById(processDefinitionId, processInstanceKey);

        if (instance == null) {
            log.error("Failed to create process instance with id {} and key {}", processDefinitionId,
                    processInstanceKey);
            throw new WecubeCoreException(String.format("Failed to create process instance with id %s and key %s.",
                    processDefinitionId, processInstanceKey));
        }

        return instance;
    }

    protected ProcessDefinition doDeployProcessDefinition(ProcDefInfoDto procDefDto) {
        String fileName = procDefDto.getProcDefName() + BPMN_SUFFIX;
        BpmnParseAttachment bpmnParseAttachment = buildBpmnParseAttachment(procDefDto.getTaskNodeInfos());

        BpmnProcessModelCustomizer customizer = new BpmnProcessModelCustomizer(fileName, procDefDto.getProcDefData(),
                encoding);
        customizer.setBpmnParseAttachment(bpmnParseAttachment);
        BpmnModelInstance procModelInstance = customizer.build();

        DeploymentWithDefinitions deployment = repositoryService.createDeployment()
                .addModelInstance(fileName, procModelInstance).deployWithResult();
        List<ProcessDefinition> processDefs = deployment.getDeployedProcessDefinitions();

        if (processDefs == null || processDefs.isEmpty()) {
            log.error("abnormally to parse process definition,request={}", procDefDto);
            throw new WecubeCoreException("process deploying failed");
        }

        ProcessDefinition processDef = processDefs.get(0);

        return processDef;
    }

    public ProcDefOutline getProcDefOutline(ProcessDefinition procDef) {
        ProcDefOutline pdo = new ProcDefOutline();
        pdo.setId(procDef.getId());
        pdo.setProcDefKey(procDef.getKey());
        pdo.setProcDefName(procDef.getName());

        BpmnModelInstance bpmnModel = repositoryService.getBpmnModelInstance(procDef.getId());

        Collection<org.camunda.bpm.model.bpmn.instance.Process> processes = bpmnModel
                .getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Process.class);

        org.camunda.bpm.model.bpmn.instance.Process process = processes.iterator().next();

        Collection<StartEvent> startEvents = process.getChildElementsByType(StartEvent.class);

        StartEvent startEvent = startEvents.iterator().next();

        populateFlowNodes(pdo, startEvent);

        return pdo;
    }

    protected void populateFlowNodes(ProcDefOutline outline, FlowNode flowNode) {
        ProcFlowNode pfn = outline.findFlowNode(flowNode.getId());
        if (pfn == null) {
            pfn = buildProcFlowNode(flowNode);
            outline.addFlowNodes(pfn);
        }

        Collection<FlowNode> succeedingFlowNodes = flowNode.getSucceedingNodes().list();

        for (FlowNode fn : succeedingFlowNodes) {
            ProcFlowNode childPfn = outline.findFlowNode(fn.getId());
            if (childPfn == null) {
                childPfn = buildProcFlowNode(fn);
                outline.addFlowNodes(childPfn);
            }

            pfn.addSucceedingFlowNodes(childPfn);

            populateFlowNodes(outline, fn);
        }

    }

    protected ProcessDefinition getProcessDefinitionByProcId(String processDefinitionId) {
        ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();
        return procDef;
    }

    protected ProcFlowNode buildProcFlowNode(FlowNode fn) {
        ProcFlowNode pfn = new ProcFlowNode();
        pfn.setId(fn.getId());
        pfn.setNodeName(fn.getName() == null ? "" : fn.getName());
        pfn.setNodeType(fn.getElementType().getTypeName());

        return pfn;
    }

    private BpmnParseAttachment buildBpmnParseAttachment(List<TaskNodeDefInfoDto> taskNodeInfoDtos) {
        BpmnParseAttachment bpmnParseAttachment = new BpmnParseAttachment();

        for (TaskNodeDefInfoDto dto : taskNodeInfoDtos) {
            SubProcessAdditionalInfo info = new SubProcessAdditionalInfo();
            info.setSubProcessNodeId(dto.getNodeId());
            info.setSubProcessNodeName(dto.getNodeName());
            info.setTimeoutExpression(convertIsoTimeFormat(dto.getTimeoutExpression()));

            bpmnParseAttachment.addSubProcessAddtionalInfo(info);
        }

        return bpmnParseAttachment;
    }

    private String convertIsoTimeFormat(String timeoutExpression) {
        if (StringUtils.isBlank(timeoutExpression)) {
            return timeoutExpression;
        }

        return "PT" + timeoutExpression.trim() + "M";
    }
}
