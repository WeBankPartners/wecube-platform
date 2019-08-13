package com.webank.wecube.core.support.cmdb.dto.v2;

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
public class CiTypeChildRelativeAttributesDto {
    private Integer attributeId;
    private String attributeName;
    private Integer ciTypeId;
}

