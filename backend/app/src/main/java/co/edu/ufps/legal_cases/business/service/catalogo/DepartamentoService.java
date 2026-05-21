package co.edu.ufps.legal_cases.business.service.catalogo;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.catalogo.DepartamentoDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Departamento;
import co.edu.ufps.legal_cases.business.repository.catalogo.DepartamentoRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;

@Service
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;

    public DepartamentoService(DepartamentoRepository departamentoRepository) {
        this.departamentoRepository = departamentoRepository;
    }

    // Lista departamentos activos para formularios y selects del frontend.
    @Transactional(readOnly = true)
    public List<DepartamentoDTO> listar() {
        return departamentoRepository.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // Lista todos los departamentos para administración del catálogo.
    @Transactional(readOnly = true)
    public List<DepartamentoDTO> listarTodos() {
        return departamentoRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartamentoDTO obtenerPorId(Long id) {
        Departamento departamento = buscarPorIdActivo(id);
        return convertirADTO(departamento);
    }

    @Transactional
    public DepartamentoDTO crear(DepartamentoDTO dto) {
        validarCreacion(dto);

        String nombre = normalizarNombre(dto.getNombre());

        if (departamentoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessException("Ya existe un departamento con ese nombre");
        }

        Departamento departamento = new Departamento();
        departamento.setNombre(nombre);
        departamento.setActivo(true);

        return convertirADTO(departamentoRepository.save(departamento));
    }

    @Transactional
    public DepartamentoDTO actualizar(Long id, DepartamentoDTO dto) {
        Departamento departamentoExistente = buscarPorId(id);

        validarActualizacion(id, dto);

        String nombreNuevo = normalizarNombre(dto.getNombre());

        if (departamentoRepository.existsByNombreIgnoreCaseAndIdNot(nombreNuevo, departamentoExistente.getId())) {
            throw new BusinessException("Ya existe un departamento con ese nombre");
        }

        boolean sinCambios = Objects.equals(departamentoExistente.getNombre(), nombreNuevo);

        if (sinCambios) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        departamentoExistente.setNombre(nombreNuevo);

        return convertirADTO(departamentoRepository.save(departamentoExistente));
    }

    @Transactional
    public void eliminar(Long id) {
        Departamento departamento = buscarPorIdActivo(id);

        // Desactivación lógica: se conserva porque puede estar asociado a procesos u otros registros.
        departamento.setActivo(false);

        departamentoRepository.save(departamento);
    }

    @Transactional
    public DepartamentoDTO cambiarEstado(Long id, Boolean activo) {
        Departamento departamento = buscarPorId(id);

        if (activo == null) {
            throw new BusinessException("El estado activo es obligatorio");
        }

        if (Objects.equals(departamento.getActivo(), activo)) {
            throw new BusinessException("El departamento ya tiene ese estado");
        }

        departamento.setActivo(activo);

        return convertirADTO(departamentoRepository.save(departamento));
    }

    private void validarCreacion(DepartamentoDTO dto) {
        validarDtoObligatorio(dto);

        if (dto.getId() != null) {
            throw new BusinessException("El id no debe enviarse en la creación");
        }
    }

    private void validarActualizacion(Long id, DepartamentoDTO dto) {
        validarDtoObligatorio(dto);

        if (dto.getId() != null && !Objects.equals(dto.getId(), id)) {
            throw new BusinessException("No se permite cambiar el id del departamento");
        }
    }

    private void validarDtoObligatorio(DepartamentoDTO dto) {
        if (dto == null) {
            throw new BusinessException("Los datos del departamento son obligatorios");
        }
    }

    private String normalizarNombre(String nombre) {
        String nombreNormalizado = normalizarTexto(nombre);

        if (nombreNormalizado == null || nombreNormalizado.isBlank()) {
            throw new BusinessException("El nombre del departamento es obligatorio");
        }

        if (nombreNormalizado.length() > 80) {
            throw new BusinessException("El nombre no puede superar los 80 caracteres");
        }

        return nombreNormalizado;
    }

    private Departamento buscarPorId(Long id) {
        if (id == null) {
            throw new BusinessException("El id del departamento es obligatorio");
        }

        return departamentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Departamento no encontrado con id: " + id));
    }

    private Departamento buscarPorIdActivo(Long id) {
        if (id == null) {
            throw new BusinessException("El id del departamento es obligatorio");
        }

        return departamentoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException("Departamento no encontrado o inactivo con id: " + id));
    }

    private DepartamentoDTO convertirADTO(Departamento departamento) {
        return new DepartamentoDTO(
                departamento.getId(),
                departamento.getNombre(),
                departamento.getActivo());
    }
}