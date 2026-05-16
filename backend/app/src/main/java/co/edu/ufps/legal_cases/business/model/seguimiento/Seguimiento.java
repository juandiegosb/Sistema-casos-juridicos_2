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

    // Si es null, no se programa recordatorio.
    @Column(name = "fecha_entrega")
    private LocalDate fechaEntrega;

    // Dias antes de la fecha de entrega para enviar el recordatorio.
    @Column(name = "dias_notificacion")
    private Integer diasNotificacion;

    // Notifica por correo a la persona principal, partes y contrapartes.
    @Column(name = "notificar_partes", nullable = false)
    private Boolean notificarPartes = false;

    // Notifica por correo al estudiante y permite que vea el seguimiento.
    @Column(name = "notificar_estudiante", nullable = false)
    private Boolean notificarEstudiante = false;

    // Marca el seguimiento como alerta disciplinaria para notificar administrativos.
    @Column(name = "alerta_disciplinaria", nullable = false)
    private Boolean alertaDisciplinaria = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_seguimiento", nullable = false)
    private CategoriaSeguimiento categoriaSeguimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consulta", nullable = false)
    private Consulta consulta;

    // Usuario del sistema que crea el seguimiento.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor", nullable = false)
    private UsuarioSistema autor;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }

        normalizarBooleanos();
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
        normalizarBooleanos();
    }

    private void normalizarBooleanos() {
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