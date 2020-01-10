package com.webank.wecube.platform.core.jpa.event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.core.entity.event.OperationEventEntity;

public interface OperationEventRepository extends JpaRepository<OperationEventEntity, Long> {

    @Query("select t from OperationEventEntity t where t.eventSeqNo = :eventSeqNo")
    List<OperationEventEntity> findAllByEventSeqNo(@Param("eventSeqNo") String eventSeqNo);
}
