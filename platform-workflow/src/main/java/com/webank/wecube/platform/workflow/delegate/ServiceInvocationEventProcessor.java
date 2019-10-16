package com.webank.wecube.platform.workflow.delegate;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.webank.wecube.platform.workflow.model.ServiceInvocationEvent;

@Component
public class ServiceInvocationEventProcessor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ServiceInvocationEventProcessor.class);

    private ExecutorService masterExecutor = Executors.newFixedThreadPool(1);

    private ExecutorService slaveExecutor = Executors.newFixedThreadPool(3);

    private int maxRetryTimes = 1;

    private boolean needStop;

    @Autowired
    private ServiceInvocationEventResolver serviceInvocationEventResolver;

    @PostConstruct
    public void afterPropertiesSet() {
        log.info("{} is ready", ServiceInvocationEventProcessor.class.getSimpleName());
        start();
    }

    @PreDestroy
    public void preDestroy() {
        log.info("try to destroy {} ", ServiceInvocationEventProcessor.class.getSimpleName());
        if (masterExecutor != null) {
            masterExecutor.shutdown();
        }

        if (slaveExecutor != null) {
            slaveExecutor.shutdown();
        }
    }

    private void start() {
        masterExecutor.execute(this);
    }

    public boolean isNeedStop() {
        return needStop;
    }

    public void setNeedStop(boolean needStop) {
        this.needStop = needStop;
    }

    @Override
    public void run() {
        log.info("{} start to work", ServiceInvocationEventProcessor.class.getSimpleName());

        long startTime = System.currentTimeMillis();
        while (!isNeedStop()) {
            try {
                ServiceInvocationEvent event = QueueHolder.pollServiceInvocationEvent(1000, TimeUnit.MILLISECONDS);

                if (event != null) {
                    processServiceInvocationEvent(event);
                }

                if ((System.currentTimeMillis() - startTime) > (1000 * 6)) {
                    startTime = System.currentTimeMillis();
                    ServiceInvocationEvent deferredEvent = QueueHolder.pollDeferredServiceInvocationEvent();
                    if (deferredEvent != null) {
                        log.info("process deferred event {}", deferredEvent);
                        processServiceInvocationEvent(deferredEvent);
                    }
                }

            } catch (InterruptedException e) {
                log.error("errors while running processor", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processServiceInvocationEvent(ServiceInvocationEvent event) throws InterruptedException {
        try {
            slaveExecutor
                    .submit(new ServiceInvocationEventWorker(event, this.serviceInvocationEventResolver));
        } catch (Exception e) {
            log.error("errors while processing event", e);
            event.increaseRetryTimes();

            if (event.getRetryTimes() < maxRetryTimes) {
                log.info("retry and put back to deffered queue,{}", event);
                QueueHolder.putDeferredServiceInvocationEvent(event);
            } else {
                log.error("event was abandoned due to resolvation errors, details:{}", event);
            }
        }
    }

    private static class ServiceInvocationEventWorker implements Callable<Integer> {

        public static final int EXECUTION_SUCC = 1;

        private ServiceInvocationEvent event;

        private ServiceInvocationEventResolver resolver;

        public ServiceInvocationEventWorker(ServiceInvocationEvent event, ServiceInvocationEventResolver resolver) {
            super();
            this.event = event;
            this.resolver = resolver;
        }

        @Override
        public Integer call() throws Exception {

            log.debug("{} processing {}", Thread.currentThread().getName(), event);

            try {
                resolver.resolveServiceInvocationEvent(event);
            } catch (Exception e) {
                log.error("resolvation errors", e);
//                throw e;
            }

            return EXECUTION_SUCC;
        }

    }

}
