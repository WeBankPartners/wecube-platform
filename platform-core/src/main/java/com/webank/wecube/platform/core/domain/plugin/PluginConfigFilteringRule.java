package com.webank.wecube.platform.core.domain.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "plugin_cfg_filter_rules")
public class PluginConfigFilteringRule {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "config_id")
    private PluginConfig pluginConfig;

    @Column
    private Integer cmdbAttributeId;
    @Column
    private String cmdbColumnName;
    @Column
    private String filteringValues;


}
