package com.bravo.brain.service;

import com.bravo.brain.domain.entity.User;
import com.bravo.brain.domain.repository.UserRepository;
import com.bravo.brain.model.dto.UserDto;
import com.bravo.brain.model.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    // ── USER YARAT ─────────────────────────────────────────
    @Transactional
    public UserDto.UserResponse createUser(UserDto.CreateRequest req) {
        if (repo.existsByEmail(req.getEmail()))
            throw new RuntimeException("Bu email artıq istifadə olunur");

        Role role = mapRole(req.getRole());
        String userId = generateUserId(role);

        User user = User.builder()
                .userId(userId)
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .role(role)
                .filial(req.getFilial())
                .categories(req.getCategories() != null ? req.getCategories() : new ArrayList<>())
                .active(true)
                .firstLogin(true)
                .build();

        return toResponse(repo.save(user));
    }

    // ── BÜTÜN USERLƏR (activeOnly filter ilə) ─────────────
    public List<UserDto.UserResponse> getAllUsers(boolean activeOnly) {
        List<User> users = activeOnly ? repo.findByActiveTrue() : repo.findAll();
        return users.stream()
                .filter(u -> u.getRole() != Role.SUPER_ADMIN)  // SUPER_ADMIN-i göstərmə
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── USER YENİLƏ ────────────────────────────────────────
    @Transactional
    public UserDto.UserResponse updateUser(String userId, UserDto.UpdateRequest req) {
        User user = repo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı: " + userId));

        if (req.getFirstName() != null && !req.getFirstName().isBlank())
            user.setFirstName(req.getFirstName());
        if (req.getLastName() != null && !req.getLastName().isBlank())
            user.setLastName(req.getLastName());
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            if (!req.getEmail().equals(user.getEmail()) && repo.existsByEmail(req.getEmail()))
                throw new RuntimeException("Bu email artıq istifadə olunur");
            user.setEmail(req.getEmail());
        }
        if (req.getFilial() != null)
            user.setFilial(req.getFilial());
        if (req.getRole() != null)
            user.setRole(mapRole(req.getRole()));
        if (req.getCategories() != null)
            user.setCategories(req.getCategories());
        if (req.getNewPassword() != null && req.getNewPassword().length() >= 8) {
            if (req.getNewPassword().length() > 72)
                throw new RuntimeException("Şifrə 72 simvoldan çox ola bilməz");
            user.setPassword(encoder.encode(req.getNewPassword()));
        }

        return toResponse(repo.save(user));
    }

    // ── DEAKTİV ET ────────────────────────────────────────
    @Transactional
    public void deactivateUser(String userId) {
        User user = repo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı: " + userId));
        if (user.getRole() == Role.SUPER_ADMIN)
            throw new RuntimeException("Super Admin deaktiv edilə bilməz");
        user.setActive(false);
        repo.save(user);
    }

    // ── ROL ÇEVIRMƏ — frontend string → enum ──────────────
    private Role mapRole(String roleStr) {
        if (roleStr == null) return Role.REGIONAL_MANAGER;
        return switch (roleStr.toUpperCase()) {
            case "ADMIN"          -> Role.REGIONAL_MANAGER;
            case "MANAGER"        -> Role.DEPARTMENT_HEAD;
            case "SUPER_ADMIN"    -> Role.SUPER_ADMIN;
            case "REGIONAL_MANAGER" -> Role.REGIONAL_MANAGER;
            case "DEPARTMENT_HEAD"  -> Role.DEPARTMENT_HEAD;
            default -> Role.DEPARTMENT_HEAD;
        };
    }

    // ── ROL ÇEVIRMƏ — enum → frontend string ──────────────
    private String mapRoleToFrontend(Role role) {
        return switch (role) {
            case SUPER_ADMIN      -> "SUPER_ADMIN";
            case REGIONAL_MANAGER -> "ADMIN";
            case DEPARTMENT_HEAD  -> "MANAGER";
        };
    }

    // ── USER ID GENERASIYA ─────────────────────────────────
    private String generateUserId(Role role) {
        String prefix = switch (role) {
            case SUPER_ADMIN      -> "FG-SA";
            case REGIONAL_MANAGER -> "FG-RM";
            case DEPARTMENT_HEAD  -> "FG-DH";
        };
        int counter = 1;
        String candidate;
        do {
            candidate = String.format("%s-%03d", prefix, counter++);
        } while (repo.existsByUserId(candidate));
        return candidate;
    }

    // ── ENTITY → DTO ──────────────────────────────────────
    private UserDto.UserResponse toResponse(User u) {
        return new UserDto.UserResponse(
                u.getUserId(),
                u.getFirstName(),
                u.getLastName(),
                u.getEmail(),
                u.getFilial(),
                mapRoleToFrontend(u.getRole()),
                u.getCategories() != null ? u.getCategories() : new ArrayList<>(),
                u.isActive()
        );
    }
}