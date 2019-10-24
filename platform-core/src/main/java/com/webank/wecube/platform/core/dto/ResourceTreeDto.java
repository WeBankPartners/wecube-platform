package com.webank.wecube.platform.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.webank.wecube.platform.core.support.cmdb.dto.v2.CiTypeAttrDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@NoArgsConstructor
@AllArgsConstructor
public class ResourceTreeDto {
    private Integer ciTypeId;
    private String guid;
    private String flag;
    private Object attrs;
    private Object data;

    private List<CiTypeAttrDto> referenceByAttributesWithBelongType;

    private List<ResourceTreeDto> children = new LinkedList<>();
}

