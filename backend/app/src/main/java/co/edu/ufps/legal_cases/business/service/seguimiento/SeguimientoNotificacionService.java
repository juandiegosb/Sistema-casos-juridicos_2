package co.edu.ufps.legal_cases.business.service.seguimiento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.seguimiento.DatosCorreoSeguimientoDTO;
import co.edu.ufps.legal_cases.business.model.seguimiento.SeguimientoNotificacion;
import co.edu.ufps.legal_cases.business.repository.seguimiento.SeguimientoNotificacionRepository;
import co.edu.ufps.legal_cases.business.repository.seguimiento.SeguimientoRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;

// Este servicio coordina todo el proceso de notificaciones de un seguimiento.
@Service
@RequiredArgsConstructor
public class SeguimientoNotificacionService {

    private final SeguimientoRepository seguimientoRepository;
    private final SeguimientoNotificacionRepository seguimientoNotificacionRepository;
    private final SeguimientoNotificacionInmediataService seguimientoNotificacionInmediataService;
    private final SeguimientoRecordatorioService seguimientoRecordatorioService;
    private final SeguimientoEnvioNotificacionService seguimientoEnvioNotificacionService;

    // Cada que se crea o actualiza un seguimiento, se sincronizan sus notificaciones.
    @Transactional
    public void sincronizarNotificaciones(Long seguimientoId) {
        // Obtiene solo los datos necesarios para decidir que notificaciones aplicar.
        DatosCorreoSeguimientoDTO datos = obtenerDatosCorreo(seguimientoId);

        // Maneja los correos inmediatos.
        seguimientoNotificacionInmediataService.sincronizar(datos);

        // Maneja los recordatorios programados.
        seguimientoRecordatorioService.sincronizar(datos);
    }

    // Cancela las notificaciones pendientes de un seguimiento.
    // No las elimina para conservar historial.
    @Transactional
    public void cancelarNotificacionesPendientes(Long seguimientoId) {
        if (seguimientoId == null) {
            return;
        }

        // Busca solamente las que no han sido enviadas.
        List<SeguimientoNotificacion> pendientes = seguimientoNotificacionRepository
                .findBySeguimiento_IdAndEnviadaFalse(seguimientoId);

        // Las deja inactivas en vez de borrarlas.
        pendientes.forEach(this::desactivarSiEstaPendiente);
    }

    // Este metodo lo llama el scheduler para enviar las notificaciones pendientes.
    @Transactional
    public void procesarPendientes(LocalDate fecha) {
        // Si no llega fecha, se procesa hasta la fecha actual.
        LocalDate fechaProceso = fecha != null ? fecha : LocalDate.now();

        // Busca notificaciones activas, no enviadas y con fecha programada vencida.
        List<SeguimientoNotificacion> notificaciones = seguimientoNotificacionRepository
                .findByFechaProgramadaLessThanEqualAndEnviadaFalseAndActivaTrue(fechaProceso);

        // Intenta enviar cada notificacion pendiente.
        notificaciones.forEach(seguimientoEnvioNotificacionService::enviarNotificacionPendiente);
    }

    private void desactivarSiEstaPendiente(SeguimientoNotificacion notificacion) {
        if (notificacion == null) {
            return;
        }

        // Si ya fue enviada, no se modifica porque ya hace parte del historial real.
        if (Boolean.TRUE.equals(notificacion.getEnviada())) {
            return;
        }

        if (!Boolean.TRUE.equals(notificacion.getActiva())) {
            return;
        }

        // Se desactiva en vez de eliminar para conservar trazabilidad.
        notificacion.setActiva(false);
        notificacion.setFechaCancelacion(LocalDateTime.now());

        seguimientoNotificacionRepository.save(notificacion);
    }

    // Obtiene los datos necesarios para manejar las notificaciones de seguimiento.
    private DatosCorreoSeguimientoDTO obtenerDatosCorreo(Long seguimientoId) {
        if (seguimientoId == null) {
            throw new BusinessException("El id del seguimiento es obligatorio para notificar");
        }

        // Consulta un DTO con la informacion minima, no toda la entidad.
        return seguimientoRepository.findDatosCorreoById(seguimientoId)
                .orElseThrow(() -> new BusinessException("Seguimiento no encontrado con id: " + seguimientoId));
    }
}