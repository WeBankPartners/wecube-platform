package com.webank.wecube.platform.auth.server.service;

import com.webank.wecube.platform.auth.server.model.SysSubSystemInfo;

public interface SubSystemInfoDataService {
    SysSubSystemInfo retrieveSysSubSystemInfoWithSystemCode(String systemCode);
}
