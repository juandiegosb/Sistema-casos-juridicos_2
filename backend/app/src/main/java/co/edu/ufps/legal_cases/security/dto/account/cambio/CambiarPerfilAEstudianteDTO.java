package co.edu.ufps.legal_cases.security.dto.account.cambio;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambiarPerfilAEstudianteDTO extends CambiarPerfilBaseDTO {

    // Todo estudiante debe estar asociado a un asesor activo.
    private Long asesorId;

    private Boolean conciliacion;
}