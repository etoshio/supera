package com.example.accesscontrol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AccessControlApplicationTest {

    @Test
    void mainDeveExecutarSemErros() {
        assertDoesNotThrow(() ->
                AccessControlApplication.main(new String[]{})
        );
    }
}
