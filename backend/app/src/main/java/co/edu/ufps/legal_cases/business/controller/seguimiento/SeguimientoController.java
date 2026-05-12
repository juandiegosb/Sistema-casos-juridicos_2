package co.edu.ufps.legal_cases.business.controller.seguimiento;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.ufps.legal_cases.business.dto.seguimiento.SeguimientoRequestDTO;
import co.edu.ufps.legal_cases.business.dto.seguimiento.SeguimientoResponseDTO;
import co.edu.ufps.legal_cases.business.service.seguimiento.SeguimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/seguimientos")
@RequiredArgsConstructor
public class SeguimientoController {

    private final SeguimientoService seguimientoService;

    @GetMapping("/consulta/{consultaId}")
    public List<SeguimientoResponseDTO> listarPorConsulta(@PathVariable Long consultaId) {
        return seguimientoService.listarPorConsulta(consultaId);
    }

    @GetMapping("/autor/{autorId}")
    public List<SeguimientoResponseDTO> listarPorAutor(@PathVariable Long autorId) {
        return seguimientoService.listarPorAutor(autorId);
    }

    @GetMapping("/alertas-disciplinarias")
    public List<SeguimientoResponseDTO> listarAlertasDisciplinarias() {
        return seguimientoService.listarAlertasDisciplinarias();
    }

    @GetMapping("/fecha-entrega")
    public List<SeguimientoResponseDTO> listarPorFechaEntrega(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaEntrega) {
        return seguimientoService.listarPorFechaEntrega(fechaEntrega);
    }

    @GetMapping("/{id}")
    public SeguimientoResponseDTO obtenerPorId(@PathVariable Long id) {
        return seguimientoService.obtenerPorId(id);
    }

    @PostMapping
    public SeguimientoResponseDTO crear(
            @Valid @RequestBody SeguimientoRequestDTO dto,
            Authentication authentication) {

        String autorUsername = authentication != null ? authentication.getName() : null;

        return seguimientoService.crear(dto, autorUsername);
    }

    @PutMapping("/{id}")
    public SeguimientoResponseDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SeguimientoRequestDTO dto) {
        return seguimientoService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        seguimientoService.eliminar(id);
    }
}