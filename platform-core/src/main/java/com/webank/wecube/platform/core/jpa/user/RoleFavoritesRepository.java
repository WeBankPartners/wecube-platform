package com.webank.wecube.platform.core.jpa.user;

import com.webank.wecube.platform.core.entity.workflow.FavoritesRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author howechen
 */
public interface RoleFavoritesRepository extends JpaRepository<FavoritesRoleEntity, String> {
    @Transactional
    void deleteByFavoritesId(String favoritesId);

    Optional<List<FavoritesRoleEntity>> findAllByFavoritesIdAndPermission(String favoritesId, FavoritesRoleEntity.permissionEnum mgmt);

    Optional<List<FavoritesRoleEntity>> findByRoleIdIn(List<String> currentUserRoleNameList);
}
