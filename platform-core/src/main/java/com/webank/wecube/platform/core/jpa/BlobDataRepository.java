package com.webank.wecube.platform.core.jpa;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.webank.wecube.platform.core.domain.BlobData;

public interface BlobDataRepository extends CrudRepository<BlobData, Integer> {

    Optional<BlobData> findFirstByType(String type);
}
