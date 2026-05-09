package co.edu.ufps.legal_cases.business.service.catalogo;

import co.edu.ufps.legal_cases.business.dto.catalogo.AreaDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Area;
import co.edu.ufps.legal_cases.business.repository.catalogo.AreaRepository;
import co.edu.ufps.legal_cases.exception.BusinessException;

import org.springframework.stereotype.Service;
import static co.edu.ufps.legal_cases.util.NormalizacionUtils.normalizarTexto;

import java.util.List;

@Service
public class AreaService {

    private final AreaRepository areaRepository;

    public AreaService(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    public List<AreaDTO> listar() {
        // Obtiene todas las áreas y las convierte a DTO
        return areaRepository.findAll()
                .stream()
                .map(area -> convertirADTO(area))
                .toList();
    }

    public AreaDTO obtenerPorId(Long id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Área no encontrada con id: " + id));

        return convertirADTO(area);
    }

    public AreaDTO crear(AreaDTO areaDTO) {
        String nombre = normalizarTexto(areaDTO.getNombre());

        if (nombre == null || nombre.isBlank()) {
            throw new BusinessException("El nombre del área es obligatorio");
        }

        if (areaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessException("Ya existe un área con ese nombre");
        }

        Area area = new Area();
        area.setNombre(nombre);

        Area areaGuardada = areaRepository.save(area);
        return convertirADTO(areaGuardada);
    }

    public AreaDTO actualizar(Long id, AreaDTO areaDTO) {

        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Área no encontrada con id: " + id));

        String nuevoNombre = normalizarTexto(areaDTO.getNombre());

        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new BusinessException("El nombre del área es obligatorio");
        }

        //VALIDAR SI ES EL MISMO
        if (area.getNombre().equalsIgnoreCase(nuevoNombre)) {
            throw new BusinessException("El nombre es el mismo, no hay cambios");
        }

        //VALIDAR DUPLICADO
        if (areaRepository.existsByNombreIgnoreCase(nuevoNombre)) {
            throw new BusinessException("Ya existe un área con ese nombre");
        }

        area.setNombre(nuevoNombre);
        Area areaActualizada = areaRepository.save(area);

        return convertirADTO(areaActualizada);
    }

    public void eliminar(Long id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Área no encontrada con id: " + id));

        // Para evitar eliminar temas accidentalmente en cascada
        if (area.getTemas() != null && !area.getTemas().isEmpty()) {
            throw new BusinessException("No se puede eliminar el área porque tiene temas asociados");
        }

        areaRepository.delete(area);
    }

    private AreaDTO convertirADTO(Area area) {
        return new AreaDTO(area.getId(), area.getNombre());
    }
}