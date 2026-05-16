package co.edu.ufps.legal_cases.security.dto.account.cambio;

import co.edu.ufps.legal_cases.security.model.account.TipoPerfilUsuario;
import lombok.AllArgsConstructor;
import lombok.Getter;

// Esto se va a usar para que en service por medio de este objeto se diga que perfil quedo activo
@Getter
@AllArgsConstructor
public class ResultadoCambioPerfil {

    private Long perfilId;

    private TipoPerfilUsuario tipoPerfil;
}