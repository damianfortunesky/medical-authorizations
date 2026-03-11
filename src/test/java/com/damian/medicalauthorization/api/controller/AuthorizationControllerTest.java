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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
        Authorization created = new Authorization(id, "MEM001", "PROV001", "PROC001", "RECEIVED", OffsetDateTime.now());
        when(authorizationUseCase.create(any())).thenReturn(created);

        mockMvc.perform(post("/api/v1/authorizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "memberId": "MEM001",
                                  "providerId": "PROV001",
                                  "procedureCode": "PROC001"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithAnonymousUser
    void shouldReturn404WhenAuthorizationNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(authorizationUseCase.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/authorizations/{id}", id))
                .andExpect(status().isNotFound());
    }
}
