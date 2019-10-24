package com.webank.wecube.platform.core.support.cmdb.dto.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@NoArgsConstructor
@AllArgsConstructor
public class CiDataTreeDto {
    private Integer ciTypeId;
    private String rootGuid;
    private String guid;
    private Object data;
    private List<CiDataTreeDto> children = new LinkedList<>();
}

