package co.edu.ufps.legal_cases.business.service.catalogo;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.catalogo.TipoDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Tema;
import co.edu.ufps.legal_cases.business.model.catalogo.Tipo;
import co.edu.ufps.legal_cases.business.repository.catalogo.TemaRepository;
import co.edu.ufps.legal_cases.business.repository.catalogo.TipoRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;

@Service
public class TipoService {

    private final TipoRepository tipoRepository;
    private final TemaRepository temaRepository;

    public TipoService(TipoRepository tipoRepository, TemaRepository temaRepository) {
        this.tipoRepository = tipoRepository;
        this.temaRepository = temaRepository;
    }

    // Lista tipos activos para formularios, selects y uso normal del sistema.
    @Transactional(readOnly = true)
    public List<TipoDTO> listar() {
        return tipoRepository.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // Lista todos los tipos para administración del catálogo.
    @Transactional(readOnly = true)
    public List<TipoDTO> listarTodos() {
        return tipoRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // Lista tipos activos de un tema activo.
    @Transactional(readOnly = true)
    public List<TipoDTO> listarPorTema(Long temaId) {
        validarTemaActivo(temaId);

        return tipoRepository.findByTemaIdAndActivoTrueOrderByNombreAsc(temaId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // Lista todos los tipos de un tema para administración.
    @Transactional(readOnly = true)
    public List<TipoDTO> listarTodosPorTema(Long temaId) {
        validarTemaExistente(temaId);

        return tipoRepository.findByTemaIdOrderByNombreAsc(temaId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public TipoDTO obtenerPorId(Long id) {
        Tipo tipo = buscarPorIdActivo(id);
        return convertirADTO(tipo);
    }

    @Transactional
    public TipoDTO crear(TipoDTO tipoDTO) {
        validarCreacion(tipoDTO);

        String nombre = normalizarNombre(tipoDTO.getNombre());
        Tema tema = obtenerTemaActivo(tipoDTO.getTemaId());

        if (tipoRepository.existsByNombreIgnoreCaseAndTemaId(nombre, tema.getId())) {
            throw new BusinessException("Ya existe un tipo con ese nombre en el tema seleccionado");
        }

        Tipo tipo = new Tipo();
        tipo.setNombre(nombre);
        tipo.setTema(tema);
        tipo.setActivo(true);

        return convertirADTO(tipoRepository.save(tipo));
    }

    @Transactional
    public TipoDTO actualizar(Long id, TipoDTO tipoDTO) {
        Tipo tipo = buscarPorId(id);

        validarActualizacion(id, tipoDTO);

        String nuevoNombre = normalizarNombre(tipoDTO.getNombre());
        Tema nuevoTema = obtenerTemaActivo(tipoDTO.getTemaId());

        if (tipoRepository.existsByNombreIgnoreCaseAndTemaIdAndIdNot(
                nuevoNombre,
                nuevoTema.getId(),
                tipo.getId())) {
            throw new BusinessException("Ya existe un tipo con ese nombre en el tema seleccionado");
        }

        boolean mismoNombre = Objects.equals(tipo.getNombre(), nuevoNombre);
        boolean mismoTema = tipo.getTema() != null
                && Objects.equals(tipo.getTema().getId(), nuevoTema.getId());

        if (mismoNombre && mismoTema) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        tipo.setNombre(nuevoNombre);
        tipo.setTema(nuevoTema);

        return convertirADTO(tipoRepository.save(tipo));
    }

    @Transactional
    public TipoDTO cambiarEstado(Long id, Boolean activo) {
        Tipo tipo = buscarPorId(id);

        if (activo == null) {
            throw new BusinessException("El estado activo es obligatorio");
        }

        if (Objects.equals(tipo.getActivo(), activo)) {
            throw new BusinessException("El tipo ya tiene ese estado");
        }

        tipo.setActivo(activo);

        return convertirADTO(tipoRepository.save(tipo));
    }

    @Transactional
    public void eliminar(Long id) {
        Tipo tipo = buscarPorIdActivo(id);

        // Desactivación lógica: se conserva porque puede estar asociado a consultas.
        tipo.setActivo(false);

        tipoRepository.save(tipo);
    }

    private void validarCreacion(TipoDTO tipoDTO) {
        validarDtoObligatorio(tipoDTO);

        if (tipoDTO.getId() != null) {
            throw new BusinessException("El id no debe enviarse en la creación");
        }
    }

    private void validarActualizacion(Long id, TipoDTO tipoDTO) {
        validarDtoObligatorio(tipoDTO);

        if (tipoDTO.getId() != null && !Objects.equals(tipoDTO.getId(), id)) {
            throw new BusinessException("No se permite cambiar el id del tipo");
        }
    }

    private void validarDtoObligatorio(TipoDTO tipoDTO) {
        if (tipoDTO == null) {
            throw new BusinessException("Los datos del tipo son obligatorios");
        }

        if (tipoDTO.getTemaId() == null) {
            throw new BusinessException("El tema es obligatorio");
        }
    }

    private String normalizarNombre(String nombre) {
        String nombreNormalizado = normalizarTexto(nombre);

        if (nombreNormalizado == null || nombreNormalizado.isBlank()) {
            throw new BusinessException("El nombre del tipo es obligatorio");
        }

        if (nombreNormalizado.length() > 80) {
            throw new BusinessException("El nombre del tipo no puede superar los 80 caracteres");
        }

        return nombreNormalizado;
    }

    private void validarTemaActivo(Long temaId) {
        obtenerTemaActivo(temaId);
    }

    private void validarTemaExistente(Long temaId) {
        if (temaId == null) {
            throw new BusinessException("El tema es obligatorio");
        }

        temaRepository.findById(temaId)
                .orElseThrow(() -> new BusinessException("Tema no encontrado con id: " + temaId));
    }

    private Tema obtenerTemaActivo(Long temaId) {
        if (temaId == null) {
            throw new BusinessException("El tema es obligatorio");
        }

        return temaRepository.findByIdAndActivoTrue(temaId)
                .orElseThrow(() -> new BusinessException("Tema no encontrado o inactivo con id: " + temaId));
    }

    private Tipo buscarPorId(Long id) {
        if (id == null) {
            throw new BusinessException("El id del tipo es obligatorio");
        }

        return tipoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tipo no encontrado con id: " + id));
    }

    private Tipo buscarPorIdActivo(Long id) {
        if (id == null) {
            throw new BusinessException("El id del tipo es obligatorio");
        }

        return tipoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException("Tipo no encontrado o inactivo con id: " + id));
    }

    private TipoDTO convertirADTO(Tipo tipo) {
        return new TipoDTO(
                tipo.getId(),
                tipo.getNombre(),
                tipo.getTema().getId(),
                tipo.getActivo());
    }
}