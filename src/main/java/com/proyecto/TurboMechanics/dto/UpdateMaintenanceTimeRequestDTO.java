package com.proyecto.TurboMechanics.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateMaintenanceTimeRequestDTO {

    /**id del la orden de trabajo */
    @NotNull(message = "El id de la orden es obligatorio")
    @Positive(message = "El id debe ser mayor a 0")
    private Long workOrderId;

    /**fehca estimada de entrega */
    @NotNull(message = "La fecha estimada es obligatoria")
    private LocalDate estimatedDelivery;
}