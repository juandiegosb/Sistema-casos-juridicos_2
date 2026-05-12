package co.edu.ufps.legal_cases.business.controller.seguimiento;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.ufps.legal_cases.business.dto.seguimiento.CategoriaSeguimientoDTO;
import co.edu.ufps.legal_cases.business.service.seguimiento.CategoriaSeguimientoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/seguimientos/categorias")
public class CategoriaSeguimientoController {

    private final CategoriaSeguimientoService categoriaSeguimientoService;

    public CategoriaSeguimientoController(CategoriaSeguimientoService categoriaSeguimientoService) {
        this.categoriaSeguimientoService = categoriaSeguimientoService;
    }

    @GetMapping
    public List<CategoriaSeguimientoDTO> listar() {
        return categoriaSeguimientoService.listar();
    }

    @GetMapping("/activas")
    public List<CategoriaSeguimientoDTO> listarActivas() {
        return categoriaSeguimientoService.listarActivas();
    }

    @GetMapping("/{id}")
    public CategoriaSeguimientoDTO obtenerPorId(@PathVariable Long id) {
        return categoriaSeguimientoService.obtenerPorId(id);
    }

    @PostMapping
    public CategoriaSeguimientoDTO crear(@Valid @RequestBody CategoriaSeguimientoDTO dto) {
        return categoriaSeguimientoService.crear(dto);
    }

    @PutMapping("/{id}")
    public CategoriaSeguimientoDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaSeguimientoDTO dto) {
        return categoriaSeguimientoService.actualizar(id, dto);
    }

    @PatchMapping("/{id}/estado")
    public CategoriaSeguimientoDTO cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo) {
        return categoriaSeguimientoService.cambiarEstado(id, activo);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        categoriaSeguimientoService.eliminar(id);
    }
}