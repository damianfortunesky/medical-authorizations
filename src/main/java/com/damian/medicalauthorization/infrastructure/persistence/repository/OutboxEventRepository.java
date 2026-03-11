package com.damian.medicalauthorization.infrastructure.persistence.repository;

import com.damian.medicalauthorization.infrastructure.persistence.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
}
