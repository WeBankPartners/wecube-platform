package com.webank.wecube.platform.auth.server.boot;

/**
 * 
 * @author gavin
 *
 */
public interface DatabaseInitializer {

    String STRATEGY_NONE = "none";
    String STRATEGY_UPDATE = "update";
    String STRATEGY_DROP_CREATE = "drop-create";
    
    /**
     * 
     */
    void initialize();
    
    String getInitializeStrategy();
}
