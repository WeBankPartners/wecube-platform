package com.webank.wecube.platform.core.jpa;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.webank.wecube.platform.core.domain.ResourceServer;

public interface ResourceServerRepository extends CrudRepository<ResourceServer, Integer> {
    List<ResourceServer> findByHost(String host);
}
