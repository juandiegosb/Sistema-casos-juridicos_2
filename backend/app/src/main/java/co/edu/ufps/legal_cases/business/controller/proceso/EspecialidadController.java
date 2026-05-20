package co.edu.ufps.legal_cases.business.controller.proceso;

import co.edu.ufps.legal_cases.business.dto.proceso.EspecialidadDTO;
import co.edu.ufps.legal_cases.business.service.proceso.EspecialidadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/especialidades")
@PreAuthorize("hasAuthority('Gestionar catálogos')")
public class EspecialidadController {

    private final EspecialidadService especialidadService;

    public EspecialidadController(EspecialidadService especialidadService) {
        this.especialidadService = especialidadService;
    }

    @GetMapping
    public List<EspecialidadDTO> listar() {
        return especialidadService.listar();
    }

    @GetMapping("/{id}")
    public EspecialidadDTO obtenerPorId(@PathVariable Long id) {
        return especialidadService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EspecialidadDTO crear(@Valid @RequestBody EspecialidadDTO dto) {
        return especialidadService.crear(dto);
    }

    @PutMapping("/{id}")
    public EspecialidadDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EspecialidadDTO dto
    ) {
        return especialidadService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        especialidadService.eliminar(id);
    }
}