package com.webank.wecube.core.jpa;

import org.springframework.data.repository.CrudRepository;

import com.webank.wecube.core.domain.ResourceItem;

public interface ResourceItemRepository extends CrudRepository<ResourceItem, Integer> {

}
