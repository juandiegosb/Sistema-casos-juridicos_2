package co.edu.ufps.legal_cases.business.controller.catalogo;

import co.edu.ufps.legal_cases.business.dto.catalogo.TemaDTO;
import co.edu.ufps.legal_cases.business.service.catalogo.TemaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/temas")
@PreAuthorize("hasAuthority('Gestionar catálogos')")
public class TemaController {

    private final TemaService temaService;

    public TemaController(TemaService temaService) {
        this.temaService = temaService;
    }

    @GetMapping
    public List<TemaDTO> listar() {
        return temaService.listar();
    }

    @GetMapping("/{id}")
    public TemaDTO obtenerPorId(@PathVariable Long id) {
        return temaService.obtenerPorId(id);
    }

    @GetMapping("/area/{areaId}")
    public List<TemaDTO> listarPorArea(@PathVariable Long areaId) {
        return temaService.listarPorArea(areaId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TemaDTO crear(@Valid @RequestBody TemaDTO temaDTO) {
        return temaService.crear(temaDTO);
    }

    @PutMapping("/{id}")
    public TemaDTO actualizar(@PathVariable Long id, @Valid @RequestBody TemaDTO temaDTO) {
        return temaService.actualizar(id, temaDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        temaService.eliminar(id);
    }
}