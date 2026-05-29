package co.edu.ufps.legal_cases.business.controller.estadisticas;

import static co.edu.ufps.legal_cases.security.constant.PermisoNombre.VER_REPORTES;
import static co.edu.ufps.legal_cases.security.constant.PermisoNombre.VER_CONSULTAS;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import co.edu.ufps.legal_cases.business.dto.estadisticas.EstadisticasSemestreDTO;
import co.edu.ufps.legal_cases.business.dto.estadisticas.SemestreDTO;
import co.edu.ufps.legal_cases.business.service.estadisticas.EstadisticasService;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticasController {

    private final EstadisticasService estadisticasService;

    public EstadisticasController(EstadisticasService estadisticasService) {
        this.estadisticasService = estadisticasService;
    }

    // Semestres disponibles para el selector del frontend.
    @GetMapping("/semestres")
    @PreAuthorize("hasAuthority('" + VER_REPORTES + "') or hasAuthority('" + VER_CONSULTAS + "')")
    public List<SemestreDTO> listarSemestresDisponibles() {
        return estadisticasService.listarSemestresDisponibles();
    }

    // Estadísticas globales — solo para administrador.
    @GetMapping("/{año}/semestre/{semestre}")
    @PreAuthorize("hasAuthority('" + VER_REPORTES + "')")
    public EstadisticasSemestreDTO obtenerPorSemestre(
            @PathVariable int año,
            @PathVariable int semestre) {
        return estadisticasService.obtenerEstadisticasSemestre(año, semestre);
    }

    // Estadísticas filtradas por estudiante — para el inicio del estudiante.
    @GetMapping("/{año}/semestre/{semestre}/estudiante/{id}")
    @PreAuthorize("hasAuthority('" + VER_CONSULTAS + "')")
    public EstadisticasSemestreDTO obtenerPorEstudiante(
            @PathVariable int año,
            @PathVariable int semestre,
            @PathVariable Long id) {
        return estadisticasService.obtenerPorEstudiante(año, semestre, id);
    }

    // Estadísticas filtradas por asesor — para el inicio del asesor.
    @GetMapping("/{año}/semestre/{semestre}/asesor/{id}")
    @PreAuthorize("hasAuthority('" + VER_CONSULTAS + "')")
    public EstadisticasSemestreDTO obtenerPorAsesor(
            @PathVariable int año,
            @PathVariable int semestre,
            @PathVariable Long id) {
        return estadisticasService.obtenerPorAsesor(año, semestre, id);
    }

    // Estadísticas filtradas por monitor — para el inicio del monitor.
    @GetMapping("/{año}/semestre/{semestre}/monitor/{id}")
    @PreAuthorize("hasAuthority('" + VER_CONSULTAS + "')")
    public EstadisticasSemestreDTO obtenerPorMonitor(
            @PathVariable int año,
            @PathVariable int semestre,
            @PathVariable Long id) {
        return estadisticasService.obtenerPorMonitor(año, semestre, id);
    }
}