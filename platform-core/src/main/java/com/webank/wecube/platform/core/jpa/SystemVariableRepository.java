package com.webank.wecube.platform.core.jpa;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.webank.wecube.platform.core.domain.SystemVariable;

public interface SystemVariableRepository extends CrudRepository<SystemVariable, String> {
    
    List<SystemVariable> findAllByStatus(String status);

    List<SystemVariable> findAllByScopeType(String scopeType);

    List<SystemVariable> findAllByPluginPackage_IdAndNameAndScopeTypeAndStatus(String pluginPackageId, String name,
                                                                               String scopeType, String status);

    List<SystemVariable> findByNameAndScopeTypeAndStatus(String name, String scopeType, String status);

    List<SystemVariable> findAllByScopeTypeAndStatus(String scopeType, String status);

    List<SystemVariable> findAllByScopeTypeAndScopeValue(String scopeType, String scopeValue);

    List<SystemVariable> findAllByScopeTypeAndScopeValueAndStatus(String scopeType, String scopeValue, String status);

}
