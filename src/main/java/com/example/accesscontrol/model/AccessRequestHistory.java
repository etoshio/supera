package com.example.accesscontrol.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "access_request_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessRequestHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private AccessRequest accessRequest;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, length = 1000)
    private String details;
}
