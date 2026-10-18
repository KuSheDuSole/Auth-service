package ru.kubsu.dekanat.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.kubsu.dekanat.auth.entity.EmailChangeCode;
import ru.kubsu.dekanat.auth.entity.User;

import java.util.Optional;

public interface EmailChangeCodeRepository extends JpaRepository<EmailChangeCode, Long> {

    Optional<EmailChangeCode> findByUserId(Long userId);

    @Modifying
    @Query("delete from EmailChangeCode c where c.user = :user")
    void deleteAllByUser(User user);

}
