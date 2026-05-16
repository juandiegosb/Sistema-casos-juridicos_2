package co.edu.ufps.legal_cases.security.dto.account.cambio;

import co.edu.ufps.legal_cases.business.model.perfil.TipoConciliador;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambiarPerfilAConciliadorDTO extends CambiarPerfilBaseDTO {

    private TipoConciliador tipoConciliador;
}