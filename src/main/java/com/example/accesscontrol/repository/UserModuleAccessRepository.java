package com.example.accesscontrol.repository;

import com.example.accesscontrol.model.User;
import com.example.accesscontrol.model.UserModuleAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserModuleAccessRepository extends JpaRepository<UserModuleAccess, Long> {

    List<UserModuleAccess> findByUserAndActiveTrue(User user);
}
