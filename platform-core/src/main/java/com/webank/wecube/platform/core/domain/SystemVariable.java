package com.webank.wecube.platform.core.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "system_variables")
public class SystemVariable {
    
    public static final String ACTIVE = "active";
    public static final String INACTIVE = "inactive";
    
    public static final String SCOPE_TYPE_GLOBAL = "global";
    public static final String SCOPE_TYPE_PLUGIN_PACKAGE = "plugin-package";

    @Id
    @GeneratedValue
    private Integer id;

    @Column
    private String name;
    @Column
    private String value;
    @Column
    private String scopeType;
    @Column
    private String scopeValue;
    @Column
    private Integer seqNo;
    @Column
    private String status;
}
