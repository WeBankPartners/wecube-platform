package com.webank.wecube.platform.core.dto.plugin;
//package com.webank.wecube.platform.core.dto;
//
//import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
//import org.apache.commons.lang.builder.ReflectionToStringBuilder;
//
///*
//  This is a workaround solution to cater the parser logic for data model and other parts in register.xml
// */
//public class PluginPackageDto {
//
//    private String id;
//
//    private String name;
//
//    private String version;
//
//    private PluginPackage pluginPackage;
//
//    private PluginPackageDataModelDto pluginPackageDataModelDto;
//
//    public PluginPackageDto() {
//    }
//
//    public PluginPackageDto(String id, String name, String version, PluginPackage pluginPackage, PluginPackageDataModelDto pluginPackageDataModelDto) {
//        this.id = id;
//        this.name = name;
//        this.version = version;
//        this.pluginPackage = pluginPackage;
//        this.pluginPackageDataModelDto = pluginPackageDataModelDto;
//    }
//
//    public String getId() {
//        return id != null ? id : pluginPackage.getId();
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name !=null ? name : pluginPackage.getName();
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getVersion() {
//        return version !=  null ? version : pluginPackage.getVersion();
//    }
//
//    public void setVersion(String version) {
//        this.version = version;
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
//    public PluginPackageDataModelDto getPluginPackageDataModelDto() {
//        return pluginPackageDataModelDto;
//    }
//
//    public void setPluginPackageDataModelDto(PluginPackageDataModelDto pluginPackageDataModelDto) {
//        this.pluginPackageDataModelDto = pluginPackageDataModelDto;
//    }
//
//    @Override
//    public String toString() {
//        return ReflectionToStringBuilder.toString(this);
//    }
//}
