package com.webank.wecube.core.jpa;

import com.webank.wecube.core.domain.plugin.PluginModelEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PluginModelEntityRepository extends CrudRepository<PluginModelEntity, Integer> {

}
