package com.webank.wecube.core.domain.plugin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wecube.core.utils.BooleanUtils;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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
