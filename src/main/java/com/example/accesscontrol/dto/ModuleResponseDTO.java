package com.example.accesscontrol.dto;

import com.example.accesscontrol.model.Department;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleResponseDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Set<Department> allowedDepartments;
    private boolean active;
    private Set<String> incompatibleModuleCodes;
}
