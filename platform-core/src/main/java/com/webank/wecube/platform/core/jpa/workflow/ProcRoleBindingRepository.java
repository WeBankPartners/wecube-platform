package com.webank.wecube.platform.core.jpa.workflow;

import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProcRoleBindingRepository extends CrudRepository<ProcRoleBindingEntity, String> {

    List<ProcRoleBindingEntity> findAllByRoleId(String roleId);

    List<ProcRoleBindingEntity> findAllByRoleIdAndPermission(String roleId, ProcRoleBindingEntity.permissionEnum permission);

    List<ProcRoleBindingEntity> findAllByProcId(String procId);

    void deleteByProcId(String procId);

    Optional<ProcRoleBindingEntity> findByProcIdAndRoleIdAndPermission(String procId, String roleId, ProcRoleBindingEntity.permissionEnum permissionEnum);

    Optional<List<ProcRoleBindingEntity>> findAllByProcIdAndPermission(String procId, ProcRoleBindingEntity.permissionEnum permissionEnum);

    void deleteByProcIdAndRoleIdAndPermission(String procId, Long roleId, ProcRoleBindingEntity.permissionEnum permissionEnum);

    @Query(value = "select distinct proc_id from core_ru_proc_role_binding where role_id in (:roleIds) and permission = 'USE'", nativeQuery = true)
    List<String> findDistinctProcIdByRoleIdsAndPermissionIsUse(@Param("roleIds") List<String> roleIds);

    void deleteByProcIdAndRoleIdAndPermission(String procId, String roleId, ProcRoleBindingEntity.permissionEnum permissionEnum);
}
