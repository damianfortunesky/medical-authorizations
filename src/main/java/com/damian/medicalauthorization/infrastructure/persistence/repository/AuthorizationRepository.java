package com.damian.medicalauthorization.infrastructure.persistence.repository;

import com.damian.medicalauthorization.infrastructure.persistence.entity.Authorization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuthorizationRepository extends JpaRepository<Authorization, UUID> {
}
