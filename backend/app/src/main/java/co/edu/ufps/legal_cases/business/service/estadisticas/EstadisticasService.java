package co.edu.ufps.legal_cases.business.service.estadisticas;

import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.ufps.legal_cases.business.dto.estadisticas.EstadisticasSemestreDTO;
import co.edu.ufps.legal_cases.business.dto.estadisticas.SemestreDTO;
import co.edu.ufps.legal_cases.business.service.estadisticas.estadisticas.EstadisticasPerfilQueryService;
import co.edu.ufps.legal_cases.business.service.estadisticas.estadisticas.EstadisticasQueryService;

// Fachada del módulo de estadísticas.
// El controller entra por aquí.
@Service
public class EstadisticasService {

    private final EstadisticasQueryService estadisticasQueryService;
    private final EstadisticasPerfilQueryService estadisticasPerfilQueryService;

    public EstadisticasService(
            EstadisticasQueryService estadisticasQueryService,
            EstadisticasPerfilQueryService estadisticasPerfilQueryService) {
        this.estadisticasQueryService = estadisticasQueryService;
        this.estadisticasPerfilQueryService = estadisticasPerfilQueryService;
    }

    // Estadísticas globales para administrador.
    public EstadisticasSemestreDTO obtenerEstadisticasSemestre(int año, int semestre) {
        return estadisticasQueryService.obtenerEstadisticasSemestre(año, semestre);
    }

    // Estadísticas filtradas por perfil para el inicio/dashboard.
    public EstadisticasSemestreDTO obtenerPorEstudiante(int año, int semestre, Long estudianteId) {
        return estadisticasPerfilQueryService.obtenerPorEstudiante(año, semestre, estudianteId);
    }

    public EstadisticasSemestreDTO obtenerPorAsesor(int año, int semestre, Long asesorId) {
        return estadisticasPerfilQueryService.obtenerPorAsesor(año, semestre, asesorId);
    }

    public EstadisticasSemestreDTO obtenerPorMonitor(int año, int semestre, Long monitorId) {
        return estadisticasPerfilQueryService.obtenerPorMonitor(año, semestre, monitorId);
    }

    public List<SemestreDTO> listarSemestresDisponibles() {
        return estadisticasQueryService.listarSemestresDisponibles();
    }
}