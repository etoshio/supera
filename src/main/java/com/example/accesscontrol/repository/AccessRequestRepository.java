package com.example.accesscontrol.repository;

import com.example.accesscontrol.model.AccessRequest;
import com.example.accesscontrol.model.User;
import com.example.accesscontrol.model.AccessRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessRequestRepository extends JpaRepository<AccessRequest, Long> {

    Page<AccessRequest> findByUser(User user, Pageable pageable);

    Page<AccessRequest> findByUserAndStatus(User user, AccessRequestStatus status, Pageable pageable);
}
