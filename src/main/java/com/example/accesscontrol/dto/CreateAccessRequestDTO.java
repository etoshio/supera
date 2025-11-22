package com.example.accesscontrol.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccessRequestDTO {

    @NotEmpty
    @Size(min = 1, max = 3)
    private List<String> moduleCodes;

    @NotBlank
    @Size(min = 20, max = 500)
    private String justification;

    private boolean urgent;
}
