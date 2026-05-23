package co.edu.ufps.legal_cases.common.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        // Maneja las excepciones personalizadas de los service
        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ErrorResponse> manejarBusinessException(
                        BusinessException ex,
                        HttpServletRequest request) {

                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Error de negocio",
                                ex.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.badRequest().body(error);
        }

        // Maneja los errores por validaciones en campos de entrada como el @Valid, @Email, etc
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> manejarErroresValidacion(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                Map<String, String> errores = new HashMap<>();

                ex.getBindingResult().getFieldErrors()
                                .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));

                Map<String, Object> respuesta = new HashMap<>();
                respuesta.put("fecha", LocalDateTime.now());
                respuesta.put("estado", HttpStatus.BAD_REQUEST.value());
                respuesta.put("error", "Error de validacion");
                respuesta.put("mensaje", "Uno o mas campos no son validos");
                respuesta.put("ruta", request.getRequestURI());
                respuesta.put("detalles", errores);

                return ResponseEntity.badRequest().body(respuesta);
        }

        // Maneja parametros con tipo invalido, por ejemplo:
        // ?id=abc, ?activo=texto, ?estado=valor_no_valido
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> manejarParametroInvalido(
                        MethodArgumentTypeMismatchException ex,
                        HttpServletRequest request) {

                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Solicitud invalida",
                                construirMensajeParametroInvalido(ex),
                                request.getRequestURI());

                return ResponseEntity.badRequest().body(error);
        }

        // Maneja parametros obligatorios que no fueron enviados
        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ErrorResponse> manejarParametroObligatorioFaltante(
                        MissingServletRequestParameterException ex,
                        HttpServletRequest request) {

                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Solicitud invalida",
                                "El parametro obligatorio '" + ex.getParameterName() + "' no fue enviado",
                                request.getRequestURI());

                return ResponseEntity.badRequest().body(error);
        }

        // Maneja errores en el cuerpo JSON, por ejemplo enums invalidos o JSON mal formado
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> manejarCuerpoNoValido(
                        HttpMessageNotReadableException ex,
                        HttpServletRequest request) {

                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Solicitud invalida",
                                "El cuerpo de la solicitud no es valido",
                                request.getRequestURI());

                return ResponseEntity.badRequest().body(error);
        }

        // Maneja cuando el usuario inicio sesion pero no tiene permisos para acceder al recurso
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> manejarAccessDeniedException(
                        AccessDeniedException ex,
                        HttpServletRequest request) {

                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.FORBIDDEN.value(),
                                "No autorizado",
                                "No tiene permisos para acceder a este recurso",
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // Para otras excepciones no manejadas
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> manejarExceptionGeneral(
                        Exception ex,
                        HttpServletRequest request) {

                log.error("Error no controlado en la ruta {}", request.getRequestURI(), ex);

                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Error interno del servidor",
                                "Ocurrio un error inesperado",
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        private String construirMensajeParametroInvalido(MethodArgumentTypeMismatchException ex) {
                String nombreParametro = ex.getName();

                if (nombreParametro == null || nombreParametro.isBlank()) {
                        return "Uno de los parametros enviados no es valido";
                }

                return "El valor enviado para el parametro '" + nombreParametro + "' no es valido";
        }
}