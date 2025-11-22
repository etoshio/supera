package com.example.accesscontrol.controller;

import com.example.accesscontrol.dto.*;
import com.example.accesscontrol.service.AccessRequestService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class AccessRequestController {

    private final AccessRequestService accessRequestService;

    @PostMapping
    @Operation(summary = "Cria nova solicitação de acesso a módulos")
    public ResponseEntity<AccessRequestDetailDTO> create(
            @Valid @RequestBody CreateAccessRequestDTO dto,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(accessRequestService.createRequest(dto, email));
    }

    @GetMapping
    @Operation(summary = "Consulta solicitações do usuário autenticado")
    public ResponseEntity<Page<AccessRequestSummaryDTO>> search(
            AccessRequestFilter filter,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(accessRequestService.searchRequests(filter, email));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhes de uma solicitação do usuário autenticado")
    public ResponseEntity<AccessRequestDetailDTO> get(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(accessRequestService.getRequest(id, email));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancela uma solicitação ativa")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @Valid @RequestBody CancelAccessRequestDTO dto,
            Authentication authentication
    ) {
        String email = authentication.getName();
        accessRequestService.cancelRequest(id, dto, email);
        return ResponseEntity.noContent().build();
    }
}
