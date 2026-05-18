package co.edu.ufps.legal_cases.business.controller.consulta;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import co.edu.ufps.legal_cases.business.dto.consulta.ConsultaBusquedaDTO;
import co.edu.ufps.legal_cases.business.dto.consulta.ConsultaDTO;
import co.edu.ufps.legal_cases.business.service.consulta.ConsultaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/consultas")
@PreAuthorize("hasAuthority('Gestionar consultas')")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    /**
     * Búsqueda de consultas jurídicas filtrada por rol del usuario autenticado.
     * - Estudiante: solo ve sus propias consultas
     * - Asesor: ve las consultas de sus estudiantes
     * - Monitor: ve las consultas donde está asignado
     * - Administrativo/Conciliador: ve todas
     */
    @GetMapping
    public List<ConsultaBusquedaDTO> buscar(
            @RequestParam(required = false, defaultValue = "") String search,
            Authentication authentication) {
        return consultaService.buscarSegunRol(search, authentication);
    }

    @GetMapping("/{id}")
    public ConsultaDTO obtenerPorId(@PathVariable Long id) {
        return consultaService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConsultaDTO crear(@Valid @RequestBody ConsultaDTO dto) {
        return consultaService.crear(dto);
    }

    @PutMapping("/{id}")
    public ConsultaDTO actualizar(@PathVariable Long id, @Valid @RequestBody ConsultaDTO dto) {
        return consultaService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        consultaService.eliminar(id);
    }

    @PatchMapping("/{id}/archivar")
    public ConsultaDTO archivar(@PathVariable Long id) {
        return consultaService.archivar(id);
    }

    @GetMapping("/archivadas")
    public List<ConsultaBusquedaDTO> listarArchivadas() {
        return consultaService.listarArchivadas();
    }
}