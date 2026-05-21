package co.edu.ufps.legal_cases.business.service.catalogo;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.catalogo.SedeDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Sede;
import co.edu.ufps.legal_cases.business.repository.catalogo.SedeRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;

@Service
public class SedeService {

    private final SedeRepository sedeRepository;

    public SedeService(SedeRepository sedeRepository) {
        this.sedeRepository = sedeRepository;
    }

    // Lista sedes activas para formularios y selects del frontend.
    @Transactional(readOnly = true)
    public List<SedeDTO> listar() {
        return sedeRepository.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // Lista todas las sedes para administración del catálogo.
    @Transactional(readOnly = true)
    public List<SedeDTO> listarTodos() {
        return sedeRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public SedeDTO obtenerPorId(Long id) {
        Sede sede = buscarPorIdActivo(id);
        return convertirADTO(sede);
    }

    @Transactional
    public SedeDTO crear(SedeDTO dto) {
        validarCreacion(dto);

        String nombre = normalizarNombre(dto.getNombre());

        if (sedeRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessException("Ya existe una sede con ese nombre");
        }

        Sede sede = new Sede();
        sede.setNombre(nombre);
        sede.setActivo(true);

        return convertirADTO(sedeRepository.save(sede));
    }

    @Transactional
    public SedeDTO actualizar(Long id, SedeDTO dto) {
        Sede sedeExistente = buscarPorId(id);

        validarActualizacion(id, dto);

        String nombreNuevo = normalizarNombre(dto.getNombre());

        if (sedeRepository.existsByNombreIgnoreCaseAndIdNot(nombreNuevo, sedeExistente.getId())) {
            throw new BusinessException("Ya existe una sede con ese nombre");
        }

        boolean sinCambios = Objects.equals(sedeExistente.getNombre(), nombreNuevo);

        if (sinCambios) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        sedeExistente.setNombre(nombreNuevo);

        return convertirADTO(sedeRepository.save(sedeExistente));
    }

    @Transactional
    public void eliminar(Long id) {
        Sede sede = buscarPorIdActivo(id);

        // Desactivación lógica: se conserva la sede porque puede estar asociada a
        // usuarios o consultas.
        sede.setActivo(false);

        sedeRepository.save(sede);
    }

    @Transactional
    public SedeDTO cambiarEstado(Long id, Boolean activo) {
        Sede sede = buscarPorId(id);

        if (activo == null) {
            throw new BusinessException("El estado activo es obligatorio");
        }

        if (Objects.equals(sede.getActivo(), activo)) {
            throw new BusinessException("La sede ya tiene ese estado");
        }

        sede.setActivo(activo);

        return convertirADTO(sedeRepository.save(sede));
    }

    private void validarCreacion(SedeDTO dto) {
        validarDtoObligatorio(dto);

        if (dto.getId() != null) {
            throw new BusinessException("El id no debe enviarse en la creación");
        }
    }

    private void validarActualizacion(Long id, SedeDTO dto) {
        validarDtoObligatorio(dto);

        if (dto.getId() != null && !Objects.equals(dto.getId(), id)) {
            throw new BusinessException("No se permite cambiar el id de la sede");
        }
    }

    private void validarDtoObligatorio(SedeDTO dto) {
        if (dto == null) {
            throw new BusinessException("Los datos de la sede son obligatorios");
        }
    }

    private String normalizarNombre(String nombre) {
        String nombreNormalizado = normalizarTexto(nombre);

        if (nombreNormalizado == null || nombreNormalizado.isBlank()) {
            throw new BusinessException("El nombre de la sede es obligatorio");
        }

        if (nombreNormalizado.length() > 100) {
            throw new BusinessException("El nombre de la sede no puede superar los 100 caracteres");
        }

        return nombreNormalizado;
    }

    private Sede buscarPorId(Long id) {
        if (id == null) {
            throw new BusinessException("El id de la sede es obligatorio");
        }

        return sedeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Sede no encontrada con id: " + id));
    }

    private Sede buscarPorIdActivo(Long id) {
        if (id == null) {
            throw new BusinessException("El id de la sede es obligatorio");
        }

        return sedeRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException("Sede no encontrada o inactiva con id: " + id));
    }

    private SedeDTO convertirADTO(Sede sede) {
        SedeDTO dto = new SedeDTO();
        dto.setId(sede.getId());
        dto.setNombre(sede.getNombre());
        dto.setActivo(sede.getActivo());
        return dto;
    }
}