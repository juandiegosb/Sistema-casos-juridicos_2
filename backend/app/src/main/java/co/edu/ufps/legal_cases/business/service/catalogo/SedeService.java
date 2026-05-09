package co.edu.ufps.legal_cases.business.service.catalogo;

import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.ufps.legal_cases.business.dto.catalogo.SedeDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Sede;
import co.edu.ufps.legal_cases.business.repository.catalogo.SedeRepository;
import co.edu.ufps.legal_cases.exception.BusinessException;

import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarTexto;

@Service
public class SedeService {

    private final SedeRepository sedeRepository;

    public SedeService(SedeRepository sedeRepository) {
        this.sedeRepository = sedeRepository;
    }

    public List<SedeDTO> listar() {
        return sedeRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public SedeDTO obtenerPorId(Long id) {
        Sede sede = sedeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Sede no encontrada con id: " + id));

        return convertirADTO(sede);
    }

    public SedeDTO crear(SedeDTO dto) {
        String nombre = normalizarTexto(dto.getNombre());

        if (nombre == null || nombre.isBlank()) {
            throw new BusinessException("El nombre de la sede es obligatorio");
        }

        if (sedeRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessException("Ya existe una sede con ese nombre");
        }

        Sede sede = new Sede();
        sede.setNombre(nombre);

        return convertirADTO(sedeRepository.save(sede));
    }

    public SedeDTO actualizar(Long id, SedeDTO dto) {
        Sede sedeExistente = sedeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Sede no encontrada con id: " + id));

        String nombreNuevo = normalizarTexto(dto.getNombre());

        if (nombreNuevo == null || nombreNuevo.isBlank()) {
            throw new BusinessException("El nombre de la sede es obligatorio");
        }

        // El id no debe cambiarse desde el DTO
        if (dto.getId() != null && !dto.getId().equals(sedeExistente.getId())) {
            throw new BusinessException("No se permite cambiar el id de la sede");
        }

        boolean mismoNombre = sedeExistente.getNombre().equalsIgnoreCase(nombreNuevo);

        if (mismoNombre) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        if (sedeRepository.existsByNombreIgnoreCaseAndIdNot(nombreNuevo, sedeExistente.getId())) {
            throw new BusinessException("Ya existe una sede con ese nombre");
        }

        sedeExistente.setNombre(nombreNuevo);

        return convertirADTO(sedeRepository.save(sedeExistente));
    }

    public void eliminar(Long id) {
        Sede sede = sedeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Sede no encontrada con id: " + id));

        // A futuro aquí hay validar si la sede está siendo usada por:
        // asesor, estudiante, monitor, administrativo, consulta, conciliador, etc.
        sedeRepository.delete(sede);
    }

    private SedeDTO convertirADTO(Sede sede) {
        SedeDTO dto = new SedeDTO();
        dto.setId(sede.getId());
        dto.setNombre(sede.getNombre());
        return dto;
    }
}