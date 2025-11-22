package com.example.accesscontrol.service;

import com.example.accesscontrol.model.*;
import com.example.accesscontrol.model.Module;
import com.example.accesscontrol.repository.UserModuleAccessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AccessRuleEngine {

    private final UserModuleAccessRepository userModuleAccessRepository;

    public void validateJustification(String justification) {
        String normalized = justification.trim().toLowerCase();
        if (normalized.length() < 20) {
            throw new IllegalArgumentException("Justificativa insuficiente ou genérica");
        }

        // remove tudo que não é letra minúscula
        String compact = normalized.replaceAll("[^a-z]", "");

        // remove ocorrências das palavras genéricas
        String semGenericas = compact
                .replace("teste", "")
                .replace("aaa", "")
                .replace("preciso", "");

        if (semGenericas.isEmpty()) {
            throw new IllegalArgumentException("Justificativa insuficiente ou genérica");
        }
    }


    public void validateDepartmentPermission(User user, List<Module> modules) {
        for (Module m : modules) {
            if (!m.getAllowedDepartments().contains(user.getDepartment())) {
                throw new IllegalArgumentException("Departamento sem permissão para acessar este módulo: " + m.getName());
            }
        }
    }

    public void validateModuleLimit(User user, List<Module> requestedModules) {
        List<UserModuleAccess> active = userModuleAccessRepository.findByUserAndActiveTrue(user);
        int current = active.size();
        int max = user.getDepartment() == Department.TI ? 10 : 5;
        if (current + requestedModules.size() > max) {
            throw new IllegalArgumentException("Limite de módulos ativos atingido");
        }
    }

    public void validateMutualExclusions(User user, List<Module> requestedModules) {
        List<UserModuleAccess> active = userModuleAccessRepository.findByUserAndActiveTrue(user);
        for (UserModuleAccess access : active) {
            for (Module req : requestedModules) {
                if (access.getModule().getIncompatibleModules() != null &&
                        access.getModule().getIncompatibleModules().contains(req)) {
                    throw new IllegalArgumentException("Módulo incompatível com outro módulo já ativo em seu perfil: " + req.getName());
                }
            }
        }
    }

    public LocalDateTime calculateExpiration() {
        return LocalDateTime.now().plusDays(180);
    }
}
