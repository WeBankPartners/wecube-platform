package com.webank.wecube.platform.core.service.workflow;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.Bpmn;
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
import com.webank.wecube.platform.workflow.WorkflowConstants;
import com.webank.wecube.platform.workflow.entity.ProcessInstanceStatusEntity;
import com.webank.wecube.platform.workflow.entity.ServiceNodeStatusEntity;
import com.webank.wecube.platform.workflow.model.ProcDefOutline;
import com.webank.wecube.platform.workflow.model.ProcFlowNode;
import com.webank.wecube.platform.workflow.model.ProcFlowNodeInst;
import com.webank.wecube.platform.workflow.model.ProcInstOutline;
import com.webank.wecube.platform.workflow.model.ServiceInvocationEvent;
import com.webank.wecube.platform.workflow.model.TraceStatus;
import com.webank.wecube.platform.workflow.parse.BpmnCustomizationException;
import com.webank.wecube.platform.workflow.parse.BpmnParseAttachment;
import com.webank.wecube.platform.workflow.parse.BpmnProcessModelCustomizer;
import com.webank.wecube.platform.workflow.parse.SubProcessAdditionalInfo;
import com.webank.wecube.platform.workflow.repository.ProcessInstanceStatusRepository;
import com.webank.wecube.platform.workflow.repository.ServiceNodeStatusRepository;

/**
 * 
 * @author gavin
 *
 */
@Service
public class WorkflowEngineService {

	private static final Logger log = LoggerFactory.getLogger(WorkflowEngineService.class);

	public static final int SERVICE_TASK_EXECUTE_SUCC = 0;
	public static final int SERVICE_TASK_EXECUTE_ERR = 1;

	public static final String PROCEED_ACT_RETRY = "retry";
	public static final String PROCEED_ACT_SKIP = "skip";

	public static final String PREFIX_EXCEPT_SUB_USER_TASK = "exceptSubUT-";

	private static final String BPMN_SUFFIX = ".bpmn20.xml";

	private String encoding = "UTF-8";

	@Autowired
	protected RepositoryService repositoryService;

	@Autowired
	protected RuntimeService runtimeService;

	@Autowired
	protected HistoryService historyService;

	@Autowired
	protected ManagementService managementService;

	@Autowired
	protected ProcessInstanceStatusRepository processInstanceStatusRepository;

	@Autowired
	protected ServiceNodeStatusRepository serviceNodeStatusRepository;

	@Autowired
	protected TaskService taskService;

	private List<String> statelessNodeTypes = Arrays.asList("startEvent", "endEvent", "exclusiveGateway",
			"parallelGateway");

	public String getTaskNodeStatus(String procInstanceId, String nodeId) {
		ServiceNodeStatusEntity nodeStatusEntity = serviceNodeStatusRepository
				.findOneByProcInstanceIdAndNodeId(procInstanceId, nodeId);

		if (nodeStatusEntity == null) {
			return null;
		}

		if (nodeStatusEntity.getStatus() == null) {
			return null;
		}

		return nodeStatusEntity.getStatus().name();
	}

	public void proceedProcessInstance(String procInstanceId, String nodeId, String userAction) {
		String instanceId = procInstanceId;
		String taskDefKey = PREFIX_EXCEPT_SUB_USER_TASK + nodeId;

		String act = StringUtils.isBlank(userAction) ? PROCEED_ACT_RETRY : userAction;

		if (!(PROCEED_ACT_RETRY.equals(act) || PROCEED_ACT_SKIP.equals(act))) {
			log.warn("invalid action met for {} {} {}", procInstanceId, nodeId, userAction);
			throw new IllegalArgumentException(String.format("action {%s} is invalid.", userAction));
		}

		Task task = taskService.createTaskQuery().processInstanceId(instanceId).active().taskDefinitionKey(taskDefKey)
				.singleResult();

		if (task == null) {
			log.error("cannot find task with instanceId {} and taskId {}", instanceId, taskDefKey);
			throw new WecubeCoreException("process instance restarting failed");
		} else {

			String actVarName = String.format("%s_%s", WorkflowConstants.VAR_KEY_USER_ACT, nodeId);

			log.info("to complete task {} put {} {}", taskDefKey, WorkflowConstants.VAR_KEY_USER_ACT, act);
			Map<String, Object> variables = new HashMap<String, Object>();
			variables.put(actVarName, act);

			taskService.complete(task.getId(), variables);
		}
	}

	public void handleServiceInvocationResult(ServiceInvocationEvent event) {
		String procInstId = event.getInstanceId();
		String procInstKey = event.getBusinessKey();
		String executionId = event.getExecutionId();

		if (log.isInfoEnabled()) {
			log.info("handling service invocation result,procInstId={},procInstKey={},executionId={},resultCode={}",
					procInstId, procInstKey, executionId, event.getResult());
		}

		String resultCode = event.getResult();

		ProcessInstance instance = null;

		for (int i = 0; i < 20; i++) {
			instance = runtimeService.createProcessInstanceQuery().processInstanceId(procInstId).singleResult();
			if (instance != null) {
				break;
			}

			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				log.debug("meet exception, InterruptedException: " + e.getMessage());
				Thread.currentThread().interrupt();
			}

			log.debug("retried {} times", (i + 1));
		}

		if (instance == null) {
			log.info("Cannot find process instance with such id, procInstKey={}", procInstKey);
			throw new RuntimeException("None process instance found.");
		}

		EventSubscription signalEventSubscription = null;

		for (int i = 0; i < 20; i++) {
			EventSubscriptionQuery eventSubscriptionQuery = runtimeService.createEventSubscriptionQuery()
					.eventType("signal").processInstanceId(instance.getProcessInstanceId());

			if (StringUtils.isNotBlank(executionId)) {
				eventSubscriptionQuery = eventSubscriptionQuery.activityId(executionId);
			}
			List<EventSubscription> signalEventSubscriptions = eventSubscriptionQuery.list();

			if (!signalEventSubscriptions.isEmpty()) {
				signalEventSubscription = signalEventSubscriptions.get(0);
			}

			if (signalEventSubscription != null) {
				break;
			}

			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				log.debug("Interrupt errors", e);
			}
		}

		if (signalEventSubscription == null) {
			log.info("Such subscription have not found for event:{} {} {}", procInstId, procInstKey, executionId);
			return;
		}

		String eventName = signalEventSubscription.getEventName();
		Map<String, Object> boundVariables = new HashMap<String, Object>();

		String varName = String.format("retCode_%s", executionId);
		boundVariables.put(varName, resultCode);
		log.debug("put {}, {}", varName, resultCode);

		try {
			tryEmitSignalEvent(eventName, signalEventSubscription, resultCode, procInstId, procInstKey, boundVariables);
		} catch (Exception e) {
			log.debug("Errors while emitting signal event", e);
		}

	}

	private void tryEmitSignalEvent(String eventName, EventSubscription signalEventSubscription, String resultCode,
			String procInstId, String procInstKey, Map<String, Object> boundVariables) {
		for (int times = 0; times <= 20; times++) {
			log.info(
					"###### {} try delivering {} to execution {}, serviceCode {}, instanceId {}, businessKey {} activityId {}",
					times, eventName, signalEventSubscription.getId(), resultCode, procInstId, procInstKey,
					signalEventSubscription.getActivityId());
			try {
				runtimeService.createSignalEvent(eventName).executionId(signalEventSubscription.getExecutionId())
						.setVariables(boundVariables).send();
				break;
			} catch (Exception e) {
				log.debug("Errors while sending signal " + resultCode);
				log.debug(
						"######Failed {} delivering {} to execution {}, serviceCode {}, instanceId {}, businessKey {} activityId {}",
						times, eventName, signalEventSubscription.getId(), resultCode, procInstId, procInstKey,
						signalEventSubscription.getActivityId());

				try {
					Thread.sleep(300);
				} catch (InterruptedException e1) {
					log.debug("Interrupted exceptions", e1);
				}
			}
		}

	}

	public ProcInstOutline getProcInstOutline(String procInstId) {
		if (procInstId == null) {
			throw new WecubeCoreException("Process instance is null.");
		}

		ProcessInstanceStatusEntity procInstStatusEntity = processInstanceStatusRepository
				.findOneByprocInstanceId(procInstId);

		if (procInstStatusEntity == null) {
			log.warn("cannot find such process instance record with procInstId={}", procInstId);
			throw new WecubeCoreException("Such process instance record does not exist.");
		}

		String processInstanceId = null;
		String processDefinitionId = null;
		if (TraceStatus.Completed.equals(procInstStatusEntity.getStatus())) {
			processInstanceId = procInstStatusEntity.getProcInstanceId();
			processDefinitionId = procInstStatusEntity.getProcDefinitionId();
		} else {
			ProcessInstance existProcInst = getProcessInstanceByProcInstId(procInstId);

			if (existProcInst == null) {
				log.warn("such process instance does not exist,procInstId={}", procInstId);
				throw new WecubeCoreException("Such process instance does not exist.");
			}

			processInstanceId = existProcInst.getId();
			processDefinitionId = existProcInst.getProcessDefinitionId();
		}

		ProcessDefinition procDef = getProcessDefinitionByProcId(processDefinitionId);

		if (procDef == null) {
			log.warn("such process definition does not exist,procDefId={}", processDefinitionId);
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
			String nodeType = n.getNodeType();

			if (!statelessNodeTypes.contains(nodeType)) {
				continue;
			}

			String nodeStatus = null;
			boolean isAllPreviousFlowNodesCompleted = true;

			for (ProcFlowNode child : n.getPreviousFlowNodes()) {
				ProcFlowNodeInst fi = (ProcFlowNodeInst) child;
				if (!TraceStatus.Completed.name().equals(fi.getStatus())) {
					isAllPreviousFlowNodesCompleted = false;
					break;
				}
			}

			if (isAllPreviousFlowNodesCompleted) {
				nodeStatus = TraceStatus.Completed.name();
			} else {
				nodeStatus = TraceStatus.NotStarted.name();
			}

			if (!TraceStatus.Completed.name().equals(nodeStatus)) {
				boolean isOneSucceedingFlowNodeStarted = false;

				for (ProcFlowNode child : n.getSucceedingFlowNodes()) {
					ProcFlowNodeInst fi = (ProcFlowNodeInst) child;
					if (fi.getStatus() != null && !TraceStatus.NotStarted.name().equals(fi.getStatus())) {
						isOneSucceedingFlowNodeStarted = true;
						break;
					}
				}

				if (isOneSucceedingFlowNodeStarted) {
					nodeStatus = TraceStatus.Completed.name();
				}
			}

			if (nodeStatus == null) {
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

			if (nodeStatus != null) {
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
			boolean needPopulateChild = false;
			if (childPfn == null) {
				childPfn = new ProcFlowNodeInst();
				childPfn.setId(fn.getId());
				childPfn.setNodeType(fn.getElementType().getTypeName());
				childPfn.setNodeName(fn.getName() == null ? "" : fn.getName());
				outline.addNodeInsts(childPfn);
				needPopulateChild = true;
			}

			pfn.addSucceedingFlowNodes(childPfn);

			if (needPopulateChild) {
				populateFlowNodeInsts(outline, fn);
			}
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
			log.warn("errors while deploy process definition", e);
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
			log.warn("such process definition did not exist,processDefinitionId={}", processDefinitionId);
			throw new WecubeCoreException(
					String.format("Such proccess definition [%s] does not exist.", processDefinitionId));
		}

		ProcessInstance instance = runtimeService.startProcessInstanceById(processDefinitionId, processInstanceKey);

		if (instance == null) {
			log.warn("Failed to create process instance with id {} and key {}", processDefinitionId,
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
			log.warn("abnormally to parse process definition,request={}", procDefDto);
			throw new WecubeCoreException("process deploying failed");
		}

		ProcessDefinition processDef = processDefs.get(0);

		return processDef;
	}

	public ProcDefOutline readProcDefOutlineFromXmlData(String xmlData) {
		BpmnModelInstance bpmnModel = readModelFromXmlData(xmlData);

		ProcDefOutline pdo = new ProcDefOutline();

		Collection<org.camunda.bpm.model.bpmn.instance.Process> processes = bpmnModel
				.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Process.class);

		org.camunda.bpm.model.bpmn.instance.Process process = processes.iterator().next();

		// pdo.setProcDefKey(process.getKey());
		pdo.setProcDefName(process.getName());

		Collection<StartEvent> startEvents = process.getChildElementsByType(StartEvent.class);

		StartEvent startEvent = startEvents.iterator().next();

		populateFlowNodes(pdo, startEvent);

		return pdo;
	}

	protected BpmnModelInstance readModelFromXmlData(String xmlData) {
		if (StringUtils.isBlank(xmlData)) {
			throw new WecubeCoreException("XML data to parse cannot be blank.");
		}

		InputStream is = null;
		BpmnModelInstance procModelInstance = null;
		try {
			is = new ByteArrayInputStream(xmlData.getBytes(encoding));
			procModelInstance = Bpmn.readModelFromStream(is);
		} catch (UnsupportedEncodingException e1) {
			log.warn("errors while reading model", e1);
			procModelInstance = null;
			throw new WecubeCoreException("failed to read xml process content");
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					log.warn("errors while closing", e);
				}
			}
		}

		if (procModelInstance == null) {
			log.warn("failed to read model instance.");
			throw new WecubeCoreException("Failed to read model instance.");
		}

		return procModelInstance;
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
			boolean needPopulateChild = false;
			if (childPfn == null) {
				childPfn = buildProcFlowNode(fn);
				outline.addFlowNodes(childPfn);
				needPopulateChild = true;
			}

			pfn.addSucceedingFlowNodes(childPfn);

			if (needPopulateChild) {
				populateFlowNodes(outline, fn);
			}
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
