//package com.webank.wecube.platform.core.jpa;
//
//import java.util.List;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import com.webank.wecube.platform.core.domain.SystemVariable;
//
//public interface SystemVariableRepository extends JpaRepository<SystemVariable, String> {
//
//    List<SystemVariable> findAllByStatus(String status);
//
//    List<SystemVariable> findAllByScope(String scope);
//
//    List<SystemVariable> findByNameAndScopeAndStatus(String name, String scope, String status);
//
//    List<SystemVariable> findAllByScopeAndSource(String scope, String source);
//
//    List<SystemVariable> findBySource(String source);
//
//    List<SystemVariable> findAllBySourceIn(List<String> sourceList);
//
//    List<SystemVariable> findAllByScopeAndStatus(String scope, String status);
//
//    @Query("select t from SystemVariable t where t.name = :name and t.scope = :scope and t.source = :source")
//    List<SystemVariable> findByNameAndScopeAndSource(@Param("name") String name, @Param("scope") String scope,
//            @Param("source") String source);
//
//    @Query(value = "select distinct scope from system_variables", nativeQuery = true)
//    List<String> findDistinctScope();
//}
