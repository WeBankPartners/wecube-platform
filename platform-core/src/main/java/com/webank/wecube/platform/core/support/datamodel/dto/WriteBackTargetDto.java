package com.webank.wecube.platform.core.support.datamodel.dto;

import java.util.List;

import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;

public class WriteBackTargetDto {
    List<CommonResponseDto> lastRequestResponse;
    String writeBackPackageName;
    String writeBackEntityName;
    String writeBackAttributeName;

    public WriteBackTargetDto(List<CommonResponseDto> lastRequestResponse, String writeBackPackageName, String writeBackEntityName, String writeBackAttributeName) {
        this.lastRequestResponse = lastRequestResponse;
        this.writeBackPackageName = writeBackPackageName;
        this.writeBackEntityName = writeBackEntityName;
        this.writeBackAttributeName = writeBackAttributeName;
    }

    public List<CommonResponseDto> getLastRequestResponse() {
        return lastRequestResponse;
    }

    public String getWriteBackPackageName() {
        return writeBackPackageName;
    }

    public String getWriteBackEntityName() {
        return writeBackEntityName;
    }

    public String getWriteBackAttributeName() {
        return writeBackAttributeName;
    }
}
