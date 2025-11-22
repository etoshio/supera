package com.example.accesscontrol.service;

import com.example.accesscontrol.dto.*;
import com.example.accesscontrol.model.*;
import com.example.accesscontrol.model.Module;
import com.example.accesscontrol.repository.AccessRequestRepository;
import com.example.accesscontrol.repository.ModuleRepository;
import com.example.accesscontrol.repository.UserModuleAccessRepository;
import com.example.accesscontrol.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccessRequestServiceTest {

    private AccessRequestRepository accessRequestRepository;
    private ModuleRepository moduleRepository;
    private UserRepository userRepository;
    private UserModuleAccessRepository userModuleAccessRepository;
    private AccessRuleEngine ruleEngine;

    private AccessRequestService service;

    @BeforeEach
    void setUp() {
        accessRequestRepository = mock(AccessRequestRepository.class);
        moduleRepository = mock(ModuleRepository.class);
        userRepository = mock(UserRepository.class);
        userModuleAccessRepository = mock(UserModuleAccessRepository.class);
        ruleEngine = mock(AccessRuleEngine.class);

        service = new AccessRequestService(accessRequestRepository,
                moduleRepository,
                userRepository,
                userModuleAccessRepository,
                ruleEngine);
    }

    @Test
    void deveCriarSolicitacaoComSucesso() {
        CreateAccessRequestDTO dto = new CreateAccessRequestDTO();
        dto.setModuleCodes(List.of("PORTAL_COLABORADOR"));
        dto.setJustification("Preciso do acesso para executar minhas atividades.");
        dto.setUrgent(true);

        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .name("User")
                .department(Department.TI)
                .build();

        Module module = Module.builder()
                .id(1L)
                .code("PORTAL_COLABORADOR")
                .name("Portal do Colaborador")
                .active(true)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(moduleRepository.findByCode("PORTAL_COLABORADOR")).thenReturn(Optional.of(module));

        AccessRequest savedReq = new AccessRequest();
        savedReq.setId(10L);
        savedReq.setUser(user);
        savedReq.setCreatedAt(LocalDateTime.now());
        savedReq.setStatus(AccessRequestStatus.ATIVO);

        when(accessRequestRepository.save(any(AccessRequest.class)))
                .thenReturn(savedReq);

        LocalDateTime exp = LocalDateTime.now().plusDays(180);
        when(ruleEngine.calculateExpiration()).thenReturn(exp);

        AccessRequestDetailDTO result = service.createRequest(dto, "user@example.com");

        verify(userRepository).findByEmail("user@example.com");
        verify(ruleEngine).validateJustification(dto.getJustification());
        verify(moduleRepository).findByCode("PORTAL_COLABORADOR");
        verify(ruleEngine).validateDepartmentPermission(eq(user), eq(List.of(module)));
        verify(ruleEngine).validateMutualExclusions(eq(user), eq(List.of(module)));
        verify(ruleEngine).validateModuleLimit(eq(user), eq(List.of(module)));
        verify(userModuleAccessRepository).saveAll(anyList());
        verify(accessRequestRepository, atLeastOnce()).save(any(AccessRequest.class));

        assertEquals("user@example.com", result.getUserEmail());
        assertEquals(AccessRequestStatus.ATIVO, result.getStatus());
        assertTrue(result.getModuleNames().contains("Portal do Colaborador"));
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontradoAoCriar() {
        CreateAccessRequestDTO dto = new CreateAccessRequestDTO();
        dto.setModuleCodes(List.of("PORTAL_COLABORADOR"));
        dto.setJustification("Justificativa válida.");
        dto.setUrgent(false);

        when(userRepository.findByEmail("naoexiste@example.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createRequest(dto, "naoexiste@example.com"));

        assertTrue(ex.getMessage().contains("Usuário não encontrado"));
        verify(userRepository).findByEmail("naoexiste@example.com");
        verifyNoInteractions(moduleRepository);
    }

    @Test
    void deveLancarExcecaoQuandoModuloNaoEncontrado() {
        CreateAccessRequestDTO dto = new CreateAccessRequestDTO();
        dto.setModuleCodes(List.of("INEXISTENTE"));
        dto.setJustification("Justificativa válida.");
        dto.setUrgent(false);

        User user = User.builder()
                .id(2L)
                .email("user@example.com")
                .department(Department.TI)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(moduleRepository.findByCode("INEXISTENTE")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createRequest(dto, "user@example.com"));

        assertTrue(ex.getMessage().contains("Módulo não encontrado"));
        verify(moduleRepository).findByCode("INEXISTENTE");
    }

    @Test
    void deveLancarExcecaoQuandoModuloInativo() {
        CreateAccessRequestDTO dto = new CreateAccessRequestDTO();
        dto.setModuleCodes(List.of("PORTAL_COLABORADOR"));
        dto.setJustification("Justificativa válida.");
        dto.setUrgent(false);

        User user = User.builder()
                .id(2L)
                .email("user@example.com")
                .department(Department.TI)
                .build();

        Module module = Module.builder()
                .id(1L)
                .code("PORTAL_COLABORADOR")
                .name("Portal do Colaborador")
                .active(false)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(moduleRepository.findByCode("PORTAL_COLABORADOR")).thenReturn(Optional.of(module));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createRequest(dto, "user@example.com"));

        assertTrue(ex.getMessage().contains("Módulo inativo"));
        verify(moduleRepository).findByCode("PORTAL_COLABORADOR");
    }

    @Test
    void deveBuscarSolicitacoesDoUsuario() {
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .name("User")
                .department(Department.RH)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        AccessRequest req = new AccessRequest();
        req.setId(5L);
        req.setUser(user);
        req.setProtocol("SOL-20251122-0001");
        req.setStatus(AccessRequestStatus.ATIVO);
        req.setJustification("Justificativa");
        req.setCreatedAt(LocalDateTime.now());

        Page<AccessRequest> page =
                new PageImpl<>(List.of(req), PageRequest.of(0, 10), 1);

        AccessRequestFilter filter = new AccessRequestFilter();
        filter.setPage(0);
        filter.setSize(10);

        when(accessRequestRepository.findByUser(eq(user), any(Pageable.class)))
                .thenReturn(page);

        var result = service.searchRequests(filter, "user@example.com");

        verify(userRepository).findByEmail("user@example.com");
        verify(accessRequestRepository).findByUser(eq(user), any(Pageable.class));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void deveRetornarDetalheQuandoUsuarioEhDono() {
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .name("User")
                .department(Department.TI)
                .build();

        AccessRequest req = new AccessRequest();
        req.setId(5L);
        req.setUser(user);
        req.setProtocol("SOL-20251122-0001");
        req.setStatus(AccessRequestStatus.ATIVO);
        req.setJustification("Justificativa");
        req.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(accessRequestRepository.findById(5L)).thenReturn(Optional.of(req));

        AccessRequestDetailDTO dto = service.getRequest(5L, "user@example.com");

        verify(userRepository).findByEmail("user@example.com");
        verify(accessRequestRepository).findById(5L);
        assertEquals("SOL-20251122-0001", dto.getProtocol());
        assertEquals("user@example.com", dto.getUserEmail());
    }

    @Test
    void deveLancarSecurityExceptionQuandoUsuarioNaoEhDono() {
        User dono = User.builder()
                .id(1L)
                .email("dono@example.com")
                .department(Department.TI)
                .build();

        User outro = User.builder()
                .id(2L)
                .email("outro@example.com")
                .department(Department.RH)
                .build();

        AccessRequest req = new AccessRequest();
        req.setId(5L);
        req.setUser(dono);

        when(userRepository.findByEmail("outro@example.com")).thenReturn(Optional.of(outro));
        when(accessRequestRepository.findById(5L)).thenReturn(Optional.of(req));

        SecurityException ex = assertThrows(SecurityException.class,
                () -> service.getRequest(5L, "outro@example.com"));

        assertTrue(ex.getMessage().contains("Não autorizado"));
        verify(accessRequestRepository).findById(5L);
    }

    @Test
    void deveCancelarSolicitacaoAtiva() {
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .department(Department.TI)
                .build();

        AccessRequest req = new AccessRequest();
        req.setId(5L);
        req.setUser(user);
        req.setStatus(AccessRequestStatus.ATIVO);
        req.setHistory(new ArrayList<>());

        CancelAccessRequestDTO dto = new CancelAccessRequestDTO();
        dto.setReason("Motivo válido");

        UserModuleAccess acesso1 = UserModuleAccess.builder()
                .id(1L).user(user).active(true).build();
        UserModuleAccess acesso2 = UserModuleAccess.builder()
                .id(2L).user(user).active(true).build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(accessRequestRepository.findById(5L)).thenReturn(Optional.of(req));
        when(userModuleAccessRepository.findByUserAndActiveTrue(user))
                .thenReturn(List.of(acesso1, acesso2));

        service.cancelRequest(5L, dto, "user@example.com");

        verify(userRepository).findByEmail("user@example.com");
        verify(accessRequestRepository).findById(5L);
        verify(userModuleAccessRepository).findByUserAndActiveTrue(user);
        verify(userModuleAccessRepository).saveAll(anyList());
        verify(accessRequestRepository).save(req);

        assertEquals(AccessRequestStatus.CANCELADO, req.getStatus());
        assertEquals("Motivo válido", req.getCancellationReason());
        assertFalse(acesso1.isActive());
        assertFalse(acesso2.isActive());
        assertFalse(req.getHistory().isEmpty());
    }

    @Test
    void deveLancarIllegalStateQuandoCancelarNaoAtivo() {
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .department(Department.TI)
                .build();

        AccessRequest req = new AccessRequest();
        req.setId(5L);
        req.setUser(user);
        req.setStatus(AccessRequestStatus.NEGADO);

        CancelAccessRequestDTO dto = new CancelAccessRequestDTO();
        dto.setReason("Motivo válido");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(accessRequestRepository.findById(5L)).thenReturn(Optional.of(req));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.cancelRequest(5L, dto, "user@example.com"));

        assertTrue(ex.getMessage().contains("Apenas solicitações ativas podem ser canceladas"));
        verify(accessRequestRepository).findById(5L);
        verifyNoInteractions(userModuleAccessRepository);
    }
}
