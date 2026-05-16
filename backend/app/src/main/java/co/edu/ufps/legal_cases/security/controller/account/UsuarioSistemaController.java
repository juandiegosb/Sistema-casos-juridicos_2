package co.edu.ufps.legal_cases.security.controller.account;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import co.edu.ufps.legal_cases.security.dto.account.UsuarioSistemaDTO;
import co.edu.ufps.legal_cases.security.dto.account.cambio.CambiarPerfilAAdministrativoDTO;
import co.edu.ufps.legal_cases.security.dto.account.cambio.CambiarPerfilAAsesorDTO;
import co.edu.ufps.legal_cases.security.dto.account.cambio.CambiarPerfilAConciliadorDTO;
import co.edu.ufps.legal_cases.security.dto.account.cambio.CambiarPerfilAEstudianteDTO;
import co.edu.ufps.legal_cases.security.dto.account.cambio.CambiarPerfilAMonitorDTO;
import co.edu.ufps.legal_cases.security.service.account.UsuarioCambioPerfilService;
import co.edu.ufps.legal_cases.security.service.account.UsuarioSistemaService;

@RestController
@RequestMapping("/api/usuarios-sistema")
@PreAuthorize("hasAuthority('Gestionar usuarios')")
public class UsuarioSistemaController {

    private final UsuarioSistemaService usuarioSistemaService;
    private final UsuarioCambioPerfilService usuarioCambioPerfilService;

    public UsuarioSistemaController(UsuarioSistemaService usuarioSistemaService,
            UsuarioCambioPerfilService usuarioCambioPerfilService) {
        this.usuarioSistemaService = usuarioSistemaService;
        this.usuarioCambioPerfilService = usuarioCambioPerfilService;
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

    // Endpoints para cambiar de roles
    
    @PatchMapping("/{id}/perfil/administrativo")
    public UsuarioSistemaDTO cambiarAAdministrativo(
            @PathVariable Long id,
            @RequestBody CambiarPerfilAAdministrativoDTO dto,
            Authentication authentication) {

        String cambiadoPorUsername = authentication != null ? authentication.getName() : null;

        return usuarioCambioPerfilService.cambiarAAdministrativo(
                id,
                dto,
                cambiadoPorUsername);
    }

    @PatchMapping("/{id}/perfil/estudiante")
    public UsuarioSistemaDTO cambiarAEstudiante(
            @PathVariable Long id,
            @RequestBody CambiarPerfilAEstudianteDTO dto,
            Authentication authentication) {

        String cambiadoPorUsername = authentication != null ? authentication.getName() : null;

        return usuarioCambioPerfilService.cambiarAEstudiante(
                id,
                dto,
                cambiadoPorUsername);
    }

    @PatchMapping("/{id}/perfil/asesor")
    public UsuarioSistemaDTO cambiarAAsesor(
            @PathVariable Long id,
            @RequestBody CambiarPerfilAAsesorDTO dto,
            Authentication authentication) {

        String cambiadoPorUsername = authentication != null ? authentication.getName() : null;

        return usuarioCambioPerfilService.cambiarAAsesor(
                id,
                dto,
                cambiadoPorUsername);
    }

    @PatchMapping("/{id}/perfil/monitor")
    public UsuarioSistemaDTO cambiarAMonitor(
            @PathVariable Long id,
            @RequestBody CambiarPerfilAMonitorDTO dto,
            Authentication authentication) {

        String cambiadoPorUsername = authentication != null ? authentication.getName() : null;

        return usuarioCambioPerfilService.cambiarAMonitor(
                id,
                dto,
                cambiadoPorUsername);
    }

    @PatchMapping("/{id}/perfil/conciliador")
    public UsuarioSistemaDTO cambiarAConciliador(
            @PathVariable Long id,
            @RequestBody CambiarPerfilAConciliadorDTO dto,
            Authentication authentication) {

        String cambiadoPorUsername = authentication != null ? authentication.getName() : null;

        return usuarioCambioPerfilService.cambiarAConciliador(
                id,
                dto,
                cambiadoPorUsername);
    }
}