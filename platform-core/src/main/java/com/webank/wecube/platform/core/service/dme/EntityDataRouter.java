package com.webank.wecube.platform.core.service.dme;

public class EntityDataRouter {

    public EntityDescription deduceEntityDescription(String packageName, String entityName){
        EntityDescription entityDef = new EntityDescription();
        entityDef.setEntityName(packageName);
        entityDef.setHttpPort("8080");// TODO
        entityDef.setHttpHost("localhost");// TODO
        entityDef.setHttpSchema("http");
        entityDef.setPackageName(entityName);
        
        return entityDef;
    }
}
