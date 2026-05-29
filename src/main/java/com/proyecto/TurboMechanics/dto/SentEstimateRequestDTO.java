package com.proyecto.TurboMechanics.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SentEstimateRequestDTO {
    
    /** orden de trabajo id */
    @NotNull(message = "El id de la orden de trabajo es obligatorio")
    @Positive(message = "El id de la orden de trabajo debe ser mayor a 0")
    private Long workOrderId;

    /** Documento de identidad del cliente */
    @NotNull(message = "La identificación es obligatoria")
    @Positive(message = "La identificación debe ser válida")
    private Integer identification;

    /** Placa del vehículo */
    @NotBlank(message = "La placa es obligatoria")
    private String plate;

    /**
     * JSON con el detalle del presupuesto:
     * repuestos, mano de obra, tiempos estimados y costos parciales
     */
    @NotBlank(message = "El detalle del presupuesto es obligatorio")
    private String description;

    /** total estimado */
    @NotNull(message = "El total estimado es obligatorio")
    @DecimalMin(value = "0.0",inclusive = false,message = "El total estimado debe ser mayor a 0")
    private BigDecimal totalEstimate;

    /** Canal de envío: EMAIL | WHATSAPP */
    @NotBlank(message = "El canal es obligatorio")
    private String canal;
}
