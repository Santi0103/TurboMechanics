package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SendReminderApponitmentRequestDTO {

    /** Id de la cita a recordar */
    @NotNull(message = "El id de la cita es obligatorio")
    @Positive(message = "El id debe ser mayor a 0")
    private Long appointmentId;

    /** Canal de envío: EMAIL | WHATSAPP */
    @NotBlank(message = "El canal es obligatorio")
    private String canal;
}