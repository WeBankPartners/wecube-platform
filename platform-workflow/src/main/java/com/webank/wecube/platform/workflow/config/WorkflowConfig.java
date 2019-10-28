package com.webank.wecube.platform.workflow.config;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.webank.wecube.platform.workflow.parse.LocalBpmnParseListener;

@Configuration
@Order(Ordering.DEFAULT_ORDER + 1)
public class WorkflowConfig extends AbstractCamundaConfiguration {

    private static final Logger log = LoggerFactory.getLogger(WorkflowConfig.class);

    @Autowired
    private LocalBpmnParseListener processStartAndEndEventInitializer;

    @PostConstruct
    public void afterPropertiesSet() {
        log.info("WorkflowConfig {} is ready.", WorkflowConfig.class.getName());
    }

    @Override
    public void preInit(SpringProcessEngineConfiguration processEngineConfiguration) {
        super.preInit(processEngineConfiguration);

        List<BpmnParseListener> preParseListeners = processEngineConfiguration.getCustomPreBPMNParseListeners();
        if (preParseListeners == null) {
            preParseListeners = new ArrayList<BpmnParseListener>();
            processEngineConfiguration.setCustomPreBPMNParseListeners(preParseListeners);
        }

        log.info("adding LocalBpmnParseListener:{}", processStartAndEndEventInitializer.getClass().getName());

        preParseListeners.add(processStartAndEndEventInitializer);
    }
}
