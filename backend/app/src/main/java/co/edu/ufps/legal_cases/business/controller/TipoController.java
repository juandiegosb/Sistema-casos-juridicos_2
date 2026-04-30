package co.edu.ufps.legal_cases.business.controller;

import co.edu.ufps.legal_cases.business.dto.TipoDTO;
import co.edu.ufps.legal_cases.business.service.TipoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos")
public class TipoController {

    private final TipoService tipoService;

    public TipoController(TipoService tipoService) {
        this.tipoService = tipoService;
    }

    @GetMapping
    public List<TipoDTO> listar() {
        return tipoService.listar();
    }

    @GetMapping("/{id}")
    public TipoDTO obtenerPorId(@PathVariable Long id) {
        return tipoService.obtenerPorId(id);
    }

    @GetMapping("/tema/{temaId}")
    public List<TipoDTO> listarPorTema(@PathVariable Long temaId) {
        return tipoService.listarPorTema(temaId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TipoDTO crear(@Valid @RequestBody TipoDTO tipoDTO) {
        return tipoService.crear(tipoDTO);
    }

    @PutMapping("/{id}")
    public TipoDTO actualizar(@PathVariable Long id, @Valid @RequestBody TipoDTO tipoDTO) {
        return tipoService.actualizar(id, tipoDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        tipoService.eliminar(id);
    }
}