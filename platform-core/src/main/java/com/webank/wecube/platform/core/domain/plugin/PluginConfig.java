package com.webank.wecube.platform.core.domain.plugin;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.webank.wecube.platform.core.support.DomainIdBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
    private String targetPackage;

    @Column
    private String targetEntity;

    @Column
    private String targetEntityFilterRule;

    @Column
    private String registerName;

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
        this.id = DomainIdBuilder.buildDomainId(this);
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

    public String getTargetPackage() {
        return targetPackage;
    }

    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Set<PluginConfigInterface> getInterfaces() {
        // TODO: need to optimize
        return interfaces.stream().sorted(Comparator.comparing(PluginConfigInterface::getAction))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void setInterfaces(Set<PluginConfigInterface> interfaces) {
        this.interfaces = interfaces;
    }

    public PluginConfig() {
    }

    public PluginConfig(String id, PluginPackage pluginPackage, String name, String targetPackage, String targetEntity,
            Status status, Set<PluginConfigInterface> interfaces) {
        this.id = id;
        this.pluginPackage = pluginPackage;
        this.name = name;
        this.targetPackage = targetPackage;
        this.targetEntity = targetEntity;
        this.status = status;
        this.interfaces = interfaces;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[] { "pluginPackage" });
    }

    public String getRegisterName() {
        return registerName;
    }

    public void setRegisterName(String registerName) {
        this.registerName = registerName;
    }

    public String getTargetEntityFilterRule() {
        return targetEntityFilterRule;
    }

    public void setTargetEntityFilterRule(String targetEntityFilterRule) {
        this.targetEntityFilterRule = targetEntityFilterRule;
    }

    public String getTargetEntityWithFilterRule() {
        return StringUtils.join(targetPackage, ":", targetEntity, targetEntityFilterRule);
    }

}
