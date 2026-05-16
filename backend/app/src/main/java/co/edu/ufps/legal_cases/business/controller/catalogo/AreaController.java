package co.edu.ufps.legal_cases.business.controller.catalogo;

import co.edu.ufps.legal_cases.business.dto.catalogo.AreaDTO;
import co.edu.ufps.legal_cases.business.service.catalogo.AreaService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/areas")
@PreAuthorize("hasAuthority('Gestionar catálogos')")
public class AreaController {

    private final AreaService areaService;

    public AreaController(AreaService areaService) {
        this.areaService = areaService;
    }

    @GetMapping
    public List<AreaDTO> listar() {
        return areaService.listar();
    }

    @GetMapping("/{id}")
    public AreaDTO obtenerPorId(@PathVariable Long id) {
        return areaService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AreaDTO crear(@Valid @RequestBody AreaDTO areaDTO) {
        return areaService.crear(areaDTO);
    }

    @PutMapping("/{id}")
    public AreaDTO actualizar(@PathVariable Long id, @Valid @RequestBody AreaDTO areaDTO) {
        return areaService.actualizar(id, areaDTO);
    }

    @GetMapping("/activos")
    public List<AreaDTO> listarActivos() {
        return areaService.listarActivos();
    }

    @PatchMapping("/{id}/desactivar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        areaService.desactivar(id);
    }
}