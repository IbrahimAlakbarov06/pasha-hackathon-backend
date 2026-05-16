package com.bravo.brain.service;

import com.bravo.brain.domain.entity.User;
import com.bravo.brain.domain.repository.UserRepository;
import com.bravo.brain.model.dto.AuthDto;
import com.bravo.brain.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    // ── LOGIN ──────────────────────────────────────────────
    public AuthDto.LoginResponse login(AuthDto.LoginRequest req) {
        User user = repo.findByUserId(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User tapılmadı"));

        if (!user.isActive())
            throw new RuntimeException("Hesab deaktivdir. Admin ilə əlaqə saxlayın.");

        if (!encoder.matches(req.getPassword(), user.getPassword()))
            throw new RuntimeException("Şifrə yanlışdır");

        user.setLastLoginAt(LocalDateTime.now());
        repo.save(user);

        String token = jwtUtil.generateToken(user.getUserId(), user.getRole().name());

        return new AuthDto.LoginResponse(
                token,
                user.getUserId(),
                user.getFullName(),
                user.getRole(),
                user.getRegion(),
                user.getStoreName(),
                user.getDepartmentName(),
                user.isFirstLogin()
        );
    }

    // ── ŞİFRƏ DƏYİŞ ───────────────────────────────────────
    public void changePassword(String userId, AuthDto.ChangePasswordRequest req) {
        User user = repo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User tapılmadı"));

        if (!encoder.matches(req.getOldPassword(), user.getPassword()))
            throw new RuntimeException("Köhnə şifrə yanlışdır");

        user.setPassword(encoder.encode(req.getNewPassword()));
        user.setFirstLogin(false);
        repo.save(user);
    }
}