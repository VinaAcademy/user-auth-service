package vn.vinaacademy.auth.repository;


import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import vn.vinaacademy.auth.entity.ActionToken;
import vn.vinaacademy.auth.enums.ActionTokenType;
import vn.vinaacademy.auth.entity.User;

import java.util.Optional;

public interface ActionTokenRepository extends JpaRepository<ActionToken, Long> {
    Optional<ActionToken> findByTokenAndType(String token, ActionTokenType type);

    Optional<ActionToken> findByUserAndType(User user, ActionTokenType actionTokenType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ActionToken a WHERE a.user = :user AND a.type = :type")
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "1000")})
    Optional<ActionToken> findForUpdate(@Param("user") User user, @Param("type") ActionTokenType type);

}
