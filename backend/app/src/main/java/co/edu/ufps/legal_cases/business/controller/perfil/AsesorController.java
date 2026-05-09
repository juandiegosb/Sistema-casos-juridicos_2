package co.edu.ufps.legal_cases.business.controller.perfil;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import co.edu.ufps.legal_cases.business.dto.perfil.AsesorDTO;
import co.edu.ufps.legal_cases.business.service.perfil.AsesorService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/asesores")
@PreAuthorize("hasAuthority('Gestionar usuarios')")
public class AsesorController {

    private final AsesorService asesorService;

    public AsesorController(AsesorService asesorService) {
        this.asesorService = asesorService;
    }

    @GetMapping
    public List<AsesorDTO> listar() {
        return asesorService.listar();
    }

    @GetMapping("/activos")
    public List<AsesorDTO> listarActivos() {
        return asesorService.listarActivos();
    }

    @GetMapping("/{id}")
    public AsesorDTO obtenerPorId(@PathVariable Long id) {
        return asesorService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AsesorDTO crear(@Valid @RequestBody AsesorDTO dto) {
        return asesorService.crear(dto);
    }

    @PutMapping("/{id}")
    public AsesorDTO actualizar(@PathVariable Long id, @Valid @RequestBody AsesorDTO dto) {
        return asesorService.actualizar(id, dto);
    }

    @PatchMapping("/{id}/activo")
    public AsesorDTO cambiarEstado(@PathVariable Long id, @RequestParam Boolean activo) {
        return asesorService.cambiarEstado(id, activo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        asesorService.eliminar(id);
    }
}