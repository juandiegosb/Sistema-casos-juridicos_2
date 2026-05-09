package co.edu.ufps.legal_cases.security.dto.account;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioSistemaDTO {

    //Aqui no valido nada porque es un DTO de salida, no de entrada
    private Long id;

    private String username;

    private Boolean activo;

    private Long rolId;

    private String rolNombre;

    private Long perfilId;

    private String tipoPerfil;

    private List<String> permisos;
}