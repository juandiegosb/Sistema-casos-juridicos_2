package co.edu.ufps.legal_cases.security.service.account;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.exception.BusinessException;
import co.edu.ufps.legal_cases.security.dto.account.PerfilUsuarioActual;
import co.edu.ufps.legal_cases.security.dto.account.UsuarioSistemaDTO;
import co.edu.ufps.legal_cases.security.model.access.Permiso;
import co.edu.ufps.legal_cases.security.model.account.UsuarioSistema;
import co.edu.ufps.legal_cases.security.repository.account.UsuarioSistemaRepository;

@Service
public class UsuarioSistemaService {

    private final UsuarioSistemaRepository usuarioSistemaRepository;
    private final PerfilUsuarioResolverService perfilUsuarioResolverService;

    public UsuarioSistemaService(
            UsuarioSistemaRepository usuarioSistemaRepository,
            PerfilUsuarioResolverService perfilUsuarioResolverService) {
        this.usuarioSistemaRepository = usuarioSistemaRepository;
        this.perfilUsuarioResolverService = perfilUsuarioResolverService;
    }

    @Transactional(readOnly = true)
    public List<UsuarioSistemaDTO> listar() {
        return usuarioSistemaRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioSistemaDTO> listarActivos() {
        return usuarioSistemaRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioSistemaDTO obtenerPorId(Long id) {
        UsuarioSistema usuario = buscarUsuarioConRolYPermisos(id);
        return convertirADTO(usuario);
    }

    @Transactional
    public UsuarioSistemaDTO cambiarEstado(Long id, Boolean activo) {
        UsuarioSistema usuario = buscarUsuarioConRolYPermisos(id);

        if (activo == null) {
            throw new BusinessException("El estado activo es obligatorio");
        }

        if (Objects.equals(usuario.getActivo(), activo)) {
            throw new BusinessException("El usuario ya tiene ese estado");
        }

        usuario.setActivo(activo);

        return convertirADTO(usuarioSistemaRepository.save(usuario));
    }

    private UsuarioSistema buscarUsuarioConRolYPermisos(Long id) {
        return usuarioSistemaRepository.findWithRolAndPermisosById(id)
                .orElseThrow(() -> new BusinessException("Usuario del sistema no encontrado con id: " + id));
    }

    private UsuarioSistemaDTO convertirADTO(UsuarioSistema usuario) {
        UsuarioSistemaDTO dto = new UsuarioSistemaDTO();

        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setActivo(usuario.getActivo());

        if (usuario.getRol() != null) {
            dto.setRolId(usuario.getRol().getId());
            dto.setRolNombre(usuario.getRol().getNombre());

            List<String> permisos = usuario.getRol().getPermisos()
                    .stream()
                    .filter(permiso -> Boolean.TRUE.equals(permiso.getActivo()))
                    .sorted(Comparator.comparing(Permiso::getNombre))
                    .map(Permiso::getNombre)
                    .toList();

            dto.setPermisos(permisos);
        }

        asignarPerfil(dto, usuario);

        return dto;
    }

    // Identifica a qué perfil real pertenece el usuario del sistema.
    private void asignarPerfil(UsuarioSistemaDTO dto, UsuarioSistema usuario) {
        try {
            // Nueva lectura normalizada.
            // Ya no depende de asesor_id, estudiante_id, monitor_id, administrativo_id
            // ni conciliador_id dentro de usuario_sistema.
            PerfilUsuarioActual perfilActual =
                    perfilUsuarioResolverService.obtenerPerfilActivoObligatorio(usuario);

            dto.setPerfilId(perfilActual.getPerfilId());
            dto.setTipoPerfil(perfilActual.getTipoPerfil().name());

        } catch (BusinessException ex) {
            dto.setPerfilId(null);
            dto.setTipoPerfil("SIN_PERFIL");
        }
    }
}