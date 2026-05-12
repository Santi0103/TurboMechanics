package com.proyecto.TurboMechanics.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class GeneratedBillRequestDTO {

    @NotNull(message = "El id de la orden de trabajo es obligatorio")
    @Positive(message = "El id de la orden de trabajo debe ser mayor a 0")
    private Long workOrderID;

    @NotNull(message = "La identificación es obligatoria")
    @Positive(message = "La identificación debe ser válida")
    private Integer identification;

    @NotBlank(message = "La placa es obligatoria")
    private String plate;

    @NotNull(message = "El método de pago es obligatorio")
    @Positive(message = "El método de pago debe ser válido")
    private Long payMethodId;

    @NotBlank(message = "El usuario creador es obligatorio")
    private String createdBy;

    @NotNull(message = "El subtotal es obligatorio")
    @Positive(message = "El subtotal debe ser mayor a 0")
    private BigDecimal subtotal;
}