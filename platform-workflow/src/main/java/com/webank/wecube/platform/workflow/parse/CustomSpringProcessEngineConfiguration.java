package com.webank.wecube.platform.workflow.parse;

import org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.calendar.CycleBusinessCalendar;
import org.camunda.bpm.engine.impl.calendar.DueDateBusinessCalendar;
import org.camunda.bpm.engine.impl.calendar.DurationBusinessCalendar;
import org.camunda.bpm.engine.impl.calendar.MapBusinessCalendarManager;
import org.camunda.bpm.engine.impl.cfg.BpmnParseFactory;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author gavinli
 *
 */
public class CustomSpringProcessEngineConfiguration extends SpringProcessEngineConfiguration {
    private static final Logger log = LoggerFactory.getLogger(CustomSpringProcessEngineConfiguration.class);

    public CustomSpringProcessEngineConfiguration() {
        log.info("{} created", CustomSpringProcessEngineConfiguration.class.getName());
    }

    protected BpmnDeployer getBpmnDeployer() {
        BpmnDeployer bpmnDeployer = new BpmnDeployer();
        bpmnDeployer.setExpressionManager(expressionManager);
        bpmnDeployer.setIdGenerator(idGenerator);

        if (bpmnParseFactory == null) {
            bpmnParseFactory = new CustomCamundaBpmnParseFactory();
        }

        BpmnParser bpmnParser = new BpmnParser(expressionManager, bpmnParseFactory);

        if (preParseListeners != null) {
            bpmnParser.getParseListeners().addAll(preParseListeners);
        }
        bpmnParser.getParseListeners().addAll(getDefaultBPMNParseListeners());
        if (postParseListeners != null) {
            bpmnParser.getParseListeners().addAll(postParseListeners);
        }

        bpmnDeployer.setBpmnParser(bpmnParser);

        return bpmnDeployer;
    }
    
    protected void initBusinessCalendarManager() {
        if (businessCalendarManager == null) {
            MapBusinessCalendarManager mapBusinessCalendarManager = new MapBusinessCalendarManager();
            mapBusinessCalendarManager.addBusinessCalendar(DurationBusinessCalendar.NAME,
                    new CustomDurationBusinessCalendar());
            mapBusinessCalendarManager.addBusinessCalendar(DueDateBusinessCalendar.NAME, new DueDateBusinessCalendar());
            mapBusinessCalendarManager.addBusinessCalendar(CycleBusinessCalendar.NAME, new CycleBusinessCalendar());

            businessCalendarManager = mapBusinessCalendarManager;
        }
    }
    
    
    static class CustomCamundaBpmnParseFactory implements BpmnParseFactory {

        @Override
        public BpmnParse createBpmnParse(BpmnParser bpmnParser) {
            return new CustomBpmnParse(bpmnParser);
        }

    }
}
