package com.bravo.brain.domain.repository;

import com.bravo.brain.domain.entity.User;
import com.bravo.brain.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);
    Optional<User> findByEmail(String email);
    boolean existsByUserId(String userId);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByActiveTrue();
    List<User> findByActiveFalse();
}