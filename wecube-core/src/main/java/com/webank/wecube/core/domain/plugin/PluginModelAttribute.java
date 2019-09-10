package com.webank.wecube.core.domain.plugin;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Data
@Entity
@Table(name = "plugin_model_attr")
public class PluginModelAttribute {
    @Id
    @NotBlank
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "entity_id")
    @NotBlank
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "plugin_model_entity_id", insertable = false, updatable = false)
    private PluginModelEntity pluginModelEntity;

    @Column(name = "description")
    private String description;

    @Column(name = "name")
    @NotBlank
    private String name;

    @Column(name = "input_type", length = 32)
    @NotBlank
    private String inputType;

    @Column(name = "reference_id")
    @OneToOne
    @JoinColumn(name = "plugin_model_attr_id", insertable = false, updatable = false)
    private PluginModelAttribute pluginModelAttribute;
}
