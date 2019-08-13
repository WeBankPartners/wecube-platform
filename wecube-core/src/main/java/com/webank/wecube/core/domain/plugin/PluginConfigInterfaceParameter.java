package com.webank.wecube.core.domain.plugin;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.persistence.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "plugin_cfg_inf_parameters")
public class PluginConfigInterfaceParameter {

    public static final String TYPE_INPUT = "INPUT";
    public static final String TYPE_OUTPUT = "OUTPUT";

    @Id
    @GeneratedValue
    private Integer id;

    @JsonIgnore @EqualsAndHashCode.Exclude @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "interface_id")
    private PluginConfigInterface pluginConfigInterface;

    @Column
    private String type;
    @Column
    private String name;
    @Column
    private String datatype;
    @Column
    private String cmdbColumnName;
    @Column
    private String cmdbColumnSource;
    @Column
    private Integer cmdbCitypeId;
    @Column
    private Integer cmdbAttributeId;
    @Column
    private String cmdbCitypePath;

    @JsonInclude
    @ToString.Include @EqualsAndHashCode.Include
    public Integer getInterfaceId() {
        return pluginConfigInterface == null ? null : pluginConfigInterface.getId();
    }
}
