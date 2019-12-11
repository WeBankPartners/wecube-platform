package com.webank.wecube.platform.core.jpa.user;

import com.webank.wecube.platform.core.domain.RoleMenu;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author howechen
 */
public interface RoleMenuRepository extends CrudRepository<RoleMenu, String> {

    List<RoleMenu> findAllByRoleId(Long roleId);

}
