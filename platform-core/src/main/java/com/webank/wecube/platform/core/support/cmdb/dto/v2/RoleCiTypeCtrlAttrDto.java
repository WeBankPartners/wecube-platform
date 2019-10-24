package com.webank.wecube.platform.core.support.cmdb.dto.v2;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
public class RoleCiTypeCtrlAttrDto extends CiTypePermissions  {
    private Integer roleCiTypeCtrlAttrId;
    private Integer roleCiTypeId;

    private List<RoleCiTypeCtrlAttrConditionDto> conditions = new LinkedList<>();
}
