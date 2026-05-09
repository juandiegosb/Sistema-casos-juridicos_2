package co.edu.ufps.legal_cases.security.service.access;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.exception.BusinessException;
import co.edu.ufps.legal_cases.security.dto.access.PermisoDTO;
import co.edu.ufps.legal_cases.security.dto.access.RolDTO;
import co.edu.ufps.legal_cases.security.model.access.Permiso;
import co.edu.ufps.legal_cases.security.model.access.Rol;
import co.edu.ufps.legal_cases.security.repository.access.PermisoRepository;
import co.edu.ufps.legal_cases.security.repository.access.RolRepository;

import static co.edu.ufps.legal_cases.util.ComparacionUtils.equalsIgnoreCase;
import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarTexto;

@Service
public class RolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    public RolService(RolRepository rolRepository, PermisoRepository permisoRepository) {
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
    }

    // Le digo a spring que solo voy a leer datos sin modificar nada
    @Transactional(readOnly = true)
    public List<RolDTO> listar() {
        return rolRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RolDTO> listarActivos() {
        return rolRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public RolDTO obtenerPorId(Long id) {
        Rol rol = buscarRolConPermisos(id);
        return convertirADTO(rol);
    }

    //Le digo que todo lo que esta aqui se debe ejecutar dentro de la transaccion
    @Transactional
    public RolDTO crear(RolDTO dto) {
        if (dto.getId() != null) {
            throw new BusinessException("El id no debe enviarse en la creación");
        }

        String nombre = normalizarTexto(dto.getNombre());
        String descripcion = normalizarTexto(dto.getDescripcion());

        if (nombre == null) {
            throw new BusinessException("El nombre del rol es obligatorio");
        }

        if (rolRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessException("Ya existe un rol con ese nombre");
        }

        // Se cargan únicamente permisos activos para evitar asignar permisos deshabilitados.
        Set<Permiso> permisos = obtenerPermisosActivos(dto.getPermisoIds());

        Rol rol = new Rol();
        rol.setNombre(nombre);
        rol.setDescripcion(descripcion);
        rol.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        rol.setPermisos(permisos);

        return convertirADTO(rolRepository.save(rol));
    }

    @Transactional
    public RolDTO actualizar(Long id, RolDTO dto) {
        Rol rolExistente = buscarRolConPermisos(id);

        String nombreNuevo = normalizarTexto(dto.getNombre());
        String descripcionNueva = normalizarTexto(dto.getDescripcion());
        Boolean activoNuevo = dto.getActivo() != null ? dto.getActivo() : rolExistente.getActivo();

        if (dto.getId() != null && !dto.getId().equals(rolExistente.getId())) {
            throw new BusinessException("No se permite cambiar el id del rol");
        }

        if (nombreNuevo == null) {
            throw new BusinessException("El nombre del rol es obligatorio");
        }

        if (rolRepository.existsByNombreIgnoreCaseAndIdNot(nombreNuevo, rolExistente.getId())) {
            throw new BusinessException("Ya existe un rol con ese nombre");
        }

        // Si se envían permisos, se reemplaza la lista actual; si no se envían, se conservan.
        Set<Permiso> permisosNuevos = dto.getPermisoIds() != null
                ? obtenerPermisosActivos(dto.getPermisoIds())
                : rolExistente.getPermisos();

        boolean sinCambios =
                equalsIgnoreCase(rolExistente.getNombre(), nombreNuevo)
                && equalsIgnoreCase(rolExistente.getDescripcion(), descripcionNueva)
                && Objects.equals(rolExistente.getActivo(), activoNuevo)
                && mismosPermisos(rolExistente.getPermisos(), permisosNuevos);

        if (sinCambios) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        rolExistente.setNombre(nombreNuevo);
        rolExistente.setDescripcion(descripcionNueva);
        rolExistente.setActivo(activoNuevo);

        if (dto.getPermisoIds() != null) {
            rolExistente.setPermisos(permisosNuevos);
        }

        return convertirADTO(rolRepository.save(rolExistente));
    }

    @Transactional
    public RolDTO cambiarEstado(Long id, Boolean activo) {
        Rol rol = buscarRolConPermisos(id);

        if (activo == null) {
            throw new BusinessException("El estado del rol es obligatorio");
        }

        if (Objects.equals(rol.getActivo(), activo)) {
            throw new BusinessException("El rol ya tiene ese estado");
        }

        rol.setActivo(activo);

        return convertirADTO(rolRepository.save(rol));
    }

    @Transactional
    public RolDTO asignarPermiso(Long rolId, Long permisoId) {
        Rol rol = buscarRolConPermisos(rolId);
        Permiso permiso = obtenerPermisoActivo(permisoId);

        // Evita repetir el mismo permiso dentro del rol.
        boolean yaAsignado = rol.getPermisos()
                .stream()
                .anyMatch(p -> p.getId().equals(permiso.getId()));

        if (yaAsignado) {
            throw new BusinessException("El permiso ya está asignado al rol");
        }

        rol.getPermisos().add(permiso);

        return convertirADTO(rolRepository.save(rol));
    }

    @Transactional
    public RolDTO quitarPermiso(Long rolId, Long permisoId) {
        Rol rol = buscarRolConPermisos(rolId);

        boolean eliminado = rol.getPermisos()
                .removeIf(permiso -> permiso.getId().equals(permisoId));

        if (!eliminado) {
            throw new BusinessException("El permiso no está asignado al rol");
        }

        return convertirADTO(rolRepository.save(rol));
    }

    // Busca el rol con sus permisos para evitar problemas por carga perezosa.
    private Rol buscarRolConPermisos(Long id) {
        return rolRepository.findWithPermisosById(id)
                .orElseThrow(() -> new BusinessException("Rol no encontrado con id: " + id));
    }

    // Valida que el permiso exista y esté activo antes de asociarlo a un rol.
    private Permiso obtenerPermisoActivo(Long permisoId) {
        if (permisoId == null) {
            throw new BusinessException("El permiso es obligatorio");
        }

        return permisoRepository.findByIdAndActivoTrue(permisoId)
                .orElseThrow(() -> new BusinessException("Permiso no encontrado o inactivo con id: " + permisoId));
    }

    // Convierte los ids recibidos en entidades Permiso válidas y activas.
    private Set<Permiso> obtenerPermisosActivos(Set<Long> permisoIds) {
        if (permisoIds == null || permisoIds.isEmpty()) {
            return new HashSet<>();
        }

        if (permisoIds.contains(null)) {
            throw new BusinessException("La lista de permisos contiene valores nulos");
        }

        List<Permiso> permisos = permisoRepository.findByIdInAndActivoTrue(permisoIds);

        if (permisos.size() != permisoIds.size()) {
            throw new BusinessException("Uno o más permisos no existen o están inactivos");
        }

        return new HashSet<>(permisos);
    }

    // Compara permisos por id para saber si realmente hubo cambios.
    private boolean mismosPermisos(Set<Permiso> actuales, Set<Permiso> nuevos) {
        //Aqui el set ayuda a ignorar el orden y comparar solo por id
        Set<Long> idsActuales = actuales.stream()
                .map(Permiso::getId)
                .collect(Collectors.toSet());

        Set<Long> idsNuevos = nuevos.stream()
                .map(Permiso::getId)
                .collect(Collectors.toSet());

        return Objects.equals(idsActuales, idsNuevos);
    }

    private RolDTO convertirADTO(Rol rol) {
        RolDTO dto = new RolDTO();
        dto.setId(rol.getId());
        dto.setNombre(rol.getNombre());
        dto.setDescripcion(rol.getDescripcion());
        dto.setActivo(rol.getActivo());

        Set<Long> permisoIds = rol.getPermisos()
                .stream()
                .map(Permiso::getId)
                .collect(Collectors.toSet());

        List<PermisoDTO> permisos = rol.getPermisos()
                .stream()
                .sorted(Comparator.comparing(Permiso::getNombre))
                .map(this::convertirPermisoADTO)
                .toList();

        dto.setPermisoIds(permisoIds);
        dto.setPermisos(permisos);

        return dto;
    }

    private PermisoDTO convertirPermisoADTO(Permiso permiso) {
        PermisoDTO dto = new PermisoDTO();
        dto.setId(permiso.getId());
        dto.setNombre(permiso.getNombre());
        dto.setDescripcion(permiso.getDescripcion());
        dto.setActivo(permiso.getActivo());
        return dto;
    }
}