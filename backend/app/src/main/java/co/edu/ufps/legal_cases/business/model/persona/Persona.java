package co.edu.ufps.legal_cases.business.model.persona;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "persona")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Informacion basica
    @Column(name = "tipo_usuario", nullable = false, length = 20)
    private String tipoUsuario;

    @Column(name = "tipo_documento", nullable = false, length = 10)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, unique = true, length = 30)
    private String numeroDocumento;

    @Column(name = "fecha_expedicion", nullable = false)
    private LocalDate fechaExpedicion;

    @Column(name = "ciudad_expedicion", nullable = false, length = 100)
    private String ciudadExpedicion;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "nombre_identitario", nullable = false, length = 100)
    private String nombreIdentitario;

    @Column(name = "pronombre", nullable = false, length = 50)
    private String pronombre;

    @Column(name = "sexo", nullable = false, length = 20)
    private String sexo;

    @Column(name = "genero", nullable = false, length = 20)
    private String genero;

    @Column(name = "orientacion_sexual", nullable = false, length = 50)
    private String orientacionSexual;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "correo", length = 120)
    private String correo;

    @Column(name = "nacionalidad", nullable = false, length = 50)
    private String nacionalidad;

    @Column(name = "estado_civil", nullable = false, length = 30)
    private String estadoCivil;

    @Column(name = "escolaridad", nullable = false, length = 100)
    private String escolaridad;

    @Column(name = "grupo_etnico", nullable = false, length = 100)
    private String grupoEtnico;

    @Column(name = "condicion_actual", nullable = false, length = 100)
    private String condicionActual;

    @Column(name = "sabe_leer_escribir", nullable = false)
    private Boolean sabeLeerEscribir;

    @Column(name = "discapacidad", nullable = false, length = 100)
    private String discapacidad;

    @Column(name = "caracterizacion_pcd", nullable = false, length = 150)
    private String caracterizacionPcd;

    @Column(name = "necesita_ajuste_pcd", nullable = false)
    private Boolean necesitaAjustePcd;

    // Informacion de vivienda
    @Column(name = "departamento", nullable = false, length = 100)
    private String departamento;

    @Column(name = "municipio", nullable = false, length = 100)
    private String municipio;

    @Column(name = "barrio", nullable = false, length = 100)
    private String barrio;

    @Column(name = "direccion", nullable = false, length = 150)
    private String direccion;

    @Column(name = "comuna", nullable = false, length = 100)
    private String comuna;

    @Column(name = "localidad", nullable = false, length = 100)
    private String localidad;

    @Column(name = "estrato", nullable = false)
    private Integer estrato;

    @Column(name = "tipo_vivienda", nullable = false, length = 100)
    private String tipoVivienda;

    @Column(name = "zona", nullable = false, length = 50)
    private String zona;

    @Column(name = "tenencia", nullable = false, length = 100)
    private String tenencia;

    @Column(name = "numero_personas_a_cargo", nullable = false)
    private Integer numeroPersonasACargo;

    @Column(name = "ingresos_adicionales", nullable = false)
    private Boolean ingresosAdicionales;

    @Column(name = "energia_electrica", nullable = false)
    private Boolean energiaElectrica;

    @Column(name = "acueducto", nullable = false)
    private Boolean acueducto;

    @Column(name = "alcantarillado", nullable = false)
    private Boolean alcantarillado;

    // Aspectos economicos
    @Column(name = "ocupacion", nullable = false, length = 100)
    private String ocupacion;

    @Column(name = "empresa", nullable = false, length = 150)
    private String empresa;

    @Column(name = "salario", nullable = false)
    private Integer salario;

    @Column(name = "cargo", nullable = false, length = 100)
    private String cargo;

    @Column(name = "direccion_empresa", nullable = false, length = 150)
    private String direccionEmpresa;

    @Column(name = "telefono_empresa", nullable = false, length = 30)
    private String telefonoEmpresa;

    // Datos del acudiente
    @Column(name = "nombre_completo", length = 150)
    private String nombreCompletoAcudiente;

    @Column(name = "relacion", length = 100)
    private String relacionAcudiente;

    @Column(name = "telefono_acudiente", length = 30)
    private String telefonoAcudiente;

    @Column(name = "correo_acudiente", length = 120)
    private String correoAcudiente;

    @Column(name = "direccion_acudiente", length = 150)
    private String direccionAcudiente;

    // Informacion del servicio
    @Column(name = "como_se_entero", nullable = false, length = 150)
    private String comoSeEntero;

    @Column(name = "relacion_con_universidad", nullable = false, length = 150)
    private String relacionConUniversidad;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}