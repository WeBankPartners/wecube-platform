package com.webank.wecube.platform.core.jpa;

import org.springframework.data.repository.CrudRepository;

import com.webank.wecube.platform.core.domain.ResourceServer;

public interface ResourceServerRepository extends CrudRepository<ResourceServer, Integer> {
    ResourceServer findOneByHost(String host);
}
