package co.edu.ufps.legal_cases.business.dto.seguimiento;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DatosNotificacionSeguimientoDTO {

    private Long seguimientoId;

    private Long consultaId;

    private Long autorUsuarioSistemaId;

    private String autorEmail;
}