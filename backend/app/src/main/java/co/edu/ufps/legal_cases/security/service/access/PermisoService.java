package co.edu.ufps.legal_cases.security.service.access;

import static co.edu.ufps.legal_cases.common.util.ComparacionUtils.equalsIgnoreCase;
import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import co.edu.ufps.legal_cases.common.exception.BusinessException;
import co.edu.ufps.legal_cases.security.dto.access.PermisoDTO;
import co.edu.ufps.legal_cases.security.model.access.Permiso;
import co.edu.ufps.legal_cases.security.repository.access.PermisoRepository;

@Service
public class PermisoService {

    private final PermisoRepository permisoRepository;

    public PermisoService(PermisoRepository permisoRepository) {
        this.permisoRepository = permisoRepository;
    }

    public List<PermisoDTO> listar() {
        return permisoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<PermisoDTO> listarActivos() {
        return permisoRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public PermisoDTO obtenerPorId(Long id) {
        Permiso permiso = permisoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Permiso no encontrado con id: " + id));

        return convertirADTO(permiso);
    }

    public PermisoDTO crear(PermisoDTO dto) {
        if (dto.getId() != null) {
            throw new BusinessException("El id no debe enviarse en la creación");
        }

        String nombre = normalizarTexto(dto.getNombre());
        String descripcion = normalizarTexto(dto.getDescripcion());

        if (nombre == null) {
            throw new BusinessException("El nombre del permiso es obligatorio");
        }

        if (permisoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessException("Ya existe un permiso con ese nombre");
        }

        Permiso permiso = new Permiso();
        permiso.setNombre(nombre);
        permiso.setDescripcion(descripcion);
        permiso.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        return convertirADTO(permisoRepository.save(permiso));
    }

    public PermisoDTO actualizar(Long id, PermisoDTO dto) {
        Permiso permisoExistente = permisoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Permiso no encontrado con id: " + id));

        String nombreNuevo = normalizarTexto(dto.getNombre());
        String descripcionNueva = normalizarTexto(dto.getDescripcion());
        Boolean activoNuevo = dto.getActivo() != null ? dto.getActivo() : permisoExistente.getActivo();

        if (dto.getId() != null && !dto.getId().equals(permisoExistente.getId())) {
            throw new BusinessException("No se permite cambiar el id del permiso");
        }

        if (nombreNuevo == null) {
            throw new BusinessException("El nombre del permiso es obligatorio");
        }

        if (permisoRepository.existsByNombreIgnoreCaseAndIdNot(nombreNuevo, permisoExistente.getId())) {
            throw new BusinessException("Ya existe un permiso con ese nombre");
        }

        boolean sinCambios =
        //Estos 2 metodos estan optimizados para comparar cadenas de texto ignorando mayúsculas, minúsculas y espacios en blanco
                equalsIgnoreCase(permisoExistente.getNombre(), nombreNuevo)
                && equalsIgnoreCase(permisoExistente.getDescripcion(), descripcionNueva)
                && Objects.equals(permisoExistente.getActivo(), activoNuevo);

        if (sinCambios) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        permisoExistente.setNombre(nombreNuevo);
        permisoExistente.setDescripcion(descripcionNueva);
        permisoExistente.setActivo(activoNuevo);

        return convertirADTO(permisoRepository.save(permisoExistente));
    }

    public PermisoDTO cambiarEstado(Long id, Boolean activo) {
        Permiso permiso = permisoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Permiso no encontrado con id: " + id));

        if (activo == null) {
            throw new BusinessException("El estado activo es obligatorio");
        }

        if (Objects.equals(permiso.getActivo(), activo)) {
            throw new BusinessException("El permiso ya tiene ese estado");
        }

        permiso.setActivo(activo);
        return convertirADTO(permisoRepository.save(permiso));
    }

    private PermisoDTO convertirADTO(Permiso permiso) {
        PermisoDTO dto = new PermisoDTO();
        dto.setId(permiso.getId());
        dto.setNombre(permiso.getNombre());
        dto.setDescripcion(permiso.getDescripcion());
        dto.setActivo(permiso.getActivo());
        return dto;
    }
}