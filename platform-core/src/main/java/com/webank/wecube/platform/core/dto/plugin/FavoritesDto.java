package com.webank.wecube.platform.core.dto.plugin;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class FavoritesDto {
    private String favoritesId;
    private String collectionName;
    private Map<String, List<String>> permissionToRole;
    private String data;
    private String createdBy;
    private Date createdTime;


    public FavoritesDto() {
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
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
