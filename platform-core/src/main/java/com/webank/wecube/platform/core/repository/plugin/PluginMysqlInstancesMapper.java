package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.PluginMysqlInstances;

@Repository
public interface PluginMysqlInstancesMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginMysqlInstances record);

    int insertSelective(PluginMysqlInstances record);

    PluginMysqlInstances selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginMysqlInstances record);

    int updateByPrimaryKey(PluginMysqlInstances record);

    /**
     * 
     * @param packageName
     * @param status
     * @return
     */
    List<PluginMysqlInstances> selectAllByPackageNameAndStatus(@Param("packageName") String packageName,
            @Param("status") String status);
}