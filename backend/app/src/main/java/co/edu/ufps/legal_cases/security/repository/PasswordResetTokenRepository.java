package co.edu.ufps.legal_cases.security.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.ufps.legal_cases.security.model.PasswordResetToken;
import co.edu.ufps.legal_cases.security.model.UsuarioSistema;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHashAndUsadoFalse(String tokenHash);

    List<PasswordResetToken> findByUsuarioSistemaAndUsadoFalse(UsuarioSistema usuarioSistema);
}