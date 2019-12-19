package com.webank.wecube.platform.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageRuntimeResourcesDocker;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageRuntimeResourcesMysql;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageRuntimeResourcesS3;

import java.util.Set;

public class PluginPackageRuntimeResouceDto {
    @JsonProperty("docker")
    Set<PluginPackageRuntimeResourcesDocker> dockerSet;
    @JsonProperty("mysql")
    Set<PluginPackageRuntimeResourcesMysql> mysqlSet;
    @JsonProperty("s3")
    Set<PluginPackageRuntimeResourcesS3> s3Set;

    public PluginPackageRuntimeResouceDto(Set<PluginPackageRuntimeResourcesDocker> dockerSet,
                                          Set<PluginPackageRuntimeResourcesMysql> mysqlSet,
                                          Set<PluginPackageRuntimeResourcesS3> s3Set) {
        this.dockerSet = dockerSet;
        this.mysqlSet = mysqlSet;
        this.s3Set = s3Set;
    }

    public Set<PluginPackageRuntimeResourcesDocker> getDockerSet() {
        return dockerSet;
    }

    public void setDockerSet(Set<PluginPackageRuntimeResourcesDocker> dockerSet) {
        this.dockerSet = dockerSet;
    }

    public Set<PluginPackageRuntimeResourcesMysql> getMysqlSet() {
        return mysqlSet;
    }

    public void setMysqlSet(Set<PluginPackageRuntimeResourcesMysql> mysqlSet) {
        this.mysqlSet = mysqlSet;
    }

    public Set<PluginPackageRuntimeResourcesS3> getS3Set() {
        return s3Set;
    }

    public void setS3Set(Set<PluginPackageRuntimeResourcesS3> s3Set) {
        this.s3Set = s3Set;
    }
}
