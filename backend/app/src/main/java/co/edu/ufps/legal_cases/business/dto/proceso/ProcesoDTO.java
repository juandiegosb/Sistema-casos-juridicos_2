package co.edu.ufps.legal_cases.business.dto.proceso;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcesoDTO {

    private Long id;

    @Pattern(
            regexp = "^.{23}$|^$",
            message = "El número de radicado debe tener exactamente 23 caracteres"
    )
    private String numeroRadicado;

    @NotNull(message = "El departamento es obligatorio")
    private Long departamentoId;

    @NotNull(message = "La consulta es obligatoria")
    private Long consultaId;

    private Long especialidadId;

    private Long organoControlId;

    private Boolean activo;
}