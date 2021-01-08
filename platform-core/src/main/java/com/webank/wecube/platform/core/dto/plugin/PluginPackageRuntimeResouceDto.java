package com.webank.wecube.platform.core.dto.plugin;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PluginPackageRuntimeResouceDto {
    @JsonProperty("docker")
    private List<PluginPackageRuntimeResourcesDockerDto> dockerSet;
    @JsonProperty("mysql")
    private List<PluginPackageRuntimeResourcesMysqlDto> mysqlSet;
    @JsonProperty("s3")
    private List<PluginPackageRuntimeResourcesS3Dto> s3Set;

    public List<PluginPackageRuntimeResourcesDockerDto> getDockerSet() {
        return dockerSet;
    }

    public void setDockerSet(List<PluginPackageRuntimeResourcesDockerDto> dockerSet) {
        this.dockerSet = dockerSet;
    }

    public List<PluginPackageRuntimeResourcesMysqlDto> getMysqlSet() {
        return mysqlSet;
    }

    public void setMysqlSet(List<PluginPackageRuntimeResourcesMysqlDto> mysqlSet) {
        this.mysqlSet = mysqlSet;
    }

    public List<PluginPackageRuntimeResourcesS3Dto> getS3Set() {
        return s3Set;
    }

    public void setS3Set(List<PluginPackageRuntimeResourcesS3Dto> s3Set) {
        this.s3Set = s3Set;
    }

}
