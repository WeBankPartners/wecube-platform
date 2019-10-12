package com.webank.wecube.core.jpa;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.webank.wecube.core.domain.SystemVariable;

public interface SystemVariableRepository extends CrudRepository<SystemVariable, Integer> {
    
    List<SystemVariable> findAllByStatus(String status);

    List<SystemVariable> findAllByScopeType(String scopeType);
    
    List<SystemVariable> findAllByScopeTypeAndStatus(String scopeType, String status);
    
    List<SystemVariable> findAllByScopeTypeAndScopeValue(String scopeType, String scopeValue);
    
    List<SystemVariable> findAllByScopeTypeAndScopeValueAndStatus(String scopeType, String scopeValue, String status);
    
}
