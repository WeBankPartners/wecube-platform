package com.webank.wecube.platform.core.support.cmdb.dto.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data @JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IdcDesignResourceDto {
    private String name;
    private String guid;

    private List<IdcDesignResourceDto> children = new LinkedList<>();
}

