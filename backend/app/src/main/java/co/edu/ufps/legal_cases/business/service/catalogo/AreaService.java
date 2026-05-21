package co.edu.ufps.legal_cases.business.service.catalogo;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.catalogo.AreaDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Area;
import co.edu.ufps.legal_cases.business.repository.catalogo.AreaRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;

@Service
public class AreaService {

    private final AreaRepository areaRepository;

    public AreaService(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    // Lista áreas activas para formularios, selects y uso normal del sistema.
    @Transactional(readOnly = true)
    public List<AreaDTO> listar() {
        return areaRepository.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // Lista todas las áreas para administración del catálogo.
    @Transactional(readOnly = true)
    public List<AreaDTO> listarTodos() {
        return areaRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public AreaDTO obtenerPorId(Long id) {
        Area area = buscarPorIdActivo(id);
        return convertirADTO(area);
    }

    @Transactional
    public AreaDTO crear(AreaDTO areaDTO) {
        validarCreacion(areaDTO);

        String nombre = normalizarNombre(areaDTO.getNombre());

        if (areaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessException("Ya existe un área con ese nombre");
        }

        Area area = new Area();
        area.setNombre(nombre);
        area.setActivo(true);

        return convertirADTO(areaRepository.save(area));
    }

    @Transactional
    public AreaDTO actualizar(Long id, AreaDTO areaDTO) {
        Area area = buscarPorId(id);

        validarActualizacion(id, areaDTO);

        String nuevoNombre = normalizarNombre(areaDTO.getNombre());

        if (areaRepository.existsByNombreIgnoreCaseAndIdNot(nuevoNombre, area.getId())) {
            throw new BusinessException("Ya existe un área con ese nombre");
        }

        boolean sinCambios = Objects.equals(area.getNombre(), nuevoNombre);

        if (sinCambios) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        area.setNombre(nuevoNombre);

        return convertirADTO(areaRepository.save(area));
    }

    @Transactional
    public AreaDTO cambiarEstado(Long id, Boolean activo) {
        Area area = buscarPorId(id);

        if (activo == null) {
            throw new BusinessException("El estado activo es obligatorio");
        }

        if (Objects.equals(area.getActivo(), activo)) {
            throw new BusinessException("El área ya tiene ese estado");
        }

        area.setActivo(activo);

        return convertirADTO(areaRepository.save(area));
    }

    @Transactional
    public void eliminar(Long id) {
        Area area = buscarPorIdActivo(id);

        // Desactivación lógica: se conserva porque puede estar asociada a temas y consultas.
        area.setActivo(false);

        areaRepository.save(area);
    }

    private void validarCreacion(AreaDTO areaDTO) {
        validarDtoObligatorio(areaDTO);

        if (areaDTO.getId() != null) {
            throw new BusinessException("El id no debe enviarse en la creación");
        }
    }

    private void validarActualizacion(Long id, AreaDTO areaDTO) {
        validarDtoObligatorio(areaDTO);

        if (areaDTO.getId() != null && !Objects.equals(areaDTO.getId(), id)) {
            throw new BusinessException("No se permite cambiar el id del área");
        }
    }

    private void validarDtoObligatorio(AreaDTO areaDTO) {
        if (areaDTO == null) {
            throw new BusinessException("Los datos del área son obligatorios");
        }
    }

    private String normalizarNombre(String nombre) {
        String nombreNormalizado = normalizarTexto(nombre);

        if (nombreNormalizado == null || nombreNormalizado.isBlank()) {
            throw new BusinessException("El nombre del área es obligatorio");
        }

        if (nombreNormalizado.length() > 50) {
            throw new BusinessException("El nombre no puede superar los 50 caracteres");
        }

        return nombreNormalizado;
    }

    private Area buscarPorId(Long id) {
        if (id == null) {
            throw new BusinessException("El id del área es obligatorio");
        }

        return areaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Área no encontrada con id: " + id));
    }

    private Area buscarPorIdActivo(Long id) {
        if (id == null) {
            throw new BusinessException("El id del área es obligatorio");
        }

        return areaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException("Área no encontrada o inactiva con id: " + id));
    }

    private AreaDTO convertirADTO(Area area) {
        return new AreaDTO(
                area.getId(),
                area.getNombre(),
                area.getActivo());
    }
}