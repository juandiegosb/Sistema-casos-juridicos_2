package co.edu.ufps.legal_cases.config.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import co.edu.ufps.legal_cases.common.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

// Para comunicar correctamente los errores por json y enviarlos en el Spring Security al frontend
//      401 = no ha iniciado sesión
//      403 = inició sesión, pero no tiene permiso
@Component
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final JsonMapper jsonMapper;    // Para convertir el error a json y enviarlo al frontend

    public SecurityExceptionHandler(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    // Sin iniciar sesion o token valido
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.core.AuthenticationException authException) throws IOException {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "No autenticado",
                "Debe iniciar sesión para acceder a este recurso",
                request.getRequestURI()
        );

        escribirRespuesta(response, HttpStatus.UNAUTHORIZED, error);
    }

    @Override
    // Si inicio sesion pero no tiene permisos
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "No autorizado",
                "No tiene permisos para acceder a este recurso",
                request.getRequestURI()
        );

        escribirRespuesta(response, HttpStatus.FORBIDDEN, error);
    }

    private void escribirRespuesta(
            HttpServletResponse response,
            HttpStatus status,
            ErrorResponse error) throws IOException {

        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        jsonMapper.writeValue(response.getWriter(), error);
    }
}