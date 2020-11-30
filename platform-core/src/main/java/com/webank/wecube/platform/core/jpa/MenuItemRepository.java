//package com.webank.wecube.platform.core.jpa;
//
//import java.util.List;
//
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.CrudRepository;
//
//import com.webank.wecube.platform.core.domain.MenuItem;
//
//public interface MenuItemRepository extends CrudRepository<MenuItem, String> {
//
//    @Query("SELECT DISTINCT menu FROM MenuItem menu WHERE menu.parentCode is null")
//    List<MenuItem> findRootMenuItems();
//
//    MenuItem findByCode(String code);
//
//    Boolean existsByCode(String code);
//
////    @Query("SELECT DISTINCT menu FROM MenuItem menu JOIN menu.assignedRoles role WHERE role.roleId IN :roleIds")
////    List<MenuItem> findMenuItemsByRoles(Integer... roleIds);
//
//
//}
