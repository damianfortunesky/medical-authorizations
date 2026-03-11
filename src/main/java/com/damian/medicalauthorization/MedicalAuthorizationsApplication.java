package com.damian.medicalauthorization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MedicalAuthorizationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicalAuthorizationsApplication.class, args);
    }
}
