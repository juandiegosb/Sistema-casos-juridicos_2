package co.edu.ufps.legal_cases.business.dto.estadisticas;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO de respuesta para las estadísticas de un semestre.
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasSemestreDTO {

    private int año;
    private int semestre;
    private String periodoInicio;
    private String periodoFin;

    // Consultas finalizadas (con resultado) en el semestre.
    private long consultasFinalizadas;

    // Consultas pendientes (sin resultado) en el semestre.
    private long consultasPendientes;

    // Total general del semestre.
    private long totalConsultas;

    // Consultas agrupadas por área jurídica del semestre.
    private List<ConteoDTO> consultasPorArea;

    // Total de personas atendidas en el semestre.
    private long totalPersonasAtendidas;

    // Procesos agrupados por estado — todos los tiempos por ahora.
    private List<ConteoDTO> procesosPorEstado;
}