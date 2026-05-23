package co.edu.ufps.legal_cases.business.service.proceso.proceso;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.proceso.ProcesoDTO;
import co.edu.ufps.legal_cases.business.model.catalogo.Departamento;
import co.edu.ufps.legal_cases.business.model.consulta.Consulta;
import co.edu.ufps.legal_cases.business.model.proceso.Especialidad;
import co.edu.ufps.legal_cases.business.model.proceso.EstadoProceso;
import co.edu.ufps.legal_cases.business.model.proceso.OrganoControl;
import co.edu.ufps.legal_cases.business.model.proceso.Proceso;
import co.edu.ufps.legal_cases.business.repository.catalogo.DepartamentoRepository;
import co.edu.ufps.legal_cases.business.repository.consulta.ConsultaRepository;
import co.edu.ufps.legal_cases.business.repository.proceso.EspecialidadRepository;
import co.edu.ufps.legal_cases.business.repository.proceso.OrganoControlRepository;
import co.edu.ufps.legal_cases.business.repository.proceso.ProcesoRepository;
import co.edu.ufps.legal_cases.business.service.acceso.proceso.ProcesoAccessService;
import co.edu.ufps.legal_cases.business.service.consulta.consulta.ConsultaEstadoService;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProcesoCommandService {

    private final ProcesoRepository procesoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final ConsultaRepository consultaRepository;
    private final OrganoControlRepository organoControlRepository;
    private final EspecialidadRepository especialidadRepository;
    private final ProcesoAccessService procesoAccessService;
    private final ProcesoValidator procesoValidator;
    private final ProcesoMapper procesoMapper;
    private final ConsultaEstadoService consultaEstadoService;

    // Crea un proceso asociado a una consulta existente.
    // El alcance se valida con la consulta porque Proceso no tiene un alcance
    // independiente.
    @Transactional
    public ProcesoDTO crear(ProcesoDTO dto) {
        procesoValidator.validarCreacion(dto);

        String numeroRadicado = procesoValidator.normalizarNumeroRadicado(dto.getNumeroRadicado());

        procesoAccessService.validarPuedeCrearProceso(dto.getConsultaId());

        if (procesoRepository.existsByNumeroRadicado(numeroRadicado)) {
            throw new BusinessException("Ya existe un proceso con ese número de radicado");
        }

        DatosProceso datos = prepararDatos(dto, numeroRadicado);

        Proceso proceso = new Proceso();
        procesoMapper.aplicarDatos(proceso, datos);
        proceso.setEstado(EstadoProceso.PENDIENTE);
        proceso.setActivo(true);

        return procesoMapper.convertirADTO(procesoRepository.save(proceso));
    }

    // Actualiza datos del proceso sin permitir cambiar la consulta.
    // La consulta define el alcance, por eso mover un proceso a otra consulta sería
    // otro flujo de negocio.
    @Transactional
    public ProcesoDTO actualizar(Long id, ProcesoDTO dto) {
        procesoAccessService.validarPuedeActualizarProceso(id);
        procesoValidator.validarActualizacion(id, dto);

        Proceso proceso = buscarProcesoActivo(id);
        procesoValidator.validarNoCambieConsulta(proceso, dto);

        String numeroRadicado = procesoValidator.normalizarNumeroRadicado(dto.getNumeroRadicado());

        if (procesoRepository.existsByNumeroRadicadoAndIdNot(numeroRadicado, id)) {
            throw new BusinessException("Ya existe un proceso con ese número de radicado");
        }

        DatosProceso datos = prepararDatos(dto, numeroRadicado);

        // Actualizar datos del proceso no debe cambiar el estado activo.
        // La desactivación se maneja por eliminar().
        procesoValidator.validarExistenCambios(proceso, datos);

        procesoMapper.aplicarDatos(proceso, datos);

        return procesoMapper.convertirADTO(procesoRepository.save(proceso));
    }

    @Transactional
    public ProcesoDTO cambiarEstadoProceso(Long id, EstadoProceso estado) {
        procesoAccessService.validarPuedeCambiarEstadoProceso(id);

        Proceso proceso = buscarProcesoActivo(id);

        consultaEstadoService.validarPermiteOperacionOperativa(proceso.getConsulta());

        procesoValidator.validarCambioEstadoProceso(proceso, estado);

        proceso.setEstado(estado);

        return procesoMapper.convertirADTO(procesoRepository.save(proceso));
    }

    @Transactional
    public void eliminar(Long id) {
        procesoAccessService.validarPuedeDesactivarProceso(id);

        Proceso proceso = buscarProcesoActivo(id);

        consultaEstadoService.validarPermiteOperacionOperativa(proceso.getConsulta());

        // Se desactiva para conservar el historial del proceso dentro de la consulta.
        proceso.setActivo(false);

        procesoRepository.save(proceso);
    }

    @Transactional
    public ProcesoDTO cambiarEstado(Long id, Boolean activo) {
        procesoAccessService.validarPuedeCambiarEstadoProceso(id);

        Proceso proceso = buscarProcesoPorId(id);

        consultaEstadoService.validarPermiteOperacionOperativa(proceso.getConsulta());

        procesoValidator.validarCambioEstado(proceso, activo);

        proceso.setActivo(activo);

        return procesoMapper.convertirADTO(procesoRepository.save(proceso));
    }

    private DatosProceso prepararDatos(ProcesoDTO dto, String numeroRadicado) {
        Departamento departamento = obtenerDepartamento(dto.getDepartamentoId());
        Consulta consulta = obtenerConsulta(dto.getConsultaId());
        OrganoControl organoControl = obtenerOrganoControlOpcional(dto.getOrganoControlId());
        Especialidad especialidad = obtenerEspecialidadOpcional(dto.getEspecialidadId());

        procesoValidator.validarEspecialidadPerteneceAlOrgano(especialidad, organoControl);

        return new DatosProceso(
                numeroRadicado,
                departamento,
                consulta,
                organoControl,
                especialidad);
    }

    private Proceso buscarProcesoActivo(Long id) {
        if (id == null) {
            throw new BusinessException("El id del proceso es obligatorio");
        }

        return procesoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException("Proceso no encontrado con id: " + id));
    }

    private Proceso buscarProcesoPorId(Long id) {
        if (id == null) {
            throw new BusinessException("El id del proceso es obligatorio");
        }

        return procesoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Proceso no encontrado con id: " + id));
    }

    private Departamento obtenerDepartamento(Long departamentoId) {
        if (departamentoId == null) {
            throw new BusinessException("El departamento es obligatorio");
        }

        return departamentoRepository.findByIdAndActivoTrue(departamentoId)
                .orElseThrow(
                        () -> new BusinessException("Departamento no encontrado o inactivo con id: " + departamentoId));
    }

    private Consulta obtenerConsulta(Long consultaId) {
        if (consultaId == null) {
            throw new BusinessException("La consulta es obligatoria");
        }

        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new BusinessException("Consulta no encontrada con id: " + consultaId));

        consultaEstadoService.validarPermiteOperacionOperativa(consulta);

        return consulta;
    }

    private OrganoControl obtenerOrganoControlOpcional(Long organoControlId) {
        if (organoControlId == null) {
            return null;
        }

        return organoControlRepository.findByIdAndActivoTrue(organoControlId)
                .orElseThrow(() -> new BusinessException(
                        "Órgano de control no encontrado o inactivo con id: " + organoControlId));
    }

    private Especialidad obtenerEspecialidadOpcional(Long especialidadId) {
        if (especialidadId == null) {
            return null;
        }

        return especialidadRepository.findByIdAndActivoTrue(especialidadId)
                .orElseThrow(() -> new BusinessException(
                        "Especialidad no encontrada o inactiva con id: " + especialidadId));
    }
}