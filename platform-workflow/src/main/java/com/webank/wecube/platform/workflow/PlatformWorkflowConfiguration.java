package com.webank.wecube.platform.workflow;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 
 * @author gavin
 *
 */
@EnableProcessApplication
@ComponentScan(basePackages = { "com.webank.wecube.platform.workflow" })
@EntityScan(basePackages = { "com.webank.wecube.platform.workflow.entity" })
@EnableJpaRepositories(basePackages = { "com.webank.wecube.platform.workflow.repository" })
public class PlatformWorkflowConfiguration {
}
