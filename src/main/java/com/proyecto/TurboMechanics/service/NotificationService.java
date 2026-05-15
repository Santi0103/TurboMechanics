package com.proyecto.TurboMechanics.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.whatsapp-number}")
    private String whatsappNumber;

    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
        log.info("Twilio inicializado correctamente");
    }

    /**
     * Enviar email.
     * 
     * @param addressee destinatario
     * @param item      asunto
     * @param pdf       archivo PDF
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
     * Enviar mensaje de WhatsApp.
     * 
     * @param phone   número de teléfono
     * @param pdf       archivo PDF
     */
    public void sendtWhatsapp(String phone, byte[] pdf) {
        try {
            String cleaned = phone.replaceAll("[^0-9]", "");
            if (!cleaned.startsWith("57"))
                cleaned = "57" + cleaned;
            String toNumber = "whatsapp:+" + cleaned;

            Message message = Message.creator(
                    new PhoneNumber(toNumber),
                    new PhoneNumber(whatsappNumber),
                    "🔧 *Turbo Mechanics* - Su comprobante de pago ha sido generado. " +
                            "Por favor descárguelo desde el portal o solicítelo al taller. " +
                            "Gracias por preferirnos.")
                    .create();
            log.info("WhatsApp enviado a {} - SID: {}", phone, message.getSid());
        } catch (Exception e) {
            log.error("Error enviando WhatsApp a {}: {}", phone, e.getMessage());
            throw new RuntimeException("No se pudo enviar el mensaje de WhatsApp", e);
        }
    }

    /**
     * Enviar mensaje de WhatsApp.
     * 
     * @param phone   número de teléfono
     * @param menssage mensaje a enviar
     */
    public void SentWhatsappText(String phone, String menssage) {
        try {
            String cleaned = phone.replaceAll("[^0-9]", "");
            if (!cleaned.startsWith("57"))
                cleaned = "57" + cleaned;
            String toNumber = "whatsapp:+" + cleaned;

            Message message = Message.creator(
                    new PhoneNumber(toNumber),
                    new PhoneNumber(whatsappNumber),
                    menssage).create();
            log.info("WhatsApp enviado a {} - SID: {}", phone, message.getSid());
        } catch (Exception e) {
            log.error("Error enviando WhatsApp a {}: {}", phone, e.getMessage());
            throw new RuntimeException("No se pudo enviar el mensaje de WhatsApp", e);
        }
    }

    /**
     * Enviar email con texto.
     * 
     * @param addressee destinatario
     * @param item      asunto
     * @param body      cuerpo del email
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