package com.proyecto.TurboMechanics.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class GeneratedBillRequestDTO {

    /** id de la orden de trabajo */
    @NotNull(message = "El id de la orden de trabajo es obligatorio")
    @Positive(message = "El id de la orden de trabajo debe ser mayor a 0")
    private Long workOrderID;

    /** identificacion del cliente */
    @NotNull(message = "La identificación es obligatoria")
    @Positive(message = "La identificación debe ser válida")
    private Integer identification;

    /**placa del vehiculo */
    @NotBlank(message = "La placa es obligatoria")
    private String plate;

    /**id del metodo de pago
    @NotNull(message = "El método de pago es obligatorio")
    @Positive(message = "El método de pago debe ser válido")
    private Long payMethodId;*/

    /** usuario que creo la factura */
    @NotBlank(message = "El usuario creador es obligatorio")
    private String createdBy;

    /**subtotal de la factura */
    @NotNull(message = "El subtotal es obligatorio")
    @Positive(message = "El subtotal debe ser mayor a 0")
    private BigDecimal subtotal;
}