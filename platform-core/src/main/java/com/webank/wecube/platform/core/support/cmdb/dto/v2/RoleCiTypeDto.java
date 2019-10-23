package com.webank.wecube.platform.core.support.cmdb.dto.v2;

import lombok.*;

import java.util.LinkedList;
import java.util.List;

@Data @NoArgsConstructor
@EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
public class RoleCiTypeDto extends CiTypePermissions {

    private Integer roleCiTypeId;

    private Integer ciTypeId;
    private Integer roleId;
    private String ciTypeName;

    private List<RoleCiTypeCtrlAttrDto> roleCiTypeCtrlAttrs = new LinkedList<>();

    public RoleCiTypeDto(Integer roleId, Integer ciTypeId, String ciTypeName) {
        this.ciTypeId = ciTypeId;
        this.roleId = roleId;
        this.ciTypeName = ciTypeName;
    }

}
