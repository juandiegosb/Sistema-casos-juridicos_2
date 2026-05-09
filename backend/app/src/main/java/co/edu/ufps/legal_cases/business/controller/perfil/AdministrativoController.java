package co.edu.ufps.legal_cases.business.controller.perfil;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import co.edu.ufps.legal_cases.business.dto.perfil.AdministrativoDTO;
import co.edu.ufps.legal_cases.business.service.perfil.AdministrativoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/administrativos")
@PreAuthorize("hasAuthority('Gestionar usuarios')")
public class AdministrativoController {

    private final AdministrativoService administrativoService;

    public AdministrativoController(AdministrativoService administrativoService) {
        this.administrativoService = administrativoService;
    }

    @GetMapping
    public List<AdministrativoDTO> listar() {
        return administrativoService.listar();
    }

    @GetMapping("/activos")
    public List<AdministrativoDTO> listarActivos() {
        return administrativoService.listarActivos();
    }

    @GetMapping("/directoras")
    public List<AdministrativoDTO> listarDirectoras() {
        return administrativoService.listarDirectoras();
    }

    @GetMapping("/{id}")
    public AdministrativoDTO obtenerPorId(@PathVariable Long id) {
        return administrativoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdministrativoDTO crear(@Valid @RequestBody AdministrativoDTO dto) {
        return administrativoService.crear(dto);
    }

    @PutMapping("/{id}")
    public AdministrativoDTO actualizar(@PathVariable Long id, @Valid @RequestBody AdministrativoDTO dto) {
        return administrativoService.actualizar(id, dto);
    }

    @PatchMapping("/{id}/activo")
    public AdministrativoDTO cambiarEstado(@PathVariable Long id, @RequestParam Boolean activo) {
        return administrativoService.cambiarEstado(id, activo);
    }

    @PatchMapping("/{id}/directora")
    public AdministrativoDTO cambiarDirectora(@PathVariable Long id, @RequestParam Boolean directora) {
        return administrativoService.cambiarDirectora(id, directora);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        administrativoService.eliminar(id);
    }
}