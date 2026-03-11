package com.damian.medicalauthorization.api.controller;

import com.damian.medicalauthorization.application.port.AuthorizationUseCase;
import com.damian.medicalauthorization.domain.model.Authorization;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthorizationController.class)
@Import(com.damian.medicalauthorization.configuration.SecurityConfiguration.class)
class AuthorizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorizationUseCase authorizationUseCase;

    @Test
    @WithAnonymousUser
    void shouldCreateAuthorization() throws Exception {
        UUID id = UUID.randomUUID();
        Authorization created = new Authorization(
                id,
                "12345678900",
                "MEM001",
                "PLAN001",
                "PRAC001",
                "PENDING",
                "corr-123",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
        when(authorizationUseCase.create(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(created);

        mockMvc.perform(post("/authorizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "patientDocument": "12345678900",
                                  "memberNumber": "MEM001",
                                  "planCode": "PLAN001",
                                  "practiceCode": "PRAC001"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.authorizationId").value(id.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }


    @Test
    @WithAnonymousUser
    void shouldReturn405WhenMethodIsNotAllowed() throws Exception {
        mockMvc.perform(put("/authorizations")
                        .header("X-Correlation-Id", "corr-405"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                .andExpect(jsonPath("$.path").value("/authorizations"))
                .andExpect(jsonPath("$.correlationId").value("corr-405"))
                .andExpect(jsonPath("$.details[0]").value("HttpRequestMethodNotSupportedException"));
    }

    @Test
    @WithAnonymousUser
    void shouldReturn404WhenAuthorizationNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(authorizationUseCase.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/authorizations/{id}", id))
                .andExpect(status().isNotFound());
    }
}
