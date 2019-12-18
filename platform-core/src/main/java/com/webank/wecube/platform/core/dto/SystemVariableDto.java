package com.webank.wecube.platform.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.webank.wecube.platform.core.domain.SystemVariable;

@JsonInclude(Include.NON_NULL)
public class SystemVariableDto {
    private String id;
    private String packageName;
    private String name;
    private String value;
    private String defaultValue;
    private String scope;
    private String source;
    private String status;

    public SystemVariableDto() {
    }

    public static SystemVariableDto fromDomain(SystemVariable domain) {
        SystemVariableDto systemVariableDto = new SystemVariableDto();
        systemVariableDto.setId(domain.getId());
        systemVariableDto.setPackageName(domain.getPackageName());
        systemVariableDto.setName(domain.getName());
        systemVariableDto.setValue(domain.getValue());
        systemVariableDto.setDefaultValue(domain.getDefaultValue());
        systemVariableDto.setScope(domain.getScope());
        systemVariableDto.setSource(domain.getSource());
        systemVariableDto.setStatus(domain.getStatus());
        return systemVariableDto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
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

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
