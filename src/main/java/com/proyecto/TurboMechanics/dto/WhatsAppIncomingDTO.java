package com.proyecto.TurboMechanics.dto;

import lombok.Data;

@Data
public class WhatsAppIncomingDTO {

    /**id de la sesion */
    private String sessionId;

    /** numero remitente */
    private String from;

    /**tipo de mensaje*/
    private String messageType;

    /**contenido del mensaje */
    private String body;

    /**marca de tiempo */
    private Long timestamp;
}