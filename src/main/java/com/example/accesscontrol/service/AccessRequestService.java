package com.example.accesscontrol.service;

import com.example.accesscontrol.dto.*;
import com.example.accesscontrol.model.*;
import com.example.accesscontrol.model.Module;
import com.example.accesscontrol.repository.AccessRequestRepository;
import com.example.accesscontrol.repository.ModuleRepository;
import com.example.accesscontrol.repository.UserModuleAccessRepository;
import com.example.accesscontrol.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccessRequestService {

    private final AccessRequestRepository accessRequestRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final UserModuleAccessRepository userModuleAccessRepository;
    private final AccessRuleEngine ruleEngine;

    @Transactional
    public AccessRequestDetailDTO createRequest(CreateAccessRequestDTO dto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        ruleEngine.validateJustification(dto.getJustification());

        List<Module> modules = dto.getModuleCodes().stream()
                .map(code -> moduleRepository.findByCode(code)
                        .orElseThrow(() -> new IllegalArgumentException("Módulo não encontrado: " + code)))
                .collect(Collectors.toList());

        modules.forEach(m -> {
            if (!m.isActive()) {
                throw new IllegalArgumentException("Módulo inativo: " + m.getName());
            }
        });

        ruleEngine.validateDepartmentPermission(user, modules);
        ruleEngine.validateMutualExclusions(user, modules);
        ruleEngine.validateModuleLimit(user, modules);

        AccessRequest request = new AccessRequest();
        request.setUser(user);
        request.setJustification(dto.getJustification());
        request.setUrgent(dto.isUrgent());
        request.setStatus(AccessRequestStatus.ATIVO);
        request.setCreatedAt(LocalDateTime.now());
        request.setExpiresAt(ruleEngine.calculateExpiration());

        AccessRequest saved = accessRequestRepository.save(request);

        String protocol = generateProtocol(saved.getId());
        saved.setProtocol(protocol);

        List<AccessRequestModule> reqModules = modules.stream()
                .map(m -> AccessRequestModule.builder()
                        .accessRequest(saved)
                        .module(m)
                        .build())
                .collect(Collectors.toList());
        saved.setRequestedModules(reqModules);

        List<UserModuleAccess> newAccesses = modules.stream()
                .map(m -> UserModuleAccess.builder()
                        .user(user)
                        .module(m)
                        .grantedAt(LocalDateTime.now())
                        .expiresAt(saved.getExpiresAt())
                        .active(true)
                        .build())
                .collect(Collectors.toList());

        userModuleAccessRepository.saveAll(newAccesses);

        AccessRequestHistory history = AccessRequestHistory.builder()
                .accessRequest(saved)
                .eventDate(LocalDateTime.now())
                .eventType("CREATED")
                .details("Solicitação criada e aprovada automaticamente")
                .build();

        saved.setHistory(Collections.singletonList(history));

        AccessRequest finalSaved = accessRequestRepository.save(saved);

        return mapToDetailDTO(finalSaved);
    }

    private String generateProtocol(Long id) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
        String seq = String.format("%04d", id % 10000);
        return "SOL-" + date + "-" + seq;
    }

    public Page<AccessRequestSummaryDTO> searchRequests(AccessRequestFilter filter, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AccessRequest> page = accessRequestRepository.findByUser(user, pageable);

        return page.map(this::mapToSummaryDTO);
    }

    public AccessRequestDetailDTO getRequest(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        AccessRequest request = accessRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        if (!request.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Não autorizado");
        }

        return mapToDetailDTO(request);
    }

    @Transactional
    public void cancelRequest(Long id, CancelAccessRequestDTO dto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        AccessRequest request = accessRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        if (!request.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Não autorizado");
        }
        if (request.getStatus() != AccessRequestStatus.ATIVO) {
            throw new IllegalStateException("Apenas solicitações ativas podem ser canceladas");
        }

        request.setStatus(AccessRequestStatus.CANCELADO);
        request.setCancellationReason(dto.getReason());
        request.setCancelledAt(LocalDateTime.now());

        List<UserModuleAccess> accesses = userModuleAccessRepository.findByUserAndActiveTrue(user);
        for (UserModuleAccess access : accesses) {
            access.setActive(false);
        }
        userModuleAccessRepository.saveAll(accesses);

        AccessRequestHistory history = AccessRequestHistory.builder()
                .accessRequest(request)
                .eventDate(LocalDateTime.now())
                .eventType("CANCELLED")
                .details("Cancelado pelo usuário. Motivo: " + dto.getReason())
                .build();
        List<AccessRequestHistory> hist = request.getHistory() == null ? new ArrayList<>() : new ArrayList<>(request.getHistory());
        hist.add(history);
        request.setHistory(hist);

        accessRequestRepository.save(request);
    }

    private AccessRequestSummaryDTO mapToSummaryDTO(AccessRequest r) {
        List<String> moduleNames = r.getRequestedModules() == null ? List.of() :
                r.getRequestedModules().stream()
                        .map(reqM -> reqM.getModule().getName())
                        .toList();

        return AccessRequestSummaryDTO.builder()
                .id(r.getId())
                .protocol(r.getProtocol())
                .moduleNames(moduleNames)
                .status(r.getStatus())
                .justification(r.getJustification())
                .urgent(r.isUrgent())
                .createdAt(r.getCreatedAt())
                .expiresAt(r.getExpiresAt())
                .denialReason(r.getDenialReason())
                .build();
    }

    private AccessRequestDetailDTO mapToDetailDTO(AccessRequest r) {
        List<String> moduleNames = r.getRequestedModules() == null ? List.of() :
                r.getRequestedModules().stream()
                        .map(reqM -> reqM.getModule().getName())
                        .toList();

        List<AccessRequestDetailDTO.HistoryEntryDTO> history = r.getHistory() == null ? List.of() :
                r.getHistory().stream()
                        .map(h -> AccessRequestDetailDTO.HistoryEntryDTO.builder()
                                .eventDate(h.getEventDate())
                                .eventType(h.getEventType())
                                .details(h.getDetails())
                                .build())
                        .toList();

        return AccessRequestDetailDTO.builder()
                .id(r.getId())
                .protocol(r.getProtocol())
                .userEmail(r.getUser().getEmail())
                .userName(r.getUser().getName())
                .userDepartment(r.getUser().getDepartment().name())
                .moduleNames(moduleNames)
                .status(r.getStatus())
                .justification(r.getJustification())
                .urgent(r.isUrgent())
                .createdAt(r.getCreatedAt())
                .expiresAt(r.getExpiresAt())
                .denialReason(r.getDenialReason())
                .cancellationReason(r.getCancellationReason())
                .cancelledAt(r.getCancelledAt())
                .history(history)
                .build();
    }
}
