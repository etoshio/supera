package com.example.accesscontrol.controller;

import com.example.accesscontrol.dto.ModuleResponseDTO;
import com.example.accesscontrol.service.ModuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModuleControllerTest {

    private ModuleService moduleService;
    private ModuleController controller;

    @BeforeEach
    void setup() {
        moduleService = mock(ModuleService.class);
        controller = new ModuleController(moduleService);
    }

    @Test
    void deveListarTodosOsModulosComSucesso() {
        ModuleResponseDTO m1 = new ModuleResponseDTO();
        ModuleResponseDTO m2 = new ModuleResponseDTO();
        List<ModuleResponseDTO> expected = List.of(m1, m2);

        when(moduleService.listAllModules()).thenReturn(expected);

        ResponseEntity<List<ModuleResponseDTO>> response = controller.listModules();

        verify(moduleService).listAllModules();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expected, response.getBody());
    }
}
