package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignOrderRequestDTO {

    /** Documento del mecánico al que se desea asignar la orden */
    @NotNull(message = "El documento del mecánico es obligatorio")
    private Long mechanicDocument;
}