package com.webank.wecube.platform.core.support.cmdb.dto.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RoleCiTypeCtrlAttrConditionDto extends AbstractResourceDto {
    private Integer conditionId;
    private Integer roleCiTypeCtrlAttrId;
    private Integer ciTypeAttrId;
    private String ciTypeAttrName;
    private String conditionValue;
    private String conditionValueType;

    private Object conditionValueObject;


}
