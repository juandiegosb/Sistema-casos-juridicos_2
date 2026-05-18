package co.edu.ufps.legal_cases.business.service.catalogo;

import co.edu.ufps.legal_cases.business.dto.catalogo.DepartamentoDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Departamento;
import co.edu.ufps.legal_cases.business.repository.catalogo.DepartamentoRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;

@Service
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;

    public DepartamentoService(DepartamentoRepository departamentoRepository) {
        this.departamentoRepository = departamentoRepository;
    }

    public List<DepartamentoDTO> listar() {
        return departamentoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public DepartamentoDTO obtenerPorId(Long id) {
        Departamento departamento = departamentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Departamento no encontrado con id: " + id));

        return convertirADTO(departamento);
    }

    public DepartamentoDTO crear(DepartamentoDTO dto) {

        String nombre = normalizarTexto(dto.getNombre());

        if (departamentoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessException("Ya existe un departamento con ese nombre");
        }

        Departamento departamento = new Departamento();
        departamento.setNombre(nombre);
        departamento.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        Departamento guardado = departamentoRepository.save(departamento);

        return convertirADTO(guardado);
    }

    public DepartamentoDTO actualizar(Long id, DepartamentoDTO dto) {

        Departamento departamento = departamentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Departamento no encontrado con id: " + id));

        String nuevoNombre = normalizarTexto(dto.getNombre());

        boolean mismoNombre = departamento.getNombre().equalsIgnoreCase(nuevoNombre);
        boolean mismoActivo = departamento.getActivo().equals(dto.getActivo());

        if (mismoNombre && mismoActivo) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        if (!departamento.getNombre().equalsIgnoreCase(nuevoNombre)
                && departamentoRepository.existsByNombreIgnoreCase(nuevoNombre)) {

            throw new BusinessException("Ya existe un departamento con ese nombre");
        }

        departamento.setNombre(nuevoNombre);
        departamento.setActivo(dto.getActivo());

        Departamento actualizado = departamentoRepository.save(departamento);

        return convertirADTO(actualizado);
    }

    public void eliminar(Long id) {

        Departamento departamento = departamentoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Departamento no encontrado con id: " + id));

        departamentoRepository.delete(departamento);
    }

    private DepartamentoDTO convertirADTO(Departamento departamento) {
        return new DepartamentoDTO(
                departamento.getId(),
                departamento.getNombre(),
                departamento.getActivo()
        );
    }
}