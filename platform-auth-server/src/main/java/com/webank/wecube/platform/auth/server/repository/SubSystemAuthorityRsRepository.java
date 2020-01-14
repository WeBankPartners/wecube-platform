package com.webank.wecube.platform.auth.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.auth.server.entity.SubSystemAuthorityRsEntity;

public interface SubSystemAuthorityRsRepository extends JpaRepository<SubSystemAuthorityRsEntity, String> {
    
    @Query("select t from SubSystemAuthorityRsEntity t where t.subSystemId = :subSystemId and t.deleted = false")
    List<SubSystemAuthorityRsEntity> findAllBySubSystemId(@Param("subSystemId") String subSystemId);

}
