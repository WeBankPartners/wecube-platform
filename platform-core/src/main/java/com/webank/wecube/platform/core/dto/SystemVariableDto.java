package com.webank.wecube.platform.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.webank.wecube.platform.core.domain.SystemVariable;

@JsonInclude(Include.NON_NULL)
public class SystemVariableDto {
    private String id;
    private String pluginPackageId;
    private String pluginPackageName;
    private String name;
    private String value;
    private String defaultValue;
    private String scopeType;
    private String scopeValue;
    private String status;

    public SystemVariableDto() {

    }
    public static SystemVariableDto fromDomain(SystemVariable domain) {
        SystemVariableDto systemVariableDto = new SystemVariableDto();
        systemVariableDto.setId(domain.getId());
        systemVariableDto.setPluginPackageId(domain.getPluginPackage()!=null? domain.getPluginPackage().getId():null);
        systemVariableDto.setPluginPackageName(domain.getPluginPackage()!=null? domain.getPluginPackage().getName():null);
        systemVariableDto.setName(domain.getName());
        systemVariableDto.setValue(domain.getValue());
        systemVariableDto.setDefaultValue(domain.getDefaultValue());
        systemVariableDto.setScopeType(domain.getScopeType());
        systemVariableDto.setScopeValue(domain.getScopeValue());
        systemVariableDto.setStatus(domain.getStatus());
        return systemVariableDto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPluginPackageId() {
        return pluginPackageId;
    }

    public void setPluginPackageId(String pluginPackageId) {
        this.pluginPackageId = pluginPackageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getScopeType() {
        return scopeType;
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    public String getScopeValue() {
        return scopeValue;
    }

    public void setScopeValue(String scopeValue) {
        this.scopeValue = scopeValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getPluginPackageName() {
        return pluginPackageName;
    }
    public void setPluginPackageName(String pluginPackageName) {
        this.pluginPackageName = pluginPackageName;
    }

}
