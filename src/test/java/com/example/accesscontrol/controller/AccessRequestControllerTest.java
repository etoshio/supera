package com.example.accesscontrol.controller;

import com.example.accesscontrol.dto.*;
import com.example.accesscontrol.service.AccessRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccessRequestControllerTest {

    private AccessRequestService service;
    private Authentication authentication;
    private AccessRequestController controller;

    @BeforeEach
    void setup() {
        service = mock(AccessRequestService.class);
        authentication = mock(Authentication.class);
        controller = new AccessRequestController(service);
    }

    // ---------------------------------------------------------
    // POST /api/requests
    // ---------------------------------------------------------

    @Test
    void deveCriarSolicitacaoComSucesso() {
        CreateAccessRequestDTO dto = new CreateAccessRequestDTO();
        AccessRequestDetailDTO expected = new AccessRequestDetailDTO();

        when(authentication.getName()).thenReturn("user@example.com");
        when(service.createRequest(eq(dto), eq("user@example.com"))).thenReturn(expected);

        ResponseEntity<AccessRequestDetailDTO> response = controller.create(dto, authentication);

        verify(authentication).getName();
        verify(service).createRequest(eq(dto), eq("user@example.com"));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expected, response.getBody());
    }

    // ---------------------------------------------------------
    // GET /api/requests
    // ---------------------------------------------------------

    @Test
    void deveBuscarSolicitacoesDoUsuario() {
        AccessRequestFilter filter = new AccessRequestFilter();
        AccessRequestSummaryDTO item = new AccessRequestSummaryDTO();
        Page<AccessRequestSummaryDTO> page = new PageImpl<>(List.of(item));

        when(authentication.getName()).thenReturn("user@example.com");
        when(service.searchRequests(eq(filter), eq("user@example.com"))).thenReturn(page);

        ResponseEntity<Page<AccessRequestSummaryDTO>> response =
                controller.search(filter, authentication);

        verify(authentication).getName();
        verify(service).searchRequests(eq(filter), eq("user@example.com"));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(page, response.getBody());
    }

    // ---------------------------------------------------------
    // GET /api/requests/{id}
    // ---------------------------------------------------------

    @Test
    void deveObterDetalheDaSolicitacao() {
        AccessRequestDetailDTO detail = new AccessRequestDetailDTO();

        when(authentication.getName()).thenReturn("user@example.com");
        when(service.getRequest(eq(10L), eq("user@example.com"))).thenReturn(detail);

        ResponseEntity<AccessRequestDetailDTO> response =
                controller.get(10L, authentication);

        verify(authentication).getName();
        verify(service).getRequest(eq(10L), eq("user@example.com"));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(detail, response.getBody());
    }

    // ---------------------------------------------------------
    // POST /api/requests/{id}/cancel
    // ---------------------------------------------------------

    @Test
    void deveCancelarSolicitacaoComSucesso() {
        CancelAccessRequestDTO dto = new CancelAccessRequestDTO();
        dto.setReason("Motivo válido");

        when(authentication.getName()).thenReturn("user@example.com");

        ResponseEntity<Void> response = controller.cancel(5L, dto, authentication);

        verify(authentication).getName();
        verify(service).cancelRequest(eq(5L), eq(dto), eq("user@example.com"));

        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());
    }
}
