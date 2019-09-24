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
@Table(name = "plugin_model_entity", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"package_name", "name"})
})
public class PluginModelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(name = "package_id")
    private Integer packageId;

    @Column(name = "package_version")
    private String packageVersion;

    @Column(name = "name")
    private String name;

    @Column(name = "package_name")
    private String packageName;

    @Column(name = "description")
    private String description;

    @Column(name = "state")
    private String state;

    @OneToMany(mappedBy = "pluginModelEntity")
    public List<PluginModelAttribute> pluginModelAttributeList;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", insertable = false, updatable = false)
    private PluginPackage pluginPackage;
}
