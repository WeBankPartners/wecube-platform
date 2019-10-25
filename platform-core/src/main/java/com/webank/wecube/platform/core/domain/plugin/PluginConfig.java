package com.webank.wecube.platform.core.domain.plugin;

import com.fasterxml.jackson.annotation.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@JsonIgnoreType
@Entity
@Table(name = "plugin_configs")
public class PluginConfig {

    public enum Status {
        NOT_CONFIGURED, CONFIGURED, ONLINE, DECOMMISSIONED
    }

    @Id
    @GeneratedValue
    private Integer id;

    @JsonBackReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "plugin_package_id")
    private PluginPackage pluginPackage;

    @Column
    private String name;

    @Column
    private Integer entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "pluginConfig", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PluginConfigInterface> interfaces = new ArrayList<>();

    public void addPluginConfigInterface(PluginConfigInterface pluginConfigInterface) {
        this.interfaces.add(pluginConfigInterface);
    }

    @JsonInclude
    @EqualsAndHashCode.Include
    @ToString.Include
    public Integer getPluginPackageId() {
        return pluginPackage == null ? null : pluginPackage.getId();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PluginPackage getPluginPackage() {
        return pluginPackage;
    }

    public void setPluginPackage(PluginPackage pluginPackage) {
        this.pluginPackage = pluginPackage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<PluginConfigInterface> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<PluginConfigInterface> interfaces) {
        this.interfaces = interfaces;
    }

    public PluginConfig() {
    }

    public PluginConfig(Integer id, PluginPackage pluginPackage, String name, Integer entityId, Status status, List<PluginConfigInterface> interfaces) {
        this.id = id;
        this.pluginPackage = pluginPackage;
        this.name = name;
        this.entityId = entityId;
        this.status = status;
        this.interfaces = interfaces;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[]{"pluginPackage"});
    }
}
