package com.webank.wecube.core.jpa;

import com.webank.wecube.core.domain.MenuItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MenuItemRepository extends CrudRepository<MenuItem, Integer> {

    @Query("SELECT DISTINCT menu FROM MenuItem menu WHERE menu.parentId is null")
    List<MenuItem> findRootMenuItems();

    MenuItem findByCode(String code);

    @Query("SELECT DISTINCT menu FROM MenuItem menu JOIN menu.assignedRoles role WHERE role.roleId IN :roleIds")
    List<MenuItem> findMenuItemsByRoles(Integer... roleIds);


}
