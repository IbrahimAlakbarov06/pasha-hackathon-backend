package com.bravo.brain.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class AuthDto {

    // ── LOGIN REQUEST ──────────────────────────────────────
    @Getter @Setter
    public static class LoginRequest {
        @NotBlank(message = "User ID boş ola bilməz")
        private String userId;

        @NotBlank(message = "Şifrə boş ola bilməz")
        private String password;
    }

    // ── LOGIN RESPONSE — frontend LoginResponse tipinə uyğun
    // Frontend gözləyir: { accessToken, expiresInSeconds, role, displayName }
    @Getter @Setter @AllArgsConstructor
    public static class LoginResponse {
        private String accessToken;
        private long expiresInSeconds;
        private String role;
        private String displayName;
    }

    // ── ŞİFRƏ DƏYİŞDİRMƏ ─────────────────────────────────
    @Getter @Setter
    public static class ChangePasswordRequest {
        @NotBlank
        private String oldPassword;

        @NotBlank
        @Size(min = 6, message = "Yeni şifrə minimum 6 simvol olmalıdır")
        private String newPassword;
    }
}