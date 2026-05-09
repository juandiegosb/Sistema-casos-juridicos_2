package co.edu.ufps.legal_cases.business.dto.catalogo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TipoDocumentoDTO {

    private Long id;

    @NotBlank(message = "El nombre visible es obligatorio")
    @Size(max = 100, message = "El nombre visible no puede superar los 100 caracteres")
    private String displayName;

    private Boolean activo;
}