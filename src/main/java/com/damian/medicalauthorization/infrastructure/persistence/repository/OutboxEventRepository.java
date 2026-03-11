package com.damian.medicalauthorization.infrastructure.persistence.repository;

import com.damian.medicalauthorization.infrastructure.persistence.entity.OutboxEvent;
import com.damian.medicalauthorization.infrastructure.persistence.entity.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus status);
}
