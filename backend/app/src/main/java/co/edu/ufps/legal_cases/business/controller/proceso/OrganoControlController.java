package co.edu.ufps.legal_cases.business.controller.proceso;

import static co.edu.ufps.legal_cases.security.constant.PermisoNombre.GESTIONAR_CATALOGOS;
import static co.edu.ufps.legal_cases.security.constant.PermisoNombre.VER_CATALOGOS;

import co.edu.ufps.legal_cases.business.dto.proceso.OrganoControlDTO;
import co.edu.ufps.legal_cases.business.service.proceso.OrganoControlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organos-control")
public class OrganoControlController {

    private final OrganoControlService organoControlService;

    public OrganoControlController(OrganoControlService organoControlService) {
        this.organoControlService = organoControlService;
    }

    // Activos para formularios y combos.
    @GetMapping
    @PreAuthorize("hasAuthority('" + VER_CATALOGOS + "')")
    public List<OrganoControlDTO> listar() {
        return organoControlService.listar();
    }

    // Todos para administración del catálogo.
    @GetMapping("/todos")
    @PreAuthorize("hasAuthority('" + GESTIONAR_CATALOGOS + "')")
    public List<OrganoControlDTO> listarTodos() {
        return organoControlService.listarTodos();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + VER_CATALOGOS + "')")
    public OrganoControlDTO obtenerPorId(@PathVariable Long id) {
        return organoControlService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('" + GESTIONAR_CATALOGOS + "')")
    public OrganoControlDTO crear(@Valid @RequestBody OrganoControlDTO dto) {
        return organoControlService.crear(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + GESTIONAR_CATALOGOS + "')")
    public OrganoControlDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody OrganoControlDTO dto) {
        return organoControlService.actualizar(id, dto);
    }

    @PatchMapping("/{id}/activo")
    @PreAuthorize("hasAuthority('" + GESTIONAR_CATALOGOS + "')")
    public OrganoControlDTO cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo) {
        return organoControlService.cambiarEstado(id, activo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('" + GESTIONAR_CATALOGOS + "')")
    public void eliminar(@PathVariable Long id) {
        organoControlService.eliminar(id);
    }
}