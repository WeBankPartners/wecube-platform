package com.webank.wecube.platform.core.service.dme.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.service.dme.EntityDataRouteFactory;
import com.webank.wecube.platform.core.service.dme.EntityRouteDescription;

@Service
public class SimpleEntityDataRouteFactory implements EntityDataRouteFactory{
    @Autowired
    private ApplicationProperties applicationProperties;

    public EntityRouteDescription deduceEntityDescription(String packageName, String entityName){
        String gatewayUrl = applicationProperties.getGatewayUrl();
        String [] parts = gatewayUrl.split(":");
        EntityRouteDescription entityDef = new EntityRouteDescription();
        entityDef.setEntityName(entityName);
        entityDef.setHttpPort(parts[1]);
        entityDef.setHttpHost(parts[0]);
        entityDef.setHttpScheme(HTTP_SCHEME_HTTP);
        entityDef.setPackageName(packageName);
        
        return entityDef;
    }
}
