package co.edu.ufps.legal_cases.security.model;

import co.edu.ufps.legal_cases.business.model.Administrativo;
import co.edu.ufps.legal_cases.business.model.Asesor;
import co.edu.ufps.legal_cases.business.model.Conciliador;
import co.edu.ufps.legal_cases.business.model.Estudiante;
import co.edu.ufps.legal_cases.business.model.Monitor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuario_sistema")
@Getter
@Setter
public class UsuarioSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Se usa el correo como usuario de acceso al sistema.
    @Column(name = "username", nullable = false, unique = true, length = 120)
    private String username;

    // La contraseña no se guarda en texto plano, se guarda cifrada.
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // Tipo de perfil real activo del usuario.
    // Este campo hace parte de la normalización nueva.
    // Por ahora puede ser null para permitir migrar usuarios existentes.
    // Más adelante reemplazará la necesidad de consultar asesor_id, estudiante_id,
    // monitor_id, administrativo_id y conciliador_id desde usuario_sistema.
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_perfil_actual", length = 30)
    private TipoPerfilUsuario tipoPerfilActual;

    // Rol administrable asignado al usuario.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    // Perfiles reales del sistema. Solo uno debe estar informado por usuario.
    // Estas relaciones se mantienen temporalmente para no romper la lógica actual.
    // Más adelante se eliminarán cuando el sistema lea el perfil desde cada tabla
    // real mediante usuario_sistema_id.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asesor_id", unique = true)
    private Asesor asesor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", unique = true)
    private Estudiante estudiante;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitor_id", unique = true)
    private Monitor monitor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrativo_id", unique = true)
    private Administrativo administrativo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conciliador_id", unique = true)
    private Conciliador conciliador;
}