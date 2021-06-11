package com.webank.wecube.platform.core.dto.plugin;

import java.util.ArrayList;
import java.util.List;

public class CoreObjectMetaDto {
    private String id;

    private String name;

    private String packageName;

    private String source;

    private String latestSource;
    
    private String configId;
    
    private List<CoreObjectPropertyMetaDto> propertyMetas = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getLatestSource() {
        return latestSource;
    }

    public void setLatestSource(String latestSource) {
        this.latestSource = latestSource;
    }

    public List<CoreObjectPropertyMetaDto> getPropertyMetas() {
        return propertyMetas;
    }

    public void setPropertyMetas(List<CoreObjectPropertyMetaDto> propertyMetas) {
        this.propertyMetas = propertyMetas;
    }
    
    public void addPropertyMeta(CoreObjectPropertyMetaDto propertyMetaDto){
        if(propertyMetaDto == null){
            return;
        }
        
        if(this.propertyMetas == null){
            this.propertyMetas = new ArrayList<>();
        }
        
        this.propertyMetas.add(propertyMetaDto);
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }
    
    

}
