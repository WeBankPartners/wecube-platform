package com.webank.wecube.platform.core.support.cmdb.dto.v2;

import lombok.Data;

@Data
public class OperateCiDto {
    private String guid;
    private int ciTypeId;

    public OperateCiDto(String guid, int ciTypeId) {
        this.guid = guid;
        this.ciTypeId = ciTypeId;
    }

    public OperateCiDto() {
    }
}
