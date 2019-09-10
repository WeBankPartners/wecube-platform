package com.webank.wecube.core.domain.plugin;

import com.webank.wecube.core.domain.plugin.PluginConfig;
import lombok.Data;
import net.bytebuddy.build.Plugin;
import org.apache.ibatis.annotations.Many;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Data
@Entity
@Table(name = "plugin_model_entity")
public class PluginModelEntity {
    @Id
    @NotBlank
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "package_id")
    @NotBlank
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "plugin_packages_id", insertable = false, updatable = false)
    private PluginPackage pluginPackage;

    @Column(name = "package_id")
    @NotBlank
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "plugin_packages_id", insertable = false, updatable = false)
    private Integer packageId;


    @Column(name = "description")
    private String description;

    @Column(name = "name")
    @NotBlank
    private String name;
}
