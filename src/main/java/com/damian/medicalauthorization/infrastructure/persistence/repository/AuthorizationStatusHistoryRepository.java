package com.damian.medicalauthorization.infrastructure.persistence.repository;

import com.damian.medicalauthorization.infrastructure.persistence.entity.AuthorizationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuthorizationStatusHistoryRepository extends JpaRepository<AuthorizationStatusHistory, UUID> {
}
