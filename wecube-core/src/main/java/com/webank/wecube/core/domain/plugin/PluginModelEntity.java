package com.webank.wecube.core.domain.plugin;

import com.webank.wecube.core.domain.plugin.PluginConfig;
import lombok.Data;
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
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "plugin_package_id", insertable = false, updatable = false)
    private PluginPackage pluginPackage;

    @Column(name = "description")
    private String description;

    @Column(name = "name")
    @NotBlank
    private String name;
}
