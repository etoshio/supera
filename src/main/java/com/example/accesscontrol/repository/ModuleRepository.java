package com.example.accesscontrol.repository;

import com.example.accesscontrol.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    Optional<Module> findByCode(String code);
}
