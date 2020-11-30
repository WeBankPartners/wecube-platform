//package com.webank.wecube.platform.core.domain.plugin;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.webank.wecube.platform.core.support.DomainIdBuilder;
//import org.apache.commons.lang.builder.ReflectionToStringBuilder;
//
//import javax.persistence.*;
//
//@Entity
//@Table(name = "plugin_package_runtime_resources_docker")
//public class PluginPackageRuntimeResourcesDocker {
//
//    @Id
//    private String id;
//
//    @JsonBackReference
//    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
//    @JoinColumn(name = "plugin_package_id")
//    private PluginPackage pluginPackage;
//
//    @Column
//    private String imageName;
//
//    @Column
//    private String containerName;
//
//    @Column
//    private String portBindings;
//
//    @Column
//    private String volumeBindings;
//
//    @Column
//    private String envVariables;
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    @PrePersist
//    public void initId() {
//        this.id = DomainIdBuilder.buildDomainId(this);
//    }
//
//    public PluginPackage getPluginPackage() {
//        return pluginPackage;
//    }
//
//    public void setPluginPackage(PluginPackage pluginPackage) {
//        this.pluginPackage = pluginPackage;
//    }
//
//    public String getImageName() {
//        return imageName;
//    }
//
//    public void setImageName(String imageName) {
//        this.imageName = imageName;
//    }
//
//    public String getContainerName() {
//        return containerName;
//    }
//
//    public void setContainerName(String containerName) {
//        this.containerName = containerName;
//    }
//
//    public String getPortBindings() {
//        return portBindings;
//    }
//
//    public void setPortBindings(String portBindings) {
//        this.portBindings = portBindings;
//    }
//
//    public String getVolumeBindings() {
//        return volumeBindings;
//    }
//
//    public void setVolumeBindings(String volumeBindings) {
//        this.volumeBindings = volumeBindings;
//    }
//
//    public String getEnvVariables() {
//        return envVariables;
//    }
//
//    public void setEnvVariables(String envVariables) {
//        this.envVariables = envVariables;
//    }
//
//    public PluginPackageRuntimeResourcesDocker() {
//        super();
//    }
//
//    public PluginPackageRuntimeResourcesDocker(String id, PluginPackage pluginPackage, String imageName, String containerName, String portBindings, String volumeBindings, String envVariables) {
//        this.id = id;
//        this.pluginPackage = pluginPackage;
//        this.imageName = imageName;
//        this.containerName = containerName;
//        this.portBindings = portBindings;
//        this.volumeBindings = volumeBindings;
//        this.envVariables = envVariables;
//    }
//
//    @Override
//    public String toString() {
//        return ReflectionToStringBuilder.toStringExclude(this, new String[]{"pluginPackage"});
//    }
//}
