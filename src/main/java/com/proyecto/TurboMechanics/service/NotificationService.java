package com.proyecto.TurboMechanics.service;

import com.proyecto.TurboMechanics.config.WhatsAppConfig;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    private final RestTemplate   restTemplate;

    private final WhatsAppConfig whatsAppConfig;

    /**
     * Enviar email.
     * 
     * @param addressee destinatario
     * @param item      asunto
     * @param pdf       archivo PDF
     */
    public void SendEmail(String addressee, String item, byte[] pdf) {
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
     * Enviar presupuesto html
     * @param addressee destinatario
     * @param item asunto
     * @param html html del correo
     */
    public void SendEmailHtml(String addressee, String item, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
            helper.setTo(addressee);
            helper.setSubject(item);
            helper.setText(html, true); 
            mailSender.send(msg);
            log.info("Presupuesto HTML enviado por email a {}", addressee);
        } catch (MessagingException e) {
            log.error("Error enviando email HTML a {}: {}", addressee, e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo HTML", e);
        }
    }

    /**
     * Enviar PDF por WhatsApp 
     *
     * @param phone número del cliente 
     * @param pdf   bytes del PDF generado por PdfGeneratorService
     */
    public void sendWhatsapp(String phone, byte[] pdf) {
        try {
            String sessionId = whatsAppConfig.getDefaultSession();
            String caption   = "🔧 *TurboMechanics* - Su comprobante de pago está listo.\n" +
                               "Gracias por preferirnos. 🚗";

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("sessionId", sessionId);
            body.add("to",        normalizePhone(phone));
            body.add("caption",   caption);
            body.add("file", new ByteArrayResource(pdf) {
                @Override public String getFilename() { return "comprobante.pdf"; }
            });

            HttpHeaders headers = multipartHeaders();
            restTemplate.postForEntity(
                whatsAppConfig.getServiceUrl() + "/api/messages/pdf",
                new HttpEntity<>(body, headers),
                Map.class
            );
            log.info("[WA] Comprobante PDF enviado a {}", phone);

        } catch (Exception e) {
            log.error("[WA] Error enviando PDF a {}: {}", phone, e.getMessage());
            throw new RuntimeException("No se pudo enviar el PDF por WhatsApp", e);
        }
    }

    /**
     * Enviar texto por WhatsApp 
     *
     * @param phone   número del cliente
     * @param message texto del mensaje 
     */
    public void SendWhatsappText(String phone, String message) {
        try {
            String sessionId = whatsAppConfig.getDefaultSession();

            Map<String, String> body = Map.of(
                "sessionId", sessionId,
                "to",        normalizePhone(phone),
                "message",   message
            );

            restTemplate.postForEntity(
                whatsAppConfig.getServiceUrl() + "/api/messages/text",
                new HttpEntity<>(body, jsonHeaders()),
                Map.class
            );
            log.info("[WA] Texto enviado a {}", phone);

        } catch (Exception e) {
            log.error("[WA] Error enviando texto a {}: {}", phone, e.getMessage());
            throw new RuntimeException("No se pudo enviar el mensaje de WhatsApp", e);
        }
    }

    /**
     * Envía presupuesto por WhatsApp con opciones de aprobar/rechazar.
     *
     * @param phone       teléfono del cliente
     * @param clientName  nombre del cliente
     * @param plate       placa del vehículo
     * @param total       monto total del presupuesto
     * @param estimateId  ID del presupuesto en BD
     */
    public void sendEstimateWithButtons(String phone, String clientName,String plate, String total,Long estimateId) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("sessionId",   whatsAppConfig.getDefaultSession());
            body.put("to",          normalizePhone(phone));
            body.put("clientName",  clientName);
            body.put("plate",       plate);
            body.put("total",       total);
            body.put("estimateId",  estimateId);

            restTemplate.postForEntity(
                whatsAppConfig.getServiceUrl() + "/api/messages/estimate",
                new HttpEntity<>(body, jsonHeaders()),
                Map.class
            );
            log.info("[WA] Presupuesto #{} con botones enviado a {}", estimateId, phone);
        } catch (Exception e) {
            log.error("[WA] Error enviando presupuesto con botones a {}: {}", phone, e.getMessage());
            throw new RuntimeException("No se pudo enviar el presupuesto por WhatsApp", e);
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

    /**
     * Normaliza un número de teléfono colombiano al formato internacional
     * @param phone numero de telefono
     * @return retorna el nuemro con prefijo del pais
     */
    private String normalizePhone(String phone) {
        String clean = phone.replaceAll("[^0-9]", "");
        if (!clean.startsWith("57")) clean = "57" + clean;
        return clean;
    }

    /**
     * Construye los headers HTTP para peticiones JSON al WhatsApp,
     * @return retorna los headers y token de servicio
     */
    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("x-service-token", whatsAppConfig.getServiceToken());
        return h;
    }

    /**
     * Construye los headers HTTP para peticiones multipart a WhatsApp.
     * @return retorna headers y token de servicio
     */
    private HttpHeaders multipartHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.MULTIPART_FORM_DATA);
        h.set("x-service-token", whatsAppConfig.getServiceToken());
        return h;
    }
}