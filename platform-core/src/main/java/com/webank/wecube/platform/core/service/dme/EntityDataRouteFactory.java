package com.webank.wecube.platform.core.service.dme;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.ApplicationProperties;

@Service
public class EntityDataRouteFactory {
    
    @Autowired
    private ApplicationProperties applicationProperties;

    public EntityRouteDescription deduceEntityDescription(String packageName, String entityName){
        String gatewayUrl = applicationProperties.getGatewayUrl();
        String [] parts = gatewayUrl.split(":");
        EntityRouteDescription entityDef = new EntityRouteDescription();
        entityDef.setEntityName(packageName);
        entityDef.setHttpPort(parts[1]);
        entityDef.setHttpHost(parts[0]);
        entityDef.setHttpScheme("http");
        entityDef.setPackageName(entityName);
        
        return entityDef;
    }
}
