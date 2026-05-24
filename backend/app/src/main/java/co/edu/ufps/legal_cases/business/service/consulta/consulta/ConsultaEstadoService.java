package co.edu.ufps.legal_cases.business.service.consulta.consulta;

import org.springframework.stereotype.Service;

import co.edu.ufps.legal_cases.business.model.consulta.Consulta;
import co.edu.ufps.legal_cases.business.model.consulta.EstadoConsulta;
import co.edu.ufps.legal_cases.business.model.proceso.EstadoProceso;
import co.edu.ufps.legal_cases.business.model.seguimiento.EstadoSeguimiento;
import co.edu.ufps.legal_cases.business.model.seguimiento.respuesta.EstadoRespuestaSeguimiento;
import co.edu.ufps.legal_cases.business.repository.proceso.ProcesoRepository;
import co.edu.ufps.legal_cases.business.repository.seguimiento.SeguimientoRepository;
import co.edu.ufps.legal_cases.business.repository.seguimiento.notificacion.SeguimientoNotificacionRepository;
import co.edu.ufps.legal_cases.business.repository.seguimiento.respuesta.SeguimientoRespuestaRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;

@Service
public class ConsultaEstadoService {

    private final ProcesoRepository procesoRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final SeguimientoRespuestaRepository seguimientoRespuestaRepository;
    private final SeguimientoNotificacionRepository seguimientoNotificacionRepository;

    public ConsultaEstadoService(
            ProcesoRepository procesoRepository,
            SeguimientoRepository seguimientoRepository,
            SeguimientoRespuestaRepository seguimientoRespuestaRepository,
            SeguimientoNotificacionRepository seguimientoNotificacionRepository) {
        this.procesoRepository = procesoRepository;
        this.seguimientoRepository = seguimientoRepository;
        this.seguimientoRespuestaRepository = seguimientoRespuestaRepository;
        this.seguimientoNotificacionRepository = seguimientoNotificacionRepository;
    }

    public void validarCambioEstado(Consulta consulta, EstadoConsulta estadoNuevo) {
        validarConsultaObligatoria(consulta);

        if (estadoNuevo == null) {
            throw new BusinessException("El estado de la consulta es obligatorio");
        }

        if (EstadoConsulta.CERRADO.equals(consulta.getEstado())
                && !EstadoConsulta.ARCHIVADO.equals(estadoNuevo)) {
            throw new BusinessException("Una consulta cerrada solo puede archivarse");
        }

        if (EstadoConsulta.CERRADO.equals(estadoNuevo)) {
            validarPuedeCerrar(consulta.getId());
        }

        if (EstadoConsulta.ARCHIVADO.equals(estadoNuevo)) {
            validarPuedeArchivar(consulta);
        }
    }

    public void validarPuedeArchivar(Consulta consulta) {
        validarConsultaObligatoria(consulta);

        if (!EstadoConsulta.CERRADO.equals(consulta.getEstado())) {
            throw new BusinessException("Solo se pueden archivar consultas cerradas");
        }

        // Defensa adicional: una consulta cerrada no debería tener pendientes,
        // pero se valida otra vez para proteger el sistema ante datos históricos o
        // cambios manuales.
        validarNoTienePendientesOperativos(consulta.getId());
    }

    public void validarPuedeDesarchivar(Consulta consulta) {
        validarConsultaObligatoria(consulta);

        if (!EstadoConsulta.ARCHIVADO.equals(consulta.getEstado())) {
            throw new BusinessException("Solo se pueden desarchivar consultas archivadas");
        }

        // Defensa adicional: una consulta archivada debió venir de CERRADO.
        // Al desarchivar vuelve a CERRADO, por eso no debe tener pendientes operativos.
        validarNoTienePendientesOperativos(consulta.getId());
    }

    // Si una consulta esta cerrada o archivada no se puede hacer nada sobre ella
    public void validarPermiteOperacionOperativa(Consulta consulta) {
        validarConsultaObligatoria(consulta);

        if (EstadoConsulta.CERRADO.equals(consulta.getEstado())) {
            throw new BusinessException("No se pueden realizar operaciones sobre una consulta cerrada");
        }

        if (EstadoConsulta.ARCHIVADO.equals(consulta.getEstado())) {
            throw new BusinessException("No se pueden realizar operaciones sobre una consulta archivada");
        }
    }

    private void validarPuedeCerrar(Long consultaId) {
        validarNoTienePendientesOperativos(consultaId);
    }

    private void validarNoTienePendientesOperativos(Long consultaId) {
        if (consultaId == null) {
            throw new BusinessException("El id de la consulta es obligatorio");
        }

        if (procesoRepository.existsByConsulta_IdAndActivoTrueAndEstado(
                consultaId,
                EstadoProceso.PENDIENTE)) {
            throw new BusinessException("No se puede cerrar la consulta porque tiene procesos pendientes");
        }

        if (seguimientoRepository.existsByConsulta_IdAndActivoTrueAndEstado(
                consultaId,
                EstadoSeguimiento.PENDIENTE)) {
            throw new BusinessException("No se puede cerrar la consulta porque tiene seguimientos pendientes");
        }

        if (seguimientoRespuestaRepository
                .existsBySeguimiento_Consulta_IdAndSeguimiento_ActivoTrueAndActivoTrueAndEstado(
                        consultaId,
                        EstadoRespuestaSeguimiento.PENDIENTE)) {
            throw new BusinessException("No se puede cerrar la consulta porque tiene respuestas pendientes");
        }

        if (seguimientoNotificacionRepository
                .existsBySeguimiento_Consulta_IdAndSeguimiento_ActivoTrueAndActivoTrueAndEnviadaFalse(
                        consultaId)) {
            throw new BusinessException("No se puede cerrar la consulta porque tiene notificaciones pendientes");
        }
    }

    private void validarConsultaObligatoria(Consulta consulta) {
        if (consulta == null || consulta.getId() == null) {
            throw new BusinessException("La consulta es obligatoria");
        }
    }
}