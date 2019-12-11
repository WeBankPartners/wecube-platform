package com.webank.wecube.platform.core.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleDto {

    @JsonProperty(value = "createdBy")
    private String createdBy;
    @JsonProperty(value = "updatedBy")
    private String updatedBy;
    @JsonProperty(value = "createdTime")
    private String createdTime;
    @JsonProperty(value = "updatedTime")
    private String updatedTime;
    @JsonProperty(value = "id")
    private Long id;
    @JsonProperty(value = "displayName")
    private String displayName;
    @JsonProperty(value = "name")
    private String name;


    public RoleDto() {
    }

    public RoleDto(String displayName, String name) {
        this.displayName = displayName;
        this.name = name;
    }

    public RoleDto(String createdBy, String updatedBy, String createdTime, String updatedTime, Long id, String displayName, String name) {
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
        this.id = id;
        this.displayName = displayName;
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
