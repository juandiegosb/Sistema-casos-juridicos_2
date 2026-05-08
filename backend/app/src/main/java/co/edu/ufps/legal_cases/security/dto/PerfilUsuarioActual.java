package co.edu.ufps.legal_cases.security.dto;

import co.edu.ufps.legal_cases.security.model.TipoPerfilUsuario;
import lombok.AllArgsConstructor;
import lombok.Getter;

// Esta clase se utiliza internamente para devolver la id del perfil real asociado a la id 
// del usuario que se autentica en la tabla de usuario del sistema
@Getter
@AllArgsConstructor
public class PerfilUsuarioActual {

    private Long perfilId;

    private TipoPerfilUsuario tipoPerfil;
}