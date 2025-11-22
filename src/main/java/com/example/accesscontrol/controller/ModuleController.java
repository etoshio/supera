package com.example.accesscontrol.controller;

import com.example.accesscontrol.dto.ModuleResponseDTO;
import com.example.accesscontrol.service.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @GetMapping
    @Operation(summary = "Lista todos os módulos disponíveis")
    public ResponseEntity<List<ModuleResponseDTO>> listModules() {
        return ResponseEntity.ok(moduleService.listAllModules());
    }
}
