package com.webank.wecube.platform.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Role {
    public final static String ADMIN = "ADMIN";
    public final static String REGULAR_USER = "REGULAR_USER";
    public final static String READ_ONLY_USER = "READ_ONLY_USER";

    private Integer roleId;
    private String roleName;
    private String roleType;
    private String description;
}
