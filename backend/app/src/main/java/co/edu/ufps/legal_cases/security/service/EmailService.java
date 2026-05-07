package co.edu.ufps.legal_cases.security.service;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import co.edu.ufps.legal_cases.exception.BusinessException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final EmailTemplateService emailTemplateService;
    private final String from;
    private final String fromName;

    public EmailService(
            JavaMailSender mailSender,
            EmailTemplateService emailTemplateService,
            @Value("${spring.mail.username}") String from,
            @Value("${app.mail.from-name}") String fromName) {
        this.mailSender = mailSender;
        this.emailTemplateService = emailTemplateService;
        this.from = from;
        this.fromName = fromName;
    }

    public void enviarRecuperacionPassword(String destinatario, String enlace) {
        try {
            log.info("Intentando enviar correo de recuperación a {}", mascararCorreo(destinatario));

            MimeMessage mensaje = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mensaje,
                    false,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(destinatario);
            helper.setSubject("Recuperación de contraseña");

            helper.setFrom(new InternetAddress(
                    from,
                    fromName,
                    StandardCharsets.UTF_8.name()
            ));

            String html = emailTemplateService.construirRecuperacionPassword(enlace);

            helper.setText(html, true);

            mailSender.send(mensaje);

            log.info("Correo de recuperación enviado correctamente a {}", mascararCorreo(destinatario));

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