package com.bravo.brain.config;

import com.bravo.brain.domain.entity.User;
import com.bravo.brain.domain.repository.UserRepository;
import com.bravo.brain.model.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedSuperAdmin();
    }

    private void seedSuperAdmin() {
        if (userRepository.existsByUserId("SA-ADMIN")) {
            return; // artıq mövcuddur
        }

        User admin = User.builder()
                .userId("SA-ADMIN")
                .firstName("Super")
                .lastName("Admin")
                .email("admin@freshguard.com")
                .password(passwordEncoder.encode("SuperAdmin!2026"))
                .role(Role.SUPER_ADMIN)
                .filial("Mərkəz")
                .categories(new ArrayList<>())
                .active(true)
                .firstLogin(false)
                .build();

        userRepository.save(admin);
        log.info("Super Admin yaradıldı: SA-ADMIN / SuperAdmin!2026");
    }
}