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

    // Si es null, no se programa recordatorio por fecha de entrega.
    @Column(name = "fecha_entrega")
    private LocalDate fechaEntrega;

    // Cantidad de días antes de fecha_entrega para notificar por correo.
    @Column(name = "dias_notificacion")
    private Integer diasNotificacion;

    // Si es true, se notifica por correo a las partes involucradas de la consulta.
    // Ejemplo: asesor, monitor u otros actores permitidos según la regla de negocio.
    @Column(name = "notificar_partes", nullable = false)
    private Boolean notificarPartes = false;

    // Si es true, también se notifica al estudiante.
    // Si es false, el estudiante no recibe correo por este seguimiento.
    @Column(name = "notificar_estudiante", nullable = false)
    private Boolean notificarEstudiante = false;

    // Si es true, el seguimiento representa una alerta disciplinaria
    // asociada a un incumplimiento del estudiante.
    @Column(name = "alerta_disciplinaria", nullable = false)
    private Boolean alertaDisciplinaria = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_seguimiento", nullable = false)
    private CategoriaSeguimiento categoriaSeguimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consulta", nullable = false)
    private Consulta consulta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor", nullable = false)
    private UsuarioSistema autor;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
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
}