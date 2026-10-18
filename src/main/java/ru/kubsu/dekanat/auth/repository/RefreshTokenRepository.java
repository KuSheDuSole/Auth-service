package ru.kubsu.dekanat.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kubsu.dekanat.auth.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
}
