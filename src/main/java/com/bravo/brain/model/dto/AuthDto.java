package com.bravo.brain.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class AuthDto {

    @Getter @Setter
    public static class LoginRequest {
        @NotBlank(message = "User ID bos ola bilmez")
        private String userId;

        @NotBlank(message = "Sifre bos ola bilmez")
        private String password;
    }

    // Frontend gozleyir:
    // { accessToken, expiresInSeconds, role, displayName, filial, department, allDepartments }
    @Getter @Setter @AllArgsConstructor
    public static class LoginResponse {
        private String accessToken;
        private long expiresInSeconds;
        private String role;
        private String displayName;
        private String filial;

        // REGIONAL_MANAGER (Admin) ucun: hansi sobede calisir (categories[0])
        // DEPARTMENT_HEAD (Mudir) ucun: null — cunki butun sobelere catisi var
        private String department;

        // true  => DEPARTMENT_HEAD (Mudir) — butun sobelere catisi var
        // false => REGIONAL_MANAGER (Admin) — yalniz oz sobesine catisi var
        private boolean allDepartments;
    }

    @Getter @Setter
    public static class ChangePasswordRequest {
        @NotBlank
        private String oldPassword;

        @NotBlank
        @Size(min = 6, message = "Yeni sifre minimum 6 simvol olmalidir")
        private String newPassword;
    }
}