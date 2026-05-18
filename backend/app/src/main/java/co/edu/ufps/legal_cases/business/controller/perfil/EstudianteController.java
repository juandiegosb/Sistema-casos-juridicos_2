package co.edu.ufps.legal_cases.business.controller.perfil;

import static co.edu.ufps.legal_cases.security.constant.PermisoNombre.CAMBIAR_ESTADO_ESTUDIANTES;
import static co.edu.ufps.legal_cases.security.constant.PermisoNombre.CREAR_USUARIOS;
import static co.edu.ufps.legal_cases.security.constant.PermisoNombre.EDITAR_USUARIOS;
import static co.edu.ufps.legal_cases.security.constant.PermisoNombre.GESTIONAR_USUARIOS;
import static co.edu.ufps.legal_cases.security.constant.PermisoNombre.VER_ESTUDIANTES;
import static co.edu.ufps.legal_cases.security.constant.PermisoNombre.VER_PERFILES_AUXILIARES;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import co.edu.ufps.legal_cases.business.dto.perfil.EstudianteDTO;
import co.edu.ufps.legal_cases.business.service.perfil.EstudianteService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    // Listar todos según alcance:
    // - Administrador: todos.
    // - Asesor: solo sus estudiantes.
    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + VER_ESTUDIANTES + "', '" + VER_PERFILES_AUXILIARES + "', '" + GESTIONAR_USUARIOS + "')")
    public List<EstudianteDTO> listar() {
        return estudianteService.listar();
    }

    // Solo activos según alcance:
    // - Administrador: todos los activos.
    // - Asesor: solo sus estudiantes activos.
    @GetMapping("/activos")
    @PreAuthorize("hasAnyAuthority('" + VER_ESTUDIANTES + "', '" + VER_PERFILES_AUXILIARES + "', '" + GESTIONAR_USUARIOS + "')")
    public List<EstudianteDTO> listarActivos() {
        return estudianteService.listarActivos();
    }

    @GetMapping("/conciliacion")
    @PreAuthorize("hasAnyAuthority('" + VER_ESTUDIANTES + "', '" + VER_PERFILES_AUXILIARES + "', '" + GESTIONAR_USUARIOS + "')")
    public List<EstudianteDTO> listarConConciliacion() {
        return estudianteService.listarConConciliacion();
    }

    // Solo activos de un asesor específico.
    // Si el usuario es asesor, solo puede consultar su propio id de asesor.
    @GetMapping("/activos/asesor/{asesorId}")
    @PreAuthorize("hasAnyAuthority('" + VER_ESTUDIANTES + "', '" + VER_PERFILES_AUXILIARES + "', '" + GESTIONAR_USUARIOS + "')")
    public List<EstudianteDTO> listarActivosPorAsesor(@PathVariable Long asesorId) {
        return estudianteService.listarActivosPorAsesor(asesorId);
    }

    // Por ID, respetando alcance.
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + VER_ESTUDIANTES + "', '" + VER_PERFILES_AUXILIARES + "', '" + GESTIONAR_USUARIOS + "')")
    public EstudianteDTO obtenerPorId(@PathVariable Long id) {
        return estudianteService.obtenerPorId(id);
    }

    // Crear estudiante.
    // Por ahora se mantiene asociado a gestión de usuarios porque crear estudiante crea usuario del sistema.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('" + CREAR_USUARIOS + "', '" + GESTIONAR_USUARIOS + "')")
    public EstudianteDTO crear(@Valid @RequestBody EstudianteDTO dto) {
        return estudianteService.crear(dto);
    }

    // Actualizar estudiante.
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + EDITAR_USUARIOS + "', '" + GESTIONAR_USUARIOS + "')")
    public EstudianteDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EstudianteDTO dto
    ) {
        return estudianteService.actualizar(id, dto);
    }

    // Activar / Desactivar.
    @PatchMapping("/{id}/activo")
    @PreAuthorize("hasAnyAuthority('" + CAMBIAR_ESTADO_ESTUDIANTES + "', '" + GESTIONAR_USUARIOS + "')")
    public EstudianteDTO cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo
    ) {
        return estudianteService.cambiarEstado(id, activo);
    }

    // Cambiar conciliación.
    @PatchMapping("/{id}/conciliacion")
    @PreAuthorize("hasAnyAuthority('" + EDITAR_USUARIOS + "', '" + GESTIONAR_USUARIOS + "')")
    public EstudianteDTO cambiarConciliacion(
            @PathVariable Long id,
            @RequestParam Boolean conciliacion
    ) {
        return estudianteService.cambiarConciliacion(id, conciliacion);
    }

    // Eliminar.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('" + GESTIONAR_USUARIOS + "')")
    public void eliminar(@PathVariable Long id) {
        estudianteService.eliminar(id);
    }
}