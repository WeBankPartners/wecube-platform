package com.webank.wecube.core.domain.plugin;

import com.webank.wecube.core.domain.plugin.PluginConfig;
import lombok.Data;
import net.bytebuddy.build.Plugin;
import org.apache.ibatis.annotations.Many;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Entity
@Table(name = "plugin_model_entity")
public class PluginModelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "plugin_packages_id", insertable = false, updatable = false)
    private PluginPackage pluginPackage;

    @Column(name = "package_id")
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "plugin_packages_id", insertable = false, updatable = false)
    private Integer packageId;


    @Column(name = "description")
    private String description;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "pluginModelEntity")
    public List<PluginModelAttribute> pluginModelAttributeList;
}
