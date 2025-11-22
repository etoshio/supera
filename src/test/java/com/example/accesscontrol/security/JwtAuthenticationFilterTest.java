package com.example.accesscontrol.security;

import com.example.accesscontrol.model.Department;
import com.example.accesscontrol.model.User;
import com.example.accesscontrol.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private UserRepository userRepository;
    private UserDetailsService userDetailsService;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userRepository = mock(UserRepository.class);
        userDetailsService = mock(UserDetailsService.class);
        filter = new JwtAuthenticationFilter(jwtService, userRepository, userDetailsService);
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveAutenticarQuandoTokenValido() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer token123");
        when(jwtService.extractUserId("token123")).thenReturn(5L);

        User user = User.builder()
                .id(5L)
                .email("user@example.com")
                .passwordHash("hash")
                .department(Department.TI)
                .build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("user@example.com")
                .password("hash")
                .authorities("USER")
                .build();

        when(userDetailsService.loadUserByUsername("user@example.com"))
                .thenReturn(userDetails);

        filter.doFilterInternal(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user@example.com",
                SecurityContextHolder.getContext().getAuthentication().getName());

        verify(jwtService).extractUserId("token123");
        verify(userRepository).findById(5L);
        verify(chain).doFilter(request, response);
    }

    @Test
    void deveIgnorarQuandoSemHeaderAuthorization() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, userRepository, userDetailsService);
        verify(chain).doFilter(request, response);
    }
}
