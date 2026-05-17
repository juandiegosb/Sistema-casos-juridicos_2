package co.edu.ufps.legal_cases.security.service.auth;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarEmail;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.common.exception.BusinessException;
import co.edu.ufps.legal_cases.common.service.EmailService;
import co.edu.ufps.legal_cases.security.dto.auth.RestablecerPasswordDTO;
import co.edu.ufps.legal_cases.security.dto.auth.SolicitarRecuperacionPasswordDTO;
import co.edu.ufps.legal_cases.security.model.account.UsuarioSistema;
import co.edu.ufps.legal_cases.security.model.auth.PasswordResetToken;
import co.edu.ufps.legal_cases.security.repository.account.UsuarioSistemaRepository;
import co.edu.ufps.legal_cases.security.repository.auth.PasswordResetTokenRepository;
import co.edu.ufps.legal_cases.security.service.account.PerfilUsuarioResolverService;

@Service
public class PasswordResetService {

    // Para generar tokens de recuperacion seguros
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UsuarioSistemaRepository usuarioSistemaRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PerfilUsuarioResolverService perfilUsuarioResolverService;
    private final EmailTemplateService emailTemplateService;

    private final String resetPasswordUrl;
    private final long expirationMinutes;

    public PasswordResetService(
            UsuarioSistemaRepository usuarioSistemaRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            PerfilUsuarioResolverService perfilUsuarioResolverService,
            EmailTemplateService emailTemplateService,
            @Value("${app.frontend.reset-password-url}") String resetPasswordUrl,
            @Value("${app.password-reset.expiration-minutes}") long expirationMinutes) {

        this.usuarioSistemaRepository = usuarioSistemaRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.perfilUsuarioResolverService = perfilUsuarioResolverService;
        this.emailTemplateService = emailTemplateService;
        this.resetPasswordUrl = resetPasswordUrl;
        this.expirationMinutes = expirationMinutes;
    }

    @Transactional
    public void solicitarRecuperacion(SolicitarRecuperacionPasswordDTO dto) {
        String username = normalizarEmail(dto.getUsername());

        if (username == null) {
            throw new BusinessException("El correo es obligatorio");
        }

        // Busca el usuario con rol cargado para poder validar si puede recuperar
        // contraseña.
        // El perfil real ya no se carga desde usuario_sistema, ahora se resuelve con
        // PerfilUsuarioResolverService usando tipo_perfil_actual y usuario_sistema_id
        // en la tabla real.
        UsuarioSistema usuario = usuarioSistemaRepository
                .findForPasswordResetByUsernameIgnoreCase(username)
                .orElse(null);

        // No se informa si el usuario existe o no, para evitar enumeracion de correos
        if (usuario == null || !usuarioPuedeRecuperarPassword(usuario)) {
            return;
        }

        // Invalida tokens anteriores para que solo quede valido el ultimo enlace
        // enviado
        invalidarTokensAnteriores(usuario);

        String token = generarTokenSeguro();

        // No se guarda el token real, se guarda su hash con SHA-256
        String tokenHash = generarHashToken(token);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setTokenHash(tokenHash);
        resetToken.setUsuarioSistema(usuario);
        resetToken.setFechaCreacion(LocalDateTime.now());

        // La expiracion viene desde application.properties
        resetToken.setFechaExpiracion(LocalDateTime.now().plusMinutes(expirationMinutes));
        resetToken.setUsado(false);

        passwordResetTokenRepository.save(resetToken);

        // Construye el enlace del frontend con el token como parametro
        String enlace = construirEnlace(token);

        String html = emailTemplateService.construirRecuperacionPassword(enlace);

        emailService.enviarHtml(
                usuario.getUsername(),
                null, // No se envia el nombre del destinatario
                "Recuperación de contraseña",
                html,
                "recuperación de contraseña");
    }

    @Transactional
    public void restablecerPassword(RestablecerPasswordDTO dto) {
        validarSolicitudRestablecimiento(dto);

        // Se genera el hash del token recibido para compararlo con el hash guardado en
        // BD
        String tokenHash = generarHashToken(dto.getToken());

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHashAndUsadoFalse(tokenHash)
                .orElseThrow(() -> new BusinessException("Token inválido o expirado"));

        // Si el token ya expiro, se marca como usado para que no vuelva a intentarse
        // usar
        if (resetToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            resetToken.setUsado(true);
            resetToken.setFechaUso(LocalDateTime.now());
            passwordResetTokenRepository.save(resetToken);

            throw new BusinessException("Token inválido o expirado");
        }

        UsuarioSistema usuario = resetToken.getUsuarioSistema();

        // Valida que el usuario, su rol y su perfil sigan activos
        if (!usuarioPuedeRecuperarPassword(usuario)) {
            throw new BusinessException("Token inválido o expirado");
        }

        // Evita que la nueva contraseña sea igual a la actual
        if (passwordEncoder.matches(dto.getPasswordNueva(), usuario.getPasswordHash())) {
            throw new BusinessException("La nueva contraseña no puede ser igual a la actual");
        }

        // Cifra la nueva contraseña antes de guardarla
        usuario.setPasswordHash(passwordEncoder.encode(dto.getPasswordNueva()));

        // Marca el token como usado para que no pueda reutilizarse
        resetToken.setUsado(true);
        resetToken.setFechaUso(LocalDateTime.now());

        usuarioSistemaRepository.save(usuario);
        passwordResetTokenRepository.save(resetToken);
    }

    // Valida los datos enviados para restablecer la contraseña
    private void validarSolicitudRestablecimiento(RestablecerPasswordDTO dto) {
        if (dto.getToken() == null || dto.getToken().isBlank()) {
            throw new BusinessException("El token es obligatorio");
        }

        if (dto.getPasswordNueva() == null || dto.getPasswordNueva().isBlank()) {
            throw new BusinessException("La nueva contraseña es obligatoria");
        }

        if (dto.getConfirmarPassword() == null || dto.getConfirmarPassword().isBlank()) {
            throw new BusinessException("La confirmación de contraseña es obligatoria");
        }

        if (dto.getPasswordNueva().length() < 8 || dto.getPasswordNueva().length() > 100) {
            throw new BusinessException("La nueva contraseña debe tener entre 8 y 100 caracteres");
        }

        if (!dto.getPasswordNueva().equals(dto.getConfirmarPassword())) {
            throw new BusinessException("La nueva contraseña y su confirmación no coinciden");
        }
    }

    // Marca como usados los tokens anteriores del usuario
    private void invalidarTokensAnteriores(UsuarioSistema usuario) {
        List<PasswordResetToken> tokensActivos = passwordResetTokenRepository
                .findByUsuarioSistemaAndUsadoFalse(usuario);

        LocalDateTime ahora = LocalDateTime.now();

        tokensActivos.forEach(token -> {
            token.setUsado(true);
            token.setFechaUso(ahora);
        });

        passwordResetTokenRepository.saveAll(tokensActivos);
    }

    // Valida si el usuario tiene todo activo para poder recuperar contraseña
    private boolean usuarioPuedeRecuperarPassword(UsuarioSistema usuario) {
        return usuario != null
                && usuarioActivo(usuario)
                && rolActivo(usuario)
                && perfilActivo(usuario);
    }

    private boolean usuarioActivo(UsuarioSistema usuario) {
        return Boolean.TRUE.equals(usuario.getActivo());
    }

    private boolean rolActivo(UsuarioSistema usuario) {
        return usuario.getRol() != null
                && Boolean.TRUE.equals(usuario.getRol().getActivo());
    }

    // Valida que el perfil real asociado al usuario tambien este activo
    private boolean perfilActivo(UsuarioSistema usuario) {
        // Nueva validación normalizada.
        // Ya no depende de asesor_id, estudiante_id, monitor_id, administrativo_id
        // ni conciliador_id dentro de usuario_sistema.
        return perfilUsuarioResolverService.tienePerfilActivo(usuario);
    }

    // Genera un token aleatorio y seguro para enviarlo por correo
    private String generarTokenSeguro() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    // Convierte el token real en un hash SHA-256 para guardarlo en base de datos
    private String generarHashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new BusinessException("No se pudo procesar el token de recuperación");
        }
    }

    // Construye el enlace que se enviara al correo del usuario
    private String construirEnlace(String token) {
        String tokenCodificado = URLEncoder.encode(token, StandardCharsets.UTF_8);
        return resetPasswordUrl + "?token=" + tokenCodificado;
    }
}