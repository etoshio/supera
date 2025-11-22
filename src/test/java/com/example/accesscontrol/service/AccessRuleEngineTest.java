package com.example.accesscontrol.service;

import com.example.accesscontrol.model.*;
import com.example.accesscontrol.model.Module;
import com.example.accesscontrol.repository.UserModuleAccessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccessRuleEngineTest {

    private UserModuleAccessRepository userModuleAccessRepository;
    private AccessRuleEngine ruleEngine;

    @BeforeEach
    void setUp() {
        userModuleAccessRepository = mock(UserModuleAccessRepository.class);
        ruleEngine = new AccessRuleEngine(userModuleAccessRepository);
    }

    @Test
    void justificativaValidaNaoLancaExcecao() {
        String just = "Preciso do acesso ao módulo para executar minhas tarefas.";
        assertDoesNotThrow(() -> ruleEngine.validateJustification(just));
    }

    @Test
    void justificativaCurtaDeveLancarExcecao() {
        String just = "Muito curta";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ruleEngine.validateJustification(just));
        assertTrue(ex.getMessage().contains("Justificativa insuficiente"));
    }

    @Test
    void justificativaGenericaDeveLancarExcecao() {
        String just = "teste teste teste teste";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ruleEngine.validateJustification(just));
        assertTrue(ex.getMessage().contains("Justificativa insuficiente"));
    }

    @Test
    void departamentoSemPermissaoDeveLancarExcecao() {
        User user = User.builder()
                .id(1L)
                .department(Department.RH)
                .build();

        Module modFinanceiro = Module.builder()
                .id(3L)
                .name("Gestão Financeira")
                .allowedDepartments(Set.of(Department.FINANCEIRO, Department.TI))
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ruleEngine.validateDepartmentPermission(user, List.of(modFinanceiro)));

        assertTrue(ex.getMessage().contains("Departamento sem permissão"));
    }

    @Test
    void limiteDeModulosAtingidoDeveLancarExcecao() {
        User user = User.builder()
                .id(1L)
                .department(Department.FINANCEIRO)
                .build();

        UserModuleAccess a1 = UserModuleAccess.builder().id(1L).active(true).user(user).build();
        UserModuleAccess a2 = UserModuleAccess.builder().id(2L).active(true).user(user).build();
        UserModuleAccess a3 = UserModuleAccess.builder().id(3L).active(true).user(user).build();
        UserModuleAccess a4 = UserModuleAccess.builder().id(4L).active(true).user(user).build();
        UserModuleAccess a5 = UserModuleAccess.builder().id(5L).active(true).user(user).build();

        when(userModuleAccessRepository.findByUserAndActiveTrue(user))
                .thenReturn(List.of(a1, a2, a3, a4, a5));

        Module novo = Module.builder().id(10L).name("Novo").build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ruleEngine.validateModuleLimit(user, List.of(novo)));

        assertTrue(ex.getMessage().contains("Limite de módulos ativos atingido"));
        verify(userModuleAccessRepository).findByUserAndActiveTrue(user);
    }

    @Test
    void limiteMaiorParaDepartamentoTi() {
        User user = User.builder()
                .id(1L)
                .department(Department.TI)
                .build();

        // 10 ativos, pedindo mais 1 -> estoura
        List<UserModuleAccess> ativos = List.of(
                UserModuleAccess.builder().id(1L).active(true).user(user).build(),
                UserModuleAccess.builder().id(2L).active(true).user(user).build(),
                UserModuleAccess.builder().id(3L).active(true).user(user).build(),
                UserModuleAccess.builder().id(4L).active(true).user(user).build(),
                UserModuleAccess.builder().id(5L).active(true).user(user).build(),
                UserModuleAccess.builder().id(6L).active(true).user(user).build(),
                UserModuleAccess.builder().id(7L).active(true).user(user).build(),
                UserModuleAccess.builder().id(8L).active(true).user(user).build(),
                UserModuleAccess.builder().id(9L).active(true).user(user).build(),
                UserModuleAccess.builder().id(10L).active(true).user(user).build()
        );

        when(userModuleAccessRepository.findByUserAndActiveTrue(user))
                .thenReturn(ativos);

        Module novo = Module.builder().id(20L).name("Outro").build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ruleEngine.validateModuleLimit(user, List.of(novo)));

        assertTrue(ex.getMessage().contains("Limite de módulos ativos atingido"));
        verify(userModuleAccessRepository).findByUserAndActiveTrue(user);
    }

    @Test
    void moduloIncompativelComAtivoDeveLancarExcecao() {
        User user = User.builder()
                .id(1L)
                .department(Department.FINANCEIRO)
                .build();

        Module solicitante = Module.builder()
                .id(5L)
                .name("Solicitante Financeiro")
                .build();

        Module aprovador = Module.builder()
                .id(4L)
                .name("Aprovador Financeiro")
                .incompatibleModules(Set.of(solicitante)) // incompatibilidade no ATIVO
                .build();

        UserModuleAccess ativo = UserModuleAccess.builder()
                .id(1L)
                .user(user)
                .module(aprovador)
                .active(true)
                .build();

        when(userModuleAccessRepository.findByUserAndActiveTrue(user))
                .thenReturn(List.of(ativo));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ruleEngine.validateMutualExclusions(user, List.of(solicitante)));

        assertTrue(ex.getMessage().contains("Módulo incompatível"));
        verify(userModuleAccessRepository).findByUserAndActiveTrue(user);
    }

}
