package com.webank.wecube.platform.gateway.dto;

import java.util.ArrayList;
import java.util.List;

public class MvcContextRouteConfigDto {
    private String context;
    private long createdTime;
    private long lastModifiedTime;

    private boolean disabled;

    private long version;
    private List<HttpDestinationDto> defaultHttpDestinations = new ArrayList<>();
    private List<MvcHttpMethodAndPathConfigDto> mvcHttpMethodAndPathConfigs = new ArrayList<>();

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public List<HttpDestinationDto> getDefaultHttpDestinations() {
        return defaultHttpDestinations;
    }

    public void setDefaultHttpDestinations(List<HttpDestinationDto> defaultHttpDestinations) {
        this.defaultHttpDestinations = defaultHttpDestinations;
    }

    public List<MvcHttpMethodAndPathConfigDto> getMvcHttpMethodAndPathConfigs() {
        return mvcHttpMethodAndPathConfigs;
    }

    public void setMvcHttpMethodAndPathConfigs(List<MvcHttpMethodAndPathConfigDto> mvcHttpMethodAndPathConfigs) {
        this.mvcHttpMethodAndPathConfigs = mvcHttpMethodAndPathConfigs;
    }

    public void addDefaultHttpDestination(HttpDestinationDto defaultHttpDestination) {
        this.defaultHttpDestinations.add(defaultHttpDestination);
    }

    public void addMvcHttpMethodAndPathConfig(MvcHttpMethodAndPathConfigDto mvcHttpMethodAndPathConfig) {
        this.mvcHttpMethodAndPathConfigs.add(mvcHttpMethodAndPathConfig);
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

}
