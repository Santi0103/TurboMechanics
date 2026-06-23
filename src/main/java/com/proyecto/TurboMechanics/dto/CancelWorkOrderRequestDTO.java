package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelWorkOrderRequestDTO {

    /** El motivo de la cancelación (obligatorio, debe ser texto explicativo, no solo números) */
    @NotBlank(message = "El motivo de cancelación es obligatorio")
    @Size(min = 5, max = 300, message = "El motivo debe tener entre 5 y 300 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-zÁÉÍÓÚÑáéíóúñ]).+$", message = "El motivo debe ser un texto explicativo, no solo números o símbolos")
    private String cancellationreason;
}