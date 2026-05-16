package com.bravo.brain.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

public class DepartmentDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank(message = "Şöbə adı boş ola bilməz")
        private String name;

        @NotBlank(message = "Mağaza adı boş ola bilməz")
        private String storeName;

        private String imageBase64;     // nullable
    }

    @Getter @Setter @AllArgsConstructor
    public static class DepartmentResponse {
        private Long id;
        private String name;
        private String storeName;
        private String imageBase64;
        private boolean active;
        private LocalDateTime createdAt;
    }
}