package com.webank.wecube.core.domain.plugin;


import com.webank.wecube.core.utils.constant.DataModelDataType;
import com.webank.wecube.core.utils.constant.DataModelState;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "plugin_model_entity", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"package_id", "name"})
})
public class PluginModelEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "package_id")
    private PluginPackage pluginPackage;

    @Column(name = "name")
    private String name;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "description")
    private String description;

    @Column(name = "state")
    private DataModelState state = DataModelState.Draft;

    @OneToMany(mappedBy = "pluginModelEntity", cascade = CascadeType.ALL)
    private List<PluginModelAttribute> pluginModelAttributeList;

    public PluginModelEntity() {
    }

    public PluginModelEntity(PluginPackage pluginPackage,
                             String name,
                             String displayName,
                             String description,
                             String state) {
        this.pluginPackage = pluginPackage;
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.state = DataModelState.fromCode(state);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state.getCode();
    }

    public void setState(String state) {
        this.state = DataModelState.fromCode(state);
    }

    public List<PluginModelAttribute> getPluginModelAttributeList() {
        return pluginModelAttributeList;
    }

    public void setPluginModelAttributeList(List<PluginModelAttribute> pluginModelAttributeList) {
        this.pluginModelAttributeList = pluginModelAttributeList;
    }
}
