package com.webank.wecube.platform.workflow.parse;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author gavin
 *
 */
@Component("LocalBpmnParseListener")
public class LocalBpmnParseListener extends AbstractBpmnParseListener {
    private static final Logger log = LoggerFactory.getLogger(LocalBpmnParseListener.class);

    @Autowired
    private ProcessInstanceEndListener processInstanceEndListener;

    @Autowired
    private EndEventListener endEventListener;

    @Autowired
    private SubProcessStartListener subProcessStartListener;

    @Autowired
    private SubProcessEndListener subProcessEndListener;

    @Autowired
    private ProcessInstanceStartListener processInstanceStartListener;
    
    @Autowired
    private ServiceTaskEndListener serviceTaskEndListener;
    
    @Autowired
    private ServiceTaskStartListener serviceTaskStartListener;
    
    @Autowired
    private UserTaskStartListener userTaskStartListener;
    
    @Autowired
    private UserTaskEndListener userTaskEndListener;


    @Override
    public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
        log.info("add listener {} {}", ExecutionListener.EVENTNAME_START, subProcessStartListener.getClass().getSimpleName());
        log.info("add listener {} {}", ExecutionListener.EVENTNAME_END, subProcessEndListener.getClass().getSimpleName());
        activity.addListener(ExecutionListener.EVENTNAME_START, subProcessStartListener);
        activity.addListener(ExecutionListener.EVENTNAME_END, subProcessEndListener);
    }

    @Override
    public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
        log.info("add listener {} {}", ExecutionListener.EVENTNAME_START, processInstanceStartListener.getClass().getSimpleName());
        log.info("add listener {} {}", ExecutionListener.EVENTNAME_END, processInstanceEndListener.getClass().getSimpleName());
        processDefinition.addListener(ExecutionListener.EVENTNAME_START, processInstanceStartListener);
        processDefinition.addListener(ExecutionListener.EVENTNAME_END, processInstanceEndListener);
    }

    @Override
    public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
        log.info("add listener {} {}", ExecutionListener.EVENTNAME_START, serviceTaskStartListener.getClass().getSimpleName());
        log.info("add listener {} {}", ExecutionListener.EVENTNAME_END, serviceTaskEndListener.getClass().getSimpleName());
        activity.addListener(ExecutionListener.EVENTNAME_START, serviceTaskStartListener);
        activity.addListener(ExecutionListener.EVENTNAME_END, serviceTaskEndListener);
    }


    @Override
    public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
        log.info("add listener {} {}", ExecutionListener.EVENTNAME_END, endEventListener.getClass().getSimpleName());
        activity.addListener(ExecutionListener.EVENTNAME_END, endEventListener);
    }

    @Override
    public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
        log.info("add listener {} {}", ExecutionListener.EVENTNAME_START, userTaskStartListener.getClass().getSimpleName());
        log.info("add listener {} {}", ExecutionListener.EVENTNAME_END, userTaskEndListener.getClass().getSimpleName());
        activity.addListener(ExecutionListener.EVENTNAME_START, userTaskStartListener);
        activity.addListener(ExecutionListener.EVENTNAME_END, userTaskEndListener);
    }
}
