package co.edu.ufps.legal_cases.business.controller.proceso;

import static co.edu.ufps.legal_cases.security.constant.PermisoNombre.GESTIONAR_PROCESOS;
import static co.edu.ufps.legal_cases.security.constant.PermisoNombre.VER_PROCESOS;

import co.edu.ufps.legal_cases.business.dto.proceso.ProcesoDTO;
import co.edu.ufps.legal_cases.business.service.proceso.ProcesoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procesos")
public class ProcesoController {

    private final ProcesoService procesoService;

    public ProcesoController(ProcesoService procesoService) {
        this.procesoService = procesoService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + VER_PROCESOS + "')")
    public List<ProcesoDTO> listar() {
        return procesoService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + VER_PROCESOS + "')")
    public ProcesoDTO obtenerPorId(@PathVariable Long id) {
        return procesoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('" + GESTIONAR_PROCESOS + "')")
    public ProcesoDTO crear(@Valid @RequestBody ProcesoDTO dto) {
        return procesoService.crear(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + GESTIONAR_PROCESOS + "')")
    public ProcesoDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProcesoDTO dto) {
        return procesoService.actualizar(id, dto);
    }

    @PatchMapping("/{id}/activo")
    @PreAuthorize("hasAuthority('" + GESTIONAR_PROCESOS + "')")
    public ProcesoDTO cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo) {
        return procesoService.cambiarEstado(id, activo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('" + GESTIONAR_PROCESOS + "')")
    public void eliminar(@PathVariable Long id) {
        procesoService.eliminar(id);
    }
}