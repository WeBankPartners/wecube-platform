package com.webank.wecube.platform.core.service.plugin;

import java.util.Map;

public class CoreObjectVarCalculationContext {
    
    private Map<Object, Object> externalCacheMap;

    public Map<Object, Object> getExternalCacheMap() {
        return externalCacheMap;
    }

    public void setExternalCacheMap(Map<Object, Object> externalCacheMap) {
        this.externalCacheMap = externalCacheMap;
    }
    
    public CoreObjectVarCalculationContext withExternalCacheMap(Map<Object, Object> externalCacheMap){
        this.externalCacheMap = externalCacheMap;
        return this;
    }

}
