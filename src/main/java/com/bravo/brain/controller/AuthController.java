package com.bravo.brain.controller;

import com.bravo.brain.model.dto.AuthDto;
import com.bravo.brain.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthDto.LoginResponse> login(@Valid @RequestBody AuthDto.LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    // POST /api/auth/change-password
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody AuthDto.ChangePasswordRequest req) {
        authService.changePassword(userId, req);
        return ResponseEntity.ok("Şifrə uğurla dəyişdirildi");
    }
}