package com.damian.medicalauthorization.infrastructure.persistence.repository;

import com.damian.medicalauthorization.infrastructure.persistence.entity.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, String> {
}
