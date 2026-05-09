package co.edu.ufps.legal_cases.security.dto.account.cambio;

import co.edu.ufps.legal_cases.business.model.perfil.TipoConciliador;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambiarPerfilAConciliadorDTO {

    // No se pone email porque eso ya existe en el usuario de sistema

    // Rol conciliador que tendrá el usuario después del cambio.
    private Long rolId;

    // Motivo del cambio. Se guardará en el historial.
    private String motivo;

    // Datos propios del nuevo perfil conciliador.
    private String nombre;

    private Long tipoDocumentoId;

    private String documento;

    private String telefono;

    private String usuario;

    private String codigo;

    private Long sedeId;

    private TipoConciliador tipoConciliador;
}