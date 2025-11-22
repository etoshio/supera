package com.example.accesscontrol;

import com.example.accesscontrol.dto.LoginRequest;
import com.example.accesscontrol.dto.LoginResponse;
import com.example.accesscontrol.model.Department;
import com.example.accesscontrol.model.User;
import com.example.accesscontrol.repository.UserRepository;
import com.example.accesscontrol.security.JwtService;
import com.example.accesscontrol.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        userRepository = mock(UserRepository.class);
        jwtService = mock(JwtService.class);
        passwordEncoder = mock(PasswordEncoder.class);

        authService = new AuthService(authenticationManager, userRepository, jwtService, passwordEncoder);
    }

    @Test
    void deveAutenticarComSucesso() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("Password123!");

        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .passwordHash("hash")
                .department(Department.TI)
                .name("User")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("token123");

        LoginResponse resp = authService.login(req);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        UsernamePasswordAuthenticationToken token = captor.getValue();
        assertEquals("user@example.com", token.getPrincipal());
        assertEquals("Password123!", token.getCredentials());

        assertEquals("token123", resp.getAccessToken());
        assertEquals("Bearer", resp.getTokenType());
    }
}
