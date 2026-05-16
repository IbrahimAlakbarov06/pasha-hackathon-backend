package com.bravo.brain.model.dto;

import com.bravo.brain.model.enums.Role;
import jakarta.validation.constraints.*;
import lombok.*;

public class AuthDto {

    // ── LOGIN ──────────────────────────────────────────────
    @Getter @Setter
    public static class LoginRequest {
        @NotBlank(message = "User ID boş ola bilməz")
        private String userId;

        @NotBlank(message = "Şifrə boş ola bilməz")
        private String password;
    }

    @Getter @Setter @AllArgsConstructor
    public static class LoginResponse {
        private String token;
        private String userId;
        private String fullName;
        private Role role;
        private String region;
        private String storeName;
        private String departmentName;
        private boolean firstLogin;
    }

    // ── ŞİFRƏ DƏYİŞ ───────────────────────────────────────
    @Getter @Setter
    public static class ChangePasswordRequest {
        @NotBlank
        private String oldPassword;

        @NotBlank
        @Size(min = 6, message = "Yeni şifrə minimum 6 simvol olmalıdır")
        private String newPassword;
    }
}