package co.edu.ufps.legal_cases.business.service.seguimiento.notificacion;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.seguimiento.notificacion.DatosCorreoSeguimientoDTO;
import co.edu.ufps.legal_cases.business.model.seguimiento.Seguimiento;
import co.edu.ufps.legal_cases.business.model.seguimiento.notificacion.MomentoNotificacionSeguimiento;
import co.edu.ufps.legal_cases.business.model.seguimiento.notificacion.SeguimientoNotificacion;
import co.edu.ufps.legal_cases.business.model.seguimiento.notificacion.TipoNotificacionSeguimiento;
import co.edu.ufps.legal_cases.business.repository.seguimiento.SeguimientoRepository;
import co.edu.ufps.legal_cases.business.repository.seguimiento.notificacion.SeguimientoNotificacionRepository;
import lombok.RequiredArgsConstructor;

// Crear, reactivar, cancelar y enviar notificaciones inmediatas.
@Service
@RequiredArgsConstructor
public class SeguimientoNotificacionInmediataService {

    private final SeguimientoRepository seguimientoRepository;
    private final SeguimientoNotificacionRepository seguimientoNotificacionRepository;
    private final SeguimientoEnvioNotificacionService seguimientoEnvioNotificacionService;

    @Transactional
    public void sincronizar(DatosCorreoSeguimientoDTO datos) {
        // Las notificaciones inmediatas dependen de las banderas del seguimiento.
        sincronizarNotificacionInmediata(
                datos,
                TipoNotificacionSeguimiento.PARTES,
                Boolean.TRUE.equals(datos.getNotificarPartes()));

        sincronizarNotificacionInmediata(
                datos,
                TipoNotificacionSeguimiento.ESTUDIANTE,
                Boolean.TRUE.equals(datos.getNotificarEstudiante()));

        sincronizarNotificacionInmediata(
                datos,
                TipoNotificacionSeguimiento.ALERTA_DISCIPLINARIA,
                Boolean.TRUE.equals(datos.getAlertaDisciplinaria()));
    }

    private void sincronizarNotificacionInmediata(
            DatosCorreoSeguimientoDTO datos,
            TipoNotificacionSeguimiento tipoNotificacion,
            boolean aplica) {

        // Busca si ya existe una notificacion inmediata de este tipo para el seguimiento.
        // Esto evita duplicados cuando el seguimiento se actualiza varias veces.
        SeguimientoNotificacion notificacion = buscarNotificacion(
                datos.getSeguimientoId(),
                tipoNotificacion);

        // Si esta notificacion ya no aplica, se cancela si esta pendiente.
        if (!aplica) {
            desactivarSiEstaPendiente(notificacion);
            return;
        }

        // Si aplica y no existe, se crea para enviarla inmediatamente.
        if (notificacion == null) {
            notificacion = crearNotificacion(
                    datos.getSeguimientoId(),
                    tipoNotificacion,
                    LocalDate.now());

        // Si ya existia pero no se ha enviado, se reactiva por si estaba cancelada.
        } else if (!Boolean.TRUE.equals(notificacion.getEnviada())) {
            reactivarNotificacion(notificacion, LocalDate.now());
        }

        // Si sigue pendiente y activa, se envia.
        // Si ya fue enviada antes, no se reenvia para evitar correos duplicados.
        if (!Boolean.TRUE.equals(notificacion.getEnviada())
                && Boolean.TRUE.equals(notificacion.getActiva())) {
            seguimientoEnvioNotificacionService.enviarNotificacionPendiente(notificacion);
        }
    }

    private SeguimientoNotificacion crearNotificacion(
            Long seguimientoId,
            TipoNotificacionSeguimiento tipoNotificacion,
            LocalDate fechaProgramada) {

        // Se usa la referencia por id para no cargar todo el seguimiento.
        Seguimiento seguimiento = seguimientoRepository.getReferenceById(seguimientoId);

        SeguimientoNotificacion notificacion = new SeguimientoNotificacion();
        notificacion.setSeguimiento(seguimiento);
        notificacion.setTipoNotificacion(tipoNotificacion);
        notificacion.setMomentoNotificacion(MomentoNotificacionSeguimiento.INMEDIATA);
        notificacion.setFechaProgramada(fechaProgramada);

        // Estado inicial de una notificacion nueva.
        notificacion.setEnviada(false);
        notificacion.setActiva(true);
        notificacion.setIntentos(0);
        notificacion.setError(null);
        notificacion.setFechaCancelacion(null);

        return seguimientoNotificacionRepository.save(notificacion);
    }

    private void reactivarNotificacion(
            SeguimientoNotificacion notificacion,
            LocalDate fechaProgramada) {

        // Se activa de nuevo porque la bandera del seguimiento volvio a aplicar.
        notificacion.setActiva(true);

        // Para inmediatas la fecha programada es hoy.
        notificacion.setFechaProgramada(fechaProgramada);

        // Se limpian datos de cancelacion y errores anteriores.
        notificacion.setFechaCancelacion(null);
        notificacion.setError(null);

        seguimientoNotificacionRepository.save(notificacion);
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

    private SeguimientoNotificacion buscarNotificacion(
            Long seguimientoId,
            TipoNotificacionSeguimiento tipoNotificacion) {

        return seguimientoNotificacionRepository
                .findBySeguimiento_IdAndTipoNotificacionAndMomentoNotificacion(
                        seguimientoId,
                        tipoNotificacion,
                        MomentoNotificacionSeguimiento.INMEDIATA)
                .orElse(null);
    }
}