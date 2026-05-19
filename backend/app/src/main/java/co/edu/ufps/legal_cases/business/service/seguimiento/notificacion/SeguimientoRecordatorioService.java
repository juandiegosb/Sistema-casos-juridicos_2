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

// Este servicio envia una notificación y actualizar su estado.
@Service
@RequiredArgsConstructor
public class SeguimientoRecordatorioService {

    private final SeguimientoRepository seguimientoRepository;
    private final SeguimientoNotificacionRepository seguimientoNotificacionRepository;

    @Transactional
    public void sincronizar(DatosCorreoSeguimientoDTO datos) {
        // Si no hay fecha de entrega o dias de notificacion,
        // no se puede programar ningun recordatorio.
        if (!tieneDatosParaRecordatorio(datos)) {
            cancelarRecordatorio(datos, TipoNotificacionSeguimiento.AUTOR);
            cancelarRecordatorio(datos, TipoNotificacionSeguimiento.PARTES);
            cancelarRecordatorio(datos, TipoNotificacionSeguimiento.ESTUDIANTE);
            cancelarRecordatorio(datos, TipoNotificacionSeguimiento.ALERTA_DISCIPLINARIA);
            return;
        }

        // La fecha del recordatorio se calcula una sola vez.
        LocalDate fechaProgramada = datos.getFechaEntrega().minusDays(datos.getDiasNotificacion());

        // El autor siempre recibe recordatorio si hay fecha y dias de notificacion.
        sincronizarRecordatorio(datos, TipoNotificacionSeguimiento.AUTOR, true, fechaProgramada);

        // Los demas recordatorios dependen de las banderas del seguimiento.
        sincronizarRecordatorio(
                datos,
                TipoNotificacionSeguimiento.PARTES,
                Boolean.TRUE.equals(datos.getNotificarPartes()),
                fechaProgramada);

        sincronizarRecordatorio(
                datos,
                TipoNotificacionSeguimiento.ESTUDIANTE,
                Boolean.TRUE.equals(datos.getNotificarEstudiante()),
                fechaProgramada);

        sincronizarRecordatorio(
                datos,
                TipoNotificacionSeguimiento.ALERTA_DISCIPLINARIA,
                Boolean.TRUE.equals(datos.getAlertaDisciplinaria()),
                fechaProgramada);
    }

    private void sincronizarRecordatorio(
            DatosCorreoSeguimientoDTO datos,
            TipoNotificacionSeguimiento tipoNotificacion,
            boolean aplica,
            LocalDate fechaProgramada) {

        // Busca si ya existe un recordatorio de este tipo para el seguimiento.
        SeguimientoNotificacion notificacion = buscarRecordatorio(
                datos.getSeguimientoId(),
                tipoNotificacion);

        // Si el recordatorio ya no aplica, se cancela si esta pendiente.
        if (!aplica) {
            desactivarSiEstaPendiente(notificacion);
            return;
        }

        // Si aplica y no existe, se crea programado para la fecha calculada.
        if (notificacion == null) {
            crearRecordatorio(
                    datos.getSeguimientoId(),
                    tipoNotificacion,
                    fechaProgramada);
            return;
        }

        // Si ya fue enviado, no se modifica para respetar el historial del envio.
        if (Boolean.TRUE.equals(notificacion.getEnviada())) {
            return;
        }

        // Si existe y sigue pendiente, se actualiza la fecha y se reactiva si estaba cancelado.
        reactivarRecordatorio(notificacion, fechaProgramada);
    }

    private void cancelarRecordatorio(
            DatosCorreoSeguimientoDTO datos,
            TipoNotificacionSeguimiento tipoNotificacion) {

        SeguimientoNotificacion notificacion = buscarRecordatorio(
                datos.getSeguimientoId(),
                tipoNotificacion);

        desactivarSiEstaPendiente(notificacion);
    }

    private SeguimientoNotificacion crearRecordatorio(
            Long seguimientoId,
            TipoNotificacionSeguimiento tipoNotificacion,
            LocalDate fechaProgramada) {

        // Se usa la referencia por id para no cargar todo el seguimiento.
        Seguimiento seguimiento = seguimientoRepository.getReferenceById(seguimientoId);

        SeguimientoNotificacion notificacion = new SeguimientoNotificacion();
        notificacion.setSeguimiento(seguimiento);
        notificacion.setTipoNotificacion(tipoNotificacion);
        notificacion.setMomentoNotificacion(MomentoNotificacionSeguimiento.RECORDATORIO);
        notificacion.setFechaProgramada(fechaProgramada);

        // Estado inicial de un recordatorio nuevo.
        notificacion.setEnviada(false);
        notificacion.setActiva(true);
        notificacion.setIntentos(0);
        notificacion.setError(null);
        notificacion.setFechaCancelacion(null);

        return seguimientoNotificacionRepository.save(notificacion);
    }

    private void reactivarRecordatorio(
            SeguimientoNotificacion notificacion,
            LocalDate fechaProgramada) {

        // Se activa de nuevo porque el recordatorio vuelve a aplicar.
        notificacion.setActiva(true);

        // Se actualiza por si cambio fechaEntrega o diasNotificacion.
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

    private SeguimientoNotificacion buscarRecordatorio(
            Long seguimientoId,
            TipoNotificacionSeguimiento tipoNotificacion) {

        return seguimientoNotificacionRepository
                .findBySeguimiento_IdAndTipoNotificacionAndMomentoNotificacion(
                        seguimientoId,
                        tipoNotificacion,
                        MomentoNotificacionSeguimiento.RECORDATORIO)
                .orElse(null);
    }

    private boolean tieneDatosParaRecordatorio(DatosCorreoSeguimientoDTO datos) {
        return datos.getFechaEntrega() != null
                && datos.getDiasNotificacion() != null;
    }
}