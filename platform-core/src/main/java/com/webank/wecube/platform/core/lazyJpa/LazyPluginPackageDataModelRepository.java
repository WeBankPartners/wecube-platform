package com.webank.wecube.platform.core.lazyJpa;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.lazyDomain.plugin.LazyPluginPackageDataModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface LazyPluginPackageDataModelRepository extends JpaRepository<LazyPluginPackageDataModel, String> {

    @Query(value = "SELECT dataModel.id " +
            "FROM LazyPluginPackageDataModel dataModel " +
            "WHERE  dataModel.version = (SELECT max(dataModel.version ) from LazyPluginPackageDataModel dataModel WHERE dataModel.packageName=:packageName GROUP BY dataModel.packageName) AND dataModel.packageName=:packageName")
//    @EntityGraph(value = "overview-graph", type = EntityGraphType.FETCH)
    Optional<String> findLatestDataModelIdByPackageName(@Param("packageName") String packageName);

    @EntityGraph(value = "overview-graph", type = EntityGraphType.LOAD)
    Optional<LazyPluginPackageDataModel> findById(String id);
}
