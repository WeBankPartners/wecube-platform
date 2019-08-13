package com.webank.wecube.core.jpa;

import com.webank.wecube.core.domain.BlobData;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface BlobDataRepository extends CrudRepository<BlobData, Integer> {

    Optional<BlobData> findFirstByType(String type);
}
