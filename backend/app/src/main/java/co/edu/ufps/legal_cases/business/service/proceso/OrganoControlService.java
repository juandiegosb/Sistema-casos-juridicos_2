package co.edu.ufps.legal_cases.business.service.proceso;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.proceso.OrganoControlDTO;
import co.edu.ufps.legal_cases.business.model.proceso.OrganoControl;
import co.edu.ufps.legal_cases.business.repository.proceso.EspecialidadRepository;
import co.edu.ufps.legal_cases.business.repository.proceso.OrganoControlRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;

@Service
public class OrganoControlService {

    private final OrganoControlRepository organoControlRepository;
    private final EspecialidadRepository especialidadRepository;

    public OrganoControlService(
            OrganoControlRepository organoControlRepository,
            EspecialidadRepository especialidadRepository) {
        this.organoControlRepository = organoControlRepository;
        this.especialidadRepository = especialidadRepository;
    }

    // Lista órganos activos para formularios y combos del frontend.
    @Transactional(readOnly = true)
    public List<OrganoControlDTO> listar() {
        return organoControlRepository.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // Lista todos para administración de catálogos.
    @Transactional(readOnly = true)
    public List<OrganoControlDTO> listarTodos() {
        return organoControlRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(OrganoControl::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrganoControlDTO obtenerPorId(Long id) {
        OrganoControl organoControl = buscarPorIdActivo(id);
        return convertirADTO(organoControl);
    }

    @Transactional
    public OrganoControlDTO crear(OrganoControlDTO dto) {
        validarCreacion(dto);

        String nombre = normalizarNombre(dto.getNombre());

        if (organoControlRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessException("Ya existe un órgano de control con ese nombre");
        }

        OrganoControl organoControl = new OrganoControl();
        organoControl.setNombre(nombre);
        organoControl.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        return convertirADTO(organoControlRepository.save(organoControl));
    }

    @Transactional
    public OrganoControlDTO actualizar(Long id, OrganoControlDTO dto) {
        OrganoControl organoControl = buscarPorId(id);

        validarActualizacion(id, dto);

        String nombreNuevo = normalizarNombre(dto.getNombre());
        Boolean activoNuevo = dto.getActivo() != null ? dto.getActivo() : organoControl.getActivo();

        if (organoControlRepository.existsByNombreIgnoreCaseAndIdNot(nombreNuevo, id)) {
            throw new BusinessException("Ya existe un órgano de control con ese nombre");
        }

        if (Boolean.FALSE.equals(activoNuevo)) {
            validarPuedeDesactivarse(id);
        }

        boolean sinCambios = Objects.equals(organoControl.getNombre(), nombreNuevo)
                && Objects.equals(organoControl.getActivo(), activoNuevo);

        if (sinCambios) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        organoControl.setNombre(nombreNuevo);
        organoControl.setActivo(activoNuevo);

        return convertirADTO(organoControlRepository.save(organoControl));
    }

    @Transactional
    public void eliminar(Long id) {
        OrganoControl organoControl = buscarPorIdActivo(id);

        validarPuedeDesactivarse(id);

        // Desactivación lógica: se conserva el registro para historial.
        organoControl.setActivo(false);

        organoControlRepository.save(organoControl);
    }

    private void validarCreacion(OrganoControlDTO dto) {
        validarDtoObligatorio(dto);

        if (dto.getId() != null) {
            throw new BusinessException("El id no debe enviarse en la creación");
        }
    }

    private void validarActualizacion(Long id, OrganoControlDTO dto) {
        validarDtoObligatorio(dto);

        if (dto.getId() != null && !Objects.equals(dto.getId(), id)) {
            throw new BusinessException("No se permite cambiar el id del órgano de control");
        }
    }

    private void validarDtoObligatorio(OrganoControlDTO dto) {
        if (dto == null) {
            throw new BusinessException("Los datos del órgano de control son obligatorios");
        }
    }

    private String normalizarNombre(String nombre) {
        String nombreNormalizado = normalizarTexto(nombre);

        if (nombreNormalizado == null || nombreNormalizado.isBlank()) {
            throw new BusinessException("El nombre del órgano de control es obligatorio");
        }

        if (nombreNormalizado.length() > 80) {
            throw new BusinessException("El nombre no puede superar los 80 caracteres");
        }

        return nombreNormalizado;
    }

    private void validarPuedeDesactivarse(Long id) {
        if (especialidadRepository.existsByOrganoControlIdAndActivoTrue(id)) {
            throw new BusinessException(
                    "No se puede desactivar el órgano de control porque tiene especialidades activas");
        }
    }

    private OrganoControl buscarPorId(Long id) {
        if (id == null) {
            throw new BusinessException("El id del órgano de control es obligatorio");
        }

        return organoControlRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Órgano de control no encontrado con id: " + id));
    }

    private OrganoControl buscarPorIdActivo(Long id) {
        if (id == null) {
            throw new BusinessException("El id del órgano de control es obligatorio");
        }

        return organoControlRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException("Órgano de control no encontrado o inactivo con id: " + id));
    }

    private OrganoControlDTO convertirADTO(OrganoControl organoControl) {
        return new OrganoControlDTO(
                organoControl.getId(),
                organoControl.getNombre(),
                organoControl.getActivo());
    }
}