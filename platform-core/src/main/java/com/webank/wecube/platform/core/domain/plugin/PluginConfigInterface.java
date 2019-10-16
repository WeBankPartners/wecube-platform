package com.webank.wecube.platform.core.domain.plugin;

import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Data @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "plugin_cfg_interfaces")
public class PluginConfigInterface {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "config_id")
    private PluginConfig pluginConfig;

    @Column
    private String name;
    @Column
    private String serviceName;
    @Column
    private String serviceDisplayName;
    @Column
    private String path;
    @Column
    private Integer cmdbQueryTemplateId;
    @Column
    private String filterStatus;
    @Column
    private String resultStatus;

    @OneToMany(mappedBy = "pluginConfigInterface", cascade = CascadeType.ALL, orphanRemoval = true)
    @Where(clause = "type = 'INPUT'")
    private Set<PluginConfigInterfaceParameter> inputParameters = new LinkedHashSet<>();

    @OneToMany(mappedBy = "pluginConfigInterface", cascade = CascadeType.ALL, orphanRemoval = true)
    @Where(clause = "type = 'OUTPUT'")
    private Set<PluginConfigInterfaceParameter> outputParameters = new LinkedHashSet<>();

    public void addInputParameter(PluginConfigInterfaceParameter parameter) {
        this.inputParameters.add(parameter);
    }

    public void addOutputParameter(PluginConfigInterfaceParameter parameter) {
        this.outputParameters.add(parameter);
    }


}
