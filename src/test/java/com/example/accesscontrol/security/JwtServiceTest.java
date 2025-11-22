package com.example.accesscontrol.security;

import com.example.accesscontrol.model.Department;
import com.example.accesscontrol.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret",
                "YmVlbmRyZWFsdGVzdGUtamV0LXN1cGVrZXktY29tLXRhbWFuaG8tMzIy");
        ReflectionTestUtils.setField(jwtService, "expirationSeconds", 900L);
    }

    @Test
    void deveGerarETerUserIdNoToken() {
        User user = User.builder()
                .id(123L)
                .email("user@example.com")
                .department(Department.TI)
                .build();

        String token = jwtService.generateToken(user);
        assertNotNull(token);

        Long userId = jwtService.extractUserId(token);
        assertEquals(123L, userId);
    }
}
