package co.edu.ufps.legal_cases.security.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.ufps.legal_cases.security.dto.LoginRequestDTO;
import co.edu.ufps.legal_cases.security.dto.LoginResponseDTO;
import co.edu.ufps.legal_cases.security.dto.LoginResultDTO;
import co.edu.ufps.legal_cases.security.dto.UsuarioSistemaDTO;
import co.edu.ufps.legal_cases.security.service.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        LoginResultDTO result = authService.login(dto);

        // Creando cookie con el token JWT y se nombra "access_token"
        ResponseCookie cookie = ResponseCookie.from("access_token", result.getToken())
                .httpOnly(true) // Para que el js del frontend no pueda acceder a la cookie
                .secure(false) // Para que se puede usar sin https, luego debe ser true para que solo se envie
                               // por https
                .path("/") // Para que este distonible en toda la app
                .maxAge(60 * 60) // Cuando tura la cookie (debe ser igual que la duracion del token)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok() // Responde con 200 ok
                .header(HttpHeaders.SET_COOKIE, cookie.toString()) // Envia la cookie al navagador
                .body(result.getResponse()); // En el cuerpo manda el dto con la info y permisos de usuario
    }

    // Para el que frontend verifique si el usuario esta logueado y obtener su info
    // cuando se cambia de pagina y demas
    @GetMapping("/me")
    public UsuarioSistemaDTO me(
            // La cookie no es obligatoria porque puede que el usuario no este logueado
            @CookieValue(name = "access_token", required = false) String token) {
        return authService.me(token);
    }

    //Para borrar la cookie del token jwt y cerrar la sesion del usuario
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0) // para que expire
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}