package com.webank.wecube.platform.core.jpa.user;

import com.webank.wecube.platform.core.domain.RoleMenu;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author howechen
 */
public interface RoleMenuRepository extends CrudRepository<RoleMenu, String> {

    Optional<List<RoleMenu>> findAllByRoleName(String roleName);

    Boolean existsRoleMenuByRoleNameAndMenuCode(String roleName, String menuCode);

}
