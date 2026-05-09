package co.edu.ufps.legal_cases.business.service.catalogo;

import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.ufps.legal_cases.business.dto.catalogo.TipoDocumentoDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.TipoDocumento;
import co.edu.ufps.legal_cases.business.repository.catalogo.TipoDocumentoRepository;
import co.edu.ufps.legal_cases.exception.BusinessException;

import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarTexto;

@Service
public class TipoDocumentoService {

    private final TipoDocumentoRepository tipoDocumentoRepository;

    public TipoDocumentoService(TipoDocumentoRepository tipoDocumentoRepository) {
        this.tipoDocumentoRepository = tipoDocumentoRepository;
    }

    public List<TipoDocumentoDTO> listar() {
        return tipoDocumentoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<TipoDocumentoDTO> listarActivos() {
        return tipoDocumentoRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public TipoDocumentoDTO obtenerPorId(Long id) {
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tipo de documento no encontrado con id: " + id));

        return convertirADTO(tipoDocumento);
    }

    public TipoDocumentoDTO crear(TipoDocumentoDTO dto) {
        String displayName = normalizarTexto(dto.getDisplayName());

        if (displayName == null || displayName.isBlank()) {
            throw new BusinessException("El nombre visible es obligatorio");
        }

        if (tipoDocumentoRepository.existsByDisplayNameIgnoreCase(displayName)) {
            throw new BusinessException("Ya existe un tipo de documento con ese nombre");
        }

        TipoDocumento tipoDocumento = new TipoDocumento();
        tipoDocumento.setDisplayName(displayName);
        //Guarda por defecto como activo
        tipoDocumento.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        return convertirADTO(tipoDocumentoRepository.save(tipoDocumento));
    }

    public TipoDocumentoDTO actualizar(Long id, TipoDocumentoDTO dto) {
        TipoDocumento documentoExistente = tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tipo de documento no encontrado con id: " + id));

        String displayNameNuevo = normalizarTexto(dto.getDisplayName());

        if (displayNameNuevo == null || displayNameNuevo.isBlank()) {
            throw new BusinessException("El nombre visible es obligatorio");
        }

        // El id no debe cambiarse desde el DTO
        if (dto.getId() != null && !dto.getId().equals(documentoExistente.getId())) {
            throw new BusinessException("No se permite cambiar el id del tipo de documento");
        }

        boolean mismoNombre = documentoExistente.getDisplayName().equalsIgnoreCase(displayNameNuevo);
        boolean mismoEstado = dto.getActivo() == null || documentoExistente.getActivo().equals(dto.getActivo());

        if (mismoNombre && mismoEstado) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        if (tipoDocumentoRepository.existsByDisplayNameIgnoreCaseAndIdNot(displayNameNuevo, documentoExistente.getId())) {
            throw new BusinessException("Ya existe un tipo de documento con ese nombre");
        }

        documentoExistente.setDisplayName(displayNameNuevo);

        if (dto.getActivo() != null) {
            documentoExistente.setActivo(dto.getActivo());
        }

        return convertirADTO(tipoDocumentoRepository.save(documentoExistente));
    }

    public TipoDocumentoDTO cambiarEstado(Long id, Boolean activo) {
        TipoDocumento documentoExistente = tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tipo de documento no encontrado con id: " + id));

        if (activo == null) {
            throw new BusinessException("El estado activo es obligatorio");
        }

        if (documentoExistente.getActivo().equals(activo)) {
            throw new BusinessException("El tipo de documento ya tiene ese estado");
        }

        documentoExistente.setActivo(activo);
        return convertirADTO(tipoDocumentoRepository.save(documentoExistente));
    }

    private TipoDocumentoDTO convertirADTO(TipoDocumento tipoDocumento) {
        TipoDocumentoDTO dto = new TipoDocumentoDTO();
        dto.setId(tipoDocumento.getId());
        dto.setDisplayName(tipoDocumento.getDisplayName());
        dto.setActivo(tipoDocumento.getActivo());
        return dto;
    }
}