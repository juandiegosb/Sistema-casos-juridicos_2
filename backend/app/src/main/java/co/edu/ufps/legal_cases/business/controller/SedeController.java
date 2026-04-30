package co.edu.ufps.legal_cases.business.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.edu.ufps.legal_cases.business.dto.SedeDTO;
import co.edu.ufps.legal_cases.business.service.SedeService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sedes")
public class SedeController {

    private final SedeService sedeService;

    public SedeController(SedeService sedeService) {
        this.sedeService = sedeService;
    }

    @GetMapping
    public List<SedeDTO> listar() {
        return sedeService.listar();
    }

    @GetMapping("/{id}")
    public SedeDTO obtenerPorId(@PathVariable Long id) {
        return sedeService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SedeDTO crear(@Valid @RequestBody SedeDTO dto) {
        return sedeService.crear(dto);
    }

    @PutMapping("/{id}")
    public SedeDTO actualizar(@PathVariable Long id, @Valid @RequestBody SedeDTO dto) {
        return sedeService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        sedeService.eliminar(id);
    }
}