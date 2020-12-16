package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;

@Repository
public interface ProcRoleBindingMapper {

    int deleteByPrimaryKey(String id);

    int insert(ProcRoleBindingEntity record);

    int insertSelective(ProcRoleBindingEntity record);

    ProcRoleBindingEntity selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ProcRoleBindingEntity record);

    int updateByPrimaryKey(ProcRoleBindingEntity record);

    /**
     * 
     * @param roleName
     * @return
     */
    List<ProcRoleBindingEntity> findAllByRoleName(@Param("roleName") String roleName);

    /**
     * 
     * @param roleName
     * @param permission
     * @return
     */
    List<ProcRoleBindingEntity> findAllByRoleNameAndPermission(@Param("roleName") String roleName,
            @Param("permission") String permission);

    /**
     * 
     * @param procId
     * @return
     */
    List<ProcRoleBindingEntity> findAllByProcId(@Param("procId") String procId);

    /**
     * 
     * 
     * @param procId
     * @param roleName
     * @param permission
     * @return
     */
    ProcRoleBindingEntity findByProcIdAndRoleNameAndPermission(@Param("procId") String procId,
            @Param("roleName") String roleName, @Param("permission") String permission);

    /**
     * 
     * @param procId
     * @param permission
     * @return
     */
    List<ProcRoleBindingEntity> findAllByProcIdAndPermission(@Param("procId") String procId,
            @Param("permission") String permission);

    /**
     * 
     * @param roleIds
     * @return
     */
    List<ProcRoleBindingEntity> findDistinctProcIdByRolesAndPermissionIsUse(@Param("roleNames") Set<String> roleNames);

    /**
     * 
     * 
     * @param procId
     * @param roleId
     * @param permission
     */
    void deleteByProcIdAndRoleIdAndPermission(@Param("procId") String procId, @Param("roleId") String roleId,
            @Param("permission") String permission);
}
