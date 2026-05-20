package co.edu.ufps.legal_cases.business.controller.catalogo;

import co.edu.ufps.legal_cases.business.dto.catalogo.DepartamentoDTO;
import co.edu.ufps.legal_cases.business.service.catalogo.DepartamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departamentos")
@PreAuthorize("hasAuthority('Gestionar catálogos')")
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    public DepartamentoController(DepartamentoService departamentoService) {
        this.departamentoService = departamentoService;
    }

    @GetMapping
    public List<DepartamentoDTO> listar() {
        return departamentoService.listar();
    }

    @GetMapping("/{id}")
    public DepartamentoDTO obtenerPorId(@PathVariable Long id) {
        return departamentoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartamentoDTO crear(@Valid @RequestBody DepartamentoDTO dto) {
        return departamentoService.crear(dto);
    }

    @PutMapping("/{id}")
    public DepartamentoDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DepartamentoDTO dto
    ) {
        return departamentoService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        departamentoService.eliminar(id);
    }
}