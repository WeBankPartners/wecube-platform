package com.webank.wecube.platform.core.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.core.entity.PluginAuthEntity;

public interface PluginAuthRepository extends JpaRepository<PluginAuthEntity, String> {

    @Query("select t from PluginAuthEntity t where t.pluginConfigId = :pluginConfigId and t.permissionType =:permission")
    List<PluginAuthEntity> findAllByPluginConfigIdAndPermission(@Param("pluginConfigId") String pluginConfigId,
            @Param("permission")String permission);
}
