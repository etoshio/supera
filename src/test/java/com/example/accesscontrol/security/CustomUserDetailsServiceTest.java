package com.example.accesscontrol.security;

import com.example.accesscontrol.model.Department;
import com.example.accesscontrol.model.User;
import com.example.accesscontrol.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    private UserRepository userRepository;
    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        service = new CustomUserDetailsService(userRepository);
    }

    @Test
    void deveCarregarUsuarioPorEmail() {
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .passwordHash("hash")
                .department(Department.TI)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        var details = service.loadUserByUsername("user@example.com");

        verify(userRepository).findByEmail("user@example.com");
        assertEquals("user@example.com", details.getUsername());
        assertEquals("hash", details.getPassword());
    }

    @Test
    void deveLancarQuandoUsuarioNaoEncontrado() {
        when(userRepository.findByEmail("naoexiste@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("naoexiste@example.com"));

        verify(userRepository).findByEmail("naoexiste@example.com");
    }
}
