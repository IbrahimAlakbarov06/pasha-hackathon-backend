package com.bravo.brain.domain.entity;

import com.bravo.brain.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId;          // avtomatik generasiya — FG-RM-001, FG-DH-012

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;        // bcrypt hash

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String region;          // REGIONAL_MANAGER üçün — Bakı, Sumqayıt, Gəncə
    private String storeName;       // DEPARTMENT_HEAD üçün — hansı mağaza
    private String departmentName;  // DEPARTMENT_HEAD üçün — hansı şöbə

    @Column(nullable = false)
    private boolean active = true;

    private boolean firstLogin = true;  // ilk girişdə şifrə dəyişdirilməlidir

    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}