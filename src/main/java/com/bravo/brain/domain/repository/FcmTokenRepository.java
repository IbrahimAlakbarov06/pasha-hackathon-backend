package com.bravo.brain.domain.repository;

import com.bravo.brain.domain.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByUserId(String userId);
    void deleteByUserId(String userId);
}