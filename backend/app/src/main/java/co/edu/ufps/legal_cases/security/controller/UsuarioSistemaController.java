package co.edu.ufps.legal_cases.security.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import co.edu.ufps.legal_cases.security.dto.UsuarioSistemaDTO;
import co.edu.ufps.legal_cases.security.service.UsuarioSistemaService;

@RestController
@RequestMapping("/api/usuarios-sistema")
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