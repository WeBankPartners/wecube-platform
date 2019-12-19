package com.webank.wecube.platform.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class User {
    private String userId;
    private String username;
    private String fullName;
    private String description;

}
