package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelWorkOrderRequestDTO {
    
    /** El motivo de la cancelación (obligatorio) */
    @NotBlank(message = "El motivo de cancelación es obligatorio")
    private String cancellationreason;
}
