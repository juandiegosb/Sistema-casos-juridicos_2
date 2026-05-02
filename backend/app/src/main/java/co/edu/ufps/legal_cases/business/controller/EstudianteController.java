package co.edu.ufps.legal_cases.business.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import co.edu.ufps.legal_cases.business.dto.EstudianteDTO;
import co.edu.ufps.legal_cases.business.service.EstudianteService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/estudiantes")
@PreAuthorize("hasAuthority('Gestionar usuarios')")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    // Listar todos
    @GetMapping
    public List<EstudianteDTO> listar() {
        return estudianteService.listar();
    }

    // Solo activos
    @GetMapping("/activos")
    public List<EstudianteDTO> listarActivos() {
        return estudianteService.listarActivos();
    }

    // Por ID
    @GetMapping("/{id}")
    public EstudianteDTO obtenerPorId(@PathVariable Long id) {
        return estudianteService.obtenerPorId(id);
    }

    // Crear
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EstudianteDTO crear(@Valid @RequestBody EstudianteDTO dto) {
        return estudianteService.crear(dto);
    }

    // Actualizar
    @PutMapping("/{id}")
    public EstudianteDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EstudianteDTO dto
    ) {
        return estudianteService.actualizar(id, dto);
    }

    // Activar / Desactivar
    @PatchMapping("/{id}/activo")
    public EstudianteDTO cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo
    ) {
        return estudianteService.cambiarEstado(id, activo);
    }

    // Cambiar conciliación (propio de estudiante)
    @PatchMapping("/{id}/conciliacion")
    public EstudianteDTO cambiarConciliacion(
            @PathVariable Long id,
            @RequestParam Boolean conciliacion
    ) {
        return estudianteService.cambiarConciliacion(id, conciliacion);
    }

    // Eliminar
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        estudianteService.eliminar(id);
    }
}