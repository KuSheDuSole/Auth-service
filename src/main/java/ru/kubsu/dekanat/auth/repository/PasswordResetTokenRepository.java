package ru.kubsu.dekanat.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.kubsu.dekanat.auth.entity.PasswordResetToken;
import ru.kubsu.dekanat.auth.entity.User;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from PasswordResetToken t where t.user = :user")
    void deleteAllByUser(User user);

    @Modifying
    @Query("delete from PasswordResetToken t where t.expiresAt < :now")
    int deleteAllExpired(Instant now);

}
