package co.edu.ufps.legal_cases.security.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import co.edu.ufps.legal_cases.exception.BusinessException;

// Servicio para mandar correos
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String from;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    // El destinatario es el correo 
    public void enviarRecuperacionPassword(String destinatario, String enlace) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(from);
            mensaje.setTo(destinatario);
            mensaje.setSubject("Recuperación de contraseña");
            mensaje.setText("""
                    Hola,

                    Recibimos una solicitud para restablecer tu contraseña.

                    Ingresa al siguiente enlace para crear una nueva contraseña:

                    %s

                    Si no solicitaste este cambio, puedes ignorar este mensaje.

                    Este enlace expirará pronto.
                    """.formatted(enlace));

            mailSender.send(mensaje);
        } catch (Exception ex) {
            throw new BusinessException("No se pudo enviar el correo de recuperación");
        }
    }
}