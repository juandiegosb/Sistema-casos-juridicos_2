package co.edu.ufps.legal_cases.business.service.seguimiento;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.seguimiento.CategoriaSeguimientoDTO;
import co.edu.ufps.legal_cases.business.model.seguimiento.CategoriaSeguimiento;
import co.edu.ufps.legal_cases.business.repository.seguimiento.CategoriaSeguimientoRepository;
import co.edu.ufps.legal_cases.business.repository.seguimiento.SeguimientoRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;

@Service
public class CategoriaSeguimientoService {

    private final CategoriaSeguimientoRepository categoriaSeguimientoRepository;
    private final SeguimientoRepository seguimientoRepository;

    public CategoriaSeguimientoService(
            CategoriaSeguimientoRepository categoriaSeguimientoRepository,
            SeguimientoRepository seguimientoRepository) {
        this.categoriaSeguimientoRepository = categoriaSeguimientoRepository;
        this.seguimientoRepository = seguimientoRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaSeguimientoDTO> listar() {
        return categoriaSeguimientoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoriaSeguimientoDTO> listarActivas() {
        return categoriaSeguimientoRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaSeguimientoDTO obtenerPorId(Long id) {
        CategoriaSeguimiento categoria = buscarPorId(id);
        return convertirADTO(categoria);
    }

    @Transactional
    public CategoriaSeguimientoDTO crear(CategoriaSeguimientoDTO dto) {
        if (dto == null) {
            throw new BusinessException("Los datos de la categoría son obligatorios");
        }

        if (dto.getId() != null) {
            throw new BusinessException("El id no debe enviarse en la creación");
        }

        String nombre = normalizarTexto(dto.getNombre());

        validarNombre(nombre);

        if (categoriaSeguimientoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessException("Ya existe una categoría de seguimiento con ese nombre");
        }

        CategoriaSeguimiento categoria = new CategoriaSeguimiento();
        categoria.setNombre(nombre);
        categoria.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        return convertirADTO(categoriaSeguimientoRepository.save(categoria));
    }

    @Transactional
    public CategoriaSeguimientoDTO actualizar(Long id, CategoriaSeguimientoDTO dto) {
        CategoriaSeguimiento categoria = buscarPorId(id);

        if (dto == null) {
            throw new BusinessException("Los datos de la categoría son obligatorios");
        }

        if (dto.getId() != null && !Objects.equals(dto.getId(), id)) {
            throw new BusinessException("No se permite cambiar el id de la categoría");
        }

        String nombre = normalizarTexto(dto.getNombre());
        Boolean activo = dto.getActivo() != null ? dto.getActivo() : categoria.getActivo();

        validarNombre(nombre);

        if (categoriaSeguimientoRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new BusinessException("Ya existe una categoría de seguimiento con ese nombre");
        }

        boolean sinCambios = Objects.equals(categoria.getNombre(), nombre)
                && Objects.equals(categoria.getActivo(), activo);

        if (sinCambios) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        categoria.setNombre(nombre);
        categoria.setActivo(activo);

        return convertirADTO(categoriaSeguimientoRepository.save(categoria));
    }

    @Transactional
    public CategoriaSeguimientoDTO cambiarEstado(Long id, Boolean activo) {
        CategoriaSeguimiento categoria = buscarPorId(id);

        if (activo == null) {
            throw new BusinessException("El estado activo es obligatorio");
        }

        if (Objects.equals(categoria.getActivo(), activo)) {
            throw new BusinessException("La categoría ya tiene ese estado");
        }

        categoria.setActivo(activo);

        return convertirADTO(categoriaSeguimientoRepository.save(categoria));
    }

    @Transactional
    public void eliminar(Long id) {
        CategoriaSeguimiento categoria = buscarPorId(id);

        if (seguimientoRepository.existsByCategoriaSeguimiento_Id(id)) {
            throw new BusinessException("No se puede eliminar la categoría porque tiene seguimientos asociados");
        }

        categoriaSeguimientoRepository.delete(categoria);
    }

    private CategoriaSeguimiento buscarPorId(Long id) {
        if (id == null) {
            throw new BusinessException("El id de la categoría es obligatorio");
        }

        return categoriaSeguimientoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Categoría de seguimiento no encontrada con id: " + id));
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new BusinessException("El nombre de la categoría es obligatorio");
        }

        if (nombre.length() > 50) {
            throw new BusinessException("El nombre de la categoría no puede superar 50 caracteres");
        }
    }

    private CategoriaSeguimientoDTO convertirADTO(CategoriaSeguimiento categoria) {
        CategoriaSeguimientoDTO dto = new CategoriaSeguimientoDTO();

        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());
        dto.setActivo(categoria.getActivo());

        return dto;
    }
}