package co.edu.ufps.legal_cases.business.dto.seguimiento;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeguimientoRequestDTO {

    private Long id;

    @NotBlank(message = "La descripción del seguimiento es obligatoria")
    @Size(max = 200, message = "La descripción del seguimiento no puede superar 200 caracteres")
    private String descripcion;

    // Puede ser null.
    // Si es null, no se programa recordatorio por fecha de entrega.
    @FutureOrPresent(message = "La fecha de entrega no puede ser anterior a la fecha actual")
    private LocalDate fechaEntrega;

    // Puede ser null.
    // Si viene informado, debe ser mayor o igual a cero.
    @Min(value = 0, message = "Los días de notificación no pueden ser negativos")
    private Integer diasNotificacion;

    // Si es true, se notifica por correo a las partes involucradas de la consulta.
    // Ejemplo: asesor, monitor u otros actores permitidos según la regla de negocio.
    private Boolean notificarPartes;

    // Si es true, también se notifica al estudiante.
    // Si es false, el estudiante no recibe correo por este seguimiento.
    private Boolean notificarEstudiante;

    // Si es true, el seguimiento representa una alerta disciplinaria
    // asociada a un incumplimiento del estudiante.
    private Boolean alertaDisciplinaria;

    @NotNull(message = "La categoría del seguimiento es obligatoria")
    private Long categoriaSeguimientoId;

    @NotNull(message = "La consulta es obligatoria")
    private Long consultaId;

    // Regla condicional:
    // No se pueden definir días de notificación si no hay fecha de entrega.
    @AssertTrue(message = "No se pueden definir días de notificación sin fecha de entrega")
    public boolean isDiasNotificacionConsistente() {
        return diasNotificacion == null || fechaEntrega != null;
    }

    // Regla condicional:
    // No tiene sentido notificar al estudiante si no se notificará a las partes.
    // Si luego deciden permitir notificación exclusiva al estudiante, se puede quitar.
    @AssertTrue(message = "No se puede notificar al estudiante si no se notifican las partes")
    public boolean isNotificacionEstudianteConsistente() {
        return !Boolean.TRUE.equals(notificarEstudiante)
                || Boolean.TRUE.equals(notificarPartes);
    }
}