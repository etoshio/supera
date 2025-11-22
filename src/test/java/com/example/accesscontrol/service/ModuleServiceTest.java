package com.example.accesscontrol.service;

import com.example.accesscontrol.dto.ModuleResponseDTO;
import com.example.accesscontrol.model.Department;
import com.example.accesscontrol.model.Module;
import com.example.accesscontrol.repository.ModuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModuleServiceTest {

    private ModuleRepository moduleRepository;
    private ModuleService moduleService;

    @BeforeEach
    void setUp() {
        moduleRepository = mock(ModuleRepository.class);
        moduleService = new ModuleService(moduleRepository);
    }

    @Test
    void deveListarModulos() {
        Module m1 = Module.builder()
                .id(1L)
                .code("PORTAL")
                .name("Portal")
                .description("Portal")
                .allowedDepartments(Set.of(Department.TI, Department.RH))
                .active(true)
                .build();

        Module m2 = Module.builder()
                .id(2L)
                .code("FIN")
                .name("Financeiro")
                .description("Financeiro")
                .allowedDepartments(Set.of(Department.FINANCEIRO))
                .active(false)
                .build();

        when(moduleRepository.findAll()).thenReturn(List.of(m1, m2));

        List<ModuleResponseDTO> result = moduleService.listAllModules();

        verify(moduleRepository).findAll();
        assertEquals(2, result.size());

        ModuleResponseDTO dto1 = result.get(0);
        assertEquals(1L, dto1.getId());
        assertTrue(dto1.isActive());
        assertTrue(dto1.getAllowedDepartments().contains(Department.TI));
    }
}
