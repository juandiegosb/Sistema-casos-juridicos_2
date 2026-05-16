package co.edu.ufps.legal_cases.business.service.seguimiento;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import co.edu.ufps.legal_cases.business.dto.seguimiento.SeguimientoDestinatarioDTO;
import co.edu.ufps.legal_cases.business.model.seguimiento.TipoNotificacionSeguimiento;
import co.edu.ufps.legal_cases.common.service.EmailService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeguimientoCorreoService {

    private final EmailService emailService;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.app-name}")
    private String nombreSistema;

    public void enviar(
            List<SeguimientoDestinatarioDTO> destinatarios,
            TipoNotificacionSeguimiento tipoNotificacion,
            String descripcion,
            String categoria,
            Long consultaId,
            LocalDate fechaEntrega) {

        if (destinatarios == null || destinatarios.isEmpty()) {
            return;
        }

        // Envia a cada destinatario de forma individual con correo personalizado
        for (SeguimientoDestinatarioDTO destinatario : destinatarios) {
            enviarADestinatario(
                    destinatario,
                    tipoNotificacion,
                    descripcion,
                    categoria,
                    consultaId,
                    fechaEntrega
            );
        }
    }

    private void enviarADestinatario(
            SeguimientoDestinatarioDTO destinatario,
            TipoNotificacionSeguimiento tipoNotificacion,
            String descripcion,
            String categoria,
            Long consultaId,
            LocalDate fechaEntrega) {

        String asunto = construirAsunto(tipoNotificacion);

        String html = construirHtml(
                destinatario.getNombre(),
                asunto,
                tipoNotificacion,
                descripcion,
                categoria,
                consultaId,
                fechaEntrega
        );

        // Usa el servicio de correo para enviar el mensaje
        emailService.enviarHtml(
                destinatario.getEmail(),
                destinatario.getNombre(),
                asunto,
                html,
                "seguimiento"
        );
    }

    private String construirHtml(
            String nombreDestinatario,
            String tituloCorreo,
            TipoNotificacionSeguimiento tipoNotificacion,
            String descripcion,
            String categoria,
            Long consultaId,
            LocalDate fechaEntrega) {

        // Para enviarle a la plantilla la informacion para las veriables dinamicas
        Context context = new Context();
        context.setVariable("nombreSistema", nombreSistema);
        context.setVariable("nombreDestinatario", nombreDestinatario);
        context.setVariable("tituloCorreo", tituloCorreo);
        context.setVariable("mensajePrincipal", construirMensajePrincipal(tipoNotificacion));
        context.setVariable("mensajeAdvertencia", construirMensajeAdvertencia(tipoNotificacion));
        context.setVariable("descripcion", descripcion);
        context.setVariable("categoria", categoria);
        context.setVariable("consultaId", consultaId);
        context.setVariable("fechaEntrega", fechaEntrega);

        // Envia la ruta donde esta la plantilla en resources/templates
        return templateEngine.process("emails/notificacion-seguimiento", context);
    }

    private String construirAsunto(TipoNotificacionSeguimiento tipoNotificacion) {
        return switch (tipoNotificacion) {
            case PARTES -> "Nuevo seguimiento de su consulta";
            case ESTUDIANTE -> "Nuevo seguimiento de la consulta";
            case ALERTA_DISCIPLINARIA -> "Alerta disciplinaria de seguimiento";
            case RECORDATORIO_AUTOR -> "Recordatorio de seguimiento pendiente";
        };
    }

    private String construirMensajePrincipal(TipoNotificacionSeguimiento tipoNotificacion) {
        return switch (tipoNotificacion) {
            case PARTES -> "Se ha registrado un seguimiento relacionado con su consulta.";
            case ESTUDIANTE -> "Se ha registrado un seguimiento que debes revisar en el sistema.";
            case ALERTA_DISCIPLINARIA -> "Se ha registrado un seguimiento marcado como alerta disciplinaria.";
            case RECORDATORIO_AUTOR -> "Este es un recordatorio de un seguimiento que tienes pendiente.";
        };
    }

    private String construirMensajeAdvertencia(TipoNotificacionSeguimiento tipoNotificacion) {
        if (TipoNotificacionSeguimiento.ALERTA_DISCIPLINARIA.equals(tipoNotificacion)) {
            return "Esta alerta requiere revisión por parte del equipo administrativo.";
        }

        return null;
    }
}