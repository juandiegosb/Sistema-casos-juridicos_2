package co.edu.ufps.legal_cases.security.dto.account.cambio;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Para guardar ya los datos comunes entre perfiles definitivos que se van mas adelanta a escribir en la bd
@Getter
@AllArgsConstructor
public class DatosPerfilCambioNormalizados {

    private String nombre;

    private String documento;

    private String email;

    private String telefono;

    private String usuario;

    private String codigo;
}