package com.webank.wecube.platform.workflow.delegate;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.webank.wecube.platform.workflow.model.ServiceInvocationEvent;

/**
 * 
 * @author gavin
 *
 */
public final class QueueHolder {
    private static BlockingQueue<ServiceInvocationEvent> serviceInvocationEventQueue = new LinkedBlockingQueue<ServiceInvocationEvent>();
    private static BlockingQueue<ServiceInvocationEvent> deferredServiceInvocationEventQueue = new LinkedBlockingQueue<ServiceInvocationEvent>();

    public static void putServiceInvocationEvent(ServiceInvocationEvent event) throws InterruptedException {
        serviceInvocationEventQueue.put(event);
    }

    public static void putDeferredServiceInvocationEvent(ServiceInvocationEvent event) throws InterruptedException {
        deferredServiceInvocationEventQueue.put(event);
    }

    public static ServiceInvocationEvent pollServiceInvocationEvent(long timeout, TimeUnit unit)
            throws InterruptedException {
        return serviceInvocationEventQueue.poll(timeout, unit);
    }

    public static ServiceInvocationEvent pollDeferredServiceInvocationEvent() {
        return deferredServiceInvocationEventQueue.poll();
    }
}
