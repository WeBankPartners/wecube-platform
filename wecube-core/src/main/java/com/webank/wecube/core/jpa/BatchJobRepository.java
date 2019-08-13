package com.webank.wecube.core.jpa;
import com.webank.wecube.core.domain.BatchJob;
import com.webank.wecube.core.domain.BlobData;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface BatchJobRepository extends  CrudRepository<BatchJob, Integer> {

}