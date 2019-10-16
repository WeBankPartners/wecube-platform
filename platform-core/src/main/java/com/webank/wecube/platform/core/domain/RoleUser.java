package com.webank.wecube.platform.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleUser {
    private Integer roleUserId;
	private Integer roleId;
	private String userId;

    public RoleUser(Integer roleId, String userId) {
        this.roleId = roleId;
        this.userId = userId;
    }
}
