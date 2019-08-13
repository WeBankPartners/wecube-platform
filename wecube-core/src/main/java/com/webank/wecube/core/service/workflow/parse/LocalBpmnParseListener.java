package com.webank.wecube.core.service.workflow.parse;

import javax.annotation.PostConstruct;

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

import com.webank.wecube.core.service.workflow.delegate.ProcessInstanceEndListener;

@Component
public class LocalBpmnParseListener extends AbstractBpmnParseListener {
    private static final Logger log = LoggerFactory.getLogger(LocalBpmnParseListener.class);

    @Autowired
    private ProcessInstanceEndListener processInstanceEndListener;
    
    @Autowired
    private EndEventListener endEventListener;
    
    @PostConstruct
    public void afterPropertiesSet() {
        log.info("{} added", LocalBpmnParseListener.class.getName());
        
        if(processInstanceEndListener == null) {
            throw new RuntimeException("cannot be null");
        }
        
        if(endEventListener == null) {
            throw new RuntimeException("cannot be null");
        }
    }

    @Override
    public void parseProcess(Element element, ProcessDefinitionEntity entity) {
        log.debug("to add {} to listeners", ProcessInstanceEndListener.class.getName());
        entity.addListener(ExecutionListener.EVENTNAME_END, processInstanceEndListener);
    }

    @Override
    public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
        activity.addListener(ExecutionListener.EVENTNAME_END, endEventListener);
    }
}
