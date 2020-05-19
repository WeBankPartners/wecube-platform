package com.webank.wecube.platform.workflow.parse;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 
 * @author gavin
 *
 */
@Component
public class SpringApplicationContextUtil implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(SpringApplicationContextUtil.class);

    private static ApplicationContext ctx;

    @PostConstruct
    public void afterPropertiesSet() {
        if(ctx == null){
            log.error("application context must not be null");
            throw new IllegalStateException("Application Context must not be null");
        }
        log.debug("{} is already with ApplicationContext:{}", SpringApplicationContextUtil.class.getSimpleName(),
                ctx.getClass().getName());
    }

    @Override
    public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
        ctx = appCtx;
    }
    
    public static <T> T getBean(String name, Class<T> requiredType){
        return ctx.getBean(name, requiredType);
    }
    
    public static <T> T getBean(Class<T> requiredType){
        return ctx.getBean(requiredType);
    }

}
