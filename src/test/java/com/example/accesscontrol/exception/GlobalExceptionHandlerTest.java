package com.example.accesscontrol.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveTratarBadCredentials() {
        BadCredentialsException ex = new BadCredentialsException("Erro auth");
        ResponseEntity<?> resp = handler.handleBadCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) resp.getBody();
        assertEquals("Erro auth", body.get("error"));
    }

    @Test
    void deveTratarValidacao() {
        BeanPropertyBindingResult result =
                new BeanPropertyBindingResult(new Object(), "obj");
        result.addError(new FieldError("obj", "campo", "mensagem de erro"));

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, result);

        ResponseEntity<?> resp = handler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) resp.getBody();
        assertTrue(body.containsKey("campo"));
    }

    @Test
    void deveTratarSecurityException() {
        SecurityException ex = new SecurityException("sem acesso");
        ResponseEntity<?> resp = handler.handleSecurity(ex);
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }

    @Test
    void deveTratarIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("arg");
        ResponseEntity<?> resp = handler.handleIllegalArgument(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void deveTratarIllegalState() {
        IllegalStateException ex = new IllegalStateException("state");
        ResponseEntity<?> resp = handler.handleIllegalState(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }
}
