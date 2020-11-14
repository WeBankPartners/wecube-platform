package com.webank.wecube.platform.core.entity.plugin;

import static com.webank.wecube.platform.core.utils.Constants.LEFT_BRACKET_STRING;
import static com.webank.wecube.platform.core.utils.Constants.RIGHT_BRACKET_STRING;
import static com.webank.wecube.platform.core.utils.Constants.SEPARATOR_OF_NAMES;

public class PluginConfigInterfaces {
    public static final String DEFAULT_INTERFACE_TYPE = "EXECUTION";
    public static final String DEFAULT_IS_ASYNC_PROCESSING_VALUE = "N";
    private String id;

    private String pluginConfigId;

    private String action;

    private String serviceName;

    private String serviceDisplayName;

    private String path;

    private String httpMethod;

    private String isAsyncProcessing;

    private String type;

    private String filterRule;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getPluginConfigId() {
        return pluginConfigId;
    }

    public void setPluginConfigId(String pluginConfigId) {
        this.pluginConfigId = pluginConfigId == null ? null : pluginConfigId.trim();
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action == null ? null : action.trim();
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName == null ? null : serviceName.trim();
    }

    public String getServiceDisplayName() {
        return serviceDisplayName;
    }

    public void setServiceDisplayName(String serviceDisplayName) {
        this.serviceDisplayName = serviceDisplayName == null ? null : serviceDisplayName.trim();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path == null ? null : path.trim();
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod == null ? null : httpMethod.trim();
    }

    public String getIsAsyncProcessing() {
        return isAsyncProcessing;
    }

    public void setIsAsyncProcessing(String isAsyncProcessing) {
        this.isAsyncProcessing = isAsyncProcessing == null ? null : isAsyncProcessing.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getFilterRule() {
        return filterRule;
    }

    public void setFilterRule(String filterRule) {
        this.filterRule = filterRule == null ? null : filterRule.trim();
    }
    
    public String generateServiceName(PluginPackages pluginPackage, PluginConfigs pluginConfig) {
        return pluginPackage.getName() + SEPARATOR_OF_NAMES + pluginConfig.getName()
                + (null != pluginConfig.getRegisterName()
                        ? LEFT_BRACKET_STRING + pluginConfig.getRegisterName() + RIGHT_BRACKET_STRING : "")
                + SEPARATOR_OF_NAMES + action;
    }
}