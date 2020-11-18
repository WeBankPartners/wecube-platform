package com.webank.wecube.platform.core.entity.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PluginPackages {

    public static final String UNREGISTERED = "UNREGISTERED";
    public static final String REGISTERED = "REGISTERED";
    public static final String RUNNING = "RUNNING";
    public static final String STOPPED = "STOPPED";
    public static final String DECOMMISSIONED = "DECOMMISSIONED";

    public static final List<String> PLUGIN_PACKAGE_ACTIVE_STATUSES = Arrays.asList(REGISTERED, RUNNING, STOPPED);

    private String id;

    private String name;

    private String version;

    private String status;

    private Date uploadTimestamp;

    private Boolean uiPackageIncluded;

    private transient List<PluginPackageDependencies> pluginPackageDependencies = new ArrayList<>();
    private transient List<PluginPackageAuthorities> pluginPackageAuthorities = new ArrayList<>();
    
    private transient List<PluginPackageRuntimeResourcesDocker> dockers = new ArrayList<>();
    
    private transient List<PluginPackageRuntimeResourcesMysql> mysqls = new ArrayList<>();
    private transient List<PluginPackageRuntimeResourcesS3> s3s = new ArrayList<>();
 
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version == null ? null : version.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public Date getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(Date uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public Boolean getUiPackageIncluded() {
        return uiPackageIncluded;
    }

    public void setUiPackageIncluded(Boolean uiPackageIncluded) {
        this.uiPackageIncluded = uiPackageIncluded;
    }

    public List<PluginPackageDependencies> getPluginPackageDependencies() {
        return pluginPackageDependencies;
    }

    public void setPluginPackageDependencies(List<PluginPackageDependencies> pluginPackageDependencies) {
        this.pluginPackageDependencies = pluginPackageDependencies;
    }

    public List<PluginPackageAuthorities> getPluginPackageAuthorities() {
        return pluginPackageAuthorities;
    }

    public void setPluginPackageAuthorities(List<PluginPackageAuthorities> pluginPackageAuthorities) {
        this.pluginPackageAuthorities = pluginPackageAuthorities;
    }

    public List<PluginPackageRuntimeResourcesDocker> getDockers() {
        return dockers;
    }

    public void setDockers(List<PluginPackageRuntimeResourcesDocker> dockers) {
        this.dockers = dockers;
    }

    public List<PluginPackageRuntimeResourcesMysql> getMysqls() {
        return mysqls;
    }

    public void setMysqls(List<PluginPackageRuntimeResourcesMysql> mysqls) {
        this.mysqls = mysqls;
    }

    public List<PluginPackageRuntimeResourcesS3> getS3s() {
        return s3s;
    }

    public void setS3s(List<PluginPackageRuntimeResourcesS3> s3s) {
        this.s3s = s3s;
    }

    
}