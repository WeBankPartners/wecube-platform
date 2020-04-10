package com.webank.wecube.platform.core.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.webank.wecube.platform.core.domain.SystemVariable;

public interface SystemVariableRepository extends CrudRepository<SystemVariable, String> {

    List<SystemVariable> findAllByStatus(String status);

    List<SystemVariable> findAllByScope(String scope);

    List<SystemVariable> findByNameAndScopeAndStatus(String name, String scope, String status);
    
    List<SystemVariable> findAllByScopeAndSource(String scope, String source);

    Optional<List<SystemVariable>> findBySource(String source);
    
    List<SystemVariable> findAllBySourceIn(List<String> sourceList);

    List<SystemVariable> findAllByScopeAndStatus(String scope, String status);

    @Query(value = "select distinct scope from system_variables", nativeQuery = true)
    List<String> findDistinctScope();
}
