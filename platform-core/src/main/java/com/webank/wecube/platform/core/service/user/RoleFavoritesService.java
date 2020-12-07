package com.webank.wecube.platform.core.service.user;

import com.webank.wecube.platform.core.dto.plugin.FavoritesDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleRequestDto;

import java.util.List;

public interface RoleFavoritesService {

    void createCollectionByRole(FavoritesDto favoritesDto);

    void deleteCollectionById(String favoritesId);

    List<FavoritesDto> retrieveAllCollections();

    void deleteFavoritesRoleBinding(String favoritesId, ProcRoleRequestDto favoritesRoleRequestDto);

    void updateFavoritesRoleBinding(String favoritesId, ProcRoleRequestDto favoritesRoleRequestDto);

}
