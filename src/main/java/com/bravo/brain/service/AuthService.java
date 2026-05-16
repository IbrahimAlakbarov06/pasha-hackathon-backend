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

        // REGIONAL_MANAGER (Admin) - yalniz oz sobesine catisi var, department doldurulur
        // DEPARTMENT_HEAD (Mudir)  - butun sobelere catisi var, department = null, allDepartments = true
        // SUPER_ADMIN              - her seye catisi var
        String department = null;
        boolean allDepartments;

        if (user.getRole() == Role.REGIONAL_MANAGER) {
            allDepartments = false;
            department = (user.getCategories() != null && !user.getCategories().isEmpty())
                    ? user.getCategories().get(0)
                    : null;
        } else {
            allDepartments = true;
        }

        return new AuthDto.LoginResponse(
                token,
                86400L,
                user.getRole().name(),
                displayName,
                user.getFilial(),
                department,
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