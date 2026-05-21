package co.edu.ufps.legal_cases.business.service.catalogo;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.catalogo.TemaDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Area;
import co.edu.ufps.legal_cases.business.model.catalogo.Tema;
import co.edu.ufps.legal_cases.business.repository.catalogo.AreaRepository;
import co.edu.ufps.legal_cases.business.repository.catalogo.TemaRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;

@Service
public class TemaService {

    private final TemaRepository temaRepository;
    private final AreaRepository areaRepository;

    public TemaService(TemaRepository temaRepository, AreaRepository areaRepository) {
        this.temaRepository = temaRepository;
        this.areaRepository = areaRepository;
    }

    // Lista temas activos para formularios, selects y uso normal del sistema.
    @Transactional(readOnly = true)
    public List<TemaDTO> listar() {
        return temaRepository.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // Lista todos los temas para administración del catálogo.
    @Transactional(readOnly = true)
    public List<TemaDTO> listarTodos() {
        return temaRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // Lista temas activos de un área activa.
    @Transactional(readOnly = true)
    public List<TemaDTO> listarPorArea(Long areaId) {
        validarAreaActiva(areaId);

        return temaRepository.findByAreaIdAndActivoTrueOrderByNombreAsc(areaId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // Lista todos los temas de un área para administración.
    @Transactional(readOnly = true)
    public List<TemaDTO> listarTodosPorArea(Long areaId) {
        validarAreaExistente(areaId);

        return temaRepository.findByAreaIdOrderByNombreAsc(areaId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public TemaDTO obtenerPorId(Long id) {
        Tema tema = buscarPorIdActivo(id);
        return convertirADTO(tema);
    }

    @Transactional
    public TemaDTO crear(TemaDTO temaDTO) {
        validarCreacion(temaDTO);

        String nombre = normalizarNombre(temaDTO.getNombre());
        Area area = obtenerAreaActiva(temaDTO.getAreaId());

        if (temaRepository.existsByNombreIgnoreCaseAndAreaId(nombre, area.getId())) {
            throw new BusinessException("Ya existe un tema con ese nombre en el área seleccionada");
        }

        Tema tema = new Tema();
        tema.setNombre(nombre);
        tema.setArea(area);
        tema.setActivo(true);

        return convertirADTO(temaRepository.save(tema));
    }

    @Transactional
    public TemaDTO actualizar(Long id, TemaDTO temaDTO) {
        Tema tema = buscarPorId(id);

        validarActualizacion(id, temaDTO);

        String nuevoNombre = normalizarNombre(temaDTO.getNombre());
        Area nuevaArea = obtenerAreaActiva(temaDTO.getAreaId());

        if (temaRepository.existsByNombreIgnoreCaseAndAreaIdAndIdNot(
                nuevoNombre,
                nuevaArea.getId(),
                tema.getId())) {
            throw new BusinessException("Ya existe un tema con ese nombre en el área seleccionada");
        }

        boolean mismoNombre = Objects.equals(tema.getNombre(), nuevoNombre);
        boolean mismaArea = tema.getArea() != null
                && Objects.equals(tema.getArea().getId(), nuevaArea.getId());

        if (mismoNombre && mismaArea) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        tema.setNombre(nuevoNombre);
        tema.setArea(nuevaArea);

        return convertirADTO(temaRepository.save(tema));
    }

    @Transactional
    public TemaDTO cambiarEstado(Long id, Boolean activo) {
        Tema tema = buscarPorId(id);

        if (activo == null) {
            throw new BusinessException("El estado activo es obligatorio");
        }

        if (Objects.equals(tema.getActivo(), activo)) {
            throw new BusinessException("El tema ya tiene ese estado");
        }

        tema.setActivo(activo);

        return convertirADTO(temaRepository.save(tema));
    }

    @Transactional
    public void eliminar(Long id) {
        Tema tema = buscarPorIdActivo(id);

        // Desactivación lógica: se conserva porque puede estar asociado a tipos y consultas.
        tema.setActivo(false);

        temaRepository.save(tema);
    }

    private void validarCreacion(TemaDTO temaDTO) {
        validarDtoObligatorio(temaDTO);

        if (temaDTO.getId() != null) {
            throw new BusinessException("El id no debe enviarse en la creación");
        }
    }

    private void validarActualizacion(Long id, TemaDTO temaDTO) {
        validarDtoObligatorio(temaDTO);

        if (temaDTO.getId() != null && !Objects.equals(temaDTO.getId(), id)) {
            throw new BusinessException("No se permite cambiar el id del tema");
        }
    }

    private void validarDtoObligatorio(TemaDTO temaDTO) {
        if (temaDTO == null) {
            throw new BusinessException("Los datos del tema son obligatorios");
        }

        if (temaDTO.getAreaId() == null) {
            throw new BusinessException("El área es obligatoria");
        }
    }

    private String normalizarNombre(String nombre) {
        String nombreNormalizado = normalizarTexto(nombre);

        if (nombreNormalizado == null || nombreNormalizado.isBlank()) {
            throw new BusinessException("El nombre del tema es obligatorio");
        }

        if (nombreNormalizado.length() > 80) {
            throw new BusinessException("El nombre del tema no puede superar los 80 caracteres");
        }

        return nombreNormalizado;
    }

    private void validarAreaActiva(Long areaId) {
        obtenerAreaActiva(areaId);
    }

    private void validarAreaExistente(Long areaId) {
        if (areaId == null) {
            throw new BusinessException("El área es obligatoria");
        }

        areaRepository.findById(areaId)
                .orElseThrow(() -> new BusinessException("Área no encontrada con id: " + areaId));
    }

    private Area obtenerAreaActiva(Long areaId) {
        if (areaId == null) {
            throw new BusinessException("El área es obligatoria");
        }

        return areaRepository.findByIdAndActivoTrue(areaId)
                .orElseThrow(() -> new BusinessException("Área no encontrada o inactiva con id: " + areaId));
    }

    private Tema buscarPorId(Long id) {
        if (id == null) {
            throw new BusinessException("El id del tema es obligatorio");
        }

        return temaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tema no encontrado con id: " + id));
    }

    private Tema buscarPorIdActivo(Long id) {
        if (id == null) {
            throw new BusinessException("El id del tema es obligatorio");
        }

        return temaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException("Tema no encontrado o inactivo con id: " + id));
    }

    private TemaDTO convertirADTO(Tema tema) {
        return new TemaDTO(
                tema.getId(),
                tema.getNombre(),
                tema.getArea().getId(),
                tema.getActivo());
    }
}