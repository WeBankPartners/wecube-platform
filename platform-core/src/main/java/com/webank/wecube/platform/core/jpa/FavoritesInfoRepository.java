//package com.webank.wecube.platform.core.jpa;
//
//import com.webank.wecube.platform.core.entity.workflow.FavoritesEntity;
//import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface FavoritesInfoRepository extends JpaRepository<FavoritesEntity, String> {
//    List<FavoritesEntity> findAllCollectionByCollectionName(String collectionName);
//
//    // @Transactional
//    // @Modifying
//    // @Query("delete from ProcessDefInfoEntity t where t.id = :id")
//    // void deleteByEntityId(@Param("id") String id);
//}
