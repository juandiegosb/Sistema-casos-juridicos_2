package co.edu.ufps.legal_cases.business.controller.proceso;

import co.edu.ufps.legal_cases.business.dto.proceso.ProcesoDTO;
import co.edu.ufps.legal_cases.business.service.proceso.ProcesoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procesos")
@PreAuthorize("hasAuthority('Gestionar procesos')")
public class ProcesoController {

    private final ProcesoService procesoService;

    public ProcesoController(ProcesoService procesoService) {
        this.procesoService = procesoService;
    }

    @GetMapping
    public List<ProcesoDTO> listar() {
        return procesoService.listar();
    }

    @GetMapping("/{id}")
    public ProcesoDTO obtenerPorId(@PathVariable Long id) {
        return procesoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProcesoDTO crear(@Valid @RequestBody ProcesoDTO dto) {
        return procesoService.crear(dto);
    }

    @PutMapping("/{id}")
    public ProcesoDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProcesoDTO dto
    ) {
        return procesoService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        procesoService.eliminar(id);
    }
}