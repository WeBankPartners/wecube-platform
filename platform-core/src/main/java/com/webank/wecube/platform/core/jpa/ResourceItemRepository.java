package com.webank.wecube.platform.core.jpa;

import org.springframework.data.repository.CrudRepository;

import com.webank.wecube.platform.core.domain.ResourceItem;

public interface ResourceItemRepository extends CrudRepository<ResourceItem, Integer> {

}
