//package com.webank.wecube.platform.core.entity.workflow;
//
//import com.webank.wecube.platform.core.dto.FavoritesDto;
//import com.webank.wecube.platform.core.entity.BaseTraceableEntity;
//
//import javax.persistence.*;
//import java.util.List;
//import java.util.Map;
//
//@Entity
//@Table(name = "FAVORITES")
//public class FavoritesEntity extends BaseTraceableEntity {
//    @Id
//    private String favoritesId;
//    @Column
//    private String collectionName;
//    @Lob
//    private byte[] data;
//
//    public FavoritesEntity() {
//    }
//
//    public String getFavoritesId() {
//        return favoritesId;
//    }
//
//    public void setFavoritesId(String favoritesId) {
//        this.favoritesId = favoritesId;
//    }
//
//    public String getCollectionName() {
//        return collectionName;
//    }
//
//    public void setCollectionName(String collectionName) {
//        this.collectionName = collectionName;
//    }
//
//    public byte[] getData() {
//        return data;
//    }
//
//    public void setData(byte[] data) {
//        this.data = data;
//    }
//
//    public static FavoritesDto fromDomain(Map<String, List<String>> permissionToRole, FavoritesEntity favoritesEntity) {
//        FavoritesDto result = new FavoritesDto();
//        result.setFavoritesId(favoritesEntity.getFavoritesId());
//        result.setData(new String(favoritesEntity.getData()));
//        result.setCollectionName(favoritesEntity.getCollectionName());
//        result.setPermissionToRole(permissionToRole);
//        result.setCreatedBy(favoritesEntity.getCreatedBy());
//        result.setCreatedTime(favoritesEntity.getCreatedTime());
//        return result;
//    }
//}
