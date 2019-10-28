package com.webank.wecube.platform.workflow.config;

import java.util.List;

import org.camunda.bpm.engine.impl.cfg.CompositeProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.util.CamundaSpringBootUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.webank.wecube.platform.workflow.parse.CustomSpringProcessEngineConfiguration;

@Configuration
public class CustomBpmConfig {
    private static final Logger log = LoggerFactory.getLogger(CustomBpmConfig.class);
    
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
