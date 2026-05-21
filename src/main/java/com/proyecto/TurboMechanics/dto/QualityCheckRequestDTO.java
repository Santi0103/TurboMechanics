package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class QualityCheckRequestDTO {

    /** Id de la orden de trabajo (obligatorio al iniciar) */
    @NotNull(message = "El id de la orden de trabajo es obligatorio")
    private Long workOrderId;

    /** Observaciones generales del control (opcional) */
    private String observations;

    /** Lista de ítems de verificación actualizados (opcional en creación) */
    private List<QualityCheckItemRequestDTO> items;
}