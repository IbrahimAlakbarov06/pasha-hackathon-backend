package com.bravo.brain.service;

import com.bravo.brain.domain.entity.User;
import com.bravo.brain.domain.repository.UserRepository;
import com.bravo.brain.model.dto.AuthDto;
import com.bravo.brain.model.enums.Role;
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

    public AuthDto.LoginResponse login(AuthDto.LoginRequest req) {
        User user = repo.findByUserId(req.getUserId())
                .orElseThrow(() -> new RuntimeException("Istifadeci tapiilmadi"));

        if (!user.isActive())
            throw new RuntimeException("Hesab deaktivdir. Admin ile elaqe saxlayin.");

        if (!encoder.matches(req.getPassword(), user.getPassword()))
            throw new RuntimeException("Sifre yanlisdir");

        user.setLastLoginAt(LocalDateTime.now());
        repo.save(user);

        String token = jwtUtil.generateToken(user.getUserId(), user.getRole().name());
        String displayName = user.getFirstName() + " " + user.getLastName();

        String departmentName = null;
        Long departmentId = null;
        boolean allDepartments;

        if (user.getRole() == Role.REGIONAL_MANAGER) {
            allDepartments = false;
            if (user.getDepartment() != null) {
                departmentId = user.getDepartment().getId();
                departmentName = user.getDepartment().getName();
            }
        } else {
            allDepartments = true;
        }

        return new AuthDto.LoginResponse(
                token,
                86400L,
                user.getRole().name(),
                displayName,
                user.getFilial(),
                departmentId,
                departmentName,
                allDepartments
        );
    }

    public void changePassword(String userId, AuthDto.ChangePasswordRequest req) {
        User user = repo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Istifadeci tapiilmadi"));

        if (!encoder.matches(req.getOldPassword(), user.getPassword()))
            throw new RuntimeException("Kohne sifre yanlisdir");

        user.setPassword(encoder.encode(req.getNewPassword()));
        user.setFirstLogin(false);
        repo.save(user);
    }
}