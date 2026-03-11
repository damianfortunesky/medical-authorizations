package com.damian.medicalauthorization.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PersistenceProperties.class)
public class PersistenceConfiguration {
}
