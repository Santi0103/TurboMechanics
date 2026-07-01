package com.proyecto.TurboMechanics.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Envía el código de recuperación por correo de forma asíncrona.
     * Si falla, queda registrado en logs pero no rompe el flujo del usuario
     * (el código ya quedó guardado en BD y puede reenviarse).
     */
    @Async
    public void sendResetCodeEmail(String toEmail, String code, int expiryMinutes) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Turbo Mechanics - Código de recuperación de contraseña");
            message.setText(
                "Hola,\n\n" +
                "Recibimos una solicitud para restablecer tu contraseña en Turbo Mechanics.\n\n" +
                "Tu código de recuperación es: " + code + "\n\n" +
                "Este código es válido por " + expiryMinutes + " minutos.\n\n" +
                "Si no solicitaste este cambio, ignora este mensaje.\n\n" +
                "Turbo Mechanics"
            );

            log.info("Enviando código de recuperación a {}", toEmail);
            mailSender.send(message);
            log.info("Correo de recuperación enviado correctamente a {}", toEmail);

        } catch (MailException e) {
            log.error("Error enviando correo de recuperación a {}: {}", toEmail, e.getMessage(), e);
        }
    }
}