package com.example.accesscontrol.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "modules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Module {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // ex: PORTAL_COLABORADOR

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "module_allowed_departments", joinColumns = @JoinColumn(name = "module_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "department")
    private Set<Department> allowedDepartments;

    @Column(nullable = false)
    private boolean active;

    @ManyToMany
    @JoinTable(name = "module_incompatible",
            joinColumns = @JoinColumn(name = "module_id"),
            inverseJoinColumns = @JoinColumn(name = "incompatible_module_id"))
    private Set<Module> incompatibleModules;
}
