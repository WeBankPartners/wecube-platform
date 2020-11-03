package com.webank.wecube.platform.core.jpa.workflow;

import com.webank.wecube.platform.core.entity.workflow.CoreRuGraphNode;
import com.webank.wecube.platform.core.entity.workflow.CoreRuGraphNodeWithBLOBs;

public interface CoreRuGraphNodeMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CoreRuGraphNodeWithBLOBs record);

    int insertSelective(CoreRuGraphNodeWithBLOBs record);

    CoreRuGraphNodeWithBLOBs selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CoreRuGraphNodeWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(CoreRuGraphNodeWithBLOBs record);

    int updateByPrimaryKey(CoreRuGraphNode record);
}