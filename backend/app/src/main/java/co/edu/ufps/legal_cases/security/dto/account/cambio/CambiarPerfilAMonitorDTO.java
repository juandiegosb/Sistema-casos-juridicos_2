package co.edu.ufps.legal_cases.security.dto.account.cambio;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambiarPerfilAMonitorDTO {

    // No se pone email porque eso ya existe en el usuario de sistema

    // No se pone email porque eso ya existe en el usuario de sistema

    // Rol monitor que tendrá el usuario después del cambio.
    private Long rolId;

    // Motivo del cambio. Se guardará en el historial.
    private String motivo;

    // Datos propios del nuevo perfil monitor.
    private String nombre;

    private Long tipoDocumentoId;

    // En Monitor el documento puede ser opcional según la lógica actual.
    private String documento;

    private String telefono;

    private String usuario;

    private String codigo;

    private Long sedeId;
}