package com.webank.wecube.platform.core.repository.plugin;

import com.webank.wecube.platform.core.entity.plugin.PluginArtifactPullReq;

public interface PluginArtifactPullReqMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginArtifactPullReq record);

    int insertSelective(PluginArtifactPullReq record);

    PluginArtifactPullReq selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginArtifactPullReq record);

    int updateByPrimaryKey(PluginArtifactPullReq record);
}