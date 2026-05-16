package com.bravo.brain.service;

import com.bravo.brain.domain.entity.User;
import com.bravo.brain.domain.repository.UserRepository;
import com.bravo.brain.model.dto.UserDto;
import com.bravo.brain.model.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    // ── USER YARAT ─────────────────────────────────────────
    public UserDto.CreateResponse createUser(UserDto.CreateRequest req) {
        if (repo.existsByEmail(req.getEmail()))
            throw new RuntimeException("Bu email artıq istifadə olunur");

        validateRoleFields(req);

        String userId = generateUserId(req.getRole());
        String tempPassword = generateTempPassword();

        User user = User.builder()
                .userId(userId)
                .fullName(req.getFullName())
                .email(req.getEmail())
                .password(encoder.encode(tempPassword))
                .role(req.getRole())
                .region(req.getRegion())
                .storeName(req.getStoreName())
                .departmentName(req.getDepartmentName())
                .active(true)
                .firstLogin(true)
                .build();

        repo.save(user);

        return new UserDto.CreateResponse(
                userId, tempPassword, req.getFullName(),
                req.getRole(), req.getRegion(),
                req.getStoreName(), req.getDepartmentName()
        );
    }

    // ── BÜTÜN USERLƏR ──────────────────────────────────────
    public List<UserDto.UserResponse> getAllUsers() {
        return repo.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── ROL ÜZRƏ ──────────────────────────────────────────
    public List<UserDto.UserResponse> getUsersByRole(Role role) {
        return repo.findByRole(role).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── USER REDAKTƏ ───────────────────────────────────────
    public UserDto.UserResponse updateUser(Long id, UserDto.UpdateRequest req) {
        User user = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User tapılmadı"));

        if (req.getFullName() != null) user.setFullName(req.getFullName());
        if (req.getRegion() != null) user.setRegion(req.getRegion());
        if (req.getStoreName() != null) user.setStoreName(req.getStoreName());
        if (req.getDepartmentName() != null) user.setDepartmentName(req.getDepartmentName());
        if (req.getActive() != null) user.setActive(req.getActive());

        return toResponse(repo.save(user));
    }

    // ── DEAKTİV ET ────────────────────────────────────────
    public void deactivateUser(Long id) {
        User user = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User tapılmadı"));
        user.setActive(false);
        repo.save(user);
    }

    // ── USER ID GENERASIYA ─────────────────────────────────
    // Format: FG-RM-001 (Regional Manager), FG-DH-012 (Department Head)
    private String generateUserId(Role role) {
        String prefix = switch (role) {
            case SUPER_ADMIN -> "FG-SA";
            case REGIONAL_MANAGER -> "FG-RM";
            case DEPARTMENT_HEAD -> "FG-DH";
        };
        String candidate;
        int counter = 1;
        do {
            candidate = String.format("%s-%03d", prefix, counter++);
        } while (repo.existsByUserId(candidate));
        return candidate;
    }

    // ── TEMP ŞİFRƏ ────────────────────────────────────────
    private String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // ── ROLE VALİDASİYA ────────────────────────────────────
    private void validateRoleFields(UserDto.CreateRequest req) {
        if (req.getRole() == Role.REGIONAL_MANAGER && (req.getRegion() == null || req.getRegion().isBlank()))
            throw new RuntimeException("Reyon meneceri üçün region məcburidir");
        if (req.getRole() == Role.DEPARTMENT_HEAD) {
            if (req.getStoreName() == null || req.getStoreName().isBlank())
                throw new RuntimeException("Şöbə rəhbəri üçün mağaza adı məcburidir");
            if (req.getDepartmentName() == null || req.getDepartmentName().isBlank())
                throw new RuntimeException("Şöbə rəhbəri üçün şöbə adı məcburidir");
        }
    }

    // ── ENTITY → DTO ──────────────────────────────────────
    private UserDto.UserResponse toResponse(User u) {
        return new UserDto.UserResponse(
                u.getId(), u.getUserId(), u.getFullName(), u.getEmail(),
                u.getRole(), u.getRegion(), u.getStoreName(),
                u.getDepartmentName(), u.isActive(),
                u.getCreatedAt(), u.getLastLoginAt()
        );
    }
}