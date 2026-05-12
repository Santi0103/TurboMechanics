package com.proyecto.TurboMechanics.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final JavaMailSender mailSender;
 
    /**
     * Envía comprobante en PDF al correo del cliente.
     *
     * @param addressee email del cliente
     * @param item       asunto del correo
     * @param pdf          bytes del PDF generado
     */
    public void SentEmail(String addressee, String item, byte[] pdf) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(addressee);
            helper.setSubject(item);
            helper.setText("Adjunto encontrará su comprobante de pago. Gracias por preferirnos.", false);
            helper.addAttachment("comprobante.pdf", new ByteArrayResource(pdf));
            mailSender.send(msg);
            log.info("Comprobante enviado por email a {}", addressee);
        } catch (MessagingException e) {
            log.error("Error enviando email a {}: {}", addressee, e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo", e);
        }
    }
 
    /**
     * Envía comprobante por WhatsApp
     *
     * @param phone número del cliente
     * @param pdf      bytes del PDF
     */
    public void enviarWhatsapp(String phone, byte[] pdf) {
        log.warn("enviarWhatsapp() aún no implementado para teléfono {}", phone);
    }
 
    /**
     * Envía el texto del presupuesto por WhatsApp.
     *
     * @param phone número del cliente
     * @param menssage  texto con detalle del presupuesto
     */
    public void SentWhatsappText(String phone, String menssage) {
        log.warn("enviarWhatsappTexto() aún no implementado para teléfono {}: {}", phone, menssage);
    }
 
    /**
     * Envía el presupuesto por correo como texto plano.
     *
     * @param addressee email del cliente
     * @param item       asunto del correo
     * @param body       texto con detalle del presupuesto
     */
    public void SentEmailText(String addressee, String item, String body) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
            helper.setTo(addressee);
            helper.setSubject(item);
            helper.setText(body, false);
            mailSender.send(msg);
            log.info("Presupuesto enviado por email a {}", addressee);
        } catch (MessagingException e) {
            log.error("Error enviando email de presupuesto a {}: {}", addressee, e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo de presupuesto", e);
        }
    }
}
