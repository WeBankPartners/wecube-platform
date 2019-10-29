package com.webank.wecube.platform.core.support.cmdb.dto.v2;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
@JsonInclude(Include.NON_EMPTY)
public class CatCodeDto extends AbstractResourceDto {
	private String code;
	private String value;
	private Integer seqNo;
	private Integer codeId;
	private String codeDescription;
	//groupCodeId can be integer (for update) or map (query result form cmdb)
	private Object groupCodeId;
	private String groupName;
	private Integer catId;
	private String status;

    private List<CiTypeDto> ciTypes = new ArrayList<>();
	private CategoryDto cat ;

}
