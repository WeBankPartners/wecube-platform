package com.webank.wecube.platform.core.domain.plugin;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import javax.persistence.*;

@Entity
@Table(name = "plugin_package_resource_files")
public class PluginPackageResourceFile {

    @Id
    @GeneratedValue
    private int id;

    @JsonBackReference
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "plugin_package_id")
    private PluginPackage pluginPackage;

    @Column
    private String source;

    @Column
    private String relatedPath;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PluginPackage getPluginPackage() {
        return pluginPackage;
    }

    public void setPluginPackage(PluginPackage pluginPackage) {
        this.pluginPackage = pluginPackage;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRelatedPath() {
        return relatedPath;
    }

    public void setRelatedPath(String relatedPath) {
        this.relatedPath = relatedPath;
    }

    public PluginPackageResourceFile() {
        super();
    }

    public PluginPackageResourceFile(int id, PluginPackage pluginPackage, String source, String relatedPath) {
        this.id = id;
        this.pluginPackage = pluginPackage;
        this.source = source;
        this.relatedPath = relatedPath;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[]{"pluginPackage"});
    }
}
