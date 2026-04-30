package co.edu.ufps.legal_cases.business.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.edu.ufps.legal_cases.business.dto.ConciliadorDTO;
import co.edu.ufps.legal_cases.business.service.ConciliadorService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/conciliadores")
public class ConciliadorController {

    private final ConciliadorService conciliadorService;

    public ConciliadorController(ConciliadorService conciliadorService) {
        this.conciliadorService = conciliadorService;
    }

    @GetMapping
    public List<ConciliadorDTO> listar() {
        return conciliadorService.listar();
    }

    @GetMapping("/activos")
    public List<ConciliadorDTO> listarActivos() {
        return conciliadorService.listarActivos();
    }

    @GetMapping("/{id}")
    public ConciliadorDTO obtenerPorId(@PathVariable Long id) {
        return conciliadorService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConciliadorDTO crear(@Valid @RequestBody ConciliadorDTO dto) {
        return conciliadorService.crear(dto);
    }

    @PutMapping("/{id}")
    public ConciliadorDTO actualizar(@PathVariable Long id, @Valid @RequestBody ConciliadorDTO dto) {
        return conciliadorService.actualizar(id, dto);
    }

    @PatchMapping("/{id}/activo")
    public ConciliadorDTO cambiarEstado(@PathVariable Long id, @RequestParam Boolean activo) {
        return conciliadorService.cambiarEstado(id, activo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        conciliadorService.eliminar(id);
    }
}