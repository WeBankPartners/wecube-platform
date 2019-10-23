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
public class CatTypeDto extends AbstractResourceDto {
	private Integer catTypeId;
    private Integer ciTypeId;
	private String catTypeName;
	private String description;
	private List<CategoryDto> cats = new LinkedList<>();

}
