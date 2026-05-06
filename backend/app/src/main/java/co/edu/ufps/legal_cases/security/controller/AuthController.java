package co.edu.ufps.legal_cases.security.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Value;
import co.edu.ufps.legal_cases.security.dto.AuthMessageResponseDTO;
import co.edu.ufps.legal_cases.security.dto.CambiarPasswordRequestDTO;
import co.edu.ufps.legal_cases.security.dto.LoginRequestDTO;
import co.edu.ufps.legal_cases.security.dto.LoginResponseDTO;
import co.edu.ufps.legal_cases.security.dto.LoginResultDTO;
import co.edu.ufps.legal_cases.security.dto.RestablecerPasswordDTO;
import co.edu.ufps.legal_cases.security.dto.SolicitarRecuperacionPasswordDTO;
import co.edu.ufps.legal_cases.security.dto.UsuarioSistemaDTO;
import co.edu.ufps.legal_cases.security.service.AuthService;
import co.edu.ufps.legal_cases.security.service.PasswordResetService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

        // Aqui como algunos endpoints son publicos y otros no uso el @PreAuthorize
        // entonces lomanejo desde el SecurityConfig
        private final AuthService authService;
        private final PasswordResetService passwordResetService;
        private final boolean authCookieSecure;
        private final String authCookieSameSite;

        public AuthController(
                        AuthService authService,
                        PasswordResetService passwordResetService,
                        @Value("${app.auth.cookie.secure:true}") boolean authCookieSecure,
                        @Value("${app.auth.cookie.same-site:None}") String authCookieSameSite) {
                this.authService = authService;
                this.passwordResetService = passwordResetService;
                this.authCookieSecure = authCookieSecure;
                this.authCookieSameSite = authCookieSameSite;
        }

        @PostMapping("/login")
        public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
                LoginResultDTO result = authService.login(dto);

                // Creando cookie con el token JWT y se nombra "access_token".
                // En despliegues donde frontend y backend estan en dominios distintos,
                // el navegador solo acepta y envia la cookie con SameSite=None y Secure=true.
                ResponseCookie cookie = crearCookieAuth(result.getToken(), 60 * 60);

                return ResponseEntity.ok() // Responde con 200 ok
                                .header(HttpHeaders.SET_COOKIE, cookie.toString()) // Envia la cookie al navegador
                                .body(result.getResponse()); // En el cuerpo manda el dto con la info y permisos de
                                                             // usuario
        }

        private ResponseCookie crearCookieAuth(String value, long maxAgeSeconds) {
                return ResponseCookie.from("access_token", value)
                                .httpOnly(true) // Para que el js del frontend no pueda acceder a la cookie
                                .secure(authCookieSecure)
                                .path("/") // Para que este disponible en toda la app
                                .maxAge(maxAgeSeconds)
                                .sameSite(authCookieSameSite)
                                .build();
        }

        // Para que el frontend verifique si el usuario esta logueado y obtener su info
        // cuando se cambia de pagina, se recarga la app y demas
        @GetMapping("/me")
        public UsuarioSistemaDTO me(
                        // La cookie no es obligatoria porque puede que el usuario no este logueado
                        @CookieValue(name = "access_token", required = false) String token) {
                return authService.me(token);
        }

        // Para borrar la cookie del token jwt y cerrar la sesion del usuario
        @PostMapping("/logout")
        public ResponseEntity<Void> logout() {
                ResponseCookie cookie = crearCookieAuth("", 0); // Para que expire inmediatamente

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .build();
        }

        // Para cambiar la contraseña del usuario que ya tiene sesion activa
        @PatchMapping("/cambiar-password")
        public ResponseEntity<Void> cambiarPassword(
                        // Se lee el token desde la cookie para saber que usuario esta cambiando su
                        // contraseña
                        @CookieValue(name = "access_token", required = false) String token,
                        @Valid @RequestBody CambiarPasswordRequestDTO dto) {

                authService.cambiarPassword(token, dto);
                return ResponseEntity.noContent().build();
        }

        // Para solicitar la recuperacion de contraseña cuando el usuario no puede
        // iniciar sesion
        @PostMapping("/solicitar-recuperacion")
        public ResponseEntity<AuthMessageResponseDTO> solicitarRecuperacion(
                        @Valid @RequestBody SolicitarRecuperacionPasswordDTO dto) {

                passwordResetService.solicitarRecuperacion(dto);

                // Mensaje generico para no revelar si el correo existe o no
                return ResponseEntity.ok(new AuthMessageResponseDTO(
                                "Si el correo existe, se enviarán instrucciones para recuperar la contraseña"));
        }

        // Para restablecer la contraseña usando el token que llega en el enlace del
        // correo
        @PostMapping("/restablecer-password")
        public ResponseEntity<AuthMessageResponseDTO> restablecerPassword(
                        @Valid @RequestBody RestablecerPasswordDTO dto) {

                passwordResetService.restablecerPassword(dto);

                return ResponseEntity.ok(new AuthMessageResponseDTO(
                                "La contraseña se restableció correctamente"));
        }
}