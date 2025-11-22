package com.example.accesscontrol.service;

import com.example.accesscontrol.dto.ModuleResponseDTO;
import com.example.accesscontrol.model.Module;
import com.example.accesscontrol.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;

    public List<ModuleResponseDTO> listAllModules() {
        List<Module> modules = moduleRepository.findAll();
        return modules.stream()
                .map(m -> ModuleResponseDTO.builder()
                        .id(m.getId())
                        .code(m.getCode())
                        .name(m.getName())
                        .description(m.getDescription())
                        .allowedDepartments(m.getAllowedDepartments())
                        .active(m.isActive())
                        .incompatibleModuleCodes(
                                m.getIncompatibleModules() == null ? null :
                                        m.getIncompatibleModules().stream()
                                                .map(Module::getCode)
                                                .collect(Collectors.toSet())
                        )
                        .build())
                .collect(Collectors.toList());
    }
}
