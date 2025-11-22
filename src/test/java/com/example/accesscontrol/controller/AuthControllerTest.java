package com.example.accesscontrol.controller;

import com.example.accesscontrol.dto.LoginRequest;
import com.example.accesscontrol.dto.LoginResponse;
import com.example.accesscontrol.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private AuthService authService;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        authController = new AuthController(authService);
    }

    @Test
    void deveDelegarLoginParaAuthServiceERetornarOk() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("Password123!");

        LoginResponse expected = new LoginResponse("token123", "Bearer", 900);

        when(authService.login(request)).thenReturn(expected);

        ResponseEntity<LoginResponse> response = authController.login(request);

        // verifica delegação
        verify(authService).login(request);

        // verifica resposta HTTP
        assertEquals(200, response.getStatusCode().value());
        assertEquals("token123", response.getBody().getAccessToken());
        assertEquals("Bearer", response.getBody().getTokenType());
        assertEquals(900, response.getBody().getExpiresInSeconds());
    }
}
