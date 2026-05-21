package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ReportIssueRequestDTO {

    /**id de la orden de trabajo */
    @NotNull(message = "El id de la orden es obligatorio")
    @Positive(message = "El id debe ser mayor a 0")
    private Long workOrderId;

    /** descripcion del reporte  */
    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    /**mecanico que registro el reporte */
    @NotBlank(message = "El mecánico es obligatorio")
    private String reportedBy;
}
