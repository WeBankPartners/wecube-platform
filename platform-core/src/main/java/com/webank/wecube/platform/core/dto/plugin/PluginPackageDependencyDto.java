package com.webank.wecube.platform.core.dto.plugin;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;

public class PluginPackageDependencyDto {
    private String packageName;
    private String version;
    private Set<PluginPackageDependencyDto> dependencies = new HashSet<>();

    public PluginPackageDependencyDto(String packageName, String version,
            Set<PluginPackageDependencyDto> dependencies) {
        this.packageName = packageName;
        this.version = version;
        this.dependencies = dependencies;
    }

    public PluginPackageDependencyDto() {
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Set<PluginPackageDependencyDto> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<PluginPackageDependencyDto> dependencies) {
        this.dependencies = dependencies;
    }

    public void addDependency(PluginPackageDependencyDto dependency) {
        if (dependency == null) {
            return;
        }
        if (dependencies == null) {
            dependencies = new HashSet<>();
        }

        dependencies.add(dependency);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        PluginPackageDependencyDto that = (PluginPackageDependencyDto) o;

        return new EqualsBuilder().append(getPackageName(), that.getPackageName())
                .append(getVersion(), that.getVersion()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getPackageName()).append(getVersion()).toHashCode();
    }

}
