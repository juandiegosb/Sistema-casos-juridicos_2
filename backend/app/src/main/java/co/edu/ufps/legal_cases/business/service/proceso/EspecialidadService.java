package co.edu.ufps.legal_cases.business.service.proceso;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.proceso.EspecialidadDTO;
import co.edu.ufps.legal_cases.business.model.proceso.Especialidad;
import co.edu.ufps.legal_cases.business.model.proceso.OrganoControl;
import co.edu.ufps.legal_cases.business.repository.proceso.EspecialidadRepository;
import co.edu.ufps.legal_cases.business.repository.proceso.OrganoControlRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;

@Service
public class EspecialidadService {

    private final OrganoControlRepository organoControlRepository;
    private final EspecialidadRepository especialidadRepository;

    public EspecialidadService(
            EspecialidadRepository especialidadRepository,
            OrganoControlRepository organoControlRepository) {
        this.especialidadRepository = especialidadRepository;
        this.organoControlRepository = organoControlRepository;
    }

    // Lista especialidades activas para formularios y combos del frontend.
    @Transactional(readOnly = true)
    public List<EspecialidadDTO> listar() {
        return especialidadRepository.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // Lista todas para administración de catálogos.
    @Transactional(readOnly = true)
    public List<EspecialidadDTO> listarTodos() {
        return especialidadRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Especialidad::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(this::convertirADTO)
                .toList();
    }

    // Lista especialidades activas de un órgano de control específico.
    @Transactional(readOnly = true)
    public List<EspecialidadDTO> listarPorOrganoControl(Long organoControlId) {
        obtenerOrganoControlActivo(organoControlId);

        return especialidadRepository.findByOrganoControlIdAndActivoTrueOrderByNombreAsc(organoControlId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public EspecialidadDTO obtenerPorId(Long id) {
        Especialidad especialidad = buscarPorIdActivo(id);
        return convertirADTO(especialidad);
    }

    @Transactional
    public EspecialidadDTO crear(EspecialidadDTO dto) {
        validarCreacion(dto);

        String nombre = normalizarNombre(dto.getNombre());
        OrganoControl organoControl = obtenerOrganoControlActivo(dto.getOrganoControlId());

        if (especialidadRepository.existsByNombreIgnoreCaseAndOrganoControlId(nombre, organoControl.getId())) {
            throw new BusinessException(
                    "Ya existe una especialidad con ese nombre para el órgano de control seleccionado");
        }

        Especialidad especialidad = new Especialidad();
        especialidad.setOrganoControl(organoControl);
        especialidad.setNombre(nombre);
        especialidad.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        return convertirADTO(especialidadRepository.save(especialidad));
    }

    @Transactional
    public EspecialidadDTO actualizar(Long id, EspecialidadDTO dto) {
        Especialidad especialidad = buscarPorId(id);

        validarActualizacion(id, dto);

        String nombreNuevo = normalizarNombre(dto.getNombre());
        OrganoControl organoControl = obtenerOrganoControlActivo(dto.getOrganoControlId());
        Boolean activoNuevo = dto.getActivo() != null ? dto.getActivo() : especialidad.getActivo();

        if (especialidadRepository.existsByNombreIgnoreCaseAndOrganoControlIdAndIdNot(
                nombreNuevo,
                organoControl.getId(),
                id)) {
            throw new BusinessException(
                    "Ya existe una especialidad con ese nombre para el órgano de control seleccionado");
        }

        boolean sinCambios = Objects.equals(especialidad.getNombre(), nombreNuevo)
                && Objects.equals(especialidad.getActivo(), activoNuevo)
                && especialidad.getOrganoControl() != null
                && Objects.equals(especialidad.getOrganoControl().getId(), organoControl.getId());

        if (sinCambios) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        especialidad.setNombre(nombreNuevo);
        especialidad.setOrganoControl(organoControl);
        especialidad.setActivo(activoNuevo);

        return convertirADTO(especialidadRepository.save(especialidad));
    }

    @Transactional
    public void eliminar(Long id) {
        Especialidad especialidad = buscarPorIdActivo(id);

        // Desactivación lógica: se conserva para historial y procesos existentes.
        especialidad.setActivo(false);

        especialidadRepository.save(especialidad);
    }

    private void validarCreacion(EspecialidadDTO dto) {
        validarDtoObligatorio(dto);

        if (dto.getId() != null) {
            throw new BusinessException("El id no debe enviarse en la creación");
        }
    }

    private void validarActualizacion(Long id, EspecialidadDTO dto) {
        validarDtoObligatorio(dto);

        if (dto.getId() != null && !Objects.equals(dto.getId(), id)) {
            throw new BusinessException("No se permite cambiar el id de la especialidad");
        }
    }

    private void validarDtoObligatorio(EspecialidadDTO dto) {
        if (dto == null) {
            throw new BusinessException("Los datos de la especialidad son obligatorios");
        }
    }

    private String normalizarNombre(String nombre) {
        String nombreNormalizado = normalizarTexto(nombre);

        if (nombreNormalizado == null || nombreNormalizado.isBlank()) {
            throw new BusinessException("El nombre de la especialidad es obligatorio");
        }

        if (nombreNormalizado.length() > 80) {
            throw new BusinessException("El nombre no puede superar los 80 caracteres");
        }

        return nombreNormalizado;
    }

    private OrganoControl obtenerOrganoControlActivo(Long organoControlId) {
        if (organoControlId == null) {
            throw new BusinessException("El órgano de control es obligatorio");
        }

        return organoControlRepository.findByIdAndActivoTrue(organoControlId)
                .orElseThrow(() -> new BusinessException(
                        "Órgano de control no encontrado o inactivo con id: " + organoControlId));
    }

    private Especialidad buscarPorId(Long id) {
        if (id == null) {
            throw new BusinessException("El id de la especialidad es obligatorio");
        }

        return especialidadRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Especialidad no encontrada con id: " + id));
    }

    private Especialidad buscarPorIdActivo(Long id) {
        if (id == null) {
            throw new BusinessException("El id de la especialidad es obligatorio");
        }

        return especialidadRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException("Especialidad no encontrada o inactiva con id: " + id));
    }

    private EspecialidadDTO convertirADTO(Especialidad especialidad) {
        return new EspecialidadDTO(
                especialidad.getId(),
                especialidad.getNombre(),
                especialidad.getOrganoControl().getId(),
                especialidad.getActivo());
    }
}