package com.bravo.brain.model.dto;

import com.bravo.brain.model.enums.Role;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

public class UserDto {

    // ── USER YARAT (Super Admin) ───────────────────────────
    @Getter @Setter
    public static class CreateRequest {
        @NotBlank(message = "Ad soyad boş ola bilməz")
        private String fullName;

        @NotBlank @Email(message = "Email düzgün deyil")
        private String email;

        @NotNull(message = "Rol seçilməlidir")
        private Role role;

        private String region;          // REGIONAL_MANAGER üçün məcburi
        private String storeName;       // DEPARTMENT_HEAD üçün məcburi
        private String departmentName;  // DEPARTMENT_HEAD üçün məcburi
    }

    // ── USER YARAT CAVABI ──────────────────────────────────
    @Getter @Setter @AllArgsConstructor
    public static class CreateResponse {
        private String userId;          // avtomatik generasiya
        private String tempPassword;    // super admin-ə göstərilir, user ilk girişdə dəyişir
        private String fullName;
        private Role role;
        private String region;
        private String storeName;
        private String departmentName;
    }

    // ── USER REDAKTƏ ───────────────────────────────────────
    @Getter @Setter
    public static class UpdateRequest {
        private String fullName;
        private String region;
        private String storeName;
        private String departmentName;
        private Boolean active;
    }

    // ── USER CAVABI ────────────────────────────────────────
    @Getter @Setter @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String userId;
        private String fullName;
        private String email;
        private Role role;
        private String region;
        private String storeName;
        private String departmentName;
        private boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime lastLoginAt;
    }
}