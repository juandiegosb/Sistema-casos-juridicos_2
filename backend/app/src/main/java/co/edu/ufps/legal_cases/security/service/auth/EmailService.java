package co.edu.ufps.legal_cases.security.service.auth;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import co.edu.ufps.legal_cases.exception.BusinessException;
import tools.jackson.databind.json.JsonMapper;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private static final URI BREVO_EMAIL_URI = URI.create("https://api.brevo.com/v3/smtp/email");

    private final EmailTemplateService emailTemplateService;
    private final JsonMapper jsonMapper;
    private final HttpClient httpClient;

    private final String brevoApiKey;
    private final String fromEmail;
    private final String fromName;

    public EmailService(
            EmailTemplateService emailTemplateService,
            JsonMapper jsonMapper,
            @Value("${brevo.api-key:}") String brevoApiKey,
            @Value("${app.mail.from-email}") String fromEmail,
            @Value("${app.mail.from-name}") String fromName) {

        this.emailTemplateService = emailTemplateService;
        this.jsonMapper = jsonMapper;
        this.brevoApiKey = brevoApiKey;
        this.fromEmail = fromEmail;
        this.fromName = fromName;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public void enviarRecuperacionPassword(String destinatario, String enlace) {
        try {
            validarConfiguracion();

            log.info("Intentando enviar correo de recuperación por Brevo a {}", mascararCorreo(destinatario));

            String html = emailTemplateService.construirRecuperacionPassword(enlace);

            Map<String, Object> payload = Map.of(
                    "sender", Map.of(
                            "name", fromName,
                            "email", fromEmail
                    ),
                    "to", List.of(
                            Map.of("email", destinatario)
                    ),
                    "subject", "Recuperación de contraseña",
                    "htmlContent", html
            );

            String json = jsonMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(BREVO_EMAIL_URI)
                    .timeout(Duration.ofSeconds(20))
                    .header("api-key", brevoApiKey)
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error(
                        "Brevo no pudo enviar el correo. Status: {}. Body: {}",
                        response.statusCode(),
                        response.body()
                );

                throw new BusinessException("No se pudo enviar el correo de recuperación");
            }

            log.info("Correo de recuperación enviado correctamente por Brevo a {}", mascararCorreo(destinatario));

        } catch (BusinessException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();

            log.error(
                    "Envío de correo interrumpido para {}",
                    mascararCorreo(destinatario),
                    ex
            );

            throw new BusinessException("No se pudo enviar el correo de recuperación");
        } catch (Exception ex) {
            log.error(
                    "Error enviando correo de recuperación a {}. Tipo error: {}. Mensaje: {}",
                    mascararCorreo(destinatario),
                    ex.getClass().getName(),
                    ex.getMessage(),
                    ex
            );

            throw new BusinessException("No se pudo enviar el correo de recuperación");
        }
    }

    private void validarConfiguracion() {
        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            log.error("No está configurada la variable BREVO_API_KEY");
            throw new BusinessException("No se pudo enviar el correo de recuperación");
        }

        if (fromEmail == null || fromEmail.isBlank()) {
            log.error("No está configurada la variable MAIL_FROM_EMAIL");
            throw new BusinessException("No se pudo enviar el correo de recuperación");
        }
    }

    private String mascararCorreo(String correo) {
        if (correo == null || correo.isBlank() || !correo.contains("@")) {
            return "correo-no-valido";
        }

        String[] partes = correo.split("@", 2);
        String nombre = partes[0];
        String dominio = partes[1];

        if (nombre.length() <= 2) {
            return "***@" + dominio;
        }

        return nombre.charAt(0) + "***" + nombre.charAt(nombre.length() - 1) + "@" + dominio;
    }
}