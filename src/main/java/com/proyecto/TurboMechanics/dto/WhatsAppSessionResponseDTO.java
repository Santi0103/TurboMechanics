package com.proyecto.TurboMechanics.dto;

import lombok.Data;

@Data
public class WhatsAppSessionResponseDTO {

    /**estado de la sesion de whatsapp */
    private String status; 

    /**qr de whatsapp */
    private String qr;     
}