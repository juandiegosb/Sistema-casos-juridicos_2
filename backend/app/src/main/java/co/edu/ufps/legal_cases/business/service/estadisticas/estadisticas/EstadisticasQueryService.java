package co.edu.ufps.legal_cases.business.service.estadisticas.estadisticas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.ufps.legal_cases.business.dto.estadisticas.ConteoDTO;
import co.edu.ufps.legal_cases.business.dto.estadisticas.EstadisticasSemestreDTO;
import co.edu.ufps.legal_cases.business.dto.estadisticas.SemestreDTO;
import co.edu.ufps.legal_cases.business.repository.consulta.ConsultaRepository;
import co.edu.ufps.legal_cases.business.repository.proceso.ProcesoRepository;
import co.edu.ufps.legal_cases.common.exception.BusinessException;

@Service
public class EstadisticasQueryService {

    private final ConsultaRepository consultaRepository;
    private final ProcesoRepository procesoRepository;

    // Año mínimo con datos en el sistema.
    private static final int AÑO_MINIMO = 2024;

    public EstadisticasQueryService(
            ConsultaRepository consultaRepository,
            ProcesoRepository procesoRepository) {
        this.consultaRepository = consultaRepository;
        this.procesoRepository = procesoRepository;
    }

    // Devuelve las estadísticas consolidadas para un semestre específico.
    // Semestre 1: 1 enero - 30 junio.
    // Semestre 2: 1 julio - 31 diciembre.
    @Transactional(readOnly = true)
    public EstadisticasSemestreDTO obtenerEstadisticasSemestre(int año, int semestre) {
        validarParametrosSemestre(año, semestre);

        LocalDate inicio = calcularInicio(año, semestre);
        LocalDate fin = calcularFin(año, semestre);

        // La query nativa sin GROUP BY retorna exactamente una fila en List<Object[]>.
        List<Object[]> resultadoLista = consultaRepository
                .contarFinalizadasYPendientesPorSemestreRaw(año, semestre);
        Object[] fila = (resultadoLista != null && !resultadoLista.isEmpty())
                ? resultadoLista.get(0) : null;
        long finalizadas = fila != null && fila[0] != null
                ? ((Number) fila[0]).longValue() : 0L;
        long pendientes = fila != null && fila[1] != null
                ? ((Number) fila[1]).longValue() : 0L;

        // Consultas agrupadas por área jurídica del semestre.
        List<ConteoDTO> porArea = mapearConteo(
                consultaRepository.contarConsultasPorAreaPorSemestre(año, semestre));

        // Procesos agrupados por estado — todos los tiempos por ahora.
        // El estado es varchar; se normaliza como catálogo en vacaciones.
        List<ConteoDTO> porEstadoProceso = mapearConteo(
                procesoRepository.contarProcesosPorEstado());

        // Total de personas atendidas en el semestre.
        List<Object[]> personasResult = consultaRepository
                .contarPersonasAtendidasPorSemestre(año, semestre);
        long totalPersonas = (personasResult != null && !personasResult.isEmpty()
                && personasResult.get(0)[0] != null)
                ? ((Number) personasResult.get(0)[0]).longValue() : 0L;

        return EstadisticasSemestreDTO.builder()
                .año(año)
                .semestre(semestre)
                .periodoInicio(inicio.toString())
                .periodoFin(fin.toString())
                .consultasFinalizadas(finalizadas)
                .consultasPendientes(pendientes)
                .totalConsultas(finalizadas + pendientes)
                .consultasPorArea(porArea)
                .procesosPorEstado(porEstadoProceso)
                .totalPersonasAtendidas(totalPersonas)
                .build();
    }

    // Lista los semestres disponibles desde el año mínimo hasta el semestre actual.
    @Transactional(readOnly = true)
    public List<SemestreDTO> listarSemestresDisponibles() {
        List<SemestreDTO> semestres = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        int añoActual = hoy.getYear();

        for (int año = AÑO_MINIMO; año <= añoActual; año++) {
            LocalDate inicioS1 = LocalDate.of(año, 1, 1);
            if (!inicioS1.isAfter(hoy)) {
                semestres.add(new SemestreDTO(año, 1, año + "-1",
                        inicioS1.toString(), LocalDate.of(año, 6, 30).toString()));
            }

            LocalDate inicioS2 = LocalDate.of(año, 7, 1);
            if (!inicioS2.isAfter(hoy)) {
                semestres.add(new SemestreDTO(año, 2, año + "-2",
                        inicioS2.toString(), LocalDate.of(año, 12, 31).toString()));
            }
        }

        return semestres;
    }

    // Convierte el resultado de una query nativa agrupada a lista de ConteoDTO.
    // Si la fila tiene 3 columnas: [id, nombre, cantidad] — usa índices 1 y 2.
    // Si la fila tiene 2 columnas: [nombre, cantidad] — usa índices 0 y 1.
    private List<ConteoDTO> mapearConteo(List<Object[]> filas) {
        List<ConteoDTO> resultado = new ArrayList<>();
        for (Object[] fila : filas) {
            boolean tresColumnas = fila.length >= 3;
            String nombre = tresColumnas
                    ? (fila[1] != null ? fila[1].toString() : "Sin nombre")
                    : (fila[0] != null ? fila[0].toString() : "Sin nombre");
            long cantidad = tresColumnas
                    ? (fila[2] != null ? ((Number) fila[2]).longValue() : 0L)
                    : (fila[1] != null ? ((Number) fila[1]).longValue() : 0L);
            resultado.add(new ConteoDTO(nombre, cantidad));
        }
        return resultado;
    }

    private void validarParametrosSemestre(int año, int semestre) {
        if (semestre < 1 || semestre > 2) {
            throw new BusinessException("El semestre debe ser 1 o 2");
        }
        if (año < AÑO_MINIMO) {
            throw new BusinessException("No hay datos disponibles antes del año " + AÑO_MINIMO);
        }
        LocalDate hoy = LocalDate.now();
        if (calcularInicio(año, semestre).isAfter(hoy)) {
            throw new BusinessException(
                    "No se pueden consultar estadísticas de un semestre que aún no ha comenzado");
        }
    }

    private LocalDate calcularInicio(int año, int semestre) {
        return semestre == 1 ? LocalDate.of(año, 1, 1) : LocalDate.of(año, 7, 1);
    }

    private LocalDate calcularFin(int año, int semestre) {
        return semestre == 1 ? LocalDate.of(año, 6, 30) : LocalDate.of(año, 12, 31);
    }
}