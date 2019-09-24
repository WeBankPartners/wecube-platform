package com.webank.wecube.core.domain.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "plugin_model_attr", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"package_name", "entity_name", "name"})
})
public class PluginModelAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "entity_id")
    private Integer entityId;

    @Column(name = "name")
    private String name;

    @Column(name = "entity_name")
    private String entityName;

    @Column(name = "package_name")
    private String packageName;

    @Column(name = "description")
    private String description;

    @Column(name = "data_type", length = 32)
    private String dataType;


    @Column(name = "state")
    private String state;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id", insertable = false, updatable = false)
    private PluginModelEntity pluginModelEntity;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "id", insertable = false, updatable = false)
    private PluginModelAttribute pluginModelAttribute;

    @OneToMany(mappedBy = "pluginModelAttribute")
    public List<PluginModelAttribute> pluginModelAttributeList;
}
