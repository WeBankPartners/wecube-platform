package com.webank.wecube.platform.core.support.cmdb.dto.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

@Data @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
@JsonInclude(Include.NON_EMPTY)
public class CategoryDto extends AbstractResourceDto {
	private Integer catId;
	private String catName;
	private String description;
	private Integer groupTypeId;
    private Integer catTypeId;
	private List<CatCodeDto> codes = new LinkedList<>();
	private CatTypeDto catType;
}
