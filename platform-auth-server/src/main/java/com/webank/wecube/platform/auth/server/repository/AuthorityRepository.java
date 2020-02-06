package com.webank.wecube.platform.auth.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.auth.server.entity.SysAuthorityEntity;

public interface AuthorityRepository extends JpaRepository<SysAuthorityEntity, String> {

    @Query("select t from SysAuthorityEntity t where t.code = :code and t.deleted = false ")
    SysAuthorityEntity findNotDeletedOneByCode(@Param("code") String code);
    
    @Query("select t from SysAuthorityEntity t where t.deleted = false ")
    List<SysAuthorityEntity> findAllNotDeletedAuthorities();
}
