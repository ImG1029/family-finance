package br.com.geziel.family_finance.domain.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
}
