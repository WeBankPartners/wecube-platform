package com.webank.wecube.platform.core.domain.plugin;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "plugin_packages")
public class PluginPackage {

    @Id
    @GeneratedValue
    private Integer id;

    @Column
    private String name;
    @Column
    private String version;

    @Column
    private String dockerImageFile;

    @Column
    private String dockerImageRepository;

    @Column
    private String dockerImageTag;

    @Column
    private String containerPort;

    @Column
    private String containerConfigDirectory;

    @Column
    private String containerLogDirectory;

    @Column
    private String containerDataDirectory;

    @Column
    private String containerStartParam;

    @OneToMany(mappedBy = "pluginPackage", cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PluginConfig> pluginConfigs = new ArrayList<>();

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "pluginPackage")
    private List<PluginInstance> pluginInstances = new ArrayList<>();

    public void addPluginConfig(PluginConfig pluginConfig) {
        this.pluginConfigs.add(pluginConfig);
    }

    public void addPluginInstance(PluginInstance pluginInstance) {
        this.pluginInstances.add(pluginInstance);
    }

}
