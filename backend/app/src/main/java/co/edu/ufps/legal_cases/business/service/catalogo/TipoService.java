package co.edu.ufps.legal_cases.business.service.catalogo;

import co.edu.ufps.legal_cases.business.dto.catalogo.TipoDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Tema;
import co.edu.ufps.legal_cases.business.model.catalogo.Tipo;
import co.edu.ufps.legal_cases.business.repository.catalogo.TemaRepository;
import co.edu.ufps.legal_cases.business.repository.catalogo.TipoRepository;
import co.edu.ufps.legal_cases.exception.BusinessException;

import org.springframework.stereotype.Service;
import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarTexto;

import java.util.List;

@Service
public class TipoService {

    private final TipoRepository tipoRepository;
    private final TemaRepository temaRepository;

    public TipoService(TipoRepository tipoRepository, TemaRepository temaRepository) {
        this.tipoRepository = tipoRepository;
        this.temaRepository = temaRepository;
    }

    public List<TipoDTO> listar() {
        return tipoRepository.findAll()
                .stream()
                .map(tipo -> convertirADTO(tipo))
                .toList();
    }

    public List<TipoDTO> listarPorTema(Long temaId) {
        return tipoRepository.findByTemaId(temaId)
                .stream()
                .map(tipo -> convertirADTO(tipo))
                .toList();
    }

    public TipoDTO obtenerPorId(Long id) {
        Tipo tipo = tipoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tipo no encontrado con id: " + id));

        return convertirADTO(tipo);
    }

    public TipoDTO crear(TipoDTO tipoDTO) {
        String nombre = normalizarTexto(tipoDTO.getNombre());

        Tema tema = temaRepository.findById(tipoDTO.getTemaId())
                .orElseThrow(() -> new BusinessException("Tema no encontrado con id: " + tipoDTO.getTemaId()));

        if (tipoRepository.existsByNombreIgnoreCaseAndTemaId(nombre, tema.getId())) {
            throw new BusinessException("Ya existe un tipo con ese nombre en el tema seleccionado");
        }

        Tipo tipo = new Tipo();
        tipo.setNombre(nombre);
        tipo.setTema(tema);

        Tipo tipoGuardado = tipoRepository.save(tipo);
        return convertirADTO(tipoGuardado);
    }

    public TipoDTO actualizar(Long id, TipoDTO tipoDTO) {

        Tipo tipo = tipoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tipo no encontrado con id: " + id));

        if (tipoDTO.getNombre() == null || tipoDTO.getNombre().isBlank()) {
            throw new BusinessException("El nombre del tipo es obligatorio");
        }

        if (tipoDTO.getTemaId() == null) {
            throw new BusinessException("El tema es obligatorio");
        }

        String nuevoNombre = normalizarTexto(tipoDTO.getNombre());

        Tema tema = temaRepository.findById(tipoDTO.getTemaId())
                .orElseThrow(() -> new BusinessException("Tema no encontrado con id: " + tipoDTO.getTemaId()));

        //VALIDAR SI NO HAY CAMBIOS
        boolean mismoNombre = tipo.getNombre().equalsIgnoreCase(nuevoNombre);
        boolean mismoTema = tipo.getTema().getId().equals(tema.getId());

        if (mismoNombre && mismoTema) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        //VALIDAR DUPLICADO
        if (tipoRepository.existsByNombreIgnoreCaseAndTemaId(nuevoNombre, tema.getId())) {
            throw new BusinessException("Ya existe un tipo con ese nombre en el tema seleccionado");
        }

        tipo.setNombre(nuevoNombre);
        tipo.setTema(tema);

        Tipo tipoActualizado = tipoRepository.save(tipo);
        return convertirADTO(tipoActualizado);
    }

    public void eliminar(Long id) {
        Tipo tipo = tipoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tipo no encontrado con id: " + id));

        tipoRepository.delete(tipo);
    }

    private TipoDTO convertirADTO(Tipo tipo) {
        return new TipoDTO(
                tipo.getId(),
                tipo.getNombre(),
                tipo.getTema().getId());
    }
}