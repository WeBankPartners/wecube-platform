package com.webank.wecube.platform.core.service.dme;

/**
 * 
 * @author gavin
 *
 */
public interface EntityDataRouteFactory {
    String HTTP_SCHEME_HTTP = "http";
    String HTTP_SCHEME_HTTPS = "https";

    /**
     * Deduce the entity route base on package and entity.
     * @param packageName
     * @param entityName
     * @return
     */
    EntityRouteDescription deduceEntityDescription(String packageName, String entityName);
}
