package co.edu.ufps.legal_cases.security.service.auth;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarEmail;

import java.util.Comparator;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.exception.BusinessException;
import co.edu.ufps.legal_cases.security.dto.account.PerfilUsuarioActual;
import co.edu.ufps.legal_cases.security.dto.account.UsuarioSistemaDTO;
import co.edu.ufps.legal_cases.security.dto.auth.CambiarPasswordRequestDTO;
import co.edu.ufps.legal_cases.security.dto.auth.LoginRequestDTO;
import co.edu.ufps.legal_cases.security.dto.auth.LoginResponseDTO;
import co.edu.ufps.legal_cases.security.dto.auth.LoginResultDTO;
import co.edu.ufps.legal_cases.security.model.access.Permiso;
import co.edu.ufps.legal_cases.security.model.account.UsuarioSistema;
import co.edu.ufps.legal_cases.security.repository.account.UsuarioSistemaRepository;
import co.edu.ufps.legal_cases.security.service.account.PerfilUsuarioResolverService;
import co.edu.ufps.legal_cases.security.service.jwt.JwtService;

@Service
public class AuthService {

    private final UsuarioSistemaRepository usuarioSistemaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PerfilUsuarioResolverService perfilUsuarioResolverService;

    public AuthService(
            UsuarioSistemaRepository usuarioSistemaRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            PerfilUsuarioResolverService perfilUsuarioResolverService) {
        this.usuarioSistemaRepository = usuarioSistemaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.perfilUsuarioResolverService = perfilUsuarioResolverService;
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

        // Se valida que el perfil real asociado también esté activo.
        // Ahora se resuelve con tipo_perfil_actual y usuario_sistema_id en la tabla real.
        PerfilUsuarioActual perfilActual = validarPerfilActivo(usuario);

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPasswordHash())) {
            throw new BusinessException("Usuario o contraseña incorrectos");
        }

        LoginResponseDTO response = new LoginResponseDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getRol().getId(),
                usuario.getRol().getNombre(),
                perfilActual.getPerfilId(),
                perfilActual.getTipoPerfil().name(),
                obtenerPermisosActivos(usuario));

        String token = jwtService.generarToken(usuario.getUsername());

        return new LoginResultDTO(response, token);
    }

    @Transactional(readOnly = true)
    public UsuarioSistemaDTO me(String token) {
        UsuarioSistema usuario = obtenerUsuarioAutenticado(token);

        return convertirAUsuarioSistemaDTO(usuario);
    }

    @Transactional
    public void cambiarPassword(String token, CambiarPasswordRequestDTO dto) {
        UsuarioSistema usuario = obtenerUsuarioAutenticado(token); // Con el token esta el usuario

        validarDatosCambioPassword(dto);
        validarPasswordActual(dto.getPasswordActual(), usuario);
        validarPasswordNuevaDiferente(dto.getPasswordNueva(), usuario);

        usuario.setPasswordHash(passwordEncoder.encode(dto.getPasswordNueva()));
        usuarioSistemaRepository.save(usuario);
    }

    // Obtiene el usuario logueado a partir del token enviado en la cookie.
    // También valida que el usuario, el rol y el perfil asociado estén activos.
    private UsuarioSistema obtenerUsuarioAutenticado(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException("No hay sesión activa");
        }

        // Si expiro o es invalido, el metodo lanza una excepcion
        String username = jwtService.obtenerUsername(token);

        UsuarioSistema usuario = usuarioSistemaRepository
                .findWithRolAndPermisosByUsernameIgnoreCase(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        validarUsuarioActivo(usuario);
        validarRolActivo(usuario);

        // Se valida con la nueva relación normalizada.
        validarPerfilActivo(usuario);

        return usuario;
    }

    // Valida que los campos necesarios para cambiar la contraseña sean correctos
    private void validarDatosCambioPassword(CambiarPasswordRequestDTO dto) {
        if (dto == null) {
            throw new BusinessException("Los datos para cambiar la contraseña son obligatorios");
        }

        if (dto.getPasswordActual() == null || dto.getPasswordActual().isBlank()) {
            throw new BusinessException("La contraseña actual es obligatoria");
        }

        if (dto.getPasswordNueva() == null || dto.getPasswordNueva().isBlank()) {
            throw new BusinessException("La nueva contraseña es obligatoria");
        }

        if (dto.getPasswordNueva().length() < 8 || dto.getPasswordNueva().length() > 100) {
            throw new BusinessException("La nueva contraseña debe tener entre 8 y 100 caracteres");
        }
    }

    // Verifica que la contraseña actual enviada coincida con la guardada en base de
    // datos
    private void validarPasswordActual(String passwordActual, UsuarioSistema usuario) {
        if (!passwordEncoder.matches(passwordActual, usuario.getPasswordHash())) {
            throw new BusinessException("La contraseña actual no es correcta");
        }
    }

    // Evita que la nueva contraseña sea igual a la contraseña actual
    private void validarPasswordNuevaDiferente(String passwordNueva, UsuarioSistema usuario) {
        if (passwordEncoder.matches(passwordNueva, usuario.getPasswordHash())) {
            throw new BusinessException("La nueva contraseña no puede ser igual a la actual");
        }
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
    // Esta validación ya no depende de asesor_id, estudiante_id, monitor_id,
    // administrativo_id ni conciliador_id en usuario_sistema.
    private PerfilUsuarioActual validarPerfilActivo(UsuarioSistema usuario) {
        return perfilUsuarioResolverService.obtenerPerfilActivoObligatorio(usuario);
    }

    // Obtiene solo los permisos activos del rol y los ordena por nombre
    private List<String> obtenerPermisosActivos(UsuarioSistema usuario) {
        return usuario.getRol().getPermisos()
                .stream()
                .filter(permiso -> Boolean.TRUE.equals(permiso.getActivo()))
                // Ordena por los nombres de los permisos
                .sorted(Comparator.comparing(Permiso::getNombre))
                .map(Permiso::getNombre)
                .toList();
    }

    private UsuarioSistemaDTO convertirAUsuarioSistemaDTO(UsuarioSistema usuario) {
        UsuarioSistemaDTO dto = new UsuarioSistemaDTO();

        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setActivo(usuario.getActivo());

        if (usuario.getRol() != null) {
            dto.setRolId(usuario.getRol().getId());
            dto.setRolNombre(usuario.getRol().getNombre());
            dto.setPermisos(obtenerPermisosActivos(usuario));
        }

        PerfilUsuarioActual perfilActual = validarPerfilActivo(usuario);

        dto.setPerfilId(perfilActual.getPerfilId());
        dto.setTipoPerfil(perfilActual.getTipoPerfil().name());

        return dto;
    }
}