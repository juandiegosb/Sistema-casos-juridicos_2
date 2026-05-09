package co.edu.ufps.legal_cases.security.controller.access;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import co.edu.ufps.legal_cases.security.dto.access.RolDTO;
import co.edu.ufps.legal_cases.security.service.access.RolService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasAuthority('Gestionar roles')")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public List<RolDTO> listar() {
        return rolService.listar();
    }

    @GetMapping("/activos")
    public List<RolDTO> listarActivos() {
        return rolService.listarActivos();
    }

    @GetMapping("/{id}")
    public RolDTO obtenerPorId(@PathVariable Long id) {
        return rolService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RolDTO crear(@Valid @RequestBody RolDTO dto) {
        return rolService.crear(dto);
    }

    @PutMapping("/{id}")
    public RolDTO actualizar(@PathVariable Long id, @Valid @RequestBody RolDTO dto) {
        return rolService.actualizar(id, dto);
    }

    @PatchMapping("/{id}/activo")
    public RolDTO cambiarEstado(@PathVariable Long id, @RequestParam Boolean activo) {
        return rolService.cambiarEstado(id, activo);
    }

    @PatchMapping("/{rolId}/permisos/{permisoId}")
    public RolDTO asignarPermiso(
            @PathVariable Long rolId,
            @PathVariable Long permisoId) {
        return rolService.asignarPermiso(rolId, permisoId);
    }

    @DeleteMapping("/{rolId}/permisos/{permisoId}")
    public RolDTO quitarPermiso(
            @PathVariable Long rolId,
            @PathVariable Long permisoId) {
        return rolService.quitarPermiso(rolId, permisoId);
    }
}