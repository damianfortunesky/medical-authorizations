package com.damian.medicalauthorization.infrastructure.persistence.entity;

public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
