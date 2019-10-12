package com.webank.wecube.core.domain.plugin;

import com.webank.wecube.core.utils.constant.DataModelDataType;
import com.webank.wecube.core.utils.constant.DataModelState;

import javax.persistence.*;

@Entity
@Table(name = "plugin_package_attribute", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"entity_id", "name"})
})
public class PluginModelAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "entity_id")
    private PluginModelEntity pluginModelEntity;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "reference_id")
    private PluginModelAttribute pluginModelAttribute;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "data_type")
    private String dataType;

    @Column(name = "state")
    private String state = DataModelState.Draft.getCode();


    public PluginModelAttribute() {
    }

    public PluginModelAttribute(PluginModelEntity pluginModelEntity,
                                PluginModelAttribute pluginModelAttribute,
                                String name,
                                String description,
                                String dataType,
                                String state) {
        this.pluginModelEntity = pluginModelEntity;
        this.pluginModelAttribute = pluginModelAttribute;
        this.name = name;
        this.description = description;
        this.dataType = DataModelDataType.fromCode(dataType).getCode();
        this.state = DataModelState.fromCode(state).getCode();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PluginModelEntity getPluginModelEntity() {
        return pluginModelEntity;
    }

    public void setPluginModelEntity(PluginModelEntity pluginModelEntity) {
        this.pluginModelEntity = pluginModelEntity;
    }

    public PluginModelAttribute getPluginModelAttribute() {
        return pluginModelAttribute;
    }

    public void setPluginModelAttribute(PluginModelAttribute pluginModelAttribute) {
        this.pluginModelAttribute = pluginModelAttribute;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDataType() {
        return dataType.toLowerCase();
    }

    public void setDataType(String dataType) {
        this.dataType = DataModelDataType.fromCode(dataType).getCode();
    }

    public String getState() {
        return state.toLowerCase();
    }

    public void setState(String state) {
        this.state = DataModelState.fromCode(state).getCode();
    }
}
