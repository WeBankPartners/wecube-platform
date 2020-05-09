package com.webank.wecube.platform.workflow.parse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.GatewayDirection;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.builder.EndEventBuilder;
import org.camunda.bpm.model.bpmn.builder.IntermediateCatchEventBuilder;
import org.camunda.bpm.model.bpmn.builder.StartEventBuilder;
import org.camunda.bpm.model.bpmn.instance.ConditionExpression;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.ErrorEventDefinition;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.Signal;
import org.camunda.bpm.model.bpmn.instance.SignalEventDefinition;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

/**
 * 
 * @author gavin
 *
 */
public class BpmnProcessModelCustomizer {
    private static final Logger log = LoggerFactory.getLogger(BpmnProcessModelCustomizer.class);
    public static final String NS_BPMN = "http://www.omg.org/spec/BPMN/20100524/MODEL";

    public static final String DEFAULT_ERROR_CODE = "1";

    public static final String FORMAL_EXPR_TYPE = "bpmn:tFormalExpression";

    private String resourceName;

    private String originalProcessXml;

    private String encoding;

    private BpmnModelInstance rootBpmnModelInstance;

    private ReentrantLock reentrantBuildLock = new ReentrantLock();

    private BpmnParseAttachment bpmnParseAttachment;

    public BpmnProcessModelCustomizer(String resourceName, String originalProcessXml, String encoding) {
        super();
        if (StringUtils.isBlank(resourceName)) {
            throw new BpmnCustomizationException("resource name must not be null");
        }

        if (StringUtils.isBlank(originalProcessXml)) {
            throw new BpmnCustomizationException("process XML must provide");
        }
        this.resourceName = resourceName;
        this.originalProcessXml = originalProcessXml;

        if (StringUtils.isBlank(encoding)) {
            this.encoding = Charset.defaultCharset().name();
        } else {
            this.encoding = encoding;
        }
    }

    public String buildAsXml() {
        if (rootBpmnModelInstance == null) {
            performBuild();
        }

        if (rootBpmnModelInstance != null) {
            return Bpmn.convertToString(rootBpmnModelInstance);
        } else {
            throw new BpmnCustomizationException("failed to build BPMN model instance");
        }
    }

    public BpmnModelInstance build() {
        if (rootBpmnModelInstance == null) {
            performBuild();
        }

        if (rootBpmnModelInstance != null) {
            return rootBpmnModelInstance;
        } else {
            throw new BpmnCustomizationException("failed to build BPMN model instance");
        }
    }

    public final void performBuild() {
        reentrantBuildLock.lock();
        try {
            BpmnModelInstance modelInstance = internalPerformBuild();
            this.rootBpmnModelInstance = modelInstance;
        } finally {
            reentrantBuildLock.unlock();
        }
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getOriginalProcessXml() {
        return originalProcessXml;
    }

    protected BpmnModelInstance internalPerformBuild() {
        BpmnModelInstance procModelInstance = readModelFromStream();

        if (procModelInstance == null) {
            log.error("failed to read model from stream");
            throw new BpmnCustomizationException("failed to read model from stream");
        }

        enhanceSubProcesses(procModelInstance);
        enhanceServiceTasks(procModelInstance);
        enhanceIntermediateCatchEvents(procModelInstance);
        enhanceEndEvents(procModelInstance);
        enhanceSequenceFlows(procModelInstance);

        validateSignalEventDefinitions(procModelInstance);
        validateProcess(procModelInstance);

        return procModelInstance;
    }

    protected BpmnModelInstance readModelFromStream() {
        InputStream is = null;
        BpmnModelInstance procModelInstance = null;
        try {
            is = new ByteArrayInputStream(originalProcessXml.getBytes(encoding));
            procModelInstance = Bpmn.readModelFromStream(is);
        } catch (UnsupportedEncodingException e1) {
            log.error("errors while reading model", e1);
            procModelInstance = null;
            throw new BpmnCustomizationException("failed to read original process content");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error("errors while closing", e);
                }
            }
        }

        return procModelInstance;
    }

    protected void enhanceProcess(BpmnModelInstance procModelInstance) {
        Collection<org.camunda.bpm.model.bpmn.instance.Process> processes = procModelInstance
                .getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Process.class);
        if (processes.size() != 1) {
            log.error("only one process must provide, size={}", processes.size());
            throw new BpmnCustomizationException("only one process must provide");
        }

        org.camunda.bpm.model.bpmn.instance.Process process = processes.iterator().next();

        if (StringUtils.isBlank(process.getId())) {
            throw new BpmnCustomizationException("process ID must provide");
        }

        String procName = process.getName();
        if (StringUtils.isBlank(procName)) {
            throw new BpmnCustomizationException("process name must provide");
        }
    }

    protected void enhanceSubProcesses(BpmnModelInstance procModelInstance) {
        Collection<SubProcess> subProcesses = procModelInstance.getModelElementsByType(SubProcess.class);

        for (SubProcess subProc : subProcesses) {
            log.info("subprocess {} {}", subProc.getId(), subProc.getName());
            Collection<StartEvent> internalStartEvents = subProc.getChildElementsByType(StartEvent.class);
            if (!internalStartEvents.isEmpty()) {
                log.info("subprocess {} {} already have child nodes and no need to supplement any nodes",
                        subProc.getId(), subProc.getName());

                continue;
            }

            supplementSubProcess(subProc);

        }
    }

    protected void validateLeafNode(FlowNode dstFlowNode) {
        if (dstFlowNode.getOutgoing().isEmpty()) {
            if ("endEvent".equals(dstFlowNode.getElementType().getTypeName())) {
                log.info("end event,id={}", dstFlowNode.getId());
            } else {
                log.error("the leaf node must be end event,id={}", dstFlowNode.getId());
                throw new BpmnCustomizationException("the leaf node must be end event");
            }
        }
    }

    protected void enhanceSequenceFlow(SequenceFlow sf) {
        FlowNode srcFlowNode = sf.getSource();

     // type="bpmn:tFormalExpression"
        if (sf.getConditionExpression() == null && "exclusiveGateway".equals(srcFlowNode.getElementType().getTypeName())
                && srcFlowNode.getOutgoing().size() >= 2) {
            String sfName = sf.getName();
            if (StringUtils.isBlank(sfName)) {
                log.error("the name of squence flow {} is blank.", sf.getId());
                throw new BpmnCustomizationException("the name of sequence flow cannot be blank.");
            }

            List<SubProcess> preSubProcesses = srcFlowNode.getPreviousNodes().filterByType(SubProcess.class).list();
            if (preSubProcesses.size() == 1) {
                log.info("to add condition,sequenceFlowId={}", sf.getId());
                SubProcess preSubProcess = preSubProcesses.get(0);
                ConditionExpression okCon = sf.getModelInstance().newInstance(ConditionExpression.class);
                okCon.setType(FORMAL_EXPR_TYPE);
                okCon.setTextContent(
                        String.format("${ subProcRetCode_%s == '%s' }", preSubProcess.getId(), sfName.trim()));
                sf.builder().condition(okCon).done();
            }

            
        }
    }

    protected void enhanceSequenceFlows(BpmnModelInstance procModelInstance) {
        Collection<SequenceFlow> sequenceFlows = procModelInstance.getModelElementsByType(SequenceFlow.class);
        for (SequenceFlow sf : sequenceFlows) {

            FlowNode srcFlowNode = sf.getSource();
            FlowNode dstFlowNode = sf.getTarget();

            log.info("validate sequence flow, id={},name={},srcType={},srcId={},srcOut={},dstId={},dstOut={} ",
                    sf.getId(), sf.getName(), srcFlowNode.getElementType().getTypeName(), srcFlowNode.getId(),
                    srcFlowNode.getOutgoing().size(), dstFlowNode.getId(), dstFlowNode.getOutgoing().size());

            validateLeafNode(dstFlowNode);

            enhanceSequenceFlow(sf);

        }
    }

    protected void enhanceEndEvents(BpmnModelInstance procModelInstance) {
        Collection<EndEvent> endEvents = procModelInstance.getModelElementsByType(EndEvent.class);
        for (EndEvent endEvent : endEvents) {
            enhanceEndEvent(endEvent);
        }
    }

    protected void enhanceEndEvent(EndEvent endEvent) {
        log.info("validate end event,id={}", endEvent.getId());

        Collection<EventDefinition> eventDefinitions = endEvent.getEventDefinitions();
        List<ErrorEventDefinition> eedsToReplace = new ArrayList<ErrorEventDefinition>();
        for (EventDefinition ed : eventDefinitions) {
            if (ed instanceof ErrorEventDefinition) {
                log.info("error end event definition");
                ErrorEventDefinition eed = (ErrorEventDefinition) ed;

                if (eed.getError() != null) {
                    if (StringUtils.isBlank(eed.getError().getErrorCode())) {
                        log.info("error code is null,errorId={}", eed.getError().getId());
                        eed.getError().setErrorCode(DEFAULT_ERROR_CODE);
                    }
                } else {
                    log.info("does not have error reference, eventId={}", endEvent.getId());
                    eedsToReplace.add(eed);
                }

            }
        }

        for (ErrorEventDefinition eed : eedsToReplace) {
            endEvent.builder().error(DEFAULT_ERROR_CODE).done();

            endEvent.removeChildElement(eed);
        }
    }

    protected void validateSignalEventDefinitions(BpmnModelInstance procModelInstance) {
        Collection<SignalEventDefinition> signalEventDefinitions = procModelInstance
                .getModelElementsByType(SignalEventDefinition.class);
        for (SignalEventDefinition sed : signalEventDefinitions) {
            log.info("validate signal event definition, id={},signalRef={}", sed.getId(),
                    sed.getAttributeValueNs(NS_BPMN, "signalRef"));

            Signal sig = sed.getSignal();
            if (sig == null) {
                log.error("invalid signal defined, id={},signalRef={}", sed.getId(),
                        sed.getAttributeValueNs(NS_BPMN, "signalRef"));
                throw new BpmnCustomizationException("invalid signal definition");
            } else {
                log.info("signal id={}, name={}", sig.getId(), sig.getName());
            }
        }
    }

    protected void enhanceIntermediateCatchEvents(BpmnModelInstance procModelInstance) {
        Collection<IntermediateCatchEvent> ices = procModelInstance
                .getModelElementsByType(IntermediateCatchEvent.class);
        for (IntermediateCatchEvent ice : ices) {
            log.info("validate intermediate catch event,{} {}", ice.getId(), ice.getName());
            Collection<EventDefinition> events = ice.getEventDefinitions();

            List<EventDefinition> eventsToReplace = new ArrayList<EventDefinition>();
            for (EventDefinition e : events) {
                log.info("event definition,{}, {}", e.getId(), e.getElementType().getTypeName());
                if ("signalEventDefinition".equals(e.getElementType().getTypeName())
                        && StringUtils.isBlank(e.getAttributeValueNs(NS_BPMN, "signalRef"))) {
                    log.info("invalid event definition");
                    eventsToReplace.add(e);
                }
            }

            for (EventDefinition e : eventsToReplace) {

                if ("signalEventDefinition".equals(e.getElementType().getTypeName())
                        && StringUtils.isBlank(e.getAttributeValueNs(NS_BPMN, "signalRef"))) {
                    String signalId = "Sig_" + LocalIdGenerator.generateId();
                    IntermediateCatchEventBuilder iceBuilder = ice.builder().signal(signalId);
                    iceBuilder.done();
                    ice.removeChildElement(e);

                    log.info("add signal event definition, signalId={}", signalId);
                }

            }
        }
    }

    protected void enhanceServiceTasks(BpmnModelInstance procModelInstance) {
        Collection<ServiceTask> serviceTasks = procModelInstance.getModelElementsByType(ServiceTask.class);
        for (ServiceTask serviceTask : serviceTasks) {
            log.info("validate service task, {} {}", serviceTask.getId(), serviceTask.getName());
            String delegateExpression = serviceTask.getCamundaDelegateExpression();
            if (StringUtils.isBlank(delegateExpression)) {
                log.info("delegate expression is blank, {} {}", serviceTask.getId(), serviceTask.getName());
                delegateExpression = "${srvBean}";
                serviceTask.setCamundaDelegateExpression(delegateExpression);
            }
        }
    }

    protected void supplementSubProcess(SubProcess subProc) {
        String subProcId = subProc.getId();

        String userTaskId = "exceptSubUT-" + subProcId;
        String srvBeanServiceTaskId = String.format("srvBeanST-%s", subProcId);
        String actRetryExpr = String.format("${ act_%s == 'retry' }", subProcId);
        String actSkipExpr = String.format("${ act_%s == 'skip' }", subProcId);
        String catchEventId = subProcId + "_ice1";
        String signalId = subProcId + "_sig1";
        String retCodeOkExpr = String.format("${retCode_%s != '1'}", catchEventId);
        String retCodeNotOkExpr = String.format("${retCode_%s == '1'}", catchEventId);

        StartEventBuilder b = subProc.builder().embeddedSubProcess().startEvent(subProcId + "_startEvent1")
                .name("St1_" + subProcId);
        EndEventBuilder eb = b.serviceTask(srvBeanServiceTaskId).name("T1_" + subProcId) //
                .eventBasedGateway().name("EGW1_" + subProcId) //
                .intermediateCatchEvent(catchEventId).name("ICE1_" + subProcId) //
                .signal(signalId) //
                .exclusiveGateway().gatewayDirection(GatewayDirection.Diverging) //
                .condition("con1", retCodeOkExpr) //
                .endEvent(subProcId + "_endEvent1").name("End1_" + subProcId) //
                .moveToLastGateway() //
                .condition("con2", retCodeNotOkExpr) //
                .serviceTask("srvFailBeanST-" + subProcId) //
                .name("SRV-FAIL-HANDLER_" + subProcId).camundaDelegateExpression("${srvFailBean}") //
                .userTask(userTaskId).name("EXCEPTION-HANDLER_" + subProcId) //
                .condition("con4", actRetryExpr) //
                .connectTo(srvBeanServiceTaskId) //
                .moveToActivity(userTaskId) //
                .condition("con3", actSkipExpr) //
                .endEvent().name("End2_" + subProcId);

        String subProcessTimeoutExpr = getSubProcessTimeoutExpression(subProc);
        if (StringUtils.isNotBlank(subProcessTimeoutExpr)) {
            AbstractFlowNodeBuilder<?, ?> ab = eb.moveToLastGateway().moveToLastGateway()
                    .intermediateCatchEvent(subProcId + "_time1").timerWithDuration(subProcessTimeoutExpr)
                    .serviceTask("srvTimeOutBeanST-" + subProcId).name("SRV-TIMEOUT-HANDLER_" + subProcId)
                    .camundaDelegateExpression("${srvTimeoutBean}").connectTo(userTaskId);
            ab.done();
        } else {
            eb.done();
        }

    }

    protected String getSubProcessTimeoutExpression(SubProcess subProc) {
        if (bpmnParseAttachment == null) {
            return null;
        }

        if (bpmnParseAttachment.getSubProcessAddtionalInfos() == null
                || bpmnParseAttachment.getSubProcessAddtionalInfos().isEmpty()) {
            return null;
        }

        SubProcessAdditionalInfo subProcessAdditionalInfo = null;

        for (SubProcessAdditionalInfo info : bpmnParseAttachment.getSubProcessAddtionalInfos()) {
            if (subProc.getId().equals(info.getSubProcessNodeId())) {
                subProcessAdditionalInfo = info;
                break;
            }
        }

        if (subProcessAdditionalInfo == null) {
            return null;
        }

        return subProcessAdditionalInfo.getTimeoutExpression();
    }

    protected void validateProcess(BpmnModelInstance procModelInstance) {
        org.camunda.bpm.model.bpmn.instance.Process process = procModelInstance
                .getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Process.class).iterator().next();

        Collection<StartEvent> procStartEvents = process.getChildElementsByType(StartEvent.class);
        Collection<EndEvent> procEndEvents = process.getChildElementsByType(EndEvent.class);
        if (procStartEvents.size() != 1) {
            log.error("only one start event must provide for {} {}", process.getId(), process.getName());
            throw new BpmnCustomizationException("only one start event must provide");
        }

        if (procEndEvents.size() < 1) {
            log.error("at least one end event must provide for {} {}", process.getId(), process.getName());
            throw new BpmnCustomizationException("at least one end event must provide");
        }
    }

    public BpmnParseAttachment getBpmnParseAttachment() {
        return bpmnParseAttachment;
    }

    public void setBpmnParseAttachment(BpmnParseAttachment bpmnParseAttachment) {
        this.bpmnParseAttachment = bpmnParseAttachment;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public void setOriginalProcessXml(String originalProcessXml) {
        this.originalProcessXml = originalProcessXml;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
