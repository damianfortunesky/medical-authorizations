package com.damian.medicalauthorization.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI medicalAuthorizationOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Medical Authorization API")
                        .description("Production-style scaffold for medical authorization workflows")
                        .version("v1")
                        .license(new License().name("Proprietary")));
    }
}
