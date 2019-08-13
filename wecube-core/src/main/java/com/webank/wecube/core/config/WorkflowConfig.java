package com.webank.wecube.core.config;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.webank.wecube.core.service.workflow.parse.LocalBpmnParseListener;

@Component
@Order(Ordering.DEFAULT_ORDER + 1)
public class WorkflowConfig extends AbstractCamundaConfiguration {
	
	@Autowired
	private LocalBpmnParseListener processStartAndEndEventInitializer;

	@Override
	public void preInit(SpringProcessEngineConfiguration processEngineConfiguration) {
		super.preInit(processEngineConfiguration);
		
		List<BpmnParseListener> preParseListeners = processEngineConfiguration.getCustomPreBPMNParseListeners();
		if (preParseListeners == null) {
			preParseListeners = new ArrayList<BpmnParseListener>();
			processEngineConfiguration.setCustomPreBPMNParseListeners(preParseListeners);
		}
		
		preParseListeners.add(processStartAndEndEventInitializer);
	}

}
