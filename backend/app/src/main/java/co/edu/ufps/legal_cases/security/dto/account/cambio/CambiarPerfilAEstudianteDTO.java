package co.edu.ufps.legal_cases.security.dto.account.cambio;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambiarPerfilAEstudianteDTO {

    // No se pone email porque eso ya existe en el usuario de sistema

    // Rol estudiante que tendrá el usuario después del cambio.
    private Long rolId;

    // Motivo del cambio. Se guardará en el historial.
    private String motivo;

    // Datos propios del nuevo perfil estudiante.
    private String nombre;

    private Long tipoDocumentoId;

    private String documento;

    private String telefono;

    private String usuario;

    private String codigo;

    private Long sedeId;

    // Todo estudiante debe quedar asociado a un asesor activo.
    private Long asesorId;

    private Boolean conciliacion;
}