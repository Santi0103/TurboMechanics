package com.proyecto.TurboMechanics.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MercadoPagoWebhookDTO {

    /** Tipo de notificación: "payment" | "merchant_order" */
    private String type;

    /** Acción: "payment.created" | "payment.updated" */
    private String action;

    /** Datos del evento */
    private WebhookData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookData {
        /** Id del recurso (id del pago si type="payment") */
        private String id;
    }
}