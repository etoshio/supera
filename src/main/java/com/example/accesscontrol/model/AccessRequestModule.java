package com.example.accesscontrol.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "access_request_modules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessRequestModule {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private AccessRequest accessRequest;

    @ManyToOne(optional = false)
    private Module module;
}
