package com.bravo.brain.model.dto;

import com.bravo.brain.model.enums.Role;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

public class UserDto {

    // ── USER YARAT — frontend POST /api/admin/users ────────
    @Getter @Setter
    public static class CreateRequest {
        @NotBlank(message = "Ad boş ola bilməz")
        private String firstName;

        @NotBlank(message = "Soyad boş ola bilməz")
        private String lastName;

        @NotBlank(message = "Email boş ola bilməz")
        @Email(message = "Email düzgün formatda deyil")
        private String email;

        @NotBlank(message = "Şifrə boş ola bilməz")
        @Size(min = 8, message = "Şifrə minimum 8 simvol olmalıdır")
        private String password;

        private String filial;          // frontend-dən "Bravo Koroğlu" kimi gəlir

        private String role;            // "MANAGER" və ya "ADMIN" — string kimi gəlir

        private Long departmentId;
    }

    // ── USER CAVABI — frontend UserRow tipinə uyğun ────────
    @Getter @Setter @AllArgsConstructor
    public static class UserResponse {
        private String userId;
        private String firstName;
        private String lastName;
        private String email;
        private String filial;
        private String role;
        private Long departmentId;
        private String departmentName;
        private boolean active;
    }

    // ── USER YENİLƏ — frontend PUT /api/admin/users/{id} ───
    @Getter @Setter
    public static class UpdateRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String filial;
        private String role;
        private String newPassword;     // boş olarsa dəyişdirilmir
        private Long departmentId;
    }
}