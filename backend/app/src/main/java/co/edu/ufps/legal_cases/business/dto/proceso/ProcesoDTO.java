package co.edu.ufps.legal_cases.business.dto.proceso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "El número de radicado es obligatorio")
    @Size(min = 23, max = 23, message = "El número de radicado debe tener exactamente 23 caracteres")
    private String numeroRadicado;

    @NotNull(message = "El departamento es obligatorio")
    private Long departamentoId;

    @NotNull(message = "La consulta es obligatoria")
    private Long consultaId;

    private Long especialidadId;

    private Long organoControlId;

    private Boolean activo;
}