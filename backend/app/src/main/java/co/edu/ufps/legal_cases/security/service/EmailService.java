package co.edu.ufps.legal_cases.security.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import co.edu.ufps.legal_cases.exception.BusinessException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

// Servicio para mandar correos
@Service
public class EmailService {

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

    // El destinatario es el correo 
    public void enviarRecuperacionPassword(String destinatario, String enlace) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage(); // Para enviar correos con HTML, estilos, imágenes, adjuntos, etc.

            MimeMessageHelper helper = new MimeMessageHelper(
                    mensaje,
                    false,  // Se desactiva porque no envio adjuntos
                    StandardCharsets.UTF_8.name()); // Para que soporte tildes y demas

            //Parametros del envio del correo
            helper.setTo(destinatario);
            helper.setSubject("Recuperación de contraseña");
            helper.setFrom(new InternetAddress(from, fromName));

            String html = emailTemplateService.construirRecuperacionPassword(enlace);   // Este servicio es el que construye el html del correo para que lusca mejor

            helper.setText(html, true); // Pone el html en el cuerpo del correo

            mailSender.send(mensaje);   // Envia el correo
        } catch (Exception ex) {
            throw new BusinessException("No se pudo enviar el correo de recuperación");
        }
    }
}