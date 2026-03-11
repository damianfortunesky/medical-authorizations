package com.damian.medicalauthorization.api.controller;

import com.damian.medicalauthorization.api.dto.AuthorizationResponse;
import com.damian.medicalauthorization.api.dto.CreateAuthorizationRequest;
import com.damian.medicalauthorization.application.port.AuthorizationUseCase;
import com.damian.medicalauthorization.domain.model.Authorization;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/authorizations")
public class AuthorizationController {

    private final AuthorizationUseCase authorizationUseCase;

    public AuthorizationController(AuthorizationUseCase authorizationUseCase) {
        this.authorizationUseCase = authorizationUseCase;
    }

    @PostMapping
    public ResponseEntity<AuthorizationResponse> createAuthorization(@Valid @RequestBody CreateAuthorizationRequest request) {
        Authorization created = authorizationUseCase.create(new Authorization(
                null,
                request.memberId(),
                request.providerId(),
                request.procedureCode(),
                null,
                null
        ));

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(toResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorizationResponse> getAuthorization(@PathVariable UUID id) {
        return authorizationUseCase.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    private AuthorizationResponse toResponse(Authorization authorization) {
        return new AuthorizationResponse(
                authorization.id(),
                authorization.memberId(),
                authorization.providerId(),
                authorization.procedureCode(),
                authorization.status(),
                authorization.createdAt()
        );
    }
}
