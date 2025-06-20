package vn.vinaacademy.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import vn.vinaacademy.auth.entity.RefreshToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken t WHERE t.expireTime < :now")
    void deleteExpiredTokens(LocalDateTime now);

    Optional<RefreshToken> findByToken(String refreshToken);

    Optional<RefreshToken> findByTokenAndUsername(String refreshToken, String username);
}
