package com.webank.wecube.platform.core.domain.plugin;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import javax.persistence.*;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.webank.wecube.platform.core.utils.Constants.KEY_COLUMN_DELIMITER;

@JsonIgnoreType
@Entity
@Table(name = "plugin_configs")
public class PluginConfig {

    public enum Status {
        DISABLED, ENABLED
    }

    @Id
    private String id;

    @JsonBackReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "plugin_package_id")
    private PluginPackage pluginPackage;

    @Column
    private String name;

    @Column
    private String entityId;

    @Column
    private String entityName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "pluginConfig", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<PluginConfigInterface> interfaces;

    @JsonInclude
    @EqualsAndHashCode.Include
    @ToString.Include
    public String getPluginPackageId() {
        return pluginPackage == null ? null : pluginPackage.getId();
    }

    public String getId() {
        return this.id;
    }

    @PrePersist
    public void initId() {
        if (null == this.id || this.id.trim().equals("")) {
            this.id = String.join(KEY_COLUMN_DELIMITER,
                    null != pluginPackage ? pluginPackage.getName() : null,
                    null != pluginPackage ? pluginPackage.getVersion() : null,
                    name,
                    entityName);
            this.id = this.id.replaceAll("\\s+", "_");
        }
    }

    public void setId(String id) {
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

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Set<PluginConfigInterface> getInterfaces() {
        // TODO: need to optimize
        return interfaces
                .stream()
                .sorted(Comparator.comparing(PluginConfigInterface::getAction))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void setInterfaces(Set<PluginConfigInterface> interfaces) {
        this.interfaces = interfaces;
    }

    public PluginConfig() {
    }

    public PluginConfig(String id, PluginPackage pluginPackage, String name, String entityId, String entityName, Status status, Set<PluginConfigInterface> interfaces) {
        this.id = id;
        this.pluginPackage = pluginPackage;
        this.name = name;
        this.entityId = entityId;
        this.entityName = entityName;
        this.status = status;
        this.interfaces = interfaces;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[]{"pluginPackage"});
    }

}
