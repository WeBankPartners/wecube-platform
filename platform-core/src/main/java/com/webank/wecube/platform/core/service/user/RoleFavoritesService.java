package com.webank.wecube.platform.core.service.user;

import com.webank.wecube.platform.core.dto.FavoritesDto;

import java.util.List;

public interface RoleFavoritesService {

    void createCollectionByRole(FavoritesDto favoritesDto);

    void deleteCollectionById(String favoritesId);

    void updateCollectionByRole(FavoritesDto favoritesDto);

    List<FavoritesDto> retrieveAllCollections();
}
