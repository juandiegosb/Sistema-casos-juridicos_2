package co.edu.ufps.legal_cases.business.service.proceso;

import static co.edu.ufps.legal_cases.common.util.NormalizacionUtils.normalizarTexto;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.proceso.ProcesoDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Departamento;
import co.edu.ufps.legal_cases.business.model.consulta.Consulta;
import co.edu.ufps.legal_cases.business.model.proceso.Especialidad;
import co.edu.ufps.legal_cases.business.model.proceso.OrganoControl;
import co.edu.ufps.legal_cases.business.model.proceso.Proceso;
import co.edu.ufps.legal_cases.business.repository.catalogo.DepartamentoRepository;
import co.edu.ufps.legal_cases.business.repository.consulta.ConsultaRepository;
import co.edu.ufps.legal_cases.business.repository.proceso.EspecialidadRepository;
import co.edu.ufps.legal_cases.business.repository.proceso.OrganoControlRepository;
import co.edu.ufps.legal_cases.business.repository.proceso.ProcesoRepository;
import co.edu.ufps.legal_cases.business.service.acceso.ProcesoAccessService;
import co.edu.ufps.legal_cases.common.exception.BusinessException;

@Service
public class ProcesoService {

    private final ProcesoRepository procesoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final ConsultaRepository consultaRepository;
    private final OrganoControlRepository organoControlRepository;
    private final EspecialidadRepository especialidadRepository;
    private final ProcesoAccessService procesoAccessService;

    public ProcesoService(
            ProcesoRepository procesoRepository,
            DepartamentoRepository departamentoRepository,
            ConsultaRepository consultaRepository,
            OrganoControlRepository organoControlRepository,
            EspecialidadRepository especialidadRepository,
            ProcesoAccessService procesoAccessService) {
        this.procesoRepository = procesoRepository;
        this.departamentoRepository = departamentoRepository;
        this.consultaRepository = consultaRepository;
        this.organoControlRepository = organoControlRepository;
        this.especialidadRepository = especialidadRepository;
        this.procesoAccessService = procesoAccessService;
    }

    @Transactional(readOnly = true)
    public List<ProcesoDTO> listar() {
        procesoAccessService.validarPuedeListarProcesos();

        return procesoRepository.findByActivoTrueOrderByIdDesc()
                .stream()
                .filter(procesoAccessService::puedeAccederAProceso)
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProcesoDTO obtenerPorId(Long id) {
        procesoAccessService.validarPuedeVerProceso(id);

        Proceso proceso = buscarProcesoActivo(id);

        return convertirADTO(proceso);
    }

    @Transactional
    public ProcesoDTO crear(ProcesoDTO dto) {
        validarCreacion(dto);

        String numeroRadicado = normalizarNumeroRadicado(dto.getNumeroRadicado());

        procesoAccessService.validarPuedeCrearProceso(dto.getConsultaId());

        if (procesoRepository.existsByNumeroRadicado(numeroRadicado)) {
            throw new BusinessException("Ya existe un proceso con ese número de radicado");
        }

        DatosProceso datos = prepararDatos(dto, numeroRadicado);

        Proceso proceso = new Proceso();
        aplicarDatos(proceso, datos);
        proceso.setActivo(true);

        return convertirADTO(procesoRepository.save(proceso));
    }

    @Transactional
    public ProcesoDTO actualizar(Long id, ProcesoDTO dto) {
        procesoAccessService.validarPuedeActualizarProceso(id);
        validarActualizacion(id, dto);

        Proceso proceso = buscarProcesoActivo(id);
        validarNoCambieConsulta(proceso, dto);

        String numeroRadicado = normalizarNumeroRadicado(dto.getNumeroRadicado());

        if (procesoRepository.existsByNumeroRadicadoAndIdNot(numeroRadicado, id)) {
            throw new BusinessException("Ya existe un proceso con ese número de radicado");
        }

        DatosProceso datos = prepararDatos(dto, numeroRadicado);
        Boolean activoNuevo = dto.getActivo() != null ? dto.getActivo() : proceso.getActivo();

        if (sinCambios(proceso, datos, activoNuevo)) {
            throw new BusinessException("No hay cambios para actualizar");
        }

        aplicarDatos(proceso, datos);
        proceso.setActivo(activoNuevo);

        return convertirADTO(procesoRepository.save(proceso));
    }

    @Transactional
    public void eliminar(Long id) {
        procesoAccessService.validarPuedeDesactivarProceso(id);

        Proceso proceso = buscarProcesoActivo(id);

        // Desactivación lógica: no se elimina físicamente para conservar historial.
        proceso.setActivo(false);

        procesoRepository.save(proceso);
    }

    private void validarCreacion(ProcesoDTO dto) {
        validarDtoObligatorio(dto);

        if (dto.getId() != null) {
            throw new BusinessException("El id no debe enviarse en la creación");
        }
    }

    private void validarActualizacion(Long id, ProcesoDTO dto) {
        validarDtoObligatorio(dto);

        if (dto.getId() != null && !Objects.equals(dto.getId(), id)) {
            throw new BusinessException("No se permite cambiar el id del proceso");
        }
    }

    private void validarDtoObligatorio(ProcesoDTO dto) {
        if (dto == null) {
            throw new BusinessException("Los datos del proceso son obligatorios");
        }
    }

    private String normalizarNumeroRadicado(String numeroRadicado) {
        String normalizado = normalizarTexto(numeroRadicado);

        if (normalizado == null || normalizado.isBlank()) {
            throw new BusinessException("El número de radicado es obligatorio");
        }

        if (normalizado.length() != 23) {
            throw new BusinessException("El número de radicado debe tener exactamente 23 caracteres");
        }

        return normalizado;
    }

    private void validarNoCambieConsulta(Proceso proceso, ProcesoDTO dto) {
        if (dto.getConsultaId() == null) {
            throw new BusinessException("La consulta es obligatoria");
        }

        Long consultaActualId = proceso.getConsulta() != null
                ? proceso.getConsulta().getId()
                : null;

        if (!Objects.equals(consultaActualId, dto.getConsultaId())) {
            throw new BusinessException("No se permite cambiar la consulta de un proceso existente");
        }
    }

    private DatosProceso prepararDatos(ProcesoDTO dto, String numeroRadicado) {
        Departamento departamento = obtenerDepartamento(dto.getDepartamentoId());
        Consulta consulta = obtenerConsulta(dto.getConsultaId());
        OrganoControl organoControl = obtenerOrganoControlOpcional(dto.getOrganoControlId());
        Especialidad especialidad = obtenerEspecialidadOpcional(dto.getEspecialidadId());

        validarEspecialidadPerteneceAlOrgano(especialidad, organoControl);

        return new DatosProceso(
                numeroRadicado,
                departamento,
                consulta,
                organoControl,
                especialidad);
    }

    private void aplicarDatos(Proceso proceso, DatosProceso datos) {
        proceso.setNumeroRadicado(datos.numeroRadicado());
        proceso.setDepartamento(datos.departamento());
        proceso.setConsulta(datos.consulta());
        proceso.setOrganoControl(datos.organoControl());
        proceso.setEspecialidad(datos.especialidad());
    }

    private boolean sinCambios(Proceso proceso, DatosProceso datos, Boolean activoNuevo) {
        return Objects.equals(proceso.getNumeroRadicado(), datos.numeroRadicado())
                && mismoId(proceso.getDepartamento(), datos.departamento())
                && mismoId(proceso.getConsulta(), datos.consulta())
                && mismoId(proceso.getOrganoControl(), datos.organoControl())
                && mismoId(proceso.getEspecialidad(), datos.especialidad())
                && Objects.equals(proceso.getActivo(), activoNuevo);
    }

    private boolean mismoId(Object actual, Object nuevo) {
        if (actual == null && nuevo == null) {
            return true;
        }

        if (actual == null || nuevo == null) {
            return false;
        }

        if (actual instanceof Departamento departamentoActual && nuevo instanceof Departamento departamentoNuevo) {
            return Objects.equals(departamentoActual.getId(), departamentoNuevo.getId());
        }

        if (actual instanceof Consulta consultaActual && nuevo instanceof Consulta consultaNueva) {
            return Objects.equals(consultaActual.getId(), consultaNueva.getId());
        }

        if (actual instanceof OrganoControl organoActual && nuevo instanceof OrganoControl organoNuevo) {
            return Objects.equals(organoActual.getId(), organoNuevo.getId());
        }

        if (actual instanceof Especialidad especialidadActual && nuevo instanceof Especialidad especialidadNueva) {
            return Objects.equals(especialidadActual.getId(), especialidadNueva.getId());
        }

        return false;
    }

    private Proceso buscarProcesoActivo(Long id) {
        if (id == null) {
            throw new BusinessException("El id del proceso es obligatorio");
        }

        return procesoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException("Proceso no encontrado con id: " + id));
    }

    private Departamento obtenerDepartamento(Long departamentoId) {
        if (departamentoId == null) {
            throw new BusinessException("El departamento es obligatorio");
        }

        return departamentoRepository.findById(departamentoId)
                .orElseThrow(() -> new BusinessException("Departamento no encontrado con id: " + departamentoId));
    }

    private Consulta obtenerConsulta(Long consultaId) {
        if (consultaId == null) {
            throw new BusinessException("La consulta es obligatoria");
        }

        return consultaRepository.findById(consultaId)
                .orElseThrow(() -> new BusinessException("Consulta no encontrada con id: " + consultaId));
    }

    private OrganoControl obtenerOrganoControlOpcional(Long organoControlId) {
        if (organoControlId == null) {
            return null;
        }

        OrganoControl organoControl = organoControlRepository.findById(organoControlId)
                .orElseThrow(() -> new BusinessException(
                        "Órgano de control no encontrado con id: " + organoControlId));

        if (!Boolean.TRUE.equals(organoControl.getActivo())) {
            throw new BusinessException("El órgano de control se encuentra inactivo");
        }

        return organoControl;
    }

    private Especialidad obtenerEspecialidadOpcional(Long especialidadId) {
        if (especialidadId == null) {
            return null;
        }

        Especialidad especialidad = especialidadRepository.findById(especialidadId)
                .orElseThrow(() -> new BusinessException(
                        "Especialidad no encontrada con id: " + especialidadId));

        if (!Boolean.TRUE.equals(especialidad.getActivo())) {
            throw new BusinessException("La especialidad se encuentra inactiva");
        }

        return especialidad;
    }

    private void validarEspecialidadPerteneceAlOrgano(
            Especialidad especialidad,
            OrganoControl organoControl) {

        if (especialidad == null) {
            return;
        }

        if (organoControl == null) {
            throw new BusinessException("Debe seleccionar un órgano de control para la especialidad");
        }

        if (especialidad.getOrganoControl() == null
                || !Objects.equals(especialidad.getOrganoControl().getId(), organoControl.getId())) {
            throw new BusinessException("La especialidad no pertenece al órgano de control seleccionado");
        }
    }

    private ProcesoDTO convertirADTO(Proceso proceso) {
        return new ProcesoDTO(
                proceso.getId(),
                proceso.getNumeroRadicado(),
                proceso.getDepartamento().getId(),
                proceso.getConsulta().getId(),
                proceso.getEspecialidad() != null
                        ? proceso.getEspecialidad().getId()
                        : null,
                proceso.getOrganoControl() != null
                        ? proceso.getOrganoControl().getId()
                        : null,
                proceso.getActivo()
        );
    }

    private record DatosProceso(
            String numeroRadicado,
            Departamento departamento,
            Consulta consulta,
            OrganoControl organoControl,
            Especialidad especialidad) {
    }
}