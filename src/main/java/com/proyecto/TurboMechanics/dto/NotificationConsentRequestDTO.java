package com.proyecto.TurboMechanics.dto;

import com.proyecto.TurboMechanics.enums.NotificationChannel;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class NotificationConsentRequestDTO {

    /** Cédula del cliente */
    @NotNull(message = "La identificación es obligatoria")
    @Positive(message = "La identificación debe ser válida")
    private Integer identification;

    /** autorizacion de las notificaciones */
    @NotNull(message = "La autorización es obligatoria")
    private Boolean authorized;

    /**Canal de notificación*/
    private NotificationChannel channel;
}
