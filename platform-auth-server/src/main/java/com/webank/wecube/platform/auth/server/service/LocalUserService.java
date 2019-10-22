package com.webank.wecube.platform.auth.server.service;

import com.webank.wecube.platform.auth.server.model.SysUser;

public interface LocalUserService {
    SysUser loadUserByUsername(String username);
}
