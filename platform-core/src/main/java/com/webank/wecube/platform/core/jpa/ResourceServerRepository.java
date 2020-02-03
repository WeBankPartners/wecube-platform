package com.webank.wecube.platform.core.jpa;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.webank.wecube.platform.core.domain.ResourceServer;

public interface ResourceServerRepository extends CrudRepository<ResourceServer, String> {
    List<ResourceServer> findByHost(String host);

    List<ResourceServer> findByHostAndType(String host, String type);
}
