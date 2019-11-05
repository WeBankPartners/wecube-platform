package com.webank.wecube.platform.workflow;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * 
 * @author gavin
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(PlatformWorkflowConfiguration.class)
@ComponentScan({ "com.webank.wecube.platform.workflow" })
public @interface EnablePlatformWorkflowApplication {

}
