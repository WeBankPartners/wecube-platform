package com.webank.wecube.platform.workflow.parse;

import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.Condition;
import org.camunda.bpm.engine.impl.bpmn.behavior.CancelEndEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ClassDelegateActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.CompensationEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ErrorEndEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.IntermediateThrowNoneEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.NoneEndEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ServiceTaskDelegateExpressionActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ServiceTaskExpressionActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.TerminateEndEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ThrowEscalationEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ThrowSignalEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.bpmn.parser.CompensateEventDefinition;
import org.camunda.bpm.engine.impl.bpmn.parser.Escalation;
import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.bpmn.parser.SignalDefinition;
import org.camunda.bpm.engine.impl.core.model.CallableElement;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.el.UelExpressionCondition;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ActivityStartBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.impl.scripting.ScriptCondition;
import org.camunda.bpm.engine.impl.util.ScriptUtil;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gavinli
 */
class CustomBpmnParse extends BpmnParse {

    private static final String PREFIX_ERROR_MSG_VARIABLE_NAME = "'resultVariableName' not supported for ";

    private static final Logger logger = LoggerFactory.getLogger(CustomBpmnParse.class);

    private static final String DEFAULT_SIGNAL_ID = "Signal_default_id";

    private static final String DEFAULT_SIGNAL_NAME = "Signal_default_name";

    private static final String DEFAULT_ERROR_ID = "Error_def_id";

    private static final String DEFAULT_ERROR_CODE = "1";

    public CustomBpmnParse(BpmnParser parser) {
        super(parser);
        logger.debug("BpmnParse: {} created", CustomBpmnParse.class.getName());
    }

    @Override
    protected void parseSignals() {
        super.parseSignals();

        Expression signalExpression = expressionManager.createExpression(DEFAULT_SIGNAL_NAME);
        SignalDefinition signal = new SignalDefinition();
        signal.setId(this.targetNamespace + ":" + DEFAULT_SIGNAL_ID);
        signal.setExpression(signalExpression);

        signals.put(signal.getId(), signal);
        logger.debug("added default signal,id={},name={}", signal.getId(), DEFAULT_SIGNAL_NAME);
    }

    @Override
    protected EventSubscriptionDeclaration parseSignalEventDefinition(Element signalEventDefinitionElement,
            boolean isThrowing) {
        logger.debug("parse signal event definition");

        String signalRef = signalEventDefinitionElement.attribute("signalRef");
        if (signalRef == null) {
            logger.debug("add default signal {}:{}", DEFAULT_SIGNAL_ID, DEFAULT_SIGNAL_NAME);
            signalRef = DEFAULT_SIGNAL_ID;
        }

        SignalDefinition signalDefinition = signals.get(resolveName(signalRef));
        if (signalDefinition == null) {
            addError("Could not find signal with id '" + signalRef + "'", signalEventDefinitionElement);
        }

        EventSubscriptionDeclaration signalEventDefinition;
        if (isThrowing) {
            CallableElement payload = new CallableElement();
            parseInputParameter(signalEventDefinitionElement, payload);
            signalEventDefinition = new EventSubscriptionDeclaration(signalDefinition != null ? signalDefinition.getExpression() : null, EventType.SIGNAL,
                    payload);
        } else {
            signalEventDefinition = new EventSubscriptionDeclaration(signalDefinition != null ? signalDefinition.getExpression() : null,
                    EventType.SIGNAL);
        }

        boolean throwingAsync = TRUE
                .equals(signalEventDefinitionElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, "async", "false"));
        signalEventDefinition.setAsync(throwingAsync);

        return signalEventDefinition;

    }

    public ActivityImpl parseServiceTaskLike(String elementName, Element serviceTaskElement, ScopeImpl scope) {
        logger.debug("parse service task like element,elementName={}", elementName);

        ActivityImpl activity = createActivityOnScope(serviceTaskElement, scope);

        String type = serviceTaskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, TYPE);
        String className = serviceTaskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, PROPERTYNAME_CLASS);
        String expression = serviceTaskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, PROPERTYNAME_EXPRESSION);
        String delegateExpression = serviceTaskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS,
                PROPERTYNAME_DELEGATE_EXPRESSION);
        String resultVariableName = parseResultVariable(serviceTaskElement);

        parseAsynchronousContinuationForActivity(serviceTaskElement, activity);

        parseServiceTaskLikeAttributes(elementName, serviceTaskElement, scope, activity, type, className, expression,
                delegateExpression, resultVariableName);

        parseExecutionListenersOnScope(serviceTaskElement, activity);

        for (BpmnParseListener parseListener : parseListeners) {
            parseListener.parseServiceTask(serviceTaskElement, scope, activity);
        }

        if (activity.getActivityBehavior() == null) {
            String defaultDelegateExpression = "${taskDispatcher}";
            if (resultVariableName != null) {
                addError(PREFIX_ERROR_MSG_VARIABLE_NAME + elementName + " elements using 'delegateExpression'",
                        serviceTaskElement);
            }

            activity.setActivityBehavior(new ServiceTaskDelegateExpressionActivityBehavior(
                    expressionManager.createExpression(defaultDelegateExpression),
                    parseFieldDeclarations(serviceTaskElement)));
        }

        // activity behavior could be set by a listener (e.g. connector); thus,
        // check is after listener invocation
        if (activity.getActivityBehavior() == null) {
            addError("One of the attributes 'class', 'delegateExpression', 'type', or 'expression' is mandatory on "
                    + elementName + ".", serviceTaskElement);
        }

        return activity;
    }

    protected void parseServiceTaskLikeAttributes(String elementName, Element serviceTaskElement, ScopeImpl scope,
            ActivityImpl activity, String type, String className, String expression, String delegateExpression,
            String resultVariableName) {
        if (type != null) {
            parseServiceTaskLikeAttributesType(elementName, serviceTaskElement, scope, activity, type);
        } else if (className != null && className.trim().length() > 0) {
            parseServiceTaskLikeAttributesClassName(elementName, serviceTaskElement, scope, activity, className,
                    resultVariableName);
        } else if (delegateExpression != null) {
            parseServiceTaskLikeAttributesDelegateExpression(elementName, serviceTaskElement, scope, activity,
                    delegateExpression, resultVariableName);
        } else if (expression != null && expression.trim().length() > 0) {
            parseServiceTaskLikeAttributesExpression(elementName, serviceTaskElement, scope, activity, expression,
                    resultVariableName);
        }
    }

    protected void parseServiceTaskLikeAttributesExpression(String elementName, Element serviceTaskElement,
            ScopeImpl scope, ActivityImpl activity, String expression, String resultVariableName) {
        activity.setActivityBehavior(new ServiceTaskExpressionActivityBehavior(
                expressionManager.createExpression(expression), resultVariableName));
    }

    protected void parseServiceTaskLikeAttributesDelegateExpression(String elementName, Element serviceTaskElement,
            ScopeImpl scope, ActivityImpl activity, String delegateExpression, String resultVariableName) {
        if (resultVariableName != null) {
            addError(PREFIX_ERROR_MSG_VARIABLE_NAME + elementName + " elements using 'delegateExpression'",
                    serviceTaskElement);
        }
        activity.setActivityBehavior(new ServiceTaskDelegateExpressionActivityBehavior(
                expressionManager.createExpression(delegateExpression), parseFieldDeclarations(serviceTaskElement)));
    }

    protected void parseServiceTaskLikeAttributesClassName(String elementName, Element serviceTaskElement,
            ScopeImpl scope, ActivityImpl activity, String className, String resultVariableName) {
        if (resultVariableName != null) {
            addError(PREFIX_ERROR_MSG_VARIABLE_NAME + elementName + " elements using 'class'", serviceTaskElement);
        }
        activity.setActivityBehavior(
                new ClassDelegateActivityBehavior(className, parseFieldDeclarations(serviceTaskElement)));
    }

    protected void parseServiceTaskLikeAttributesType(String elementName, Element serviceTaskElement, ScopeImpl scope,
            ActivityImpl activity, String type) {
        if (type.equalsIgnoreCase("mail")) {
            parseEmailServiceTask(activity, serviceTaskElement, parseFieldDeclarations(serviceTaskElement));
        } else if (type.equalsIgnoreCase("shell")) {
            parseShellServiceTask(activity, serviceTaskElement, parseFieldDeclarations(serviceTaskElement));
        } else if (type.equalsIgnoreCase("external")) {
            parseExternalServiceTask(activity, serviceTaskElement);
        } else {
            addError("Invalid usage of type attribute on " + elementName + ": '" + type + "'", serviceTaskElement);
        }
    }

    public void parseErrors() {
        logger.debug("parse errors for process");
        super.parseErrors();

        org.camunda.bpm.engine.impl.bpmn.parser.Error defaultError = buildDefaultError();
        errors.put(defaultError.getId(), defaultError);
        logger.debug("added default error to errors, id={}", defaultError.getId());

    }

    public void parseEndEvents(Element parentElement, ScopeImpl scope) {
        for (Element endEventElement : parentElement.elements("endEvent")) {
            parseEndEvent(parentElement, scope, endEventElement);
        }
    }

    protected void parseEndEvent(Element parentElement, ScopeImpl scope, Element endEventElement) {
        ActivityImpl activity = createActivityOnScope(endEventElement, scope);

        Element errorEventDefinition = endEventElement.element(ERROR_EVENT_DEFINITION);
        Element cancelEventDefinition = endEventElement.element(CANCEL_EVENT_DEFINITION);
        Element terminateEventDefinition = endEventElement.element("terminateEventDefinition");
        Element messageEventDefinitionElement = endEventElement.element(MESSAGE_EVENT_DEFINITION);
        Element signalEventDefinition = endEventElement.element(SIGNAL_EVENT_DEFINITION);
        Element compensateEventDefinitionElement = endEventElement.element(COMPENSATE_EVENT_DEFINITION);
        Element escalationEventDefinition = endEventElement.element(ESCALATION_EVENT_DEFINITION);

        if (errorEventDefinition != null) { // error end event
            parseErrorEventDefinition(parentElement, scope, endEventElement, activity, errorEventDefinition);
        } else if (cancelEventDefinition != null) {
            parseCancelEventDefinition(parentElement, scope, endEventElement, activity, cancelEventDefinition);
        } else if (terminateEventDefinition != null) {
            parseTerminateEventDefinition(parentElement, scope, endEventElement, activity, terminateEventDefinition);
        } else if (messageEventDefinitionElement != null) {
            parseMessageEventDefinitionElement(parentElement, scope, endEventElement, activity,
                    messageEventDefinitionElement);
        } else if (signalEventDefinition != null) {
            parseSignalEventDefinition(parentElement, scope, endEventElement, activity, signalEventDefinition);
        } else if (compensateEventDefinitionElement != null) {
            parseCompensateEventDefinitionElement(parentElement, scope, endEventElement, activity,
                    compensateEventDefinitionElement);
        } else if (escalationEventDefinition != null) {
            parseEscalationEventDefinition(parentElement, scope, endEventElement, activity, escalationEventDefinition);
        } else { // default: none end event
            activity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.END_EVENT_NONE);
            activity.setActivityBehavior(new NoneEndEventActivityBehavior());
        }

        if (activity != null) {
            parseActivityInputOutput(endEventElement, activity);
        }

        parseAsynchronousContinuationForActivity(endEventElement, activity);

        parseExecutionListenersOnScope(endEventElement, activity);

        for (BpmnParseListener parseListener : parseListeners) {
            parseListener.parseEndEvent(endEventElement, scope, activity);
        }
    }

    protected void parseEscalationEventDefinition(Element parentElement, ScopeImpl scope, Element endEventElement,
            ActivityImpl activity, Element escalationEventDefinition) {
        activity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.END_EVENT_ESCALATION);

        Escalation escalation = findEscalationForEscalationEventDefinition(escalationEventDefinition);
        if (escalation != null && escalation.getEscalationCode() == null) {
            addError("escalation end event must have an 'escalationCode'", escalationEventDefinition);
        }
        activity.setActivityBehavior(new ThrowEscalationEventActivityBehavior(escalation));
    }

    protected void parseCompensateEventDefinitionElement(Element parentElement, ScopeImpl scope,
            Element endEventElement, ActivityImpl activity, Element compensateEventDefinitionElement) {
        activity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.END_EVENT_COMPENSATION);
        CompensateEventDefinition compensateEventDefinition = parseThrowCompensateEventDefinition(
                compensateEventDefinitionElement, scope);
        activity.setActivityBehavior(new CompensationEventActivityBehavior(compensateEventDefinition));
        activity.setProperty(PROPERTYNAME_THROWS_COMPENSATION, true);
        activity.setScope(true);
    }

    protected void parseSignalEventDefinition(Element parentElement, ScopeImpl scope, Element endEventElement,
            ActivityImpl activity, Element signalEventDefinition) {
        activity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.END_EVENT_SIGNAL);
        EventSubscriptionDeclaration signalDefinition = parseSignalEventDefinition(signalEventDefinition, true);
        activity.setActivityBehavior(new ThrowSignalEventActivityBehavior(signalDefinition));
    }

    protected void parseMessageEventDefinitionElement(Element parentElement, ScopeImpl scope, Element endEventElement,
            ActivityImpl activity, Element messageEventDefinitionElement) {
        if (isServiceTaskLike(messageEventDefinitionElement)) {

            // CAM-436 same behaviour as service task
            ActivityImpl act = parseServiceTaskLike(ActivityTypes.END_EVENT_MESSAGE, messageEventDefinitionElement,
                    scope);
            activity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.END_EVENT_MESSAGE);
            activity.setActivityBehavior(act.getActivityBehavior());
            scope.getActivities().remove(act);
        } else {
            // default to non behavior if no service task
            // properties have been specified
            activity.setActivityBehavior(new IntermediateThrowNoneEventActivityBehavior());
        }
    }

    protected void parseTerminateEventDefinition(Element parentElement, ScopeImpl scope, Element endEventElement,
            ActivityImpl activity, Element terminateEventDefinition) {
        activity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.END_EVENT_TERMINATE);
        activity.setActivityBehavior(new TerminateEndEventActivityBehavior());
        activity.setActivityStartBehavior(ActivityStartBehavior.INTERRUPT_FLOW_SCOPE);
    }

    protected void parseCancelEventDefinition(Element parentElement, ScopeImpl scope, Element endEventElement,
            ActivityImpl activity, Element cancelEventDefinition) {
        if (scope.getProperty(BpmnProperties.TYPE.getName()) == null
                || !scope.getProperty(BpmnProperties.TYPE.getName()).equals("transaction")) {
            addError("end event with cancelEventDefinition only supported inside transaction subprocess",
                    cancelEventDefinition);
        } else {
            activity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.END_EVENT_CANCEL);
            activity.setActivityBehavior(new CancelEndEventActivityBehavior());
            activity.setActivityStartBehavior(ActivityStartBehavior.INTERRUPT_FLOW_SCOPE);
            activity.setProperty(PROPERTYNAME_THROWS_COMPENSATION, true);
            activity.setScope(true);
        }
    }

    protected void parseErrorEventDefinition(Element parentElement, ScopeImpl scope, Element endEventElement,
            ActivityImpl activity, Element errorEventDefinition) {
        String errorRef = errorEventDefinition.attribute("errorRef");

        if (errorRef == null || "".equals(errorRef)) {
            errorRef = DEFAULT_ERROR_ID;
        }

        org.camunda.bpm.engine.impl.bpmn.parser.Error error = errors.get(errorRef);
        if (error != null && (error.getErrorCode() == null || "".equals(error.getErrorCode()))) {
            error.setErrorCode(DEFAULT_ERROR_CODE);
        }
        if (error != null && (error.getErrorCode() == null || "".equals(error.getErrorCode()))) {
            addError(
                    "'errorCode' is mandatory on errors referenced by throwing error event definitions, but the error '"
                            + error.getId() + "' does not define one.",
                    errorEventDefinition);
        }
        activity.getProperties().set(BpmnProperties.TYPE, ActivityTypes.END_EVENT_ERROR);
        if (error != null) {
            activity.setActivityBehavior(new ErrorEndEventActivityBehavior(error.getErrorCode()));
        } else {
            activity.setActivityBehavior(new ErrorEndEventActivityBehavior(errorRef));
        }

    }

    protected org.camunda.bpm.engine.impl.bpmn.parser.Error buildDefaultError() {
        org.camunda.bpm.engine.impl.bpmn.parser.Error error = new org.camunda.bpm.engine.impl.bpmn.parser.Error();
        error.setId(DEFAULT_ERROR_ID);
        error.setErrorCode(DEFAULT_ERROR_CODE);

        return error;

    }

    public void parseSequenceFlowConditionExpression(Element seqFlowElement, TransitionImpl seqFlow) {
        logger.debug("parse sequence flow condition expression,id={}", seqFlow.getId());

        Element conditionExprElement = seqFlowElement.element(CONDITION_EXPRESSION);
        if (conditionExprElement != null) {
            Condition condition = parseConditionExpression(conditionExprElement);
            seqFlow.setProperty(PROPERTYNAME_CONDITION_TEXT,
                    translateConditionExpressionElementText(conditionExprElement));
            seqFlow.setProperty(PROPERTYNAME_CONDITION, condition);
        } else {
            tryDeduceConditionExpression(seqFlowElement, seqFlow);
        }
    }

    protected void tryDeduceConditionExpression(Element seqFlowElement, TransitionImpl seqFlow) {
        logger.debug("try deduce condition expression,id={}", seqFlow.getId());
    }

    protected Condition parseConditionExpression(Element conditionExprElement) {
        String expression = translateConditionExpressionElementText(conditionExprElement);
        String type = conditionExprElement.attributeNS(XSI_NS, TYPE);
        String language = conditionExprElement.attribute(PROPERTYNAME_LANGUAGE);
        String resource = conditionExprElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, PROPERTYNAME_RESOURCE);
        if (type != null) {
            String value = type.contains(":") ? resolveName(type) : BpmnParser.BPMN20_NS + ":" + type;
            if (!value.equals(ATTRIBUTEVALUE_T_FORMAL_EXPRESSION)) {
                addError("Invalid type, only tFormalExpression is currently supported", conditionExprElement);
            }
        }
        Condition condition = null;
        if (language == null) {
            condition = new UelExpressionCondition(expressionManager.createExpression(expression));
        } else {
            try {
                ExecutableScript script = ScriptUtil.getScript(language, expression, resource, expressionManager);
                condition = new ScriptCondition(script);
            } catch (ProcessEngineException e) {
                addError("Unable to process condition expression:" + e.getMessage(), conditionExprElement);
            }
        }
        return condition;
    }

    protected String translateConditionExpressionElementText(Element conditionExprElement) {
        String original = conditionExprElement.getText().trim();
        if ("0".equals(original)) {
            return "${ok}";
        }

        if ("1".equals(original)) {
            return "${!ok}";
        }

        return original;
    }

}
