package co.edu.ufps.legal_cases.business.model.seguimiento;

import java.time.LocalDate;
import java.time.LocalDateTime;

import co.edu.ufps.legal_cases.business.model.consulta.Consulta;
import co.edu.ufps.legal_cases.security.model.account.UsuarioSistema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "seguimiento")
@Getter
@Setter
public class Seguimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    // Fecha límite del seguimiento.
    // Si es null, no se programa recordatorio por fecha de entrega.
    @Column(name = "fecha_entrega")
    private LocalDate fechaEntrega;

    // Cantidad de días antes de la fecha de entrega para enviar recordatorio.
    @Column(name = "dias_notificacion")
    private Integer diasNotificacion;

    // Si es true, el seguimiento debe notificar por correo a las partes externas
    // de la consulta: persona principal, partes y contrapartes.
    @Column(name = "notificar_partes", nullable = false)
    private Boolean notificarPartes = false;

    // Si es true, el seguimiento debe notificarse al estudiante y también
    // puede mostrarse al estudiante en pantalla.
    // Si es false, el estudiante no recibe correo ni debe ver este seguimiento.
    @Column(name = "notificar_estudiante", nullable = false)
    private Boolean notificarEstudiante = false;

    // Si es true, el seguimiento representa una alerta disciplinaria
    // asociada a un incumplimiento grave del estudiante.
    @Column(name = "alerta_disciplinaria", nullable = false)
    private Boolean alertaDisciplinaria = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_seguimiento", nullable = false)
    private CategoriaSeguimiento categoriaSeguimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consulta", nullable = false)
    private Consulta consulta;

    // Usuario del sistema que creó el seguimiento.
    // Normalmente será asesor o monitor.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor", nullable = false)
    private UsuarioSistema autor;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        LocalDateTime ahora = LocalDateTime.now();

        if (fechaCreacion == null) {
            fechaCreacion = ahora;
        }

        if (notificarPartes == null) {
            notificarPartes = false;
        }

        if (notificarEstudiante == null) {
            notificarEstudiante = false;
        }

        if (alertaDisciplinaria == null) {
            alertaDisciplinaria = false;
        }
    }

    // Para actualizar la fecha actual en la que se hizo modificacion
    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();

        if (notificarPartes == null) {
            notificarPartes = false;
        }

        if (notificarEstudiante == null) {
            notificarEstudiante = false;
        }

        if (alertaDisciplinaria == null) {
            alertaDisciplinaria = false;
        }
    }
}