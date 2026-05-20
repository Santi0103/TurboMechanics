package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CloseWarrantyRequestDTO {

    /** Motivo del cierre de la garantía (obligatorio) */
    @NotBlank(message = "El motivo de cierre es obligatorio")
    private String closureReason;
}