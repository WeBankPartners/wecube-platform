//package com.webank.wecube.platform.core.jpa.user;
//
//import java.util.List;
//import java.util.Optional;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import com.webank.wecube.platform.core.domain.RoleMenu;
//
///**
// * @author howechen
// */
//public interface RoleMenuRepository extends JpaRepository<RoleMenu, String> {
//
//    Optional<List<RoleMenu>> findAllByRoleName(String roleName);
//
//    Boolean existsRoleMenuByRoleNameAndMenuCode(String roleName, String menuCode);
//    
//    @Query("select t from RoleMenu t where t.menuCode = :menuCode")
//    List<RoleMenu> findAllByMenuCode(@Param("menuCode") String menuCode);
//
//}
