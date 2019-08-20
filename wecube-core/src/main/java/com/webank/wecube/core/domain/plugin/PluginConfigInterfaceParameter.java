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
    
    public static final String MAPPING_TYPE_CMDB_CI_TYPE = "CMDB_CI_TYPE";
    public static final String MAPPING_TYPE_CMDB_ENUM_CODE = "CMDB_ENUM_CODE";
    public static final String MAPPING_TYPE_RUNTIME = "RUNTIME";

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
    private String mappingType;
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
    @Column
    private Integer cmdbEnumCode;

    @JsonInclude
    @ToString.Include @EqualsAndHashCode.Include
    public Integer getInterfaceId() {
        return pluginConfigInterface == null ? null : pluginConfigInterface.getId();
    }

}
