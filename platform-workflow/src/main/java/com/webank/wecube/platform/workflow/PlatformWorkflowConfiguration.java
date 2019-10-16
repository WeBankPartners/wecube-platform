package com.webank.wecube.platform.workflow;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.CompositeProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.camunda.bpm.spring.boot.starter.util.CamundaSpringBootUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

import com.webank.wecube.platform.workflow.parse.CustomSpringProcessEngineConfiguration;
import com.webank.wecube.platform.workflow.parse.LocalBpmnParseListener;

@Configuration
@EnableProcessApplication
@ComponentScan(basePackages = { "com.webank.wecube.platform.workflow" })
@EntityScan(basePackages = { "com.webank.wecube.platform.workflow.entity" })
@EnableJpaRepositories(basePackages = { "com.webank.wecube.platform.workflow.repository" })
public class PlatformWorkflowConfiguration {
    private static final Logger log = LoggerFactory.getLogger(PlatformWorkflowConfiguration.class);

    @Configuration
    public class CustomBpmConfig {

        @Bean
        @ConditionalOnMissingBean(ProcessEngineConfigurationImpl.class)
        public ProcessEngineConfigurationImpl processEngineConfigurationImpl(
                List<ProcessEnginePlugin> processEnginePlugins) {

            log.info("configure process engine configuration impl,class={}",
                    CustomSpringProcessEngineConfiguration.class.getName());
            final SpringProcessEngineConfiguration configuration = CamundaSpringBootUtil
                    .initCustomFields(new CustomSpringProcessEngineConfiguration());
            configuration.getProcessEnginePlugins().add(new CompositeProcessEnginePlugin(processEnginePlugins));
            return configuration;
        }
    }

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
}
