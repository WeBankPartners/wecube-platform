package com.webank.wecube.platform.core.domain.plugin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "package_id")
    private PluginPackage pluginPackage;

    @Column
    private String name;

    @Column
    private Integer cmdbCiTypeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "pluginConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PluginConfigInterface> interfaces = new ArrayList<>();

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "pluginConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PluginConfigFilteringRule> filteringRules = new ArrayList<>();

    public void addPluginConfigInterface(PluginConfigInterface pluginConfigInterface) {
        this.interfaces.add(pluginConfigInterface);
    }

    public void addPluginConfigFilteringRule(PluginConfigFilteringRule pluginConfigFilteringRule) {
        this.filteringRules.add(pluginConfigFilteringRule);
    }

    @JsonInclude
    @EqualsAndHashCode.Include
    @ToString.Include
    public Integer getPluginPackageId() {
        return pluginPackage == null ? null : pluginPackage.getId();
    }


}



