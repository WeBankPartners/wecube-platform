package com.webank.wecube.platform.core.jpa.workflow;

import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProcRoleBindingRepository extends CrudRepository<ProcRoleBindingEntity, String> {

    List<ProcRoleBindingEntity> findAllByRoleIdAndPermission(Long roleId, ProcRoleBindingEntity.permissionEnum permission);
}
