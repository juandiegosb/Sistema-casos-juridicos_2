package co.edu.ufps.legal_cases.security.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.exception.BusinessException;
import co.edu.ufps.legal_cases.security.dto.LoginRequestDTO;
import co.edu.ufps.legal_cases.security.dto.LoginResponseDTO;
import co.edu.ufps.legal_cases.security.dto.LoginResultDTO;
import co.edu.ufps.legal_cases.security.model.Permiso;
import co.edu.ufps.legal_cases.security.model.UsuarioSistema;
import co.edu.ufps.legal_cases.security.repository.UsuarioSistemaRepository;

import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarEmail;

@Service
public class AuthService {

    private final UsuarioSistemaRepository usuarioSistemaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioSistemaRepository usuarioSistemaRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.usuarioSistemaRepository = usuarioSistemaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResultDTO login(LoginRequestDTO dto) {
        String username = normalizarEmail(dto.getUsername());

        if (username == null) {
            throw new BusinessException("El correo es obligatorio");
        }

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new BusinessException("La contraseña es obligatoria");
        }

        UsuarioSistema usuario = usuarioSistemaRepository
                .findWithRolAndPermisosByUsernameIgnoreCase(username)
                .orElseThrow(() -> new BusinessException("Usuario o contraseña incorrectos"));

        validarUsuarioActivo(usuario);
        validarRolActivo(usuario);
        validarPerfilActivo(usuario);

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPasswordHash())) {
            throw new BusinessException("Usuario o contraseña incorrectos");
        }

        List<String> permisos = usuario.getRol().getPermisos()
                .stream()
                .filter(permiso -> Boolean.TRUE.equals(permiso.getActivo()))
                //Ordena por los nombres de los permisos
                .sorted(Comparator.comparing(Permiso::getNombre))
                .map(Permiso::getNombre)
                .toList();

        LoginResponseDTO response = new LoginResponseDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getRol().getId(),
                usuario.getRol().getNombre(),
                obtenerPerfilId(usuario),
                obtenerTipoPerfil(usuario),
                permisos
        );

        String token = jwtService.generarToken(usuario.getUsername());

        return new LoginResultDTO(response, token);
    }

    private void validarUsuarioActivo(UsuarioSistema usuario) {
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new BusinessException("El usuario se encuentra inactivo");
        }
    }

    private void validarRolActivo(UsuarioSistema usuario) {
        if (usuario.getRol() == null || !Boolean.TRUE.equals(usuario.getRol().getActivo())) {
            throw new BusinessException("El rol del usuario se encuentra inactivo");
        }
    }

    // Se valida que el perfil real asociado también esté activo.
    private void validarPerfilActivo(UsuarioSistema usuario) {
        if (usuario.getAsesor() != null && !Boolean.TRUE.equals(usuario.getAsesor().getActivo())) {
            throw new BusinessException("El asesor asociado se encuentra inactivo");
        }

        if (usuario.getEstudiante() != null && !Boolean.TRUE.equals(usuario.getEstudiante().getActivo())) {
            throw new BusinessException("El estudiante asociado se encuentra inactivo");
        }

        if (usuario.getMonitor() != null && !Boolean.TRUE.equals(usuario.getMonitor().getActivo())) {
            throw new BusinessException("El monitor asociado se encuentra inactivo");
        }

        if (usuario.getAdministrativo() != null && !Boolean.TRUE.equals(usuario.getAdministrativo().getActivo())) {
            throw new BusinessException("El administrativo asociado se encuentra inactivo");
        }

        if (usuario.getConciliador() != null && !Boolean.TRUE.equals(usuario.getConciliador().getActivo())) {
            throw new BusinessException("El conciliador asociado se encuentra inactivo");
        }
    }

    private Long obtenerPerfilId(UsuarioSistema usuario) {
        if (usuario.getAsesor() != null) {
            return usuario.getAsesor().getId();
        }

        if (usuario.getEstudiante() != null) {
            return usuario.getEstudiante().getId();
        }

        if (usuario.getMonitor() != null) {
            return usuario.getMonitor().getId();
        }

        if (usuario.getAdministrativo() != null) {
            return usuario.getAdministrativo().getId();
        }

        if (usuario.getConciliador() != null) {
            return usuario.getConciliador().getId();
        }

        return null;
    }

    private String obtenerTipoPerfil(UsuarioSistema usuario) {
        if (usuario.getAsesor() != null) {
            return "ASESOR";
        }

        if (usuario.getEstudiante() != null) {
            return "ESTUDIANTE";
        }

        if (usuario.getMonitor() != null) {
            return "MONITOR";
        }

        if (usuario.getAdministrativo() != null) {
            return "ADMINISTRATIVO";
        }

        if (usuario.getConciliador() != null) {
            return "CONCILIADOR";
        }

        return "SIN_PERFIL";
    }
}