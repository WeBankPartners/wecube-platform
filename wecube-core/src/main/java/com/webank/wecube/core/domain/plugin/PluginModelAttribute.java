package com.webank.wecube.core.domain.plugin;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "plugin_model_attr")
public class PluginModelAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "plugin_model_entity_id", insertable = false, updatable = false)
    private PluginModelEntity pluginModelEntity;

    @Column(name = "entity_id")
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "plugin_model_entity_id", insertable = false, updatable = false)
    private Integer pluginModelEntityId;

    @Column(name = "description")
    private String description;

    @Column(name = "name")
    private String name;

    @Column(name = "input_type", length = 32)
    private String inputType;

    @Column(name = "reference_id")
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "plugin_model_attr_id", insertable = false, updatable = false)
    private PluginModelAttribute pluginModelAttribute;

    @Column(name = "reference_id")
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "plugin_model_attr_id", insertable = false, updatable = false)
    private Integer referenceId;
}
