package com.proyecto.TurboMechanics.dto;

import java.math.BigDecimal;

import com.proyecto.TurboMechanics.enums.MovementConcept;
import com.proyecto.TurboMechanics.enums.MovementType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterMovementRequestDTO {
    
    /** Tipo de movimiento */
    @NotNull(message = "El tipo de movimiento es obligatorio")
    private MovementType type;

    /** Concepto de movimiento */
    @NotNull(message = "El concepto del movimiento es obligatorio")
    private MovementConcept concept;

    /** Descripción del movimiento */
    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String description;

    /** Monto del movimiento */
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false,message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    /** Id de la factura */
    private Long billId;

    /** Documento del mecánico o admin que registra */
    @NotNull(message = "La identificación del registrador es obligatoria")
    @Positive(message = "La identificación debe ser válida")
    private Long registerByIdentification;
}
