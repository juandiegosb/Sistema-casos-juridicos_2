package co.edu.ufps.legal_cases.security.dto.account.cambio;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambiarPerfilBaseDTO {

    // Rol que tendrá el usuario después del cambio.
    private Long rolId;

    // Motivo del cambio. Se guarda en historial.
    private String motivo;

    // Campos comunes entre perfiles.
    private String nombre;

    private Long tipoDocumentoId;

    private String documento;

    private String telefono;

    private String usuario;

    private String codigo;

    private Long sedeId;
}