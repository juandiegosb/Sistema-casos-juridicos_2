package co.edu.ufps.legal_cases.security.controller.account;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import co.edu.ufps.legal_cases.security.dto.account.UsuarioSistemaDTO;
import co.edu.ufps.legal_cases.security.service.account.UsuarioSistemaService;

@RestController
@RequestMapping("/api/usuarios-sistema")
@PreAuthorize("hasAuthority('Gestionar usuarios')")
public class UsuarioSistemaController {

    private final UsuarioSistemaService usuarioSistemaService;

    public UsuarioSistemaController(UsuarioSistemaService usuarioSistemaService) {
        this.usuarioSistemaService = usuarioSistemaService;
    }

    @GetMapping
    public List<UsuarioSistemaDTO> listar() {
        return usuarioSistemaService.listar();
    }

    @GetMapping("/activos")
    public List<UsuarioSistemaDTO> listarActivos() {
        return usuarioSistemaService.listarActivos();
    }

    @GetMapping("/{id}")
    public UsuarioSistemaDTO obtenerPorId(@PathVariable Long id) {
        return usuarioSistemaService.obtenerPorId(id);
    }

    @PatchMapping("/{id}/activo")
    public UsuarioSistemaDTO cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo) {
        return usuarioSistemaService.cambiarEstado(id, activo);
    }
}