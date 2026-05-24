package co.edu.ufps.legal_cases.config.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import co.edu.ufps.legal_cases.common.exception.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

// Para comunicar correctamente los errores por JSON y enviarlos desde Spring Security al frontend.
// 401 = no ha iniciado sesión o el token no es válido.
// 403 = inició sesión, pero no tiene permiso para acceder al recurso.
@Component
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final JsonMapper jsonMapper; // Convierte el error a JSON para enviarlo al frontend.

    public SecurityExceptionHandler(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    // Sin iniciar sesión o sin token válido.
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.core.AuthenticationException authException) throws IOException {

        ErrorResponseDTO error = construirError(
                HttpStatus.UNAUTHORIZED,
                "No autenticado",
                "Debe iniciar sesión para acceder a este recurso",
                request);

        escribirRespuesta(response, HttpStatus.UNAUTHORIZED, error);
    }

    @Override
    // Inició sesión, pero no tiene permisos suficientes.
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        ErrorResponseDTO error = construirError(
                HttpStatus.FORBIDDEN,
                "No autorizado",
                "No tiene permisos para acceder a este recurso",
                request);

        escribirRespuesta(response, HttpStatus.FORBIDDEN, error);
    }

    private ErrorResponseDTO construirError(
            HttpStatus status,
            String error,
            String mensaje,
            HttpServletRequest request) {

        return ErrorResponseDTO.builder()
                .fecha(LocalDateTime.now())
                .estado(status.value())
                .error(error)
                .mensaje(mensaje)
                .ruta(request.getRequestURI())
                .build();
    }

    private void escribirRespuesta(
            HttpServletResponse response,
            HttpStatus status,
            ErrorResponseDTO error) throws IOException {

        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        jsonMapper.writeValue(response.getWriter(), error);
    }
}