package com.webank.wecube.platform.auth.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.auth.server.entity.SysSubSystemEntity;

public interface SubSystemRepository extends JpaRepository<SysSubSystemEntity, String> {
    
    @Query("select t from SysSubSystemEntity t " + " where t.systemCode = :systemCode ")
	SysSubSystemEntity findOneBySystemCode(@Param("systemCode")String systemCode);
    
    @Query("select t from SysSubSystemEntity t " + " where t.name = :name ")
    SysSubSystemEntity findOneBySystemName(@Param("name")String name);
}
