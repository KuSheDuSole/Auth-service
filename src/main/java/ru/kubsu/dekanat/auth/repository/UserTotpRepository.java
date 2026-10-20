package ru.kubsu.dekanat.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kubsu.dekanat.auth.entity.UserTotp;

public interface UserTotpRepository extends JpaRepository<UserTotp, Long> {
}
