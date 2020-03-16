package com.webank.wecube.platform.core.dto;

import java.util.List;
import java.util.Map;

public class FavoritesDto {
    private String favoritesId;
    private String collectionName;
    private Map<String, List<String>> permissionToRole;
    private String data;

    public FavoritesDto() {
    }

    public String getFavoritesId() {
        return favoritesId;
    }

    public void setFavoritesId(String favoritesId) {
        this.favoritesId = favoritesId;
    }

    public String getCollectionName() {
        return collectionName;
    }
    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public Map<String, List<String>> getPermissionToRole() {
        return permissionToRole;
    }

    public void setPermissionToRole(Map<String, List<String>> permissionToRole) {
        this.permissionToRole = permissionToRole;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
