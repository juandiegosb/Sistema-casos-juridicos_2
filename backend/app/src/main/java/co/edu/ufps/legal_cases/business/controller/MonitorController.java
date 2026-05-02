package co.edu.ufps.legal_cases.business.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import co.edu.ufps.legal_cases.business.dto.MonitorDTO;
import co.edu.ufps.legal_cases.business.service.MonitorService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/monitores")
@PreAuthorize("hasAuthority('Gestionar usuarios')")
public class MonitorController {

    private final MonitorService monitorService;

    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @GetMapping
    public List<MonitorDTO> listar() {
        return monitorService.listar();
    }

    @GetMapping("/activos")
    public List<MonitorDTO> listarActivos() {
        return monitorService.listarActivos();
    }

    @GetMapping("/{id}")
    public MonitorDTO obtenerPorId(@PathVariable Long id) {
        return monitorService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MonitorDTO crear(@Valid @RequestBody MonitorDTO dto) {
        return monitorService.crear(dto);
    }

    @PutMapping("/{id}")
    public MonitorDTO actualizar(@PathVariable Long id, @Valid @RequestBody MonitorDTO dto) {
        return monitorService.actualizar(id, dto);
    }

    @PatchMapping("/{id}/activo")
    public MonitorDTO cambiarEstado(@PathVariable Long id, @RequestParam Boolean activo) {
        return monitorService.cambiarEstado(id, activo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        monitorService.eliminar(id);
    }
}