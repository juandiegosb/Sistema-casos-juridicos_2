package co.edu.ufps.legal_cases.security.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.edu.ufps.legal_cases.security.dto.PermisoDTO;
import co.edu.ufps.legal_cases.security.service.PermisoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/permisos")
public class PermisoController {

    private final PermisoService permisoService;

    public PermisoController(PermisoService permisoService) {
        this.permisoService = permisoService;
    }

    @GetMapping
    public List<PermisoDTO> listar() {
        return permisoService.listar();
    }

    @GetMapping("/activos")
    public List<PermisoDTO> listarActivos() {
        return permisoService.listarActivos();
    }

    @GetMapping("/{id}")
    public PermisoDTO obtenerPorId(@PathVariable Long id) {
        return permisoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PermisoDTO crear(@Valid @RequestBody PermisoDTO dto) {
        return permisoService.crear(dto);
    }

    @PutMapping("/{id}")
    public PermisoDTO actualizar(@PathVariable Long id, @Valid @RequestBody PermisoDTO dto) {
        return permisoService.actualizar(id, dto);
    }

    @PatchMapping("/{id}/activo")
    public PermisoDTO cambiarEstado(@PathVariable Long id, @RequestParam Boolean activo) {
        return permisoService.cambiarEstado(id, activo);
    }
}