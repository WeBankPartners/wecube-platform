package com.webank.wecube.platform.core.jpa.workflow;

import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ProcRoleBindingRepository extends CrudRepository<ProcRoleBindingEntity, String> {

    List<ProcRoleBindingEntity> findAllByRoleId(String roleId);

    List<ProcRoleBindingEntity> findAllByRoleIdAndPermission(String roleId, ProcRoleBindingEntity.permissionEnum permission);

    List<ProcRoleBindingEntity> findAllByProcId(String procId);

    void deleteByProcId(String procId);

    Optional<ProcRoleBindingEntity> findByProcIdAndRoleIdAndPermission(String procId, String roleId, ProcRoleBindingEntity.permissionEnum permissionEnum);

    Optional<List<ProcRoleBindingEntity>> findByProcIdAndPermission(String procId, ProcRoleBindingEntity.permissionEnum permissionEnum);

    void deleteByProcIdAndRoleIdAndPermission(String procId, String roleId, ProcRoleBindingEntity.permissionEnum permissionEnum);
}
