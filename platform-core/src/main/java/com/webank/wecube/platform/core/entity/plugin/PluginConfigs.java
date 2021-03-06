package com.webank.wecube.platform.core.entity.plugin;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class PluginConfigs {
    public static final String DISABLED = "DISABLED";
    public static final String ENABLED = "ENABLED";
    private String id;

    private String pluginPackageId;

    private String name;

    private String targetPackage;

    private String targetEntity;

    private String targetEntityFilterRule;

    private String registerName;

    private String status;

    private transient List<PluginConfigInterfaces> interfaces = new ArrayList<>();

    private transient PluginPackages pluginPackage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getPluginPackageId() {
        return pluginPackageId;
    }

    public void setPluginPackageId(String pluginPackageId) {
        this.pluginPackageId = pluginPackageId == null ? null : pluginPackageId.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getTargetPackage() {
        return targetPackage;
    }

    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage == null ? null : targetPackage.trim();
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity == null ? null : targetEntity.trim();
    }

    public String getTargetEntityFilterRule() {
        return targetEntityFilterRule;
    }

    public void setTargetEntityFilterRule(String targetEntityFilterRule) {
        this.targetEntityFilterRule = targetEntityFilterRule == null ? null : targetEntityFilterRule.trim();
    }

    public String getRegisterName() {
        return registerName;
    }

    public void setRegisterName(String registerName) {
        this.registerName = registerName == null ? null : registerName.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getTargetEntityWithFilterRule() {
        return StringUtils.join(targetPackage, ":", targetEntity, targetEntityFilterRule);
    }

    public List<PluginConfigInterfaces> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<PluginConfigInterfaces> interfaces) {
        this.interfaces = interfaces;
    }

    public void addPluginConfigInterfaces(PluginConfigInterfaces intf) {
        if (intf == null) {
            return;
        }

        if (this.interfaces == null) {
            this.interfaces = new ArrayList<>();
        }

        this.interfaces.add(intf);
    }

    public PluginPackages getPluginPackage() {
        return pluginPackage;
    }

    public void setPluginPackage(PluginPackages pluginPackage) {
        this.pluginPackage = pluginPackage;
    }

}